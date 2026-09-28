package com.thematrix.labmanagement.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.thematrix.labmanagement.common.entity.RoleAuthority;
import com.thematrix.labmanagement.common.mapper.RoleAuthorityMapper;
import com.thematrix.labmanagement.common.service.RoleAuthorityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色-权限关联表 服务实现类
 */
@Service
public class RoleAuthorityServiceImpl extends ServiceImpl<RoleAuthorityMapper, RoleAuthority> implements RoleAuthorityService {
    @Autowired
    RoleAuthorityMapper roleAuthorityMapper;
    public List<Long> getAuthoritysByRoleIds(List<Long> roleIds) {
        QueryWrapper<RoleAuthority> queryWrapper = Wrappers.query();
        queryWrapper.in("role_id", roleIds);

        List<RoleAuthority> roleAuthorities = roleAuthorityMapper.selectList(queryWrapper);

        return roleAuthorities.stream()
                .map(RoleAuthority::getAuthorityId)
                .collect(Collectors.toList());
    }
    public List<Long> getAuthoritysByRoleIds(Long roleId) {
        QueryWrapper<RoleAuthority> queryWrapper = Wrappers.query();
        queryWrapper.in("role_id", roleId);

        List<RoleAuthority> roleAuthorities = roleAuthorityMapper.selectList(queryWrapper);

        return roleAuthorities.stream()
                .map(RoleAuthority::getAuthorityId)
                .collect(Collectors.toList());
    }

    public int deleteByRoleIdAndAuthorityId(Long roleId, Long authorityId) {
        QueryWrapper<RoleAuthority> queryWrapper = Wrappers.query();
        queryWrapper.eq("role_id", roleId).eq("authority_id", authorityId);

        return roleAuthorityMapper.delete(queryWrapper);
    }

}
