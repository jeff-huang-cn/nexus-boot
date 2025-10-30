package com.nexus.backend.admin.service.dict.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexus.backend.admin.controller.dict.vo.*;
import com.nexus.backend.admin.convert.DictConvert;
import com.nexus.backend.admin.dal.dataobject.dict.DictDO;
import com.nexus.backend.admin.dal.mapper.dict.DictMapper;
import com.nexus.backend.admin.service.dict.DictService;
import com.nexus.framework.web.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                        .orderByAsc(DictDO::getSort));

        return DictConvert.INSTANCE.toRespVOList(dictList);
    }

    @Override
    public List<DictTypeGroupRespVO> getDictTypeGroups() {
        // 查询所有字典数据
        List<DictDO> allDict = dictMapper.selectList(
                new LambdaQueryWrapper<DictDO>()
                        .orderByAsc(DictDO::getDictType, DictDO::getSort)
        );

        // 按 dict_type 分组
        Map<String, List<DictDO>> groupedMap = allDict.stream()
                .collect(Collectors.groupingBy(DictDO::getDictType));

        // 构建返回列表
        List<DictTypeGroupRespVO> result = new ArrayList<>();
        for (Map.Entry<String, List<DictDO>> entry : groupedMap.entrySet()) {
            String dictType = entry.getKey();
            List<DictDO> items = entry.getValue();

            DictTypeGroupRespVO groupVO = new DictTypeGroupRespVO();
            groupVO.setDictType(dictType);
            groupVO.setItemCount(items.size());

            // 取第一个字典项的状态
            if (!items.isEmpty()) {
                groupVO.setStatus(items.get(0).getStatus());
            }

            // 取前3个字典项的标签作为示例
            String sampleLabels = items.stream()
                    .limit(3)
                    .map(DictDO::getDictLabel)
                    .collect(Collectors.joining(", "));
            groupVO.setSampleLabels(sampleLabels);

            result.add(groupVO);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveDictType(DictTypeBatchSaveReqVO batchSaveReqVO) {
        String dictType = batchSaveReqVO.getDictType();
        List<DictItemSaveVO> items = batchSaveReqVO.getItems();

        // 1. 查询该字典类型下的所有现有记录
        List<DictDO> existingList = dictMapper.selectList(
                new LambdaQueryWrapper<DictDO>()
                        .eq(DictDO::getDictType, dictType)
        );

        // 构建 ID -> DO 的映射，方便后续查找
        Map<Long, DictDO> existingMap = existingList.stream()
                .collect(Collectors.toMap(DictDO::getId, Function.identity()));

        // 2. 处理新提交的数据
        Set<Long> processedIds = new HashSet<>();

        for (DictItemSaveVO item : items) {
            if (item.getId() != null && existingMap.containsKey(item.getId())) {
                // 更新已存在的记录
                DictDO updateDO = new DictDO();
                updateDO.setId(item.getId());
                updateDO.setDictType(dictType);
                updateDO.setDictLabel(item.getDictLabel());
                updateDO.setDictValue(item.getDictValue());
                updateDO.setSort(item.getSort());
                updateDO.setStatus(item.getStatus());
                updateDO.setColorType(item.getColorType());
                updateDO.setCssClass(item.getCssClass());
                updateDO.setRemark(item.getRemark());

                dictMapper.updateById(updateDO);
                processedIds.add(item.getId());
            } else {
                // 新增记录
                DictDO insertDO = new DictDO();
                insertDO.setDictType(dictType);
                insertDO.setDictLabel(item.getDictLabel());
                insertDO.setDictValue(item.getDictValue());
                insertDO.setSort(item.getSort());
                insertDO.setStatus(item.getStatus() != null ? item.getStatus() : 1);
                insertDO.setColorType(item.getColorType());
                insertDO.setCssClass(item.getCssClass());
                insertDO.setRemark(item.getRemark());

                dictMapper.insert(insertDO);
            }
        }

        // 3. 删除不在新列表中的记录
        List<Long> deleteIds = existingMap.keySet().stream()
                .filter(id -> !processedIds.contains(id))
                .collect(Collectors.toList());

        if (!deleteIds.isEmpty()) {
            dictMapper.deleteByIds(deleteIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictType(String dictType) {
        // 查询该字典类型下的所有记录
        List<DictDO> dictList = dictMapper.selectList(
                new LambdaQueryWrapper<DictDO>()
                        .eq(DictDO::getDictType, dictType)
        );

        if (dictList.isEmpty()) {
            throw new BusinessException(404, "该字典类型不存在");
        }

        // 批量删除
        List<Long> ids = dictList.stream()
                .map(DictDO::getId)
                .collect(Collectors.toList());

        dictMapper.deleteByIds(ids);
    }

}
