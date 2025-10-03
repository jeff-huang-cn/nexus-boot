/**
 * 菜单工具类
 * 用于处理菜单数据、生成路由、权限过滤等
 */

import type { Menu } from '../services/menu/types';

/**
 * 路由配置接口
 */
export interface RouteConfig {
  path: string;
  name?: string;
  component?: any;
  meta?: {
    title: string;
    icon?: string;
    permission?: string;
    keepAlive?: boolean;
    hidden?: boolean;
  };
  children?: RouteConfig[];
}

/**
 * 菜单类型枚举
 */
export enum MenuType {
  DIR = 1,    // 目录
  MENU = 2,   // 菜单
  BUTTON = 3, // 按钮
}

/**
 * 将菜单列表转换为树形结构
 * @param menuList 菜单列表
 * @param parentId 父菜单ID
 * @returns 菜单树
 */
export function buildMenuTree(menuList: Menu[], parentId: number = 0): Menu[] {
  const result: Menu[] = [];
  
  menuList
    .filter(menu => menu.parentId === parentId)
    .forEach(menu => {
      const children = buildMenuTree(menuList, menu.id!);
      if (children.length > 0) {
        menu.children = children;
      }
      result.push(menu);
    });
  
  return result.sort((a, b) => (a.sort || 0) - (b.sort || 0));
}

/**
 * 过滤按钮权限，只保留目录和菜单
 * @param menuList 菜单列表
 * @returns 过滤后的菜单
 */
export function filterButtonMenus(menuList: Menu[]): Menu[] {
  return menuList
    .filter(menu => menu.type !== MenuType.BUTTON)
    .map(menu => ({
      ...menu,
      children: menu.children ? filterButtonMenus(menu.children) : undefined,
    }));
}

/**
 * 获取所有按钮权限标识
 * @param menuList 菜单列表
 * @returns 权限标识数组
 */
export function getButtonPermissions(menuList: Menu[]): string[] {
  const permissions: string[] = [];
  
  const traverse = (menus: Menu[]) => {
    menus.forEach(menu => {
      if (menu.type === MenuType.BUTTON && menu.permission) {
        permissions.push(menu.permission);
      }
      if (menu.children) {
        traverse(menu.children);
      }
    });
  };
  
  traverse(menuList);
  return permissions;
}

/**
 * 根据权限过滤菜单
 * @param menuList 菜单列表
 * @param permissions 用户拥有的权限
 * @returns 过滤后的菜单
 */
export function filterMenusByPermission(
  menuList: Menu[],
  permissions: string[]
): Menu[] {
  return menuList
    .filter(menu => {
      // 目录不需要权限检查
      if (menu.type === MenuType.DIR) {
        return true;
      }
      // 菜单和按钮需要权限检查
      return !menu.permission || permissions.includes(menu.permission);
    })
    .map(menu => ({
      ...menu,
      children: menu.children
        ? filterMenusByPermission(menu.children, permissions)
        : undefined,
    }))
    .filter(menu => {
      // 过滤掉没有子菜单的空目录
      if (menu.type === MenuType.DIR) {
        return menu.children && menu.children.length > 0;
      }
      return true;
    });
}

/**
 * 将菜单转换为路由配置
 * @param menuList 菜单列表
 * @returns 路由配置数组
 */
export function generateRoutes(menuList: Menu[]): RouteConfig[] {
  const routes: RouteConfig[] = [];
  
  menuList.forEach(menu => {
    // 只处理目录和菜单，按钮不生成路由
    if (menu.type === MenuType.BUTTON) {
      return;
    }
    
    const route: RouteConfig = {
      path: menu.path || '',
      name: menu.componentName || undefined,
      meta: {
        title: menu.name || '',
        icon: menu.icon,
        permission: menu.permission,
        keepAlive: menu.keepAlive === 1,
        hidden: menu.visible === 0,
      },
    };
    
    // 目录使用 Layout 组件
    if (menu.type === MenuType.DIR) {
      route.component = 'Layout';
    } else if (menu.component) {
      // 菜单使用实际组件路径
      route.component = menu.component;
    }
    
    // 递归处理子菜单
    if (menu.children && menu.children.length > 0) {
      route.children = generateRoutes(menu.children);
    }
    
    routes.push(route);
  });
  
  return routes;
}

/**
 * 查找菜单路径（面包屑导航用）
 * @param menuList 菜单列表
 * @param targetId 目标菜单ID
 * @returns 菜单路径数组
 */
export function findMenuPath(menuList: Menu[], targetId: number): Menu[] {
  const path: Menu[] = [];
  
  const find = (menus: Menu[], target: number): boolean => {
    for (const menu of menus) {
      path.push(menu);
      
      if (menu.id === target) {
        return true;
      }
      
      if (menu.children && find(menu.children, target)) {
        return true;
      }
      
      path.pop();
    }
    return false;
  };
  
  find(menuList, targetId);
  return path;
}

/**
 * 获取第一个可访问的菜单路径（默认首页）
 * @param menuList 菜单列表
 * @returns 第一个菜单的完整路径
 */
export function getFirstMenuPath(menuList: Menu[]): string {
  const find = (menus: Menu[], parentPath: string = ''): string | null => {
    for (const menu of menus) {
      // 跳过按钮和隐藏的菜单
      if (menu.type === MenuType.BUTTON || menu.visible === 0) {
        continue;
      }
      
      const currentPath = parentPath + '/' + (menu.path || '');
      
      // 如果是菜单，返回路径
      if (menu.type === MenuType.MENU) {
        return currentPath;
      }
      
      // 如果是目录，继续查找子菜单
      if (menu.children && menu.children.length > 0) {
        const childPath = find(menu.children, currentPath);
        if (childPath) {
          return childPath;
        }
      }
    }
    return null;
  };
  
  return find(menuList) || '/';
}

/**
 * 扁平化菜单列表（用于搜索）
 * @param menuList 菜单列表
 * @returns 扁平化的菜单数组
 */
export function flattenMenus(menuList: Menu[]): Menu[] {
  const result: Menu[] = [];
  
  const traverse = (menus: Menu[]) => {
    menus.forEach(menu => {
      result.push(menu);
      if (menu.children) {
        traverse(menu.children);
      }
    });
  };
  
  traverse(menuList);
  return result;
}



