/**
 * 路由守卫组件
 * 保护需要登录才能访问的路由
 * 
 * @author nexus
 */

import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { Spin } from 'antd';
import { useAuth } from '../contexts/AuthContext';

interface RouteGuardProps {
  children: React.ReactNode;
}

/**
 * 路由守卫
 * 检查用户是否已登录，未登录则重定向到登录页
 */
const RouteGuard: React.FC<RouteGuardProps> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  // 加载中显示Loading页面
  if (loading) {
    return (
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          height: '100vh',
          flexDirection: 'column',
        }}
      >
        <Spin size="large" tip="加载中..." />
      </div>
    );
  }

  // 未登录重定向到登录页
  if (!isAuthenticated) {
    // 保存当前路径，登录成功后跳转回来
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // 已登录，显示子组件
  return <>{children}</>;
};

export default RouteGuard;

