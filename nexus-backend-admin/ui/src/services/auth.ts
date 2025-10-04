/**
 * 认证API服务
 * 
 * @author nexus
 */

import axios from 'axios';
import { setToken, getUserInfo } from '../utils/auth';
import type { UserInfo } from '../utils/auth';
import type { Menu } from './menu/types';

// API Base URL
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
const API_PREFIX = process.env.REACT_APP_API_PREFIX || '/';

// 登录请求接口
export interface LoginRequest {
  username: string;
  password: string;
}

// 登录响应接口
export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

/**
 * 认证API
 */
export const authApi = {
  /**
   * 登录
   * 
   * @param data 登录请求数据
   * @returns Promise<LoginResponse>
   */
  async login(data: LoginRequest): Promise<LoginResponse> {
    try {
      // Spring Security默认期望表单格式，不是JSON
      // 将数据转换为URLSearchParams（表单格式）
      const formData = new URLSearchParams();
      formData.append('username', data.username);
      formData.append('password', data.password);

      // 直接使用axios，不通过request.ts的拦截器
      // 避免循环依赖和拦截器影响
      const response = await axios.post<any>(
        `${API_BASE_URL}/login`,  // Spring Security默认登录端点
        formData,
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
        }
      );
        debugger
      // 检查响应格式
      if (response.data && response.data.code === 200) {
        const result = response.data.data;
        
        // 登录成功后自动存储Token
        if (result.accessToken) {
          setToken(result.accessToken);
        }
        
        return result;
      } else {
        throw new Error(response.data?.message || '登录失败');
      }
    } catch (error: any) {
      // 处理错误响应
      if (error.response) {
        const message = error.response.data?.message || `登录失败 (${error.response.status})`;
        throw new Error(message);
      } else if (error.request) {
        throw new Error('网络连接超时，请检查网络');
      } else {
        throw new Error(error.message || '登录失败');
      }
    }
  },

  /**
   * 登出（可选，如果后端不需要登出接口，只在前端清除Token即可）
   */
  async logout(): Promise<void> {
    // 如果后端有登出接口，可以在这里调用
    // await request.post('/logout');
    
    // 清除前端存储的Token
    // removeToken(); // 由调用方处理
  },

  /**
   * 获取当前用户信息（从Token解析）
   */
  getCurrentUser(): UserInfo | null {
    return getUserInfo();
  },

  /**
   * 获取用户菜单（根据权限过滤）
   * 
   * @returns Promise<Menu[]> 菜单树
   */
  async getUserMenus(): Promise<Menu[]> {
    // 注意：这里需要通过request.ts调用，以便添加Authorization Header
    // 但为了避免循环依赖，我们在MenuContext中直接使用menuApi.getUserMenus()
    // 这里只是定义接口，实际调用在MenuContext中
    const request = (await import('../utils/request')).default;
    return request.get<Menu[]>('/system/menu/user');
  },
};

export default authApi;

