package com.thematrix.labmanagement.common.service;

import com.thematrix.labmanagement.common.entity.RoleAuthority;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 角色-权限关联表 服务类
 */
public interface RoleAuthorityService extends IService<RoleAuthority> {
    public List<Long> getAuthoritysByRoleIds(List<Long> roleIds);
    public List<Long> getAuthoritysByRoleIds(Long roleId);
    public int deleteByRoleIdAndAuthorityId(Long roleId, Long authorityId);
}
