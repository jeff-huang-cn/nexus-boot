import request from '../../../utils/request';

// ==================== 类型定义 ====================

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
 * 字典类型分组
 */
export interface DictTypeGroup {
  dictType: string;
  itemCount: number;
  status?: number;
  sampleLabels?: string;
}

/**
 * 字典项保存
 */
export interface DictItemSave {
  id?: number;
  dictLabel: string;
  dictValue: string;
  sort?: number;
  status?: number;
  colorType?: string;
  cssClass?: string;
  remark?: string;
}

/**
 * 字典类型批量保存
 */
export interface DictTypeBatchSave {
  dictType: string;
  items: DictItemSave[];
}

// ==================== API 接口 ====================

/**
 * 数据字典相关API
 */
export const dictApi = {
  /**
   * 根据字典类型获取字典列表（用于编辑时加载）
   */
  getListByType: (dictType: string): Promise<Dict[]> => {
    return request.get(`/system/dict/type/${dictType}`);
  },

  /**
   * 获取字典类型分组列表
   */
  getDictTypeGroups: (): Promise<DictTypeGroup[]> => {
    return request.get('/system/dict/type-groups');
  },

  /**
   * 批量保存字典类型下的所有字典项
   */
  batchSaveDictType: (data: DictTypeBatchSave): Promise<void> => {
    return request.post('/system/dict/type/batch-save', data);
  },

  /**
   * 删除字典类型及其所有字典项
   */
  deleteDictType: (dictType: string): Promise<void> => {
    return request.delete(`/system/dict/type/${dictType}`);
  },
};