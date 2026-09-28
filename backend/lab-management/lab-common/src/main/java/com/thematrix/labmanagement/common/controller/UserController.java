package com.thematrix.labmanagement.common.controller;

import com.thematrix.labmanagement.common.annotation.NoLog;
import com.thematrix.labmanagement.common.entity.*;
import com.thematrix.labmanagement.common.service.*;
import com.thematrix.labmanagement.common.service.WebSocketPushService;
import com.thematrix.labmanagement.common.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@Api(tags = "用户管理相关接口")
@RequestMapping("/sysUser")
public class UserController {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private WebSocketPushService webSocketPushService;

    @NoLog("注册接口含密码明文，禁止日志记录")
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result register(@RequestBody SysUser sysUser) {
        sysUserService.register(sysUser);
        webSocketPushService.pushByRole(1L, sysUserService.getRegisterPushMessage(sysUser));
        return Result.success("注册成功！");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/RoleAndAndAuthority")
    @ApiOperation("获取指定用户的账户名与角色信息与权限信息")
    public Result getUserRoleAndAuthority(@RequestParam(value = "userId") Long userId) {
        return Result.success(sysUserService.getUserRoleAndAuthority(userId));
    }

    @PreAuthorize("hasAuthority('resource:all')||hasAuthority('resource:get')")
    @GetMapping("/roles")
    @ApiOperation("查询用户所有角色")
    public Result getRolesbyUserId(@RequestParam(value = "userId") Long userId) {
        List<Long> roleList = userRoleService.getRolesByUserId(userId);
        if (!roleList.isEmpty()) {
            return Result.success(roleList);
        } else {
            return Result.error("查询用户所有角色失败!");
        }
    }

    @PreAuthorize("hasAuthority('resource:all')||hasAuthority('resource:set')")
    @PutMapping("/updateUserRole")
    @ApiOperation("更改用户角色")
    public Result updateUserRole(@RequestParam Long userId, @RequestParam List<Long> roleIds) {
        sysUserService.updateUserRole(userId, roleIds);
        return Result.success("更改用户角色成功！");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user")
    @ApiOperation("根据 userId 或 name 查询指定用户")
    public Result getUserByIdOrName(@RequestParam(value = "userId", required = false) Integer userId,
                                     @RequestParam(value = "name", required = false) String name) {
        SysUser sysUser = null;
        List<SysUser> sysUserList = null;
        if (userId != null) {
            sysUser = sysUserService.getById(userId);
        } else if (name != null) {
            sysUserList = sysUserService.getByUsername(name);
        } else {
            return Result.error("请提供 userId 或 name 参数");
        }

        if (sysUser == null && sysUserList == null) {
            return Result.error("未找到指定用户");
        }

        if (sysUser != null) {
            return Result.success(sysUser);
        } else {
            return Result.success(sysUserList);
        }
    }

    @PreAuthorize("hasAuthority('resource:all')||hasAuthority('resource:get')")
    @GetMapping("/AllUsers")
    @ApiOperation("查询所有用户")
    public Result getAllUsers() {
        List<SysUser> userList = sysUserService.list();
        if (!userList.isEmpty()) {
            return Result.success(userList);
        } else {
            return Result.error("查询所有用户失败!");
        }
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('resource:set')")
    @PutMapping("/updateStatusAndDisable")
    @ApiOperation("修改用户状态")
    public Result updateUserStatus(@RequestBody SysUser sysUser) {
        sysUserService.updateUserStatus(sysUser);
        return Result.success("更新状态成功！");
    }

    @NoLog("头像上传含文件流，无需记录日志")
    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('resource:get')")
    @PostMapping("/uploadAvatar")
    @ApiOperation("上传用户头像或切换历史头像")
    public Result uploadAvatar(@RequestParam("userId") Long userId,
                                @RequestParam(value = "file", required = false) MultipartFile file,
                                @RequestParam(value = "avatarUrl", required = false) String avatarUrl) {
        // 切换历史头像
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            String url = sysUserService.switchAvatar(userId, avatarUrl);
            return Result.success("头像切换成功", url);
        }

        // 上传新头像
        String url = sysUserService.uploadAvatar(userId, file);
        return Result.success("头像更新成功", url);
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('resource:get')")
    @GetMapping("/avatarHistory")
    @ApiOperation("获取用户历史头像列表")
    public Result avatarHistory(@RequestParam("userId") Long userId) {
        return Result.success("获取成功", sysUserService.getAvatarHistory(userId));
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('resource:set')")
    @DeleteMapping("/deleteAvatar")
    @ApiOperation("删除历史头像")
    public Result deleteAvatar(@RequestParam("userId") Long userId,
                                @RequestParam("filename") String filename) {
        sysUserService.deleteAvatar(userId, filename);
        return Result.success("删除成功");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/simple-list")
    @ApiOperation("获取用户简要列表（供各模块下拉框使用，所有登录用户可访问）")
    public Result getSimpleUserList() {
        return Result.success(sysUserService.getSimpleUserList());
    }
}
