package com.nexus.backend.admin.dal.mapper.permission;

import com.github.yulichang.base.MPJBaseMapper;
import com.nexus.backend.admin.dal.dataobject.permission.RoleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统角色 Mapper
 *
 * @author nexus
 */
@Mapper
public interface RoleMapper extends MPJBaseMapper<RoleDO> {

    /**
     * 批量插入
     *
     * @param list 角色列表
     * @return 插入数量
     */
    int insertBatch(@Param("list") List<RoleDO> list);

    /**
     * 批量更新
     *
     * @param list 角色列表
     * @return 更新数量
     */
    int updateBatch(@Param("list") List<RoleDO> list);
}
