package com.thematrix.labmanagement.common.service;

import com.thematrix.labmanagement.common.entity.UserRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户-角色关联表 服务类
 */
public interface UserRoleService extends IService<UserRole> {
    public List<Long> getRolesByUserId(Long userId);
    public int deleteByUserIdAndRoleId(Long userId, Long roleId);
    List<Long> getUserIdsByRoleId(Long roleId);
}
