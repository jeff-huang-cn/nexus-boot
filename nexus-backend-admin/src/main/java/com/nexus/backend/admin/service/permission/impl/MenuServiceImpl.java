package com.nexus.backend.admin.service.permission.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexus.backend.admin.controller.permission.vo.menu.MenuRespVO;
import com.nexus.backend.admin.controller.permission.vo.menu.MenuSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.permission.MenuDO;
import com.nexus.backend.admin.dal.dataobject.permission.RoleMenuDO;
import com.nexus.backend.admin.dal.mapper.permission.MenuMapper;
import com.nexus.backend.admin.dal.mapper.permission.RoleMenuMapper;
import com.nexus.backend.admin.service.permission.MenuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public MenuRespVO getById(Long id) {
        MenuDO menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }
        return BeanUtil.copyProperties(menu, MenuRespVO.class);
    }

    @Override
    public List<MenuRespVO> getMenuTree() {
        // 查询所有菜单
        List<MenuDO> menuList = menuMapper.selectList(
                new LambdaQueryWrapper<MenuDO>()
                        .orderByAsc(MenuDO::getSort));

        // 转换为 VO
        List<MenuRespVO> menuVOList = menuList.stream()
                .map(menu -> BeanUtil.copyProperties(menu, MenuRespVO.class))
                .collect(Collectors.toList());

        // 构建树形结构
        return buildMenuTree(menuVOList, 0L);
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
     * 构建菜单树
     *
     * @param menuList 菜单列表
     * @param parentId 父菜单ID
     * @return 菜单树
     */
    private List<MenuRespVO> buildMenuTree(List<MenuRespVO> menuList, Long parentId) {
        List<MenuRespVO> result = new ArrayList<>();

        // 按父ID分组
        Map<Long, List<MenuRespVO>> menuMap = menuList.stream()
                .collect(Collectors.groupingBy(MenuRespVO::getParentId));

        // 递归构建树
        for (MenuRespVO menu : menuList) {
            if (menu.getParentId().equals(parentId)) {
                List<MenuRespVO> children = menuMap.get(menu.getId());
                if (children != null && !children.isEmpty()) {
                    menu.setChildren(children);
                }
                result.add(menu);
            }
        }

        return result;
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
