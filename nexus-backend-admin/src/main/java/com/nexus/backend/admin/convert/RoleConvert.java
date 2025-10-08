package com.nexus.backend.admin.convert;

import com.nexus.backend.admin.controller.permission.vo.role.RoleRespVO;
import com.nexus.backend.admin.controller.permission.vo.role.RoleSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.permission.RoleDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 角色转换器
 *
 * @author nexus
 */
@Mapper
public interface RoleConvert {

    RoleConvert INSTANCE = Mappers.getMapper(RoleConvert.class);

    /**
     * DO 转 RespVO
     */
    RoleRespVO toRespVO(RoleDO roleDO);

    /**
     * DO列表 转 RespVO列表
     */
    List<RoleRespVO> toRespVOList(List<RoleDO> roleDOList);

    /**
     * SaveReqVO 转 DO
     */
    RoleDO toDO(RoleSaveReqVO saveReqVO);
}
