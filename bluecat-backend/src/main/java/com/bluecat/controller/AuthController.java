package com.bluecat.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.bluecat.common.Result;
import com.bluecat.config.BusinessException;
import com.bluecat.dto.LoginResultDTO;
import com.bluecat.dto.MenuTreeDTO;
import com.bluecat.entity.SysRole;
import com.bluecat.entity.SysUser;
import com.bluecat.service.SysMenuService;
import com.bluecat.service.SysRoleService;
import com.bluecat.service.SysUserService;
import com.bluecat.service.SysUserConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证控制器
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Slf4j
@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysMenuService sysMenuService;
    private final SysUserConfigService sysUserConfigService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<LoginResultDTO> login(@RequestBody java.util.Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        // 查询用户
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 登录
        StpUtil.login(user.getId());

        // 更新最后登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(params.get("ip"));
        sysUserService.updateById(user);

        // 清除密码
        user.setPassword(null);

        // 获取用户角色
        List<SysRole> roles = sysRoleService.listByUserId(user.getId());

        // 获取用户菜单
        List<MenuTreeDTO> menus = sysMenuService.userMenus(user.getId());

        // 获取用户数据权限
        Integer dataScope = user.getDataScope() != null ? user.getDataScope() : 1;
        List<Long> configIdList = null;
        
        // 如果不是全部数据权限，获取授权的网吧配置列表
        if (dataScope != 2) {
            configIdList = sysUserConfigService.getConfigIdsByUserId(user.getId());
        }

        // 构建返回结果
        LoginResultDTO result = new LoginResultDTO();
        result.setToken(StpUtil.getTokenValue());
        result.setUser(user);
        result.setRoles(roles);
        result.setMenus(menus);
        result.setDataScope(dataScope);
        result.setConfigIdList(configIdList);

        log.info("用户登录成功: userId={}, username={}, dataScope={}", user.getId(), username, dataScope);

        return Result.success("登录成功", result);
    }

    @ApiOperation("登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success("登出成功");
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<LoginResultDTO> info() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);
        user.setPassword(null);

        // 获取用户角色
        List<SysRole> roles = sysRoleService.listByUserId(userId);

        // 获取用户菜单
        List<MenuTreeDTO> menus = sysMenuService.userMenus(userId);

        // 获取用户数据权限
        Integer dataScope = user.getDataScope() != null ? user.getDataScope() : 1;
        List<Long> configIdList = null;
        
        // 如果不是全部数据权限，获取授权的网吧配置列表
        if (dataScope != 2) {
            configIdList = sysUserConfigService.getConfigIdsByUserId(userId);
        }

        // 构建返回结果
        LoginResultDTO result = new LoginResultDTO();
        result.setUser(user);
        result.setRoles(roles);
        result.setMenus(menus);
        result.setDataScope(dataScope);
        result.setConfigIdList(configIdList);

        return Result.success(result);
    }
}
