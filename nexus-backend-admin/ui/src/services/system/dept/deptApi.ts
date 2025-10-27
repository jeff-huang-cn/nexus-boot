import request from '../../../utils/request';

// ==================== 类型定义 ====================

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[];
  total: number;
}

/**
 * 部门管理表
 */
export interface Dept {
  id?: number;
  name?: string;
  code?: string;
  parentId?: number;
  sort?: number;
  leaderUserId?: number;
  phone?: string;
  email?: string;
  status?: number;
  tenantId?: number;
  creator?: string;
  dateCreated?: string;
  updater?: string;
  lastUpdated?: string;
  deleted?: number;
}

/**
 * 部门管理表查询参数
 */
export interface DeptQuery {
  pageNum?: number;
  pageSize?: number;
  name?: string;
  code?: string;
  status?: number;
}

/**
 * 部门管理表创建/编辑参数
 */
export interface DeptForm {
  name?: string;
  code?: string;
  parentId?: number;
  sort?: number;
  leaderUserId?: number;
  phone?: string;
  email?: string;
  status?: number;
}

// ==================== API 接口 ====================

/**
 * 部门管理表相关API
 */
export const deptApi = {
  /**
   * 分页查询部门管理表列表
   */
  getPage: (params: DeptQuery): Promise<PageResult<Dept>> => {
    return request.get('/system/dept/page', { params });
  },

  /**
   * 根据ID查询部门管理表
   */
  getById: (id: number): Promise<Dept> => {
    return request.get(`/system/dept/${id}`);
  },

  /**
   * 创建部门管理表
   */
  create: (data: DeptForm): Promise<number> => {
    return request.post('/system/dept', data);
  },

  /**
   * 更新部门管理表
   */
  update: (id: number, data: DeptForm): Promise<void> => {
    return request.put(`/system/dept/${id}`, data);
  },

  /**
   * 删除部门管理表
   */
  delete: (id: number): Promise<void> => {
    return request.delete(`/system/dept/${id}`);
  },

  /**
   * 批量删除部门管理表
   */
  deleteBatch: (ids: number[]): Promise<void> => {
    return request.delete(`/system/dept/batch`, { data: ids });
  },

  /**
   * 导出部门管理表
   */
  exportData: (params: DeptQuery): Promise<Blob> => {
    return request.get('/system/dept/export', { 
      params, 
      responseType: 'blob' 
    });
  },
};
