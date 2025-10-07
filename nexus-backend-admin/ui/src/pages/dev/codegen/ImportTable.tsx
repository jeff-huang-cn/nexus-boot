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
  Select,
} from 'antd';
import {
  SearchOutlined,
  ReloadOutlined,
  ImportOutlined,
  ArrowLeftOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useNavigate } from 'react-router-dom';
import { databaseApi } from '../../../services/datasources/datasources';
import { codegenApi } from '../../../services/codegen/codegen';
import type { DatabaseTable, DataSourceConfig } from '../../../types/codegen';

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
    }
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

  // 加载数据库表列表
  const loadData = useCallback(async () => {
    if (!currentDataSourceId) {
      // 如果没有选择数据源，清空数据并返回
      setData([]);
      return;
    }

    setLoading(true);
    try {
      const formValues = await form.validateFields();
      const params = {
        datasourceConfigId: currentDataSourceId,
        tableName: formValues.tableName || '',
        tableComment: formValues.tableComment || '',
      };

      const response = await databaseApi.getTableList(params);
      setData(response || []);
    } catch (error) {
      console.error('加载表数据失败:', error);
      message.error('加载表数据失败');
      setData([]);
    } finally {
      setLoading(false);
    }
  }, [currentDataSourceId, form]);

  // 初始化数据源列表
  useEffect(() => {
    const initDataSources = async () => {
      try {
        const response = await databaseApi.getDataSourceList();
        setDataSources(response || []);
        
        // 不自动设置默认数据源，让用户手动选择
        // if (response && response.length > 0) {
        //   setCurrentDataSourceId(response[0].id);
        // }
      } catch (error) {
        console.error('加载数据源失败:', error);
        message.error('加载数据源失败');
      }
    };

    initDataSources();
  }, []);

  // 当数据源改变时，重新加载数据
  useEffect(() => {
    loadData();
  }, [loadData]);

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
      navigate('/dev/codegen');
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
          <Form.Item label="数据源" name="dataSourceId">
            <Select
              placeholder="请选择数据源"
              style={{ width: 200 }}
              value={currentDataSourceId}
              onChange={(value) => setCurrentDataSourceId(value)}
              options={dataSources.map(ds => ({
                label: ds.name,
                value: ds.id,
              }))}
            />
          </Form.Item>
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
