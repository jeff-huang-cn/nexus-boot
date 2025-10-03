import request from '../../utils/request';
import type { Role, RoleForm, RoleAssignMenuForm } from './types';

// 角色模块API基础路径
const API_BASE = '/system/role';

/**
 * 角色管理 API
 */
export const roleApi = {
  /**
   * 创建角色
   */
  create: (data: RoleForm) => {
    return request.post<number>(API_BASE, data);
  },

  /**
   * 更新角色
   */
  update: (id: number, data: RoleForm) => {
    return request.put(`${API_BASE}/${id}`, data);
  },

  /**
   * 删除角色
   */
  delete: (id: number) => {
    return request.delete(`${API_BASE}/${id}`);
  },

  /**
   * 获取角色详情
   */
  getById: (id: number) => {
    return request.get<Role>(`${API_BASE}/${id}`);
  },

  /**
   * 获取角色列表
   */
  getList: () => {
    return request.get<Role[]>(`${API_BASE}/list`);
  },

  /**
   * 分配菜单
   */
  assignMenu: (data: RoleAssignMenuForm) => {
    return request.post(`${API_BASE}/assign-menu`, data);
  },
};