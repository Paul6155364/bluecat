package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluecat.common.enums.TaskStatus;
import com.bluecat.common.enums.TaskType;
import com.bluecat.config.BusinessException;
import com.bluecat.entity.*;
import com.bluecat.mapper.*;
import com.bluecat.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 蓝老板API服务实现
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LaobanApiServiceImpl implements LaobanApiService {

    @Value("${bluecat.api.base-url-user}")
    private String baseUrlUser;

    @Value("${bluecat.api.base-url-book}")
    private String baseUrlBook;

    @Value("${bluecat.api.default-headers.Host}")
    private String defaultHost;

    @Value("${bluecat.api.default-headers.X-Reuqest-Appid}")
    private String defaultAppId;

    private static final String APP_SOURCE = "pc";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ShopConfigService shopConfigService;
    private final ShopInfoService shopInfoService;
    private final ShopAreaService shopAreaService;
    private final MachineInfoService machineInfoService;
    private final DataCollectionTaskService dataCollectionTaskService;
    private final ShopStatusSnapshotService shopStatusSnapshotService;
    private final AreaStatusSnapshotService areaStatusSnapshotService;
    private final MachineStatusHistoryService machineStatusHistoryService;
    private final ApiCallLogService apiCallLogService;
    private final ShopImageService shopImageService;

    private final ShopInfoMapper shopInfoMapper;
    private final ShopAreaMapper shopAreaMapper;
    private final MachineInfoMapper machineInfoMapper;
    private final ShopImageMapper shopImageMapper;
    
    private final TransactionTemplate transactionTemplate;
    
    /**
     * 自注入，用于调用@Async方法（同类内部调用需要通过代理）
     */
    @Autowired
    @Lazy
    private LaobanApiService self;

    @Override
    public Map<String, Object> testToken(ShopConfig config) {
        String url = baseUrlUser + "/getSnbid";
        return callApiGet(config, url, "testToken", null);
    }

    @Override
    public String refreshToken(ShopConfig config) {
        // 刷新Token的API地址
        String url = "https://api.wxdesk.com/api-user/client/user_c/wechatmp/refresh-token";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Host", "api.wxdesk.com");
            headers.set("X-Reuqest-Appid", "2AXEDBKW3NRC");
            headers.set("Authorization", "Bearer " + config.getJwtToken());
            headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 NetType/WIFI MicroMessenger/7.0.20.1781(0x6700143B) MacWechat/3.8.7(0x13080712) UnifiedPCMacWechat(0xf2641702) XWEB/18788 Flue");
            headers.set("Referer", "https://xmp.54laoban.cn/");
            headers.set("Origin", "https://xmp.54laoban.cn");

            // 构造带参数的URL
            String urlWithParams = url + "?appId=" + config.getAppId();

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(null, headers);
            ResponseEntity<Map> response = restTemplate.exchange(urlWithParams, HttpMethod.GET, entity, Map.class);

            Map<String, Object> body = response.getBody();
            if (body != null && Boolean.TRUE.equals(body.get("success"))) {
                String newToken = (String) body.get("data");
                log.info("Token刷新成功: configId={}", config.getId());
                return newToken;
            } else {
                log.warn("Token刷新失败: configId={}, response={}", config.getId(), body);
                return null;
            }
        } catch (Exception e) {
            log.error("Token刷新异常: configId={}", config.getId(), e);
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getShopList(ShopConfig config) {
        // Python: get_snbid_list - GET /getSnbidList
        String url = baseUrlUser + "/getSnbidList";
        Map<String, Object> response = callApiGet(config, url, "getSnbidList", null);

        if (response != null && response.containsKey("result")) {
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("result");
            return dataList != null ? dataList : new ArrayList<>();
        }
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getMachineInfo(ShopConfig config, Long shopId, String snbid) {
        // Python: get_area_com_set_info - GET /get-area-com-set-info
        String url = baseUrlBook + "/get-area-com-set-info";
        Map<String, Object> response = callApiGet(config, url, "get-area-com-set-info", shopId, snbid);

        if (response != null && response.containsKey("result")) {
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("result");
            return dataList != null ? dataList : new ArrayList<>();
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getShopStatus(ShopConfig config, Long shopId, String snbid) {
        // 此方法已被 collectShopData 内部的多步骤流程替代
        // 保留接口兼容性，返回空结果
        return new HashMap<>();
    }

    @Override
    @Async("collectionExecutor")
    public void executeCollection(Long configId) {
        log.info("开始异步采集任务: configId={}", configId);
        
        ShopConfig config = shopConfigService.getById(configId);
        if (config == null || config.getStatus() != 1) {
            log.warn("配置不存在或已禁用: {}", configId);
            return;
        }

        // 智能刷新Token：检查Token是否快过期，如果快过期则自动刷新
        if (shouldRefreshToken(config)) {
            log.info("Token即将过期或已过期，尝试自动刷新: configId={}", configId);
            String newToken = refreshToken(config);
            if (newToken != null && !newToken.isEmpty()) {
                config.setJwtToken(newToken);
                config.setTokenExpireTime(LocalDateTime.now().plusHours(1));
                shopConfigService.updateById(config);
                log.info("Token自动刷新成功: configId={}", configId);
            } else {
                log.warn("Token自动刷新失败: configId={}", configId);
            }
        }

        LocalDateTime startTime = LocalDateTime.now();
        DataCollectionTask task = createTask(configId, null, TaskType.SHOP_LIST.name());

        try {
            // 1. 获取门店列表（独立事务）
            List<Map<String, Object>> shopList = getShopList(config);
            saveShopListInTransaction(config, shopList);

            // 2. 遍历门店获取机器信息和状态（每个门店独立事务）
            List<ShopInfo> shops = shopInfoService.listByConfigId(configId);
            for (ShopInfo shop : shops) {
                collectShopDataInTransaction(config, shop);
                // 随机延迟1-3秒，模拟真人操作，避免触发反爬
                randomDelay(1000, 3000);
            }

            finishTask(task, TaskStatus.SUCCESS, startTime);
            log.info("采集任务完成: configId={}", configId);
        } catch (Exception e) {
            log.error("数据采集失败: configId={}", configId, e);
            finishTask(task, TaskStatus.FAILED, startTime, e.getMessage());
        }
    }

    @Override
    @Async("collectionExecutor")
    public void executeAllCollection() {
        log.info("开始执行所有采集任务");
        List<ShopConfig> configs = shopConfigService.listEnabled();
        for (ShopConfig config : configs) {
            try {
                // 通过代理调用，使@Async生效
                self.executeCollection(config.getId());
                // 每个配置之间延迟3-5秒
                randomDelay(3000, 5000);
            } catch (Exception e) {
                log.error("采集失败: configId={}", config.getId(), e);
            }
        }
    }
    
    /**
     * 在独立事务中保存门店列表
     */
    private void saveShopListInTransaction(ShopConfig config, List<Map<String, Object>> shopList) {
        transactionTemplate.executeWithoutResult(status -> {
            saveShopList(config, shopList);
        });
    }
    
    /**
     * 在独立事务中采集单个门店数据
     */
    private void collectShopDataInTransaction(ShopConfig config, ShopInfo shop) {
        transactionTemplate.executeWithoutResult(status -> {
            collectShopData(config, shop);
        });
    }

    private void collectShopData(ShopConfig config, ShopInfo shop) {
        LocalDateTime startTime = LocalDateTime.now();
        DataCollectionTask task = createTask(config.getId(), shop.getId(), TaskType.MACHINE_STATUS.name());

        try {
            String snbid = shop.getSnbid();

            // 1. 获取机器信息
            List<Map<String, Object>> machineList = getMachineInfo(config, shop.getId(), snbid);
            saveMachineInfo(shop.getId(), machineList);

            // 2. 获取服务器时间戳
            Long currentTs = getServerCurrentSeconds(config, shop.getId(), snbid);
            if (currentTs == null) {
                log.warn("获取服务器时间失败，跳过状态采集: shopId={}", shop.getId());
                finishTask(task, TaskStatus.SUCCESS, startTime);
                return;
            }

            // 3. 创建预订查询订单 (bookTime = 当前时间 + 随机59-60分钟)
            int randomOffset = 3540 + (int) (Math.random() * 60); // 59-60分钟随机
            Long bookTime = currentTs + randomOffset;
            String orderId = createBookOrder(config, shop.getId(), snbid, bookTime);
            if (orderId == null) {
                log.warn("创建预订订单失败，跳过状态采集: shopId={}", shop.getId());
                finishTask(task, TaskStatus.SUCCESS, startTime);
                return;
            }

            // 4. 轮询获取预订查询结果 (包含空闲机器列表)
            Map<String, Object> statusData = pollBookOrderResult(config, shop.getId(), snbid, orderId);

            // 5. 保存状态数据
            saveShopStatus(shop.getId(), task.getId(), statusData);

            finishTask(task, TaskStatus.SUCCESS, startTime);
        } catch (Exception e) {
            log.error("门店数据采集失败: shopId={}", shop.getId(), e);
            finishTask(task, TaskStatus.FAILED, startTime, e.getMessage());
        }
    }

    /**
     * 获取服务器当前时间戳
     */
    private Long getServerCurrentSeconds(ShopConfig config, Long shopId, String snbid) {
        String url = baseUrlBook + "/get-sys-current-seconds";
        Map<String, Object> response = callApiGet(config, url, "get-sys-current-seconds", shopId, snbid);
        if (response != null && response.containsKey("result")) {
            Object result = response.get("result");
            if (result instanceof Number) {
                return ((Number) result).longValue();
            }
        }
        return null;
    }

    /**
     * 创建预订查询订单
     */
    private String createBookOrder(ShopConfig config, Long shopId, String snbid, Long bookTime) {
        String url = baseUrlBook + "/get-book-area-com-info";
        Map<String, Object> body = new HashMap<>();
        body.put("bookTime", bookTime);
        body.put("continuation", 0);
        body.put("seatNum", 1);

        Map<String, Object> response = callApi(config, url, body, "get-book-area-com-info", shopId, snbid, HttpMethod.POST);
        if (response != null && response.containsKey("result")) {
            Map<String, Object> result = (Map<String, Object>) response.get("result");
            if (result != null && result.containsKey("generalOrderId")) {
                return (String) result.get("generalOrderId");
            }
        }
        return null;
    }

    /**
     * 轮询获取预订查询结果 (最多重试10次)
     */
    private Map<String, Object> pollBookOrderResult(ShopConfig config, Long shopId, String snbid, String orderId) {
        String url = baseUrlBook + "/general-order-book-area-com-result";
        Map<String, Object> body = new HashMap<>();
        body.put("generalOrderId", orderId);
        body.put("orderType", 19);

        int maxRetries = 10;
        long retryDelayMs = 1500;

        for (int i = 0; i < maxRetries; i++) {
            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            Map<String, Object> response = callApi(config, url, body, "general-order-book-area-com-result", shopId, snbid, HttpMethod.POST);
            if (response != null && response.containsKey("result")) {
                Map<String, Object> result = (Map<String, Object>) response.get("result");
                if (result != null && result.containsKey("areaComList")) {
                    log.info("获取空闲机器数据成功: shopId={}, retry={}", shopId, i + 1);
                    return result;
                }
            }
            log.debug("轮询空闲机器数据: shopId={}, retry={}", shopId, i + 1);
        }

        log.warn("轮询空闲机器数据超时: shopId={}", shopId);
        return new HashMap<>();
    }

    private void saveShopList(ShopConfig config, List<Map<String, Object>> shopList) {
        for (Map<String, Object> shopData : shopList) {
            String snbid = String.valueOf(shopData.get("snbid"));

            ShopInfo shopInfo = shopInfoService.getBySnbid(snbid);
            if (shopInfo == null) {
                shopInfo = new ShopInfo();
                shopInfo.setConfigId(config.getId());
                shopInfo.setSnbid(snbid);
            }

            // 映射字段
            mapShopInfo(shopInfo, shopData);
            shopInfo.setRawJson(shopData);

            if (shopInfo.getId() == null) {
                shopInfoService.save(shopInfo);
            } else {
                shopInfoService.updateById(shopInfo);
            }

            // 保存图片
            saveShopImages(shopInfo.getId(), shopData);
        }
    }

    private void mapShopInfo(ShopInfo shop, Map<String, Object> data) {
        shop.setName(getString(data, "name"));
        shop.setSnbName(getString(data, "snbName"));
        shop.setLoginProvince(getString(data, "loginProvince"));
        shop.setLoginCity(getString(data, "loginCity"));
        shop.setLoginZone(getString(data, "loginZone"));
        shop.setLoginAddress(getString(data, "loginAddress"));
        shop.setAddress(getString(data, "address"));
        shop.setProvinceName(getString(data, "provinceName"));
        shop.setCityName(getString(data, "cityName"));
        shop.setZoneName(getString(data, "zoneName"));
        shop.setStel(getString(data, "stel"));
        shop.setSbossTel(getString(data, "sbosstel"));
        shop.setNetMobile(getString(data, "netMobile"));
        shop.setLbPhone(getString(data, "lbPhone"));
        shop.setQq(getString(data, "qq"));
        shop.setWifiName(getString(data, "wifiName"));
        shop.setWifiPwd(getString(data, "wifiPwd"));
        shop.setHead(getString(data, "head"));
        shop.setLongitude(getBigDecimal(data, "longitude"));
        shop.setLatitude(getBigDecimal(data, "latitude"));
        shop.setLongitudeTencent(getBigDecimal(data, "longitudeTencent"));
        shop.setLatitudeTencent(getBigDecimal(data, "latitudeTencent"));
        shop.setRegionBiduId(getInteger(data, "regionBiduId"));
        shop.setProvinceNameId(getInteger(data, "provinceNameId"));
        shop.setCityNameId(getInteger(data, "cityNameId"));
        shop.setHaveRoom(getInteger(data, "haveRoom"));
        shop.setHaveBookSeat(getInteger(data, "haveBookSeat"));
        shop.setHaveOdeNewActivity(getInteger(data, "haveOdeNewActivity"));
        shop.setStatus(getInteger(data, "status"));
        shop.setHaveOpenLeaseBenefit(getInteger(data, "haveOpenLeaseBenefit"));
        shop.setHaveOpenCouponBenefit(getInteger(data, "haveOpenCouponBenefit"));
        shop.setShopId(getLong(data, "shopId"));
    }

    private void saveShopImages(Long shopId, Map<String, Object> data) {
        List<Map<String, Object>> pics = (List<Map<String, Object>>) data.get("meowBarImages");
        if (pics == null || pics.isEmpty()) return;

        // 先删除该门店的旧图片
        shopImageMapper.delete(new LambdaQueryWrapper<ShopImage>()
                .eq(ShopImage::getShopId, shopId));

        // 批量插入新图片
        List<ShopImage> imageList = new ArrayList<>();
        for (Map<String, Object> pic : pics) {
            ShopImage image = new ShopImage();
            image.setShopId(shopId);
            image.setPicId(getLong(pic, "picId"));
            image.setPicUrl(getString(pic, "picUrl"));
            image.setCover(getInteger(pic, "cover"));
            image.setSystemPic(getString(pic, "systemPic"));
            image.setPicState(getInteger(pic, "picState"));
            imageList.add(image);
        }
        if (!imageList.isEmpty()) {
            shopImageService.saveBatch(imageList);
        }
    }

    private void saveMachineInfo(Long shopId, List<Map<String, Object>> machineList) {
        // API返回的是扁平的机器列表，每个机器包含 area, com, cardId 字段
        for (Map<String, Object> machineData : machineList) {
            String areaName = getString(machineData, "area");  // 字段名是 area
            String comName = getString(machineData, "com");     // 字段名是 com
            String cardId = getString(machineData, "cardId");   // 字段名是 cardId

            // 跳过没有区域名称的数据
            if (areaName == null || areaName.isEmpty()) {
                areaName = "默认区域";
            }
            // 跳过没有机器名称的数据
            if (comName == null || comName.isEmpty()) {
                continue;
            }

            // 查询或创建区域（机器数据只包含区域名称，无其他属性可更新）
            ShopArea area = shopAreaMapper.selectOne(new LambdaQueryWrapper<ShopArea>()
                    .eq(ShopArea::getShopId, shopId)
                    .eq(ShopArea::getAreaName, areaName));
            if (area == null) {
                area = new ShopArea();
                area.setShopId(shopId);
                area.setAreaName(areaName);
                area.setAllow(1);
                shopAreaService.save(area);
            }

            // 保存机器信息
            MachineInfo machine = machineInfoService.getByShopIdAndComName(shopId, comName);
            if (machine == null) {
                machine = new MachineInfo();
                machine.setShopId(shopId);
                machine.setComName(comName);
            }
            machine.setAreaId(area.getId());
            machine.setAreaName(areaName);
            machine.setCardId(cardId);
            machine.setLastOfflineTime(getLocalDateTime(machineData, "lastOfflineTime"));
            machine.setRawJson(machineData);

            if (machine.getId() == null) {
                machineInfoService.save(machine);
            } else {
                machineInfoService.updateById(machine);
            }
        }
    }

    private void saveShopStatus(Long shopId, Long taskId, Map<String, Object> statusData) {
        if (statusData == null || statusData.isEmpty()) {
            return;
        }

        LocalDateTime snapshotTime = LocalDateTime.now();

        // 创建门店快照
        ShopStatusSnapshot snapshot = new ShopStatusSnapshot();
        snapshot.setTaskId(taskId);
        snapshot.setShopId(shopId);
        snapshot.setSnapshotTime(snapshotTime);
        snapshot.setRawJson(statusData);

        // 先保存门店快照以获取ID
        shopStatusSnapshotService.save(snapshot);

        int totalMachines = 0;
        int freeMachines = 0;
        int busyMachines = 0;

        // 解析区域数据 - API返回的是 areaComList，包含各区域的空闲机器列表
        List<Map<String, Object>> areaComList = (List<Map<String, Object>>) statusData.get("areaComList");
        if (areaComList != null) {
            // 统计各区域空闲机器
            Map<String, List<String>> areaFreeMachines = new HashMap<>();
            for (Map<String, Object> areaData : areaComList) {
                String areaName = getString(areaData, "area");
                if (areaName == null) {
                    areaName = getString(areaData, "areaName");
                }
                List<String> comList = (List<String>) areaData.get("comList");
                if (areaName != null && comList != null) {
                    areaFreeMachines.put(areaName, comList);
                    freeMachines += comList.size();
                }
            }

            // 从数据库获取各区域总机器数
            List<ShopArea> areas = shopAreaService.listByShopId(shopId);
            for (ShopArea area : areas) {
                String areaName = area.getAreaName();
                int areaFree = areaFreeMachines.containsKey(areaName) ? areaFreeMachines.get(areaName).size() : 0;

                // 查询该区域的总机器数
                int areaTotal = machineInfoService.countByShopIdAndAreaName(shopId, areaName);
                int areaBusy = areaTotal - areaFree;

                totalMachines += areaTotal;
                busyMachines += areaBusy;

                // 创建区域快照
                AreaStatusSnapshot areaSnapshot = new AreaStatusSnapshot();
                areaSnapshot.setTaskId(taskId);
                areaSnapshot.setShopId(shopId);
                areaSnapshot.setAreaName(areaName);
                areaSnapshot.setTotalMachines(areaTotal);
                areaSnapshot.setFreeMachines(areaFree);
                areaSnapshot.setBusyMachines(areaBusy);
                areaSnapshot.setFreeMachineList(areaFreeMachines.get(areaName));
                areaSnapshot.setSnapshotTime(snapshotTime); // 设置快照时间

                if (areaTotal > 0) {
                    BigDecimal rate = BigDecimal.valueOf(areaBusy)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(areaTotal), 2, RoundingMode.HALF_UP);
                    areaSnapshot.setOccupancyRate(rate);
                }

                areaSnapshot.setSnapshotId(snapshot.getId());
                areaStatusSnapshotService.save(areaSnapshot);

                // 保存机器状态历史
                saveMachineStatusHistoryFromFreeList(snapshot.getId(), taskId, shopId, areaName,
                        areaFreeMachines.get(areaName), areaTotal);
            }
        }

        // 更新门店快照统计
        snapshot.setTotalMachines(totalMachines);
        snapshot.setFreeMachines(freeMachines);
        snapshot.setBusyMachines(busyMachines);
        if (totalMachines > 0) {
            BigDecimal rate = BigDecimal.valueOf(busyMachines)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalMachines), 2, RoundingMode.HALF_UP);
            snapshot.setOccupancyRate(rate);
        }
        shopStatusSnapshotService.updateById(snapshot);
    }

    /**
     * 从空闲机器列表保存机器状态历史
     */
    private void saveMachineStatusHistoryFromFreeList(Long snapshotId, Long taskId, Long shopId, String areaName,
                                                       List<String> freeComList, int areaTotal) {
        Set<String> freeSet = freeComList != null ? new HashSet<>(freeComList) : new HashSet<>();

        // 获取该区域所有机器
        List<MachineInfo> machines = machineInfoService.listByShopIdAndAreaName(shopId, areaName);
        for (MachineInfo machine : machines) {
            MachineStatusHistory history = new MachineStatusHistory();
            history.setSnapshotId(snapshotId);
            history.setTaskId(taskId);
            history.setShopId(shopId);
            history.setMachineId(machine.getId());
            history.setComName(machine.getComName());
            history.setAreaName(areaName);
            // 空闲 = 1, 占用 = 0
            history.setStatus(freeSet.contains(machine.getComName()) ? 1 : 0);
            history.setSnapshotTime(LocalDateTime.now());
            machineStatusHistoryService.save(history);
        }
    }

    private void saveMachineStatusHistory(Long snapshotId, Long taskId, Long shopId, String areaName, Map<String, Object> areaData) {
        List<Map<String, Object>> coms = (List<Map<String, Object>>) areaData.get("com_list");
        if (coms == null) return;

        for (Map<String, Object> com : coms) {
            String comName = getString(com, "com_name");
            Integer status = getInteger(com, "status"); // 1空闲, 0占用

            MachineInfo machine = machineInfoService.getByShopIdAndComName(shopId, comName);

            MachineStatusHistory history = new MachineStatusHistory();
            history.setSnapshotId(snapshotId);
            history.setTaskId(taskId);
            history.setShopId(shopId);
            history.setMachineId(machine != null ? machine.getId() : null);
            history.setComName(comName);
            history.setAreaName(areaName);
            history.setStatus(status);
            history.setSnapshotTime(LocalDateTime.now());

            machineStatusHistoryService.save(history);
        }
    }

    private Map<String, Object> callApi(ShopConfig config, String url, Map<String, Object> body, String apiName, Long shopId) {
        return callApi(config, url, body, apiName, shopId, null, HttpMethod.POST);
    }

    private Map<String, Object> callApiGet(ShopConfig config, String url, String apiName, Long shopId) {
        return callApi(config, url, null, apiName, shopId, null, HttpMethod.GET);
    }

    private Map<String, Object> callApiGet(ShopConfig config, String url, String apiName, Long shopId, String snbid) {
        return callApi(config, url, null, apiName, shopId, snbid, HttpMethod.GET);
    }

    private Map<String, Object> callApi(ShopConfig config, String url, Map<String, Object> body, String apiName, Long shopId, String snbid, HttpMethod method) {
        long start = System.currentTimeMillis();
        ApiCallLog logEntry = new ApiCallLog();
        logEntry.setConfigId(config.getId());
        logEntry.setShopId(shopId);
        logEntry.setApiName(apiName);
        logEntry.setApiUrl(url);
        logEntry.setRequestMethod(method.name());
        logEntry.setCallTime(LocalDateTime.now());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Host", defaultHost);
            headers.set("X-Reuqest-Appid", config.getAppId() != null ? config.getAppId() : defaultAppId);
            headers.set("Authorization", "Bearer " + config.getJwtToken());
            headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 NetType/WIFI MicroMessenger/7.0.20.1781(0x6700143B) MacWechat/3.8.7(0x13080712) UnifiedPCMacWechat(0xf2641702) XWEB/18788 Flue");
            headers.set("Accept", "application/json, text/plain, */*");
            headers.set("Sec-Fetch-Site", "same-origin");
            headers.set("Sec-Fetch-Mode", "cors");
            headers.set("Sec-Fetch-Dest", "empty");
            headers.set("Accept-Language", "zh-CN,zh;q=0.9");

            // 添加门店特定的请求头
            if (snbid != null && !snbid.isEmpty()) {
                headers.set("X-Request-Product", "X");
                headers.set("X-Reuqest-platformId", snbid);
                headers.set("Referer", "https://xmp.54laoban.cn/booking");
            }

            if (config.getCookie() != null && !config.getCookie().isEmpty()) {
                headers.set("Cookie", config.getCookie());
            }
            logEntry.setRequestHeaders(headers);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            if (body != null) {
                logEntry.setRequestBody(objectMapper.writeValueAsString(body));
            }

            ResponseEntity<Map> response = restTemplate.exchange(url, method, entity, Map.class);

            logEntry.setResponseCode(response.getStatusCodeValue());
            logEntry.setResponseBody(response.getBody());
            logEntry.setStatus(1);
            logEntry.setDurationMs((int) (System.currentTimeMillis() - start));

            return response.getBody();
        } catch (Exception e) {
            logEntry.setStatus(0);
            logEntry.setErrorMsg(e.getMessage());
            logEntry.setDurationMs((int) (System.currentTimeMillis() - start));
            log.error("API调用失败: {}", apiName, e);
            return null;
        } finally {
            // 异步保存日志，不阻塞主流程
            apiCallLogService.saveAsync(logEntry);
        }
    }

    private DataCollectionTask createTask(Long configId, Long shopId, String taskType) {
        DataCollectionTask task = new DataCollectionTask();
        task.setConfigId(configId);
        task.setShopId(shopId);
        task.setTaskType(taskType);
        task.setStatus(TaskStatus.EXECUTING.getCode());
        task.setStartTime(LocalDateTime.now());
        task.setSnapshotTime(LocalDateTime.now());
        dataCollectionTaskService.save(task);
        return task;
    }

    private void finishTask(DataCollectionTask task, TaskStatus status, LocalDateTime startTime) {
        finishTask(task, status, startTime, null);
    }

    private void finishTask(DataCollectionTask task, TaskStatus status, LocalDateTime startTime, String errorMsg) {
        task.setStatus(status.getCode());
        task.setEndTime(LocalDateTime.now());
        task.setDurationMs((int) (System.currentTimeMillis() - startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()));
        task.setErrorMsg(errorMsg);
        dataCollectionTaskService.updateById(task);
    }

    /**
     * 判断Token是否需要刷新
     * 条件：Token为空、已过期、或将在30分钟内过期
     */
    private boolean shouldRefreshToken(ShopConfig config) {
        if (config.getJwtToken() == null || config.getJwtToken().isEmpty()) {
            return false;
        }
        if (config.getTokenExpireTime() == null) {
            // 没有过期时间，尝试刷新
            return true;
        }
        // 如果Token将在30分钟内过期，则需要刷新
        LocalDateTime expireTime = config.getTokenExpireTime();
        LocalDateTime refreshThreshold = LocalDateTime.now().plusMinutes(30);
        return expireTime.isBefore(refreshThreshold);
    }

    // 工具方法
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime getLocalDateTime(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        try {
            String dateStr = String.valueOf(value);
            // 过滤无效时间 (1899年是无效默认值)
            if (dateStr.contains("1899")) return null;

            // 解析 ISO 8601 格式: "2026-03-30T10:00:00.000+0000"
            if (dateStr.contains("T")) {
                // 去掉时区后缀，只保留时间部分
                String cleaned = dateStr.replaceAll("[+-]\\d{4}$", "");
                return LocalDateTime.parse(cleaned.substring(0, 19));
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 随机延迟，模拟真人操作
     * @param minMs 最小延迟毫秒数
     * @param maxMs 最大延迟毫秒数
     */
    private void randomDelay(int minMs, int maxMs) {
        try {
            int delay = minMs + (int) (Math.random() * (maxMs - minMs));
            log.debug("随机延迟: {}ms", delay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
