# JWT 与 JWK 关联机制详解

## 📖 概述

本文档详细说明JWT（JSON Web Token）如何与JWK（JSON Web Key）关联，以及密钥选择和验证的完整流程。

---

## 🔑 核心概念

### 1. JWT 结构

```
Header.Payload.Signature

Header: {
  "kid": "abc-123-456",  ← Key ID，指向用于签名的JWK
  "alg": "RS256",        ← 算法
  "typ": "JWT"
}

Payload: {
  "sub": "admin",
  "userId": 1,
  "exp": 1234567890,
  ...
}
```

### 2. JWKSet 结构

```json
{
  "keys": [
    {
      "kid": "key-2024-01",  ← 最新的JWK（用于签名新JWT）
      "kty": "RSA",
      "alg": "RS256",
      "n": "...",  // 公钥
      "d": "...",  // 私钥（只在签名时使用）
      ...
    },
    {
      "kid": "key-2023-12",  ← 旧的JWK（仍可验证旧JWT）
      "kty": "RSA",
      "alg": "RS256",
      ...
    }
  ]
}
```

---

## 🔄 完整流程

### 场景1：生成JWT（登录）

```
1. 用户登录成功
   ↓
2. JwtTokenGenerator.generateToken()
   ↓
3. NimbusJwtEncoder.encode()
   ├─ 从 JWKSource 获取 JWKSet
   ├─ 选择第一个JWK（按创建时间倒序，所以是最新的）
   ├─ 在JWT Header中自动设置 kid = "key-2024-01"
   └─ 使用JWK的私钥签名JWT
   ↓
4. 返回JWT给客户端：
   Header: { "kid": "key-2024-01", "alg": "RS256" }
   Payload: { "userId": 1, "exp": ... }
   Signature: [使用 key-2024-01 的私钥签名]
```

### 场景2：验证JWT（访问受保护资源）

```
1. 客户端发送请求，携带JWT
   Authorization: Bearer eyJhbGc...
   ↓
2. JwtAuthenticationFilter 拦截
   ↓
3. NimbusJwtDecoder.decode(token)
   ├─ 解析JWT Header，提取 kid = "key-2024-01"
   ├─ 从 JWKSource 获取 JWKSet
   ├─ 根据 kid 查找匹配的JWK
   │  └─ 遍历 JWKSet.keys，找到 kid="key-2024-01" 的JWK
   └─ 使用该JWK的公钥验证JWT签名
   ↓
4. 验证结果：
   成功 ✅ → 继续处理请求
   失败 ❌ → 返回 401 Unauthorized
```

---

## 🔄 密钥轮换场景

### 为什么要支持多个活跃的JWK？

```
时间线：
2024-01-01: 创建 key-2024-01
2024-01-15: 用户登录，获得JWT（kid=key-2024-01，有效期2小时）

2024-01-16: 密钥轮换，创建 key-2024-02
            ↓
            现在 JWKSet 包含两个JWK:
            1. key-2024-02 (最新，用于签名新JWT)
            2. key-2024-01 (旧的，但仍可验证旧JWT)

2024-01-16: 用户A 用旧JWT访问（kid=key-2024-01）
            ✅ 验证成功（因为 key-2024-01 仍在 JWKSet 中）

2024-01-16: 用户B 重新登录，获得新JWT（kid=key-2024-02）
            ✅ 新JWT使用最新的密钥

2024-02-01: key-2024-01 过期，从 JWKSet 移除
            旧JWT（kid=key-2024-01）验证失败 ❌
```

---

## 🎯 关键实现细节

### 1. JWK 排序规则

```java
// JwkService.java
.orderByDesc(JwkDO::getCreatedTime)  // 最新的JWK排在最前面
```

**原因**：
- `NimbusJwtEncoder` 选择**第一个** JWK 用于签名新JWT
- 确保使用最新的密钥签名

### 2. JWK 查询条件

```java
.eq(JwkDO::getIsActive, true)         // 只查询活跃的
.gt(JwkDO::getExpiresAt, LocalDateTime.now())  // 且未过期的
```

**结果**：
- 密钥轮换期间，可能返回2个JWK（新旧各一个）
- 旧JWT仍可验证，新JWT使用新密钥

### 3. JWT Header 中的 kid 自动设置

```java
// 生成JWT时，不需要手动设置kid
JwtClaimsSet claims = JwtClaimsSet.builder()
    .subject("admin")
    .claim("userId", 1)
    .build();

// NimbusJwtEncoder 会自动：
// 1. 从JWKSet选择第一个JWK
// 2. 在Header中设置 kid = jwk.getKeyID()
// 3. 使用该JWK的私钥签名
return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
```

### 4. JWT 验证时的 kid 自动匹配

```java
// 验证JWT时，不需要手动解析kid
Jwt jwt = jwtDecoder.decode(token);

// NimbusJwtDecoder 会自动：
// 1. 从JWT Header提取 kid
// 2. 从JWKSet中查找 kid 匹配的JWK
// 3. 使用该JWK的公钥验证签名
```

---

## ❓ 常见问题

### Q1: 如果JWKSet中有3个活跃的JWK，会使用哪一个签名新JWT？

**A**: 使用**第一个**（按创建时间倒序排列，所以是最新的）

### Q2: 如果JWT的kid在JWKSet中找不到，会怎样？

**A**: 验证失败，抛出异常：
```
Signed JWT rejected: Another algorithm expected, or no matching key(s) found
```

### Q3: 是否只会有一个JWK生效？

**A**: 不是！通常情况：
- **正常运行**: 只有1个活跃的JWK
- **密钥轮换期间**: 可能有2个活跃的JWK（新旧各一个）
- **签名新JWT**: 始终使用最新的JWK
- **验证JWT**: 根据JWT Header中的kid选择对应的JWK

### Q4: 为什么不在数据库中只保留一个JWK？

**A**: 支持**平滑的密钥轮换**：
```
单JWK方案：
轮换密钥 → 旧JWT立即失效 ❌ → 所有用户被强制登出

多JWK方案：
轮换密钥 → 旧JWT仍可验证 ✅ → 用户无感知过渡
           等旧JWT自然过期或旧JWK过期后再移除
```

---

## 🔐 安全建议

1. **定期轮换密钥**: 建议每30-90天轮换一次
2. **合理的过期时间**: JWT有效期（2小时）< JWK有效期（365天）
3. **保护私钥**: JWK的私钥只在内存和数据库中，不对外暴露
4. **监控异常**: 监控"no matching key"错误，可能是密钥管理问题

---

## 📊 数据流图

```
┌─────────────────────────────────────────────────────────────┐
│                        JWK 生命周期                          │
└─────────────────────────────────────────────────────────────┘

创建 → 使用（签名新JWT） → 轮换（仍可验证旧JWT） → 过期（删除）
  ↓                          ↓                       ↓
system_jwk              system_jwk                system_jwk
key-2024-01 (active)    key-2024-01 (active)      key-2024-01 (删除)
                        key-2024-02 (active,新)    key-2024-02 (active)


┌─────────────────────────────────────────────────────────────┐
│                    JWT 验证流程                               │
└─────────────────────────────────────────────────────────────┘

   JWT Request
       ↓
   提取 Header.kid
       ↓
   从 Redis/DB 加载 JWKSet
       ↓
   根据 kid 查找 JWK
       ↓
   ┌─────────┐
   │ 找到？  │
   └─────────┘
     ↙     ↘
   是       否
    ↓        ↓
  验证签名   报错: no matching key
    ↓
  ┌─────────┐
  │ 签名有效？│
  └─────────┘
    ↙     ↘
  成功     失败
   ↓       ↓
  允许访问  401
```

---

## 📝 总结

1. **JWT Header 的 kid** 由 `NimbusJwtEncoder` 自动设置
2. **JWK 的选择** 基于创建时间倒序（最新的优先）
3. **JWT 验证** 根据 kid 自动匹配对应的 JWK
4. **支持多个活跃的JWK** 实现平滑的密钥轮换
5. **整个过程自动化**，开发者无需手动管理 kid

---

## 🔗 相关代码

- `JwtTokenGenerator.java` - JWT生成
- `JwtAuthenticationFilter.java` - JWT验证
- `JwkService.java` - JWK管理（加载、缓存、轮换）
- `JwkConfig.java` - JWKSource配置

---

**最后更新**: 2025-10-05
**作者**: AI Assistant
