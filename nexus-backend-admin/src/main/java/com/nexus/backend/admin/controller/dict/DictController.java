package com.nexus.backend.admin.controller.dict;

import com.nexus.backend.admin.controller.dict.vo.*;
import com.nexus.framework.web.result.Result;
import com.nexus.backend.admin.service.dict.DictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理接口
 *
 * @author nexus
 */
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    /**
     * 根据字典类型获取字典列表（用于编辑时加载）
     */
    @GetMapping("/type/{dictType}")
    @PreAuthorize("hasAuthority('system:dict:query')")
    public Result<List<DictRespVO>> getListByType(@PathVariable String dictType) {
        List<DictRespVO> dictList = dictService.getListByType(dictType);
        return Result.success(dictList);
    }

    /**
     * 获取字典类型分组列表
     */
    @GetMapping("/type-groups")
    @PreAuthorize("hasAuthority('system:dict:query')")
    public Result<List<DictTypeGroupRespVO>> getDictTypeGroups() {
        List<DictTypeGroupRespVO> groups = dictService.getDictTypeGroups();
        return Result.success(groups);
    }

    /**
     * 批量保存字典类型下的所有字典项
     */
    @PostMapping("/type/batch-save")
    @PreAuthorize("hasAuthority('system:dict:create') or hasAuthority('system:dict:update')")
    public Result<Void> batchSaveDictType(@Valid @RequestBody DictTypeBatchSaveReqVO batchSaveReqVO) {
        dictService.batchSaveDictType(batchSaveReqVO);
        return Result.success();
    }

    /**
     * 删除字典类型及其所有字典项
     */
    @DeleteMapping("/type/{dictType}")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    public Result<Void> deleteDictType(@PathVariable String dictType) {
        dictService.deleteDictType(dictType);
        return Result.success();
    }

}
