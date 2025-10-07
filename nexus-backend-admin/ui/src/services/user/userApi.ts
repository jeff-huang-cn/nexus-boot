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
 * 用户信息表
 */
export interface User {
  id?: number;
  username?: string;
  password?: string;
  nickname?: string;
  remark?: string;
  deptId?: number;
  postIds?: string;
  email?: string;
  mobile?: string;
  sex?: number;
  avatar?: string;
  status?: number;
  loginIp?: string;
  loginDate?: string;
  creator?: string;
  dateCreated?: string;
  updater?: string;
  lastUpdated?: string;
  deleted?: boolean;
  tenantId?: number;
}

/**
 * 用户信息表查询参数
 */
export interface UserQuery {
  pageNum?: number;
  pageSize?: number;
  id?: number;
  username?: string;
  nickname?: string;
  remark?: string;
  deptId?: number;
  postIds?: string;
  email?: string;
  mobile?: string;
  sex?: number;
  avatar?: string;
  status?: number;
  loginIp?: string;
  loginDate?: string;
  creator?: string;
  dateCreated?: string;
  updater?: string;
  lastUpdated?: string;
  tenantId?: number;
}

/**
 * 用户信息表创建/编辑参数
 */
export interface UserForm {
  username?: string;
  password?: string;
  nickname?: string;
  remark?: string;
  deptId?: number;
  postIds?: string;
  email?: string;
  mobile?: string;
  sex?: number;
  avatar?: string;
  status?: number;
  loginIp?: string;
  loginDate?: string;
}

// ==================== API 定义 ====================

// 用户模块API基础路径
const API_BASE = '/system/user';

/**
 * 用户信息表相关API
 */
export const userApi = {
  /**
   * 分页查询用户信息表列表
   */
  getPage: (params: UserQuery): Promise<PageResult<User>> => {
    return request.get(`${API_BASE}/page`, { params });
  },

  /**
   * 根据ID查询用户信息表
   */
  getById: (id: number): Promise<User> => {
    return request.get(`${API_BASE}/${id}`);
  },

  /**
   * 创建用户信息表
   */
  create: (data: UserForm): Promise<number> => {
    return request.post(API_BASE, data);
  },

  /**
   * 更新用户信息表
   */
  update: (id: number, data: UserForm): Promise<void> => {
    return request.put(`${API_BASE}/${id}`, data);
  },

  /**
   * 删除用户信息表
   */
  delete: (id: number): Promise<void> => {
    return request.delete(`${API_BASE}/delete/${id}`);
  },

  /**
   * 批量删除用户信息表
   */
  deleteBatch: (ids: number[]): Promise<void> => {
    return request.delete(`${API_BASE}/delete-batch`, { data: { ids } });
  },
};

export default userApi;

