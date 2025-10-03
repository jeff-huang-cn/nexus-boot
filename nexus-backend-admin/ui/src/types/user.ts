/**
 * 用户信息表相关类型定义
 */

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
