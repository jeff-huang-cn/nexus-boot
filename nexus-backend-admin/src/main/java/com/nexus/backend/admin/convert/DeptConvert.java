package com.nexus.backend.admin.convert;

import com.nexus.backend.admin.controller.dept.vo.DeptRespVO;
import com.nexus.backend.admin.controller.dept.vo.DeptSaveReqVO;
import com.nexus.backend.admin.dal.dataobject.dept.DeptDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 部门管理表转换器
 *
 * @author beckend
 * @since 2025-10-27
 */
@Mapper
public interface DeptConvert {

    DeptConvert INSTANCE = Mappers.getMapper(DeptConvert.class);

    /**
     * DO 转 RespVO
     */
    DeptRespVO toRespVO(DeptDO deptDO);

    /**
     * DO列表 转 RespVO列表
     */
    List<DeptRespVO> toRespVOList(List<DeptDO> deptDOList);

    /**
     * SaveReqVO 转 DO
     */
    DeptDO toDO(DeptSaveReqVO saveReqVO);
}

