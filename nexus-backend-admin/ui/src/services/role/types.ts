/**
 * 角色管理相关类型定义
 */

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

