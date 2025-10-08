package com.nexus.backend.admin.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.nexus.backend.admin.controller.user.vo.*;
import com.nexus.backend.admin.convert.UserConvert;
import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import com.nexus.backend.admin.dal.mapper.user.UserMapper;
import com.nexus.backend.admin.service.user.UserService;
import com.nexus.framework.security.util.SecurityContextUtils;
import com.nexus.framework.web.exception.BusinessException;
import com.nexus.framework.web.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息表 Service 实现类
 *
 * @author beckend
 * @since 2025-10-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Long create(UserSaveReqVO createReqVO) {
        // 转换为 DO 并插入
        UserDO user = UserConvert.INSTANCE.toDO(createReqVO);
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public void update(UserSaveReqVO updateReqVO) {
        // 校验存在
        validateExists(updateReqVO.getId());
        // 更新
        UserDO updateUser = UserConvert.INSTANCE.toDO(updateReqVO);
        userMapper.updateById(updateUser);
    }

    @Override
    public void delete(Long id) {
        // 校验存在
        validateExists(id);
        // 删除
        userMapper.deleteById(id);
    }

    @Override
    public void batchCreate(List<UserSaveReqVO> createReqVOs) {
        if (createReqVOs == null || createReqVOs.isEmpty()) {
            return;
        }

        // 转换为 DO 列表
        List<UserDO> doList = createReqVOs.stream()
                .map(UserConvert.INSTANCE::toDO)
                .collect(Collectors.toList());

        // 分批插入，每批100条，避免锁表时间过长
        List<List<UserDO>> partitions = Lists.partition(doList, 100);
        for (List<UserDO> partition : partitions) {
            userMapper.insertBatch(partition);
        }
    }

    @Override
    public void batchUpdate(List<UserSaveReqVO> updateReqVOs) {
        if (updateReqVOs == null || updateReqVOs.isEmpty()) {
            return;
        }

        // 转换为 DO 列表
        List<UserDO> doList = updateReqVOs.stream()
                .map(UserConvert.INSTANCE::toDO)
                .collect(Collectors.toList());

        // 分批更新，每批100条，避免锁表时间过长
        List<List<UserDO>> partitions = Lists.partition(doList, 100);
        for (List<UserDO> partition : partitions) {
            userMapper.updateBatch(partition);
        }
    }

    @Override
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 分批删除，每批1000个ID，避免SQL过长
        List<List<Long>> partitions = Lists.partition(ids, 1000);
        for (List<Long> partition : partitions) {
            userMapper.deleteBatchIds(partition);
        }
    }

    @Override
    public UserDO getById(Long id) {
        UserDO user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    @Override
    public PageResult<UserRespVO> getPage(UserPageReqVO pageReqVO) {
        // 构建查询条件
        LambdaQueryWrapper<UserDO> wrapper = buildQueryWrapper(pageReqVO);

        // 分页查询
        IPage<UserDO> page = new Page<>(pageReqVO.getPageNum(), pageReqVO.getPageSize());
        IPage<UserDO> result = userMapper.selectPage(page, wrapper);

        // 转换为 VO
        List<UserRespVO> list = UserConvert.INSTANCE.toRespVOList(result.getRecords());

        return new PageResult<>(list, result.getTotal());
    }

    @Override
    public List<UserDO> getList(UserPageReqVO pageReqVO) {
        // 构建查询条件
        LambdaQueryWrapper<UserDO> wrapper = buildQueryWrapper(pageReqVO);

        // 查询列表
        return userMapper.selectList(wrapper);
    }

    @Override
    public UserDO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<UserDO> buildQueryWrapper(UserPageReqVO pageReqVO) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();

        // 用户ID - 精确匹配
        if (pageReqVO.getId() != null) {
            wrapper.eq(UserDO::getId, pageReqVO.getId());
        }
        // 用户账号 - 模糊查询
        if (StringUtils.hasText(pageReqVO.getUsername())) {
            wrapper.like(UserDO::getUsername, pageReqVO.getUsername());
        }
        // 用户昵称 - 模糊查询
        if (StringUtils.hasText(pageReqVO.getNickname())) {
            wrapper.like(UserDO::getNickname, pageReqVO.getNickname());
        }
        // 备注 - 模糊查询
        if (StringUtils.hasText(pageReqVO.getRemark())) {
            wrapper.like(UserDO::getRemark, pageReqVO.getRemark());
        }
        // 部门ID - 精确匹配
        if (pageReqVO.getDeptId() != null) {
            wrapper.eq(UserDO::getDeptId, pageReqVO.getDeptId());
        }
        // 岗位编号数组 - 模糊查询
        if (StringUtils.hasText(pageReqVO.getPostIds())) {
            wrapper.like(UserDO::getPostIds, pageReqVO.getPostIds());
        }
        // 用户邮箱 - 模糊查询
        if (StringUtils.hasText(pageReqVO.getEmail())) {
            wrapper.like(UserDO::getEmail, pageReqVO.getEmail());
        }
        // 手机号码 - 模糊查询
        if (StringUtils.hasText(pageReqVO.getMobile())) {
            wrapper.like(UserDO::getMobile, pageReqVO.getMobile());
        }
        // 用户性别（0=未知 1=男 2=女） - 精确匹配
        if (pageReqVO.getSex() != null) {
            wrapper.eq(UserDO::getSex, pageReqVO.getSex());
        }
        // 头像地址 - 模糊查询
        if (StringUtils.hasText(pageReqVO.getAvatar())) {
            wrapper.like(UserDO::getAvatar, pageReqVO.getAvatar());
        }
        // 帐号状态（0=正常 1=停用） - 精确匹配
        if (pageReqVO.getStatus() != null) {
            wrapper.eq(UserDO::getStatus, pageReqVO.getStatus());
        }
        // 最后登录IP - 模糊查询
        if (StringUtils.hasText(pageReqVO.getLoginIp())) {
            wrapper.like(UserDO::getLoginIp, pageReqVO.getLoginIp());
        }
        // 最后登录时间 - 范围查询
        if (pageReqVO.getLoginDateStart() != null) {
            wrapper.ge(UserDO::getLoginDate, pageReqVO.getLoginDateStart());
        }
        if (pageReqVO.getLoginDateEnd() != null) {
            wrapper.le(UserDO::getLoginDate, pageReqVO.getLoginDateEnd());
        }
        // 创建者 - 模糊查询
        if (StringUtils.hasText(pageReqVO.getCreator())) {
            wrapper.like(UserDO::getCreator, pageReqVO.getCreator());
        }
        // 创建时间 - 范围查询
        if (pageReqVO.getDateCreatedStart() != null) {
            wrapper.ge(UserDO::getDateCreated, pageReqVO.getDateCreatedStart());
        }
        if (pageReqVO.getDateCreatedEnd() != null) {
            wrapper.le(UserDO::getDateCreated, pageReqVO.getDateCreatedEnd());
        }
        // 更新者 - 模糊查询
        if (StringUtils.hasText(pageReqVO.getUpdater())) {
            wrapper.like(UserDO::getUpdater, pageReqVO.getUpdater());
        }
        // 更新时间 - 范围查询
        if (pageReqVO.getLastUpdatedStart() != null) {
            wrapper.ge(UserDO::getLastUpdated, pageReqVO.getLastUpdatedStart());
        }
        if (pageReqVO.getLastUpdatedEnd() != null) {
            wrapper.le(UserDO::getLastUpdated, pageReqVO.getLastUpdatedEnd());
        }

        // 默认按创建时间倒序
        wrapper.orderByDesc(UserDO::getDateCreated);

        return wrapper;
    }

    /**
     * 校验用户信息表是否存在
     */
    private void validateExists(Long id) {
        if (userMapper.selectById(id) == null) {
            throw new BusinessException(404, "用户信息表不存在");
        }
    }

    @Override
    public UserDO getProfile() {
        // 获取当前登录用户ID
        Long userId = SecurityContextUtils.getLoginUserId();
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }

        // 查询用户信息
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return user;
    }

    @Override
    public void updateProfile(ProfileUpdateReqVO updateReqVO) {
        // 获取当前登录用户ID
        Long userId = SecurityContextUtils.getLoginUserId();
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }

        // 查询用户信息
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新个人信息（只更新允许修改的字段）
        user.setNickname(updateReqVO.getNickname());
        user.setEmail(updateReqVO.getEmail());
        user.setMobile(updateReqVO.getMobile());
        user.setSex(updateReqVO.getSex());
        user.setAvatar(updateReqVO.getAvatar());

        userMapper.updateById(user);
        log.info("用户更新个人信息成功，userId: {}", userId);
    }

    @Override
    public void updatePassword(PasswordUpdateReqVO updateReqVO) {
        // 获取当前登录用户ID
        Long userId = SecurityContextUtils.getLoginUserId();
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }

        // 验证新密码和确认密码是否一致
        if (!updateReqVO.getNewPassword().equals(updateReqVO.getConfirmPassword())) {
            throw new BusinessException("新密码和确认密码不一致");
        }

        // 查询用户信息
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码是否正确
        if (!passwordEncoder.matches(updateReqVO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 不能与旧密码相同
        if (updateReqVO.getOldPassword().equals(updateReqVO.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        // 加密新密码并更新
        String encodedPassword = passwordEncoder.encode(updateReqVO.getNewPassword());
        user.setPassword(encodedPassword);
        userMapper.updateById(user);

        log.info("用户修改密码成功，userId: {}", userId);
    }

}
