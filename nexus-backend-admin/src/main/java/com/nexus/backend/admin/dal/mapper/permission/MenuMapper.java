package com.nexus.backend.admin.dal.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.dal.dataobject.permission.MenuDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统菜单 Mapper
 *
 * @author nexus
 */
@Mapper
public interface MenuMapper extends BaseMapper<MenuDO> {
}
