/**
 * 认证上下文
 * 管理用户登录状态、用户信息和认证相关操作
 * 
 * @author nexus
 */

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { authApi } from '../services/auth';
import type { LoginRequest } from '../services/auth';
import { getUserInfo, isTokenValid, removeToken, getToken } from '../utils/auth';
import type { UserInfo } from '../utils/auth';

interface AuthContextType {
  // 状态
  isAuthenticated: boolean;
  userInfo: UserInfo | null;
  loading: boolean;
  
  // 方法
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  checkAuth: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(true);

  /**
   * 检查认证状态
   * 在应用启动时调用，检查localStorage中的Token是否有效
   */
  const checkAuth = () => {
    try {
      const token = getToken();
      
      if (token && isTokenValid()) {
        // Token存在且有效
        const user = getUserInfo();
        setUserInfo(user);
        setIsAuthenticated(true);
        
        if (process.env.NODE_ENV === 'development') {
          console.log('Auth check passed:', user);
        }
      } else {
        // Token不存在或已失效
        setIsAuthenticated(false);
        setUserInfo(null);
        
        if (process.env.NODE_ENV === 'development') {
          console.log('Auth check failed: No valid token');
        }
      }
    } catch (error) {
      console.error('Check auth error:', error);
      setIsAuthenticated(false);
      setUserInfo(null);
    } finally {
      setLoading(false);
    }
  };

  /**
   * 登录
   * @param username 用户名
   * @param password 密码
   */
  const login = async (username: string, password: string) => {
    try {
      const request: LoginRequest = { username, password };
      
      // 调用登录API（API内部会自动存储Token）
      const response = await authApi.login(request);
      
      if (process.env.NODE_ENV === 'development') {
        console.log('Login success:', response);
      }
      
      // 重新检查认证状态（从Token提取用户信息）
      checkAuth();
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  };

  /**
   * 登出
   * 清除Token和用户信息，重置认证状态
   */
  const logout = () => {
    try {
      // 调用后端登出接口（可选）
      authApi.logout().catch(err => {
        console.error('Logout API error:', err);
      });
    } finally {
      // 清除前端状态
      removeToken();
      setIsAuthenticated(false);
      setUserInfo(null);
      
      if (process.env.NODE_ENV === 'development') {
        console.log('Logout completed');
      }
    }
  };

  /**
   * 应用启动时检查认证状态
   */
  useEffect(() => {
    checkAuth();
  }, []);

  /**
   * 监听localStorage变化（多标签页同步，可选）
   */
  useEffect(() => {
    const handleStorageChange = (e: StorageEvent) => {
      // 如果Token被删除或修改，重新检查认证状态
      if (e.key === 'nexus_auth_token') {
        checkAuth();
      }
    };

    window.addEventListener('storage', handleStorageChange);
    
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  const value: AuthContextType = {
    isAuthenticated,
    userInfo,
    loading,
    login,
    logout,
    checkAuth,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * 使用认证上下文的Hook
 * @returns AuthContextType
 * @throws Error 如果在AuthProvider外部使用
 */
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  
  return context;
};

export default AuthContext;

