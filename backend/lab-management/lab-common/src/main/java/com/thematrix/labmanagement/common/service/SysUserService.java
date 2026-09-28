package com.thematrix.labmanagement.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.common.entity.SysUser;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 系统用户 服务类
 */
public interface SysUserService extends IService<SysUser> {

    void register(SysUser sysUser);

    SysUser getUserByAccount(String account);

    void updateUserRole(Long userId, List<Long> roleIds);

    void updateUserStatus(SysUser sysUser);

    List<SysUser> getByUsername(String name);

    List<SysUserSimpleDTO> getSimpleUserList();

    String uploadAvatar(Long userId, org.springframework.web.multipart.MultipartFile file);

    String switchAvatar(Long userId, String avatarUrl);

    List<Map<String, Object>> getAvatarHistory(Long userId);

    void deleteAvatar(Long userId, String filename);

    /**
     * 注册成功后的推送消息体
     */
    String getRegisterPushMessage(SysUser sysUser);

    /**
     * 获取用户角色与权限信息
     */
    Object getUserRoleAndAuthority(Long userId);

    @Setter
    @Getter
    class SysUserSimpleDTO {
        private Long userId;
        private String account;
        private String name;
        private Integer departmentId;
        private String departmentName;
    }
}
