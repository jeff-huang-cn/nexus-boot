import React, { useEffect, useState } from 'react';
import { Modal, Tree, Spin, message } from 'antd';
import type { DataNode } from 'antd/es/tree';
import { menuApi } from '../../../services/menu/menuApi';
import { roleApi } from '../../../services/role/roleApi';
import type { Menu } from '../../../services/menu/menuApi';

interface AssignMenuModalProps {
  visible: boolean;
  roleId: number;
  onCancel: () => void;
  onSuccess: () => void;
}

// 将菜单列表转换为树形数据
const menuToTreeData = (menus: Menu[]): DataNode[] => {
  return menus.map((menu) => ({
    key: menu.id!,
    title: menu.name,
    children: menu.children ? menuToTreeData(menu.children) : undefined,
  }));
};

// 获取所有展开的节点key
const getAllExpandedKeys = (menus: Menu[]): React.Key[] => {
  const keys: React.Key[] = [];
  const traverse = (items: Menu[]) => {
    items.forEach((item) => {
      if (item.children && item.children.length > 0) {
        keys.push(item.id!);
        traverse(item.children);
      }
    });
  };
  traverse(menus);
  return keys;
};

const AssignMenuModal: React.FC<AssignMenuModalProps> = ({
  visible,
  roleId,
  onCancel,
  onSuccess,
}) => {
  const [loading, setLoading] = useState(false);
  const [treeData, setTreeData] = useState<DataNode[]>([]);
  const [checkedKeys, setCheckedKeys] = useState<React.Key[]>([]);
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);

  // 加载数据
  const loadData = React.useCallback(async () => {
    setLoading(true);
    try {
      // 获取所有菜单树
      const menuTree = await menuApi.getMenuTree() as Menu[];
      setTreeData(menuToTreeData(menuTree));
      setExpandedKeys(getAllExpandedKeys(menuTree));

      // 获取角色已分配的菜单ID
      const menuIds = await menuApi.getMenuIdsByRoleId(roleId) as number[];
      setCheckedKeys(menuIds);
    } catch (error) {
      message.error('加载数据失败');
    } finally {
      setLoading(false);
    }
  }, [roleId]);

  useEffect(() => {
    if (visible) {
      loadData();
    }
  }, [visible, loadData]);

  // 保存
  const handleSave = async () => {
    setLoading(true);
    try {
      await roleApi.assignMenu({
        roleId,
        menuIds: checkedKeys as number[],
      });
      onSuccess();
    } catch (error) {
      message.error('分配权限失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title="分配菜单权限"
      open={visible}
      onOk={handleSave}
      onCancel={onCancel}
      width={600}
      confirmLoading={loading}
    >
      <Spin spinning={loading}>
        <div style={{ maxHeight: 500, overflow: 'auto' }}>
          <Tree
            checkable
            treeData={treeData}
            checkedKeys={checkedKeys}
            expandedKeys={expandedKeys}
            onCheck={(checked) => {
              setCheckedKeys(checked as React.Key[]);
            }}
            onExpand={(expanded) => {
              setExpandedKeys(expanded);
            }}
          />
        </div>
      </Spin>
    </Modal>
  );
};

export default AssignMenuModal;

