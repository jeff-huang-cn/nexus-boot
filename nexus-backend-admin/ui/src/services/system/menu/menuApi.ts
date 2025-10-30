import request from '../../../utils/request';

// ==================== 类型定义 ====================

/**
 * 菜单
 */
export interface Menu {
  id?: number;
  name?: string;
  permission?: string;
  type?: number; // 1-目录 2-菜单 3-按钮
  sort?: number;
  parentId?: number;
  path?: string;
  icon?: string;
  component?: string;
  componentName?: string;
  status?: number; // 0-禁用 1-启用
  visible?: number; // 0-隐藏 1-显示
  keepAlive?: number; // 0-不缓存 1-缓存
  alwaysShow?: number; // 0-否 1-是
  dateCreated?: string;
  children?: Menu[];
}

/**
 * 菜单保存参数
 */
export interface MenuForm {
  id?: number;
  name: string;
  permission?: string;
  type: number;
  sort?: number;
  parentId: number;
  path?: string;
  icon?: string;
  component?: string;
  componentName?: string;
  status?: number;
  visible?: number;
  keepAlive?: number;
  alwaysShow?: number;
}

// ==================== API 定义 ====================

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
   * 获取菜单树列表（包含所有类型：目录、菜单、按钮）
   */
  getMenuTree: () => {
    return request.get<Menu[]>(`${API_BASE}/tree`);
  },

  /**
   * 获取完整菜单树（用于菜单管理页面，包含按钮）
   */
  getFullMenuTree: () => {
    return request.get<Menu[]>(`${API_BASE}/tree/full`);
  },

  /**
   * 获取角色的菜单ID列表
   */
  getMenuIdsByRoleId: (roleId: number) => {
    return request.get<number[]>(`${API_BASE}/role/${roleId}`);
  },
};

export default menuApi;

