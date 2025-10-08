package com.nexus.backend.admin.service.tenant;

import com.nexus.backend.admin.controller.tenant.vo.*;
import com.nexus.backend.admin.dal.dataobject.tenant.TenantDO;
import com.nexus.framework.web.result.PageResult;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 租户管理表 Service 接口
 *
 * @author beckend
 * @since 2025-10-08
 */
public interface TenantService {

    /**
     * 创建租户管理表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long create(@Valid TenantSaveReqVO createReqVO);

    /**
     * 更新租户管理表
     *
     * @param updateReqVO 更新信息
     */
    void update(@Valid TenantSaveReqVO updateReqVO);

    /**
     * 删除租户管理表
     *
     * @param id 编号
     */
    void delete(Long id);

    /**
     * 批量创建租户管理表
     *
     * @param createReqVOs 创建信息列表
     */
    void batchCreate(List<TenantSaveReqVO> createReqVOs);

    /**
     * 批量更新租户管理表
     *
     * @param updateReqVOs 更新信息列表
     */
    void batchUpdate(List<TenantSaveReqVO> updateReqVOs);

    /**
     * 批量删除租户管理表
     *
     * @param ids 编号列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 获得租户管理表详情
     *
     * @param id 编号
     * @return 租户管理表详情
     */
    TenantDO getById(Long id);

    /**
     * 获得租户管理表分页
     *
     * @param pageReqVO 分页查询
     * @return 租户管理表分页
     */
    PageResult<TenantRespVO> getPage(TenantPageReqVO pageReqVO);

    /**
     * 获得租户管理表列表（用于导出）
     *
     * @param pageReqVO 查询条件
     * @return 租户管理表列表
     */
    List<TenantDO> getList(TenantPageReqVO pageReqVO);

}

