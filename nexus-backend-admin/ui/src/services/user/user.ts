import request from '../../utils/request';
import type { User, UserQuery, UserForm, PageResult } from '../../types/user';

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
    return request.delete(`${API_BASE}/${id}`);
  },

  /**
   * 批量删除用户信息表
   */
  deleteBatch: (ids: number[]): Promise<void> => {
    return request.delete(`${API_BASE}/batch`, { data: { ids } });
  },
};