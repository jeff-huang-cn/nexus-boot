import request from '../../utils/request';

// ==================== 类型定义 ====================

/**
 * 数据库表信息
 */
export interface DatabaseTable {
  tableName: string;
  tableComment: string;
  dateCreated?: string;
  lastUpdated?: string;
  engine?: string;
  tableCollation?: string;
  dataLength?: number;
  tableRows?: number;
}

/**
 * 数据库字段信息
 */
export interface DatabaseColumn {
  columnName: string;
  dataType: string;
  columnComment: string;
  isNullable: string;
  columnKey: string;
  extra?: string;
  columnDefault?: string;
  ordinalPosition: number;
  characterMaximumLength?: number;
  numericPrecision?: number;
  numericScale?: number;
}

/**
 * 数据源配置
 */
export interface DataSourceConfig {
  id: number;
  name: string;
  url: string;
  username: string;
  password?: string;
  dateCreated?: string;
  lastUpdated?: string;
}

// ==================== API 定义 ====================

// 数据库模块API基础路径
const API_BASE = '/database';

/**
 * 数据库管理相关API
 */
export const databaseApi = {
  /**
   * 查询数据源配置列表
   */
  getDataSourceList: (): Promise<DataSourceConfig[]> => {
    return request.get(`${API_BASE}/datasources`);
  },

  /**
   * 查询数据库表列表
   */
  getTableList: (params?: {
    datasourceConfigId?: number;
    tableName?: string;
  }): Promise<DatabaseTable[]> => {
    return request.get(`${API_BASE}/tables`, { params });
  },

  /**
   * 查询表字段列表
   */
  getColumnList: (params: {
    datasourceConfigId: number;
    tableName: string;
  }): Promise<DatabaseColumn[]> => {
    const { tableName, ...restParams } = params;
    return request.get(`${API_BASE}/tables/${tableName}/columns`, { 
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
    return request.get(`${API_BASE}/tables/${tableName}`, { 
      params: restParams 
    });
  },
};

export default databaseApi;

