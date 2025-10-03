package com.nexus.backend.admin.controller.permission.vo.role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色分配菜单请求 VO
 *
 * @author nexus
 */
@Data
public class RoleAssignMenuReqVO {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /**
     * 菜单ID列表
     */
    @NotNull(message = "菜单ID列表不能为空")
    private List<Long> menuIds;

}
