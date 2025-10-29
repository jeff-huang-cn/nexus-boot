package com.nexus.backend.admin.convert;

import com.nexus.backend.admin.controller.dict.vo.DictRespVO;
import com.nexus.backend.admin.controller.dict.vo.DictSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.dict.DictDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 字典转换器
 *
 * @author system
 */
@Mapper
public interface DictConvert {

    DictConvert INSTANCE = Mappers.getMapper(DictConvert.class);

    DictDO toDO(DictSaveReqVO saveReqVO);

    DictRespVO toRespVO(DictDO dictDO);

    List<DictRespVO> toRespVOList(List<DictDO> dictDOList);

}