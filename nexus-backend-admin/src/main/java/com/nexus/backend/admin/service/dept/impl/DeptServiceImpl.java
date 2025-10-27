package com.nexus.backend.admin.service.dept.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexus.framework.web.result.PageResult;
import com.nexus.framework.web.exception.BusinessException;
import com.nexus.backend.admin.controller.dept.vo.*;
import com.nexus.backend.admin.convert.DeptConvert;
import com.nexus.backend.admin.dal.dataobject.dept.DeptDO;
import com.nexus.backend.admin.dal.mapper.dept.DeptMapper;
import com.nexus.backend.admin.service.dept.DeptService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门管理表 Service 实现类
 *
 * @author beckend
 * @since 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;

    @Override
    public Long create(DeptSaveReqVO createReqVO) {
        // 转换为 DO 并插入
        DeptDO dept = DeptConvert.INSTANCE.toDO(createReqVO);
        deptMapper.insert(dept);
        return dept.getId();
    }

    @Override
    public void update(DeptSaveReqVO updateReqVO) {
        // 校验存在
        validateExists(updateReqVO.getId());
        // 更新主表
        DeptDO updateDept = DeptConvert.INSTANCE.toDO(updateReqVO);
        deptMapper.updateById(updateDept);
    }

    @Override
    public void delete(Long id) {
        // 校验存在
        validateExists(id);
        // 删除主表
        deptMapper.deleteById(id);
    }

    @Override
    public void batchCreate(List<DeptSaveReqVO> createReqVOs) {
        if (createReqVOs == null || createReqVOs.isEmpty()) {
            return;
        }
        
        // 转换为 DO 列表
        List<DeptDO> doList = createReqVOs.stream()
                .map(DeptConvert.INSTANCE::toDO)
                .collect(Collectors.toList());
        // 分批插入，每批100条，避免锁表时间过长
        List<List<DeptDO>> partitions = Lists.partition(doList, 100);
        for (List<DeptDO> partition : partitions) {
            deptMapper.insertBatch(partition);
        }

    }

    @Override
    public void batchUpdate(List<DeptSaveReqVO> updateReqVOs) {
        if (updateReqVOs == null || updateReqVOs.isEmpty()) {
            return;
        }

        // 转换为 DO 列表
        List<DeptDO> doList = updateReqVOs.stream()
                .map(DeptConvert.INSTANCE::toDO)
                .collect(Collectors.toList());

        // 分批更新，每批100条，避免锁表时间过长
        List<List<DeptDO>> partitions = Lists.partition(doList, 100);
        for (List<DeptDO> partition : partitions) {
            deptMapper.updateBatch(partition);
        }

    }

    @Override
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 分批删除，每批1000个ID，避免锁表时间过长
        List<List<Long>> partitions = Lists.partition(ids, 1000);
        for (List<Long> partition : partitions) {
            deptMapper.deleteByIds(partition);
        }

    }

    @Override
    public DeptDO getById(Long id) {
        DeptDO dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(404, "部门管理表不存在");
        }
        return dept;
    }

    @Override
    public PageResult<DeptRespVO> getPage(DeptPageReqVO pageReqVO) {
        // 构建查询条件
        LambdaQueryWrapper<DeptDO> wrapper = buildQueryWrapper(pageReqVO);

        // 分页查询
        IPage<DeptDO> page = new Page<>(pageReqVO.getPageNum(), pageReqVO.getPageSize());
        IPage<DeptDO> result = deptMapper.selectPage(page, wrapper);

        // 转换为 VO
        List<DeptRespVO> list = DeptConvert.INSTANCE.toRespVOList(result.getRecords());

        return new PageResult<>(list, result.getTotal());
    }

    @Override
    public List<DeptDO> getList(DeptPageReqVO pageReqVO) {
        // 构建查询条件
        LambdaQueryWrapper<DeptDO> wrapper = buildQueryWrapper(pageReqVO);

        // 查询列表
        return deptMapper.selectList(wrapper);
    }


    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<DeptDO> buildQueryWrapper(DeptPageReqVO reqVO) {
        LambdaQueryWrapper<DeptDO> wrapper = new LambdaQueryWrapper<>();

        // 部门名称 - 模糊查询
        if (StringUtils.hasText(reqVO.getName())) {
            wrapper.like(DeptDO::getName, reqVO.getName());
        }
        // 部门编码 - 模糊查询
        if (StringUtils.hasText(reqVO.getCode())) {
            wrapper.like(DeptDO::getCode, reqVO.getCode());
        }
        // 状态：0-禁用 1-启用 - 精确匹配
        if (reqVO.getStatus() != null) {
            wrapper.eq(DeptDO::getStatus, reqVO.getStatus());
        }

        // 默认按创建时间倒序
        wrapper.orderByDesc(DeptDO::getDateCreated);

        return wrapper;
    }

    /**
     * 校验部门管理表是否存在
     */
    private void validateExists(Long id) {
        if (deptMapper.selectById(id) == null) {
            throw new BusinessException(404, "部门管理表不存在");
        }
    }
}

