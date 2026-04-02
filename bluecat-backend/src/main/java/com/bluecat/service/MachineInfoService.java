package com.bluecat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluecat.entity.MachineInfo;

import java.util.List;

/**
 * 机器信息表 Service
 *
 * @author BlueCat
 * @since 2026-03-30
 */
public interface MachineInfoService extends IService<MachineInfo> {

    /**
     * 分页查询
     */
    Page<MachineInfo> pageList(Integer pageNum, Integer pageSize, Long shopId, String areaName, String comName);

    /**
     * 根据门店ID查询机器列表
     */
    List<MachineInfo> listByShopId(Long shopId);

    /**
     * 根据门店ID和机器名称查询
     */
    MachineInfo getByShopIdAndComName(Long shopId, String comName);

    /**
     * 统计某门店某区域的机器数量
     */
    int countByShopIdAndAreaName(Long shopId, String areaName);

    /**
     * 查询某门店某区域的机器列表
     */
    List<MachineInfo> listByShopIdAndAreaName(Long shopId, String areaName);
}
