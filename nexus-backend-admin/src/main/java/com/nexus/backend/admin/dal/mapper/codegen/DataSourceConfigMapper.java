package com.nexus.backend.admin.dal.mapper.codegen;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.backend.admin.dal.entity.codegen.DataSourceConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据源配置 Mapper 接口
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Mapper
public interface DataSourceConfigMapper extends BaseMapper<DataSourceConfigDO> {

    /**
     * 查询所有可用的数据源配置
     *
     * @return 数据源配置列表
     */
    default List<DataSourceConfigDO> selectActiveList() {
        return selectList(new LambdaQueryWrapper<DataSourceConfigDO>()
                .orderByAsc(DataSourceConfigDO::getDateCreated));
    }

    /**
     * 根据名称查询数据源配置
     *
     * @param name 数据源名称
     * @return 数据源配置
     */
    default DataSourceConfigDO selectByName(String name) {
        return selectOne(new LambdaQueryWrapper<DataSourceConfigDO>()
                .eq(DataSourceConfigDO::getName, name)
                .last("LIMIT 1"));
    }

}
