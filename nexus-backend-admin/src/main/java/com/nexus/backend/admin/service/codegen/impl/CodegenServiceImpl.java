package com.nexus.backend.admin.service.codegen.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexus.framework.web.exception.BusinessException;
import com.nexus.backend.admin.utils.CodegenUtils;
import com.nexus.backend.admin.controller.codegen.vo.DatabaseColumnVO;
import com.nexus.backend.admin.controller.codegen.vo.DatabaseTableDVO;
import com.nexus.backend.admin.dal.dataobject.codegen.CodegenColumnDO;
import com.nexus.backend.admin.dal.dataobject.codegen.CodegenTableDO;
import com.nexus.backend.admin.dal.mapper.codegen.CodegenColumnMapper;
import com.nexus.backend.admin.dal.mapper.codegen.CodegenTableMapper;
import com.nexus.backend.admin.service.codegen.CodegenService;
import com.nexus.backend.admin.service.codegen.DatabaseTableService;
import com.nexus.backend.admin.service.codegen.engine.VelocityTemplateEngine;
import com.nexus.backend.admin.service.codegen.engine.TemplateContext;
import com.nexus.framework.web.result.PageResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
    public PageResult<CodegenTableDO> getTableList(Long current, Long size, String tableName, String tableComment) {
        // 构建查询条件
        LambdaQueryWrapper<CodegenTableDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(tableName)) {
            wrapper.like(CodegenTableDO::getTableName, tableName);
        }
        if (StringUtils.hasText(tableComment)) {
            wrapper.like(CodegenTableDO::getTableComment, tableComment);
        }
        wrapper.orderByDesc(CodegenTableDO::getDateCreated);

        // 分页查询
        IPage<CodegenTableDO> page = new Page<>(current, size);
        IPage<CodegenTableDO> result = codegenTableMapper.selectPage(page, wrapper);

        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public CodegenTableDO getTableById(Long id) {
        CodegenTableDO table = codegenTableMapper.selectById(id);
        if (table == null) {
            throw new BusinessException("代码生成表不存在");
        }
        return table;
    }

    @Override
    public List<CodegenColumnDO> getColumnsByTableId(Long tableId) {
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
            CodegenTableDO existingTable = codegenTableMapper.selectByTableName(tableName, datasourceConfigId);
            if (existingTable != null) {
                log.warn("表 {} 已经导入，跳过", tableName);
                continue;
            }

            // 获取数据库表信息
            DatabaseTableDVO databaseTable = databaseTableService.selectTableByName(datasourceConfigId, tableName);
            if (databaseTable == null) {
                log.warn("表 {} 不存在，跳过", tableName);
                continue;
            }

            // 获取表字段信息
            List<DatabaseColumnVO> databaseColumns = databaseTableService.selectColumnList(datasourceConfigId,
                    tableName);
            if (CollectionUtils.isEmpty(databaseColumns)) {
                log.warn("表 {} 没有字段，跳过", tableName);
                continue;
            }

            // 初始化代码生成表配置
            CodegenTableDO codegenTableDO = CodegenUtils.initTable(databaseTable, datasourceConfigId);
            codegenTableMapper.insert(codegenTableDO);
            tableIds.add(codegenTableDO.getId());

            // 初始化代码生成字段配置
            List<CodegenColumnDO> codegenColumnDOS = databaseColumns.stream()
                    .map(column -> CodegenUtils.initColumn(column, codegenTableDO.getId()))
                    .collect(Collectors.toList());

            for (CodegenColumnDO column : codegenColumnDOS) {
                codegenColumnMapper.insert(column);
            }

            log.info("成功导入表：{}", tableName);
        }

        return tableIds;
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void updateTableConfig(CodegenTableDO table, List<CodegenColumnDO> columns) {
        // 更新表配置
        table.setLastUpdated(LocalDateTime.now());
        table.setUpdater("admin"); // TODO: 从上下文获取当前用户
        codegenTableMapper.updateById(table);

        // 更新字段配置
        if (!CollectionUtils.isEmpty(columns)) {
            for (CodegenColumnDO column : columns) {
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
        CodegenTableDO table = getTableById(id);

        // 删除字段配置
        LambdaQueryWrapper<CodegenColumnDO> columnWrapper = new LambdaQueryWrapper<>();
        columnWrapper.eq(CodegenColumnDO::getTableId, id);
        codegenColumnMapper.delete(columnWrapper);

        // 删除表配置
        codegenTableMapper.deleteById(id);

        log.info("删除表配置成功：{}", table.getTableName());
    }

    @Override
    public Map<String, String> previewCode(Long tableId) {
        CodegenTableDO table = getTableById(tableId);
        List<CodegenColumnDO> columns = getColumnsByTableId(tableId);

        Map<String, String> result = new HashMap<>();
        Map<String, Object> context = TemplateContext.build(table, columns);

        try {
            String businessName = table.getBusinessName();
            String className = table.getClassName();

            // 后端代码 - Controller 层
            result.put("backend/controller/" + businessName + "/" + className + "Controller.java",
                    velocityTemplateEngine.render("templates/java/controller/controller.vm", context));
            result.put("backend/controller/" + businessName + "/vo/" + className + "PageReqVO.java",
                    velocityTemplateEngine.render("templates/java/controller/vo/PageReqVO.vm", context));
            result.put("backend/controller/" + businessName + "/vo/" + className + "SaveReqVO.java",
                    velocityTemplateEngine.render("templates/java/controller/vo/SaveReqVO.vm", context));
            result.put("backend/controller/" + businessName + "/vo/" + className + "RespVO.java",
                    velocityTemplateEngine.render("templates/java/controller/vo/RespVO.vm", context));

            // 后端代码 - Service 层
            result.put("backend/service/" + businessName + "/" + className + "Service.java",
                    velocityTemplateEngine.render("templates/java/service/service.vm", context));
            result.put("backend/service/" + businessName + "/impl/" + className + "ServiceImpl.java",
                    velocityTemplateEngine.render("templates/java/service/serviceImpl.vm", context));

            // 后端代码 - DAL 层
            result.put("backend/dal/dataobject/" + businessName + "/" + className + "DO.java",
                    velocityTemplateEngine.render("templates/java/dal/entity/do.vm", context));
            result.put("backend/dal/mapper/" + businessName + "/" + className + "Mapper.java",
                    velocityTemplateEngine.render("templates/java/dal/mapper/mapper.vm", context));

            // 后端代码 - 配置文件 (Mapper XML 放在 resources/mapper 目录下)
            result.put("backend/resources/mapper/" + businessName + "/" + className + "Mapper.xml",
                    velocityTemplateEngine.render("templates/java/dal/mapper/mapper.xml.vm", context));

            // 前端代码 - React
            if (table.getFrontType() == 30) {
                // 页面文件 (业务组件直接放在页面目录下)
                result.put("frontend/pages/" + businessName + "/" + className + "List.tsx",
                        velocityTemplateEngine.render("templates/react/List.tsx.vm", context));
                result.put("frontend/pages/" + businessName + "/" + className + "Form.tsx",
                        velocityTemplateEngine.render("templates/react/Form.tsx.vm", context));

                // API 服务
                result.put("frontend/services/" + businessName + ".ts",
                        velocityTemplateEngine.render("templates/react/api.ts.vm", context));

                // 类型定义
                result.put("frontend/types/" + businessName + ".ts",
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
            CodegenTableDO table = getTableById(tableId);
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
