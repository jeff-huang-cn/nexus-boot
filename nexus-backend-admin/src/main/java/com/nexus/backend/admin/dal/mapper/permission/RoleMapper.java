package com.nexus.backend.admin.dal.mapper.permission;

import com.github.yulichang.base.MPJBaseMapper;
import com.nexus.backend.admin.dal.dataobject.permission.RoleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统角色 Mapper
 *
 * @author nexus
 */
@Mapper
public interface RoleMapper extends MPJBaseMapper<RoleDO> {
}
