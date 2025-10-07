import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Input,
  Space,
  Button,
  Modal,
  Form,
  message,
  Tag,
  Popconfirm,
  Divider,
} from 'antd';
import {
  SearchOutlined,
  ReloadOutlined,
  DatabaseOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ApiOutlined,
  EyeOutlined,
  EyeInvisibleOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { datasourceApi, DataSourceConfig, DataSourceConfigCreateReq, DataSourceConfigUpdateReq } from '../../../services/codegen/datasourceApi';

/**
 * 数据源管理页面
 */
const DatasourceManage: React.FC = () => {
  const [form] = Form.useForm();
  const [modalForm] = Form.useForm();
  const [data, setData] = useState<DataSourceConfig[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [size, setSize] = useState(10);
  const [modalVisible, setModalVisible] = useState(false);
  const [editId, setEditId] = useState<number | undefined>();
  const [testLoading, setTestLoading] = useState<number | undefined>();
  const [passwordVisible, setPasswordVisible] = useState<{ [key: number]: boolean }>({});
  const [passwordData, setPasswordData] = useState<{ [key: number]: string }>({});

  // 表格列配置
  const columns: ColumnsType<DataSourceConfig> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '数据源名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
      render: (text: string) => (
        <Space>
          <DatabaseOutlined style={{ color: '#1890ff' }} />
          <strong>{text}</strong>
        </Space>
      ),
    },
    {
      title: '连接URL',
      dataIndex: 'url',
      key: 'url',
      ellipsis: true,
      render: (text: string) => <code>{text}</code>,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      width: 150,
    },
    {
      title: '密码',
      dataIndex: 'password',
      key: 'password',
      width: 150,
      render: (text: string, record) => {
        const isVisible = passwordVisible[record.id!];
        const password = passwordData[record.id!];
        
        return (
          <Space>
            {record.hasPassword ? (
              <>
                <span style={{ fontFamily: 'monospace' }}>
                  {isVisible && password ? password : '••••••'}
                </span>
                <Button
                  type="text"
                  size="small"
                  icon={isVisible ? <EyeInvisibleOutlined /> : <EyeOutlined />}
                  onClick={() => handleTogglePassword(record.id!)}
                  style={{ padding: '0 4px' }}
                />
              </>
            ) : (
              <Tag color="default">未设置</Tag>
            )}
          </Space>
        );
      },
    },
    {
      title: '创建时间',
      dataIndex: 'dateCreated',
      key: 'dateCreated',
      width: 180,
      render: (text: string) => text ? new Date(text).toLocaleString() : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 280,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<ApiOutlined />}
            loading={testLoading === record.id}
            onClick={() => handleTestConnection(record.id!)}
          >
            测试连接
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个数据源吗？"
            onConfirm={() => handleDelete(record.id!)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            >
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
      
      const result = await datasourceApi.getPage(searchParams);
      setData(result.list);
      setTotal(result.total);
    } catch (error) {
      message.error('加载数据失败');
      console.error('加载数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 初始化加载
  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
  const handleCreate = () => {
    setEditId(undefined);
    modalForm.resetFields();
    setModalVisible(true);
  };

  // 编辑
  const handleEdit = async (record: DataSourceConfig) => {
    try {
      setEditId(record.id);
      setModalVisible(true);
      
      // Modal打开后再获取数据并设置（避免destroyOnClose导致的问题）
      const fullData = await datasourceApi.getById(record.id!);
      console.log('获取到的完整数据:', fullData);
      
      // 使用setTimeout确保Modal已完全渲染
      setTimeout(() => {
        modalForm.setFieldsValue({
          name: fullData.name,
          url: fullData.url,
          username: fullData.username,
          password: '', // 密码不回显
        });
      }, 100);
    } catch (error) {
      message.error('获取数据源详情失败');
      console.error('获取数据源详情失败:', error);
    }
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await modalForm.validateFields();
      
      if (editId) {
        // 更新
        const updateData: DataSourceConfigUpdateReq = {
          name: values.name,
          url: values.url,
          username: values.username,
        };
        // 只有输入了密码才更新密码
        if (values.password) {
          updateData.password = values.password;
        }
        await datasourceApi.update(editId, updateData);
        message.success('更新成功');
      } else {
        // 创建
        const createData: DataSourceConfigCreateReq = {
          name: values.name,
          url: values.url,
          username: values.username,
          password: values.password,
        };
        await datasourceApi.create(createData);
        message.success('创建成功');
      }
      
      setModalVisible(false);
      setEditId(undefined);
      modalForm.resetFields();
      loadData();
    } catch (error: any) {
      if (error.errorFields) {
        // 表单验证错误
        return;
      }
      message.error(editId ? '更新失败' : '创建失败');
      console.error('提交失败:', error);
    }
  };

  // 删除
  const handleDelete = async (id: number) => {
    try {
      await datasourceApi.delete(id);
      message.success('删除成功');
      loadData();
    } catch (error) {
      message.error('删除失败');
      console.error('删除失败:', error);
    }
  };

  // 切换密码显示
  const handleTogglePassword = async (id: number) => {
    const isCurrentlyVisible = passwordVisible[id];
    
    if (isCurrentlyVisible) {
      // 如果当前是显示状态，点击后隐藏
      setPasswordVisible(prev => ({ ...prev, [id]: false }));
    } else {
      // 如果当前是隐藏状态，点击后获取并显示密码
      try {
        const data = await datasourceApi.getById(id);
        setPasswordData(prev => ({ ...prev, [id]: data.password || '' }));
        setPasswordVisible(prev => ({ ...prev, [id]: true }));
      } catch (error) {
        message.error('获取密码失败');
        console.error('获取密码失败:', error);
      }
    }
  };

  // 测试连接
  const handleTestConnection = async (id: number) => {
    console.log('测试连接，ID:', id);
    setTestLoading(id);
    try {
      const success = await datasourceApi.testConnection(id);
      console.log('测试连接结果:', success);
      if (success) {
        message.success('连接测试成功');
      } else {
        message.error('连接测试失败，请检查数据源配置');
      }
    } catch (error: any) {
      console.error('连接测试失败:', error);
      message.error(error?.message || '连接测试失败');
    } finally {
      setTestLoading(undefined);
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
          <Form.Item label="数据源名称" name="name">
            <Input placeholder="请输入数据源名称" style={{ width: 200 }} />
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
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              新增数据源
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
          rowKey="id"
          scroll={{ x: 1200 }}
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

      {/* 新增/编辑弹窗 */}
      <Modal
        title={editId ? '编辑数据源' : '新增数据源'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => {
          setModalVisible(false);
          setEditId(undefined);
          modalForm.resetFields();
        }}
        width={600}
        destroyOnClose
      >
        <Form
          form={modalForm}
          layout="vertical"
        >
          <Form.Item
            label="数据源名称"
            name="name"
            rules={[{ required: true, message: '请输入数据源名称' }]}
          >
            <Input placeholder="例如：主数据库" />
          </Form.Item>
          <Form.Item
            label="连接URL"
            name="url"
            rules={[{ required: true, message: '请输入连接URL' }]}
          >
            <Input placeholder="例如：jdbc:mysql://localhost:3306/test" />
          </Form.Item>
          <Form.Item
            label="用户名"
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input placeholder="数据库用户名" />
          </Form.Item>
          <Form.Item
            label={editId ? '密码（不填则不修改）' : '密码'}
            name="password"
            rules={editId ? [] : [{ required: true, message: '请输入密码' }]}
          >
            <Input.Password placeholder="数据库密码" />
          </Form.Item>
          {editId && (
            <div style={{ marginTop: -8, marginBottom: 16, color: '#666', fontSize: 12 }}>
              💡 提示：保存后可在列表中点击"测试连接"按钮测试数据源
            </div>
          )}
        </Form>
      </Modal>
    </div>
  );
};

export default DatasourceManage;

