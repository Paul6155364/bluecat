package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluecat.common.enums.TaskStatus;
import com.bluecat.common.enums.TaskType;
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

    // 银杏管家配置
    @Value("${bluecat.api.yinxing-host:chain24819.tmwanba.com}")
    private String yinxingHost;

    @Value("${bluecat.api.yinxing-user-agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 NetType/WIFI MicroMessenger/7.0.20.1781(0x6700143B) MacWechat/3.8.7(0x13080712) UnifiedPCMacWechat(0xf2641702) XWEB/18788 Flue}")
    private String yinxingUserAgent;

    @Value("${bluecat.api.yinxing-referer:https://chain24819.tmwanba.com/release209/}")
    private String yinxingReferer;

    // 网鱼网咖配置
    @Value("${bluecat.api.wangyu-host:vip-gateway.wywk.cn}")
    private String wangyuHost;

    @Value("${bluecat.api.wangyu-client-source:MA_WECHAT}")
    private String wangyuClientSource;

    @Value("${bluecat.api.wangyu-version:3.8.7}")
    private String wangyuVersion;

    @Value("${bluecat.api.wangyu-user-agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 MicroMessenger/7.0.20.1781(0x6700143B) NetType/WIFI MiniProgramEnv/Mac MacWechat/WMPF MacWechat/3.8.7(0x13080712) UnifiedPCMacWechat(0xf2641702) XWEB/18788}")
    private String wangyuUserAgent;

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
    private final AreaFeeSnapshotService areaFeeSnapshotService;
    private final ApiCallLogService apiCallLogService;
    private final ShopImageService shopImageService;

    private final ShopInfoMapper shopInfoMapper;
    private final ShopAreaMapper shopAreaMapper;
    private final MachineInfoMapper machineInfoMapper;
    private final ShopImageMapper shopImageMapper;
    private final ShopStatusSnapshotMapper shopStatusSnapshotMapper;

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

    /**
     * 银杏管家-测试连接
     * 调用 /default/check-bind-phone 接口
     */
    @Override
    public Map<String, Object> testYinxing(ShopConfig config) {
        log.info("银杏管家测试连接: configId={}, snbid={}", config.getId(), config.getSnbid());
        long start = System.currentTimeMillis();

        ApiCallLog logEntry = new ApiCallLog();
        logEntry.setConfigId(config.getId());
        logEntry.setApiName("yinxing-check-bind-phone");
        logEntry.setApiUrl("/default/check-bind-phone");
        logEntry.setRequestMethod("GET");
        logEntry.setCallTime(LocalDateTime.now());

        try {
            String url = "https://" + yinxingHost + "/default/check-bind-phone";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Host", yinxingHost);
            headers.set("User-Agent", yinxingUserAgent);
            headers.set("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.set("X-Requested-With", "XMLHttpRequest");
            headers.set("Sec-Fetch-Site", "same-origin");
            headers.set("Sec-Fetch-Mode", "cors");
            headers.set("Sec-Fetch-Dest", "empty");
            headers.set("Referer", yinxingReferer);
            headers.set("Accept-Language", "zh-CN,zh;q=0.9");
            headers.set("Priority", "u=1, i");

            // 设置 Cookie - 模拟浏览器完整Cookie
            StringBuilder cookieBuilder = new StringBuilder();
            cookieBuilder.append("chain-id=").append(config.getSnbid());

            // 如果配置有 Cookie，使用配置的 Cookie
            if (config.getCookie() != null && !config.getCookie().isEmpty()) {
                String configCookie = config.getCookie();
                appendCookieValue(cookieBuilder, configCookie, "HMACCOUNT");
                appendCookieValue(cookieBuilder, configCookie, "chain");
                appendCookieValue(cookieBuilder, configCookie, "Hm_lvt_");
                appendCookieValue(cookieBuilder, configCookie, "Hm_lpvt_");
            }
            headers.set("Cookie", cookieBuilder.toString());

            logEntry.setRequestHeaders(headers);

            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            logEntry.setResponseCode(response.getStatusCodeValue());
            logEntry.setResponseBody(response.getBody());
            logEntry.setStatus(1);
            logEntry.setDurationMs((int) (System.currentTimeMillis() - start));

            Map<String, Object> body = response.getBody();
            log.info("银杏管家测试结果: configId={}, code={}, msg={}", config.getId(), body != null ? body.get("code") : "null", body != null ? body.get("msg") : "null");

            return body != null ? body : new HashMap<>();
        } catch (Exception e) {
            log.error("银杏管家测试连接异常: configId={}", config.getId(), e);
            logEntry.setStatus(0);
            logEntry.setErrorMsg(e.getMessage());
            logEntry.setDurationMs((int) (System.currentTimeMillis() - start));

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", -1);
            errorResult.put("msg", e.getMessage());
            return errorResult;
        } finally {
            apiCallLogService.saveAsync(logEntry);
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

    // ========== 银杏管家采集方法 ==========

    @Override
    public List<Map<String, Object>> getYinxingShopList(ShopConfig config) {
        // 1. 先调用recharge接口，模拟用户操作（去过的门店）
        String rechargeUrl = "https://" + yinxingHost + "/default/chains?name=recharge&chain_id=" + config.getSnbid();
        callYinxingApi(config, rechargeUrl, "yinxing-chains", null, null, HttpMethod.GET);

        // 2. 调用全量门店接口，获取所有门店
        String allShopsUrl = "https://" + yinxingHost + "/default/chains?name=&chain_id=" + config.getSnbid() + "&dingzuo=1";
        Map<String, Object> response = callYinxingApi(config, allShopsUrl, "yinxing-chains", null, null, HttpMethod.GET);
        if (response != null && response.containsKey("data")) {
            Object data = response.get("data");
            if (data instanceof List) {
                return (List<Map<String, Object>>) data;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getYinxingShopDetail(ShopConfig config, String chainId) {
        // 银杏管家门店详情: GET /default/index?chain_id=xxx
        String url = "https://" + yinxingHost + "/default/index?chain_id=" + chainId;
        // referer为null，shopId为null，使用默认referer
        Map<String, Object> response = callYinxingApi(config, url, "yinxing-index", null, null, HttpMethod.GET);
        if (response != null && response.containsKey("data")) {
            return (Map<String, Object>) response.get("data");
        }
        return new HashMap<>();
    }

    @Override
    public YinxingRoomResult getYinxingRoomInfo(ShopConfig config, Long shopId, String chainId, Long mchId) {
        // 银杏管家切换门店session: GET /default/session-mch?mch_id=xxx
        // 必须先调用此接口切换到指定门店，再调用dingzuo/item才能获取正确数据
        String sessionUrl = "https://" + yinxingHost + "/default/session-mch?mch_id=" + mchId;
        Map<String, Object> sessionResponse = callYinxingApi(config, sessionUrl, "yinxing-session-mch", null, null, HttpMethod.GET);
        log.debug("银杏管家切换门店session: mchId={}, response={}", mchId, sessionResponse);

        // 银杏管家舱位信息: GET /dingzuo/item
        String referrer = String.format("https://%s/release209/#/select-seat-new?name=新预约订座-多人&mch_id=%d&chain_id=%s",
                yinxingHost, mchId, chainId);

        String encodedReferrer;
        try {
            // UTF-8 是标准编码，这里理论上永远不会抛出异常
            encodedReferrer = java.net.URLEncoder.encode(referrer, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            // 编码失败时返回空，避免接口报错
            return new YinxingRoomResult(new ArrayList<>(), null);
        }

        String url = "https://" + yinxingHost + "/dingzuo/item?referrer=" + encodedReferrer;

        Map<String, Object> response = callYinxingApi(config, url, "yinxing-dingzuo-item", referrer, null, HttpMethod.GET);
        List<Map<String, Object>> roomList = new ArrayList<>();
        Map<String, Object> ext = null;

        if (response != null) {
            if (response.containsKey("data") && response.get("data") instanceof List) {
                roomList = (List<Map<String, Object>>) response.get("data");
            }
            if (response.containsKey("ext") && response.get("ext") instanceof Map) {
                ext = (Map<String, Object>) response.get("ext");
            }
            // 调试日志：打印API返回的ext数据，重点关注ext.mch_id与传入mchId的对比
            Integer extMchId = ext != null ? getInteger(ext, "mch_id") : null;
            log.info("银杏管家舱位API返回: 传入mchId={}, ext.mch_id={}, chainId={}, roomCount={}, ext.total={}, ext.online_num={}",
                    mchId, extMchId, chainId, roomList.size(),
                    ext != null ? getInteger(ext, "total") : null,
                    ext != null ? getInteger(ext, "online_num") : null);
        }
        return new YinxingRoomResult(roomList, ext);
    }

    /**
     * 银杏管家订座记录: GET /dingzuo/record
     * 模拟用户查看订座页面，保持session活跃
     */
    private Map<String, Object> getYinxingDingzuoRecord(ShopConfig config) {
        String url = "https://" + yinxingHost + "/dingzuo/record";
        Map<String, Object> response = callYinxingApi(config, url, "yinxing-dingzuo-record", null, null, HttpMethod.GET);
        return response != null ? response : new HashMap<>();
    }

    @Override
    @Async("collectionExecutor")
    public void executeYinxingCollection(Long configId) {
        log.info("开始银杏管家采集任务: configId={}", configId);

        ShopConfig config = shopConfigService.getById(configId);
        if (config == null || config.getStatus() != 1 || config.getPlatformType() != 1) {
            log.warn("配置不存在、已禁用或非银杏管家配置: configId={}", configId);
            return;
        }

        LocalDateTime startTime = LocalDateTime.now();
        DataCollectionTask task = createTask(configId, null, "YINXING_SHOP_LIST");

        try {
            // Step 1: 模拟浏览器访问首页 - 检查绑定状态（探路请求，建立session）
            Map<String, Object> checkResult = testYinxing(config);
            if (checkResult != null && checkResult.containsKey("code")) {
                Integer code = (Integer) checkResult.get("code");
                if (code != null && code == -201) {
                    log.error("银杏管家Cookie已失效，终止采集: configId={}", configId);
                    finishTask(task, TaskStatus.FAILED, startTime, "Cookie已失效，请重新获取");
                    return;
                }
            }
            log.info("银杏管家探路请求完成: configId={}", configId);

            // 模拟用户浏览页面间隔 3-6秒
            randomDelay(3000, 6000);

            // Step 2: 获取门店列表（对应浏览器访问充值页面的请求）
            List<Map<String, Object>> shopList = getYinxingShopList(config);
            log.info("银杏管家门店列表: configId={}, count={}", configId, shopList.size());

            if (shopList.isEmpty()) {
                finishTask(task, TaskStatus.SUCCESS, startTime);
                return;
            }

            // 模拟用户查看门店列表 3-8秒
            randomDelay(3000, 8000);

            // Step 3: 遍历门店采集数据（直接用chains数据，无需再调index）
            for (Map<String, Object> shopData : shopList) {
                // chains返回的id是mch_id（单门店ID），snbid是chain_id（连锁机构ID）
                Long mchId = getLong(shopData, "id");
                if (mchId == null) {
                    continue;
                }
                // 调试日志：打印当前门店信息
                log.info("银杏管家处理门店: mchId={}, name={}, chainId={}, configCookie={}",
                        mchId, getString(shopData, "name"), config.getSnbid(),
                        config.getCookie() != null ? config.getCookie().substring(0, Math.min(30, config.getCookie().length())) + "..." : "null");

                // 直接用chains接口数据保存门店（chains已包含name/lng/lat/省市区等）
                ShopInfo shop = saveYinxingShopInfo(config, shopData);
                if (shop == null) {
                    randomDelay(2000, 4000);
                    continue;
                }

                // Step 3a: 获取订座记录（模拟用户查看订座页面）
                getYinxingDingzuoRecord(config);

                // 模拟用户查看订座后思考 2-5秒
                randomDelay(2000, 5000);

                // Step 3b: 获取舱位/机器信息并保存快照
                // chainId用config.getSnbid()（连锁机构ID），mchId用chains返回的id（单门店ID）
                String chainId = config.getSnbid();
                YinxingRoomResult roomResult = getYinxingRoomInfo(config, null, chainId, mchId);
                // 无论舱位数据是否为空，都保存快照（确保每个门店都有快照记录）
                saveYinxingWithSnapshot(shop, roomResult.getRoomList(), roomResult.getExt(), task.getId());

                // 模拟用户浏览完一个门店后，切换到下一个门店 5-10秒
                randomDelay(5000, 10000);
            }

            finishTask(task, TaskStatus.SUCCESS, startTime);
            log.info("银杏管家采集任务完成: configId={}", configId);
        } catch (Exception e) {
            log.error("银杏管家数据采集失败: configId={}", configId, e);
            finishTask(task, TaskStatus.FAILED, startTime, e.getMessage());
        }
    }

    @Override
    @Async("collectionExecutor")
    public void executeAllYinxingCollection() {
        log.info("开始执行所有银杏管家采集任务");

        // 查询所有银杏管家配置
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<ShopConfig>()
                .eq(ShopConfig::getStatus, 1)
                .eq(ShopConfig::getPlatformType, 1);
        List<ShopConfig> configs = shopConfigService.list(wrapper);

        for (ShopConfig config : configs) {
            try {
                self.executeYinxingCollection(config.getId());
                randomDelay(3000, 5000);
            } catch (Exception e) {
                log.error("银杏管家采集失败: configId={}", config.getId(), e);
            }
        }
    }

    // ========== 网鱼网咖采集方法 ==========

    @Override
    public Map<String, Object> testWangyu(ShopConfig config) {
        log.info("网鱼网咖测试连接: configId={}", config.getId());
        long start = System.currentTimeMillis();

        ApiCallLog logEntry = new ApiCallLog();
        logEntry.setConfigId(config.getId());
        logEntry.setApiName("wangyu-test");
        logEntry.setApiUrl("/asset-web/api/shop/v3/bookingStoreList");
        logEntry.setRequestMethod("POST");
        logEntry.setCallTime(LocalDateTime.now());

        try {
            // 调用门店列表接口进行测试
            Map<String, Object> result = getWangyuShopList(config, "", 1, 1);
            logEntry.setResponseBody(result);
            logEntry.setStatus(1);
            logEntry.setDurationMs((int) (System.currentTimeMillis() - start));

            if (result != null && result.containsKey("code")) {
                Integer code = (Integer) result.get("code");
                if (code == 0) {
                    log.info("网鱼网咖测试成功: configId={}", config.getId());
                    return result;
                } else {
                    log.warn("网鱼网咖测试失败: configId={}, code={}, msg={}", config.getId(), code, result.get("message"));
                    return result;
                }
            }
            return result != null ? result : new HashMap<>();
        } catch (Exception e) {
            log.error("网鱼网咖测试连接异常: configId={}", config.getId(), e);
            logEntry.setStatus(0);
            logEntry.setErrorMsg(e.getMessage());
            logEntry.setDurationMs((int) (System.currentTimeMillis() - start));

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", -1);
            errorResult.put("msg", e.getMessage());
            return errorResult;
        } finally {
            apiCallLogService.saveAsync(logEntry);
        }
    }

    @Override
    public Map<String, Object> getWangyuShopList(ShopConfig config, String keyword, Integer page, Integer pageSize) {
        String url = "https://" + wangyuHost + "/asset-web/api/shop/v3/bookingStoreList";

        Map<String, Object> body = new HashMap<>();
        body.put("keyword", keyword != null ? keyword : "");
        body.put("defaultStoreCode", "");
        // 使用配置的经纬度或默认值
        body.put("longitude", 104.043113);
        body.put("latitude", 30.642419);
        body.put("page", page != null ? page : 1);
        body.put("pageSize", pageSize != null ? pageSize : 10);
        body.put("filterItemList", new ArrayList<>());

        return callWangyuApi(config, url, body, "wangyu-bookingStoreList");
    }

    /**
     * 调用网鱼网咖API
     */
    private Map<String, Object> callWangyuApi(ShopConfig config, String url, Map<String, Object> body, String apiName) {
        long start = System.currentTimeMillis();
        ApiCallLog logEntry = new ApiCallLog();
        logEntry.setConfigId(config.getId());
        logEntry.setApiName(apiName);
        logEntry.setApiUrl(url);
        logEntry.setRequestMethod("POST");
        logEntry.setCallTime(LocalDateTime.now());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Host", wangyuHost);
            headers.set("client-source", wangyuClientSource);
            // 生成x-ma-uid（可使用固定值或随机生成）
            headers.set("x-ma-uid", "da14b97ded62e386a4def522b6b13500f34d492d723e0bd96543841f5feaedab");
            headers.set("x-version", wangyuVersion);
            headers.set("xweb_xhr", "1");
            headers.set("x-auth", config.getJwtToken());
            headers.set("User-Agent", wangyuUserAgent);

            // 设备信息
            String deviceInfo = "{\"brand\":\"apple\",\"model\":\"Mac16,13\",\"systemVersion\":\"Mac OS X 26.3.1 arm64\",\"platform\":\"mac\",\"deviceId\":\"\",\"coreVersion\":\"\",\"netWorkType\":\"wifi\",\"cpuType\":\"\",\"screenHeight\":780,\"screenWidth\":414,\"language\":\"zh_CN\",\"latitude\":30.642419,\"longitude\":104.043113}";
            headers.set("device-info", deviceInfo);

            headers.set("Accept", "*/*");
            headers.set("Sec-Fetch-Site", "cross-site");
            headers.set("Sec-Fetch-Mode", "cors");
            headers.set("Sec-Fetch-Dest", "empty");
            headers.set("Referer", "https://servicewechat.com/wx8f16a17a2e75284b/609/page-frame.html");
            headers.set("Accept-Encoding", "gzip, deflate, br");
            headers.set("Accept-Language", "zh-CN,zh;q=0.9");
            headers.set("Priority", "u=1, i");

            logEntry.setRequestHeaders(headers);
            logEntry.setRequestBody(objectMapper.writeValueAsString(body));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            logEntry.setResponseCode(response.getStatusCodeValue());
            logEntry.setResponseBody(response.getBody());
            logEntry.setStatus(1);
            logEntry.setDurationMs((int) (System.currentTimeMillis() - start));

            return response.getBody();
        } catch (Exception e) {
            log.error("网鱼网咖API调用失败: {}", apiName, e);
            logEntry.setStatus(0);
            logEntry.setErrorMsg(e.getMessage());
            logEntry.setDurationMs((int) (System.currentTimeMillis() - start));

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", -1);
            errorResult.put("msg", e.getMessage());
            return errorResult;
        } finally {
            apiCallLogService.saveAsync(logEntry);
        }
    }

    /**
     * 调用网鱼网咖API（GET请求）
     */
    private Map<String, Object> callWangyuGetApi(ShopConfig config, String url, String apiName) {
        long start = System.currentTimeMillis();
        ApiCallLog logEntry = new ApiCallLog();
        logEntry.setConfigId(config.getId());
        logEntry.setApiName(apiName);
        logEntry.setApiUrl(url);
        logEntry.setRequestMethod("GET");
        logEntry.setCallTime(LocalDateTime.now());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Host", wangyuHost);
            headers.set("client-source", wangyuClientSource);
            headers.set("x-ma-uid", "da14b97ded62e386a4def522b6b13500f34d492d723e0bd96543841f5feaedab");
            headers.set("x-version", wangyuVersion);
            headers.set("xweb_xhr", "1");
            headers.set("x-auth", config.getJwtToken());
            headers.set("User-Agent", wangyuUserAgent);
            String deviceInfo = "{\"brand\":\"apple\",\"model\":\"Mac16,13\",\"systemVersion\":\"Mac OS X 26.3.1 arm64\",\"platform\":\"mac\",\"deviceId\":\"\",\"coreVersion\":\"\",\"netWorkType\":\"wifi\",\"cpuType\":\"\",\"screenHeight\":780,\"screenWidth\":414,\"language\":\"zh_CN\",\"latitude\":30.642419,\"longitude\":104.043113}";
            headers.set("device-info", deviceInfo);
            headers.set("Accept", "*/*");
            headers.set("Sec-Fetch-Site", "cross-site");
            headers.set("Sec-Fetch-Mode", "cors");
            headers.set("Sec-Fetch-Dest", "empty");
            headers.set("Referer", "https://servicewechat.com/wx8f16a17a2e75284b/609/page-frame.html");
            headers.set("Accept-Encoding", "gzip, deflate, br");
            headers.set("Accept-Language", "zh-CN,zh;q=0.9");
            headers.set("Priority", "u=1, i");

            logEntry.setRequestHeaders(headers);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            logEntry.setResponseCode(response.getStatusCodeValue());
            logEntry.setResponseBody(response.getBody());
            logEntry.setStatus(1);
            logEntry.setDurationMs((int) (System.currentTimeMillis() - start));

            return response.getBody();
        } catch (Exception e) {
            log.error("网鱼网咖API调用失败: {}", apiName, e);
            logEntry.setStatus(0);
            logEntry.setErrorMsg(e.getMessage());
            logEntry.setDurationMs((int) (System.currentTimeMillis() - start));

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", -1);
            errorResult.put("msg", e.getMessage());
            return errorResult;
        } finally {
            apiCallLogService.saveAsync(logEntry);
        }
    }

    /**
     * 网鱼网咖-获取门店区域信息
     * 接口: GET /surf-internet/shop/storearea?commonCode=xxx
     * 返回 spaceAssetsAreaInfo[]: areaId(逗号分隔的elementId), areaName
     */
    private Map<String, String> getWangyuShopAreaMap(ShopConfig config, String commonCode) {
        String url = "https://" + wangyuHost + "/surf-internet/shop/storearea?commonCode=" + commonCode;

        Map<String, Object> result = callWangyuGetApi(config, url, "wangyu-storearea");
        if (result == null) {
            return Collections.emptyMap();
        }

        Map<String, String> elementIdToAreaName = new HashMap<>();
        Integer code = getInteger(result, "code");
        if (code == null || code != 0) {
            return Collections.emptyMap();
        }

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        if (data == null) {
            return Collections.emptyMap();
        }

        List<Map<String, Object>> areaInfoList = getElementList(data, "spaceAssetsAreaInfo");
        if (areaInfoList == null) {
            return Collections.emptyMap();
        }

        for (Map<String, Object> areaInfo : areaInfoList) {
            String areaId = getString(areaInfo, "areaId");
            String areaName = getString(areaInfo, "areaName");
            if (areaId != null && areaName != null) {
                // areaId是逗号分隔的element ID列表
                for (String id : areaId.split(",")) {
                    elementIdToAreaName.put(id.trim(), areaName);
                }
            }
        }

        return elementIdToAreaName;
    }

    @Override
    @Async("collectionExecutor")
    public void executeWangyuCollection(Long configId) {
        log.info("开始网鱼网咖采集任务: configId={}", configId);

        ShopConfig config = shopConfigService.getById(configId);
        if (config == null || config.getStatus() != 1 || config.getPlatformType() != 2) {
            log.warn("配置不存在、已禁用或非网鱼网咖配置: configId={}", configId);
            return;
        }

        LocalDateTime startTime = LocalDateTime.now();
        DataCollectionTask task = createTask(configId, null, "WANGYU_SHOP_LIST");

        try {
            // 获取总页数和总数量
            Map<String, Object> firstPage = getWangyuShopList(config, "", 1, 10);
            if (firstPage == null || firstPage.get("data") == null) {
                log.warn("网鱼网咖获取数据失败: configId={}", configId);
                finishTask(task, TaskStatus.FAILED, startTime, "获取数据失败");
                return;
            }

            Map<String, Object> data = (Map<String, Object>) firstPage.get("data");
            Integer totalPages = getInteger(data, "totalPages");
            Integer totalElements = getInteger(data, "totalElements");

            // 只采集前78个门店，超过的是其他地区的门店不需要
            int maxShops = 78;

            log.info("网鱼网咖门店总数: configId={}, totalPages={}, totalElements={}, 限制采集={}", configId, totalPages, totalElements, maxShops);

            if (totalPages == null || totalPages <= 0) {
                finishTask(task, TaskStatus.SUCCESS, startTime);
                return;
            }

            // 分页获取门店（最多采集maxShops个）
            int page = 1;
            int allSaved = 0;
            List<Map<String, Object>> allStores = new ArrayList<>();

            while (page <= totalPages && allStores.size() < maxShops) {
                log.info("网鱼网咖采集第{}页: configId={}", page, configId);
                Map<String, Object> pageResult = getWangyuShopList(config, "", page, 20);

                if (pageResult == null || pageResult.get("data") == null) {
                    log.warn("网鱼网咖获取第{}页失败: configId={}", page, configId);
                    break;
                }

                Map<String, Object> pageData = (Map<String, Object>) pageResult.get("data");
                List<Map<String, Object>> storeList = getStoreList(pageData);

                if (storeList != null && !storeList.isEmpty()) {
                    // 如果已收集+本页数据超过上限，只截取需要的部分
                    if (allStores.size() + storeList.size() > maxShops) {
                        storeList = storeList.subList(0, maxShops - allStores.size());
                    }
                    allStores.addAll(storeList);
                    int saved = saveWangyuShopList(config, storeList);
                    allSaved += saved;
                    log.info("网鱼网咖第{}页保存完成: configId={}, 保存{}个门店, 累计{}/{}", page, configId, saved, allStores.size(), maxShops);
                }

                // 达到上限或最后一页
                if (allStores.size() >= maxShops || page >= totalPages) {
                    break;
                }

                page++;
                // 模拟翻页延迟 2-4秒
                randomDelay(2000, 4000);
            }

            // Step 2: 遍历门店采集舱位/座位数据
            log.info("网鱼网咖开始采集门店座位布局: configId={}, shopCount={}", configId, allStores.size());
            for (Map<String, Object> shopData : allStores) {
                String commonCode = getString(shopData, "commonCode");
                if (commonCode == null || commonCode.isEmpty()) {
                    continue;
                }

                // 获取对应的门店
                ShopInfo shop = shopInfoService.getBySnbid(commonCode);
                if (shop == null) {
                    continue;
                }

                // 获取门店详情并更新
                try {
                    Map<String, Object> detailResult = getWangyuShopDetail(config, commonCode);
                    if (detailResult != null && detailResult.containsKey("data")) {
                        Map<String, Object> detailData = (Map<String, Object>) detailResult.get("data");
                        if (detailData != null) {
                            shop.setName(getString(detailData, "shopName"));
                            shop.setAddress(getString(detailData, "address"));
                            shop.setProvinceName(getString(detailData, "province"));
                            shop.setCityName(getString(detailData, "city"));
                            shop.setZoneName(getString(detailData, "regional"));
                            shop.setRawJson(detailData);
                            shopInfoService.updateById(shop);
                            log.info("网鱼网咖更新门店详情: shopId={}, commonCode={}, shopName={}", shop.getId(), commonCode, shop.getName());
                        }
                    }
                } catch (Exception e) {
                    log.warn("网鱼网咖获取门店详情失败，继续后续采集: shopId={}, commonCode={}", shop.getId(), commonCode, e);
                }

                // 模拟用户浏览间隔 1-2秒
                randomDelay(1000, 2000);

                // 获取座位布局数据
                try {
                    // Step 2a: 先调storearea获取区域映射（elementId → areaName）
                    Map<String, String> elementIdToAreaName = getWangyuShopAreaMap(config, commonCode);
                    log.info("网鱼网咖区域映射: shopId={}, commonCode={}, areaCount={}", shop.getId(), commonCode, elementIdToAreaName.size());

                    // 模拟用户浏览间隔 2-3秒
                    randomDelay(2000, 3000);

                    // Step 2b: 调shop/v3/get获取座位布局
                    Map<String, Object> layoutResult = getWangyuShopLayout(config, commonCode);
                    if (layoutResult != null && layoutResult.containsKey("data")) {
                        Map<String, Object> layoutData = (Map<String, Object>) layoutResult.get("data");
                        if (layoutData == null) {
                            log.warn("网鱼网咖门店座位布局数据为空: shopId={}, commonCode={}", shop.getId(), commonCode);
                            continue;
                        }
                        // 数据结构: data.areas[].elements[]
                        List<Map<String, Object>> areas = getElementList(layoutData, "areas");
                        if (areas != null && !areas.isEmpty()) {
                            // 收集所有区域的elements
                            List<Map<String, Object>> allElements = new ArrayList<>();
                            for (Map<String, Object> area : areas) {
                                List<Map<String, Object>> areaElements = getElementList(area, "elements");
                                if (areaElements != null) {
                                    allElements.addAll(areaElements);
                                }
                            }
                            if (!allElements.isEmpty()) {
                                saveWangyuWithSnapshot(shop, allElements, elementIdToAreaName, task.getId());
                                log.info("网鱼网咖门店座位布局采集完成: shopId={}, shopName={}, areas={}, elements={}",
                                        shop.getId(), shop.getName(), areas.size(), allElements.size());
                            } else {
                                // 即使没有座位数据，也保存空快照（确保每个门店都有快照记录）
                                saveWangyuWithSnapshot(shop, new ArrayList<>(), elementIdToAreaName, task.getId());
                                log.warn("网鱼网咖门店无座位数据: shopId={}, commonCode={}", shop.getId(), commonCode);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("网鱼网咖门店座位布局采集失败: shopId={}, commonCode={}", shop.getId(), commonCode, e);
                }

                // 模拟用户浏览间隔 3-5秒
                randomDelay(3000, 5000);
            }

            finishTask(task, TaskStatus.SUCCESS, startTime);
            log.info("网鱼网咖采集任务完成: configId={}, 共保存{}个门店", configId, allSaved);
        } catch (Exception e) {
            log.error("网鱼网咖数据采集失败: configId={}", configId, e);
            finishTask(task, TaskStatus.FAILED, startTime, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getStoreList(Map<String, Object> data) {
        // 优先获取oftenAndCollectdList（常去和收藏的门店），其次获取otherStoreList
        List<Map<String, Object>> storeList = new ArrayList<>();

        if (data.containsKey("oftenAndCollectdList") && data.get("oftenAndCollectdList") instanceof List) {
            List<Map<String, Object>> oftenList = (List<Map<String, Object>>) data.get("oftenAndCollectdList");
            if (oftenList != null) {
                storeList.addAll(oftenList);
            }
        }

        if (data.containsKey("otherStoreList") && data.get("otherStoreList") instanceof List) {
            List<Map<String, Object>> otherList = (List<Map<String, Object>>) data.get("otherStoreList");
            if (otherList != null) {
                storeList.addAll(otherList);
            }
        }

        return storeList;
    }

    /**
     * 保存网鱼网咖门店列表
     * @return 保存的门店数量
     */
    private int saveWangyuShopList(ShopConfig config, List<Map<String, Object>> shopList) {
        int saved = 0;
        for (Map<String, Object> shopData : shopList) {
            String commonCode = getString(shopData, "commonCode");
            if (commonCode == null || commonCode.isEmpty()) {
                continue;
            }

            ShopInfo shop = shopInfoService.getBySnbid(commonCode);
            if (shop == null) {
                shop = new ShopInfo();
                shop.setConfigId(config.getId());
                shop.setSnbid(commonCode);
            }

            // 映射网鱼网咖门店字段（bookingStoreList接口）
            shop.setName(getString(shopData, "shopName"));
            shop.setAddress(getString(shopData, "address"));
            // shopId 由 store/portal/detail 接口补充，此处不覆盖

            // 经纬度
            String lat = getString(shopData, "shopLat");
            String lng = getString(shopData, "shoplng");
            if (lat != null && !lat.isEmpty()) {
                try {
                    shop.setLatitude(new BigDecimal(lat));
                } catch (NumberFormatException ignored) {}
            }
            if (lng != null && !lng.isEmpty()) {
                try {
                    shop.setLongitude(new BigDecimal(lng));
                } catch (NumberFormatException ignored) {}
            }

            // 保存原始数据
            shop.setRawJson(shopData);

            if (shop.getId() == null) {
                shopInfoService.save(shop);
            } else {
                shopInfoService.updateById(shop);
            }
            saved++;
        }
        return saved;
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    @Async("collectionExecutor")
    public void executeAllWangyuCollection() {
        log.info("开始执行所有网鱼网咖采集任务");

        // 查询所有网鱼网咖配置
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<ShopConfig>()
                .eq(ShopConfig::getStatus, 1)
                .eq(ShopConfig::getPlatformType, 2);
        List<ShopConfig> configs = shopConfigService.list(wrapper);

        for (ShopConfig config : configs) {
            try {
                self.executeWangyuCollection(config.getId());
                randomDelay(3000, 5000);
            } catch (Exception e) {
                log.error("网鱼网咖采集失败: configId={}", config.getId(), e);
            }
        }
    }

    /**
     * 网鱼网咖-获取门店详情
     * 接口: GET /asset-web/shop/store/portal/detail?commonCode=xxx
     * 返回数据结构:
     * - commonCode: 门店编码
     * - shopName: 门店名称
     * - address: 地址
     * - storeState: 门店状态（开业等）
     * - serviceState: 服务状态
     * - storeType: 门店类型
     * - parentOrganization: 上级组织
     * - higherLevelOrganization: 更高级组织
     * - bigRegional: 大区
     * - province: 省份
     * - city: 城市
     * - regional: 区县
     */
    @Override
    public Map<String, Object> getWangyuShopDetail(ShopConfig config, String commonCode) {
        String url = "https://" + wangyuHost + "/asset-web/shop/store/portal/detail?commonCode=" + commonCode;
        return callWangyuGetApi(config, url, "wangyu-store-detail");
    }

    /**
     * 网鱼网咖-获取门店座位布局
     * 接口: POST /surf-internet/shop/v3/get
     * 返回数据结构:
     * - elements[]: 布局元素数组
     *   - type: PRIVATE_ROOM(包间)、SEAT(座位)、SPACE(过道)、BAR_COUNTER(吧台)
     *   - name: 元素名称（如包间名称）
     *   - clientInfo[]: 机器信息数组
     *     - clientNo: 机器编号
     *     - status: 0=空闲, 1=占用
     */
    @Override
    public Map<String, Object> getWangyuShopLayout(ShopConfig config, String storeCode) {
        String url = "https://" + wangyuHost + "/surf-internet/shop/v3/get";

        Map<String, Object> body = new HashMap<>();
        body.put("commonCode", storeCode);
        body.put("areas", "undefined");

        return callWangyuApi(config, url, body, "wangyu-shop-layout");
    }

    /**
     * 保存网鱼网咖舱位/座位数据（带状态快照）
     * 网鱼网咖结构: areas[].elements[] 中
     * - PRIVATE_ROOM: 包间区域标记，有 displayName（包间名）
     * - SEAT: 座位，有 clientInfo（含 clientNo、status），displayName（座位号）
     *   SEAT 的 clientInfo.roomName 可能为大厅房间名
     * - SPACE/BAR_COUNTER/ENTER 等为装饰元素，跳过
     *
     * @param shop 门店信息
     * @param elements 座位元素列表（从areas中提取的扁平列表）
     * @param elementIdToAreaName 从storearea接口获取的elementId→areaName映射
     * @param taskId 任务ID
     */
    private void saveWangyuWithSnapshot(ShopInfo shop, List<Map<String, Object>> elements,
                                        Map<String, String> elementIdToAreaName, Long taskId) {
        LocalDateTime snapshotTime = LocalDateTime.now();

        // 创建门店快照
        ShopStatusSnapshot snapshot = new ShopStatusSnapshot();
        snapshot.setTaskId(taskId);
        snapshot.setShopId(shop.getId());
        snapshot.setSnapshotTime(snapshotTime);
        snapshot.setRawJson(elements);
        shopStatusSnapshotService.save(snapshot);

        // 计算出的各状态机器数
        int calculatedTotal = 0;
        int freeMachines = 0;
        int busyMachines = 0;

        log.info("网鱼网咖机器计算明细: shopId={}, elements={}", shop.getId(), elements.size());

        // 按区域分组处理
        // SEAT 元素属于哪个区域：优先用 _areaName（从areas层级传入），
        // 否则用 clientInfo.roomName，最后默认"大厅"
        Map<String, List<Map<String, Object>>> areaMachinesMap = new LinkedHashMap<>();

        for (Map<String, Object> element : elements) {
            String elementCode = getString(element, "elementCode");
            if (!"SEAT".equals(elementCode)) {
                // PRIVATE_ROOM/SPACE/BAR_COUNTER/ENTER 等不是座位，跳过
                continue;
            }

            // 获取区域名称
            // 优先级: elementIdToAreaName(id映射) > clientInfo.roomName > 默认"大厅"
            // 注意: storearea接口的areaId映射的是PRIVATE_ROOM的id，不是SEAT的id
            //       SEAT的id不在storearea映射中，所以主要靠clientInfo.roomName
            String areaName = null;
            // 尝试用元素id查找（API返回的元素id字段名为"id"，非"elementId"）
            String elementId = element.get("id") != null ? String.valueOf(element.get("id")) : null;
            if (elementId != null && elementIdToAreaName != null) {
                areaName = elementIdToAreaName.get(elementId);
            }
            // 从 clientInfo.roomName 获取（最可靠的来源，如"大厅房间1"）
            if (areaName == null || areaName.isEmpty()) {
                Map<String, Object> clientInfo = (Map<String, Object>) element.get("clientInfo");
                if (clientInfo != null) {
                    areaName = getString(clientInfo, "roomName");
                }
            }
            if (areaName == null || areaName.isEmpty()) {
                areaName = "大厅";
            }

            // 获取 clientInfo（每个SEAT只有一个clientInfo对象，不是数组）
            Object clientInfoObj = element.get("clientInfo");
            if (clientInfoObj instanceof Map) {
                Map<String, Object> clientInfo = (Map<String, Object>) clientInfoObj;
                String clientNo = getString(clientInfo, "clientNo");
                Integer status = getInteger(clientInfo, "status"); // 0=空闲, 1=占用

                if (clientNo != null && !clientNo.isEmpty()) {
                    areaMachinesMap.computeIfAbsent(areaName, k -> new ArrayList<>())
                            .add(clientInfo);
                }
            }
        }

        // 遍历每个区域（包间/大厅）
        for (Map.Entry<String, List<Map<String, Object>>> entry : areaMachinesMap.entrySet()) {
            String areaName = entry.getKey();
            List<Map<String, Object>> machines = entry.getValue();

            // 创建或更新区域
            ShopArea area = shopAreaMapper.selectOne(new LambdaQueryWrapper<ShopArea>()
                    .eq(ShopArea::getShopId, shop.getId())
                    .eq(ShopArea::getAreaName, areaName));
            if (area == null) {
                area = new ShopArea();
                area.setShopId(shop.getId());
                area.setAreaName(areaName);
                area.setAllow(1);
                shopAreaService.save(area);
            }

            int areaTotal = machines.size();
            int areaBusy = 0;
            int areaFree = 0;

            for (Map<String, Object> clientInfo : machines) {
                String clientNo = getString(clientInfo, "clientNo");
                Integer status = getInteger(clientInfo, "status"); // 0=空闲, 1=占用

                if (status != null && status == 1) {
                    areaBusy++;
                } else {
                    areaFree++;
                }

                // 保存机器信息
                MachineInfo machine = machineInfoService.getByShopIdAndComName(shop.getId(), clientNo);
                if (machine == null) {
                    machine = new MachineInfo();
                    machine.setShopId(shop.getId());
                    machine.setComName(clientNo);
                }
                machine.setAreaId(area.getId());
                machine.setAreaName(areaName);

                if (machine.getId() == null) {
                    machineInfoService.save(machine);
                } else {
                    machineInfoService.updateById(machine);
                }

                // 保存机器状态历史
                MachineStatusHistory history = new MachineStatusHistory();
                history.setSnapshotId(snapshot.getId());
                history.setTaskId(taskId);
                history.setShopId(shop.getId());
                history.setMachineId(machine.getId());
                history.setComName(clientNo);
                history.setAreaName(areaName);
                // 状态: 0=占用, 1=空闲
                history.setStatus(status != null && status == 1 ? 0 : 1);
                history.setSnapshotTime(snapshotTime);
                machineStatusHistoryService.save(history);
            }

            calculatedTotal += areaTotal;
            freeMachines += areaFree;
            busyMachines += areaBusy;

            // 保存区域快照
            AreaStatusSnapshot areaSnapshot = new AreaStatusSnapshot();
            areaSnapshot.setSnapshotId(snapshot.getId());
            areaSnapshot.setTaskId(taskId);
            areaSnapshot.setShopId(shop.getId());
            areaSnapshot.setAreaName(areaName);
            areaSnapshot.setTotalMachines(areaTotal);
            areaSnapshot.setFreeMachines(areaFree);
            areaSnapshot.setBusyMachines(areaBusy);
            areaSnapshot.setSnapshotTime(snapshotTime);
            // 计算上座率 = busyMachines / totalMachines × 100%
            if (areaTotal > 0) {
                BigDecimal rate = BigDecimal.valueOf(areaBusy)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(areaTotal), 2, RoundingMode.HALF_UP);
                areaSnapshot.setOccupancyRate(rate);
            }
            areaStatusSnapshotService.save(areaSnapshot);

            log.debug("网鱼网咖区域明细: shopId={}, area={}, total={}, free={}, busy={}, rate={}",
                    shop.getId(), areaName, areaTotal, areaFree, areaBusy,
                    areaTotal > 0 ? (areaBusy * 100.0 / areaTotal) : 0);
        }

        // 更新门店快照中的统计信息
        if (snapshot.getId() != null) {
            snapshot.setTotalMachines(calculatedTotal);
            snapshot.setFreeMachines(freeMachines);
            snapshot.setBusyMachines(busyMachines);
            // 计算上座率 = busyMachines / totalMachines × 100%
            if (calculatedTotal > 0) {
                BigDecimal rate = BigDecimal.valueOf(busyMachines)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(calculatedTotal), 2, RoundingMode.HALF_UP);
                snapshot.setOccupancyRate(rate);
            }
            shopStatusSnapshotService.updateById(snapshot);
        }

        log.info("网鱼网咖机器采集完成: shopId={}, total={}, free={}, busy={}",
                shop.getId(), calculatedTotal, freeMachines, busyMachines);
    }

    /**
     * 获取Map中List类型的字段
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getElementList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List) {
            return (List<Map<String, Object>>) value;
        }
        return null;
    }

    /**
     * 保存银杏管家门店信息（从chains接口数据构建）
     */
    private ShopInfo saveYinxingShopInfo(ShopConfig config, Map<String, Object> chainsData) {
        String chainId = getString(chainsData, "id");
        ShopInfo shop = shopInfoService.getBySnbid(chainId);
        if (shop == null) {
            shop = new ShopInfo();
            shop.setConfigId(config.getId());
            shop.setSnbid(chainId);
        }

        // chains接口字段映射
        shop.setName(getString(chainsData, "name"));                // 门店名称
        shop.setAddress(getString(chainsData, "gps_addr"));         // 地址
        shop.setProvinceName(getString(chainsData, "province_name")); // 省份
        shop.setCityName(getString(chainsData, "city_name"));       // 城市
        shop.setZoneName(getString(chainsData, "region_name"));     // 区县
        shop.setLongitude(getBigDecimal(chainsData, "lng"));        // 经度
        shop.setLatitude(getBigDecimal(chainsData, "lat"));         // 纬度
        shop.setShopId(getLong(chainsData, "id"));                  // 门店ID（chains的id即mch_id）

        // 设置原始数据
        shop.setRawJson(chainsData);

        if (shop.getId() == null) {
            shopInfoService.save(shop);
        } else {
            shopInfoService.updateById(shop);
        }
        log.debug("保存银杏管家门店: chainId={}, name={}", chainId, shop.getName());
        return shop;
    }

    /**
     * 保存银杏管家舱位/机器信息（带状态快照）
     * 银杏管家结构: id, name, type, on_machine[], off_machine[], fee, bj_machine[]
     * @param ext API返回的ext数据，包含真正的total（总机位数）、online_num（上机人数）等
     */
    private void saveYinxingWithSnapshot(ShopInfo shop, List<Map<String, Object>> roomList, Map<String, Object> ext, Long taskId) {
        LocalDateTime snapshotTime = LocalDateTime.now();

        // 从ext获取真正的总机位数（ext.total是API提供的权威数据）
        // ext字段：total=总机位, online_num=实际上机人数, uid=用户ID, mch_id=商户ID等
        Integer extTotal = getInteger(ext, "total");
        Integer extOnlineNum = getInteger(ext, "online_num");

        // 创建门店快照
        ShopStatusSnapshot snapshot = new ShopStatusSnapshot();
        snapshot.setTaskId(taskId);
        snapshot.setShopId(shop.getId());
        snapshot.setSnapshotTime(snapshotTime);
        snapshot.setRawJson(roomList);
        shopStatusSnapshotService.save(snapshot);

        // 计算出的各状态机器数（基于API返回的on/off_machine，不含subscribe_machine）
        int calculatedTotal = 0;
        int freeMachines = 0;
        int busyMachines = 0;
        int subscribedMachines = 0;

        // 调试日志：打印每个舱位的详细计算
        log.info("银杏管家机器计算明细: shopId={}, 舱位数={}", shop.getId(), roomList.size());

        // 遍历舱位
        for (Map<String, Object> roomData : roomList) {
            String roomName = getString(roomData, "name");
            if (roomName == null || roomName.isEmpty()) {
                continue;
            }

            // 创建或更新区域
            ShopArea area = shopAreaMapper.selectOne(new LambdaQueryWrapper<ShopArea>()
                    .eq(ShopArea::getShopId, shop.getId())
                    .eq(ShopArea::getAreaName, roomName));
            if (area == null) {
                area = new ShopArea();
                area.setShopId(shop.getId());
                area.setAreaName(roomName);
                area.setAllow(1);
                shopAreaService.save(area);
            }

            // 获取空闲、占用和预约锁定机器列表
            // 注意：subscribe_machine是临时预约锁定，不计入"总机位"
            // 真正的总机位 = on_machine + off_machine
            List<String> onMachines = getStringList(roomData, "on_machine");       // 占用中
            List<String> offMachines = getStringList(roomData, "off_machine");    // 空闲
            List<String> subscribeMachines = getStringList(roomData, "subscribe_machine"); // 预约锁定（临时，不计入总机位）

            int areaTotal = onMachines.size() + offMachines.size();  // 不含预约锁定
            int areaFree = offMachines.size();
            int areaBusy = onMachines.size();
            int areaSubscribed = subscribeMachines.size();

            // 调试日志：每个舱位的机器明细
            log.debug("银杏管家舱位明细: shopId={}, area={}, on={}, off={}, subscribe={}, total={}",
                    shop.getId(), roomName, onMachines.size(), offMachines.size(), subscribeMachines.size(), areaTotal);

            calculatedTotal += areaTotal;
            freeMachines += areaFree;
            busyMachines += areaBusy;
            subscribedMachines += areaSubscribed;

            // 保存机器信息
            Set<String> allMachines = new HashSet<>();
            allMachines.addAll(onMachines);
            allMachines.addAll(offMachines);
            allMachines.addAll(subscribeMachines);

            for (String comName : allMachines) {
                MachineInfo machine = machineInfoService.getByShopIdAndComName(shop.getId(), comName);
                if (machine == null) {
                    machine = new MachineInfo();
                    machine.setShopId(shop.getId());
                    machine.setComName(comName);
                }
                machine.setAreaId(area.getId());
                machine.setAreaName(roomName);

                if (machine.getId() == null) {
                    machineInfoService.save(machine);
                } else {
                    machineInfoService.updateById(machine);
                }

                // 保存机器状态历史
                MachineStatusHistory history = new MachineStatusHistory();
                history.setSnapshotId(snapshot.getId());
                history.setTaskId(taskId);
                history.setShopId(shop.getId());
                history.setMachineId(machine.getId());
                history.setComName(comName);
                history.setAreaName(roomName);
                // 状态: 0=占用, 1=空闲, 2=预约锁定
                if (subscribeMachines.contains(comName)) {
                    history.setStatus(2);  // 预约锁定
                } else if (offMachines.contains(comName)) {
                    history.setStatus(1);  // 空闲
                } else {
                    history.setStatus(0);  // 占用
                }
                history.setSnapshotTime(snapshotTime);
                machineStatusHistoryService.save(history);
            }

            // 创建区域快照
            AreaStatusSnapshot areaSnapshot = new AreaStatusSnapshot();
            areaSnapshot.setTaskId(taskId);
            areaSnapshot.setSnapshotId(snapshot.getId());
            areaSnapshot.setShopId(shop.getId());
            areaSnapshot.setAreaName(roomName);
            areaSnapshot.setTotalMachines(areaTotal);
            areaSnapshot.setFreeMachines(areaFree);
            areaSnapshot.setBusyMachines(areaBusy);
            areaSnapshot.setFreeMachineList(offMachines);
            areaSnapshot.setSnapshotTime(snapshotTime);

            if (areaTotal > 0) {
                BigDecimal rate = BigDecimal.valueOf(areaBusy)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(areaTotal), 2, RoundingMode.HALF_UP);
                areaSnapshot.setOccupancyRate(rate);
            }
            areaStatusSnapshotService.save(areaSnapshot);

            // 保存区域费用快照
            AreaFeeSnapshot feeSnapshot = new AreaFeeSnapshot();
            feeSnapshot.setSnapshotId(snapshot.getId());
            feeSnapshot.setShopId(shop.getId());
            feeSnapshot.setAreaName(roomName);
            feeSnapshot.setVipRoom(roomData.containsKey("is_no") && Boolean.TRUE.equals(roomData.get("is_no")) ? 1 : 0);
            feeSnapshot.setTotalSeats(areaTotal);
            feeSnapshot.setRate(getBigDecimal(roomData, "fee"));
            feeSnapshot.setWhole(roomData.containsKey("is_no") && Boolean.TRUE.equals(roomData.get("is_no")) ? 1 : 0);
            areaFeeSnapshotService.save(feeSnapshot);
        }

        // 更新门店快照统计
        // 优先使用ext.total作为总机位数（API权威数据），否则用计算值
        int finalTotal = (extTotal != null && extTotal > 0) ? extTotal : calculatedTotal;
        snapshot.setTotalMachines(finalTotal);
        snapshot.setFreeMachines(freeMachines);
        snapshot.setBusyMachines(busyMachines);
        if (finalTotal > 0) {
            BigDecimal rate = BigDecimal.valueOf(busyMachines)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(finalTotal), 2, RoundingMode.HALF_UP);
            snapshot.setOccupancyRate(rate);
        }
        shopStatusSnapshotService.updateById(snapshot);

        // 记录日志，包含ext验证数据和计算明细
        log.info("保存银杏管家状态快照: shopId={}, calculatedTotal={}, ext.total={}, finalTotal={}, free={}, busy={}, subscribed={}, ext.online_num={}",
                shop.getId(), calculatedTotal, extTotal, finalTotal, freeMachines, busyMachines, subscribedMachines, extOnlineNum);
    }

    /**
     * 保存银杏管家舱位/机器信息（兼容旧方法，无快照）
     */
    private void saveYinxingRoomInfo(Long shopId, List<Map<String, Object>> roomList) {
        for (Map<String, Object> roomData : roomList) {
            String roomName = getString(roomData, "name");
            if (roomName == null || roomName.isEmpty()) {
                continue;
            }

            // 创建或更新区域
            ShopArea area = shopAreaMapper.selectOne(new LambdaQueryWrapper<ShopArea>()
                    .eq(ShopArea::getShopId, shopId)
                    .eq(ShopArea::getAreaName, roomName));
            if (area == null) {
                area = new ShopArea();
                area.setShopId(shopId);
                area.setAreaName(roomName);
                area.setAllow(1);
                shopAreaService.save(area);
            }

            // 处理在用机器 on_machine[]
            List<String> onMachines = getStringList(roomData, "on_machine");
            for (String comName : onMachines) {
                saveYinxingMachine(shopId, area.getId(), roomName, comName);
            }

            // 处理空闲机器 off_machine[]
            List<String> offMachines = getStringList(roomData, "off_machine");
            for (String comName : offMachines) {
                saveYinxingMachine(shopId, area.getId(), roomName, comName);
            }
        }
    }

    private void saveYinxingMachine(Long shopId, Long areaId, String areaName, String comName) {
        if (comName == null || comName.isEmpty()) {
            return;
        }

        MachineInfo machine = machineInfoService.getByShopIdAndComName(shopId, comName);
        if (machine == null) {
            machine = new MachineInfo();
            machine.setShopId(shopId);
            machine.setComName(comName);
        }
        machine.setAreaId(areaId);
        machine.setAreaName(areaName);

        if (machine.getId() == null) {
            machineInfoService.save(machine);
        } else {
            machineInfoService.updateById(machine);
        }
    }

    /**
     * 调用银杏管家API
     *
     * @param config  网吧配置
     * @param url     请求URL
     * @param apiName API名称
     * @param referer Referer头（可选，用于dingzuo等接口）
     * @param shopId  门店ID
     * @param method  请求方法
     */
    private Map<String, Object> callYinxingApi(ShopConfig config, String url, String apiName, String referer, Long shopId, HttpMethod method) {
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
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Host", yinxingHost);
            headers.set("User-Agent", yinxingUserAgent);
            headers.set("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.set("X-Requested-With", "XMLHttpRequest");
            headers.set("Sec-Fetch-Site", "same-origin");
            headers.set("Sec-Fetch-Mode", "cors");
            headers.set("Sec-Fetch-Dest", "empty");
            // 优先使用动态referer，否则使用默认referer
            headers.set("Referer", referer != null ? referer : yinxingReferer);
            headers.set("Accept-Language", "zh-CN,zh;q=0.9");
            headers.set("Priority", "u=1, i");

            // 设置 Cookie - 模拟浏览器完整Cookie
            StringBuilder cookieBuilder = new StringBuilder();
            cookieBuilder.append("chain-id=").append(config.getSnbid());

            if (config.getCookie() != null && !config.getCookie().isEmpty()) {
                String configCookie = config.getCookie();
                // 解析并拼接所有配置中的cookie项
                // 支持 HMACCOUNT, chain, Hm_lvt_*, Hm_lpvt_* 等
                appendCookieValue(cookieBuilder, configCookie, "HMACCOUNT");
                appendCookieValue(cookieBuilder, configCookie, "chain");
                appendCookieValue(cookieBuilder, configCookie, "Hm_lvt_");
                appendCookieValue(cookieBuilder, configCookie, "Hm_lpvt_");
            }
            headers.set("Cookie", cookieBuilder.toString());

            logEntry.setRequestHeaders(headers);

            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, method, entity, Map.class);

            logEntry.setResponseCode(response.getStatusCodeValue());
            logEntry.setResponseBody(response.getBody());
            logEntry.setStatus(1);
            logEntry.setDurationMs((int) (System.currentTimeMillis() - start));

            return response.getBody();
        } catch (Exception e) {
            log.error("银杏管家API调用失败: {}", apiName, e);
            logEntry.setStatus(0);
            logEntry.setErrorMsg(e.getMessage());
            logEntry.setDurationMs((int) (System.currentTimeMillis() - start));

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", -1);
            errorResult.put("msg", e.getMessage());
            return errorResult;
        } finally {
            apiCallLogService.saveAsync(logEntry);
        }
    }

    private List<String> getStringList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return new ArrayList<>();
    }

    /**
     * 从配置Cookie字符串中提取指定key的值并拼接到cookieBuilder
     * 支持精确匹配（如 HMACCOUNT）和前缀匹配（如 Hm_lvt_）
     */
    private void appendCookieValue(StringBuilder cookieBuilder, String configCookie, String key) {
        if (configCookie == null || !configCookie.contains(key)) {
            return;
        }
        // 找到所有匹配的cookie项
        int searchFrom = 0;
        while (searchFrom < configCookie.length()) {
            int startIdx = configCookie.indexOf(key, searchFrom);
            if (startIdx == -1) break;

            // 确保是cookie的开头（前面是;或开头位置）
            if (startIdx > 0 && configCookie.charAt(startIdx - 1) != ' ' && configCookie.charAt(startIdx - 1) != ';') {
                searchFrom = startIdx + 1;
                continue;
            }

            int endIdx = configCookie.indexOf(";", startIdx);
            if (endIdx == -1) endIdx = configCookie.length();
            String cookieItem = configCookie.substring(startIdx, endIdx).trim();
            if (!cookieItem.isEmpty()) {
                cookieBuilder.append("; ").append(cookieItem);
            }
            searchFrom = endIdx + 1;
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

        // 解析区域费用数据 - API返回的是 areaFeeList，包含各区域的费率信息
        saveAreaFeeSnapshots(snapshot.getId(), shopId, statusData);

        // 更新今日累计营收
        BigDecimal todayRevenue = shopStatusSnapshotMapper.getTodayRevenueByShopId(shopId);
        shopStatusSnapshotMapper.updateDayRevenueByShopId(shopId, todayRevenue);

        // 更新门店快照统计
        snapshot.setTotalMachines(totalMachines);
        snapshot.setFreeMachines(freeMachines);
        snapshot.setBusyMachines(busyMachines);
        snapshot.setRemain(getBigDecimal(statusData, "remain"));
        if (totalMachines > 0) {
            BigDecimal rate = BigDecimal.valueOf(busyMachines)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalMachines), 2, RoundingMode.HALF_UP);
            snapshot.setOccupancyRate(rate);
        }
        shopStatusSnapshotService.updateById(snapshot);
    }

    /**
     * 保存区域费用快照
     */
    private void saveAreaFeeSnapshots(Long snapshotId, Long shopId, Map<String, Object> statusData) {
        List<Map<String, Object>> areaFeeList = (List<Map<String, Object>>) statusData.get("areaFeeList");
        if (areaFeeList == null || areaFeeList.isEmpty()) {
            return;
        }

        for (Map<String, Object> feeData : areaFeeList) {
            AreaFeeSnapshot feeSnapshot = new AreaFeeSnapshot();
            feeSnapshot.setSnapshotId(snapshotId);
            feeSnapshot.setShopId(shopId);
            feeSnapshot.setAreaName(getString(feeData, "areaDesc"));
            feeSnapshot.setVipRoom(getInteger(feeData, "viproom"));
            feeSnapshot.setTotalSeats(getInteger(feeData, "totalSeats"));
            feeSnapshot.setIsSingleUp(getInteger(feeData, "isSingleUp"));
            feeSnapshot.setRate(getBigDecimal(feeData, "rate"));
            feeSnapshot.setEstimateFee(getBigDecimal(feeData, "estimateFee"));
            feeSnapshot.setUnitRate(getBigDecimal(feeData, "unitRate"));
            feeSnapshot.setWhole(getInteger(feeData, "whole"));
            areaFeeSnapshotService.save(feeSnapshot);
        }
        log.info("保存区域费用快照: snapshotId={}, shopId={}, count={}", snapshotId, shopId, areaFeeList.size());
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
     *
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
