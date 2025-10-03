import React, { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Space,
  Input,
  Form,
  Card,
  message,
  Popconfirm,
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { userApi } from '../../../services/user/user';

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
    } catch (error) {
      message.error('加载数据失败');
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
    message.info('新增功能待实现');
  };

  const handleEdit = (record: User) => {
    message.info('编辑功能待实现');
  };

  const handleDelete = async (id: number) => {
    try {
      await userApi.delete(id);
      message.success('删除成功');
      loadData();
    } catch (error) {
      message.error('删除失败');
    }
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
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增用户
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={data}
          loading={loading}
          rowKey="id"
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
    </div>
  );
};

export default UserList;

