package com.nexus.backend.admin.dal.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.dal.dataobject.permission.UserRoleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper
 *
 * @author nexus
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleDO> {
}
