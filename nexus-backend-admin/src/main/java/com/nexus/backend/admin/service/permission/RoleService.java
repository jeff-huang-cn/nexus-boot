package com.nexus.backend.admin.service.permission;

import com.nexus.backend.admin.controller.permission.vo.role.RoleAssignMenuReqVO;
import com.nexus.backend.admin.controller.permission.vo.role.RoleSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.permission.RoleDO;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author nexus
 */
public interface RoleService {

    /**
     * 创建角色
     *
     * @param reqVO 角色信息
     * @return 角色ID
     */
    Long create(RoleSaveReqVO reqVO);

    /**
     * 更新角色
     *
     * @param reqVO 角色信息
     */
    void update(RoleSaveReqVO reqVO);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void delete(Long id);

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色信息
     */
    RoleDO getById(Long id);

    /**
     * 获取角色列表
     *
     * @return 角色列表
     */
    List<RoleDO> getList();

    /**
     * 分配菜单
     *
     * @param reqVO 分配信息
     */
    void assignMenu(RoleAssignMenuReqVO reqVO);

    List<RoleDO> getListByUserId(Long userId);
}
