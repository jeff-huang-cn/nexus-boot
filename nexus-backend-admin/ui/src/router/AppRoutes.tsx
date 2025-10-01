import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Dashboard from '../pages/Dashboard';
import CodegenTableList from '../pages/codegen/TableList';
import ImportTable from '../pages/codegen/ImportTable';
import EditTable from '../pages/codegen/EditTable';
import PreviewCode from '../pages/codegen/PreviewCode';

const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* 默认重定向到仪表盘 */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      
      {/* 仪表盘 */}
      <Route path="/dashboard" element={<Dashboard />} />
      
      {/* 代码生成相关路由 */}
      <Route path="/codegen/table" element={<CodegenTableList />} />
      <Route path="/codegen/import" element={<ImportTable />} />
      <Route path="/codegen/edit/:id" element={<EditTable />} />
      <Route path="/codegen/preview/:id" element={<PreviewCode />} />
      
      {/* 404页面 */}
      <Route path="*" element={<div>页面未找到</div>} />
    </Routes>
  );
};

export default AppRoutes;
