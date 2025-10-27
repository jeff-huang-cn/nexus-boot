import request from '../../../utils/request';

// ==================== 类型定义 ====================

/**
 * 角色
 */
export interface Role {
  id?: number;
  name?: string;
  code?: string;
  sort?: number;
  status?: number; // 0-禁用 1-启用
  type?: number; // 1-系统内置 2-自定义
  remark?: string;
  dateCreated?: string;
}

/**
 * 角色保存参数
 */
export interface RoleForm {
  id?: number;
  name: string;
  code: string;
  sort?: number;
  status?: number;
  remark?: string;
}

/**
 * 角色分配菜单参数
 */
export interface RoleAssignMenuForm {
  roleId: number;
  menuIds: number[];
}

// ==================== API 定义 ====================

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

  /**
   * 批量删除角色
   */
  deleteBatch: (ids: number[]) => {
    return request.delete(`${API_BASE}/batch`, { data: ids });
  },

  /**
   * 导出角色
   */
  exportData: (params?: any) => {
    return request.get(`${API_BASE}/export`, { 
      params, 
      responseType: 'blob' 
    });
  },
};

export default roleApi;

