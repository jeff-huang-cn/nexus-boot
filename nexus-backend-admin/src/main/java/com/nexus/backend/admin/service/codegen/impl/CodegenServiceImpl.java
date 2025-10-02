package com.nexus.backend.admin.service.codegen.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexus.backend.admin.common.exception.BusinessException;
import com.nexus.backend.admin.common.result.PageResult;
import com.nexus.backend.admin.common.utils.CodegenUtils;
import com.nexus.backend.admin.controller.codegen.dto.DatabaseColumnDTO;
import com.nexus.backend.admin.controller.codegen.dto.DatabaseTableDTO;
import com.nexus.backend.admin.entity.codegen.CodegenColumn;
import com.nexus.backend.admin.entity.codegen.CodegenTable;
import com.nexus.backend.admin.mapper.codegen.CodegenColumnMapper;
import com.nexus.backend.admin.mapper.codegen.CodegenTableMapper;
import com.nexus.backend.admin.service.codegen.CodegenService;
import com.nexus.backend.admin.service.codegen.DatabaseTableService;
import com.nexus.backend.admin.service.codegen.engine.VelocityTemplateEngine;
import com.nexus.backend.admin.service.codegen.engine.TemplateContext;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成服务实现
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Slf4j
@Service
public class CodegenServiceImpl implements CodegenService {
    @Resource
    private CodegenTableMapper codegenTableMapper;
    @Resource
    private CodegenColumnMapper codegenColumnMapper;
    @Resource
    private DatabaseTableService databaseTableService;
    @Resource
    private VelocityTemplateEngine velocityTemplateEngine;

    @Override
    public PageResult<CodegenTable> getTableList(Long current, Long size, String tableName, String tableComment) {
        // 构建查询条件
        LambdaQueryWrapper<CodegenTable> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(tableName)) {
            wrapper.like(CodegenTable::getTableName, tableName);
        }
        if (StringUtils.hasText(tableComment)) {
            wrapper.like(CodegenTable::getTableComment, tableComment);
        }
        wrapper.orderByDesc(CodegenTable::getDateCreated);

        // 分页查询
        IPage<CodegenTable> page = new Page<>(current, size);
        IPage<CodegenTable> result = codegenTableMapper.selectPage(page, wrapper);

        return PageResult.of(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize());
    }

    @Override
    public CodegenTable getTableById(Long id) {
        CodegenTable table = codegenTableMapper.selectById(id);
        if (table == null) {
            throw new BusinessException("代码生成表不存在");
        }
        return table;
    }

    @Override
    public List<CodegenColumn> getColumnsByTableId(Long tableId) {
        return codegenColumnMapper.selectByTableId(tableId);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public List<Long> importTables(Long datasourceConfigId, List<String> tableNames) {
        if (CollectionUtils.isEmpty(tableNames)) {
            throw new BusinessException("请选择要导入的表");
        }

        List<Long> tableIds = new ArrayList<>();

        for (String tableName : tableNames) {
            // 检查表是否已经导入
            CodegenTable existingTable = codegenTableMapper.selectByTableName(tableName, datasourceConfigId);
            if (existingTable != null) {
                log.warn("表 {} 已经导入，跳过", tableName);
                continue;
            }

            // 获取数据库表信息
            DatabaseTableDTO databaseTable = databaseTableService.selectTableByName(datasourceConfigId, tableName);
            if (databaseTable == null) {
                log.warn("表 {} 不存在，跳过", tableName);
                continue;
            }

            // 获取表字段信息
            List<DatabaseColumnDTO> databaseColumns = databaseTableService.selectColumnList(datasourceConfigId,
                    tableName);
            if (CollectionUtils.isEmpty(databaseColumns)) {
                log.warn("表 {} 没有字段，跳过", tableName);
                continue;
            }

            // 初始化代码生成表配置
            CodegenTable codegenTable = CodegenUtils.initTable(databaseTable, datasourceConfigId);
            codegenTableMapper.insert(codegenTable);
            tableIds.add(codegenTable.getId());

            // 初始化代码生成字段配置
            List<CodegenColumn> codegenColumns = databaseColumns.stream()
                    .map(column -> CodegenUtils.initColumn(column, codegenTable.getId()))
                    .collect(Collectors.toList());

            for (CodegenColumn column : codegenColumns) {
                codegenColumnMapper.insert(column);
            }

            log.info("成功导入表：{}", tableName);
        }

        return tableIds;
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void updateTableConfig(CodegenTable table, List<CodegenColumn> columns) {
        // 更新表配置
        table.setLastUpdated(LocalDateTime.now());
        table.setUpdater("admin"); // TODO: 从上下文获取当前用户
        codegenTableMapper.updateById(table);

        // 更新字段配置
        if (!CollectionUtils.isEmpty(columns)) {
            for (CodegenColumn column : columns) {
                column.setLastUpdated(LocalDateTime.now());
                column.setUpdater("admin"); // TODO: 从上下文获取当前用户
                codegenColumnMapper.updateById(column);
            }
        }

        log.info("更新表配置成功：{}", table.getTableName());
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void deleteTable(Long id) {
        CodegenTable table = getTableById(id);

        // 删除字段配置
        LambdaQueryWrapper<CodegenColumn> columnWrapper = new LambdaQueryWrapper<>();
        columnWrapper.eq(CodegenColumn::getTableId, id);
        codegenColumnMapper.delete(columnWrapper);

        // 删除表配置
        codegenTableMapper.deleteById(id);

        log.info("删除表配置成功：{}", table.getTableName());
    }

    @Override
    public Map<String, String> previewCode(Long tableId) {
        CodegenTable table = getTableById(tableId);
        List<CodegenColumn> columns = getColumnsByTableId(tableId);

        Map<String, String> result = new HashMap<>();
        Map<String, Object> context = TemplateContext.build(table, columns);

        try {
            // Java后端代码
            result.put(table.getClassName() + ".java",
                    velocityTemplateEngine.render("templates/java/entity.vm", context));
            result.put(table.getClassName() + "Mapper.java",
                    velocityTemplateEngine.render("templates/java/mapper.vm", context));
            result.put(table.getClassName() + "Service.java",
                    velocityTemplateEngine.render("templates/java/service.vm", context));
            result.put(table.getClassName() + "ServiceImpl.java",
                    velocityTemplateEngine.render("templates/java/serviceImpl.vm", context));
            result.put(table.getClassName() + "Controller.java",
                    velocityTemplateEngine.render("templates/java/controller.vm", context));

            // React前端代码
            if (table.getFrontType() == 30) { // React
                result.put(table.getClassName() + "List.tsx",
                        velocityTemplateEngine.render("templates/react/List.tsx.vm", context));
                result.put(table.getClassName() + "Form.tsx",
                        velocityTemplateEngine.render("templates/react/Form.tsx.vm", context));
                result.put(table.getBusinessName() + "Api.ts",
                        velocityTemplateEngine.render("templates/react/api.ts.vm", context));
                result.put(table.getBusinessName() + "Types.ts",
                        velocityTemplateEngine.render("templates/react/types.ts.vm", context));
            }
        } catch (Exception e) {
            log.error("模板渲染失败", e);
            throw new BusinessException("代码生成失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    public byte[] generateCode(Long tableId) {
        Map<String, String> codeMap = previewCode(tableId);
        return createZipFile(codeMap);
    }

    @Override
    public byte[] batchGenerateCode(List<Long> tableIds) {
        Map<String, String> allCodeMap = new HashMap<>();

        for (Long tableId : tableIds) {
            CodegenTable table = getTableById(tableId);
            Map<String, String> codeMap = previewCode(tableId);

            // 为每个表的代码添加目录前缀
            String prefix = table.getBusinessName() + "/";
            codeMap.forEach((fileName, content) -> {
                allCodeMap.put(prefix + fileName, content);
            });
        }

        return createZipFile(allCodeMap);
    }

    /**
     * 创建ZIP文件
     */
    private byte[] createZipFile(Map<String, String> codeMap) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Map.Entry<String, String> entry : codeMap.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue().getBytes("UTF-8"));
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("生成代码文件失败：" + e.getMessage());
        }
    }

}
