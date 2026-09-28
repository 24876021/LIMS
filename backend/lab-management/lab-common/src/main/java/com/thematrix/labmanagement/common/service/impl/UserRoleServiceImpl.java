package com.thematrix.labmanagement.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.thematrix.labmanagement.common.entity.UserRole;
import com.thematrix.labmanagement.common.mapper.UserRoleMapper;
import com.thematrix.labmanagement.common.service.UserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户-角色关联表 服务实现类
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
    @Autowired
    UserRoleMapper userRoleMapper;
    public List<Long> getRolesByUserId(Long userId) {
        QueryWrapper<UserRole> queryWrapper = Wrappers.query();
        queryWrapper.eq("user_id", userId);

        List<UserRole> userRoles = userRoleMapper.selectList(queryWrapper);

        return userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
    }

    public int deleteByUserIdAndRoleId(Long userId, Long roleId) {
        QueryWrapper<UserRole> queryWrapper = Wrappers.query();
        queryWrapper.eq("user_id", userId).eq("role_id", roleId);

        return userRoleMapper.delete(queryWrapper);
    }

    @Override
    public List<Long> getUserIdsByRoleId(Long roleId) {
        QueryWrapper<UserRole> queryWrapper = Wrappers.query();
        queryWrapper.eq("role_id", roleId);
        List<UserRole> userRoles = userRoleMapper.selectList(queryWrapper);
        return userRoles.stream()
                .map(UserRole::getUserId)
                .collect(Collectors.toList());
    }
}
