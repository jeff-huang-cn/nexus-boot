package com.nexus.backend.admin.service.user.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexus.backend.admin.controller.user.vo.UserPageReqVO;
import com.nexus.backend.admin.controller.user.vo.UserRespVO;
import com.nexus.backend.admin.controller.user.vo.UserSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import com.nexus.backend.admin.dal.mapper.user.UserMapper;
import com.nexus.backend.admin.service.user.UserService;
import com.nexus.framework.web.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(UserSaveReqVO createReqVO) {
        // 转换为 DO 并插入
        UserDO user = BeanUtil.copyProperties(createReqVO, UserDO.class);
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserSaveReqVO updateReqVO) {
        // 校验存在
        validateExists(updateReqVO.getId());
        // 更新
        UserDO updateUser = BeanUtil.copyProperties(updateReqVO, UserDO.class);
        userMapper.updateById(updateUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 校验存在
        validateExists(id);
        // 删除
        userMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        // 批量删除
        userMapper.deleteBatchIds(ids);
    }

    @Override
    public UserDO getById(Long id) {
        UserDO user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
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
        List<UserRespVO> list = result.getRecords().stream()
                .map(item -> BeanUtil.copyProperties(item, UserRespVO.class))
                .collect(Collectors.toList());

        return new PageResult<>(list, result.getTotal());
    }

    @Override
    public List<UserDO> getList(UserPageReqVO pageReqVO) {
        // 构建查询条件
        LambdaQueryWrapper<UserDO> wrapper = buildQueryWrapper(pageReqVO);

        // 查询列表
        return userMapper.selectList(wrapper);
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
        // 租户编号 - 精确匹配
        if (pageReqVO.getTenantId() != null) {
            wrapper.eq(UserDO::getTenantId, pageReqVO.getTenantId());
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
            throw new RuntimeException("用户信息表不存在");
        }
    }
}
