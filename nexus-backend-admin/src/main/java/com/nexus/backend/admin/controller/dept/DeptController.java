package com.nexus.backend.admin.controller.dept;

import com.nexus.framework.web.result.Result;
import com.nexus.framework.web.result.PageResult;
import com.nexus.framework.excel.ExcelUtils;
import com.nexus.backend.admin.convert.DeptConvert;
import com.nexus.backend.admin.controller.dept.vo.*;
import com.nexus.backend.admin.dal.dataobject.dept.DeptDO;
import com.nexus.backend.admin.service.dept.DeptService;
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
 * 部门管理表 控制器
 *
 * @author beckend
 * @since 2025-10-27
 */
@Slf4j
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
@Validated
public class DeptController {

    private final DeptService deptService;

    /**
     * 创建部门管理表
     *
     * @param createReqVO 创建请求
     * @return 创建的记录ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:dept:create')")
    public Result<Long> create(@Valid @RequestBody DeptSaveReqVO createReqVO) {
        Long id = deptService.create(createReqVO);
        return Result.success(id);
    }

    /**
     * 更新部门管理表
     *
     * @param id 记录ID
     * @param updateReqVO 更新请求
     * @return 成功结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:update')")
    public Result<Void> update(@PathVariable @NotNull Long id,
                                @Valid @RequestBody DeptSaveReqVO updateReqVO) {
        updateReqVO.setId(id); // 确保ID传入Service
        deptService.update(updateReqVO);
        return Result.success();
    }

    /**
     * 删除部门管理表
     *
     * @param id 记录ID
     * @return 成功结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:delete')")
    public Result<Void> delete(@PathVariable @NotNull Long id) {
        deptService.delete(id);
        return Result.success();
    }

    /**
     * 批量创建部门管理表
     *
     * @param createReqVOs 创建信息列表
     * @return 成功结果
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('system:dept:create')")
    public Result<Void> batchCreate(@RequestBody @Valid @NotEmpty List<DeptSaveReqVO> createReqVOs) {
        deptService.batchCreate(createReqVOs);
        return Result.success();
    }

    /**
     * 批量更新部门管理表
     *
     * @param updateReqVOs 更新信息列表
     * @return 成功结果
     */
    @PutMapping("/batch")
    @PreAuthorize("hasAuthority('system:dept:update')")
    public Result<Void> batchUpdate(@RequestBody @Valid @NotEmpty List<DeptSaveReqVO> updateReqVOs) {
        deptService.batchUpdate(updateReqVOs);
        return Result.success();
    }

    /**
     * 批量删除部门管理表
     *
     * @param ids ID列表
     * @return 成功结果
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:dept:delete')")
    public Result<Void> batchDelete(@RequestBody @NotEmpty List<Long> ids) {
        deptService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 获得部门管理表详情
     *
     * @param id 记录ID
     * @return 部门管理表详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:query')")
    public Result<DeptRespVO> getById(@PathVariable @NotNull Long id) {
        DeptDO dept = deptService.getById(id);
        DeptRespVO respVO = DeptConvert.INSTANCE.toRespVO(dept);
        return Result.success(respVO);
    }

    /**
     * 获得部门管理表分页列表
     *
     * @param pageReqVO 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:dept:query')")
    public Result<PageResult<DeptRespVO>> getPage(@Valid DeptPageReqVO pageReqVO) {
        PageResult<DeptRespVO> pageResult = deptService.getPage(pageReqVO);
        return Result.success(pageResult);
    }

    /**
     * 导出部门管理表 Excel
     *
     * @param pageReqVO 查询参数
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('system:dept:export')")
    public void export(@Valid DeptPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        // 查询数据
        List<DeptDO> list = deptService.getList(pageReqVO);

        // 使用 MapStruct 转换为 VO
        List<DeptRespVO> voList = DeptConvert.INSTANCE.toRespVOList(list);

        // 使用 ExcelUtils 导出
        ExcelUtils.export(response, voList, DeptRespVO.class, "部门管理表数据", "部门管理表数据");
    }
}

