import React, { useState } from 'react';
import {
  Layout,
  Menu,
  Button,
  theme,
  Breadcrumb,
} from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DatabaseOutlined,
  ToolOutlined,
  HomeOutlined,
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import type { MenuProps } from 'antd';

const { Header, Sider, Content } = Layout;

interface AppLayoutProps {
  children: React.ReactNode;
}

const AppLayout: React.FC<AppLayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  // 菜单配置
  const menuItems: MenuProps['items'] = [
    {
      key: '/dashboard',
      icon: <HomeOutlined />,
      label: '仪表盘',
    },
    {
      key: '/codegen',
      icon: <ToolOutlined />,
      label: '代码生成',
      children: [
        {
          key: '/codegen/table',
          label: '表管理',
        },
        {
          key: '/codegen/import',
          label: '导入表',
        },
      ],
    },
  ];

  // 处理菜单点击
  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    navigate(key);
  };

  // 获取当前选中的菜单key
  const getSelectedKeys = () => {
    const path = location.pathname;
    
    // 精确匹配
    if (menuItems?.some(item => item?.key === path)) {
      return [path];
    }
    
    // 查找子菜单匹配
    for (const item of menuItems || []) {
      if (item && 'children' in item && item.children) {
        const childMatch = item.children.find(child => child?.key === path);
        if (childMatch) {
          return [path];
        }
      }
    }
    
    return ['/dashboard'];
  };

  // 获取展开的菜单key
  const getOpenKeys = () => {
    const path = location.pathname;
    const openKeys: string[] = [];
    
    for (const item of menuItems || []) {
      if (item && 'children' in item && item.children) {
        const hasChild = item.children.some(child => child?.key === path);
        if (hasChild && item.key) {
          openKeys.push(item.key as string);
        }
      }
    }
    
    return openKeys;
  };

  // 生成面包屑
  const getBreadcrumbItems = () => {
    const path = location.pathname;
    const items = [{ title: '首页' }];
    
    if (path.startsWith('/codegen')) {
      items.push({ title: '代码生成' });
      if (path === '/codegen/table') {
        items.push({ title: '表管理' });
      } else if (path === '/codegen/import') {
        items.push({ title: '导入表' });
      } else if (path.startsWith('/codegen/edit/')) {
        items.push({ title: '表配置' });
      } else if (path.startsWith('/codegen/preview/')) {
        items.push({ title: '代码预览' });
      }
    } else if (path === '/dashboard') {
      items.push({ title: '仪表盘' });
    }
    
    return items;
  };

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
        <Header style={{ padding: 0, background: colorBgContainer }}>
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
            <Breadcrumb 
              style={{ margin: '0 16px' }} 
              items={getBreadcrumbItems()} 
            />
          </div>
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 280,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
          }}
        >
          {children}
        </Content>
      </Layout>
    </Layout>
  );
};

export default AppLayout;
