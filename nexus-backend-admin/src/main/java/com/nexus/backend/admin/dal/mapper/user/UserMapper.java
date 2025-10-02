package com.nexus.backend.admin.dal.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息表 Mapper 接口
 *
 * @author beckend
 * @since 2025-10-02
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

}
