package com.nexus.backend.admin.service.dept.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexus.framework.web.exception.BusinessException;
import com.nexus.backend.admin.controller.dept.vo.*;
import com.nexus.backend.admin.convert.DeptConvert;
import com.nexus.backend.admin.dal.dataobject.dept.DeptDO;
import com.nexus.backend.admin.dal.mapper.dept.DeptMapper;
import com.nexus.backend.admin.service.dept.DeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 部门管理表 Service 实现类
 * （树表）
 *
 * @author beckend
 * @since 2025-10-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;

    @Override
    public Long create(DeptSaveReqVO createReqVO) {
        // 校验父节点和名称唯一性
        validateParentAndNameUnique(null, createReqVO.getParentId(), createReqVO.getName());
        // 转换为 DO 并插入
        DeptDO dept = DeptConvert.INSTANCE.toDO(createReqVO);
        deptMapper.insert(dept);
        return dept.getId();
    }

    @Override
    public void update(DeptSaveReqVO updateReqVO) {
        // 校验存在
        validateExists(updateReqVO.getId());
        // 校验父节点和名称唯一性
        validateParentAndNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());
        // 更新主表
        DeptDO updateDept = DeptConvert.INSTANCE.toDO(updateReqVO);
        deptMapper.updateById(updateDept);
    }

    @Override
    public void delete(Long id) {
        // 校验存在
        validateExists(id);
        // 校验是否有子节点
        Long childCount = deptMapper.selectCount(
                new LambdaQueryWrapper<DeptDO>()
                        .eq(DeptDO::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(400, "存在子节点，无法删除");
        }
        // 删除主表
        deptMapper.deleteById(id);
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
    public List<DeptDO> getList(DeptListReqVO listReqVO) {
        // 构建查询条件
        LambdaQueryWrapper<DeptDO> wrapper = buildQueryWrapper(listReqVO);

        // 查询列表并返回 DO
        return deptMapper.selectList(wrapper);
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<DeptDO> buildQueryWrapper(DeptListReqVO reqVO) {
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

        // 默认按排序号排序
        wrapper.orderByAsc(DeptDO::getSort);

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

    /**
     * 校验父节点和名称唯一性
     */
    private void validateParentAndNameUnique(Long id, Long parentId, String name) {
        // 1. 校验父节点
        if (parentId != null && parentId != 0L) {
            DeptDO parent = deptMapper.selectById(parentId);
            if (parent == null) {
                throw new BusinessException(400, "父节点不存在");
            }
        }

        // 2. 校验名称唯一性
        LambdaQueryWrapper<DeptDO> wrapper = new LambdaQueryWrapper<DeptDO>()
                .eq(DeptDO::getParentId, parentId)
                .eq(DeptDO::getName, name);
        if (id != null) {
            wrapper.ne(DeptDO::getId, id);
        }
        Long count = deptMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(400, "同级目录下已存在相同名称的部门管理表");
        }
    }
}

