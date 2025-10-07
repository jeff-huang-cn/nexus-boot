import React, { useEffect, useState } from 'react';
import {
  Button,
  Table,
  Space,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  message,
  Popconfirm,
  Tag,
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SafetyOutlined, SearchOutlined } from '@ant-design/icons';
import { roleApi } from '../../../services/role/roleApi';
import type { Role, RoleForm } from '../../../services/role/roleApi';
import AssignMenuModal from './AssignMenuModal';

const RolePage: React.FC = () => {
  const [dataSource, setDataSource] = useState<Role[]>([]);
  const [filteredData, setFilteredData] = useState<Role[]>([]);
  const [loading, setLoading] = useState(false);
  const [formVisible, setFormVisible] = useState(false);
  const [assignMenuVisible, setAssignMenuVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<Role | null>(null);
  const [currentRoleId, setCurrentRoleId] = useState<number | null>(null);
  const [searchForm] = Form.useForm();
  const [form] = Form.useForm();

  // 状态选项
  const statusOptions = [
    { label: '启用', value: 1 },
    { label: '禁用', value: 0 },
  ];

  // 表格列定义
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '角色名称',
      dataIndex: 'name',
      key: 'name',
      width: 150,
    },
    {
      title: '角色编码',
      dataIndex: 'code',
      key: 'code',
      width: 150,
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: number) =>
        type === 1 ? <Tag color="purple">系统内置</Tag> : <Tag color="blue">自定义</Tag>,
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
      width: 80,
      render: (status: number) =>
        status === 1 ? <Tag color="success">启用</Tag> : <Tag color="error">禁用</Tag>,
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      width: 200,
    },
    {
      title: '创建时间',
      dataIndex: 'dateCreated',
      key: 'dateCreated',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right' as const,
      width: 250,
      render: (_: any, record: Role) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<SafetyOutlined />}
            onClick={() => handleAssignMenu(record.id!)}
          >
            分配权限
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          {record.type !== 1 && (
            <Popconfirm
              title="确定删除该角色吗？"
              onConfirm={() => handleDelete(record.id!)}
              okText="确定"
              cancelText="取消"
            >
              <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                删除
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  // 加载数据
  const loadData = async () => {
    setLoading(true);
    try {
      const result = await roleApi.getList() as Role[];
      setDataSource(result);
        setFilteredData(result);
    } catch (error) {
      message.error('加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // 搜索
  const handleSearch = () => {
    const values = searchForm.getFieldsValue();
    const { name, code } = values;

    if (!name && !code) {
      setFilteredData(dataSource);
      return;
    }

    const filtered = dataSource.filter(role => {
      const matchName = !name || role.name?.toLowerCase().includes(name.toLowerCase());
      const matchCode = !code || role.code?.toLowerCase().includes(code.toLowerCase());
      return matchName && matchCode;
    });
    setFilteredData(filtered);
  };

  // 重置
  const handleReset = () => {
    searchForm.resetFields();
    setFilteredData(dataSource);
  };

  // 新增
  const handleAdd = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ status: 1, sort: 0 });
    setFormVisible(true);
  };

  // 编辑
  const handleEdit = (record: Role) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setFormVisible(true);
  };

  // 删除
  const handleDelete = async (id: number) => {
    try {
      await roleApi.delete(id);
      message.success('删除成功');
      loadData();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 分配菜单
  const handleAssignMenu = (roleId: number) => {
    setCurrentRoleId(roleId);
    setAssignMenuVisible(true);
  };

  // 保存
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      if (editingRecord?.id) {
        await roleApi.update(editingRecord.id, values);
        message.success('更新成功');
      } else {
        await roleApi.create(values);
        message.success('创建成功');
      }
      setFormVisible(false);
      loadData();
    } catch (error) {
      console.error('保存失败:', error);
    }
  };

  return (
    <div style={{ padding: 24 }}>
      {/* 搜索表单 */}
      <Form
        form={searchForm}
        layout="inline"
        onFinish={handleSearch}
        style={{ marginBottom: 16 }}
      >
        <Form.Item label="角色名称" name="name">
          <Input placeholder="请输入角色名称" style={{ width: 200 }} />
        </Form.Item>
        <Form.Item label="角色编码" name="code">
          <Input placeholder="请输入角色编码" style={{ width: 200 }} />
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

      {/* 新增按钮 */}
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新增角色
        </Button>
      </div>

      {/* 数据表格 */}
      <Table
        columns={columns}
        dataSource={filteredData}
        loading={loading}
        rowKey="id"
        pagination={{
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
        }}
        scroll={{ x: 1200 }}
      />

      <Modal
        title={editingRecord ? '编辑角色' : '新增角色'}
        open={formVisible}
        onOk={handleSave}
        onCancel={() => setFormVisible(false)}
        width={600}
        destroyOnClose
        bodyStyle={{ 
          maxHeight: 'calc(100vh - 300px)', 
          overflowY: 'auto',
          paddingRight: '8px' 
        }}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            label="角色名称"
            name="name"
            rules={[{ required: true, message: '请输入角色名称' }]}
          >
            <Input placeholder="请输入角色名称" />
          </Form.Item>

          <Form.Item
            label="角色编码"
            name="code"
            rules={[{ required: true, message: '请输入角色编码' }]}
          >
            <Input placeholder="如：admin、user" />
          </Form.Item>

          <Form.Item label="显示顺序" name="sort">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item label="角色状态" name="status">
            <Select options={statusOptions} />
          </Form.Item>

          <Form.Item label="备注" name="remark">
            <Input.TextArea rows={3} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>

      {assignMenuVisible && currentRoleId && (
        <AssignMenuModal
          visible={assignMenuVisible}
          roleId={currentRoleId}
          onCancel={() => setAssignMenuVisible(false)}
          onSuccess={() => {
            setAssignMenuVisible(false);
            message.success('分配权限成功');
          }}
        />
      )}
    </div>
  );
};

export default RolePage;

