/**
 * 认证API服务
 * 
 * @author nexus
 */

import axios from 'axios';
import { setToken, getUserInfo, getToken } from '../utils/auth';
import type { UserInfo } from '../utils/auth';
import type { Menu } from './menu/menuApi';

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
      console.error('登录请求失败:', error);
      
      // 处理错误响应
      if (error.response) {
        // 后端返回了响应
        const status = error.response.status;
        const data = error.response.data;
        
        // 尝试从不同的响应格式中提取错误消息
        let message = '登录失败';
        
        if (data) {
          // 尝试标准格式：{ code, message, data }
          if (data.message) {
            message = data.message;
          }
          // 尝试Spring Security默认格式：{ error, error_description }
          else if (data.error_description) {
            message = data.error_description;
          }
          // 尝试纯文本格式
          else if (typeof data === 'string') {
            message = data;
          }
          // 添加状态码说明
          else {
            message = `登录失败 (HTTP ${status})`;
          }
        }
        
        // 特殊处理401错误
        if (status === 401) {
          message = data?.message || '用户名或密码错误';
        }
        
        throw new Error(message);
      } else if (error.request) {
        // 请求已发出但没有收到响应
        throw new Error('网络连接超时，请检查网络设置');
      } else {
        // 其他错误
        throw new Error(error.message || '登录失败，请重试');
      }
    }
  },

  /**
   * 登出
   * 调用后端/logout端点，将JWT加入黑名单
   */
  async logout(): Promise<void> {
    try {
      const token = getToken();
      if (token) {
        console.log('调用后端logout接口，Token:', token.substring(0, 20) + '...');
        
        // 调用Spring Security默认的logout端点
        await axios.post(
          `${API_BASE_URL}/logout`,
          {},
          {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json',
            },
          }
        );
        
        console.log('后端logout接口调用成功');
      } else {
        console.log('未找到Token，跳过后端logout调用');
      }
    } catch (error) {
      console.error('调用logout接口失败:', error);
      // 即使后端失败，前端也要清除Token
    }
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

