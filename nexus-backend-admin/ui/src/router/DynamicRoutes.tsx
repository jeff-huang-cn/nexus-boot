import React, { lazy, Suspense } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Spin } from 'antd';
import { useMenu } from '../contexts/MenuContext';
import type { Menu } from '../services/menu/types';
import Dashboard from '../pages/Dashboard';
import Profile from '../pages/Profile';
import Settings from '../pages/Settings';
import ImportTable from '../pages/dev/codegen/ImportTable';
import EditTable from '../pages/dev/codegen/EditTable';
import PreviewCode from '../pages/dev/codegen/PreviewCode';

// 加载页面组件的函数
const loadComponent = (componentPath: string) => {
  return lazy(() =>
    import(`../pages/${componentPath}.tsx`)
      .catch((error) => {
        console.error(`Failed to load component: ${componentPath}`, error);
        // 返回404页面
        return import('../pages/NotFound');
      })
  );
};

// 从菜单生成路由
const generateRoutesFromMenus = (menus: Menu[]): React.ReactElement[] => {
  const routes: React.ReactElement[] = [];

  const traverse = (items: Menu[]) => {
    items.forEach((item) => {
      // 只处理菜单类型（type=2），目录（type=1）和按钮（type=3）不生成路由
      if (item.type === 2 && item.path && item.component) {
        const Component = loadComponent(item.component);
        routes.push(
          <Route
            key={item.id}
            path={item.path}
            element={
              <Suspense fallback={<Spin size="large" style={{ display: 'flex', justifyContent: 'center', marginTop: '20%' }} />}>
                <Component />
              </Suspense>
            }
          />
        );
      }

      // 递归处理子菜单
      if (item.children && item.children.length > 0) {
        traverse(item.children);
      }
    });
  };

  traverse(menus);
  return routes;
};

const DynamicRoutes: React.FC = () => {
  const { menus, loading } = useMenu();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" tip="加载菜单中..." />
      </div>
    );
  }

  const dynamicRoutes = generateRoutesFromMenus(menus);

  return (
    <Routes>
      {/* 默认重定向到仪表盘 */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />

      {/* 固定路由 */}
      <Route path="/dashboard" element={<Dashboard />} />
      <Route path="/profile" element={<Profile />} />
      <Route path="/settings" element={<Settings />} />

      {/* 代码生成子页面路由（这些页面不在菜单中，需要手动注册） */}
      <Route path="/dev/codegen/import" element={<ImportTable />} />
      <Route path="/dev/codegen/edit/:id" element={<EditTable />} />
      <Route path="/dev/codegen/preview/:id" element={<PreviewCode />} />

      {/* 动态生成的路由 */}
      {dynamicRoutes}

      {/* 404页面 */}
      <Route path="*" element={<div>页面未找到</div>} />
    </Routes>
  );
};

export default DynamicRoutes;

