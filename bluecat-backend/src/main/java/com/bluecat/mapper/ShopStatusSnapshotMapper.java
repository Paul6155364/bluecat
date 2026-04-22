package com.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluecat.entity.ShopStatusSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 门店实时状态快照表 Mapper
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Mapper
public interface ShopStatusSnapshotMapper extends BaseMapper<ShopStatusSnapshot> {

    /**
     * 查询指定门店今日累计营收
     * 计算方式：当天该门店所有 area_fee_snapshot 的 estimate_fee 之和
     */
    @Select("SELECT COALESCE(SUM(IFNULL(estimate_fee, 0)), 0) " +
            "FROM area_fee_snapshot " +
            "WHERE shop_id = #{shopId} AND DATE(create_time) = CURDATE()")
    BigDecimal getTodayRevenueByShopId(@Param("shopId") Long shopId);

    /**
     * 更新指定门店最新快照的今日累计营收
     */
    @Update("UPDATE shop_status_snapshot " +
            "SET day_revenue = #{dayRevenue} " +
            "WHERE shop_id = #{shopId} " +
            "  AND DATE(create_time) = CURDATE() " +
            "ORDER BY id DESC LIMIT 1")
    int updateDayRevenueByShopId(@Param("shopId") Long shopId, @Param("dayRevenue") BigDecimal dayRevenue);
}
