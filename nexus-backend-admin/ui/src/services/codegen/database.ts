import request from '../../utils/request';

/**
 * 数据源配置类型
 */
export interface DataSourceConfig {
  id?: number;
  name: string;
  url: string;
  username: string;
  password?: string;
  hasPassword?: boolean;
  dateCreated?: string;
  lastUpdated?: string;
}

/**
 * 数据源配置创建请求
 */
export interface DataSourceConfigCreateReq {
  name: string;
  url: string;
  username: string;
  password: string;
}

/**
 * 数据源配置更新请求
 */
export interface DataSourceConfigUpdateReq {
  name: string;
  url: string;
  username: string;
  password?: string;
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[];
  total: number;
}

/**
 * 数据源管理相关API
 */
export const datasourceApi = {
  /**
   * 分页查询数据源配置
   */
  getPage: (params: {
    pageNum?: number;
    pageSize?: number;
    name?: string;
  }): Promise<PageResult<DataSourceConfig>> => {
    return request.get('/codegen/datasource/page', { params });
  },

  /**
   * 查询所有数据源配置
   */
  getList: (): Promise<DataSourceConfig[]> => {
    return request.get('/codegen/datasource/list');
  },

  /**
   * 根据ID查询数据源配置
   */
  getById: (id: number): Promise<DataSourceConfig> => {
    return request.get(`/codegen/datasource/${id}`);
  },

  /**
   * 创建数据源配置
   */
  create: (data: DataSourceConfigCreateReq): Promise<number> => {
    return request.post('/codegen/datasource', data);
  },

  /**
   * 更新数据源配置
   */
  update: (id: number, data: DataSourceConfigUpdateReq): Promise<void> => {
    return request.put(`/codegen/datasource/${id}`, data);
  },

  /**
   * 删除数据源配置
   */
  delete: (id: number): Promise<void> => {
    return request.delete(`/codegen/datasource/${id}`);
  },

  /**
   * 测试数据源连接
   */
  testConnection: (id: number): Promise<boolean> => {
    return request.post(`/codegen/datasource/${id}/test`);
  },
};

