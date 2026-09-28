package com.thematrix.labmanagement.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.common.entity.Role;

import java.util.List;

/**
 * 系统角色表 服务类
 */
public interface RoleService extends IService<Role> {

    /**
     * 保存角色（含重名校验）
     */
    boolean saveRole(Role role);

    /**
     * 更新角色权限（先删旧权限，再插新权限）
     */
    void updateRoleAuthority(Long roleId, List<Long> authorityIds);

    /**
     * 根据角色 ID 列表获取角色名称列表
     */
    List<String> getUserRoleName(List<Long> roleIds);
}
