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
  Popconfirm,
  Tag,
} from 'antd';
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  SafetyOutlined, 
  SearchOutlined,
  DownloadOutlined,
} from '@ant-design/icons';
import { roleApi } from '../../../services/system/role/roleApi';
import type { Role, RoleForm } from '../../../services/system/role/roleApi';
import AssignMenuModal from './AssignMenuModal';
import { useMenu } from '../../../contexts/MenuContext';
import { globalMessage } from '../../../utils/globalMessage';

const RolePage: React.FC = () => {
  const [dataSource, setDataSource] = useState<Role[]>([]);
  const [filteredData, setFilteredData] = useState<Role[]>([]);
  const [loading, setLoading] = useState(false);
  const [formVisible, setFormVisible] = useState(false);
  const [assignMenuVisible, setAssignMenuVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<Role | null>(null);
  const [currentRoleId, setCurrentRoleId] = useState<number | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [searchForm] = Form.useForm();
  const [form] = Form.useForm();
  const { permissions } = useMenu();

  // 权限检查函数
  const hasPermission = (permission: string) => {
    return permissions.includes(permission);
  };

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
          {hasPermission('system:role:update') && (
            <Button
              type="link"
              size="small"
              icon={<SafetyOutlined />}
              onClick={() => handleAssignMenu(record.id!)}
            >
              分配权限
            </Button>
          )}
          {hasPermission('system:role:update') && (
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            >
              编辑
            </Button>
          )}
          {record.type !== 1 && hasPermission('system:role:delete') && (
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
    } catch (error: any) {
      // 错误已在 request.ts 中统一显示
      console.error('加载数据失败:', error);
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
      globalMessage.success('删除成功');
      loadData();
    } catch (error: any) {
      // 错误已在 request.ts 中统一显示
      console.error('删除角色失败:', error);
    }
  };

  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      globalMessage.warning('请选择要删除的数据');
      return;
    }
    
    try {
      await roleApi.deleteBatch(selectedRowKeys as number[]);
      globalMessage.success(`成功删除 ${selectedRowKeys.length} 条数据`);
      setSelectedRowKeys([]);
      loadData();
    } catch (error) {
      globalMessage.error('批量删除失败');
    }
  };

  const handleExport = async () => {
    try {
      const blob = await roleApi.exportData();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `角色数据_${new Date().getTime()}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      globalMessage.success('导出成功');
    } catch (error) {
      globalMessage.error('导出失败');
    }
  };

  const rowSelection = {
    selectedRowKeys,
    onChange: (newSelectedRowKeys: React.Key[]) => {
      setSelectedRowKeys(newSelectedRowKeys);
    },
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
        globalMessage.success('更新成功');
      } else {
        await roleApi.create(values);
        globalMessage.success('创建成功');
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

      {/* 操作按钮 */}
      <div style={{ marginBottom: 16 }}>
        <Space>
          {hasPermission('system:role:create') && (
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增
            </Button>
          )}
          {hasPermission('system:role:delete') && (
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
          {hasPermission('system:role:export') && (
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
        </Space>
      </div>

      {/* 数据表格 */}
      <Table
        columns={columns}
        dataSource={filteredData}
        loading={loading}
        rowKey="id"
        rowSelection={rowSelection}
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
            globalMessage.success('分配权限成功');
          }}
        />
      )}
    </div>
  );
};

export default RolePage;

