import request from '../../utils/request';

// ==================== 类型定义 ====================

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[];
  total: number;
}

/**
 * 租户管理表
 */
export interface Tenant {
  id?: number;
  name?: string;
  code?: string;
  datasourceId?: number;
  menuIds?: string;
  maxUsers?: number;
  usedUsers?: number;
  expireTime?: string;
  status?: number;
  creator?: string;
  dateCreated?: string;
  updater?: string;
  lastUpdated?: string;
  deleted?: number;
}

/**
 * 租户管理表查询参数
 */
export interface TenantQuery {
  pageNum?: number;
  pageSize?: number;
  id?: number;
  name?: string;
  code?: string;
  datasourceId?: number;
  menuIds?: string;
  maxUsers?: number;
  usedUsers?: number;
  expireTime?: string;
  status?: number;
  creator?: string;
  dateCreated?: string;
  updater?: string;
  lastUpdated?: string;
}

/**
 * 租户管理表创建/编辑参数
 */
export interface TenantForm {
  name?: string;
  code?: string;
  datasourceId?: number;
  menuIds?: string;
  maxUsers?: number;
  usedUsers?: number;
  expireTime?: string;
  status?: number;
}

// ==================== API 接口 ====================

/**
 * 租户管理表相关API
 */
export const tenantApi = {
  /**
   * 分页查询租户管理表列表
   */
  getPage: (params: TenantQuery): Promise<PageResult<Tenant>> => {
    return request.get('/system/tenant/page', { params });
  },

  /**
   * 根据ID查询租户管理表
   */
  getById: (id: number): Promise<Tenant> => {
    return request.get(`/system/tenant/${id}`);
  },

  /**
   * 创建租户管理表
   */
  create: (data: TenantForm): Promise<number> => {
    return request.post('/system/tenant', data);
  },

  /**
   * 更新租户管理表
   */
  update: (id: number, data: TenantForm): Promise<void> => {
    return request.put(`/system/tenant/${id}`, data);
  },

  /**
   * 删除租户管理表
   */
  delete: (id: number): Promise<void> => {
    return request.delete(`/system/tenant/${id}`);
  },

  /**
   * 批量删除租户管理表
   */
  deleteBatch: (ids: number[]): Promise<void> => {
    return request.delete(`/system/tenant/batch`, { data: ids });
  },

  /**
   * 导出租户管理表
   */
  exportData: (params: TenantQuery): Promise<Blob> => {
    return request.get('/system/tenant/export', { 
      params, 
      responseType: 'blob' 
    });
  },

  /**
   * 获取数据源列表（用于下拉选择）
   */
  getDataSources: (): Promise<Array<{ id: number; name: string }>> => {
    return request.get('/system/tenant/datasources');
  },

  /**
   * 获取菜单树（用于分配菜单）
   */
  getMenuTree: (): Promise<any[]> => {
    return request.get('/system/menu/tree');
  },
};
