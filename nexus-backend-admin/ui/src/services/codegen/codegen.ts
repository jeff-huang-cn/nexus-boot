import request from '../../utils/request';
import type {
  PageResult,
  CodegenTable,
  CodegenColumn,
  CodegenTableVO,
  ImportTableRequest,
  UpdateTableConfigRequest,
} from '../../types/codegen';

/**
 * 代码生成相关API
 */
export const codegenApi = {
  /**
   * 分页查询代码生成表列表
   */
  getTableList: (params: {
    current?: number;
    size?: number;
    tableName?: string;
    tableComment?: string;
  }): Promise<PageResult<CodegenTable>> => {
    return request.get('/codegen/tables', { params });
  },

  /**
   * 导入数据库表
   */
  importTables: (data: ImportTableRequest): Promise<void> => {
    return request.post('/codegen/import', data);
  },

  /**
   * 更新代码生成表配置
   */
  updateTableConfig: (id: number, data: UpdateTableConfigRequest): Promise<void> => {
    return request.put(`/codegen/tables/${id}`, data);
  },

  /**
   * 删除代码生成表
   */
  deleteTable: (id: number): Promise<void> => {
    return request.delete(`/codegen/tables/${id}`);
  },

  /**
   * 预览生成代码
   */
  previewCode: (id: number): Promise<Record<string, string>> => {
    return request.get(`/codegen/preview/${id}`);
  },

  /**
   * 生成代码并下载
   */
  generateCode: (id: number): Promise<Blob> => {
    return request.post(`/codegen/generate/${id}`, {}, {
      responseType: 'blob',
    }).then(response => response.data);
  },

  /**
   * 同步表结构
   */
  syncTable: (id: number): Promise<void> => {
    return request.put(`/codegen/sync-from-db/${id}`);
  },

  /**
   * 获取表详细配置（包含字段信息）
   */
  getTableDetail: (id: number): Promise<CodegenTable & { columns: CodegenColumn[] }> => {
    return request.get(`/codegen/tables/${id}`);
  },
};
