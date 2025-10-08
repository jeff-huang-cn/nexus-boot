package com.nexus.backend.admin.dal.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户信息表 Mapper 接口
 *
 * @author beckend
 * @since 2025-10-02
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

    /**
     * 批量插入
     *
     * @param list 用户列表
     * @return 插入数量
     */
    int insertBatch(@Param("list") List<UserDO> list);

    /**
     * 批量更新
     *
     * @param list 用户列表
     * @return 更新数量
     */
    int updateBatch(@Param("list") List<UserDO> list);
}
