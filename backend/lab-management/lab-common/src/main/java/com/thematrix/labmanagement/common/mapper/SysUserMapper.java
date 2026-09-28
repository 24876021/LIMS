package com.thematrix.labmanagement.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thematrix.labmanagement.common.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统用户表 Mapper 接口
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE account = #{account} LIMIT 1")
    SysUser getByAccount(@Param("account") String account);
}
