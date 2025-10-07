package com.nexus.backend.admin.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.nexus.backend.admin.controller.user.vo.PasswordUpdateReqVO;
import com.nexus.backend.admin.controller.user.vo.ProfileRespVO;
import com.nexus.backend.admin.controller.user.vo.ProfileUpdateReqVO;
import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import com.nexus.backend.admin.service.user.UserService;
import com.nexus.framework.web.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户个人信息 控制器
 *
 * @author nexus
 */
@Slf4j
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Validated
public class UserProfileController {

    private final UserService userService;

    /**
     * 获取当前用户个人信息
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<ProfileRespVO> getProfile() {
        UserDO user = userService.getProfile();
        ProfileRespVO respVO = BeanUtil.copyProperties(user, ProfileRespVO.class);
        return Result.success(respVO);
    }

    /**
     * 更新当前用户个人信息
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> updateProfile(@Valid @RequestBody ProfileUpdateReqVO updateReqVO) {
        userService.updateProfile(updateReqVO);
        return Result.success(true);
    }

    /**
     * 修改当前用户密码
     */
    @PutMapping("/update-password")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> updatePassword(@Valid @RequestBody PasswordUpdateReqVO updateReqVO) {
        userService.updatePassword(updateReqVO);
        return Result.success(true);
    }
}
