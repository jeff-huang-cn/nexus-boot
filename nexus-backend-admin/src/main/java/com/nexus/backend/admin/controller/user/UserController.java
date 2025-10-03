package com.nexus.backend.admin.controller.user;


import com.nexus.backend.admin.controller.user.vo.*;
import com.nexus.backend.admin.service.user.UserService;
import com.nexus.framework.web.result.PageResult;
import com.nexus.framework.web.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户信息表 控制器
 *
 * @author beckend
 * @since 2025-10-02
 */
@Slf4j
@RestController
@RequestMapping("/user/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 创建用户信息表
     */
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody UserSaveReqVO createReqVO) {
        Long id = userService.create(createReqVO);
        return Result.success(id);
    }

    /**
     * 更新用户信息表
     */
    @PutMapping("/update")
    public Result<Boolean> update(@Valid @RequestBody UserSaveReqVO updateReqVO) {
        userService.update(updateReqVO);
        return Result.success(true);
    }

    /**
     * 删除用户信息表
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return Result.success(true);
    }

    /**
     * 批量删除用户信息表
     */
    @DeleteMapping("/delete-batch")
    public Result<Boolean> deleteBatch(@RequestBody List<Long> ids) {
        userService.deleteBatch(ids);
        return Result.success(true);
    }

    /**
     * 获得用户信息表详情
     */
    @GetMapping("/get/{id}")
    public Result<UserRespVO> getById(@PathVariable("id") Long id) {
        UserRespVO respVO = userService.getById(id);
        return Result.success(respVO);
    }

    /**
     * 获得用户信息表分页列表
     */
    @GetMapping("/page")
    public Result<PageResult<UserRespVO>> getPage(@Valid UserPageReqVO pageReqVO) {
        PageResult<UserRespVO> pageResult = userService.getPage(pageReqVO);
        return Result.success(pageResult);
    }

    /**
     * 导出用户信息表 Excel
     */
    @GetMapping("/export")
    public void export(@Valid UserPageReqVO pageReqVO) {
        List<UserRespVO> list = userService.getList(pageReqVO);
        // TODO: 实现 Excel 导出逻辑
    }
}

