import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ConfigProvider, App as AntApp } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { AuthProvider } from './contexts/AuthContext';
import { MenuProvider } from './contexts/MenuContext';
import RouteGuard from './components/RouteGuard';
import AppLayout from './components/Layout/AppLayout';
import DynamicRoutes from './router/DynamicRoutes';
import Login from './pages/Login';
import { setGlobalMessage } from './utils/globalMessage';
import './App.css';

// 内部组件，用于初始化全局 message
function AppContent() {
  const { message } = AntApp.useApp();

  useEffect(() => {
    // 初始化全局 message 实例
    setGlobalMessage(message);
    console.log('✅ 全局 message 实例已初始化');
  }, [message]);

  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* 公开路由 */}
          <Route path="/login" element={<Login />} />
          
          {/* 受保护路由 */}
          <Route
            path="*"
            element={
              <RouteGuard>
                <MenuProvider>
                  <AppLayout>
                    <DynamicRoutes />
                  </AppLayout>
                </MenuProvider>
              </RouteGuard>
            }
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <AntApp>
        <AppContent />
      </AntApp>
    </ConfigProvider>
  );
}

export default App;
