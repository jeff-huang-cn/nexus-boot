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
} from 'antd';
import dayjs from 'dayjs';
import { deptApi, type Dept } from '../../../services/system/dept/deptApi';

interface DeptFormProps {
  initialValues?: Dept | null;
  onSuccess?: () => void;
  onCancel?: () => void;
}

const DeptForm: React.FC<DeptFormProps> = ({
  initialValues,
  onSuccess,
  onCancel,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = React.useState(false);

  useEffect(() => {
    if (initialValues) {
      // 转换日期字符串为 dayjs 对象
      const formValues = { ...initialValues };
      form.setFieldsValue(formValues);
    } else {
      form.resetFields();
    }
  }, [initialValues, form]);

  const handleSubmit = async (values: any) => {
    setLoading(true);
    try {
      // 转换 dayjs 对象为字符串
      const submitValues = { ...values };

      if (initialValues?.id) {
        // 编辑
        await deptApi.update(initialValues.id, submitValues);
        message.success('修改成功');
      } else {
        // 新增
        await deptApi.create(submitValues);
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
        label="部门名称"
        name="name"
        rules={[{ required: true, message: '请输入部门名称' }]}
      >
        <Input placeholder="请输入部门名称" />
      </Form.Item>
      <Form.Item
        label="部门编码"
        name="code"
      >
        <Input placeholder="请输入部门编码" />
      </Form.Item>
      <Form.Item
        label="父部门ID（0表示根部门）"
        name="parentId"
      >
        <InputNumber
          style={{ width: '100%' }}
          placeholder="请输入父部门ID（0表示根部门）"
        />
      </Form.Item>
      <Form.Item
        label="显示顺序"
        name="sort"
      >
        <InputNumber
          style={{ width: '100%' }}
          placeholder="请输入显示顺序"
        />
      </Form.Item>
      <Form.Item
        label="负责人ID"
        name="leaderUserId"
      >
        <InputNumber
          style={{ width: '100%' }}
          placeholder="请输入负责人ID"
        />
      </Form.Item>
      <Form.Item
        label="联系电话"
        name="phone"
      >
        <Input placeholder="请输入联系电话" />
      </Form.Item>
      <Form.Item
        label="邮箱"
        name="email"
      >
        <Input placeholder="请输入邮箱" />
      </Form.Item>
      <Form.Item
        label="状态：0-禁用 1-启用"
        name="status"
      >
        <Select placeholder="请选择状态：0-禁用 1-启用">
          {/* TODO: 添加选项 */}
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

export default DeptForm;
