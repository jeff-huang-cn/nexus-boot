# 环境变量配置说明

## 配置文件

在项目根目录创建以下文件：

### `.env.development` (开发环境)
```bash
# 后端服务地址
REACT_APP_API_BASE_URL=http://localhost:8080

# API统一前缀（所有接口都会自动加上这个前缀）
REACT_APP_API_PREFIX=/api
```

### `.env.production` (生产环境)
```bash
# 后端服务地址
REACT_APP_API_BASE_URL=https://your-domain.com

# API统一前缀
REACT_APP_API_PREFIX=/api/v1
```

## 配置说明

### `REACT_APP_API_BASE_URL`
- 后端服务器地址
- 开发环境：`http://localhost:8080`
- 生产环境：`https://your-domain.com`

### `REACT_APP_API_PREFIX`
- 统一的API前缀，所有API请求都会自动加上
- 默认值：`/api`
- 可以根据版本切换：`/api/v1`, `/api/v2`

## 扩展配置

如果有独立的文件上传服务器，可以在 `request.ts` 中创建新的 axios 实例：

```typescript
// 文件上传服务
const UPLOAD_BASE_URL = process.env.REACT_APP_UPLOAD_BASE_URL || 'http://localhost:9000';

export const uploadRequest = axios.create({
  baseURL: UPLOAD_BASE_URL,
  timeout: 30000, // 上传超时时间更长
});
```

然后在对应的 service 中使用：

```typescript
// services/upload.ts
import { uploadRequest } from '../utils/request';

const API_BASE = '/upload';

export const uploadApi = {
  uploadFile: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return uploadRequest.post(`${API_BASE}/file`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },
};
```

## 默认值

如果没有配置环境变量，系统使用以下默认值：
- `REACT_APP_API_BASE_URL`: `http://localhost:8080`
- `REACT_APP_API_PREFIX`: `/api`

