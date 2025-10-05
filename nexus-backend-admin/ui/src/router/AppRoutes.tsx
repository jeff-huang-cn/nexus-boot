import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Dashboard from '../pages/Dashboard';
import CodegenTableList from '../pages/dev/codegen/index';
import ImportTable from '../pages/dev/codegen/ImportTable';
import EditTable from '../pages/dev/codegen/EditTable';
import PreviewCode from '../pages/dev/codegen/PreviewCode';
import UserList from '../pages/system/user/index';
import Profile from '../pages/Profile';
import Settings from '../pages/Settings';

const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* 默认重定向到仪表盘 */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      
      {/* 仪表盘 */}
      <Route path="/dashboard" element={<Dashboard />} />
      
      {/* 代码生成模块 */}
      <Route path="/codegen/table" element={<CodegenTableList />} />
      <Route path="/codegen/import" element={<ImportTable />} />
      <Route path="/codegen/edit/:id" element={<EditTable />} />
      <Route path="/codegen/preview/:id" element={<PreviewCode />} />

      <Route path="/user/list" element={<UserList />} />

      {/* 个人中心 */}
      <Route path="/profile" element={<Profile />} />
      
      {/* 系统设置 */}
      <Route path="/settings" element={<Settings />} />

      {/* 404页面 */}
      <Route path="*" element={<div>页面未找到</div>} />
    </Routes>
  );
};

export default AppRoutes;
