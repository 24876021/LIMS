package com.thematrix.labmanagement.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thematrix.labmanagement.common.constant.RsaProperties;
import com.thematrix.labmanagement.common.entity.Department;
import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.entity.UserRole;
import com.thematrix.labmanagement.common.handler.password.PasswordEncoder;
import com.thematrix.labmanagement.common.mapper.SysUserMapper;
import com.thematrix.labmanagement.common.service.*;
import com.thematrix.labmanagement.common.user.UserDetailServiceImpl;
import com.thematrix.labmanagement.common.utils.RSAUtils;
import com.thematrix.labmanagement.common.utils.RedisUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RoleAuthorityService roleAuthorityService;

    @Autowired
    private AuthorityService authorityService;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RoleService roleService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public SysUser getUserByAccount(String account) {
        return getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getAccount, account));
    }

    @Override
    public String getRegisterPushMessage(SysUser sysUser) {
        return "新用户注册通知：账号 " + sysUser.getAccount() + " 已注册，请及时审核。";
    }

    @Override
    public void register(SysUser sysUser) {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        try {
            // 1. 账号唯一性校验
            SysUser existing = getUserByAccount(sysUser.getAccount());
            if (existing != null) {
                throw new RuntimeException("当前账号已经存在，请更换账号");
            }
            // 2. RSA 解密前端传来的加密密码
            String pwd = RSAUtils.decryptByPrivate(sysUser.getPassword(), RsaProperties.privateKey);
            // 3. BCrypt 编码
            String encodePwd = passwordEncoder.encode(pwd);
            sysUser.setPassword(encodePwd);
            // 4. 设置初始状态
            sysUser.setStatus(true);
            sysUser.setDisable(true);
            if (sysUser.getCtime() == null) {
                sysUser.setCtime(java.time.LocalDateTime.now());
            }
            // 5. 保存用户
            save(sysUser);
            // 6. 分配默认角色（roleId=4）
            Long userId = sysUser.getUserId();
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(4L);
            userRoleService.save(userRole);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateUserRole(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            // 清空所有角色
            userRoleService.remove(
                    new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
            );
            // WebSocket 通知用户刷新权限
            WebSocketServerImpl.sendToUser(userId, "refreshPermissions");
            // 通知用户角色已被清空
            notificationService.sendNotification(null, userId,
                    "角色权限变更",
                    "管理员已清空您的所有角色权限，如权限未生效请刷新页面。",
                    "warning");
            return;
        }

        // 1. 查询现有角色
        List<Long> currentRoleIds = userRoleService.getRolesByUserId(userId);
        // 2. 删除不再需要的角色关联
        for (Long roleId : currentRoleIds) {
            if (!roleIds.contains(roleId)) {
                userRoleService.deleteByUserIdAndRoleId(userId, roleId);
            }
        }
        // 3. 添加新的角色关联
        for (Long roleId : roleIds) {
            if (!currentRoleIds.contains(roleId)) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleService.save(userRole);
            }
        }

        // WebSocket 通知用户刷新权限
        WebSocketServerImpl.sendToUser(userId, "refreshPermissions");

        // 通知用户角色已更新
        List<String> roleNames = roleService.getUserRoleName(roleIds);
        String roleStr = roleNames != null && !roleNames.isEmpty()
                ? "，当前角色：" + String.join("、", roleNames)
                : "";
        notificationService.sendNotification(null, userId,
                "角色权限变更",
                "管理员已更新您的角色权限" + roleStr + "，如权限未生效请刷新页面。",
                "info");
    }

    @Override
    public void updateUserStatus(SysUser sysUser) {
        // 如果用户被启用（status=true），清除 Redis 登录锁定计数
        String lockKey = UserDetailServiceImpl.LOCK_KEY + sysUser.getAccount();
        if (sysUser.isStatus()) {
            redisUtil.del(lockKey);
        }

        updateById(sysUser);

        // 通知被操作的用户账号状态变更
        if (sysUser.getUserId() != null) {
            if (!sysUser.isDisable()) {
                // disable=false 表示账号被禁用
                notificationService.sendNotification(null, sysUser.getUserId(),
                        "账号已被禁用",
                        "您的账号已被管理员禁用，如有疑问请联系管理员。",
                        "warning");
            } else if (sysUser.isDisable() && sysUser.isStatus()) {
                // 账号重新启用/解锁
                notificationService.sendNotification(null, sysUser.getUserId(),
                        "账号已恢复正常",
                        "您的账号已被管理员解锁/启用，可以正常登录使用。",
                        "success");
            }
        }
    }

    @Override
    public List<SysUser> getByUsername(String name) {
        return list(new LambdaQueryWrapper<SysUser>().eq(SysUser::getName, name));
    }

    @Override
    public List<SysUserService.SysUserSimpleDTO> getSimpleUserList() {
        // 仅查询启用状态的用户（与源码一致：eq(SysUser::isStatus, true)）
        List<SysUser> users = list(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::isStatus, true)
        );
        // 查询所有部门，建立 departmentId -> departmentName 映射
        List<Department> departments = departmentService.list();
        Map<Integer, String> deptMap = departments.stream()
                .collect(Collectors.toMap(
                        Department::getDepartmentId,
                        Department::getDepartmentName,
                        (a, b) -> a
                ));
        return users.stream().map(user -> {
            SysUserService.SysUserSimpleDTO dto = new SysUserService.SysUserSimpleDTO();
            dto.setUserId(user.getUserId());
            dto.setAccount(user.getAccount());
            dto.setName(user.getName());
            dto.setDepartmentId(user.getDepartmentId());
            dto.setDepartmentName(deptMap.get(user.getDepartmentId()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择图片文件");
        }
        SysUser sysUser = getById(userId);
        if (sysUser == null) {
            throw new RuntimeException("用户不存在");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = "avatar_" + userId + "_" + System.currentTimeMillis() + extension;

            String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File destFile = new File(uploadDir + filename);
            file.transferTo(destFile);

            String newAvatarUrl = "/uploads/avatar/" + filename;
            sysUser.setAvatar(newAvatarUrl);

            if (!updateById(sysUser)) {
                throw new RuntimeException("头像更新失败");
            }

            // 清理多余的历史头像：最多保留6张（1当前+5历史），按时间倒序删除旧的
            File[] allFiles = dir.listFiles((d, name) -> name.startsWith("avatar_" + userId + "_"));
            if (allFiles != null && allFiles.length > 6) {
                java.util.Arrays.sort(allFiles, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                for (int i = 6; i < allFiles.length; i++) {
                    allFiles[i].delete();
                }
            }
            return newAvatarUrl;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public String switchAvatar(Long userId, String avatarUrl) {
        SysUser sysUser = getById(userId);
        if (sysUser == null) {
            throw new RuntimeException("用户不存在");
        }
        // 安全校验：只允许使用自己的头像
        String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";
        File targetFile = new File(uploadDir + avatarUrl.replace("/uploads/avatar/", ""));
        if (!targetFile.exists()) {
            throw new RuntimeException("头像文件不存在");
        }
        sysUser.setAvatar(avatarUrl);
        targetFile.setLastModified(System.currentTimeMillis());
        if (!updateById(sysUser)) {
            throw new RuntimeException("头像切换失败");
        }
        return avatarUrl;
    }

    @Override
    public List<Map<String, Object>> getAvatarHistory(Long userId) {
        String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            return new ArrayList<>();
        }
        File[] files = dir.listFiles((d, name) -> name.startsWith("avatar_" + userId + "_"));
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }
        // 获取当前头像文件名
        SysUser sysUser = getById(userId);
        String currentAvatar = sysUser != null ? sysUser.getAvatar() : null;
        String currentFilename = currentAvatar != null ? currentAvatar.replace("/uploads/avatar/", "") : null;

        // 当前头像排最前，其余按修改时间倒序
        java.util.Arrays.sort(files, (a, b) -> {
            if (a.getName().equals(currentFilename)) return -1;
            if (b.getName().equals(currentFilename)) return 1;
            return Long.compare(b.lastModified(), a.lastModified());
        });
        List<Map<String, Object>> list = new ArrayList<>();
        for (File f : files) {
            Map<String, Object> item = new HashMap<>();
            item.put("filename", f.getName());
            item.put("url", "/uploads/avatar/" + f.getName());
            item.put("size", f.length());
            item.put("lastModified", new java.util.Date(f.lastModified()));
            list.add(item);
        }
        return list;
    }

    @Override
    public void deleteAvatar(Long userId, String filename) {
        // 安全校验：只允许删除自己的头像
        if (!filename.startsWith("avatar_" + userId + "_")) {
            throw new RuntimeException("无权删除此文件");
        }
        // 不允许删除当前正在使用的头像
        SysUser sysUser = getById(userId);
        if (sysUser != null && filename.equals(sysUser.getAvatar())) {
            throw new RuntimeException("不能删除当前正在使用的头像");
        }
        String uploadDir = System.getProperty("user.dir") + "/uploads/avatar/";
        File file = new File(uploadDir + filename);
        if (!file.exists() || !file.delete()) {
            throw new RuntimeException("文件不存在或删除失败");
        }
    }

    @Override
    public Object getUserRoleAndAuthority(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 查询用户基本信息，获取 account
        SysUser sysUser = getById(userId);
        result.put("account", sysUser != null ? sysUser.getAccount() : "");

        // 2. 查询用户拥有哪些角色（返回角色名称，与源码一致）
        List<Long> roleIds = userRoleService.getRolesByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            throw new RuntimeException("用户没有角色");
        }
        List<String> roleNames = roleService.getUserRoleName(roleIds);
        if (roleNames == null || roleNames.isEmpty()) {
            throw new RuntimeException("用户角色信息为空");
        }
        result.put("role", roleNames);

        // 3. 通过角色→权限关联表，查询用户拥有哪些权限名称
        List<Long> authorityIds = roleAuthorityService.getAuthoritysByRoleIds(roleIds);
        List<String> authorityNames = authorityService.getAuthorityByAuthoritys(authorityIds);
        result.put("authority", authorityNames);

        return result;
    }
}
