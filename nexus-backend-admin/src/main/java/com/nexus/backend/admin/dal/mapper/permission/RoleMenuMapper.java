package com.nexus.backend.admin.dal.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.dal.dataobject.permission.RoleMenuDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色菜单关联 Mapper
 *
 * @author nexus
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenuDO> {
}
