package com.nexus.backend.admin.service.dict.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexus.backend.admin.controller.dict.vo.DictPageReqVO;
import com.nexus.backend.admin.controller.dict.vo.DictRespVO;
import com.nexus.backend.admin.controller.dict.vo.DictSaveReqVO;
import com.nexus.backend.admin.convert.DictConvert;
import com.nexus.backend.admin.dal.dataobject.dict.DictDO;
import com.nexus.backend.admin.dal.mapper.dict.DictMapper;
import com.nexus.backend.admin.service.dict.DictService;
import com.nexus.framework.web.exception.BusinessException;
import com.nexus.framework.web.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典服务实现
 *
 * @author nexus
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final DictMapper dictMapper;

    @Override
    public List<DictRespVO> getListByType(String dictType) {
        List<DictDO> dictList = dictMapper.selectList(
                new LambdaQueryWrapper<DictDO>()
                        .eq(DictDO::getDictType, dictType)
                        .eq(DictDO::getStatus, 1) // 只查询启用的
                        .orderByAsc(DictDO::getSort));

        return DictConvert.INSTANCE.toRespVOList(dictList);
    }

    @Override
    public List<DictRespVO> getAllDict() {
        List<DictDO> dictList = dictMapper.selectList(
                new LambdaQueryWrapper<DictDO>()
                        .eq(DictDO::getStatus, 1) // 只查询启用的
                        .orderByAsc(DictDO::getDictType, DictDO::getSort));

        return DictConvert.INSTANCE.toRespVOList(dictList);
    }

    @Override
    public PageResult<DictRespVO> getPage(DictPageReqVO pageReqVO) {
        // 构建查询条件
        LambdaQueryWrapper<DictDO> wrapper = new LambdaQueryWrapper<>();

        // 字典类型 - 精确查询
        if (StringUtils.hasText(pageReqVO.getDictType())) {
            wrapper.eq(DictDO::getDictType, pageReqVO.getDictType());
        }

        // 字典标签 - 模糊查询
        if (StringUtils.hasText(pageReqVO.getDictLabel())) {
            wrapper.like(DictDO::getDictLabel, pageReqVO.getDictLabel());
        }

        // 字典值 - 模糊查询
        if (StringUtils.hasText(pageReqVO.getDictValue())) {
            wrapper.like(DictDO::getDictValue, pageReqVO.getDictValue());
        }

        // 状态 - 精确查询
        if (pageReqVO.getStatus() != null) {
            wrapper.eq(DictDO::getStatus, pageReqVO.getStatus());
        }

        // 排序：按字典类型、排序号
        wrapper.orderByAsc(DictDO::getDictType, DictDO::getSort);

        // 分页查询
        IPage<DictDO> page = new Page<>(pageReqVO.getPageNum(), pageReqVO.getPageSize());
        IPage<DictDO> result = dictMapper.selectPage(page, wrapper);

        // 转换为 VO
        List<DictRespVO> list = DictConvert.INSTANCE.toRespVOList(result.getRecords());

        return new PageResult<>(list, result.getTotal());
    }

    @Override
    public DictRespVO getById(Long id) {
        DictDO dictDO = dictMapper.selectById(id);
        if (dictDO == null) {
            throw new BusinessException(404, "字典不存在");
        }
        return DictConvert.INSTANCE.toRespVO(dictDO);
    }

    @Override
    public Long create(DictSaveReqVO saveReqVO) {
        // 校验字典类型和值的唯一性
        validateDictUnique(null, saveReqVO.getDictType(), saveReqVO.getDictValue());

        // 转换为 DO 并插入
        DictDO dictDO = DictConvert.INSTANCE.toDO(saveReqVO);
        dictMapper.insert(dictDO);
        return dictDO.getId();
    }

    @Override
    public void update(DictSaveReqVO saveReqVO) {
        // 校验存在
        validateExists(saveReqVO.getId());

        // 校验字典类型和值的唯一性
        validateDictUnique(saveReqVO.getId(), saveReqVO.getDictType(), saveReqVO.getDictValue());

        // 更新
        DictDO dictDO = DictConvert.INSTANCE.toDO(saveReqVO);
        dictMapper.updateById(dictDO);
    }

    @Override
    public void delete(Long id) {
        // 校验存在
        validateExists(id);

        // 删除
        dictMapper.deleteById(id);
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 批量删除
        dictMapper.deleteBatchIds(ids);
    }

    /**
     * 校验字典是否存在
     */
    private void validateExists(Long id) {
        if (dictMapper.selectById(id) == null) {
            throw new BusinessException(404, "字典不存在");
        }
    }

    /**
     * 校验字典类型和值的唯一性
     */
    private void validateDictUnique(Long id, String dictType, String dictValue) {
        LambdaQueryWrapper<DictDO> wrapper = new LambdaQueryWrapper<DictDO>()
                .eq(DictDO::getDictType, dictType)
                .eq(DictDO::getDictValue, dictValue);

        if (id != null) {
            wrapper.ne(DictDO::getId, id);
        }

        Long count = dictMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(400, "该字典类型下已存在相同的字典值");
        }
    }

}
