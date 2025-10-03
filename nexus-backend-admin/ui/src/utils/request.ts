import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

// 从环境变量读取配置
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
const API_PREFIX = process.env.REACT_APP_API_PREFIX || '/admin';

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
    // 在发送请求之前做些什么
    console.log('Request:', config);
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
      throw new Error(data.message || '请求失败');
    }
  },
  (error) => {
    // 对响应错误做点什么
    console.error('Response Error:', error);
    
    let message = '网络错误';
    if (error.response) {
      // 服务器返回错误状态码
      message = error.response.data?.message || `请求失败 ${error.response.status}`;
    } else if (error.request) {
      // 请求已发出但没有收到响应
      message = '网络连接超时';
    } else {
      // 其他错误
      message = error.message || '请求失败';
    }
    
    throw new Error(message);
  }
);

export default request;