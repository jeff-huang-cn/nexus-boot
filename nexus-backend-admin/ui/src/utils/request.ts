import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api/',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

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
