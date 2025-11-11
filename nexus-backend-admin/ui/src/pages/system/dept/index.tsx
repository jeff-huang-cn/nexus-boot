import React, { useState, useEffect, useRef } from 'react';
import { Table, Button, Space, Modal, message, Form, Input, Popconfirm, Card } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined, ExpandOutlined, ShrinkOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import DeptForm from './Form';
import { deptApi, type Dept } from '@/services/system/dept/deptApi';
import { useMenu as useMenuContext } from '@/contexts/MenuContext';
import DictSelect from '@/components/DictSelect';
import { DictType } from '@/types/dict';
import { getDictData } from '@/utils/dictCache';
import type { Dict } from '@/services/system/dict/dictApi';
import { useTableHeight } from '@/hooks/useTableHeight';

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
  const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([]);
  const [isAllExpanded, setIsAllExpanded] = useState(true);
  const [statusDictList, setStatusDictList] = useState<Dict[]>([]);

  // 创建表格容器的 ref
  const tableContainerRef = useRef<HTMLDivElement>(null);
  // 计算表格高度，有搜索栏+按钮栏+无分页器：130px
  const tableHeight = useTableHeight(tableContainerRef, 130);

  // 加载字典数据
  useEffect(() => {
    getDictData(DictType.COMMON_STATUS).then(data => {
      setStatusDictList(data);
    });
  }, []);

  // 获取状态标签
  const getStatusLabel = (value: number): string => {
    const dict = statusDictList.find(d => Number(d.dictValue) === value);
    return dict?.dictLabel || String(value);
  };

  // 权限检查函数
  const hasPermission = (permission: string) => {
    return permissions.includes(permission);
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

  // 获取所有节点的ID（递归）
  const getAllNodeKeys = (data: Dept[]): React.Key[] => {
    const keys: React.Key[] = [];
    const traverse = (nodes: Dept[]) => {
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
      const data = await deptApi.getList({});
      // 构建树形结构
      const treeData = buildTree(data);
      setDataSource(treeData);
      setFilteredData(treeData);
      // 默认展开所有节点
      const allKeys = getAllNodeKeys(treeData);
      setExpandedRowKeys(allKeys);
    } catch (error) {
      // 错误消息已在 request.ts 中统一处理
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

    // 如果没有查询值，显示全部数据
    const hasSearchValue = Object.values(values).some(v => v !== undefined && v !== '');
    if (!hasSearchValue) {
      setFilteredData(dataSource);
      return;
    }

    // 递归过滤树形数据
    const filterTree = (items: Dept[]): Dept[] => {
      return items.reduce((acc: Dept[], item) => {
        let matches = true;

        // 根据查询条件过滤
        if (values.name && item.name) {
          matches = matches && item.name.toString().toLowerCase().includes(values.name.toLowerCase());
        }
        if (values.code && item.code) {
          matches = matches && item.code.toString().toLowerCase().includes(values.code.toLowerCase());
        }
        if (values.status !== undefined && values.status !== null && item.status !== undefined) {
          matches = matches && item.status === values.status;
        }

        const children = item.children ? filterTree(item.children) : [];

        // 如果当前节点匹配或子节点有匹配，则保留
        if (matches || children.length > 0) {
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
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (value: number) => getStatusLabel(value),
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
        {/* 搜索表单 */}
        <Form
          form={searchForm}
          layout="inline"
          onFinish={handleSearch}
          style={{ marginBottom: 16 }}
        >
          <Form.Item label="部门名称" name="name">
            <Input placeholder="请输入部门名称" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item label="部门编码" name="code">
            <Input placeholder="请输入部门编码" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item label="状态" name="status">
            <DictSelect
              dictType={DictType.COMMON_STATUS}
              placeholder="请选择状态"
              style={{ width: 200 }}
              allowClear
              valueType="number"
            />
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
            {hasPermission('system:dept:create') && (
              <Button type="primary" icon={<PlusOutlined />} onClick={handleAddRoot}>
                新增
              </Button>
            )}
          </Space>
        </div>

        {/* 数据表格 */}
        <div ref={tableContainerRef}>
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