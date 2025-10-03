package com.nexus.backend.admin.dal.mapper.dict;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.dal.dataobject.dict.DictDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统字典 Mapper
 *
 * @author nexus
 */
@Mapper
public interface DictMapper extends BaseMapper<DictDO> {
}
