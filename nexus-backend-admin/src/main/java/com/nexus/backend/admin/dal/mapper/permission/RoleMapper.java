package com.nexus.backend.admin.dal.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.dal.dataobject.permission.RoleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统角色 Mapper
 *
 * @author nexus
 */
@Mapper
public interface RoleMapper extends BaseMapper<RoleDO> {
}
