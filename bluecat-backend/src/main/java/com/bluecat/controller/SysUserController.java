package com.bluecat.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluecat.annotation.DataScope;
import com.bluecat.common.PageResult;
import com.bluecat.common.Result;
import com.bluecat.config.BusinessException;
import com.bluecat.dto.UserDTO;
import com.bluecat.entity.ShopConfig;
import com.bluecat.entity.SysUser;
import com.bluecat.service.ShopConfigService;
import com.bluecat.service.SysMenuService;
import com.bluecat.service.SysUserRoleService;
import com.bluecat.service.SysUserService;
import com.bluecat.service.SysUserConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 *
 * @author BlueCat
 * @since 2026-04-02
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;
    private final SysUserRoleService sysUserRoleService;
    private final SysMenuService sysMenuService;
    private final SysUserConfigService sysUserConfigService;
    private final ShopConfigService shopConfigService;

    @ApiOperation("用户列表")
    @GetMapping("/list")
    public Result<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String username, String realName, Integer status) {

        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(username != null, SysUser::getUsername, username)
                .like(realName != null, SysUser::getRealName, realName)
                .eq(status != null, SysUser::getStatus, status)
                .eq(SysUser::getDeleted, 0)
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> result = sysUserService.page(page, wrapper);

        return Result.success(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @ApiOperation("新增用户")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody UserDTO userDTO) {
        // 检查用户名是否已存在
        long count = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, userDTO.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        // 默认密码: 123456（检查 null 和空字符串）
        String defaultPassword = (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty())
                ? userDTO.getPassword()
                : "123456";
        user.setPassword(BCrypt.hashpw(defaultPassword, BCrypt.gensalt()));
        user.setStatus(userDTO.getStatus() != null ? userDTO.getStatus() : 1);
        user.setCreateTime(LocalDateTime.now());
        user.setDeleted(0);

        sysUserService.save(user);

        // 分配角色
        if (userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            sysUserRoleService.assignRoles(user.getId(), userDTO.getRoleIds());
        }

        return Result.success();
    }

    @ApiOperation("修改密码")
    @PutMapping("/updatePwd")
    public Result<Void> updatePwd(@RequestBody UserDTO userDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!BCrypt.checkpw(userDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 更新新密码
        user.setPassword(BCrypt.hashpw(userDTO.getNewPassword(), BCrypt.gensalt()));
        user.setUpdateTime(LocalDateTime.now());
        sysUserService.updateById(user);

        return Result.success("密码修改成功");
    }

    @ApiOperation("编辑用户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 不能修改用户名
        user.setRealName(userDTO.getRealName());
        user.setPhone(userDTO.getPhone());
        user.setEmail(userDTO.getEmail());
        user.setAvatar(userDTO.getAvatar());
        user.setStatus(userDTO.getStatus());
        user.setUpdateTime(LocalDateTime.now());

        sysUserService.updateById(user);

        // 更新角色
        if (userDTO.getRoleIds() != null) {
            sysUserRoleService.assignRoles(id, userDTO.getRoleIds());
        }

        return Result.success();
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 不能删除自己
        if (id.equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException("不能删除当前登录用户");
        }

        // 删除用户角色关联
        sysUserRoleService.deleteByUserId(id);

        // 删除用户网吧配置关联
        sysUserConfigService.deleteUserConfigs(id);

        // 逻辑删除用户
        user.setDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        sysUserService.updateById(user);

        return Result.success();
    }

    @ApiOperation("重置密码")
    @PutMapping("/resetPwd/{id}")
    public Result<Void> resetPwd(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 重置为默认密码: 123456
        user.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt()));
        user.setUpdateTime(LocalDateTime.now());
        sysUserService.updateById(user);

        return Result.success("密码已重置为: 123456");
    }

    @ApiOperation("分配角色")
    @PutMapping("/assignRole")
    public Result<Void> assignRole(@RequestBody UserDTO userDTO) {
        sysUserRoleService.assignRoles(userDTO.getId(), userDTO.getRoleIds());
        return Result.success();
    }

    @ApiOperation("获取用户角色ID列表")
    @GetMapping("/roles/{userId}")
    public Result<List<Long>> getUserRoles(@PathVariable Long userId) {
        List<Long> roleIds = sysUserRoleService.listRoleIdsByUserId(userId);
        return Result.success(roleIds);
    }

    // ==================== 数据权限管理 ====================

    @ApiOperation("获取用户授权的网吧配置列表")
    @GetMapping("/configs/{userId}")
    public Result<List<ShopConfig>> getUserConfigs(@PathVariable Long userId) {
        // 获取用户授权的网吧配置ID列表
        List<Long> configIds = sysUserConfigService.getConfigIdsByUserId(userId);
        
        if (configIds == null || configIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 查询网吧配置详情
        List<ShopConfig> configs = shopConfigService.listByIds(configIds);
        return Result.success(configs);
    }

    @ApiOperation("保存用户授权网吧配置")
    @PostMapping("/configs/{userId}")
    public Result<Void> saveUserConfigs(@PathVariable Long userId, @RequestBody List<Long> configIds) {
        // 检查用户是否存在
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 保存用户授权网吧配置
        sysUserConfigService.saveUserConfigs(userId, configIds);

        return Result.success("保存成功");
    }

    @ApiOperation("获取所有网吧配置列表（用于授权选择）")
    @GetMapping("/all-configs")
    public Result<List<ShopConfig>> getAllConfigs() {
        List<ShopConfig> configs = shopConfigService.list();
        return Result.success(configs);
    }

    @ApiOperation("获取用户未授权的网吧配置列表")
    @GetMapping("/unauthorized-configs/{userId}")
    public Result<List<ShopConfig>> getUnauthorizedConfigs(@PathVariable Long userId) {
        // 获取用户授权的网吧配置ID列表
        List<Long> authorizedConfigIds = sysUserConfigService.getConfigIdsByUserId(userId);

        // 查询所有网吧配置
        List<ShopConfig> allConfigs = shopConfigService.list();

        // 过滤出未授权的网吧配置
        List<ShopConfig> unauthorizedConfigs = allConfigs.stream()
                .filter(config -> authorizedConfigIds == null || !authorizedConfigIds.contains(config.getId()))
                .collect(Collectors.toList());

        return Result.success(unauthorizedConfigs);
    }

    @ApiOperation("更新用户数据权限范围")
    @PutMapping("/data-scope/{userId}")
    public Result<Void> updateDataScope(@PathVariable Long userId, @RequestBody Map<String, Integer> params) {
        // 检查用户是否存在
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新数据权限范围
        Integer dataScope = params.get("dataScope");
        if (dataScope == null || (dataScope != 1 && dataScope != 2)) {
            throw new BusinessException("数据权限范围参数错误");
        }

        user.setDataScope(dataScope);
        user.setUpdateTime(LocalDateTime.now());
        sysUserService.updateById(user);

        // 如果是全部数据权限，清除网吧配置授权
        if (dataScope == 2) {
            sysUserConfigService.deleteUserConfigs(userId);
        }

        return Result.success("更新成功");
    }

    @ApiOperation("批量授权网吧配置给多个用户")
    @PostMapping("/batch-configs")
    public Result<Void> batchAssignConfigs(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> userIds = (List<Long>) params.get("userIds");
        @SuppressWarnings("unchecked")
        List<Long> configIds = (List<Long>) params.get("configIds");

        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("用户ID列表不能为空");
        }
        if (configIds == null || configIds.isEmpty()) {
            throw new BusinessException("网吧配置ID列表不能为空");
        }

        // 批量授权
        for (Long userId : userIds) {
            sysUserConfigService.saveUserConfigs(userId, configIds);
        }

        return Result.success("批量授权成功");
    }
}
