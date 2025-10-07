import React, { useEffect } from 'react';
import { Form, Input, Select, Button, Space, message } from 'antd';
import { userApi, User, UserForm as UserFormData } from '../../../services/user/userApi';

const { Option } = Select;

interface UserFormProps {
  initialValues?: User | null;
  onSuccess: () => void;
  onCancel: () => void;
}

const UserForm: React.FC<UserFormProps> = ({ initialValues, onSuccess, onCancel }) => {
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = React.useState(false);

  useEffect(() => {
    if (initialValues) {
      form.setFieldsValue(initialValues);
    } else {
      form.resetFields();
    }
  }, [initialValues, form]);

  const handleSubmit = async (values: UserFormData) => {
    setSubmitting(true);
    try {
      if (initialValues?.id) {
        await userApi.update(initialValues.id, values);
        message.success('更新成功');
      } else {
        await userApi.create(values);
        message.success('创建成功');
      }
      onSuccess();
    } catch (error: any) {
      console.error('保存失败:', error);
      // 错误信息已在 request.ts 中处理
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Form
      form={form}
      layout="vertical"
      onFinish={handleSubmit}
    >
      <Form.Item
        label="用户账号"
        name="username"
        rules={[{ required: true, message: '请输入用户账号' }]}
      >
        <Input placeholder="请输入用户账号" disabled={!!initialValues?.id} />
      </Form.Item>

      {!initialValues?.id && (
        <Form.Item
          label="登录密码"
          name="password"
          rules={[{ required: true, message: '请输入登录密码' }]}
        >
          <Input.Password placeholder="请输入登录密码" />
        </Form.Item>
      )}

      <Form.Item
        label="用户昵称"
        name="nickname"
        rules={[{ required: true, message: '请输入用户昵称' }]}
      >
        <Input placeholder="请输入用户昵称" />
      </Form.Item>

      <Form.Item
        label="用户邮箱"
        name="email"
        rules={[
          { type: 'email', message: '请输入正确的邮箱格式' }
        ]}
      >
        <Input placeholder="请输入用户邮箱" />
      </Form.Item>

      <Form.Item
        label="手机号码"
        name="mobile"
        rules={[
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码' }
        ]}
      >
        <Input placeholder="请输入手机号码" />
      </Form.Item>

      <Form.Item
        label="性别"
        name="sex"
      >
        <Select placeholder="请选择性别" allowClear>
          <Option value={1}>男</Option>
          <Option value={2}>女</Option>
        </Select>
      </Form.Item>

      <Form.Item
        label="帐号状态"
        name="status"
        initialValue={0}
      >
        <Select placeholder="请选择帐号状态">
          <Option value={0}>正常</Option>
          <Option value={1}>停用</Option>
        </Select>
      </Form.Item>

      <Form.Item
        label="备注"
        name="remark"
      >
        <Input.TextArea rows={3} placeholder="请输入备注" />
      </Form.Item>

      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit" loading={submitting}>
            确定
          </Button>
          <Button onClick={onCancel}>
            取消
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
};

export default UserForm;

