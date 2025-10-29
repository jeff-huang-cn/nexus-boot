package com.nexus.backend.admin.service.dict;

import com.nexus.backend.admin.controller.dict.vo.*;

import java.util.List;

/**
 * 字典服务接口
 *
 * @author nexus
 */
public interface DictService {

    /**
     * 根据字典类型获取字典列表
     *
     * @param dictType 字典类型
     * @return 字典列表
     */
    List<DictRespVO> getListByType(String dictType);

    /**
     * 获取字典类型分组列表
     *
     * @return 字典类型分组列表
     */
    List<DictTypeGroupRespVO> getDictTypeGroups();

    /**
     * 批量保存某个字典类型下的所有字典项
     *
     * @param batchSaveReqVO 批量保存参数
     */
    void batchSaveDictType(DictTypeBatchSaveReqVO batchSaveReqVO);

    /**
     * 删除字典类型及其所有字典项
     *
     * @param dictType 字典类型
     */
    void deleteDictType(String dictType);

}
