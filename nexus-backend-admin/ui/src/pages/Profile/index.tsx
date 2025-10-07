import React, { useState, useEffect } from 'react';
import { Form, Input, Button, Card, Radio, message, Avatar, Spin } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import { profileApi, ProfileVO, ProfileUpdateDTO } from '../../services/user/profileApi';

const Profile: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [profile, setProfile] = useState<ProfileVO | null>(null);

  // 加载个人信息
  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      setLoading(true);
      const data = await profileApi.getProfile();
      setProfile(data);
      
      // 填充表单
      form.setFieldsValue({
        username: data.username,
        nickname: data.nickname,
        email: data.email,
        mobile: data.mobile,
        sex: data.sex ?? 0,
      });
    } catch (error: any) {
      message.error(error.message || '加载个人信息失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (values: ProfileUpdateDTO) => {
    try {
      setSubmitting(true);
      await profileApi.updateProfile(values);
      message.success('个人信息更新成功');
      loadProfile();
    } catch (error: any) {
      message.error(error.message || '更新个人信息失败');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <Card title="个人信息" style={{ maxWidth: 800, margin: '0 auto' }}>
      <div style={{ textAlign: 'center', marginBottom: 24 }}>
        <Avatar size={100} icon={<UserOutlined />} src={profile?.avatar} />
      </div>

      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        autoComplete="off"
      >
        <Form.Item
          label="用户名"
          name="username"
        >
          <Input disabled />
        </Form.Item>

        <Form.Item
          label="昵称"
          name="nickname"
          rules={[{ required: true, message: '请输入昵称' }]}
        >
          <Input placeholder="请输入昵称" />
        </Form.Item>

        <Form.Item
          label="邮箱"
          name="email"
          rules={[
            { type: 'email', message: '邮箱格式不正确' }
          ]}
        >
          <Input placeholder="请输入邮箱" />
        </Form.Item>

        <Form.Item
          label="手机号"
          name="mobile"
          rules={[
            { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
          ]}
        >
          <Input placeholder="请输入手机号" />
        </Form.Item>

        <Form.Item
          label="性别"
          name="sex"
        >
          <Radio.Group>
            <Radio value={0}>未知</Radio>
            <Radio value={1}>男</Radio>
            <Radio value={2}>女</Radio>
          </Radio.Group>
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" loading={submitting} block>
            保存
          </Button>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default Profile;
