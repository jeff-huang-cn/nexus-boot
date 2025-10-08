package com.nexus.backend.admin.convert;

import com.nexus.backend.admin.controller.user.vo.UserRespVO;
import com.nexus.backend.admin.controller.user.vo.UserSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户转换器
 *
 * @author nexus
 */
@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    /**
     * DO 转 RespVO
     */
    UserRespVO toRespVO(UserDO userDO);

    /**
     * DO列表 转 RespVO列表
     */
    List<UserRespVO> toRespVOList(List<UserDO> userDOList);

    /**
     * SaveReqVO 转 DO
     */
    UserDO toDO(UserSaveReqVO saveReqVO);
}
