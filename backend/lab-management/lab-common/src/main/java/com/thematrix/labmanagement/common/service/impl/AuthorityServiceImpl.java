package com.thematrix.labmanagement.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.thematrix.labmanagement.common.entity.Authority;
import com.thematrix.labmanagement.common.mapper.AuthorityMapper;
import com.thematrix.labmanagement.common.service.AuthorityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统权限表 服务实现类
 */
@Service
public class AuthorityServiceImpl extends ServiceImpl<AuthorityMapper, Authority> implements AuthorityService {
    @Autowired
    AuthorityMapper authorityMapper;
    public List<String> getAuthorityByAuthoritys(List<Long> authorityIds) {

        QueryWrapper<Authority> queryWrapper = Wrappers.query();
        queryWrapper.in("authority_id", authorityIds);

        List<Authority> authorities = authorityMapper.selectList(queryWrapper);

        return authorities.stream()
                .map(Authority::getAuthorityName)
                .collect(Collectors.toList());
    }

}
