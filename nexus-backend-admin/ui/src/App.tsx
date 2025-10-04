import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { AuthProvider } from './contexts/AuthContext';
import { MenuProvider } from './contexts/MenuContext';
import RouteGuard from './components/RouteGuard';
import AppLayout from './components/Layout/AppLayout';
import DynamicRoutes from './router/DynamicRoutes';
import Login from './pages/Login';
import './App.css';

function App() {
  return (
    <ConfigProvider locale={zhCN}>
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
    </ConfigProvider>
  );
}

export default App;
