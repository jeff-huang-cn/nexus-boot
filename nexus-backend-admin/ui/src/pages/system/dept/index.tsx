import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Modal, message, Form, Input, Popconfirm, Card } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import DeptForm from './Form';
import { deptApi, type Dept } from '@/services/system/dept/deptApi';
import { useMenu as useMenuContext } from '@/contexts/MenuContext';

/**
 * 部门管理表管理（树表）
 */
const DeptList: React.FC = () => {
  const [dataSource, setDataSource] = useState<Dept[]>([]);
  const [filteredData, setFilteredData] = useState<Dept[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editId, setEditId] = useState<number | undefined>();
  const [parentId, setParentId] = useState<number | undefined>();
  const [searchForm] = Form.useForm();
  const { permissions } = useMenuContext();

  // 权限检查函数
  const hasPermission = (permission: string) => {
    return permissions.includes(permission);
  };

  // 加载数据
  const loadData = async () => {
    setLoading(true);
    try {
      const data = await deptApi.getList({});
      // 构建树形结构
      const treeData = buildTree(data);
      setDataSource(treeData);
      setFilteredData(treeData);
    } catch (error) {
      // 错误消息已在 request.ts 中统一处理
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // 构建树形数据
  const buildTree = (flatData: Dept[]): Dept[] => {
    const map = new Map<number, any>();
    const tree: any[] = [];

    // 第一遍：构建映射
    flatData.forEach((item) => {
      map.set(item.id!, { ...item, children: [] });
    });

    // 第二遍：构建树形结构
    flatData.forEach((item) => {
      const node = map.get(item.id!);
      if (!item.parentId || item.parentId === 0) {
        // 根节点
        tree.push(node);
      } else {
        // 子节点
        const parent = map.get(item.parentId);
        if (parent) {
          parent.children.push(node);
        } else {
          // 如果找不到父节点，作为根节点
          tree.push(node);
        }
      }
    });

    // 删除空的 children 数组
    const removeEmptyChildren = (nodes: any[]) => {
      nodes.forEach((node) => {
        if (node.children && node.children.length === 0) {
          delete node.children;
        } else if (node.children) {
          removeEmptyChildren(node.children);
        }
      });
    };
    removeEmptyChildren(tree);

    return tree;
  };

  // 搜索
  const handleSearch = () => {
    const values = searchForm.getFieldsValue();
    setFilteredData(dataSource);
  };

  // 重置
  const handleReset = () => {
    searchForm.resetFields();
    setFilteredData(dataSource);
  };

  // 新增（根节点）
  const handleAddRoot = () => {
    setEditId(undefined);
    setParentId(0);
    setModalVisible(true);
  };

  // 新增（子节点）
  const handleAdd = (record: Dept) => {
    setEditId(undefined);
    setParentId(record.id);
    setModalVisible(true);
  };

  // 编辑
  const handleEdit = (record: Dept) => {
    setEditId(record.id);
    setParentId(undefined);
    setModalVisible(true);
  };

  // 删除
  const handleDelete = async (id: number) => {
    try {
      await deptApi.delete(id);
      message.success('删除成功');
      loadData();
    } catch (error: any) {
      // 错误消息已在 request.ts 中统一处理
    }
  };

  // 表格列定义
  const columns: ColumnsType<Dept> = [
    {
      title: '部门ID',
      dataIndex: 'id',
      key: 'id',
      width: 200,
    },
    {
      title: '部门名称',
      dataIndex: 'name',
      key: 'name',
      width: 150,
    },
    {
      title: '部门编码',
      dataIndex: 'code',
      key: 'code',
      width: 150,
    },
    {
      title: '父部门ID（0表示根部门）',
      dataIndex: 'parentId',
      key: 'parentId',
      width: 100,
    },
    {
      title: '显示顺序',
      dataIndex: 'sort',
      key: 'sort',
      width: 100,
    },
    {
      title: '负责人ID',
      dataIndex: 'leaderUserId',
      key: 'leaderUserId',
      width: 100,
    },
    {
      title: '联系电话',
      dataIndex: 'phone',
      key: 'phone',
      width: 150,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      width: 150,
    },
    {
      title: '状态：0-禁用 1-启用',
      dataIndex: 'status',
      key: 'status',
      width: 100,
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      fixed: 'right' as const,
      render: (_: any, record: Dept) => (
        <Space size="small">
          {hasPermission('system:dept:create') && (
            <Button
              type="link"
              size="small"
              icon={<PlusOutlined />}
              onClick={() => handleAdd(record)}
            >
              新增
            </Button>
          )}
          {hasPermission('system:dept:update') && (
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            >
              编辑
            </Button>
          )}
          {hasPermission('system:dept:delete') && (
            <Popconfirm
              title="确定删除吗？"
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

  return (
    <div>
      <Card>
        {/* 新增按钮 */}
        {hasPermission('system:dept:create') && (
          <div style={{ marginBottom: 16 }}>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAddRoot}>
              新增
            </Button>
          </div>
        )}

        {/* 数据表格 */}
        <Table
          key={filteredData.length}
          columns={columns}
          dataSource={filteredData}
          loading={loading}
          rowKey="id"
          pagination={false}
          scroll={{ x: 'max-content' }}
          expandable={{
            defaultExpandAllRows: true,
          }}
        />
      </Card>

      <DeptForm
        visible={modalVisible}
        editId={editId}
        parentId={parentId}
        onClose={() => {
          setModalVisible(false);
          setEditId(undefined);
          setParentId(undefined);
        }}
        onSuccess={() => {
          setModalVisible(false);
          setEditId(undefined);
          setParentId(undefined);
          loadData();
        }}
      />
    </div>
  );
};

export default DeptList;