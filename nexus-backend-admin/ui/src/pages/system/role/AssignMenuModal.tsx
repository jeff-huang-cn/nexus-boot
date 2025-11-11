import React, { useEffect, useState } from 'react';
import { Modal, Tree, Spin, message, Button } from 'antd';
import { ShrinkOutlined, ExpandOutlined } from '@ant-design/icons';
import type { DataNode } from 'antd/es/tree';
import { menuApi } from '../../../services/system/menu/menuApi';
import { roleApi } from '../../../services/system/role/roleApi';
import type { Menu } from '../../../services/system/menu/menuApi';

interface AssignMenuModalProps {
  visible: boolean;
  roleId: number;
  onCancel: () => void;
  onSuccess: () => void;
  showButtons?: boolean; // 是否显示按钮权限，默认为 true
}

// 将菜单列表转换为树形数据
const menuToTreeData = (menus: Menu[], showButtons: boolean = true): DataNode[] => {
  return menus
    .filter(menu => showButtons || menu.type !== 3) // 如果不显示按钮，过滤掉 type=3
    .map((menu) => {
      // 根据菜单类型设置标题样式
      let title: React.ReactNode = menu.name;
      
      if (menu.type === 3) {
        // 按钮权限 - 使用徽章样式突出显示
        title = (
          <span>
            <span style={{ 
              backgroundColor: '#52c41a', 
              color: 'white', 
              padding: '2px 8px', 
              borderRadius: '4px', 
              fontSize: '12px',
              marginRight: '8px',
              fontWeight: 'bold'
            }}>
              按钮
            </span>
            {menu.name}
            <span style={{ color: '#999', fontSize: '12px', marginLeft: '8px' }}>
              {menu.permission ? `(${menu.permission})` : ''}
            </span>
          </span>
        );
      } else if (menu.type === 2) {
        // 菜单
        title = (
          <span>
            <span style={{ 
              color: '#1890ff', 
              marginRight: '6px',
              fontWeight: 'bold'
            }}>
              📄
            </span>
            {menu.name}
          </span>
        );
      } else if (menu.type === 1) {
        // 目录
        title = (
          <span>
            <span style={{ 
              color: '#faad14', 
              marginRight: '6px',
              fontWeight: 'bold'
            }}>
              📁
            </span>
            <strong>{menu.name}</strong>
          </span>
        );
      }

      return {
        key: menu.id!,
        title,
        children: menu.children ? menuToTreeData(menu.children, showButtons) : undefined,
      };
    });
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

// 获取某个节点的所有父节点ID
const getParentKeys = (menuId: number, menus: Menu[], parentIds: number[] = []): number[] => {
  for (const menu of menus) {
    if (menu.id === menuId) {
      return parentIds;
    }
    if (menu.children && menu.children.length > 0) {
      const result = getParentKeys(menuId, menu.children, [...parentIds, menu.id!]);
      if (result.length > 0 || menu.children.some(child => child.id === menuId)) {
        return result.length > 0 ? result : [...parentIds, menu.id!];
      }
    }
  }
  return [];
};

const AssignMenuModal: React.FC<AssignMenuModalProps> = ({
  visible,
  roleId,
  onCancel,
  onSuccess,
  showButtons = true, // 默认显示按钮权限
}) => {
  const [loading, setLoading] = useState(false);
  const [treeData, setTreeData] = useState<DataNode[]>([]);
  const [checkedKeys, setCheckedKeys] = useState<React.Key[] | { checked: React.Key[]; halfChecked: React.Key[] }>([]);
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);
  const [menuTree, setMenuTree] = useState<Menu[]>([]);

  // 加载数据
  const loadData = React.useCallback(async () => {
    setLoading(true);
    try {
      // 获取所有菜单树
      const tree = await menuApi.getMenuTree() as Menu[];
      setMenuTree(tree);
      setTreeData(menuToTreeData(tree, showButtons));
      setExpandedKeys(getAllExpandedKeys(tree));

      // 获取角色已分配的菜单ID
      const menuIds = await menuApi.getMenuIdsByRoleId(roleId) as number[];
      // checkStrictly=true 时需要使用对象格式
      setCheckedKeys({ checked: menuIds, halfChecked: [] } as any);
    } catch (error: any) {
      // 错误已在 request.ts 中统一显示
      console.error('加载数据失败:', error);
    } finally {
      setLoading(false);
    }
  }, [roleId]);

  useEffect(() => {
    if (visible) {
      loadData();
    }
  }, [visible, loadData]);

  // 处理节点选中变化，实现级联选择
  const handleCheck = (checked: React.Key[] | { checked: React.Key[]; halfChecked: React.Key[] }) => {
    // checkStrictly=true 时，checked 是对象格式 { checked, halfChecked }
    const checkedArray = Array.isArray(checked) ? checked : checked.checked;
    const previousCheckedSet = new Set(Array.isArray(checkedKeys) ? checkedKeys : checkedKeys.checked);
    const newCheckedSet = new Set(checkedArray);
    
    // 找出新增的节点（被选中的）
    const addedKeys = checkedArray.filter(key => !previousCheckedSet.has(key));
    
    let finalCheckedKeys = new Set(checkedArray);
    
    // 只处理新增节点：自动选中所有父节点（向上级联）
    // 不处理删除节点，也不向下级联
    addedKeys.forEach(key => {
      const parentKeys = getParentKeys(key as number, menuTree);
      parentKeys.forEach(parentKey => finalCheckedKeys.add(parentKey));
    });
    
    // checkStrictly=true 时需要返回对象格式
    setCheckedKeys({ checked: Array.from(finalCheckedKeys), halfChecked: [] } as any);
  };

  // 切换展开/折叠
  const handleToggleExpand = () => {
    const allKeys = getAllExpandedKeys(menuTree);
    // 如果当前已全部展开，则折叠；否则展开
    if (expandedKeys.length === allKeys.length && allKeys.length > 0) {
      setExpandedKeys([]);
    } else {
      setExpandedKeys(allKeys);
    }
  };

  // 判断是否全部展开
  const isAllExpanded = () => {
    const allKeys = getAllExpandedKeys(menuTree);
    return expandedKeys.length === allKeys.length && allKeys.length > 0;
  };

  // 保存
  const handleSave = async () => {
    setLoading(true);
    try {
      const menuIds = Array.isArray(checkedKeys)
        ? checkedKeys
        : (checkedKeys as any).checked || [];

      await roleApi.assignMenu({
        roleId,
        menuIds: menuIds as number[],
      });
      message.success('分配权限成功');
      onSuccess();
    } catch (error: any) {
      // 错误已在 request.ts 中统一显示
      console.error('分配权限失败:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginRight: 40 }}>
          <span>分配菜单权限</span>
          <Button
            size="small"
            icon={isAllExpanded() ? <ShrinkOutlined /> : <ExpandOutlined />}
            onClick={handleToggleExpand}
            style={{ fontSize: '12px', color: '#666' }}
          >
            {isAllExpanded() ? '全部折叠' : '全部展开'}
          </Button>
        </div>
      }
      open={visible}
      onOk={handleSave}
      onCancel={onCancel}
      width={600}
      confirmLoading={loading}
    >
      <Spin spinning={loading}>
        <div style={{ maxHeight: 'calc(100vh - 300px)', overflow: 'auto' }}>
          <Tree
            checkable
            checkStrictly={true}
            treeData={treeData}
            checkedKeys={checkedKeys}
            expandedKeys={expandedKeys}
            onCheck={handleCheck}
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

