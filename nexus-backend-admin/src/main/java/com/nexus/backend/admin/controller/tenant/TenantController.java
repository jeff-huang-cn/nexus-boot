package com.nexus.backend.admin.controller.tenant;

import com.nexus.framework.web.result.Result;
import com.nexus.framework.web.result.PageResult;
import com.nexus.framework.excel.ExcelUtils;
import com.nexus.backend.admin.convert.TenantConvert;
import com.nexus.backend.admin.controller.tenant.vo.*;
import com.nexus.backend.admin.dal.dataobject.tenant.TenantDO;
import com.nexus.backend.admin.dal.dataobject.codegen.DataSourceConfigDO;
import com.nexus.backend.admin.service.tenant.TenantService;
import com.nexus.backend.admin.service.codegen.DataSourceConfigService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 租户管理表 控制器
 *
 * @author beckend
 * @since 2025-10-08
 */
@Slf4j
@RestController
@RequestMapping("/system/tenant")
@RequiredArgsConstructor
@Validated
public class TenantController {

    private final TenantService tenantService;
    private final DataSourceConfigService dataSourceConfigService;

    /**
     * 创建租户管理表
     *
     * @param createReqVO 创建请求
     * @return 创建的记录ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:tenant:create')")
    public Result<Long> create(@Valid @RequestBody TenantSaveReqVO createReqVO) {
        Long id = tenantService.create(createReqVO);
        return Result.success(id);
    }

    /**
     * 更新租户管理表
     *
     * @param id          记录ID
     * @param updateReqVO 更新请求
     * @return 成功结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:tenant:update')")
    public Result<Void> update(@PathVariable @NotNull Long id,
            @Valid @RequestBody TenantSaveReqVO updateReqVO) {
        updateReqVO.setId(id); // 确保ID传入Service
        tenantService.update(updateReqVO);
        return Result.success();
    }

    /**
     * 删除租户管理表
     *
     * @param id 记录ID
     * @return 成功结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:tenant:delete')")
    public Result<Void> delete(@PathVariable @NotNull Long id) {
        tenantService.delete(id);
        return Result.success();
    }

    /**
     * 批量创建租户管理表
     *
     * @param createReqVOs 创建信息列表
     * @return 成功结果
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('system:tenant:create')")
    public Result<Void> batchCreate(@RequestBody @Valid @NotEmpty List<TenantSaveReqVO> createReqVOs) {
        tenantService.batchCreate(createReqVOs);
        return Result.success();
    }

    /**
     * 批量更新租户管理表
     *
     * @param updateReqVOs 更新信息列表
     * @return 成功结果
     */
    @PutMapping("/batch")
    @PreAuthorize("hasAuthority('system:tenant:update')")
    public Result<Void> batchUpdate(@RequestBody @Valid @NotEmpty List<TenantSaveReqVO> updateReqVOs) {
        tenantService.batchUpdate(updateReqVOs);
        return Result.success();
    }

    /**
     * 批量删除租户管理表
     *
     * @param ids ID列表
     * @return 成功结果
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:tenant:delete')")
    public Result<Void> batchDelete(@RequestBody @NotEmpty List<Long> ids) {
        tenantService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 获得租户管理表详情
     *
     * @param id 记录ID
     * @return 租户管理表详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:tenant:query')")
    public Result<TenantRespVO> getById(@PathVariable @NotNull Long id) {
        TenantDO tenant = tenantService.getById(id);
        TenantRespVO respVO = TenantConvert.INSTANCE.toRespVO(tenant);
        return Result.success(respVO);
    }

    /**
     * 获得租户管理表分页列表
     *
     * @param pageReqVO 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:tenant:query')")
    public Result<PageResult<TenantRespVO>> getPage(@Valid TenantPageReqVO pageReqVO) {
        PageResult<TenantRespVO> pageResult = tenantService.getPage(pageReqVO);
        return Result.success(pageResult);
    }

    /**
     * 导出租户管理表 Excel
     *
     * @param pageReqVO 查询参数
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('system:tenant:export')")
    public void export(@Valid TenantPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        // 查询数据
        List<TenantDO> list = tenantService.getList(pageReqVO);

        // 使用 MapStruct 转换为 VO
        List<TenantRespVO> voList = TenantConvert.INSTANCE.toRespVOList(list);

        // 使用 ExcelUtils 导出
        ExcelUtils.export(response, voList, TenantRespVO.class, "租户管理表数据", "租户管理表数据");
    }

    /**
     * 获取数据源列表（用于下拉选择）
     *
     * @return 数据源列表
     */
    @GetMapping("/datasources")
    public Result<List<DataSourceConfigDO>> getDataSources() {
        List<DataSourceConfigDO> list = dataSourceConfigService.getList();
        return Result.success(list);
    }
}
