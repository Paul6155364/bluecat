package com.bluecat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluecat.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员Mapper接口
 *
 * @author BlueCat
 * @since 2026-03-30
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}
