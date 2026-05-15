package com.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluecat.entity.ShopStatusSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 门店实时状态快照表 Mapper
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Mapper
public interface ShopStatusSnapshotMapper extends BaseMapper<ShopStatusSnapshot> {

    /**
     * 获取所有门店最新快照（优化后的JOIN查询）
     */
    @Select("SELECT s.* FROM shop_status_snapshot s " +
            "INNER JOIN (" +
            "    SELECT shop_id, MAX(snapshot_time) as max_time " +
            "    FROM shop_status_snapshot " +
            "    GROUP BY shop_id" +
            ") t ON s.shop_id = t.shop_id AND s.snapshot_time = t.max_time " +
            "ORDER BY s.occupancy_rate DESC")
    List<ShopStatusSnapshot> selectLatestAll();

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

    /**
     * 批量查询配置关联的最新采集时间
     * 通过 shop_info 表关联 shop_status_snapshot 表
     * 返回 Map: configId -> lastCollectTime
     */
    @Select("<script>" +
            "SELECT sc.id AS config_id, " +
            "       DATE_FORMAT(MAX(ss.snapshot_time), '%Y-%m-%d %H:%i:%s') AS last_collect_time " +
            "FROM shop_config sc " +
            "LEFT JOIN shop_info si ON si.config_id = sc.id " +
            "LEFT JOIN shop_status_snapshot ss ON ss.shop_id = si.id " +
            "WHERE sc.id IN " +
            "<foreach collection='configIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "GROUP BY sc.id" +
            "</script>")
    List<Map<String, Object>> getLatestCollectTimeByConfigIds(@Param("configIds") List<Long> configIds);
}
