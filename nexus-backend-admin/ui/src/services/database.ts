import request from '../utils/request';
import type {
  DatabaseTable,
  DatabaseColumn,
  DataSourceConfig,
} from '../types/codegen';

/**
 * 数据库管理相关API
 */
export const databaseApi = {
  /**
   * 查询数据源配置列表
   */
  getDataSourceList: (): Promise<DataSourceConfig[]> => {
    return request.get('/database/datasources');
  },

  /**
   * 查询数据库表列表
   */
  getTableList: (params?: {
    datasourceConfigId?: number;
    tableName?: string;
  }): Promise<DatabaseTable[]> => {
    return request.get('/database/tables', { params });
  },

  /**
   * 查询表字段列表
   */
  getColumnList: (params: {
    datasourceConfigId: number;
    tableName: string;
  }): Promise<DatabaseColumn[]> => {
    const { tableName, ...restParams } = params;
    return request.get(`/database/tables/${tableName}/columns`, { 
      params: restParams 
    });
  },

  /**
   * 查询表信息
   */
  getTableInfo: (params: {
    datasourceConfigId: number;
    tableName: string;
  }): Promise<DatabaseTable> => {
    const { tableName, ...restParams } = params;
    return request.get(`/database/tables/${tableName}`, { 
      params: restParams 
    });
  },
};
