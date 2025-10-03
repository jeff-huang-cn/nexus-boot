package com.nexus.backend.admin.controller.permission;

import com.nexus.backend.admin.common.result.Result;
import com.nexus.backend.admin.controller.permission.vo.menu.MenuRespVO;
import com.nexus.backend.admin.controller.permission.vo.menu.MenuSaveReqVO;
import com.nexus.backend.admin.service.permission.MenuService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        MenuRespVO menu = menuService.getById(id);
        return Result.success(menu);
    }

    /**
     * 获取菜单树列表
     */
    @GetMapping("/tree")
    public Result<List<MenuRespVO>> getMenuTree() {
        List<MenuRespVO> menuTree = menuService.getMenuTree();
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
