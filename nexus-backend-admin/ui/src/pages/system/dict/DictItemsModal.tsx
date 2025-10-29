import React, { useState, useEffect } from 'react';
import { Modal, Form, Input, Button, Space, InputNumber, Select, Table } from 'antd';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { dictApi, type Dict, type DictItemSave } from '@/services/system/dict/dictApi';
import { globalMessage } from '@/utils/globalMessage';

interface DictItemsModalProps {
  visible: boolean;
  dictType: string | null;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * 字典项配置弹窗
 */
const DictItemsModal: React.FC<DictItemsModalProps> = ({ visible, dictType, onClose, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [items, setItems] = useState<DictItemSave[]>([]);

  // 状态选项
  const statusOptions = [
    { label: '启用', value: 1 },
    { label: '禁用', value: 0 },
  ];

  // 颜色类型选项
  const colorTypeOptions = [
    { label: '默认', value: 'default' },
    { label: '主色(蓝)', value: 'primary' },
    { label: '成功(绿)', value: 'success' },
    { label: '警告(橙)', value: 'warning' },
    { label: '危险(红)', value: 'danger' },
    { label: '信息(青)', value: 'info' },
    { label: '蓝色', value: 'blue' },
    { label: '绿色', value: 'green' },
    { label: '红色', value: 'red' },
    { label: '橙色', value: 'orange' },
    { label: '粉色', value: 'pink' },
    { label: '紫色', value: 'purple' },
    { label: '青色', value: 'cyan' },
    { label: '洋红', value: 'magenta' },
    { label: '黄色', value: 'yellow' },
    { label: '火山', value: 'volcano' },
    { label: '极客蓝', value: 'geekblue' },
    { label: '柠檬', value: 'lime' },
    { label: '金色', value: 'gold' },
  ];

  // 表格列定义
  const columns: ColumnsType<DictItemSave> = [
    {
      title: '字典标签',
      dataIndex: 'dictLabel',
      key: 'dictLabel',
      width: 150,
      render: (_: any, record: DictItemSave, index: number) => (
        <Input
          value={record.dictLabel}
          placeholder="请输入标签"
          onChange={(e) => handleFieldChange(index, 'dictLabel', e.target.value)}
        />
      ),
    },
    {
      title: '字典值',
      dataIndex: 'dictValue',
      key: 'dictValue',
      width: 150,
      render: (_: any, record: DictItemSave, index: number) => (
        <Input
          value={record.dictValue}
          placeholder="请输入值"
          onChange={(e) => handleFieldChange(index, 'dictValue', e.target.value)}
        />
      ),
    },
    {
      title: '排序',
      dataIndex: 'sort',
      key: 'sort',
      width: 100,
      render: (_: any, record: DictItemSave, index: number) => (
        <InputNumber
          value={record.sort}
          min={0}
          placeholder="排序"
          style={{ width: '100%' }}
          onChange={(value) => handleFieldChange(index, 'sort', value || 0)}
        />
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (_: any, record: DictItemSave, index: number) => (
        <Select
          value={record.status}
          options={statusOptions}
          style={{ width: '100%' }}
          onChange={(value) => handleFieldChange(index, 'status', value)}
        />
      ),
    },
    {
      title: '颜色类型',
      dataIndex: 'colorType',
      key: 'colorType',
      width: 150,
      render: (_: any, record: DictItemSave, index: number) => (
        <Select
          value={record.colorType}
          options={colorTypeOptions}
          placeholder="选择颜色"
          style={{ width: '100%' }}
          allowClear
          onChange={(value) => handleFieldChange(index, 'colorType', value)}
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 80,
      fixed: 'right' as const,
      render: (_: any, record: DictItemSave, index: number) => (
        <Button
          type="link"
          danger
          icon={<DeleteOutlined />}
          onClick={() => handleRemoveItem(index)}
        >
          删除
        </Button>
      ),
    },
  ];

  // 加载字典项
  const loadDictItems = async (type: string) => {
    try {
      const result = await dictApi.getListByType(type);
      const itemList: DictItemSave[] = result.map((dict: Dict) => ({
        id: dict.id,
        dictLabel: dict.dictLabel,
        dictValue: dict.dictValue,
        sort: dict.sort || 0,
        status: dict.status || 1,
        colorType: dict.colorType || '',
        cssClass: dict.cssClass || '',
        remark: dict.remark || '',
      }));
      setItems(itemList);
    } catch (error) {
      globalMessage.error('加载字典项失败');
    }
  };

  useEffect(() => {
    if (visible) {
      if (dictType) {
        // 编辑模式：加载现有字典项
        form.setFieldsValue({ dictType });
        loadDictItems(dictType);
      } else {
        // 新增模式：清空表单
        form.resetFields();
        setItems([]);
      }
    }
  }, [visible, dictType, form]);

  // 添加字典项
  const handleAddItem = () => {
    setItems([
      ...items,
      {
        dictLabel: '',
        dictValue: '',
        sort: items.length,
        status: 1,
        colorType: '',
        cssClass: '',
        remark: '',
      },
    ]);
  };

  // 删除字典项
  const handleRemoveItem = (index: number) => {
    const newItems = [...items];
    newItems.splice(index, 1);
    setItems(newItems);
  };

  // 修改字典项字段
  const handleFieldChange = (index: number, field: keyof DictItemSave, value: any) => {
    const newItems = [...items];
    newItems[index] = { ...newItems[index], [field]: value };
    setItems(newItems);
  };

  // 保存
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      const dictType = values.dictType;

      // 校验字典项
      if (items.length === 0) {
        globalMessage.warning('请至少添加一个字典项');
        return;
      }

      for (const item of items) {
        if (!item.dictLabel || !item.dictValue) {
          globalMessage.warning('请填写完整的字典标签和字典值');
          return;
        }
      }

      setLoading(true);
      await dictApi.batchSaveDictType({
        dictType,
        items,
      });

      globalMessage.success('保存成功');
      onSuccess();
    } catch (error) {
      console.error('保存失败:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={dictType ? `配置字典项: ${dictType}` : '新增字典类型'}
      open={visible}
      onCancel={onClose}
      onOk={handleSave}
      confirmLoading={loading}
      width={1000}
      destroyOnClose
      bodyStyle={{ maxHeight: 'calc(100vh - 300px)', overflowY: 'auto' }}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="字典类型"
          name="dictType"
          rules={[{ required: true, message: '请输入字典类型' }]}
        >
          <Input
            placeholder="如：sys_user_sex"
            disabled={!!dictType}
          />
        </Form.Item>
      </Form>

      <div style={{ marginBottom: 16 }}>
        <Button type="dashed" icon={<PlusOutlined />} onClick={handleAddItem} block>
          添加字典项
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={items}
        rowKey={(record, index) => index?.toString() || '0'}
        pagination={false}
        bordered
        scroll={{ x: 800 }}
      />

      <div style={{ marginTop: 16, color: '#999', fontSize: 12 }}>
        <p>提示：</p>
        <ul style={{ paddingLeft: 20 }}>
          <li>字典标签：用于前端显示的文本</li>
          <li>字典值：实际存储的值</li>
          <li>排序：数字越小越靠前</li>
          <li>颜色类型：从下拉列表选择，用于前端标签的颜色展示</li>
        </ul>
      </div>
    </Modal>
  );
};

export default DictItemsModal;
