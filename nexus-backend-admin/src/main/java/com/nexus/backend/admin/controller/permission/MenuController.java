package com.nexus.backend.admin.controller.permission;

import cn.hutool.core.bean.BeanUtil;
import com.nexus.framework.security.util.SecurityContextUtils;
import com.nexus.framework.web.exception.BusinessException;
import com.nexus.framework.web.result.Result;
import com.nexus.framework.utils.tree.TreeUtils;
import com.nexus.backend.admin.controller.permission.vo.menu.MenuRespVO;
import com.nexus.backend.admin.controller.permission.vo.menu.MenuSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.permission.MenuDO;
import com.nexus.backend.admin.service.permission.MenuService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单管理接口
 *
 * @author nexus
 */
@RestController
@RequestMapping("/system/menu")
public class MenuController {

    @Resource
    private MenuService menuService;

    /**
     * 创建菜单
     */
    @PostMapping
    @PreAuthorize("@ss.hasPermission('system:menu:create')")
    public Result<Long> create(@Valid @RequestBody MenuSaveReqVO reqVO) {
        Long id = menuService.create(reqVO);
        return Result.success(id);
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('system:menu:update')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MenuSaveReqVO reqVO) {
        reqVO.setId(id);
        menuService.update(reqVO);
        return Result.success();
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('system:menu:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('system:menu:query')")
    public Result<MenuRespVO> getById(@PathVariable Long id) {
        MenuDO menu = menuService.getById(id);
        MenuRespVO vo = BeanUtil.copyProperties(menu, MenuRespVO.class);
        return Result.success(vo);
    }

    /**
     * 获取菜单树列表（包含所有类型：目录、菜单、按钮）
     * 由前端决定显示哪些类型
     */
    @GetMapping("/tree")
    @PreAuthorize("@ss.hasPermission('system:menu:query')")
    public Result<List<MenuRespVO>> getMenuTree() {
        // 1. Service 返回 DO 列表
        List<MenuDO> menuList = menuService.getMenuList();

        // 2. Controller 转换为 VO（包含所有类型：目录、菜单、按钮）
        // 由前端决定是否显示按钮权限
        List<MenuRespVO> menuVOList = menuList.stream()
                .map(menu -> BeanUtil.copyProperties(menu, MenuRespVO.class))
                .collect(Collectors.toList());

        // 3. 使用 TreeUtils 构建树形结构
        List<MenuRespVO> menuTree = TreeUtils.buildTree(
                menuVOList,
                MenuRespVO::getId,
                MenuRespVO::getParentId,
                MenuRespVO::setChildren,
                0L);

        return Result.success(menuTree);
    }

    /**
     * 获取完整菜单树（用于菜单管理页面，包含按钮）
     */
    @GetMapping("/tree/full")
    @PreAuthorize("@ss.hasPermission('system:menu:query')")
    public Result<List<MenuRespVO>> getFullMenuTree() {
        // 1. Service 返回 DO 列表
        List<MenuDO> menuList = menuService.getMenuList();

        // 2. Controller 转换为 VO（包含所有类型：目录、菜单、按钮）
        List<MenuRespVO> menuVOList = menuList.stream()
                .map(menu -> BeanUtil.copyProperties(menu, MenuRespVO.class))
                .collect(Collectors.toList());

        // 3. 使用 TreeUtils 构建树形结构
        List<MenuRespVO> menuTree = TreeUtils.buildTree(
                menuVOList,
                MenuRespVO::getId,
                MenuRespVO::getParentId,
                MenuRespVO::setChildren,
                0L);

        return Result.success(menuTree);
    }

    /**
     * 获取角色的菜单ID列表
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("@ss.hasPermission('system:menu:query')")
    public Result<List<Long>> getMenuIdsByRoleId(@PathVariable Long roleId) {
        List<Long> menuIds = menuService.getMenuIdsByRoleId(roleId);
        return Result.success(menuIds);
    }

    /**
     * 获取当前登录用户的菜单树
     * 用于前端导航菜单，根据用户权限过滤
     */
    @GetMapping("/user")
    @PreAuthorize("@ss.hasPermission('system:menu:query')")
    public Result<List<MenuRespVO>> getUserMenuTree() {
        Long userId = SecurityContextUtils.getLoginUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        List<MenuDO> menuList = menuService.getUserMenus(userId);

        List<MenuRespVO> menuVOList = menuList.stream()
                .map(menu -> BeanUtil.copyProperties(menu, MenuRespVO.class))
                .collect(Collectors.toList());

        List<MenuRespVO> menuTree = TreeUtils.buildTree(
                menuVOList,
                MenuRespVO::getId,
                MenuRespVO::getParentId,
                MenuRespVO::setChildren,
                0L);

        return Result.success(menuTree);
    }

}
