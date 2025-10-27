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
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { deptApi, type Dept } from '../../../services/system/dept/deptApi';
import DeptForm from './Form';
import { globalMessage } from '../../../utils/globalMessage';

const DeptList: React.FC = () => {
  const [form] = Form.useForm();
  const [data, setData] = useState<Dept[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [size, setSize] = useState(10);
  const [formVisible, setFormVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<Dept | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  const columns: ColumnsType<Dept> = [
    {
      title: '部门ID',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '部门名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '部门编码',
      dataIndex: 'code',
      key: 'code',
    },
    {
      title: '父部门ID（0表示根部门）',
      dataIndex: 'parentId',
      key: 'parentId',
    },
    {
      title: '显示顺序',
      dataIndex: 'sort',
      key: 'sort',
    },
    {
      title: '负责人ID',
      dataIndex: 'leaderUserId',
      key: 'leaderUserId',
    },
    {
      title: '联系电话',
      dataIndex: 'phone',
      key: 'phone',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: '状态：0-禁用 1-启用',
      dataIndex: 'status',
      key: 'status',
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="middle">
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
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
      
      const result = await deptApi.getPage(searchParams);
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
  const handleEdit = (record: Dept) => {
    setEditingRecord(record);
    setFormVisible(true);
  };

  // 删除
  const handleDelete = async (id: number) => {
    try {
      await deptApi.delete(id);
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
      await deptApi.deleteBatch(selectedRowKeys as number[]);
      globalMessage.success(`成功删除 \${selectedRowKeys.length} 条数据`);
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
      
      const blob = await deptApi.exportData(searchParams);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = '部门管理表_' + new Date().getTime() + '.xlsx';
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
            label="部门名称"
            name="name"
          >
            <Input placeholder="请输入部门名称" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item
            label="部门编码"
            name="code"
          >
            <Input placeholder="请输入部门编码" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item
            label="状态：0-禁用 1-启用"
            name="status"
          >
            <Input placeholder="请输入状态：0-禁用 1-启用" style={{ width: 200 }} />
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
            showTotal: (total) => `共 \${total} 条记录`,
            onChange: (page, pageSize) => {
              setCurrent(page);
              setSize(pageSize || 10);
            },
          }}
        />
      </Card>

      <Modal
        title={editingRecord ? '编辑部门管理表' : '新增部门管理表'}
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
        <DeptForm
          initialValues={editingRecord}
          onSuccess={handleFormSuccess}
          onCancel={() => {
            setFormVisible(false);
            setEditingRecord(null);
          }}
        />
      </Modal>
    </div>
  );
};

export default DeptList;
