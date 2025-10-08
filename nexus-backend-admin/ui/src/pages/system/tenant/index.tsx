import React, { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Space,
  Input,
  Form,
  Card,
  Modal,
  Popconfirm,
} from 'antd';
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  SearchOutlined,
  DownloadOutlined,
  KeyOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { tenantApi, type Tenant } from '../../../services/tenant/tenantApi';
import TenantForm from './TenantForm';
import AssignMenuModal from './AssignMenuModal';
import { globalMessage } from '../../../utils/globalMessage';

const Index: React.FC = () => {
  const [form] = Form.useForm();
  const [data, setData] = useState<Tenant[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [size, setSize] = useState(10);
  const [formVisible, setFormVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<Tenant | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [assignMenuVisible, setAssignMenuVisible] = useState(false);
  const [assigningTenantId, setAssigningTenantId] = useState<number>(0);

  const columns: ColumnsType<Tenant> = [
    {
      title: '租户ID',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '租户名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '租户编码',
      dataIndex: 'code',
      key: 'code',
    },
    {
      title: '数据源ID',
      dataIndex: 'datasourceId',
      key: 'datasourceId',
    },
    {
      title: '用户数量',
      dataIndex: 'maxUsers',
      key: 'maxUsers',
    },
    {
      title: '过期时间',
      dataIndex: 'expireTime',
      key: 'expireTime',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
    },
    {
      title: '创建人',
      dataIndex: 'creator',
      key: 'creator',
    },
    {
      title: '创建时间',
      dataIndex: 'dateCreated',
      key: 'dateCreated',
      render: (text: string) => text ? new Date(text).toLocaleString() : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 280,
      render: (_, record) => (
        <Space size="middle">
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            icon={<KeyOutlined />}
            onClick={() => handleAssignMenu(record)}
          >
            分配菜单
          </Button>
          <Popconfirm
            title="确定要删除这条记录吗？"
            onConfirm={() => handleDelete(record.id!)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // 加载数据
  const loadData = async (params?: any) => {
    setLoading(true);
    try {
      const searchParams = {
        pageNum: current,
        pageSize: size,
        ...form.getFieldsValue(),
        ...params,
      };
      
      const result = await tenantApi.getPage(searchParams);
      setData(result.list);
      setTotal(result.total);
      setCurrent(searchParams.pageNum);
      setSize(searchParams.pageSize);
    } catch (error) {
      globalMessage.error('加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [current, size]);

  // 搜索
  const handleSearch = () => {
    setCurrent(1);
    loadData({ pageNum: 1 });
  };

  // 重置
  const handleReset = () => {
    form.resetFields();
    setCurrent(1);
    loadData({ pageNum: 1 });
  };

  // 新增
  const handleAdd = () => {
    setEditingRecord(null);
    setFormVisible(true);
  };

  // 编辑
  const handleEdit = (record: Tenant) => {
    setEditingRecord(record);
    setFormVisible(true);
  };

  // 分配菜单
  const handleAssignMenu = (record: Tenant) => {
    setAssigningTenantId(record.id!);
    setAssignMenuVisible(true);
  };

  // 删除
  const handleDelete = async (id: number) => {
    try {
      await tenantApi.delete(id);
      globalMessage.success('删除成功');
      loadData();
    } catch (error) {
      globalMessage.error('删除失败');
    }
  };

  // 批量删除
  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      globalMessage.warning('请选择要删除的数据');
      return;
    }
    
    try {
      await tenantApi.deleteBatch(selectedRowKeys as number[]);
      globalMessage.success(`成功删除 ${selectedRowKeys.length} 条数据`);
      setSelectedRowKeys([]);
      loadData();
    } catch (error) {
      globalMessage.error('批量删除失败');
    }
  };


  // 导出
  const handleExport = async () => {
    try {
      const searchParams = {
        ...form.getFieldsValue(),
      };
      
      const blob = await tenantApi.exportData(searchParams);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `租户管理表_${new Date().getTime()}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      globalMessage.success('导出成功');
    } catch (error) {
      globalMessage.error('导出失败');
    }
  };

  // 行选择配置
  const rowSelection = {
    selectedRowKeys,
    onChange: (newSelectedRowKeys: React.Key[]) => {
      setSelectedRowKeys(newSelectedRowKeys);
    },
  };

  // 表单保存成功
  const handleFormSuccess = () => {
    setFormVisible(false);
    setEditingRecord(null);
    loadData();
  };

  return (
    <div>
      <Card>
        <Form
          form={form}
          layout="inline"
          onFinish={handleSearch}
          style={{ marginBottom: 16 }}
        >
          <Form.Item
            label="租户名称"
            name="name"
          >
            <Input placeholder="请输入租户名称" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item
            label="租户编码"
            name="code"
          >
            <Input placeholder="请输入租户编码（" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                查询
              </Button>
              <Button onClick={handleReset}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>

        <div style={{ marginBottom: 16 }}>
          <Space>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增
            </Button>
            <Popconfirm
              title="确定要删除选中的数据吗？"
              onConfirm={handleBatchDelete}
              disabled={selectedRowKeys.length === 0}
            >
              <Button 
                danger 
                icon={<DeleteOutlined />}
                disabled={selectedRowKeys.length === 0}
              >
                批量删除
              </Button>
            </Popconfirm>
            <Button 
              icon={<DownloadOutlined />} 
              onClick={handleExport}
              style={{ 
                borderColor: '#52c41a', 
                color: '#52c41a',
              }}
            >
              导出
            </Button>
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={data}
          loading={loading}
          rowKey="id"
          rowSelection={rowSelection}
          pagination={{
            current,
            pageSize: size,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
            onChange: (page, pageSize) => {
              setCurrent(page);
              setSize(pageSize || 10);
            },
          }}
        />
      </Card>

      <Modal
        title={editingRecord ? '编辑租户管理表' : '新增租户管理表'}
        open={formVisible}
        onCancel={() => {
          setFormVisible(false);
          setEditingRecord(null);
        }}
        footer={null}
        width={800}
        destroyOnClose
        bodyStyle={{ 
          maxHeight: 'calc(100vh - 300px)', 
          overflowY: 'auto',
          paddingRight: '8px' 
        }}
      >
        <TenantForm
          initialValues={editingRecord}
          onSuccess={handleFormSuccess}
          onCancel={() => {
            setFormVisible(false);
            setEditingRecord(null);
          }}
        />
      </Modal>

      <AssignMenuModal
        tenantId={assigningTenantId}
        visible={assignMenuVisible}
        onCancel={() => setAssignMenuVisible(false)}
        onSuccess={() => {
          setAssignMenuVisible(false);
          globalMessage.success('菜单分配成功');
        }}
      />
    </div>
  );
};

export default Index;
