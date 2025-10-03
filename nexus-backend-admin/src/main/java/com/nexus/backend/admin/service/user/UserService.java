package com.nexus.backend.admin.service.user;

import com.nexus.backend.admin.controller.user.vo.UserPageReqVO;
import com.nexus.backend.admin.controller.user.vo.UserRespVO;
import com.nexus.backend.admin.controller.user.vo.UserSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import com.nexus.framework.web.result.PageResult;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户信息表 Service 接口
 *
 * @author beckend
 * @since 2025-10-02
 */
public interface UserService {

    /**
     * 创建用户信息表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long create(@Valid UserSaveReqVO createReqVO);

    /**
     * 更新用户信息表
     *
     * @param updateReqVO 更新信息
     */
    void update(@Valid UserSaveReqVO updateReqVO);

    /**
     * 删除用户信息表
     *
     * @param id 编号
     */
    void delete(Long id);

    /**
     * 批量删除用户信息表
     *
     * @param ids 编号列表
     */
    void deleteBatch(List<Long> ids);

    /**
     * 获得用户信息表详情
     *
     * @param id 编号
     * @return 用户信息表详情
     */
    UserDO getById(Long id);

    /**
     * 获得用户信息表分页
     *
     * @param pageReqVO 分页查询
     * @return 用户信息表分页
     */
    PageResult<UserRespVO> getPage(UserPageReqVO pageReqVO);

    /**
     * 获得用户信息表列表（用于导出）
     *
     * @param pageReqVO 查询条件
     * @return 用户信息表列表
     */
    List<UserDO> getList(UserPageReqVO pageReqVO);
}
