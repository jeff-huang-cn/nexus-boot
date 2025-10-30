package com.nexus.backend.admin.service.permission.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.query.MPJLambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.nexus.backend.admin.controller.permission.vo.role.RoleAssignMenuReqVO;
import com.nexus.backend.admin.controller.permission.vo.role.RoleSaveReqVO;
import com.nexus.backend.admin.convert.RoleConvert;
import com.nexus.backend.admin.dal.dataobject.permission.RoleDO;
import com.nexus.backend.admin.dal.dataobject.permission.RoleMenuDO;
import com.nexus.backend.admin.dal.dataobject.permission.UserRoleDO;
import com.nexus.backend.admin.dal.mapper.permission.RoleMapper;
import com.nexus.backend.admin.dal.mapper.permission.RoleMenuMapper;
import com.nexus.backend.admin.dal.mapper.permission.UserRoleMapper;
import com.nexus.backend.admin.service.permission.RoleService;
import com.nexus.framework.web.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 *
 * @author nexus
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Override
    public Long create(RoleSaveReqVO reqVO) {
        // 校验角色编码唯一性
        validateCodeUnique(null, reqVO.getCode());
        // 转换为 DO
        RoleDO role = RoleConvert.INSTANCE.toDO(reqVO);
        role.setType(2); // 自定义角色
        // 插入数据库
        roleMapper.insert(role);
        return role.getId();
    }

    @Override
    public void update(RoleSaveReqVO reqVO) {
        // 校验角色是否存在
        validateExists(reqVO.getId());
        // 校验角色编码唯一性
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        // 转换为 DO
        RoleDO role = RoleConvert.INSTANCE.toDO(reqVO);
        // 更新数据库
        roleMapper.updateById(role);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 校验角色是否存在
        RoleDO role = validateExists(id);

        // 校验是否为系统内置角色
        if (role.getType().equals(1)) {
            throw new BusinessException(400, "系统内置角色，无法删除");
        }

        // 校验是否有用户关联
        Long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<UserRoleDO>()
                        .eq(UserRoleDO::getRoleId, id));
        if (userCount > 0) {
            throw new BusinessException(400, "角色已分配给用户，无法删除");
        }

        // 删除角色
        roleMapper.deleteById(id);

        // 删除角色菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenuDO>()
                        .eq(RoleMenuDO::getRoleId, id));
    }

    @Override
    public void batchCreate(List<RoleSaveReqVO> createReqVOs) {
        if (createReqVOs == null || createReqVOs.isEmpty()) {
            return;
        }

        // 转换为 DO 列表
        List<RoleDO> doList = createReqVOs.stream()
                .map(reqVO -> {
                    RoleDO role = RoleConvert.INSTANCE.toDO(reqVO);
                    role.setType(2); // 自定义角色
                    return role;
                })
                .collect(java.util.stream.Collectors.toList());

        // 分批插入，每批100条，避免锁表时间过长
        List<List<RoleDO>> partitions = com.google.common.collect.Lists.partition(doList, 100);
        for (List<RoleDO> partition : partitions) {
            roleMapper.insertBatch(partition);
        }
    }

    @Override
    public void batchUpdate(List<RoleSaveReqVO> updateReqVOs) {
        if (updateReqVOs == null || updateReqVOs.isEmpty()) {
            return;
        }

        // 转换为 DO 列表
        List<RoleDO> doList = updateReqVOs.stream()
                .map(RoleConvert.INSTANCE::toDO)
                .collect(java.util.stream.Collectors.toList());

        // 分批更新，每批100条，避免锁表时间过长
        List<List<RoleDO>> partitions = com.google.common.collect.Lists.partition(doList, 100);
        for (List<RoleDO> partition : partitions) {
            roleMapper.updateBatch(partition);
        }
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 校验系统角色
        List<RoleDO> roles = roleMapper.selectByIds(ids);
        boolean hasSystemRole = roles.stream().anyMatch(role -> role.getType().equals(1));
        if (hasSystemRole) {
            throw new BusinessException(400, "系统内置角色，无法删除");
        }

        // 校验是否有用户关联
        for (Long roleId : ids) {
            Long userCount = userRoleMapper.selectCount(
                    new LambdaQueryWrapper<UserRoleDO>()
                            .eq(UserRoleDO::getRoleId, roleId));
            if (userCount > 0) {
                throw new BusinessException(400, "角色已分配给用户，无法删除");
            }
        }

        // 分批删除，每批1000个ID，避免SQL过长
        List<List<Long>> partitions = com.google.common.collect.Lists.partition(ids, 1000);
        for (List<Long> partition : partitions) {
            roleMapper.deleteByIds(partition); // 使用 MyBatis-Plus 自带方法

            // 删除角色菜单关联
            roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenuDO>()
                    .in(RoleMenuDO::getRoleId, partition));
        }
    }

    @Override
    public RoleDO getById(Long id) {
        RoleDO role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        return role;
    }

    @Override
    public List<RoleDO> getList() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<RoleDO>()
                        .orderByAsc(RoleDO::getSort));
    }

    @Override
    @DSTransactional
    public void assignMenu(RoleAssignMenuReqVO reqVO) {
        // 校验角色是否存在
        validateExists(reqVO.getRoleId());

        // 删除原有的角色菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenuDO>()
                        .eq(RoleMenuDO::getRoleId, reqVO.getRoleId()));

        // 批量插入新的角色菜单关联
        if (reqVO.getMenuIds() != null && !reqVO.getMenuIds().isEmpty()) {
            List<RoleMenuDO> roleMenuList = reqVO.getMenuIds().stream()
                    .map(menuId -> {
                        RoleMenuDO roleMenu = new RoleMenuDO();
                        roleMenu.setRoleId(reqVO.getRoleId());
                        roleMenu.setMenuId(menuId);
                        return roleMenu;
                    })
                    .toList();

            roleMenuList.forEach(roleMenuMapper::insert);
        }
    }

    @Override
    public List<RoleDO> getListByUserId(Long userId) {
        MPJLambdaWrapper<RoleDO> wrapper = new MPJLambdaWrapper<RoleDO>()
                .selectAll(RoleDO.class)
                .innerJoin(UserRoleDO.class, UserRoleDO::getRoleId, RoleDO::getId)
                .eq(UserRoleDO::getUserId, userId);
        return roleMapper.selectJoinList(RoleDO.class, wrapper);
    }

    /**
     * 校验角色是否存在
     *
     * @param id 角色ID
     * @return 角色信息
     */
    private RoleDO validateExists(Long id) {
        RoleDO role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        return role;
    }

    /**
     * 校验角色编码唯一性
     *
     * @param id   角色ID
     * @param code 角色编码
     */
    private void validateCodeUnique(Long id, String code) {
        RoleDO role = roleMapper.selectOne(
                new LambdaQueryWrapper<RoleDO>()
                        .eq(RoleDO::getCode, code));
        if (role != null && !role.getId().equals(id)) {
            throw new BusinessException(400, "角色编码已存在");
        }
    }

}
