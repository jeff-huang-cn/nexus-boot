/**
 * Token管理和JWT解析工具
 * 
 * @author nexus
 */

// localStorage key
const TOKEN_KEY = 'nexus_auth_token';

// JWT Payload接口（根据后端实际返回调整）
export interface JwtPayload {
  sub: string;              // 用户名
  userId?: number;          // 用户ID（可选）
  authorities: string[];    // 权限列表（按钮权限）
  exp: number;              // 过期时间（秒级时间戳）
  iat: number;              // 签发时间（秒级时间戳）
}

// 用户信息接口
export interface UserInfo {
  userId?: number;
  username: string;
}

// ==================== Token存储管理 ====================

/**
 * 获取Token
 */
export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY);
};

/**
 * 存储Token
 */
export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token);
};

/**
 * 清除Token
 */
export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY);
};

// ==================== JWT解析 ====================

/**
 * 解析JWT Token（原生实现，无需第三方库）
 * 
 * @param token JWT Token字符串
 * @returns Payload对象，解析失败返回null
 */
export const parseJwtToken = (token: string): JwtPayload | null => {
  try {
    // JWT格式: Header.Payload.Signature
    const parts = token.split('.');
    if (parts.length !== 3) {
      console.error('Invalid JWT format');
      return null;
    }

    // 解码Payload（Base64Url编码）
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    
    // Base64解码
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );

    return JSON.parse(jsonPayload) as JwtPayload;
  } catch (error) {
    console.error('Failed to parse JWT token:', error);
    return null;
  }
};

/**
 * 检查Token是否过期
 * 
 * @param token JWT Token字符串
 * @returns true=已过期，false=未过期
 */
export const isTokenExpired = (token: string): boolean => {
  const payload = parseJwtToken(token);
  if (!payload || !payload.exp) {
    return true;
  }

  // exp是秒级时间戳，需要转换为毫秒
  const expirationTime = payload.exp * 1000;
  const currentTime = Date.now();

  return currentTime >= expirationTime;
};

// ==================== 用户信息提取 ====================

/**
 * 从Token获取用户信息
 * 
 * @returns 用户信息，失败返回null
 */
export const getUserInfo = (): UserInfo | null => {
  const token = getToken();
  if (!token || isTokenExpired(token)) {
    return null;
  }

  const payload = parseJwtToken(token);
  if (!payload) {
    return null;
  }

  return {
    userId: payload.userId,
    username: payload.sub,
  };
};

/**
 * 从Token获取权限列表
 * 
 * @returns 权限字符串数组
 */
export const getPermissions = (): string[] => {
  const token = getToken();
  if (!token || isTokenExpired(token)) {
    return [];
  }

  const payload = parseJwtToken(token);
  if (!payload || !payload.authorities) {
    return [];
  }

  return payload.authorities;
};

/**
 * 从Token获取用户名
 * 
 * @returns 用户名，失败返回null
 */
export const getUsername = (): string | null => {
  const userInfo = getUserInfo();
  return userInfo ? userInfo.username : null;
};

// ==================== Token验证 ====================

/**
 * 验证Token是否有效
 * 
 * @returns true=有效，false=无效
 */
export const isTokenValid = (): boolean => {
  const token = getToken();
  if (!token) {
    return false;
  }

  // 检查是否过期
  if (isTokenExpired(token)) {
    return false;
  }

  // 检查是否能正常解析
  const payload = parseJwtToken(token);
  return payload !== null;
};

