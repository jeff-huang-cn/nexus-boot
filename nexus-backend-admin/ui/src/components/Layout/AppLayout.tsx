import React, { useState, useMemo } from 'react';
import { Layout, Menu, Button, theme, Breadcrumb, Dropdown, Avatar, Space, Modal } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  HomeOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import * as Icons from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import type { MenuProps } from 'antd';
import { useMenu } from '../../contexts/MenuContext';
import { useAuth } from '../../contexts/AuthContext';
import type { Menu as MenuItem } from '../../services/system/menu/menuApi';

const { Header, Sider, Content } = Layout;

interface AppLayoutProps {
  children: React.ReactNode;
}

/**
 * 动态获取图标组件
 * @param iconName 图标名称（如：'setting', 'user'）
 * @returns React图标组件
 */
const getIcon = (iconName?: string) => {
  if (!iconName) return <HomeOutlined />;
  
  // 将图标名称转换为PascalCase并添加Outlined后缀
  // 例如: 'setting' -> 'SettingOutlined', 'user' -> 'UserOutlined'
  const iconComponentName = iconName.charAt(0).toUpperCase() + 
    iconName.slice(1).toLowerCase() + 'Outlined';
  
  const IconComponent = (Icons as any)[iconComponentName];
  
  return IconComponent ? React.createElement(IconComponent) : <HomeOutlined />;
};

/**
 * 转换菜单数据为Ant Design Menu格式
 * 
 * 菜单类型说明：
 * - type=1: 目录（Directory）- 可以有子菜单，显示展开箭头
 * - type=2: 菜单（Menu）- 点击跳转页面，不显示子菜单
 * - type=3: 按钮（Button）- 权限控制，不在菜单中显示（后端已过滤）
 */
const convertMenus = (menuList: MenuItem[]): MenuProps['items'] => {
  return menuList
    .filter(item => item.visible === 1 && (item.type === 1 || item.type === 2)) // 只显示可见的目录和菜单
    .map(item => ({
      key: item.path || `menu-${item.id}`,
      icon: getIcon(item.icon), // 动态获取图标
      label: item.name,
      // 只有目录类型（type=1）才递归处理子菜单
      // 菜单类型（type=2）点击后直接跳转，不显示子菜单
      children: item.type === 1 && item.children && item.children.length > 0
        ? convertMenus(item.children)
        : undefined,
    }));
};

const AppLayout: React.FC<AppLayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { menus, loading } = useMenu();
  const { userInfo, logout } = useAuth();
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  // 生成菜单配置
  const menuItems: MenuProps['items'] = useMemo(() => {
    const dynamicMenus = convertMenus(menus) || [];
    
    // 添加固定的仪表盘菜单
    return [
      {
        key: '/dashboard',
        icon: <HomeOutlined />,
        label: '仪表盘',
      },
      ...dynamicMenus,
    ];
  }, [menus]);

  // 处理菜单点击
  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    navigate(key);
  };

  // 获取当前选中的菜单key
  const getSelectedKeys = () => {
    const currentPath = location.pathname;
    
    // 特殊处理：/codegen/xxx 路径映射到 /dev/codegen
    if (currentPath.startsWith('/codegen/')) {
      return ['/dev/codegen'];
    }
    
    // 默认返回当前路径
    return [currentPath];
  };

  // 获取展开的菜单key
  const getOpenKeys = () => {
    const currentPath = location.pathname;
    const openKeys: string[] = [];
    
    // 查找当前路径对应的父菜单
    const findParentKeys = (items: MenuItem[], parentKey?: string) => {
      items.forEach(item => {
        const itemKey = item.path || `menu-${item.id}`;
        
        if (currentPath.startsWith(item.path || '')) {
          if (parentKey) {
            openKeys.push(parentKey);
          }
          if (item.children && item.children.length > 0) {
            openKeys.push(itemKey);
            findParentKeys(item.children, itemKey);
          }
        } else if (item.children && item.children.length > 0) {
          findParentKeys(item.children, itemKey);
        }
      });
    };
    
    findParentKeys(menus);
    return openKeys;
  };

  // 生成面包屑
  const getBreadcrumbItems = () => {
    const path = location.pathname;
    const items = [{ title: '首页' }];
    
    // 从菜单中查找当前路径的面包屑
    const findBreadcrumb = (menuList: MenuItem[], breadcrumb: string[] = []): string[] | null => {
      for (const item of menuList) {
        if (!item.name) continue;
        
        const newBreadcrumb = [...breadcrumb, item.name];
        
        if (item.path && item.path === path) {
          return newBreadcrumb;
        }
        
        if (item.children && item.children.length > 0) {
          const result = findBreadcrumb(item.children, newBreadcrumb);
          if (result) {
            return result;
          }
        }
      }
      return null;
    };
    
    const breadcrumb = findBreadcrumb(menus);
    if (breadcrumb) {
      breadcrumb.forEach(name => {
        items.push({ title: name });
      });
    } else if (path === '/dashboard') {
      items.push({ title: '仪表盘' });
    }
    
    return items;
  };

  // 用户下拉菜单
  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人信息',
      onClick: () => navigate('/profile'),
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '系统设置',
      onClick: () => navigate('/settings'),
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      danger: true,
      onClick: async () => {
        try {
          await logout();
          navigate('/login');
        } catch (error) {
          console.error('退出登录过程中发生错误:', error);
        }
      },
    },
  ];

  if (loading) {
    return <div>加载中...</div>;
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed}>
        <div
          style={{
            height: 32,
            margin: 16,
            background: 'rgba(255, 255, 255, 0.1)',
            borderRadius: 6,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white',
            fontWeight: 'bold',
          }}
        >
          {collapsed ? 'CG' : '代码生成器'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={getSelectedKeys()}
          defaultOpenKeys={getOpenKeys()}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            padding: 0,
            background: colorBgContainer,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', height: '100%' }}>
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed(!collapsed)}
              style={{
                fontSize: '16px',
                width: 64,
                height: 64,
              }}
            />
            <Breadcrumb style={{ margin: '0 16px' }} items={getBreadcrumbItems()} />
          </div>

          {/* 右侧用户信息 */}
          <div style={{ paddingRight: '24px' }}>
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <Space style={{ cursor: 'pointer' }}>
                <Avatar icon={<UserOutlined />} />
                <span>{userInfo?.username || '未知用户'}</span>
              </Space>
            </Dropdown>
          </div>
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 280,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
            overflow: 'hidden',
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          {children}
        </Content>
      </Layout>
    </Layout>
  );
};

export default AppLayout;