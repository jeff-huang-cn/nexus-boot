package com.nexus.backend.admin.controller.dept;

import com.nexus.framework.web.result.Result;
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
 * @since 2025-10-28
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
     * 获得部门管理表列表（树表不分页）
     *
     * @param listReqVO 查询参数
     * @return 部门管理表列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:dept:query')")
    public Result<List<DeptRespVO>> getList(@Valid DeptListReqVO listReqVO) {
        List<DeptDO> list = deptService.getList(listReqVO);
        List<DeptRespVO> voList = DeptConvert.INSTANCE.toRespVOList(list);
        return Result.success(voList);
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
}

