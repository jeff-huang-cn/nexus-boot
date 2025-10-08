import request from '../../utils/request';

// ==================== 类型定义 ====================

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
}

/**
 * 代码生成表
 */
export interface CodegenTable {
  id?: number;
  tableName: string;
  tableComment: string;
  remark?: string;
  moduleName: string;
  businessName: string;
  className: string;
  classComment: string;
  author: string;
  templateType: number;
  frontType: number;
  scene: number;
  packageName: string;
  modulePackageName: string;
  businessPackageName: string;
  classPrefix: string;
  parentMenuId?: number;
  datasourceConfigId: number;
  dateCreated?: string;
  lastUpdated?: string;
}

/**
 * 代码生成字段
 */
export interface CodegenColumn {
  id?: number;
  tableId: number;
  columnName: string;
  dataType: string;
  columnComment: string;
  nullable: boolean;
  primaryKey: boolean;
  autoIncrement?: string;
  ordinalPosition: number;
  javaType: string;
  javaField: string;
  dictType?: string;
  example?: string;
  createOperation: boolean;
  updateOperation: boolean;
  listOperation: boolean;
  listOperationCondition: string;
  listOperationResult: boolean;
  htmlType: string;
  dateCreated?: string;
  lastUpdated?: string;
}

/**
 * 代码生成表配置VO
 */
export interface CodegenTableVO extends CodegenTable {
  columns: CodegenColumn[];
}

/**
 * 数据库表信息
 */
export interface DatabaseTable {
  tableName: string;
  tableComment: string;
  dateCreated?: string;
  lastUpdated?: string;
  engine?: string;
  tableCollation?: string;
  dataLength?: number;
  tableRows?: number;
}

/**
 * 数据库字段信息
 */
export interface DatabaseColumn {
  columnName: string;
  dataType: string;
  columnComment: string;
  isNullable: string;
  columnKey: string;
  extra?: string;
  columnDefault?: string;
  ordinalPosition: number;
  characterMaximumLength?: number;
  numericPrecision?: number;
  numericScale?: number;
}

/**
 * 数据源配置
 */
export interface DataSourceConfig {
  id: number;
  name: string;
  url: string;
  username: string;
  password?: string;
  dateCreated?: string;
  lastUpdated?: string;
}

/**
 * 导入表请求
 */
export interface ImportTableRequest {
  datasourceConfigId: number;
  tableNames: string[];
}

/**
 * 更新表配置请求
 */
export interface UpdateTableConfigRequest {
  table: CodegenTable;
  columns: CodegenColumn[];
}

/**
 * 表单字段类型
 */
export type HtmlType = 
  | 'input'
  | 'textarea'
  | 'select'
  | 'radio'
  | 'checkbox'
  | 'datetime'
  | 'password'
  | 'email'
  | 'url'
  | 'tel';

/**
 * 模板类型
 */
export enum TemplateType {
  CRUD = 1,
  TREE = 2,
  MASTER_SUB = 3,
}

/**
 * 前端类型
 */
export enum FrontType {
  VUE2 = 10,
  VUE3 = 20,
  VUE3_SCHEMA = 21,
  REACT = 30,
}

/**
 * 场景类型
 */
export enum SceneType {
  ADMIN = 1,
  APP = 2,
}

// ==================== API 定义 ====================

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
    });
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

export default codegenApi;

