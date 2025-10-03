/**
 * 权限检查 Hook
 * 用于在组件中检查用户是否有某个权限
 */

import { useMenu } from '../contexts/MenuContext';

/**
 * 权限检查 Hook
 * @returns 权限检查函数
 * 
 * @example
 * const { hasPermission, hasAnyPermission, hasAllPermissions } = usePermission();
 * 
 * // 检查单个权限
 * if (hasPermission('system:user:create')) {
 *   // 显示新增按钮
 * }
 */
export const usePermission = () => {
  // 从 MenuContext 获取权限数据
  const { permissions } = useMenu();

  /**
   * 检查是否有指定权限
   * @param permission 权限标识
   * @returns 是否有权限
   */
  const hasPermission = (permission: string): boolean => {
    if (!permission) return true;
    return permissions.includes(permission);
  };

  /**
   * 检查是否有任意一个权限
   * @param permissionList 权限标识数组
   * @returns 是否有任意一个权限
   */
  const hasAnyPermission = (permissionList: string[]): boolean => {
    if (!permissionList || permissionList.length === 0) return true;
    return permissionList.some(permission => permissions.includes(permission));
  };

  /**
   * 检查是否拥有所有权限
   * @param permissionList 权限标识数组
   * @returns 是否拥有所有权限
   */
  const hasAllPermissions = (permissionList: string[]): boolean => {
    if (!permissionList || permissionList.length === 0) return true;
    return permissionList.every(permission => permissions.includes(permission));
  };

  return {
    permissions,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
  };
};

/**
 * 权限按钮组件属性接口
 */
export interface PermissionButtonProps {
  permission?: string;
  children: React.ReactNode;
}

/**
 * 权限按钮包装组件（可选使用）
 */
export const PermissionButton = ({
  permission,
  children,
}: PermissionButtonProps) => {
  const { hasPermission } = usePermission();
  
  if (!permission || hasPermission(permission)) {
    return children;
  }
  
  return null;
};
