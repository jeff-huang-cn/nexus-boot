package com.nexus.backend.admin.controller.permission;

import cn.hutool.core.bean.BeanUtil;
import com.nexus.framework.security.util.SecurityUtils;
import com.nexus.framework.web.result.Result;
import com.nexus.framework.utils.tree.TreeUtils;
import com.nexus.backend.admin.controller.permission.vo.menu.MenuRespVO;
import com.nexus.backend.admin.controller.permission.vo.menu.MenuSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.permission.MenuDO;
import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import com.nexus.backend.admin.service.permission.MenuService;
import com.nexus.backend.admin.service.user.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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

    @Resource
    private UserService userService;

    /**
     * 创建菜单
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody MenuSaveReqVO reqVO) {
        Long id = menuService.create(reqVO);
        return Result.success(id);
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MenuSaveReqVO reqVO) {
        reqVO.setId(id);
        menuService.update(reqVO);
        return Result.success();
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    public Result<MenuRespVO> getById(@PathVariable Long id) {
        MenuDO menu = menuService.getById(id);
        MenuRespVO vo = BeanUtil.copyProperties(menu, MenuRespVO.class);
        return Result.success(vo);
    }

    /**
     * 获取菜单树列表（用于前端导航，过滤掉按钮）
     */
    @GetMapping("/tree")
    public Result<List<MenuRespVO>> getMenuTree() {
        // 1. Service 返回 DO 列表
        List<MenuDO> menuList = menuService.getMenuList();

        // 2. Controller 转换为 VO，过滤掉按钮类型（type=3）
        // 按钮权限用于权限控制，不显示在菜单树中
        List<MenuRespVO> menuVOList = menuList.stream()
                .filter(menu -> menu.getType() != 3) // 过滤掉按钮
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
    public Result<List<Long>> getMenuIdsByRoleId(@PathVariable Long roleId) {
        List<Long> menuIds = menuService.getMenuIdsByRoleId(roleId);
        return Result.success(menuIds);
    }

    /**
     * 获取当前登录用户的菜单树
     * 用于前端导航菜单，根据用户权限过滤
     */
    @GetMapping("/user")
    public Result<List<MenuRespVO>> getUserMenuTree() {
        // 1. 获取当前登录用户名
        String username = SecurityUtils.getUsername();
        if (username == null) {
            throw new RuntimeException("未登录");
        }

        // 2. 根据用户名获取用户信息
        UserDO user = userService.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 3. 获取用户的菜单列表（已过滤权限和按钮）
        List<MenuDO> menuList = menuService.getUserMenus(user.getId());

        // 4. 转换为VO
        List<MenuRespVO> menuVOList = menuList.stream()
                .map(menu -> BeanUtil.copyProperties(menu, MenuRespVO.class))
                .collect(Collectors.toList());

        // 5. 构建树形结构
        List<MenuRespVO> menuTree = TreeUtils.buildTree(
                menuVOList,
                MenuRespVO::getId,
                MenuRespVO::getParentId,
                MenuRespVO::setChildren,
                0L);

        return Result.success(menuTree);
    }

}
