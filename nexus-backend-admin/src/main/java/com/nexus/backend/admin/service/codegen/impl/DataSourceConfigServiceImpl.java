package com.nexus.backend.admin.service.codegen.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexus.backend.admin.controller.codegen.vo.DataSourcePageReqVO;
import com.nexus.backend.admin.dal.dataobject.codegen.DataSourceConfigDO;
import com.nexus.backend.admin.dal.mapper.codegen.DataSourceConfigMapper;
import com.nexus.backend.admin.service.codegen.DataSourceConfigService;
import com.nexus.framework.web.result.PageResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * 数据源配置服务实现
 *
 * @author nexus
 */
@Slf4j
@Service
public class DataSourceConfigServiceImpl implements DataSourceConfigService {

    @Resource
    private DataSourceConfigMapper dataSourceConfigMapper;

    @Override
    public PageResult<DataSourceConfigDO> getPage(DataSourcePageReqVO pageReqVO) {
        Page<DataSourceConfigDO> page = new Page<>(pageReqVO.getPageNum(), pageReqVO.getPageSize());
        LambdaQueryWrapper<DataSourceConfigDO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(pageReqVO.getName())) {
            wrapper.like(DataSourceConfigDO::getName, pageReqVO.getName());
        }

        wrapper.orderByDesc(DataSourceConfigDO::getDateCreated);

        Page<DataSourceConfigDO> resultPage = dataSourceConfigMapper.selectPage(page, wrapper);
        return new PageResult<>(resultPage.getRecords(), resultPage.getTotal());
    }

    @Override
    public List<DataSourceConfigDO> getList() {
        return dataSourceConfigMapper.selectActiveList();
    }

    @Override
    public DataSourceConfigDO getById(Long id) {
        return dataSourceConfigMapper.selectById(id);
    }

    @Override
    public Long create(DataSourceConfigDO dataSourceConfig) {
        dataSourceConfigMapper.insert(dataSourceConfig);
        return dataSourceConfig.getId();
    }

    @Override
    public void update(DataSourceConfigDO dataSourceConfig) {
        dataSourceConfigMapper.updateById(dataSourceConfig);
    }

    @Override
    public void delete(Long id) {
        dataSourceConfigMapper.deleteById(id);
    }

    @Override
    public boolean testConnection(Long id) {
        DataSourceConfigDO config = dataSourceConfigMapper.selectById(id);
        if (config == null) {
            return false;
        }

        try (Connection conn = DriverManager.getConnection(
                config.getUrl(),
                config.getUsername(),
                config.getPassword())) {
            return conn.isValid(5); // 5秒超时
        } catch (Exception e) {
            log.error("测试数据源连接失败: {}", e.getMessage());
            return false;
        }
    }
}
