package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.ShopConfig;
import com.bluecat.entity.ShopStatusSnapshot;
import com.bluecat.mapper.ShopConfigMapper;
import com.bluecat.mapper.ShopStatusSnapshotMapper;
import com.bluecat.service.ShopConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopConfigServiceImpl extends ServiceImpl<ShopConfigMapper, ShopConfig> implements ShopConfigService {

    private final ShopStatusSnapshotMapper shopStatusSnapshotMapper;

    @Override
    public Page<ShopConfig> pageList(Integer pageNum, Integer pageSize, String configName, Integer status) {
        Page<ShopConfig> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(configName), ShopConfig::getConfigName, configName)
                .eq(status != null, ShopConfig::getStatus, status)
                .orderByDesc(ShopConfig::getCreateTime);
        Page<ShopConfig> result = page(page, wrapper);

        // 批量查询每个配置的最近采集时间
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            List<Long> configIds = result.getRecords().stream()
                    .map(ShopConfig::getId)
                    .collect(Collectors.toList());

            // 查询这些配置关联门店的最新快照时间
            List<Map<String, Object>> collectTimeList = shopStatusSnapshotMapper.getLatestCollectTimeByConfigIds(configIds);
            Map<Long, String> collectTimeMap = new HashMap<>();
            for (Map<String, Object> item : collectTimeList) {
                Long configId = ((Number) item.get("config_id")).longValue();
                String lastTime = (String) item.get("last_collect_time");
                collectTimeMap.put(configId, lastTime);
            }

            // 设置到配置对象
            result.getRecords().forEach(config -> {
                config.setLastCollectTime(collectTimeMap.get(config.getId()));
            });
        }

        return result;
    }

    @Override
    public ShopConfig getBySnbid(String snbid) {
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<ShopConfig>()
                .eq(ShopConfig::getSnbid, snbid);
        return getOne(wrapper);
    }

    @Override
    public List<ShopConfig> listEnabled() {
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<ShopConfig>()
                .eq(ShopConfig::getStatus, 1)
                .eq(ShopConfig::getPlatformType, 0);
        return list(wrapper);
    }

    @Override
    public List<ShopConfig> listAllEnabled() {
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<ShopConfig>()
                .eq(ShopConfig::getStatus, 1);
        return list(wrapper);
    }
}
