package com.thematrix.labmanagement.common.user;

import com.thematrix.labmanagement.common.service.WebSocketPushService;
import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.service.*;
import com.thematrix.labmanagement.common.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 从数据库中验证账户名、密码
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    public static final int MAX_FAIL_COUNT = 5;
    public static final String LOGIN_FAIL_KEY = "login:fail:";
    public static final long LOCK_EXPIRE_SECONDS = 900;
    public static final long MANUAL_LOCK_EXPIRE_SECONDS = 1800;
    public static final String LOCK_KEY = "login:lock:";

    public static final String LOCK_TYPE_PWD_ERROR = "pwd";
    public static final String LOCK_TYPE_MANUAL = "manual";

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    UserRoleService userRoleService;
    @Autowired
    AuthorityService authorityService;
    @Autowired
    RoleAuthorityService roleAuthorityService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private WebSocketPushService webSocketPushService;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {

        SysUser sysUser = sysUserService.getUserByAccount(account);
        if (sysUser == null) {
            throw new UsernameNotFoundException("账户名错误");
        }

        boolean isLocked = false;

        if (!sysUser.isStatus()) {
            String lockKey = LOCK_KEY + sysUser.getAccount();
            Object lockObj = redisUtil.get(lockKey);

            if (lockObj != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, String> map = (Map<String, String>) lockObj;
                    String expireStr = map.get("expire");
                    long expireTime = Long.parseLong(expireStr);

                    if (System.currentTimeMillis() < expireTime) {
                        isLocked = true;
                    } else {
                        sysUser.setStatus(true);
                        sysUserService.updateById(sysUser);
                        redisUtil.del(lockKey);
                        webSocketPushService.pushByRole(1L, "unlock:" + account);
                    }
                } catch (Exception e) {
                    redisUtil.del(lockKey);
                    sysUser.setStatus(true);
                    sysUserService.updateById(sysUser);
                }
            } else {
                Map<String, String> map = new HashMap<>();
                map.put("type", LOCK_TYPE_MANUAL);
                map.put("expire", String.valueOf(System.currentTimeMillis() + MANUAL_LOCK_EXPIRE_SECONDS * 1000));
                redisUtil.set(lockKey, map, MANUAL_LOCK_EXPIRE_SECONDS);
                isLocked = true;
            }
        }

        if (isLocked) {
            return new User(
                    sysUser.getAccount(),
                    sysUser.getPassword(),
                    true,
                    true,
                    true,
                    false,
                    Collections.emptyList()
            );
        }
        return new AccountUser(
                sysUser.getUserId(),
                sysUser.getAccount(),
                sysUser.getPassword(),
                sysUser.isDisable(),
                sysUser.isStatus(),
                getUserAuthority(sysUser.getUserId())
        );
    }


    /**
     * 获取用户权限信息
     * 当用户拥有 module:all 权限时，自动展开为该模块下所有具体操作权限（get/set/add/remove），
     * 以确保与 @PreAuthorize("hasAuthority('module:get')") 等精确匹配兼容。
     * @param userId 用户ID
     * @return 当前权限列表
     */
    public List<GrantedAuthority> getUserAuthority(Long userId) {
        try {
            List<Long> roleIds = userRoleService.getRolesByUserId(userId);
            if (roleIds == null || roleIds.isEmpty())
                return Collections.emptyList();
            List<Long> authorityIds = roleAuthorityService.getAuthoritysByRoleIds(roleIds);
            if (authorityIds == null || authorityIds.isEmpty())
                return Collections.emptyList();

            List<String> authorityNames = authorityService.getAuthorityByAuthoritys(authorityIds);
            // 展开 :all 通配权限
            Set<String> expanded = new java.util.LinkedHashSet<>(authorityNames);
            String[] ops = {"get", "set", "add", "remove"};
            for (String name : authorityNames) {
                if (name.endsWith(":all")) {
                    String prefix = name.substring(0, name.length() - 4);
                    for (String op : ops) {
                        expanded.add(prefix + ":" + op);
                    }
                }
            }
            return expanded.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
