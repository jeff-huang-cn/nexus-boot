package com.nexus.backend.admin.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.nexus.backend.admin.controller.user.vo.*;
import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import com.nexus.backend.admin.service.user.UserService;
import com.nexus.framework.web.result.PageResult;
import com.nexus.framework.web.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
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
        userService.deleteBatch(ids);
        return Result.success(true);
    }

    /**
     * 获得用户信息表详情
     */
    @GetMapping("/get/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<UserRespVO> getById(@PathVariable("id") Long id) {
        UserDO user = userService.getById(id);
        UserRespVO respVO = BeanUtil.copyProperties(user, UserRespVO.class);
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
    public void export(@Valid UserPageReqVO pageReqVO) {
        // TODO: 实现 Excel 导出逻辑
        // List<UserDO> list = userService.getList(pageReqVO);
        // 转换为 VO 并导出
    }
}
