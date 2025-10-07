package com.nexus.backend.admin.controller.codegen;

import com.nexus.backend.admin.controller.codegen.vo.ImportTableVO;
import com.nexus.backend.admin.controller.codegen.vo.CodegenTableVO;
import com.nexus.backend.admin.controller.codegen.vo.UpdateTableConfigVo;
import com.nexus.backend.admin.dal.dataobject.codegen.CodegenColumnDO;
import com.nexus.backend.admin.dal.dataobject.codegen.CodegenTableDO;
import com.nexus.backend.admin.service.codegen.CodegenService;
import com.nexus.framework.web.result.PageResult;
import com.nexus.framework.web.result.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 代码生成控制器
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/codegen")
@Validated
public class CodegenController {

    @Resource
    private CodegenService codegenService;

    /**
     * 分页查询代码生成表列表
     *
     * @param current      当前页
     * @param size         每页条数
     * @param tableName    表名
     * @param tableComment 表注释
     * @return 分页结果
     */
    @GetMapping("/tables")
    @PreAuthorize("hasAuthority('codegen:table:query')")
    public Result<PageResult<CodegenTableDO>> getTableList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String tableComment) {

        PageResult<CodegenTableDO> result = codegenService.getTableList(current, size, tableName, tableComment);
        return Result.success(result);
    }

    /**
     * 根据ID查询代码生成表配置
     *
     * @param id 表ID
     * @return 表配置
     */
    @GetMapping("/tables/{id}")
    @PreAuthorize("@ss.hasPermission('codegen:table:query')")
    public Result<CodegenTableVO> getTableConfig(@PathVariable @NotNull Long id) {
        CodegenTableDO table = codegenService.getTableById(id);
        List<CodegenColumnDO> columns = codegenService.getColumnsByTableId(id);

        CodegenTableVO vo = new CodegenTableVO();
        BeanUtils.copyProperties(table, vo);
        vo.setColumns(columns);

        return Result.success(vo);
    }

    /**
     * 导入数据库表
     *
     * @param dto 导入请求
     * @return 导入的表ID列表
     */
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('codegen:table:import')")
    public Result<List<Long>> importTables(@Valid @RequestBody ImportTableVO dto) {
        List<Long> tableIds = codegenService.importTables(dto.getDatasourceConfigId(), dto.getTableNames());
        return Result.success(tableIds);
    }

    /**
     * 更新代码生成表配置
     *
     * @param id  表ID
     * @param dto 更新请求
     * @return 成功结果
     */
    @PutMapping("/tables/{id}")
    @PreAuthorize("hasAuthority('codegen:table:update')")
    public Result<Void> updateTableConfig(@PathVariable @NotNull Long id,
            @Valid @RequestBody UpdateTableConfigVo dto) {
        // 确保ID一致
        dto.getTable().setId(id);

        codegenService.updateTableConfig(dto.getTable(), dto.getColumns());
        return Result.success();
    }

    /**
     * 删除代码生成表
     *
     * @param id 表ID
     * @return 成功结果
     */
    @DeleteMapping("/tables/{id}")
    @PreAuthorize("hasAuthority('codegen:table:delete')")
    public Result<Void> deleteTable(@PathVariable @NotNull Long id) {
        codegenService.deleteTable(id);
        return Result.success();
    }

    /**
     * 批量删除代码生成表
     *
     * @param ids 表ID列表
     * @return 成功结果
     */
    @DeleteMapping("/tables")
    @PreAuthorize("hasAuthority('codegen:table:delete')")
    public Result<Void> deleteTables(@RequestBody @NotEmpty List<Long> ids) {
        for (Long id : ids) {
            codegenService.deleteTable(id);
        }
        return Result.success();
    }

    /**
     * 预览生成代码
     *
     * @param id 表ID
     * @return 预览代码
     */
    @GetMapping("/preview/{id}")
    @PreAuthorize("@ss.hasPermission('codegen:table:preview')")
    public Result<Map<String, String>> previewCode(@PathVariable @NotNull Long id) {
        Map<String, String> codeMap = codegenService.previewCode(id);
        return Result.success(codeMap);
    }

    /**
     * 生成代码并下载
     *
     * @param id 表ID
     * @return 代码文件
     */
    @PostMapping("/generate/{id}")
    @PreAuthorize("hasAuthority('codegen:table:generate')")
    public ResponseEntity<byte[]> generateCode(@PathVariable @NotNull Long id) {
        CodegenTableDO table = codegenService.getTableById(id);
        byte[] data = codegenService.generateCode(id);

        String fileName = table.getClassName() + "_code.zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    /**
     * 批量生成代码并下载
     *
     * @param ids 表ID列表
     * @return 代码文件
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('codegen:table:generate')")
    public ResponseEntity<byte[]> batchGenerateCode(@RequestBody @NotEmpty List<Long> ids) {
        byte[] data = codegenService.batchGenerateCode(ids);
        String fileName = "batch_code_" + System.currentTimeMillis() + ".zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

}
