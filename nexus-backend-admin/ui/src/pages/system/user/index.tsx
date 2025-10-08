import React, { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Space,
  Input,
  Form,
  Card,
  Popconfirm,
  Modal,
} from 'antd';
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  SearchOutlined,
  DownloadOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { userApi } from '../../../services/user/userApi';
import UserForm from './UserForm';
import ImportModal from '../../../components/ImportModal';
import { useMenu } from '../../../contexts/MenuContext';
import { globalMessage } from '../../../utils/globalMessage';

interface User {
  id?: number;
  username?: string;
  nickname?: string;
  email?: string;
  mobile?: string;
  status?: number;
  dateCreated?: string;
}

interface PageResult<T> {
  list: T[];
  total: number;
}

const UserList: React.FC = () => {
  const [form] = Form.useForm();
  const [data, setData] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [size, setSize] = useState(10);
  const [modalVisible, setModalVisible] = useState(false);
  const [importVisible, setImportVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<User | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const { permissions } = useMenu();

  // 权限检查函数
  const hasPermission = (permission: string) => {
    return permissions.includes(permission);
  };

  const columns: ColumnsType<User> = [
    {
      title: '用户ID',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '用户账号',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '用户昵称',
      dataIndex: 'nickname',
      key: 'nickname',
    },
    {
      title: '用户邮箱',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: '手机号码',
      dataIndex: 'mobile',
      key: 'mobile',
    },
    {
      title: '帐号状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (status === 0 ? '正常' : '停用'),
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
      width: 200,
      render: (_, record) => (
        <Space size="middle">
          {hasPermission('system:user:update') && (
            <Button
              type="link"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            >
              编辑
            </Button>
          )}
          {hasPermission('system:user:delete') && (
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
          )}
        </Space>
      ),
    },
  ];

  const loadData = async (params?: any) => {
    setLoading(true);
    try {
      const searchParams = {
        pageNum: current,
        pageSize: size,
        ...form.getFieldsValue(),
        ...params,
      };
      
      const result = await userApi.getPage(searchParams);
      setData(result.list);
      setTotal(result.total);
      setCurrent(searchParams.pageNum);
      setSize(searchParams.pageSize);
    } catch (error: any) {
      // 错误已在 request.ts 中统一显示
      console.error('加载数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [current, size]);

  const handleSearch = () => {
    setCurrent(1);
    loadData({ pageNum: 1 });
  };

  const handleReset = () => {
    form.resetFields();
    setCurrent(1);
    loadData({ pageNum: 1 });
  };

  const handleAdd = () => {
    setEditingRecord(null);
    setModalVisible(true);
  };

  const handleEdit = (record: User) => {
    setEditingRecord(record);
    setModalVisible(true);
  };

  const handleFormSuccess = () => {
    setModalVisible(false);
    setEditingRecord(null);
    loadData();
  };

  const handleDelete = async (id: number) => {
    try {
      await userApi.delete(id);
      globalMessage.success('删除成功');
      loadData();
    } catch (error: any) {
      // 错误已在 request.ts 中统一显示
      console.error('删除用户失败:', error);
    }
  };

  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      globalMessage.warning('请选择要删除的数据');
      return;
    }
    
    try {
      await userApi.deleteBatch(selectedRowKeys as number[]);
      globalMessage.success(`成功删除 ${selectedRowKeys.length} 条数据`);
      setSelectedRowKeys([]);
      loadData();
    } catch (error) {
      globalMessage.error('批量删除失败');
    }
  };

  const handleExport = async () => {
    try {
      const searchParams = {
        ...form.getFieldsValue(),
      };
      
      const blob = await userApi.exportData(searchParams);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `用户数据_${new Date().getTime()}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      globalMessage.success('导出成功');
    } catch (error) {
      globalMessage.error('导出失败');
    }
  };

  const handleImport = async (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    
    await userApi.importData(formData);
    loadData();
    return true;
  };

  const rowSelection = {
    selectedRowKeys,
    onChange: (newSelectedRowKeys: React.Key[]) => {
      setSelectedRowKeys(newSelectedRowKeys);
    },
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
          <Form.Item label="用户账号" name="username">
            <Input placeholder="请输入用户账号" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item label="用户昵称" name="nickname">
            <Input placeholder="请输入用户昵称" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                查询
              </Button>
              <Button onClick={handleReset}>重置</Button>
            </Space>
          </Form.Item>
        </Form>

        <div style={{ marginBottom: 16 }}>
          <Space>
            {hasPermission('system:user:create') && (
              <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                新增
              </Button>
            )}
            {hasPermission('system:user:delete') && (
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
            )}
            {hasPermission('system:user:export') && (
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
            )}
            {hasPermission('system:user:import') && (
              <Button 
                icon={<UploadOutlined />}
                onClick={() => setImportVisible(true)}
                style={{ 
                  borderColor: '#1890ff', 
                  color: '#1890ff',
                }}
              >
                导入
              </Button>
            )}
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
        title={editingRecord ? '编辑用户' : '新增用户'}
        open={modalVisible}
        onCancel={() => {
          setModalVisible(false);
          setEditingRecord(null);
        }}
        footer={null}
        width={600}
        destroyOnClose
      >
        <UserForm
          initialValues={editingRecord}
          onSuccess={handleFormSuccess}
          onCancel={() => {
            setModalVisible(false);
            setEditingRecord(null);
          }}
        />
      </Modal>

      <ImportModal
        visible={importVisible}
        title="用户数据导入"
        templateUrl="/system/user/export-template"
        onImport={handleImport}
        onCancel={() => setImportVisible(false)}
      />
    </div>
  );
};

export default UserList;

