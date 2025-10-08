package com.nexus.backend.admin.dal.mapper.tenant;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.dal.dataobject.tenant.TenantDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租户管理表 Mapper 接口
 *
 * @author beckend
 * @since 2025-10-08
 */
@Mapper
public interface TenantMapper extends BaseMapper<TenantDO> {

    /**
     * 批量插入
     * 
     * @param list 数据列表
     * @return 插入条数
     */
    int insertBatch(@Param("list") List<TenantDO> list);

    /**
     * 批量更新
     * 
     * @param list 数据列表
     * @return 更新条数
     */
    int updateBatch(@Param("list") List<TenantDO> list);
}

