import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import request from '../utils/request';
import { getPermissions } from '../utils/auth';
import type { Menu } from '../services/system/menu/menuApi';

interface MenuContextType {
  menus: Menu[];
  permissions: string[];
  loading: boolean;
  loadMenus: () => Promise<void>;
  hasPermission: (permission: string) => boolean;
}

const MenuContext = createContext<MenuContextType | undefined>(undefined);

export const MenuProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [menus, setMenus] = useState<Menu[]>([]);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);

  // 加载用户菜单
  const loadMenus = async () => {
    setLoading(true);
    try {
      // 1. 从JWT Token获取权限列表（按钮权限）
      const userPermissions = getPermissions();
      setPermissions(userPermissions);
      
      // 2. 调用后端接口获取用户菜单树（已根据用户权限过滤）
      const menuTree = await request.get<Menu[]>('/system/menu/user');
      setMenus(menuTree);
      
      // 3. 存储到localStorage（可选，用于刷新页面时快速恢复）
      localStorage.setItem('user_menus', JSON.stringify(menuTree));
      localStorage.setItem('user_permissions', JSON.stringify(userPermissions));
      
      if (process.env.NODE_ENV === 'development') {
        console.log('User menus loaded:', menuTree);
        console.log('User permissions:', userPermissions);
      }
    } catch (error) {
      console.error('加载菜单失败:', error);
      setMenus([]);
      setPermissions([]);
    } finally {
      setLoading(false);
    }
  };

  // 检查权限
  const hasPermission = (permission: string): boolean => {
    if (!permission) return true;
    return permissions.includes(permission);
  };

  useEffect(() => {
    loadMenus();
  }, []);

  return (
    <MenuContext.Provider value={{ menus, permissions, loading, loadMenus, hasPermission }}>
      {children}
    </MenuContext.Provider>
  );
};

export const useMenu = () => {
  const context = useContext(MenuContext);
  if (context === undefined) {
    throw new Error('useMenu must be used within a MenuProvider');
  }
  return context;
};

