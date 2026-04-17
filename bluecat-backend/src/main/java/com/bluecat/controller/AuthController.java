package com.bluecat.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.bluecat.common.Result;
import com.bluecat.config.BusinessException;
import com.bluecat.dto.LoginResultDTO;
import com.bluecat.dto.MenuTreeDTO;
import com.bluecat.entity.SysRole;
import com.bluecat.entity.SysUser;
import com.bluecat.service.SysLoginLogService;
import com.bluecat.service.SysMenuService;
import com.bluecat.service.SysRoleService;
import com.bluecat.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    private final SysLoginLogService sysLoginLogService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<LoginResultDTO> login(@RequestBody java.util.Map<String, String> params, 
                                        HttpServletRequest request) {
        String username = params.get("username");
        String password = params.get("password");
        String ip = getClientIp(request);
        String browser = getBrowser(request);
        String os = getOs(request);

        // 查询用户
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            // 记录登录失败日志
            sysLoginLogService.recordLoginLog(null, username, null, ip, browser, os, 0, "用户不存在");
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!BCrypt.checkpw(password, user.getPassword())) {
            // 记录登录失败日志
            sysLoginLogService.recordLoginLog(user.getId(), username, user.getRealName(), ip, browser, os, 0, "密码错误");
            throw new BusinessException("用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() == 0) {
            // 记录登录失败日志
            sysLoginLogService.recordLoginLog(user.getId(), username, user.getRealName(), ip, browser, os, 0, "账号已被禁用");
            throw new BusinessException("账号已被禁用");
        }

        // 登录
        StpUtil.login(user.getId());

        // 更新最后登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        sysUserService.updateById(user);

        // 记录登录成功日志
        sysLoginLogService.recordLoginLog(user.getId(), username, user.getRealName(), ip, browser, os, 1, "登录成功");

        // 清除密码
        user.setPassword(null);

        // 获取用户角色
        List<SysRole> roles = sysRoleService.listByUserId(user.getId());

        // 获取用户菜单
        List<MenuTreeDTO> menus = sysMenuService.userMenus(user.getId());

        // 构建返回结果
        LoginResultDTO result = new LoginResultDTO();
        result.setToken(StpUtil.getTokenValue());
        result.setUser(user);
        result.setRoles(roles);
        result.setMenus(menus);

        log.info("用户登录成功: userId={}, username={}", user.getId(), username);

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

        // 构建返回结果
        LoginResultDTO result = new LoginResultDTO();
        result.setUser(user);
        result.setRoles(roles);
        result.setMenus(menus);

        return Result.success(result);
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取浏览器信息
     */
    private String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "未知";
        }
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("edge")) {
            return "Edge";
        } else if (userAgent.contains("chrome")) {
            return "Chrome";
        } else if (userAgent.contains("firefox")) {
            return "Firefox";
        } else if (userAgent.contains("safari")) {
            return "Safari";
        } else if (userAgent.contains("opera") || userAgent.contains("opr")) {
            return "Opera";
        } else if (userAgent.contains("msie") || userAgent.contains("trident")) {
            return "IE";
        }
        return "其他";
    }

    /**
     * 获取操作系统信息
     */
    private String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "未知";
        }
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac")) {
            return "Mac OS";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "iOS";
        }
        return "其他";
    }
}
