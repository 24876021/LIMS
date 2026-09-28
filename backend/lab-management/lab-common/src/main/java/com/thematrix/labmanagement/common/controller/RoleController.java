package com.thematrix.labmanagement.common.controller;

import com.thematrix.labmanagement.common.entity.Authority;
import com.thematrix.labmanagement.common.entity.Role;
import com.thematrix.labmanagement.common.service.AuthorityService;
import com.thematrix.labmanagement.common.service.RoleAuthorityService;
import com.thematrix.labmanagement.common.service.RoleService;
import com.thematrix.labmanagement.common.service.UserRoleService;
import com.thematrix.labmanagement.common.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统角色表 前端控制器（重构版：业务逻辑已下沉到 Service 层）
 */
@RestController
@Api(tags = "角色管理相关接口")
@RequestMapping("/role")
public class RoleController {

    @Autowired
    RoleService roleService;
    @Autowired
    AuthorityService authorityService;
    @Autowired
    RoleAuthorityService roleAuthorityService;
    @Autowired
    private UserRoleService userRoleService;

    @PreAuthorize("hasAuthority('resource:all')||hasAuthority('resource:get')")
    @GetMapping("/AllRoles")
    @ApiOperation("查询所有角色")
    public Result getAllRoles() {
        List<Role> roleList = roleService.list();
        return roleList.isEmpty() ? Result.error("查询所有角色失败!") : Result.success(roleList);
    }

    @PreAuthorize("hasAuthority('resource:all')||hasAuthority('resource:get')")
    @GetMapping("/AllAuthorities")
    @ApiOperation("查询所有权限")
    public Result getAllAuthorities() {
        List<Authority> authList = authorityService.list();
        return authList.isEmpty() ? Result.error("查询所有权限失败!") : Result.success(authList);
    }

    @PreAuthorize("hasAuthority('resource:all')||hasAuthority('resource:get')")
    @GetMapping("/Authorities")
    @ApiOperation("查询角色所有权限")
    public Result getAuthoritiesByRoleId(@RequestParam(value = "roleId") Long roleId) {
        List<Long> authList = roleAuthorityService.getAuthoritysByRoleIds(roleId);
        return authList.isEmpty() ? Result.error("查询角色所有权限失败!") : Result.success(authList);
    }

    @PreAuthorize("hasAuthority('resource:all')||hasAuthority('resource:set')")
    @PutMapping("/updateRoleAuthority")
    @ApiOperation("更改角色权限")
    public Result updateRoleAuthority(@RequestParam Long roleId, @RequestParam List<Long> authorityIds) {
        roleService.updateRoleAuthority(roleId, authorityIds);
        return Result.success("更改角色权限成功！");
    }

    @PreAuthorize("hasAuthority('resource:all')||hasAuthority('resource:set')")
    @PostMapping("/insert")
    @ApiOperation("插入角色")
    public Result addRole(@RequestBody Role role) {
        try {
            return roleService.saveRole(role) ? Result.success("插入角色成功！") : Result.error("插入角色失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('resource:all')||hasAuthority('resource:remove')")
    @DeleteMapping("/delete/{roleId}")
    @ApiOperation("删除角色")
    public Result deleteRole(@PathVariable Integer roleId) {
        return roleService.removeById(roleId) ? Result.success("删除角色成功！") : Result.error("删除角色失败！");
    }

    @GetMapping("/AuthoritiesByUserId")
    public Result getAuthoritiesByUserId(@RequestParam Long userId) {
        List<Long> roleIds = userRoleService.getRolesByUserId(userId);
        List<Long> authIds = roleAuthorityService.getAuthoritysByRoleIds(roleIds);
        return Result.success(authIds);
    }
}
