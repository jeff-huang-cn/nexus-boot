/**
 * 菜单管理相关类型定义
 */

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

