package com.bluecat.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluecat.common.Result;
import com.bluecat.config.BusinessException;
import com.bluecat.dto.ShopPkRelationDTO;
import com.bluecat.entity.ShopPkRelation;
import com.bluecat.service.ShopPkRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 门店PK关系控制器
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Api(tags = "门店PK关系管理")
@RestController
@RequestMapping("/shop/pk/relation")
@RequiredArgsConstructor
public class ShopPkRelationController {

    private final ShopPkRelationService shopPkRelationService;

    @ApiOperation("获取PK关系列表")
    @GetMapping("/list")
    public Result<List<ShopPkRelation>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<ShopPkRelation> list = shopPkRelationService.listByUserId(userId);
        return Result.success(list);
    }

    @ApiOperation("获取PK关系详情")
    @GetMapping("/{id}")
    public Result<ShopPkRelation> getById(@PathVariable Long id) {
        ShopPkRelation relation = shopPkRelationService.getById(id);
        if (relation == null) {
            throw new BusinessException("PK关系不存在");
        }
        return Result.success(relation);
    }

    @ApiOperation("新增PK关系")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody ShopPkRelationDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        ShopPkRelation relation = new ShopPkRelation();
        BeanUtils.copyProperties(dto, relation);
        relation.setCompetitorShopIds(dto.getCompetitorShopIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));
        if (dto.getCompetitorShopNames() != null) {
            relation.setCompetitorShopNames(String.join(",", dto.getCompetitorShopNames()));
        }
        relation.setCreateBy(userId);

        shopPkRelationService.save(relation);
        return Result.success();
    }

    @ApiOperation("编辑PK关系")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ShopPkRelationDTO dto) {
        ShopPkRelation relation = shopPkRelationService.getById(id);
        if (relation == null) {
            throw new BusinessException("PK关系不存在");
        }

        relation.setName(dto.getName());
        relation.setMainShopId(dto.getMainShopId());
        relation.setMainShopName(dto.getMainShopName());
        relation.setCompetitorShopIds(dto.getCompetitorShopIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));
        if (dto.getCompetitorShopNames() != null) {
            relation.setCompetitorShopNames(String.join(",", dto.getCompetitorShopNames()));
        }

        shopPkRelationService.updateById(relation);
        return Result.success();
    }

    @ApiOperation("删除PK关系")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shopPkRelationService.removeById(id);
        return Result.success();
    }
}
