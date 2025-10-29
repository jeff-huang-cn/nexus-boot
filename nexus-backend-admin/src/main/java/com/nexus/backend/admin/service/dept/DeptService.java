package com.nexus.backend.admin.service.dept;

import com.nexus.backend.admin.controller.dept.vo.*;
import com.nexus.backend.admin.dal.dataobject.dept.DeptDO;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 部门管理表 Service 接口
 *
 * @author beckend
 * @since 2025-10-28
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
     * 获得部门管理表详情
     *
     * @param id 编号
     * @return 部门管理表详情
     */
    DeptDO getById(Long id);

    /**
     * 获得部门管理表列表（树形结构）
     *
     * @param listReqVO 查询条件
     * @return 部门管理表列表
     */
    List<DeptDO> getList(DeptListReqVO listReqVO);
}

