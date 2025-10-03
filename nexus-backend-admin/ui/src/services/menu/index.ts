import request from '../../utils/request';
import type { Menu, MenuForm } from './types';

// 菜单模块API基础路径
const API_BASE = '/system/menu';

/**
 * 菜单管理 API
 */
export const menuApi = {
  /**
   * 创建菜单
   */
  create: (data: MenuForm) => {
    return request.post<number>(API_BASE, data);
  },

  /**
   * 更新菜单
   */
  update: (id: number, data: MenuForm) => {
    return request.put(`${API_BASE}/${id}`, data);
  },

  /**
   * 删除菜单
   */
  delete: (id: number) => {
    return request.delete(`${API_BASE}/${id}`);
  },

  /**
   * 获取菜单详情
   */
  getById: (id: number) => {
    return request.get<Menu>(`${API_BASE}/${id}`);
  },

  /**
   * 获取菜单树列表
   */
  getMenuTree: () => {
    return request.get<Menu[]>(`${API_BASE}/tree`);
  },

  /**
   * 获取角色的菜单ID列表
   */
  getMenuIdsByRoleId: (roleId: number) => {
    return request.get<number[]>(`${API_BASE}/role/${roleId}`);
  },
};