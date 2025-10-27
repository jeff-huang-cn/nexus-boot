package com.nexus.backend.admin.service.dept;

import com.nexus.backend.admin.controller.dept.vo.*;
import com.nexus.backend.admin.dal.dataobject.dept.DeptDO;
import com.nexus.framework.web.result.PageResult;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 部门管理表 Service 接口
 *
 * @author beckend
 * @since 2025-10-27
 */
public interface DeptService {

    /**
     * 创建部门管理表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long create(@Valid DeptSaveReqVO createReqVO);

    /**
     * 更新部门管理表
     *
     * @param updateReqVO 更新信息
     */
    void update(@Valid DeptSaveReqVO updateReqVO);

    /**
     * 删除部门管理表
     *
     * @param id 编号
     */
    void delete(Long id);

    /**
     * 批量创建部门管理表
     *
     * @param createReqVOs 创建信息列表
     */
    void batchCreate(List<DeptSaveReqVO> createReqVOs);

    /**
     * 批量更新部门管理表
     *
     * @param updateReqVOs 更新信息列表
     */
    void batchUpdate(List<DeptSaveReqVO> updateReqVOs);

    /**
     * 批量删除部门管理表
     *
     * @param ids 编号列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 获得部门管理表详情
     *
     * @param id 编号
     * @return 部门管理表详情
     */
    DeptDO getById(Long id);

    /**
     * 获得部门管理表分页
     *
     * @param pageReqVO 分页查询
     * @return 部门管理表分页
     */
    PageResult<DeptRespVO> getPage(DeptPageReqVO pageReqVO);

    /**
     * 获得部门管理表列表（用于导出）
     *
     * @param pageReqVO 查询条件
     * @return 部门管理表列表
     */
    List<DeptDO> getList(DeptPageReqVO pageReqVO);

}

