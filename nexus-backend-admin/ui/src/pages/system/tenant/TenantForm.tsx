import React, { useEffect } from 'react';
import {
  Form,
  Input,
  Button,
  Space,
  message,
  InputNumber,
  Select,
  Radio,
  DatePicker,
  TreeSelect,
} from 'antd';
import dayjs from 'dayjs';
import { tenantApi, type Tenant } from '../../../services/tenant/tenantApi';

interface TenantFormProps {
  initialValues?: Tenant | null;
  onSuccess?: () => void;
  onCancel?: () => void;
}

const TenantForm: React.FC<TenantFormProps> = ({
  initialValues,
  onSuccess,
  onCancel,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = React.useState(false);
  const [dataSources, setDataSources] = React.useState<Array<{ id: number; name: string }>>([]);
  const [menuTree, setMenuTree] = React.useState<any[]>([]);

  useEffect(() => {
    // 加载数据源列表和菜单树
    const loadData = async () => {
      try {
        const [dsData, menuData] = await Promise.all([
          tenantApi.getDataSources(),
          tenantApi.getMenuTree(),
        ]);
        setDataSources(dsData);
        setMenuTree(menuData);
      } catch (error) {
        message.error('加载数据失败');
        console.error('加载数据失败:', error);
      }
    };
    loadData();
  }, []);

  useEffect(() => {
    if (initialValues) {
      // 转换日期字符串为 dayjs 对象，menuIds JSON 字符串为数组
      const formValues = {
        ...initialValues,
        expireTime: initialValues.expireTime ? dayjs(initialValues.expireTime) : null,
        menuIds: initialValues.menuIds ? JSON.parse(initialValues.menuIds as any) : [],
      };
      form.setFieldsValue(formValues);
    } else {
      form.resetFields();
    }
  }, [initialValues, form]);

  const handleSubmit = async (values: any) => {
    setLoading(true);
    try {
      // 转换 dayjs 对象为字符串，menuIds 数组为 JSON 字符串
      const submitValues = {
        ...values,
        expireTime: values.expireTime ? values.expireTime.format('YYYY-MM-DDTHH:mm:ss') : null,
        menuIds: values.menuIds ? JSON.stringify(values.menuIds) : null,
      };

      if (initialValues?.id) {
        // 编辑
        await tenantApi.update(initialValues.id, submitValues);
        message.success('修改成功');
      } else {
        // 新增
        await tenantApi.create(submitValues);
        message.success('创建成功');
      }
      onSuccess?.();
    } catch (error) {
      message.error(initialValues?.id ? '修改失败' : '创建失败');
      console.error('保存失败:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Form
      form={form}
      layout="vertical"
      onFinish={handleSubmit}
      initialValues={initialValues || {}}
    >
      <Form.Item
        label="租户名称"
        name="name"
        rules={[{ required: true, message: '请输入租户名称' }]}
      >
        <Input placeholder="请输入租户名称" />
      </Form.Item>
      <Form.Item
        label="租户编码（用于识别租户）"
        name="code"
        rules={[{ required: true, message: '请输入租户编码' }]}
      >
        <Input placeholder="请输入租户编码（用于识别租户）" />
      </Form.Item>
      <Form.Item
        label="数据源"
        name="datasourceId"
        rules={[{ required: true, message: '请选择数据源' }]}
      >
        <Select
          placeholder="请选择数据源"
          allowClear
          showSearch
          options={dataSources.map(ds => ({
            label: ds.name,
            value: ds.id,
          }))}
          filterOption={(input, option) =>
            (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
          }
        />
      </Form.Item>
      <Form.Item
        label="分配菜单"
        name="menuIds"
      >
        <TreeSelect
          style={{ width: '100%' }}
          placeholder="请选择分配的菜单"
          treeData={menuTree}
          treeCheckable
          showCheckedStrategy={TreeSelect.SHOW_PARENT}
          allowClear
          maxTagCount={5}
          fieldNames={{ label: 'name', value: 'id', children: 'children' }}
        />
      </Form.Item>
      <Form.Item
        label="用户数量（可创建的用户数量）"
        name="maxUsers"
        rules={[{ required: true, message: '请输入用户数量（可创建的用户数量）' }]}
      >
        <InputNumber
          style={{ width: '100%' }}
          placeholder="请输入用户数量（可创建的用户数量）"
        />
      </Form.Item>
      <Form.Item
        label="过期时间"
        name="expireTime"
        rules={[{ required: true, message: '请输入过期时间' }]}
      >
        <DatePicker
          style={{ width: '100%' }}
          placeholder="请选择过期时间"
          showTime
        />
      </Form.Item>
      <Form.Item
        label="状态"
        name="status"
        rules={[{ required: true, message: '请选择状态' }]}
      >
        <Select placeholder="请选择状态">
          <Select.Option value={1}>启用</Select.Option>
          <Select.Option value={0}>禁用</Select.Option>
        </Select>
      </Form.Item>

      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={loading}>
            {initialValues?.id ? '更新' : '创建'}
          </Button>
          <Button onClick={onCancel}>
            取消
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
};

export default TenantForm;
