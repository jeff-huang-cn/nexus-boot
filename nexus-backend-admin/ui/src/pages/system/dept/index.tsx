import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Modal, message, Card } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import DeptForm from './Form';
import { deptApi, type Dept } from '@/services/system/dept/deptApi';

/**
 * 部门管理表管理（树表）
 */
const DeptList: React.FC = () => {
  const [list, setList] = useState<Dept[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editId, setEditId] = useState<number | undefined>();
  const [parentId, setParentId] = useState<number | undefined>();

  // 加载数据
  const loadData = async () => {
    setLoading(true);
    try {
      const data = await deptApi.getList({});
      // 构建树形结构
      setList(buildTree(data));
    } catch (error) {
      // 错误消息已在 request.ts 中统一处理
    } finally {
      setLoading(false);
    }
  };

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

  // 删除
  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '删除后不可恢复，确定要删除吗？',
      onOk: async () => {
        try {
          await deptApi.delete(id);
          message.success('删除成功');
          loadData();
        } catch (error: any) {
          // 错误消息已在 request.ts 中统一处理
        }
      },
    });
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
      width: 200,
      fixed: 'right',
      render: (_: any, record: Dept) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditId(undefined);
              setParentId(record.id);
              setModalVisible(true);
            }}
          >
            添加子节点
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => {
              setEditId(record.id);
              setParentId(undefined);
              setModalVisible(true);
            }}
          >
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.id!)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ];

  useEffect(() => {
    loadData();
  }, []);

  return (
    <Card
      title="部门管理表管理"
      extra={
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadData}>
            刷新
          </Button>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditId(undefined);
              setParentId(0); // 顶级节点
              setModalVisible(true);
            }}
          >
            新增
          </Button>
        </Space>
      }
    >
      <Table
        columns={columns}
        dataSource={list}
        loading={loading}
        rowKey="id"
        pagination={false} // 树表不分页
        defaultExpandAllRows // 默认展开所有行
        scroll={{ x: 'max-content' }}
      />

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
    </Card>
  );
};

export default DeptList;
