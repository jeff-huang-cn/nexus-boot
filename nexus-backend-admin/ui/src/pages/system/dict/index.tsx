import React, { useState, useEffect, useRef } from 'react';
import { Table, Button, Space, Card, Popconfirm, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { dictApi, type DictTypeGroup } from '@/services/system/dict/dictApi';
import { useMenu } from '@/contexts/MenuContext';
import { globalMessage } from '@/utils/globalMessage';
import { useTableHeight } from '@/hooks/useTableHeight';
import DictItemsModal from './DictItemsModal';

/**
 * 字典类型管理页面
 */
const DictList: React.FC = () => {
  const [data, setData] = useState<DictTypeGroup[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingDictType, setEditingDictType] = useState<string | null>(null);
  const { permissions } = useMenu();

  // 创建表格容器的 ref
  const tableContainerRef = useRef<HTMLDivElement>(null);
  // 计算表格高度，减去按钮栏(约60px)
  const tableHeight = useTableHeight(tableContainerRef, 60);

  // 权限检查函数
  const hasPermission = (permission: string) => {
    return permissions.includes(permission);
  };

  const columns: ColumnsType<DictTypeGroup> = [
    {
      title: '字典类型',
      dataIndex: 'dictType',
      key: 'dictType',
      width: 200,
    },
    {
      title: '字典项数量',
      dataIndex: 'itemCount',
      key: 'itemCount',
      width: 120,
      render: (count: number) => <Tag color="blue">{count} 项</Tag>,
    },
    {
      title: '示例数据',
      dataIndex: 'sampleLabels',
      key: 'sampleLabels',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number) => (
        status === 1 ? <Tag color="success">启用</Tag> : <Tag color="error">禁用</Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right' as const,
      render: (_: any, record: DictTypeGroup) => (
        <Space size="small">
          {hasPermission('system:dict:update') && (
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleConfig(record.dictType)}
            >
              配置
            </Button>
          )}
          {hasPermission('system:dict:delete') && (
            <Popconfirm
              title="确定要删除这个字典类型及其所有字典项吗？"
              onConfirm={() => handleDelete(record.dictType)}
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
      const result = await dictApi.getDictTypeGroups();
      setData(result);
    } catch (error) {
      globalMessage.error('加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // 新增字典类型
  const handleAdd = () => {
    setEditingDictType(null);
    setModalVisible(true);
  };

  // 配置字典项
  const handleConfig = (dictType: string) => {
    setEditingDictType(dictType);
    setModalVisible(true);
  };

  // 删除字典类型
  const handleDelete = async (dictType: string) => {
    try {
      await dictApi.deleteDictType(dictType);
      globalMessage.success('删除成功');
      loadData();
    } catch (error) {
      globalMessage.error('删除失败');
    }
  };

  // 保存成功
  const handleSaveSuccess = () => {
    setModalVisible(false);
    setEditingDictType(null);
    loadData();
  };

  return (
    <div>
      <Card>
        <div style={{ marginBottom: 16 }}>
          <Space>
            {hasPermission('system:dict:create') && (
              <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                新增字典类型
              </Button>
            )}
          </Space>
        </div>

        <div ref={tableContainerRef}>
          <Table
            columns={columns}
            dataSource={data}
            loading={loading}
            rowKey="dictType"
            pagination={false}
            scroll={{ y: tableHeight }}
          />
        </div>
      </Card>

      <DictItemsModal
        visible={modalVisible}
        dictType={editingDictType}
        onClose={() => {
          setModalVisible(false);
          setEditingDictType(null);
        }}
        onSuccess={handleSaveSuccess}
      />
    </div>
  );
};

export default DictList;
