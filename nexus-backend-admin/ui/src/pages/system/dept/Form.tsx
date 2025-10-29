import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, InputNumber, Select, TreeSelect, message } from 'antd';
import { deptApi, type Dept, type DeptForm as FormData } from '@/services/system/dept/deptApi';

interface Props {
  visible: boolean;
  editId?: number;
  parentId?: number;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * 部门管理表表单（树表）
 */
const DeptForm: React.FC<Props> = ({ visible, editId, parentId, onClose, onSuccess }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [treeData, setTreeData] = useState<any[]>([]);
  const [expandedKeys, setExpandedKeys] = useState<(string | number)[]>([]);

  // 收集所有节点的 key
  const collectAllKeys = (nodes: any[]): (string | number)[] => {
    const keys: (string | number)[] = [];
    const traverse = (items: any[]) => {
      items.forEach(item => {
        if (item.value !== undefined) {
          keys.push(item.value);
        }
        if (item.children && item.children.length > 0) {
          traverse(item.children);
        }
      });
    };
    traverse(nodes);
    return keys;
  };

  // 加载树形数据（用于父节点选择）
  const loadTreeData = async () => {
    try {
      const data = await deptApi.getList({});
      // 构建树形选择器数据
      const tree = buildTreeSelectData(data);
      const treeWithRoot = [
        {
          title: '根节点',
          value: 0,
          children: tree,
        },
      ];
      setTreeData(treeWithRoot);
      // 收集所有节点的 key 用于展开
      setExpandedKeys(collectAllKeys(treeWithRoot));
    } catch (error) {
      // 错误消息已在 request.ts 中统一处理
    }
  };

  // 构建树形选择器数据
  const buildTreeSelectData = (flatData: Dept[]): any[] => {
    const map = new Map<number, any>();
    const tree: any[] = [];

    flatData.forEach((item) => {
      // 排除当前编辑的节点（防止选择自己为父节点）
      if (editId && item.id === editId) {
        return;
      }

      map.set(item.id!, {
        title: item.name,
        value: item.id,
        children: [],
      });
    });

    flatData.forEach((item) => {
      if (editId && item.id === editId) {
        return;
      }

      const node = map.get(item.id!);
      if (!item.parentId || item.parentId === 0) {
        tree.push(node);
      } else {
        const parent = map.get(item.parentId);
        if (parent) {
          parent.children.push(node);
        } else {
          tree.push(node);
        }
      }
    });

    return tree;
  };

  // 加载编辑数据
  useEffect(() => {
    if (visible) {
      loadTreeData();

      if (editId) {
        setLoading(true);
        deptApi
          .getById(editId)
          .then((data) => {
            form.setFieldsValue(data);
          })
          .catch(() => {
            // 错误消息已在 request.ts 中统一处理
          })
          .finally(() => {
            setLoading(false);
          });
      } else {
        form.resetFields();
        // 设置父节点：如果传入了 parentId 则使用，否则默认为根节点(0)
        form.setFieldsValue({ parentId: parentId !== undefined ? parentId : 0 });
      }
    }
  }, [visible, editId, parentId, form]);

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      if (editId) {
        await deptApi.update({ ...values, id: editId });
        message.success('更新成功');
      } else {
        await deptApi.create(values);
        message.success('创建成功');
      }

      onSuccess();
    } catch (error: any) {
      if (error.errorFields) {
        // 表单校验失败
        return;
      }
      // 其他错误消息已在 request.ts 中统一处理
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={editId ? '编辑部门管理表' : '新增部门管理表'}
      open={visible}
      onOk={handleSubmit}
      onCancel={onClose}
      confirmLoading={loading}
      width={600}
      destroyOnClose
    >
      <Form
        form={form}
        labelCol={{ span: 6 }}
        wrapperCol={{ span: 16 }}
        autoComplete="off"
      >
        <Form.Item name="id" hidden>
          <Input />
        </Form.Item>

        <Form.Item
          name="parentId"
          label="父节点"
          rules={[{ required: true, message: '请选择父节点' }]}
        >
          <TreeSelect
            placeholder="请选择父节点"
            treeData={treeData}
            treeDefaultExpandedKeys={expandedKeys}
            allowClear
          />
        </Form.Item>

        <Form.Item
          name="name"
          label="部门名称"
          rules={[{ required: true, message: '请输入部门名称' }]}
        >
          <Input placeholder="请输入部门名称" />
        </Form.Item>

        <Form.Item
          name="code"
          label="部门编码"
        >
          <Input placeholder="请输入部门编码" />
        </Form.Item>

        <Form.Item
          name="sort"
          label="显示顺序"
        >
          <InputNumber placeholder="请输入显示顺序" style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item
          name="leaderUserId"
          label="负责人ID"
        >
          <InputNumber placeholder="请输入负责人ID" style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item
          name="phone"
          label="联系电话"
        >
          <Input placeholder="请输入联系电话" />
        </Form.Item>

        <Form.Item
          name="email"
          label="邮箱"
        >
          <Input placeholder="请输入邮箱" />
        </Form.Item>

        <Form.Item
          name="status"
          label="状态：0-禁用 1-启用"
        >
          <InputNumber placeholder="请输入状态：0-禁用 1-启用" style={{ width: '100%' }} />
        </Form.Item>

      </Form>
    </Modal>
  );
};

export default DeptForm;
