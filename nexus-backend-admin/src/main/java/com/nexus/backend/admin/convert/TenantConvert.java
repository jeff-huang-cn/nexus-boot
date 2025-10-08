package com.nexus.backend.admin.convert;

import com.nexus.backend.admin.controller.tenant.vo.TenantRespVO;
import com.nexus.backend.admin.controller.tenant.vo.TenantSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.tenant.TenantDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 租户管理表转换器
 *
 * @author beckend
 * @since 2025-10-08
 */
@Mapper
public interface TenantConvert {

    TenantConvert INSTANCE = Mappers.getMapper(TenantConvert.class);

    /**
     * DO 转 RespVO
     */
    TenantRespVO toRespVO(TenantDO tenantDO);

    /**
     * DO列表 转 RespVO列表
     */
    List<TenantRespVO> toRespVOList(List<TenantDO> tenantDOList);

    /**
     * SaveReqVO 转 DO
     */
    TenantDO toDO(TenantSaveReqVO saveReqVO);
}

