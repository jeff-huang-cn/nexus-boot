package com.nexus.backend.admin.controller.permission;

import com.nexus.backend.admin.controller.permission.vo.role.RoleAssignMenuReqVO;
import com.nexus.backend.admin.controller.permission.vo.role.RoleRespVO;
import com.nexus.backend.admin.controller.permission.vo.role.RoleSaveReqVO;
import com.nexus.backend.admin.convert.RoleConvert;
import com.nexus.backend.admin.dal.dataobject.permission.RoleDO;
import com.nexus.backend.admin.service.permission.RoleService;
import com.nexus.framework.excel.ExcelUtils;
import com.nexus.framework.web.result.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        RoleRespVO vo = RoleConvert.INSTANCE.toRespVO(role);
        return Result.success(vo);
    }

    /**
     * 获取角色列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<List<RoleRespVO>> getList() {
        List<RoleDO> roleList = roleService.getList();
        List<RoleRespVO> voList = RoleConvert.INSTANCE.toRespVOList(roleList);
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

    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        roleService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 导出角色 Excel
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('system:role:export')")
    public void export(HttpServletResponse response) throws IOException {
        // 查询数据
        List<RoleDO> list = roleService.getList();

        // 使用 MapStruct 转换为 VO
        List<RoleRespVO> voList = RoleConvert.INSTANCE.toRespVOList(list);

        // 使用 ExcelUtils 导出
        ExcelUtils.export(response, voList, RoleRespVO.class, "角色数据", "角色数据");
    }

}
