package com.bluecat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 门店PK关系实体
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Data
@TableName("shop_pk_relation")
public class ShopPkRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * PK关系名称
     */
    private String name;

    /**
     * 主门店ID
     */
    private Long mainShopId;

    /**
     * 主门店名称（冗余存储）
     */
    private String mainShopName;

    /**
     * PK对手门店ID列表（逗号分隔）
     */
    private String competitorShopIds;

    /**
     * PK对手门店名称列表（逗号分隔，冗余存储）
     */
    private String competitorShopNames;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记 0-正常 1-删除
     */
    @TableLogic
    private Integer deleted;
}
