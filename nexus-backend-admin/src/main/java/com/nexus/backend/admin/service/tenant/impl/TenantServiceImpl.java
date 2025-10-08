package com.nexus.backend.admin.service.tenant.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexus.framework.web.exception.BusinessException;
import com.nexus.backend.admin.controller.tenant.vo.*;
import com.nexus.backend.admin.convert.TenantConvert;
import com.nexus.backend.admin.dal.dataobject.tenant.TenantDO;
import com.nexus.backend.admin.dal.mapper.tenant.TenantMapper;
import com.nexus.backend.admin.service.tenant.TenantService;
import com.google.common.collect.Lists;
import com.nexus.framework.web.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户管理表 Service 实现类
 *
 * @author beckend
 * @since 2025-10-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantMapper tenantMapper;

    @Override
    public Long create(TenantSaveReqVO createReqVO) {
        // 转换为 DO 并插入
        TenantDO tenant = TenantConvert.INSTANCE.toDO(createReqVO);
        tenantMapper.insert(tenant);
        return tenant.getId();
    }

    @Override
    public void update(TenantSaveReqVO updateReqVO) {
        // 校验存在
        validateExists(updateReqVO.getId());
        // 更新主表
        TenantDO updateTenant = TenantConvert.INSTANCE.toDO(updateReqVO);
        tenantMapper.updateById(updateTenant);
    }

    @Override
    public void delete(Long id) {
        // 校验存在
        validateExists(id);
        // 删除主表
        tenantMapper.deleteById(id);
    }

    @Override
    public void batchCreate(List<TenantSaveReqVO> createReqVOs) {
        if (createReqVOs == null || createReqVOs.isEmpty()) {
            return;
        }

        // 转换为 DO 列表
        List<TenantDO> doList = createReqVOs.stream()
                .map(TenantConvert.INSTANCE::toDO)
                .collect(Collectors.toList());
        // 分批插入，每批100条，避免锁表时间过长
        List<List<TenantDO>> partitions = Lists.partition(doList, 100);
        for (List<TenantDO> partition : partitions) {
            tenantMapper.insertBatch(partition);
        }

    }

    @Override
    public void batchUpdate(List<TenantSaveReqVO> updateReqVOs) {
        if (updateReqVOs == null || updateReqVOs.isEmpty()) {
            return;
        }

        // 转换为 DO 列表
        List<TenantDO> doList = updateReqVOs.stream()
                .map(TenantConvert.INSTANCE::toDO)
                .collect(Collectors.toList());

        // 分批更新，每批100条，避免锁表时间过长
        List<List<TenantDO>> partitions = Lists.partition(doList, 100);
        for (List<TenantDO> partition : partitions) {
            tenantMapper.updateBatch(partition);
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
            tenantMapper.deleteByIds(partition);
        }

    }

    @Override
    public TenantDO getById(Long id) {
        TenantDO tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(404, "租户管理表不存在");
        }
        return tenant;
    }

    @Override
    public PageResult<TenantRespVO> getPage(TenantPageReqVO pageReqVO) {
        // 构建查询条件
        LambdaQueryWrapper<TenantDO> wrapper = buildQueryWrapper(pageReqVO);

        // 分页查询
        IPage<TenantDO> page = new Page<>(pageReqVO.getPageNum(), pageReqVO.getPageSize());
        IPage<TenantDO> result = tenantMapper.selectPage(page, wrapper);

        // 转换为 VO
        List<TenantRespVO> list = TenantConvert.INSTANCE.toRespVOList(result.getRecords());

        return new PageResult<>(list, result.getTotal());
    }

    @Override
    public List<TenantDO> getList(TenantPageReqVO pageReqVO) {
        // 构建查询条件
        LambdaQueryWrapper<TenantDO> wrapper = buildQueryWrapper(pageReqVO);

        // 查询列表
        return tenantMapper.selectList(wrapper);
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<TenantDO> buildQueryWrapper(TenantPageReqVO reqVO) {
        LambdaQueryWrapper<TenantDO> wrapper = new LambdaQueryWrapper<>();

        // 租户ID - 精确匹配
        if (reqVO.getId() != null) {
            wrapper.eq(TenantDO::getId, reqVO.getId());
        }
        // 租户名称 - 模糊查询
        if (StringUtils.hasText(reqVO.getName())) {
            wrapper.like(TenantDO::getName, reqVO.getName());
        }
        // 租户编码（用于识别租户） - 模糊查询
        if (StringUtils.hasText(reqVO.getCode())) {
            wrapper.like(TenantDO::getCode, reqVO.getCode());
        }
        // 数据源ID（关联datasource_config.id） - 精确匹配
        if (reqVO.getDatasourceId() != null) {
            wrapper.eq(TenantDO::getDatasourceId, reqVO.getDatasourceId());
        }
        // 分配的菜单ID集合，格式：[1, 2, 100, 101, 102] - 模糊查询
        if (StringUtils.hasText(reqVO.getMenuIds())) {
            wrapper.like(TenantDO::getMenuIds, reqVO.getMenuIds());
        }
        // 用户数量（可创建的用户数量） - 精确匹配
        if (reqVO.getMaxUsers() != null) {
            wrapper.eq(TenantDO::getMaxUsers, reqVO.getMaxUsers());
        }
        // 过期时间 - 范围查询
        if (reqVO.getExpireTimeStart() != null) {
            wrapper.ge(TenantDO::getExpireTime, reqVO.getExpireTimeStart());
        }
        if (reqVO.getExpireTimeEnd() != null) {
            wrapper.le(TenantDO::getExpireTime, reqVO.getExpireTimeEnd());
        }
        // 状态：0-禁用 1-启用 - 精确匹配
        if (reqVO.getStatus() != null) {
            wrapper.eq(TenantDO::getStatus, reqVO.getStatus());
        }
        // 创建人 - 模糊查询
        if (StringUtils.hasText(reqVO.getCreator())) {
            wrapper.like(TenantDO::getCreator, reqVO.getCreator());
        }
        // 创建时间 - 范围查询
        if (reqVO.getDateCreatedStart() != null) {
            wrapper.ge(TenantDO::getDateCreated, reqVO.getDateCreatedStart());
        }
        if (reqVO.getDateCreatedEnd() != null) {
            wrapper.le(TenantDO::getDateCreated, reqVO.getDateCreatedEnd());
        }
        // 更新人 - 模糊查询
        if (StringUtils.hasText(reqVO.getUpdater())) {
            wrapper.like(TenantDO::getUpdater, reqVO.getUpdater());
        }
        // 更新时间 - 范围查询
        if (reqVO.getLastUpdatedStart() != null) {
            wrapper.ge(TenantDO::getLastUpdated, reqVO.getLastUpdatedStart());
        }
        if (reqVO.getLastUpdatedEnd() != null) {
            wrapper.le(TenantDO::getLastUpdated, reqVO.getLastUpdatedEnd());
        }

        // 默认按创建时间倒序
        wrapper.orderByDesc(TenantDO::getDateCreated);

        return wrapper;
    }

    /**
     * 校验租户管理表是否存在
     */
    private void validateExists(Long id) {
        if (tenantMapper.selectById(id) == null) {
            throw new BusinessException(404, "租户管理表不存在");
        }
    }
}
