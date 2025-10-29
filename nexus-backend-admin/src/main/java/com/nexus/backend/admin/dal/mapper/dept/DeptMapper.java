package com.nexus.backend.admin.dal.mapper.dept;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.dal.dataobject.dept.DeptDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门管理表 Mapper 接口
 *
 * @author beckend
 * @since 2025-10-28
 */
@Mapper
public interface DeptMapper extends BaseMapper<DeptDO> {

    /**
     * 批量插入
     * 
     * @param list 数据列表
     * @return 插入条数
     */
    int insertBatch(@Param("list") List<DeptDO> list);

    /**
     * 批量更新
     * 
     * @param list 数据列表
     * @return 更新条数
     */
    int updateBatch(@Param("list") List<DeptDO> list);
}

