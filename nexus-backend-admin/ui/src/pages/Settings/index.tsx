import React, { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import { profileApi, PasswordUpdateDTO } from '../../services/user/profileApi';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

const Settings: React.FC = () => {
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();
  const { logout } = useAuth();

  const handleSubmit = async (values: PasswordUpdateDTO) => {
    try {
      setSubmitting(true);
      await profileApi.updatePassword(values);
      message.success('密码修改成功，请重新登录');
      
      // 清空表单
      form.resetFields();
      
      // 延迟1秒后退出登录
      setTimeout(() => {
        logout();
        navigate('/login');
      }, 1000);
    } catch (error: any) {
      message.error(error.message || '修改密码失败');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Card title="系统设置" style={{ maxWidth: 800, margin: '0 auto' }}>
      <h3 style={{ marginBottom: 24 }}>
        <LockOutlined /> 修改密码
      </h3>

      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        autoComplete="off"
      >
        <Form.Item
          label="旧密码"
          name="oldPassword"
          rules={[{ required: true, message: '请输入旧密码' }]}
        >
          <Input.Password placeholder="请输入旧密码" />
        </Form.Item>

        <Form.Item
          label="新密码"
          name="newPassword"
          rules={[
            { required: true, message: '请输入新密码' },
            { min: 6, max: 20, message: '密码长度必须在6-20之间' }
          ]}
        >
          <Input.Password placeholder="请输入新密码" />
        </Form.Item>

        <Form.Item
          label="确认密码"
          name="confirmPassword"
          dependencies={['newPassword']}
          rules={[
            { required: true, message: '请确认新密码' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('newPassword') === value) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error('两次输入的密码不一致'));
              },
            }),
          ]}
        >
          <Input.Password placeholder="请再次输入新密码" />
        </Form.Item>

        <Form.Item style={{ marginTop: 32 }}>
          <Button type="primary" htmlType="submit" loading={submitting} block>
            修改密码
          </Button>
        </Form.Item>

        <div style={{ marginTop: 16, color: '#999', fontSize: 12 }}>
          <p>温馨提示：</p>
          <ul style={{ paddingLeft: 20 }}>
            <li>密码长度为6-20个字符</li>
            <li>建议使用字母、数字和符号的组合</li>
            <li>修改成功后将自动退出，需要重新登录</li>
          </ul>
        </div>
      </Form>
    </Card>
  );
};

export default Settings;
