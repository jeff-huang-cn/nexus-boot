import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { message } from 'antd';
import { getToken, removeToken, isTokenExpired } from './auth';

// 从环境变量读取配置
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
const API_PREFIX = process.env.REACT_APP_API_PREFIX || '/';

// 自定义请求接口，返回实际数据而不是 AxiosResponse
interface RequestInstance extends AxiosInstance {
  <T = any>(config: AxiosRequestConfig): Promise<T>;
  <T = any>(url: string, config?: AxiosRequestConfig): Promise<T>;
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>;
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>;
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>;
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>;
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: `${API_BASE_URL}${API_PREFIX}`, // 自动包含API前缀
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

const request = service as RequestInstance;

// 请求拦截器
request.interceptors.request.use(
  (config: any) => {
    // 1. 获取Token
    const token = getToken();
    
    // 2. 如果Token存在且未过期，添加到请求头
    if (token) {
      // 检查Token是否过期
      if (isTokenExpired(token)) {
        // Token已过期，清除并跳转登录页
        removeToken();
        message.error('登录已过期，请重新登录');
        
        // 避免在登录页循环重定向
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
        
        return Promise.reject(new Error('Token已过期'));
      }
      
      // 添加Authorization Header
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // 3. 打印请求日志（开发环境）
    if (process.env.NODE_ENV === 'development') {
      console.log('Request:', config.method?.toUpperCase(), config.url);
    }
    
    return config;
  },
  (error) => {
    // 对请求错误做些什么
    console.error('Request Error:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    // 对响应数据做点什么
    const { data } = response;
    
    // 如果是下载文件的响应，直接返回
    if (response.config.responseType === 'blob') {
      return response;
    }
    
    // 检查业务状态码
    if (data.code === 200) {
      return data.data;
    } else {
      // 业务错误
      console.error('Business Error:', data.message);
      message.error(data.message || '请求失败');
      throw new Error(data.message || '请求失败');
    }
  },
  (error) => {
    // 对响应错误做点什么
    console.error('Response Error:', error);
    
    let errorMessage = '网络错误';
    
    if (error.response) {
      const status = error.response.status;
      
      // 处理401未授权
      if (status === 401) {
        errorMessage = '登录已过期，请重新登录';
        
        // 清除Token
        removeToken();
        
        // 显示提示
        message.error(errorMessage);
        
        // 跳转登录页（避免循环重定向）
        if (window.location.pathname !== '/login') {
          // 延迟跳转，确保消息显示
          setTimeout(() => {
            window.location.href = '/login';
          }, 500);
        }
        
        return Promise.reject(new Error(errorMessage));
      }
      
      // 处理403无权限
      if (status === 403) {
        errorMessage = '无权限访问';
        message.error(errorMessage);
        return Promise.reject(new Error(errorMessage));
      }
      
      // 其他错误
      errorMessage = error.response.data?.message || `请求失败 (${status})`;
    } else if (error.request) {
      // 请求已发出但没有收到响应
      errorMessage = '网络连接超时，请检查网络';
    } else {
      // 其他错误
      errorMessage = error.message || '请求失败';
    }
    
    // 显示错误提示（排除401，已单独处理）
    if (error.response?.status !== 401) {
      message.error(errorMessage);
    }
    
    throw new Error(errorMessage);
  }
);

export default request;