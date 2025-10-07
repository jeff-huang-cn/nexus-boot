package com.nexus.backend.admin.controller.permission;

import cn.hutool.core.bean.BeanUtil;
import com.nexus.framework.web.result.Result;
import com.nexus.backend.admin.controller.permission.vo.role.RoleAssignMenuReqVO;
import com.nexus.backend.admin.controller.permission.vo.role.RoleRespVO;
import com.nexus.backend.admin.controller.permission.vo.role.RoleSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.permission.RoleDO;
import com.nexus.backend.admin.service.permission.RoleService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    @PreAuthorize("hasAuthority('system:role:create')")
    public Result<Long> create(@Valid @RequestBody RoleSaveReqVO reqVO) {
        Long id = roleService.create(reqVO);
        return Result.success(id);
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:update')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleSaveReqVO reqVO) {
        reqVO.setId(id);
        roleService.update(reqVO);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<RoleRespVO> getById(@PathVariable Long id) {
        RoleDO role = roleService.getById(id);
        RoleRespVO vo = BeanUtil.copyProperties(role, RoleRespVO.class);
        return Result.success(vo);
    }

    /**
     * 获取角色列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<List<RoleRespVO>> getList() {
        List<RoleDO> roleList = roleService.getList();
        List<RoleRespVO> voList = roleList.stream()
                .map(role -> BeanUtil.copyProperties(role, RoleRespVO.class))
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 分配菜单
     */
    @PostMapping("/assign-menu")
    @PreAuthorize("hasAuthority('system:role:assign')")
    public Result<Void> assignMenu(@Valid @RequestBody RoleAssignMenuReqVO reqVO) {
        roleService.assignMenu(reqVO);
        return Result.success();
    }

}
