package com.nexus.backend.admin.controller.user;

import com.nexus.backend.admin.controller.user.vo.*;
import com.nexus.backend.admin.convert.UserConvert;
import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import com.nexus.backend.admin.service.user.UserService;
import com.nexus.framework.excel.ExcelUtils;
import com.nexus.framework.web.result.PageResult;
import com.nexus.framework.web.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 用户信息表 控制器
 *
 * @author beckend
 * @since 2025-10-02
 */
@Slf4j
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 创建用户信息表
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:user:create')")
    public Result<Long> create(@Valid @RequestBody UserSaveReqVO createReqVO) {
        Long id = userService.create(createReqVO);
        return Result.success(id);
    }

    /**
     * 更新用户信息表
     */
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('system:user:update')")
    public Result<Boolean> update(@Valid @RequestBody UserSaveReqVO updateReqVO) {
        userService.update(updateReqVO);
        return Result.success(true);
    }

    /**
     * 删除用户信息表
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return Result.success(true);
    }

    /**
     * 批量删除用户信息表
     */
    @DeleteMapping("/delete-batch")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Boolean> deleteBatch(@RequestBody List<Long> ids) {
        userService.batchDelete(ids);
        return Result.success(true);
    }

    /**
     * 获得用户信息表详情
     */
    @GetMapping("/get/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<UserRespVO> getById(@PathVariable("id") Long id) {
        UserDO user = userService.getById(id);
        UserRespVO respVO = UserConvert.INSTANCE.toRespVO(user);
        return Result.success(respVO);
    }

    /**
     * 获得用户信息表分页列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<PageResult<UserRespVO>> getPage(@Valid UserPageReqVO pageReqVO) {
        PageResult<UserRespVO> pageResult = userService.getPage(pageReqVO);
        return Result.success(pageResult);
    }

    /**
     * 导出用户信息表 Excel
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('system:user:export')")
    public void export(@Valid UserPageReqVO pageReqVO, HttpServletResponse response)
            throws IOException {
        // 查询数据
        List<UserDO> list = userService.getList(pageReqVO);

        // 使用 MapStruct 转换为 VO
        List<UserRespVO> voList = UserConvert.INSTANCE.toRespVOList(list);

        // 使用 ExcelUtils 导出
        ExcelUtils.export(response, voList, UserRespVO.class, "用户数据", "用户数据");
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/export-template")
    public void exportTemplate(HttpServletResponse response)
            throws IOException {
        // 导出空模板
        ExcelUtils.export(response, List.of(), UserRespVO.class, "用户导入模板", "用户数据");
    }

    /**
     * 导入用户信息表 Excel
     */
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('system:user:import')")
    public Result<String> importData(@RequestParam("file") MultipartFile file)
            throws IOException {
        // 使用 ExcelUtils 导入
        List<UserSaveReqVO> dataList = ExcelUtils.importExcel(file.getInputStream(), UserSaveReqVO.class);

        if (dataList == null || dataList.isEmpty()) {
            return Result.success("未读取到数据");
        }

        // 逐条校验并导入（按照 create 的业务逻辑）
        int successCount = 0;
        int failCount = 0;
        for (UserSaveReqVO reqVO : dataList) {
            try {
                userService.create(reqVO);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.warn("导入用户失败，用户名: {}, 原因: {}", reqVO.getUsername(), e.getMessage());
            }
        }

        return Result.success(String.format("导入完成！成功: %d 条，失败: %d 条", successCount, failCount));
    }
}
