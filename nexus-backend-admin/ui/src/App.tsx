import React from 'react';
import { BrowserRouter } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { MenuProvider } from './contexts/MenuContext';
import AppLayout from './components/Layout/AppLayout';
import DynamicRoutes from './router/DynamicRoutes';
import './App.css';

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <MenuProvider>
          <AppLayout>
            <DynamicRoutes />
          </AppLayout>
        </MenuProvider>
      </BrowserRouter>
    </ConfigProvider>
  );
}

export default App;
