package com.thematrix.labmanagement.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thematrix.labmanagement.common.entity.Role;
import com.thematrix.labmanagement.common.entity.RoleAuthority;
import com.thematrix.labmanagement.common.mapper.RoleMapper;
import com.thematrix.labmanagement.common.service.RoleAuthorityService;
import com.thematrix.labmanagement.common.service.RoleService;
import com.thematrix.labmanagement.common.service.UserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RoleAuthorityService roleAuthorityService;

    @Autowired
    private UserRoleService userRoleService;

    @Override
    public boolean saveRole(Role role) {
        Role exist = getOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleName, role.getRoleName())
        );
        if (exist != null) {
            throw new RuntimeException("角色名称「" + role.getRoleName() + "」已存在，请勿重复添加");
        }
        return save(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleAuthority(Long roleId, List<Long> authorityIds) {
        if (authorityIds == null || authorityIds.isEmpty()) {
            // 清空该角色的所有权限
            roleAuthorityService.remove(
                    new LambdaQueryWrapper<RoleAuthority>().eq(RoleAuthority::getRoleId, roleId)
            );
            // 通知拥有该角色的所有在线用户刷新权限
            List<Long> userIds = userRoleService.getUserIdsByRoleId(roleId);
            for (Long userId : userIds) {
                WebSocketServerImpl.sendToUser(userId, "refreshPermissions");
            }
            return;
        }

        // 增量 diff 策略：只删除多余的，只添加缺失的
        List<Long> currentAuthorityIds = roleAuthorityService.getAuthoritysByRoleIds(roleId);

        // 删除不再需要的权限关联
        for (Long currentAuthorityId : currentAuthorityIds) {
            if (!authorityIds.contains(currentAuthorityId)) {
                roleAuthorityService.deleteByRoleIdAndAuthorityId(roleId, currentAuthorityId);
            }
        }

        // 添加新的权限关联
        for (Long authorityId : authorityIds) {
            if (!currentAuthorityIds.contains(authorityId)) {
                RoleAuthority roleAuthority = new RoleAuthority();
                roleAuthority.setRoleId(roleId);
                roleAuthority.setAuthorityId(authorityId);
                roleAuthorityService.save(roleAuthority);
            }
        }

        // 通知拥有该角色的所有在线用户刷新权限
        List<Long> userIds = userRoleService.getUserIdsByRoleId(roleId);
        for (Long userId : userIds) {
            WebSocketServerImpl.sendToUser(userId, "refreshPermissions");
        }
    }

    @Override
    public List<String> getUserRoleName(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return listByIds(roleIds).stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }
}
