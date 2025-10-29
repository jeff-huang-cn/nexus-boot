import React, { useEffect } from 'react';
import { Form, Input, Select, InputNumber, Button, Space, App } from 'antd';
import { dictApi, type Dict, type DictForm as DictFormType } from '@/services/system/dict/dictApi';

interface DictFormProps {
  initialValues: Dict | null;
  onSuccess: () => void;
  onCancel: () => void;
}

const DictForm: React.FC<DictFormProps> = ({ initialValues, onSuccess, onCancel }) => {
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [loading, setLoading] = React.useState(false);

  useEffect(() => {
    if (initialValues) {
      form.setFieldsValue(initialValues);
    } else {
      form.resetFields();
    }
  }, [initialValues, form]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const formData: DictFormType = {
        dictType: values.dictType,
        dictLabel: values.dictLabel,
        dictValue: values.dictValue,
        sort: values.sort || 0,
        status: values.status ?? 1,
        colorType: values.colorType,
        cssClass: values.cssClass,
        remark: values.remark,
      };

      if (initialValues?.id) {
        // 更新
        await dictApi.update(initialValues.id, formData);
        message.success('更新成功');
      } else {
        // 创建
        await dictApi.create(formData);
        message.success('创建成功');
      }

      onSuccess();
    } catch (error: any) {
      if (!error.errorFields) {
        message.error(error?.message || '操作失败');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Form
      form={form}
      layout="vertical"
      initialValues={{
        status: 1,
        sort: 0,
      }}
    >
      <Form.Item
        label="字典类型"
        name="dictType"
        rules={[{ required: true, message: '请输入字典类型' }]}
      >
        <Input placeholder="例如：sys_user_sex" maxLength={100} />
      </Form.Item>

      <Form.Item
        label="字典标签"
        name="dictLabel"
        rules={[{ required: true, message: '请输入字典标签' }]}
      >
        <Input placeholder="显示值，如：男" maxLength={100} />
      </Form.Item>

      <Form.Item
        label="字典值"
        name="dictValue"
        rules={[{ required: true, message: '请输入字典值' }]}
      >
        <Input placeholder="存储值，如：1" maxLength={100} />
      </Form.Item>

      <Form.Item label="排序号" name="sort">
        <InputNumber min={0} style={{ width: '100%' }} />
      </Form.Item>

      <Form.Item label="颜色类型" name="colorType">
        <Select placeholder="请选择颜色类型" allowClear>
          <Select.Option value="default">默认（灰色）</Select.Option>
          <Select.Option value="primary">主要（蓝色）</Select.Option>
          <Select.Option value="success">成功（绿色）</Select.Option>
          <Select.Option value="info">信息（青色）</Select.Option>
          <Select.Option value="warning">警告（橙色）</Select.Option>
          <Select.Option value="danger">危险（红色）</Select.Option>
          <Select.Option value="blue">蓝色</Select.Option>
          <Select.Option value="pink">粉色</Select.Option>
        </Select>
      </Form.Item>

      <Form.Item label="CSS类名" name="cssClass">
        <Input placeholder="CSS类名" maxLength={100} />
      </Form.Item>

      <Form.Item label="状态" name="status">
        <Select>
          <Select.Option value={1}>启用</Select.Option>
          <Select.Option value={0}>禁用</Select.Option>
        </Select>
      </Form.Item>

      <Form.Item label="备注" name="remark">
        <Input.TextArea rows={3} placeholder="备注信息" maxLength={500} />
      </Form.Item>

      <Form.Item>
        <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
          <Button onClick={onCancel}>取消</Button>
          <Button type="primary" onClick={handleSubmit} loading={loading}>
            确定
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
};

export default DictForm;