# 前端Spring Security认证集成实施计划

## 实施策略

**原则**: 自底向上，先基础后应用，确保每一步都可测试

**总工时估算**: 约8-10小时（含测试）

---

## 阶段一：基础设施搭建（优先级：P0）

### 任务1: 创建Token管理工具
**文件**: `src/utils/auth.ts`

**功能清单**:
```typescript
// Token存储相关
- getToken(): string | null
- setToken(token: string): void
- removeToken(): void

// JWT解析相关
- parseJwtToken(token: string): JwtPayload | null
- isTokenExpired(token: string): boolean

// 用户信息提取
- getUserInfo(): UserInfo | null
- getPermissions(): string[]
- getRoles(): string[]

// 类型定义
interface JwtPayload {
  sub: string;           // 用户名
  userId: number;        // 用户ID
  roles: string[];       // 角色列表
  permissions: string[]; // 权限列表
  exp: number;           // 过期时间
  iat: number;           // 签发时间
}

interface UserInfo {
  userId: number;
  username: string;
  roles: string[];
}
```

**实现要点**:
- localStorage key: `nexus_auth_token`
- JWT解析使用原生方法（无需第三方库）
- Token过期判断: `exp * 1000 < Date.now()`
- 异常处理: 解析失败返回null

**预计工时**: 1小时

---

### 任务2: 创建认证API服务
**文件**: `src/services/auth.ts`

**接口定义**:
```typescript
// 登录请求
interface LoginRequest {
  username: string;
  password: string;
}

// 登录响应
interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

// API方法
export const authApi = {
  // 登录
  login(data: LoginRequest): Promise<LoginResponse>
  
  // 登出（可选，后端可能不需要）
  logout(): Promise<void>
  
  // 获取当前用户信息（从Token解析）
  getCurrentUser(): UserInfo | null
  
  // 获取用户菜单（基于权限过滤）
  getUserMenus(): Promise<Menu[]>
}
```

**实现要点**:
- 登录端点: `POST /login` （Spring Security默认）
- Content-Type: `application/json`
- 登录成功后调用 `setToken()` 存储Token
- 不通过 `request.ts`，避免拦截器影响

**预计工时**: 0.5小时

---

### 任务3: 增强Axios拦截器
**文件**: `src/utils/request.ts` （修改）

**请求拦截器增强**:
```typescript
request.interceptors.request.use(
  (config) => {
    // 1. 获取Token
    const token = getToken();
    
    // 2. 排除登录接口
    if (token && config.url !== '/login') {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // 3. 检查Token是否过期（可选，提前处理）
    if (token && isTokenExpired(token)) {
      removeToken();
      window.location.href = '/login';
      return Promise.reject(new Error('Token已过期'));
    }
    
    return config;
  },
  (error) => Promise.reject(error)
);
```

**响应拦截器增强**:
```typescript
request.interceptors.response.use(
  (response) => {
    // ... 原有逻辑
  },
  (error) => {
    // 处理401未授权
    if (error.response?.status === 401) {
      // 清除Token
      removeToken();
      
      // 显示提示（可选）
      message.error('登录已过期，请重新登录');
      
      // 跳转登录页（避免循环重定向）
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
      
      return Promise.reject(new Error('未登录或登录已过期'));
    }
    
    // 处理403无权限
    if (error.response?.status === 403) {
      message.error('无权限访问');
      return Promise.reject(new Error('无权限'));
    }
    
    // ... 原有逻辑
  }
);
```

**实现要点**:
- 自动添加 `Authorization` Header
- 401错误自动跳转登录
- 避免循环重定向（判断当前路径）
- 使用Ant Design的message组件提示

**预计工时**: 1小时

---

## 阶段二：认证上下文和状态管理（优先级：P0）

### 任务4: 创建认证上下文
**文件**: `src/contexts/AuthContext.tsx`

**上下文结构**:
```typescript
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

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(true);
  
  // 初始化：检查Token是否有效
  useEffect(() => {
    checkAuth();
  }, []);
  
  // 检查认证状态
  const checkAuth = () => {
    const token = getToken();
    if (token && !isTokenExpired(token)) {
      const user = getUserInfo();
      setUserInfo(user);
      setIsAuthenticated(true);
    } else {
      setIsAuthenticated(false);
      setUserInfo(null);
    }
    setLoading(false);
  };
  
  // 登录
  const login = async (username: string, password: string) => {
    const response = await authApi.login({ username, password });
    setToken(response.accessToken);
    checkAuth();
  };
  
  // 登出
  const logout = () => {
    removeToken();
    setIsAuthenticated(false);
    setUserInfo(null);
    // 可选：调用后端登出接口
  };
  
  return (
    <AuthContext.Provider value={{ isAuthenticated, userInfo, loading, login, logout, checkAuth }}>
      {children}
    </AuthContext.Provider>
  );
};

// Hook
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};
```

**实现要点**:
- 应用启动时自动检查Token有效性
- loading状态避免页面闪烁
- 登录成功后更新状态
- 登出清空所有状态

**预计工时**: 1.5小时

---

## 阶段三：路由守卫和登录页面（优先级：P0）

### 任务5: 创建路由守卫组件
**文件**: `src/components/RouteGuard.tsx`

**组件实现**:
```typescript
interface RouteGuardProps {
  children: React.ReactNode;
}

const RouteGuard: React.FC<RouteGuardProps> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();
  
  // 加载中显示Loading
  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" tip="加载中..." />
      </div>
    );
  }
  
  // 未登录重定向到登录页
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  
  // 已登录显示内容
  return <>{children}</>;
};

export default RouteGuard;
```

**使用方式**:
```typescript
// 在App.tsx中使用
<AuthProvider>
  <Routes>
    <Route path="/login" element={<Login />} />
    <Route path="*" element={
      <RouteGuard>
        <MenuProvider>
          <AppLayout>
            <DynamicRoutes />
          </AppLayout>
        </MenuProvider>
      </RouteGuard>
    } />
  </Routes>
</AuthProvider>
```

**实现要点**:
- 使用 `Navigate` 重定向
- 保存来源路径（登录后跳回）
- loading状态显示Spin
- 公开路由（/login）不需要守卫

**预计工时**: 1小时

---

### 任务6: 创建登录页面
**文件**: `src/pages/Login.tsx`

**页面设计**:
```typescript
const Login: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(false);
  
  const from = location.state?.from?.pathname || '/dashboard';
  
  const onFinish = async (values: LoginRequest) => {
    setLoading(true);
    try {
      await login(values.username, values.password);
      message.success('登录成功');
      navigate(from, { replace: true });
    } catch (error) {
      message.error(error.message || '登录失败');
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="login-container">
      <Card title="Nexus 管理系统" style={{ width: 400 }}>
        <Form onFinish={onFinish}>
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>
          
          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>
          
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};
```

**样式设计**:
```css
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
```

**实现要点**:
- 使用Ant Design Form组件
- 表单验证
- 登录成功后跳转到来源页面
- 加载状态禁用按钮
- 美观的UI设计

**预计工时**: 1.5小时

---

## 阶段四：权限集成（优先级：P1）

### 任务7: 修改MenuContext获取用户菜单
**文件**: `src/contexts/MenuContext.tsx` （修改）

**修改点**:
```typescript
// 修改前
const loadMenus = async () => {
  const menuTree = await menuApi.getMenuTree(); // 获取所有菜单
  setMenus(menuTree);
};

// 修改后
const loadMenus = async () => {
  // 从JWT Token获取权限列表
  const permissions = getPermissions();
  setPermissions(permissions);
  
  // 获取用户有权限的菜单树
  const menuTree = await authApi.getUserMenus(); // 后端已过滤
  setMenus(menuTree);
  
  // 存储到localStorage（可选）
  localStorage.setItem('user_menus', JSON.stringify(menuTree));
  localStorage.setItem('user_permissions', JSON.stringify(permissions));
};
```

**实现要点**:
- 从JWT Token提取权限列表
- 调用后端接口获取用户菜单
- 后端返回已过滤的菜单树
- 菜单和权限都缓存到localStorage

**预计工时**: 0.5小时

---

### 任务8: 实现按钮权限控制
**文件**: 各业务页面 （修改）

**使用示例**:
```typescript
import { usePermission } from '@/hooks/usePermission';

const UserList: React.FC = () => {
  const { hasPermission } = usePermission();
  
  return (
    <div>
      <Space>
        {hasPermission('system:user:create') && (
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            新增用户
          </Button>
        )}
        
        {hasPermission('system:user:import') && (
          <Button icon={<ImportOutlined />} onClick={handleImport}>
            导入
          </Button>
        )}
        
        {hasPermission('system:user:export') && (
          <Button icon={<ExportOutlined />} onClick={handleExport}>
            导出
          </Button>
        )}
      </Space>
      
      <Table
        columns={[
          // ... 其他列
          {
            title: '操作',
            render: (_, record) => (
              <Space>
                {hasPermission('system:user:update') && (
                  <Button type="link" onClick={() => handleEdit(record)}>编辑</Button>
                )}
                {hasPermission('system:user:delete') && (
                  <Popconfirm title="确定删除?" onConfirm={() => handleDelete(record)}>
                    <Button type="link" danger>删除</Button>
                  </Popconfirm>
                )}
              </Space>
            ),
          },
        ]}
      />
    </div>
  );
};
```

**或使用权限按钮组件**:
```typescript
import { PermissionButton } from '@/hooks/usePermission';

<PermissionButton permission="system:user:create">
  <Button type="primary">新增用户</Button>
</PermissionButton>
```

**实现要点**:
- 使用 `usePermission` Hook
- 按钮根据权限显示/隐藏
- 表格操作列也需要权限控制
- 权限标识与后端Menu表的permission字段一致

**预计工时**: 2小时（需要修改多个页面）

---

### 任务9: 菜单权限过滤
**文件**: `src/components/Layout/AppLayout.tsx` （已自动实现）

**说明**:
由于 `MenuContext` 已经从后端获取过滤后的菜单树，`AppLayout` 组件无需修改，会自动显示用户有权限的菜单。

**验证点**:
- 不同用户登录看到不同的菜单
- 无权限的菜单不显示
- 菜单结构正确（目录、菜单层级）

**预计工时**: 0小时（已实现）

---

## 阶段五：应用集成和测试（优先级：P1）

### 任务10: 修改App.tsx集成所有模块
**文件**: `src/App.tsx` （修改）

**修改后的结构**:
```typescript
import { AuthProvider } from './contexts/AuthContext';
import { MenuProvider } from './contexts/MenuContext';
import RouteGuard from './components/RouteGuard';
import Login from './pages/Login';

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            {/* 公开路由 */}
            <Route path="/login" element={<Login />} />
            
            {/* 受保护路由 */}
            <Route path="*" element={
              <RouteGuard>
                <MenuProvider>
                  <AppLayout>
                    <DynamicRoutes />
                  </AppLayout>
                </MenuProvider>
              </RouteGuard>
            } />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ConfigProvider>
  );
}
```

**实现要点**:
- `AuthProvider` 在最外层
- `RouteGuard` 保护所有业务路由
- `MenuProvider` 在 `RouteGuard` 内部（登录后才加载）
- `/login` 路由公开访问

**预计工时**: 0.5小时

---

### 任务11: 添加用户信息和登出按钮
**文件**: `src/components/Layout/AppLayout.tsx` （修改）

**Header增强**:
```typescript
import { useAuth } from '@/contexts/AuthContext';
import { Avatar, Dropdown, Space } from 'antd';
import { UserOutlined, LogoutOutlined, SettingOutlined } from '@ant-design/icons';

const AppLayout: React.FC<AppLayoutProps> = ({ children }) => {
  const { userInfo, logout } = useAuth();
  const navigate = useNavigate();
  
  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人信息',
      onClick: () => navigate('/profile'),
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '系统设置',
      onClick: () => navigate('/settings'),
    },
    { type: 'divider' },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: () => {
        Modal.confirm({
          title: '确认退出?',
          content: '退出后需要重新登录',
          onOk: () => {
            logout();
            navigate('/login');
          },
        });
      },
    },
  ];
  
  return (
    <Layout>
      {/* ... Sider */}
      <Layout>
        <Header style={{ display: 'flex', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            {/* 折叠按钮和面包屑 */}
          </div>
          
          {/* 右侧用户信息 */}
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <Space style={{ cursor: 'pointer' }}>
              <Avatar icon={<UserOutlined />} />
              <span>{userInfo?.username}</span>
            </Space>
          </Dropdown>
        </Header>
        {/* ... Content */}
      </Layout>
    </Layout>
  );
};
```

**实现要点**:
- Header右侧显示用户头像和名称
- 下拉菜单包含登出选项
- 登出前二次确认
- 登出后跳转到登录页

**预计工时**: 1小时

---

## 阶段六：测试和优化（优先级：P2）

### 任务12: 功能测试清单

**认证流程测试**:
- [ ] 未登录访问业务路由 → 跳转登录页
- [ ] 正确的用户名密码登录 → 成功进入系统
- [ ] 错误的用户名密码 → 显示错误提示
- [ ] 登录成功后跳转到原来要访问的页面
- [ ] 刷新页面保持登录状态
- [ ] Token过期后访问接口 → 跳转登录页
- [ ] 点击登出 → 清空Token并跳转登录页

**权限控制测试**:
- [ ] 不同用户登录看到不同的菜单
- [ ] 按钮根据权限显示/隐藏
- [ ] 无权限的按钮不显示
- [ ] 手动输入无权限的URL → 403或跳转

**边界情况测试**:
- [ ] 手动删除localStorage中的Token → 跳转登录
- [ ] Token格式错误 → 跳转登录
- [ ] 网络错误时的提示
- [ ] 同时打开多个标签页（可选）

**预计工时**: 2小时

---

### 任务13: 用户体验优化

**优化项**:
1. **加载状态优化**
   - 登录按钮loading状态
   - 应用启动时的loading提示
   - Token验证时的loading

2. **错误提示优化**
   - 登录失败显示具体原因
   - Token过期友好提示
   - 网络错误提示

3. **记住登录状态**
   - Token存储在localStorage
   - 刷新页面自动恢复

4. **样式美化**
   - 登录页面美化
   - Loading动画
   - 过渡动画

**预计工时**: 1小时

---

## 实施顺序总结

### 第一批（核心功能，必须完成）
1. ✅ 任务1: Token管理工具 (1h)
2. ✅ 任务2: 认证API服务 (0.5h)
3. ✅ 任务3: Axios拦截器增强 (1h)
4. ✅ 任务4: 认证上下文 (1.5h)
5. ✅ 任务5: 路由守卫 (1h)
6. ✅ 任务6: 登录页面 (1.5h)
7. ✅ 任务10: App.tsx集成 (0.5h)

**小计**: 7小时

### 第二批（权限功能，重要）
8. ✅ 任务7: MenuContext修改 (0.5h)
9. ✅ 任务8: 按钮权限控制 (2h)
10. ✅ 任务11: 用户信息和登出 (1h)

**小计**: 3.5小时

### 第三批（测试优化，建议）
11. ✅ 任务12: 功能测试 (2h)
12. ✅ 任务13: 用户体验优化 (1h)

**小计**: 3小时

---

## 总计工时

**开发时间**: 10.5小时  
**测试时间**: 2小时  
**总计**: 12.5小时（约2个工作日）

---

## 依赖关系图

```
任务1 (Token工具) ─┬─→ 任务2 (认证API) ─┬─→ 任务4 (认证上下文) → 任务5 (路由守卫) ─┐
                   │                    │                                     │
                   └─→ 任务3 (拦截器)  ─┘                                     │
                                                                              │
任务6 (登录页面) ─────────────────────────────────────────────────────────────┤
                                                                              │
                                                                              ├→ 任务10 (集成) → 测试
                                                                              │
任务7 (MenuContext) ─→ 任务8 (按钮权限) ─────────────────────────────────────┤
                                                                              │
任务11 (用户信息) ─────────────────────────────────────────────────────────────┘
```

---

## 后端配置确认清单

在开始前端开发前，请确认后端已完成以下配置：

- [ ] `/login` 端点已配置并返回JWT Token
- [ ] JWT Token包含必要字段: userId, username, roles, permissions
- [ ] JWT使用RS256算法
- [ ] CORS配置允许前端域名
- [ ] `GET /api/user/menus` 接口已实现（返回用户有权限的菜单）
- [ ] Token验证正常工作
- [ ] 401/403错误响应格式统一

---

## 风险提示

1. **Token存储安全性**
   - localStorage可被XSS攻击窃取
   - 建议生产环境使用httpOnly Cookie

2. **Token过期处理**
   - 当前方案: 过期跳转登录
   - 改进方案: Refresh Token自动续期

3. **多标签页同步**
   - 当前未实现
   - 可通过 localStorage 事件监听实现

4. **并发请求问题**
   - Token刷新时的并发请求处理
   - 建议使用请求队列

---

## 成功标准

✅ 未登录无法访问业务页面  
✅ 登录成功后显示用户菜单和权限  
✅ Token过期自动跳转登录页  
✅ 按钮根据权限显示/隐藏  
✅ 刷新页面保持登录状态  
✅ 用户可以正常登出  
✅ 所有测试用例通过

---

## 下一步

请Review本方案和计划，确认无误后我将开始实施。

**建议Review重点**:
1. 整体架构设计是否合理？
2. 实施顺序是否合适？
3. 是否有遗漏的功能？
4. 后端配置是否已就绪？

