import React, { useEffect, useState, useRef } from 'react';
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
    TreeSelect, Card,
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined, ExpandOutlined, ShrinkOutlined } from '@ant-design/icons';
import { menuApi } from '@/services/system/menu/menuApi';
import type { Menu, MenuForm } from '@/services/system/menu/menuApi';
import { useMenu as useMenuContext } from '@/contexts/MenuContext';
import { useTableHeight } from '@/hooks/useTableHeight';

const MenuPage: React.FC = () => {
  const [dataSource, setDataSource] = useState<Menu[]>([]);
  const [filteredData, setFilteredData] = useState<Menu[]>([]);
  const [loading, setLoading] = useState(false);
  const [formVisible, setFormVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<Menu | null>(null);
  const [searchForm] = Form.useForm();
  const [form] = Form.useForm();
  const { permissions } = useMenuContext();
  const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([]);
  const [isAllExpanded, setIsAllExpanded] = useState(true);

  // 创建表格容器的 ref
  const tableContainerRef = useRef<HTMLDivElement>(null);
  // 计算表格高度，减去搜索栏(约70px)和按钮栏(约60px)
  const tableHeight = useTableHeight(tableContainerRef, 130);

  // 权限检查函数
  const hasPermission = (permission: string) => {
    return permissions.includes(permission);
  };

  // 菜单类型选项
  const menuTypeOptions = [
    { label: '目录', value: 1 },
    { label: '菜单', value: 2 },
    { label: '按钮', value: 3 },
  ];

  // 状态选项
  const statusOptions = [
    { label: '启用', value: 1 },
    { label: '禁用', value: 0 },
  ];

  // 是否选项
  const yesNoOptions = [
    { label: '是', value: 1 },
    { label: '否', value: 0 },
  ];

  // 表格列定义
  const columns = [
    {
      title: '菜单名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
    },
    {
      title: '图标',
      dataIndex: 'icon',
      key: 'icon',
      width: 80,
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 80,
      render: (type: number) => {
        const typeMap = {
          1: <Tag color="blue">目录</Tag>,
          2: <Tag color="green">菜单</Tag>,
          3: <Tag color="orange">按钮</Tag>,
        };
        return typeMap[type as keyof typeof typeMap] || '-';
      },
    },
    {
      title: '权限标识',
      dataIndex: 'permission',
      key: 'permission',
      width: 180,
    },
    {
      title: '路由地址',
      dataIndex: 'path',
      key: 'path',
      width: 150,
    },
    {
      title: '组件路径',
      dataIndex: 'component',
      key: 'component',
      width: 200,
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
      title: '操作',
      key: 'action',
      fixed: 'right' as const,
      width: 200,
      render: (_: any, record: Menu) => (
        <Space size="small">
          {hasPermission('system:menu:create') && (
            <Button
              type="link"
              size="small"
              icon={<PlusOutlined />}
              onClick={() => handleAdd(record.id)}
            >
              新增
            </Button>
          )}
          {hasPermission('system:menu:update') && (
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            >
              编辑
            </Button>
          )}
          {hasPermission('system:menu:delete') && (
            <Popconfirm
              title="确定删除该菜单吗？"
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

  // 获取所有节点的ID（递归）
  const getAllNodeKeys = (data: Menu[]): React.Key[] => {
    const keys: React.Key[] = [];
    const traverse = (nodes: Menu[]) => {
      nodes.forEach((node) => {
        if (node.id !== undefined) {
          keys.push(node.id);
        }
        if (node.children && node.children.length > 0) {
          traverse(node.children);
        }
      });
    };
    traverse(data);
    return keys;
  };

  // 加载数据
  const loadData = async () => {
    setLoading(true);
    try {
      // 使用 getFullMenuTree 获取包含按钮的完整菜单树
      const result = await menuApi.getFullMenuTree() as Menu[];
      setDataSource(result);
      setFilteredData(result);
      // 默认展开所有节点
      const allKeys = getAllNodeKeys(result);
      setExpandedRowKeys(allKeys);
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

  // 切换展开/收起
  const handleToggleExpand = () => {
    if (isAllExpanded) {
      // 当前是展开状态，则收起
      setExpandedRowKeys([]);
      setIsAllExpanded(false);
    } else {
      // 当前是收起状态，则展开
      const allKeys = getAllNodeKeys(filteredData);
      setExpandedRowKeys(allKeys);
      setIsAllExpanded(true);
    }
  };

  // 搜索
  const handleSearch = () => {
    const values = searchForm.getFieldsValue();
    const { name } = values;

    if (!name) {
      setFilteredData(dataSource);
      return;
    }

    const filterTree = (items: Menu[]): Menu[] => {
      return items.reduce((acc: Menu[], item) => {
        const matchName = item.name?.toLowerCase().includes(name.toLowerCase());
        const children = item.children ? filterTree(item.children) : [];
        
        if (matchName || children.length > 0) {
          acc.push({
            ...item,
            children: children.length > 0 ? children : item.children,
          });
        }
        return acc;
      }, []);
    };

    setFilteredData(filterTree(dataSource));
  };

  // 重置
  const handleReset = () => {
    searchForm.resetFields();
    setFilteredData(dataSource);
  };

  // 构建父级菜单树选择器数据
  const buildParentTreeData = (menus: Menu[], excludeId?: number): any[] => {
    return menus
      .filter(menu => menu.id !== excludeId && menu.type !== 3) // 排除当前菜单和按钮类型
      .map(menu => ({
        title: menu.name,
        value: menu.id,
        children: menu.children ? buildParentTreeData(menu.children, excludeId) : undefined,
      }));
  };

  // 新增（根节点）
  const handleAddRoot = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ parentId: 0, type: 1, status: 1, visible: 1, keepAlive: 1, alwaysShow: 0, sort: 0 });
    setFormVisible(true);
  };

  // 新增（子节点）
  const handleAdd = (parentId?: number) => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ parentId: parentId || 0, type: 2, status: 1, visible: 1, keepAlive: 1, alwaysShow: 0, sort: 0 });
    setFormVisible(true);
  };

  // 编辑
  const handleEdit = (record: Menu) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setFormVisible(true);
  };

  // 删除
  const handleDelete = async (id: number) => {
    try {
      await menuApi.delete(id);
      message.success('删除成功');
      loadData();
    } catch (error: any) {
      // 直接在这里显示错误，确保用户能看到
      console.error('❌ 删除菜单失败:', error);
      const errorMsg = error?.message || error?.response?.data?.message || '删除失败';
      message.error(errorMsg);
    }
  };

  // 保存
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      if (editingRecord?.id) {
        await menuApi.update(editingRecord.id, values);
        message.success('更新成功');
      } else {
        await menuApi.create(values);
        message.success('创建成功');
      }
      setFormVisible(false);
      loadData();
    } catch (error) {
      console.error('保存失败:', error);
    }
  };

  return (
    <Card style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* 搜索表单 */}
      <Form
        form={searchForm}
        layout="inline"
        onFinish={handleSearch}
        style={{ marginBottom: 16 }}
      >
        <Form.Item label="菜单名称" name="name">
          <Input placeholder="请输入菜单名称" style={{ width: 200 }} />
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
          <Button
            icon={isAllExpanded ? <ShrinkOutlined /> : <ExpandOutlined />}
            onClick={handleToggleExpand}
          >
            {isAllExpanded ? '全部收起' : '全部展开'}
          </Button>
          {hasPermission('system:menu:create') && (
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAddRoot}>
              新增菜单
            </Button>
          )}
        </Space>
      </div>

      {/* 数据表格 */}
      <div ref={tableContainerRef} style={{ flex: 1 }}>
        <Table
          key={filteredData.length}
          columns={columns}
          dataSource={filteredData}
          loading={loading}
          rowKey="id"
          pagination={false}
          scroll={{ y: tableHeight }}
          expandable={{
            expandedRowKeys: expandedRowKeys,
            onExpandedRowsChange: (expandedKeys) => setExpandedRowKeys([...expandedKeys]),
          }}
        />
      </div>

      <Modal
        title={editingRecord ? '编辑菜单' : '新增菜单'}
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
            label="父级菜单"
            name="parentId"
            rules={[{ required: true, message: '请选择父级菜单' }]}
          >
            <TreeSelect
              showSearch
              placeholder="请选择父级菜单（根节点请选择0）"
              treeDefaultExpandAll={true}
              treeData={[
                { title: '根目录', value: 0 },
                ...buildParentTreeData(dataSource, editingRecord?.id),
              ]}
            />
          </Form.Item>

          <Form.Item
            label="菜单名称"
            name="name"
            rules={[{ required: true, message: '请输入菜单名称' }]}
          >
            <Input placeholder="请输入菜单名称" />
          </Form.Item>

          <Form.Item
            label="菜单类型"
            name="type"
            rules={[{ required: true, message: '请选择菜单类型' }]}
          >
            <Select options={menuTypeOptions} placeholder="请选择菜单类型" />
          </Form.Item>

          <Form.Item label="权限标识" name="permission">
            <Input placeholder="如：system:user:create" />
          </Form.Item>

          <Form.Item label="路由地址" name="path">
            <Input placeholder="如：/system/user" />
          </Form.Item>

          <Form.Item label="菜单图标" name="icon">
            <Input placeholder="图标名称" />
          </Form.Item>

          <Form.Item label="组件路径" name="component">
            <Input placeholder="如：system/user/index" />
          </Form.Item>

          <Form.Item label="组件名称" name="componentName">
            <Input placeholder="如：SystemUser" />
          </Form.Item>

          <Form.Item label="显示顺序" name="sort">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>

          {editingRecord && (
            <>
              <Form.Item label="菜单状态" name="status">
                <Select options={statusOptions} />
              </Form.Item>

              <Form.Item label="是否可见" name="visible">
                <Select options={yesNoOptions} />
              </Form.Item>

              <Form.Item label="是否缓存" name="keepAlive">
                <Select options={yesNoOptions} />
              </Form.Item>

              <Form.Item label="总是显示" name="alwaysShow">
                <Select options={yesNoOptions} />
              </Form.Item>
            </>
          )}
        </Form>
      </Modal>
    </Card>
  );
};

export default MenuPage;

