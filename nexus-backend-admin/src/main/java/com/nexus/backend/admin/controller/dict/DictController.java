package com.nexus.backend.admin.controller.dict;

import com.nexus.framework.web.result.PageResult;
import com.nexus.framework.web.result.Result;
import com.nexus.backend.admin.controller.dict.vo.DictPageReqVO;
import com.nexus.backend.admin.controller.dict.vo.DictRespVO;
import com.nexus.backend.admin.controller.dict.vo.DictSaveReqVO;
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
     * 分页查询字典列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:dict:query')")
    public Result<PageResult<DictRespVO>> getPage(@Valid DictPageReqVO pageReqVO) {
        PageResult<DictRespVO> pageResult = dictService.getPage(pageReqVO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID获取字典详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dict:query')")
    public Result<DictRespVO> getById(@PathVariable Long id) {
        DictRespVO dict = dictService.getById(id);
        return Result.success(dict);
    }

    /**
     * 根据字典类型获取字典列表（用于下拉框）
     */
    @GetMapping("/type/{dictType}")
    @PreAuthorize("hasAuthority('system:dict:query')")
    public Result<List<DictRespVO>> getListByType(@PathVariable String dictType) {
        List<DictRespVO> dictList = dictService.getListByType(dictType);
        return Result.success(dictList);
    }

    /**
     * 获取所有字典数据（用于前端缓存）
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('system:dict:query')")
    public Result<List<DictRespVO>> getAllDict() {
        List<DictRespVO> dictList = dictService.getAllDict();
        return Result.success(dictList);
    }

    /**
     * 创建字典
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:dict:create')")
    public Result<Long> create(@Valid @RequestBody DictSaveReqVO saveReqVO) {
        Long id = dictService.create(saveReqVO);
        return Result.success(id);
    }

    /**
     * 更新字典
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dict:update')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DictSaveReqVO saveReqVO) {
        saveReqVO.setId(id);
        dictService.update(saveReqVO);
        return Result.success();
    }

    /**
     * 删除字典
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        dictService.delete(id);
        return Result.success();
    }

    /**
     * 批量删除字典
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        dictService.deleteBatch(ids);
        return Result.success();
    }

}
