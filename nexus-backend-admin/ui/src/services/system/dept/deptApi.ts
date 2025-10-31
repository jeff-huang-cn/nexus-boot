import request from '@/utils/request';

/**
 * 部门管理表 API（树表）
 */

// ==================== 类型定义 ====================

export interface PageResult<T> {
  list: T[];
  total: number;
}

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
  children?: Dept[];
}

export interface DeptQuery {
  name?: string;
  code?: string;
  status?: number;
}

export interface DeptForm {
  id?: number;
  name?: string;
  code?: string;
  parentId?: number;
  sort?: number;
  leaderUserId?: number;
  phone?: string;
  email?: string;
  status?: number;
}

// ==================== API 方法 ====================

/**
 * 获取部门管理表列表（树表不分页）
 */
export const getList = (params: DeptQuery) => {
  return request.get<Dept[]>('/system/dept/list', { params });
};

/**
 * 获取部门管理表详情
 */
export const getById = (id: number) => {
  return request.get<Dept>(`/system/dept/\${id}`);
};

/**
 * 创建部门管理表
 */
export const create = (data: DeptForm) => {
  return request.post<number>('/system/dept', data);
};

/**
 * 更新部门管理表
 */
export const update = (data: DeptForm) => {
  return request.put<void>(`/system/dept/\${data.id}`, data);
};

/**
 * 删除部门管理表
 */
export const deleteDept = (id: number) => {
  return request.delete<void>(`/system/dept/\${id}`);
};

export const deptApi = {
  getList,
  getById,
  create,
  update,
  delete: deleteDept,
};
