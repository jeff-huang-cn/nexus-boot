package com.nexus.backend.admin.service.codegen;

import com.nexus.backend.admin.controller.codegen.vo.DataSourcePageReqVO;
import com.nexus.backend.admin.dal.dataobject.codegen.DataSourceConfigDO;
import com.nexus.framework.web.result.PageResult;

import java.util.List;

/**
 * 数据源配置服务接口
 *
 * @author nexus
 */
public interface DataSourceConfigService {

    /**
     * 分页查询数据源配置
     *
     * @param pageReqVO 分页参数
     * @return 分页结果
     */
    PageResult<DataSourceConfigDO> getPage(DataSourcePageReqVO pageReqVO);

    /**
     * 查询所有数据源配置
     *
     * @return 数据源配置列表
     */
    List<DataSourceConfigDO> getList();

    /**
     * 根据ID查询数据源配置
     *
     * @param id 数据源ID
     * @return 数据源配置
     */
    DataSourceConfigDO getById(Long id);

    /**
     * 创建数据源配置
     *
     * @param dataSourceConfig 数据源配置
     * @return 数据源ID
     */
    Long create(DataSourceConfigDO dataSourceConfig);

    /**
     * 更新数据源配置
     *
     * @param dataSourceConfig 数据源配置
     */
    void update(DataSourceConfigDO dataSourceConfig);

    /**
     * 删除数据源配置
     *
     * @param id 数据源ID
     */
    void delete(Long id);

    /**
     * 测试数据源连接
     *
     * @param id 数据源ID
     * @return 是否连接成功
     */
    boolean testConnection(Long id);
}
