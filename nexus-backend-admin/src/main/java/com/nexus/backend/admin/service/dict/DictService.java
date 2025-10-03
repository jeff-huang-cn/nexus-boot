package com.nexus.backend.admin.service.dict;

import com.nexus.backend.admin.controller.dict.vo.DictRespVO;

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
     * 获取所有字典数据（用于前端缓存）
     *
     * @return 所有字典数据
     */
    List<DictRespVO> getAllDict();

}
