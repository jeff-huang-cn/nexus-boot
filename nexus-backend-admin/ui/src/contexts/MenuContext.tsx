import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { menuApi } from '../services/menu';
import type { Menu } from '../services/menu/types';

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

  // 从菜单树中提取所有权限
  const extractPermissions = (menuList: Menu[]): string[] => {
    const perms: string[] = [];
    const traverse = (items: Menu[]) => {
      items.forEach(item => {
        if (item.permission) {
          perms.push(item.permission);
        }
        if (item.children && item.children.length > 0) {
          traverse(item.children);
        }
      });
    };
    traverse(menuList);
    return perms;
  };

  // 加载用户菜单
  const loadMenus = async () => {
    setLoading(true);
    try {
      // TODO: 后续改为 menuApi.getUserMenus() 获取当前用户的菜单
      const menuTree = await menuApi.getMenuTree();
      setMenus(menuTree);
      
      // 提取所有权限
      const perms = extractPermissions(menuTree);
      setPermissions(perms);
      
      // 存储到 localStorage
      localStorage.setItem('user_permissions', JSON.stringify(perms));
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

