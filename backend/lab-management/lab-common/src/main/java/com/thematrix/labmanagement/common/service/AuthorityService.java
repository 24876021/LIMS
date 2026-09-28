package com.thematrix.labmanagement.common.service;

import com.thematrix.labmanagement.common.entity.Authority;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统权限表 服务类
 */
public interface AuthorityService extends IService<Authority> {
    public List<String> getAuthorityByAuthoritys(List<Long> authorityIds);

}
