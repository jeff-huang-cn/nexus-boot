package com.nexus.backend.admin.service.dict;

import com.nexus.backend.admin.controller.dict.vo.DictPageReqVO;
import com.nexus.backend.admin.controller.dict.vo.DictRespVO;
import com.nexus.backend.admin.controller.dict.vo.DictSaveReqVO;
import com.nexus.framework.web.result.PageResult;

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

    /**
     * 分页查询字典列表
     *
     * @param pageReqVO 分页查询参数
     * @return 分页结果
     */
    PageResult<DictRespVO> getPage(DictPageReqVO pageReqVO);

    /**
     * 根据ID获取字典详情
     *
     * @param id 字典ID
     * @return 字典详情
     */
    DictRespVO getById(Long id);

    /**
     * 创建字典
     *
     * @param saveReqVO 保存参数
     * @return 字典ID
     */
    Long create(DictSaveReqVO saveReqVO);

    /**
     * 更新字典
     *
     * @param saveReqVO 保存参数
     */
    void update(DictSaveReqVO saveReqVO);

    /**
     * 删除字典
     *
     * @param id 字典ID
     */
    void delete(Long id);

    /**
     * 批量删除字典
     *
     * @param ids 字典ID列表
     */
    void deleteBatch(List<Long> ids);

}
