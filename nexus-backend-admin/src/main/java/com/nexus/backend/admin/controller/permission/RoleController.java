package com.nexus.backend.admin.controller.permission;

import com.nexus.backend.admin.common.result.Result;
import com.nexus.backend.admin.controller.permission.vo.role.RoleAssignMenuReqVO;
import com.nexus.backend.admin.controller.permission.vo.role.RoleRespVO;
import com.nexus.backend.admin.controller.permission.vo.role.RoleSaveReqVO;
import com.nexus.backend.admin.service.permission.RoleService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口
 *
 * @author nexus
 */
@RestController
@RequestMapping("/system/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    /**
     * 创建角色
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody RoleSaveReqVO reqVO) {
        Long id = roleService.create(reqVO);
        return Result.success(id);
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleSaveReqVO reqVO) {
        reqVO.setId(id);
        roleService.update(reqVO);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public Result<RoleRespVO> getById(@PathVariable Long id) {
        RoleRespVO role = roleService.getById(id);
        return Result.success(role);
    }

    /**
     * 获取角色列表
     */
    @GetMapping("/list")
    public Result<List<RoleRespVO>> getList() {
        List<RoleRespVO> roleList = roleService.getList();
        return Result.success(roleList);
    }

    /**
     * 分配菜单
     */
    @PostMapping("/assign-menu")
    public Result<Void> assignMenu(@Valid @RequestBody RoleAssignMenuReqVO reqVO) {
        roleService.assignMenu(reqVO);
        return Result.success();
    }

}
