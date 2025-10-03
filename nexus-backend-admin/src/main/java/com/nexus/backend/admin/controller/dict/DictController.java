package com.nexus.backend.admin.controller.dict;

import com.nexus.framework.web.result.Result;
import com.nexus.backend.admin.controller.dict.vo.DictRespVO;
import com.nexus.backend.admin.service.dict.DictService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 字典管理接口
 *
 * @author nexus
 */
@RestController
@RequestMapping("/system/dict")
public class DictController {

    @Resource
    private DictService dictService;

    /**
     * 根据字典类型获取字典列表
     */
    @GetMapping("/type/{dictType}")
    public Result<List<DictRespVO>> getListByType(@PathVariable String dictType) {
        List<DictRespVO> dictList = dictService.getListByType(dictType);
        return Result.success(dictList);
    }

    /**
     * 获取所有字典数据
     */
    @GetMapping("/all")
    public Result<List<DictRespVO>> getAllDict() {
        List<DictRespVO> dictList = dictService.getAllDict();
        return Result.success(dictList);
    }

}
