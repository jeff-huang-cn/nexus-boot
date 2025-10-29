import React, { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Space,
  Input,
  Form,
  Card,
  Modal,
  App,
  Popconfirm,
  Select,
  Tag,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { dictApi, type Dict } from '@/services/system/dict/dictApi';
import DictForm from './Form';

const DictList: React.FC = () => {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [data, setData] = useState<Dict[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [size, setSize] = useState(10);
  const [formVisible, setFormVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<Dict | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  // 颜色标签映射
  const colorMap: Record<string, string> = {
    default: 'default',
    primary: 'blue',
    success: 'green',
    info: 'cyan',
    warning: 'orange',
    danger: 'red',
    blue: 'blue',
    pink: 'pink',
  };

  const columns: ColumnsType<Dict> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '字典类型',
      dataIndex: 'dictType',
      key: 'dictType',
      width: 200,
    },
    {
      title: '字典标签',
      dataIndex: 'dictLabel',
      key: 'dictLabel',
      width: 150,
      render: (text, record) => (
        <Tag color={colorMap[record.colorType || 'default']}>{text}</Tag>
      ),
    },
    {
      title: '字典值',
      dataIndex: 'dictValue',
      key: 'dictValue',
      width: 120,
    },
    {
      title: '排序',
      dataIndex: 'sort',
      key: 'sort',
      width: 80,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => (
        <Tag color={status === 1 ? 'success' : 'default'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
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

      const result = await dictApi.getPage(searchParams);
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
  const handleEdit = (record: Dict) => {
    setEditingRecord(record);
    setFormVisible(true);
  };

  // 删除
  const handleDelete = async (id: number) => {
    try {
      await dictApi.delete(id);
      message.success('删除成功');
      loadData();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 批量删除
  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要删除的数据');
      return;
    }

    try {
      await dictApi.deleteBatch(selectedRowKeys as number[]);
      message.success(`成功删除 ${selectedRowKeys.length} 条数据`);
      setSelectedRowKeys([]);
      loadData();
    } catch (error) {
      message.error('批量删除失败');
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
          <Form.Item label="字典类型" name="dictType">
            <Input placeholder="请输入字典类型" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item label="字典标签" name="dictLabel">
            <Input placeholder="请输入字典标签" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item label="状态" name="status">
            <Select placeholder="请选择状态" style={{ width: 120 }} allowClear>
              <Select.Option value={1}>启用</Select.Option>
              <Select.Option value={0}>禁用</Select.Option>
            </Select>
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
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={data}
          loading={loading}
          rowKey="id"
          rowSelection={rowSelection}
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

      <Modal
        title={editingRecord ? '编辑字典' : '新增字典'}
        open={formVisible}
        onCancel={() => {
          setFormVisible(false);
          setEditingRecord(null);
        }}
        footer={null}
        width={600}
        destroyOnClose
      >
        <DictForm
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

export default DictList;