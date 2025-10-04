package com.nexus.backend.admin.service.permission;

import com.nexus.backend.admin.controller.permission.vo.menu.MenuSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.permission.MenuDO;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author nexus
 */
public interface MenuService {

    /**
     * 创建菜单
     *
     * @param reqVO 菜单信息
     * @return 菜单ID
     */
    Long create(MenuSaveReqVO reqVO);

    /**
     * 更新菜单
     *
     * @param reqVO 菜单信息
     */
    void update(MenuSaveReqVO reqVO);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     */
    void delete(Long id);

    /**
     * 获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单信息
     */
    MenuDO getById(Long id);

    /**
     * 获取菜单列表
     *
     * @return 菜单列表
     */
    List<MenuDO> getMenuList();

    /**
     * 获取角色的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);

    /**
     * 获取用户的菜单列表（根据用户权限过滤）
     * 用于前端导航菜单展示，只返回用户有权限访问的菜单
     *
     * @param userId 用户ID
     * @return 菜单列表（已过滤权限，不包含按钮）
     */
    List<MenuDO> getUserMenus(Long userId);
}
