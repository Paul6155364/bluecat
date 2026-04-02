package com.bluecat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluecat.entity.MachineInfo;
import com.bluecat.mapper.MachineInfoMapper;
import com.bluecat.service.MachineInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 机器信息表 Service实现
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Service
@RequiredArgsConstructor
public class MachineInfoServiceImpl extends ServiceImpl<MachineInfoMapper, MachineInfo> implements MachineInfoService {

    @Override
    public Page<MachineInfo> pageList(Integer pageNum, Integer pageSize, Long shopId, String areaName, String comName) {
        Page<MachineInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MachineInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(shopId != null, MachineInfo::getShopId, shopId)
                .eq(StringUtils.hasText(areaName), MachineInfo::getAreaName, areaName)
                .like(StringUtils.hasText(comName), MachineInfo::getComName, comName)
                .orderByAsc(MachineInfo::getComName);
        return page(page, wrapper);
    }

    @Override
    public List<MachineInfo> listByShopId(Long shopId) {
        return list(new LambdaQueryWrapper<MachineInfo>()
                .eq(MachineInfo::getShopId, shopId)
                .orderByAsc(MachineInfo::getComName));
    }

    @Override
    public MachineInfo getByShopIdAndComName(Long shopId, String comName) {
        return getOne(new LambdaQueryWrapper<MachineInfo>()
                .eq(MachineInfo::getShopId, shopId)
                .eq(MachineInfo::getComName, comName));
    }

    @Override
    public int countByShopIdAndAreaName(Long shopId, String areaName) {
        return (int) count(new LambdaQueryWrapper<MachineInfo>()
                .eq(MachineInfo::getShopId, shopId)
                .eq(MachineInfo::getAreaName, areaName));
    }

    @Override
    public List<MachineInfo> listByShopIdAndAreaName(Long shopId, String areaName) {
        return list(new LambdaQueryWrapper<MachineInfo>()
                .eq(MachineInfo::getShopId, shopId)
                .eq(MachineInfo::getAreaName, areaName)
                .orderByAsc(MachineInfo::getComName));
    }
}
