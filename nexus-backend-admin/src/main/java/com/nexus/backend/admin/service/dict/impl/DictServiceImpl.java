package com.nexus.backend.admin.service.dict.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexus.backend.admin.controller.dict.vo.DictRespVO;
import com.nexus.backend.admin.dal.dataobject.dict.DictDO;
import com.nexus.backend.admin.dal.mapper.dict.DictMapper;
import com.nexus.backend.admin.service.dict.DictService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典服务实现
 *
 * @author nexus
 */
@Service
public class DictServiceImpl implements DictService {

    @Resource
    private DictMapper dictMapper;

    @Override
    public List<DictRespVO> getListByType(String dictType) {
        List<DictDO> dictList = dictMapper.selectList(
                new LambdaQueryWrapper<DictDO>()
                        .eq(DictDO::getDictType, dictType)
                        .eq(DictDO::getStatus, 1) // 只查询启用的
                        .orderByAsc(DictDO::getSort));

        return dictList.stream()
                .map(dict -> BeanUtil.copyProperties(dict, DictRespVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<DictRespVO> getAllDict() {
        List<DictDO> dictList = dictMapper.selectList(
                new LambdaQueryWrapper<DictDO>()
                        .eq(DictDO::getStatus, 1) // 只查询启用的
                        .orderByAsc(DictDO::getDictType, DictDO::getSort));

        return dictList.stream()
                .map(dict -> BeanUtil.copyProperties(dict, DictRespVO.class))
                .collect(Collectors.toList());
    }

}
