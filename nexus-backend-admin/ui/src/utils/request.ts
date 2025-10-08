import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { getToken, removeToken, isTokenExpired } from './auth';
import { globalMessage } from './globalMessage';

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
    
    console.log('=== 请求拦截器 ===');
    console.log('URL:', config.url);
    console.log('Method:', config.method);
    console.log('Token存在:', !!token);
    
    // 2. 如果Token存在且未过期，添加到请求头
    if (token) {
      // 检查Token是否过期
      const expired = isTokenExpired(token);
      console.log('Token是否过期:', expired);
      
      if (expired) {
        // Token已过期，清除并跳转登录页
        removeToken();
        globalMessage.error('登录已过期，请重新登录');
        
        // 避免在登录页循环重定向
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
        
        return Promise.reject(new Error('Token已过期'));
      }
      
      // 添加Authorization Header
      config.headers.Authorization = `Bearer ${token}`;
      console.log('已添加Authorization头');
      console.log('Token前20个字符:', token.substring(0, 20) + '...');
    } else {
      console.warn('没有Token，跳过Authorization头');
    }
    
    // 3. 打印请求日志（开发环境）
    if (process.env.NODE_ENV === 'development') {
      console.log('Request:', config.method?.toUpperCase(), config.url);
      console.log('Headers:', config.headers);
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
    
    // 如果是下载文件的响应，直接返回blob数据
    if (response.config.responseType === 'blob') {
      return response.data;
    }
    
    // 检查业务状态码
    if (data.code === 200) {
      return data.data;
    } else {
      // 业务错误 - 提取错误信息并显示
      const errorMsg = data.message || data.msg || data.error || '请求失败';
      console.error('业务错误:', { code: data.code, message: errorMsg, fullData: data });
      console.log('准备调用 globalMessage.error (业务错误):', errorMsg);
      globalMessage.error(errorMsg);
      console.log('globalMessage.error 已调用 (业务错误)');
      
      return Promise.reject(new Error(errorMsg));
    }
  },
  (error) => {
    // 对响应错误做点什么
    console.error('响应错误:', error);
    
    let errorMessage = '网络错误';
    
    if (error.response) {
      const status = error.response.status;
      const responseData = error.response.data;
      
      // 统一的错误信息提取函数
      const extractErrorMessage = (data: any, defaultMsg: string): string => {
        if (!data) return defaultMsg;
        if (typeof data === 'string') return data;
        return data.message || data.msg || data.error || defaultMsg;
      };
      
      console.error('错误详情:', {
        status,
        url: error.config?.url,
        method: error.config?.method,
        responseData
      });
      
      // 根据HTTP状态码提取错误信息
      switch (status) {
        case 400:
          errorMessage = extractErrorMessage(responseData, '请求参数错误');
          break;
        case 401:
          errorMessage = extractErrorMessage(responseData, '认证失败，请重新登录');
          // 可选：自动跳转登录（生产环境建议启用）
          removeToken();
          if (window.location.pathname !== '/login') {
            setTimeout(() => window.location.href = '/login', 500);
          }
          break;
        case 403:
          errorMessage = extractErrorMessage(responseData, '无权限访问');
          break;
        case 404:
          errorMessage = extractErrorMessage(responseData, '请求的资源不存在');
          break;
        case 500:
          errorMessage = extractErrorMessage(responseData, '服务器内部错误');
          break;
        default:
          errorMessage = extractErrorMessage(responseData, `请求失败 (HTTP ${status})`);
      }
      
      // 统一显示HTTP错误
      console.error('HTTP错误:', errorMessage);
      console.log('准备调用 globalMessage.error:', errorMessage);
      globalMessage.error(errorMessage);
      console.log('globalMessage.error 已调用');
      
      return Promise.reject(new Error(errorMessage));
      
    } else if (error.request) {
      // 请求已发出但没有收到响应
      errorMessage = '网络连接超时，请检查网络';
      console.error('网络错误:', errorMessage);
      globalMessage.error(errorMessage);
      return Promise.reject(new Error(errorMessage));
    } else {
      // 其他错误
      errorMessage = error.message || '请求失败';
      console.error('其他错误:', errorMessage);
      globalMessage.error(errorMessage);
      return Promise.reject(new Error(errorMessage));
    }
  }
);

export default request;