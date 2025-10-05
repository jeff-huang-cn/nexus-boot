package com.nexus.framework.security.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.framework.security.dal.dataobject.JwkDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JwkMapper extends BaseMapper<JwkDO> {
}
