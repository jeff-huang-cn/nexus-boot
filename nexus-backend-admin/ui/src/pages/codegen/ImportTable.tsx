import React, { useState, useEffect, useCallback } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Form,
  message,
  Alert,
} from 'antd';
import {
  SearchOutlined,
  ReloadOutlined,
  ImportOutlined,
  ArrowLeftOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useNavigate } from 'react-router-dom';
import { databaseApi } from '../../services/database';
import { codegenApi } from '../../services/codegen';
import type { DatabaseTable, DataSourceConfig } from '../../types/codegen';

/**
 * 导入数据库表页面
 */
const ImportTable: React.FC = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [data, setData] = useState<DatabaseTable[]>([]);
  const [loading, setLoading] = useState(false);
  const [importing, setImporting] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [selectedRows, setSelectedRows] = useState<DatabaseTable[]>([]);
  const [dataSources, setDataSources] = useState<DataSourceConfig[]>([]);
  const [currentDataSourceId, setCurrentDataSourceId] = useState<number | null>(null);

  // 表格列配置
  const columns: ColumnsType<DatabaseTable> = [
    {
      title: '表名',
      dataIndex: 'tableName',
      key: 'tableName',
      width: 250,
      render: (text: string) => <code>{text}</code>,
    },
    {
      title: '表描述',
      dataIndex: 'tableComment',
      key: 'tableComment',
      width: 200,
      render: (text: string) => text || '-',
    },
    {
      title: '字段数量',
      dataIndex: 'columnCount',
      key: 'columnCount',
      width: 100,
      align: 'center',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
      render: (text: string) => text ? new Date(text).toLocaleString() : '-',
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 180,
      render: (text: string) => text ? new Date(text).toLocaleString() : '-',
    },
  ];

  // 行选择配置
  const rowSelection = {
    selectedRowKeys,
    onChange: (newSelectedRowKeys: React.Key[], newSelectedRows: DatabaseTable[]) => {
      setSelectedRowKeys(newSelectedRowKeys);
      setSelectedRows(newSelectedRows);
    },
    onSelectAll: (selected: boolean, selectedRows: DatabaseTable[], changeRows: DatabaseTable[]) => {
      if (selected) {
        const newSelectedRowKeys = data.map(item => item.tableName);
        setSelectedRowKeys(newSelectedRowKeys);
        setSelectedRows(data);
      } else {
        setSelectedRowKeys([]);
        setSelectedRows([]);
      }
    },
  };

  // 加载数据源列表
  const loadDataSources = useCallback(async () => {
    try {
      const result = await databaseApi.getDataSourceList();
      setDataSources(result);
      if (result.length > 0) {
        setCurrentDataSourceId(result[0].id);
      }
    } catch (error) {
      message.error('加载数据源配置失败');
      console.error('Failed to load data sources:', error);
    }
  }, []);

  // 加载数据库表列表
  const loadData = useCallback(async (params?: any) => {
    if (!currentDataSourceId) {
      return;
    }
    
    setLoading(true);
    try {
      const searchParams = {
        datasourceConfigId: currentDataSourceId,
        ...form.getFieldsValue(),
        ...params,
      };
      
      const result = await databaseApi.getTableList(searchParams);
      setData(result);
    } catch (error) {
      message.error('加载数据库表失败');
      console.error('加载数据库表失败:', error);
    } finally {
      setLoading(false);
    }
  }, [form, currentDataSourceId]);

  // 初始化加载数据源
  useEffect(() => {
    loadDataSources();
  }, [loadDataSources]);

  // 数据源变化时加载表数据
  useEffect(() => {
    if (currentDataSourceId) {
      loadData();
    }
  }, [currentDataSourceId, loadData]);

  // 搜索
  const handleSearch = () => {
    loadData();
  };

  // 重置
  const handleReset = () => {
    form.resetFields();
    loadData();
  };

  // 导入选中的表
  const handleImport = async () => {
    if (selectedRows.length === 0) {
      message.warning('请选择要导入的表');
      return;
    }

    if (!currentDataSourceId) {
      message.error('请先选择数据源');
      return;
    }

    setImporting(true);
    try {
      const importData = {
        tableNames: selectedRows.map(table => table.tableName),
        datasourceConfigId: currentDataSourceId,
      };

      await codegenApi.importTables(importData);
      message.success(`成功导入 ${selectedRows.length} 个表，已自动设置合理的默认配置`);
      
      // 导入成功后跳转到表列表页面
      navigate('/codegen/table');
    } catch (error) {
      message.error('导入表失败');
      console.error('导入表失败:', error);
    } finally {
      setImporting(false);
    }
  };

  // 返回上一页
  const handleBack = () => {
    navigate(-1);
  };

  return (
    <div>
      <Card
        title={
          <Space>
            <Button
              type="text"
              icon={<ArrowLeftOutlined />}
              onClick={handleBack}
            >
              返回
            </Button>
            <span>导入数据库表</span>
          </Space>
        }
      >
        <Alert
          message="导入说明"
          description="请选择需要生成代码的数据库表。系统将自动推导模块名、业务名、类名等配置，符合yudao项目最佳实践，导入后可进一步调整。"
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />

        <Form
          form={form}
          layout="inline"
          onFinish={handleSearch}
          style={{ marginBottom: 16 }}
        >
          <Form.Item label="表名" name="tableName">
            <Input placeholder="请输入表名" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item label="表描述" name="tableComment">
            <Input placeholder="请输入表描述" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                搜索
              </Button>
              <Button onClick={handleReset} icon={<ReloadOutlined />}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>

        <div style={{ marginBottom: 16 }}>
          <Space>
            <Button
              type="primary"
              icon={<ImportOutlined />}
              onClick={handleImport}
              loading={importing}
              disabled={selectedRows.length === 0}
            >
              导入选中的表 ({selectedRows.length})
            </Button>
            <Button icon={<ReloadOutlined />} onClick={() => loadData()}>
              刷新
            </Button>
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={data}
          loading={loading}
          rowKey="tableName"
          rowSelection={rowSelection}
          pagination={{
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              `第 ${range?.[0]}-${range?.[1]} 条，共 ${total} 条记录`,
          }}
        />

        {selectedRows.length > 0 && (
          <Card 
            size="small" 
            title={`已选择 ${selectedRows.length} 个表`}
            style={{ marginTop: 16 }}
          >
            <div style={{ maxHeight: 200, overflow: 'auto' }}>
              {selectedRows.map(table => (
                <div key={table.tableName} style={{ marginBottom: 4 }}>
                  <code>{table.tableName}</code>
                  {table.tableComment && (
                    <span style={{ marginLeft: 8, color: '#666' }}>
                      - {table.tableComment}
                    </span>
                  )}
                </div>
              ))}
            </div>
          </Card>
        )}
      </Card>
    </div>
  );
};

export default ImportTable;
