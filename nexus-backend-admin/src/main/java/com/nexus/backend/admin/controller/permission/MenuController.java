package com.nexus.backend.admin.controller.permission;

import cn.hutool.core.bean.BeanUtil;
import com.nexus.framework.web.result.Result;
import com.nexus.framework.utils.tree.TreeUtils;
import com.nexus.backend.admin.controller.permission.vo.menu.MenuRespVO;
import com.nexus.backend.admin.controller.permission.vo.menu.MenuSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.permission.MenuDO;
import com.nexus.backend.admin.service.permission.MenuService;
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
     * 获取菜单树列表
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
     * 获取角色的菜单ID列表
     */
    @GetMapping("/role/{roleId}")
    public Result<List<Long>> getMenuIdsByRoleId(@PathVariable Long roleId) {
        List<Long> menuIds = menuService.getMenuIdsByRoleId(roleId);
        return Result.success(menuIds);
    }

}
