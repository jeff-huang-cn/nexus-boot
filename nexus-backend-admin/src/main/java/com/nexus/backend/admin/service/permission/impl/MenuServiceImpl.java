package com.nexus.backend.admin.service.permission.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexus.backend.admin.controller.permission.vo.menu.MenuSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.permission.MenuDO;
import com.nexus.backend.admin.dal.dataobject.permission.RoleMenuDO;
import com.nexus.backend.admin.dal.mapper.permission.MenuMapper;
import com.nexus.backend.admin.dal.mapper.permission.RoleMenuMapper;
import com.nexus.backend.admin.service.permission.MenuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 *
 * @author nexus
 */
@Service
public class MenuServiceImpl implements MenuService {

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MenuSaveReqVO reqVO) {
        // 转换为 DO
        MenuDO menu = BeanUtil.copyProperties(reqVO, MenuDO.class);

        // 插入数据库
        menuMapper.insert(menu);

        return menu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MenuSaveReqVO reqVO) {
        // 校验菜单是否存在
        validateExists(reqVO.getId());

        // 转换为 DO
        MenuDO menu = BeanUtil.copyProperties(reqVO, MenuDO.class);

        // 更新数据库
        menuMapper.updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 校验菜单是否存在
        validateExists(id);

        // 校验是否有子菜单
        Long childCount = menuMapper.selectCount(
                new LambdaQueryWrapper<MenuDO>()
                        .eq(MenuDO::getParentId, id));
        if (childCount > 0) {
            throw new RuntimeException("存在子菜单，无法删除");
        }

        // 删除菜单
        menuMapper.deleteById(id);

        // 删除角色菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenuDO>()
                        .eq(RoleMenuDO::getMenuId, id));
    }

    @Override
    public MenuDO getById(Long id) {
        MenuDO menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }
        return menu;
    }

    @Override
    public List<MenuDO> getMenuList() {
        // 查询所有菜单，按排序号排序
        return menuMapper.selectList(
                new LambdaQueryWrapper<MenuDO>()
                        .orderByAsc(MenuDO::getSort));
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        List<RoleMenuDO> roleMenuList = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenuDO>()
                        .eq(RoleMenuDO::getRoleId, roleId));
        return roleMenuList.stream()
                .map(RoleMenuDO::getMenuId)
                .collect(Collectors.toList());
    }

    /**
     * 校验菜单是否存在
     *
     * @param id 菜单ID
     */
    private void validateExists(Long id) {
        MenuDO menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }
    }

}
