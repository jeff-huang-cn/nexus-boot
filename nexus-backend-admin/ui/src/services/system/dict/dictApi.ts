import request from '../../../utils/request';

// ==================== 类型定义 ====================

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[];
  total: number;
}

/**
 * 数据字典
 */
export interface Dict {
  id?: number;
  dictType: string;
  dictLabel: string;
  dictValue: string;
  sort?: number;
  status?: number;
  colorType?: string;
  cssClass?: string;
  remark?: string;
  dateCreated?: string;
  lastUpdated?: string;
}

/**
 * 数据字典查询参数
 */
export interface DictQuery {
  pageNum?: number;
  pageSize?: number;
  dictType?: string;
  dictLabel?: string;
  dictValue?: string;
  status?: number;
}

/**
 * 数据字典创建/编辑参数
 */
export interface DictForm {
  dictType: string;
  dictLabel: string;
  dictValue: string;
  sort?: number;
  status?: number;
  colorType?: string;
  cssClass?: string;
  remark?: string;
}

// ==================== API 接口 ====================

/**
 * 数据字典相关API
 */
export const dictApi = {
  /**
   * 分页查询数据字典列表
   */
  getPage: (params: DictQuery): Promise<PageResult<Dict>> => {
    return request.get('/system/dict/page', { params });
  },

  /**
   * 根据ID查询数据字典
   */
  getById: (id: number): Promise<Dict> => {
    return request.get(`/system/dict/${id}`);
  },

  /**
   * 根据字典类型获取字典列表
   */
  getListByType: (dictType: string): Promise<Dict[]> => {
    return request.get(`/system/dict/type/${dictType}`);
  },

  /**
   * 获取所有字典数据（用于前端缓存）
   */
  getAllDict: (): Promise<Dict[]> => {
    return request.get('/system/dict/all');
  },

  /**
   * 创建数据字典
   */
  create: (data: DictForm): Promise<number> => {
    return request.post('/system/dict', data);
  },

  /**
   * 更新数据字典
   */
  update: (id: number, data: DictForm): Promise<void> => {
    return request.put(`/system/dict/${id}`, data);
  },

  /**
   * 删除数据字典
   */
  delete: (id: number): Promise<void> => {
    return request.delete(`/system/dict/${id}`);
  },

  /**
   * 批量删除数据字典
   */
  deleteBatch: (ids: number[]): Promise<void> => {
    return request.delete(`/system/dict/batch`, { data: ids });
  },
};