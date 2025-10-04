# Spring Security 登录实现方案研究与选型

> 本文档研究了 yudao 项目的 Spring Security 使用方法，对比分析了不同的登录实现方案，并为 nexus-boot 框架提供了实现建议。

## 目录
1. [Yudao登录实现分析](#1-yudao登录实现分析)
2. [Spring Security登录方案对比](#2-spring-security登录方案对比)
3. [JWT vs 数据库Token方案](#3-jwt-vs-数据库token方案)
4. [推荐实现方案](#4-推荐实现方案)
5. [具体实现步骤](#5-具体实现步骤)
6. [多种登录模式支持](#6-多种登录模式支持)

---

## 1. Yudao登录实现分析

### 1.1 核心架构

Yudao项目采用的是**手动实现登录接口**的方式，而不是使用Spring Security的默认端点。

#### 登录流程：

```
用户请求 → AuthController.login() 
       ↓
    AdminAuthService.authenticate() (验证用户名密码)
       ↓
    OAuth2TokenService.createAccessToken() (生成Token)
       ↓
    返回Token给客户端
```

#### 认证流程：

```
请求 → TokenAuthenticationFilter 
    ↓
  获取Token从请求头/参数
    ↓
  OAuth2TokenApi.checkAccessToken() (从Redis/MySQL查询Token)
    ↓
  构建LoginUser并放入SecurityContext
    ↓
  继续过滤链
```

### 1.2 关键组件

#### （1）YudaoWebSecurityConfigurerAdapter

```java:yudao-boot-mini/yudao-framework/yudao-spring-boot-starter-security/src/main/java/cn/iocoder/yudao/framework/security/config/YudaoWebSecurityConfigurerAdapter.java
@Bean
protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        // 禁用CSRF
        .csrf(AbstractHttpConfigurer::disable)
        // 无状态Session
        .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // 添加自定义Token过滤器
        .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    
    return httpSecurity.build();
}
```

**关键特点：**
- ❌ 不使用Spring Security的formLogin
- ❌ 不使用Spring Security的默认登录端点
- ✅ 使用自定义的TokenAuthenticationFilter
- ✅ 通过@PermitAll注解标记免登录接口

#### （2）AuthController - 手动实现的登录接口

```java:yudao-boot-mini/yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/controller/admin/auth/AuthController.java
@PostMapping("/login")
@PermitAll
public CommonResult<AuthLoginRespVO> login(@RequestBody @Valid AuthLoginReqVO reqVO) {
    return success(authService.login(reqVO));
}
```

**支持的登录方式：**
- `/login` - 账户密码登录
- `/sms-login` - 短信验证码登录
- `/social-login` - 社交登录（OAuth2）

#### （3）Token管理 - OAuth2TokenServiceImpl

```java:yudao-boot-mini/yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2TokenServiceImpl.java
@Override
public OAuth2AccessTokenDO createAccessToken(Long userId, Integer userType, String clientId, List<String> scopes) {
    // 创建刷新令牌
    OAuth2RefreshTokenDO refreshTokenDO = createOAuth2RefreshToken(userId, userType, clientDO, scopes);
    // 创建访问令牌（UUID生成）
    return createOAuth2AccessToken(refreshTokenDO, clientDO);
}

private static String generateAccessToken() {
    return IdUtil.fastSimpleUUID(); // 使用UUID，不是JWT
}
```

**Token存储方式：**
- 主存储：Redis（快速访问）
- 备份存储：MySQL（持久化）
- Token格式：UUID字符串（非JWT）

#### （4）TokenAuthenticationFilter - 认证过滤器

```java:yudao-boot-mini/yudao-framework/yudao-spring-boot-starter-security/src/main/java/cn/iocoder/yudao/framework/security/core/filter/TokenAuthenticationFilter.java
private LoginUser buildLoginUserByToken(String token, Integer userType) {
    OAuth2AccessTokenCheckRespDTO accessToken = oauth2TokenApi.checkAccessToken(token);
    if (accessToken == null) {
        return null;
    }
    // 构建登录用户
    return new LoginUser()
        .setId(accessToken.getUserId())
        .setUserType(accessToken.getUserType())
        .setInfo(accessToken.getUserInfo())
        .setScopes(accessToken.getScopes())
        .setExpiresTime(accessToken.getExpiresTime());
}
```

### 1.3 Yudao方案的优缺点

#### ✅ 优点：
1. **灵活性高**：完全控制登录逻辑，易于扩展多种登录方式
2. **清晰明了**：登录流程直观，易于理解和调试
3. **统一接口**：所有登录方式都通过自定义的Controller处理
4. **多种登录模式**：已经支持账户密码、短信、社交登录三种方式

#### ❌ 缺点：
1. **不使用Spring Security标准**：失去了框架提供的标准化支持
2. **Token存储成本高**：每次请求都需要查询Redis/MySQL
3. **无法利用JWT优势**：Token无状态、分布式友好等特性
4. **扩展性有限**：添加新的认证方式需要修改Controller
5. **与OAuth2 Resource Server不兼容**：无法直接使用Spring的OAuth2生态

---

## 2. Spring Security登录方案对比

### 2.1 方案一：Spring Security FormLogin（不推荐）

**特点：**
```java
http.formLogin()
    .loginPage("/login")
    .loginProcessingUrl("/login")
    .successHandler(authenticationSuccessHandler())
    .failureHandler(authenticationFailureHandler());
```

**适用场景：**
- 传统的Web应用（使用Session）
- 主要用于浏览器渲染的页面

**不推荐原因：**
- ❌ 主要设计用于Session based认证
- ❌ 对前后端分离项目支持不友好
- ❌ 对RESTful API支持有限

### 2.2 方案二：手动实现登录接口 + 自定义Filter（Yudao方案）

**特点：**
```java
@PostMapping("/login")
@PermitAll
public Result<LoginResp> login(@RequestBody LoginReq req) {
    // 手动验证
    // 生成Token
    // 返回Token
}

// 自定义Filter验证Token
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    // 从数据库验证Token
}
```

**优点：**
- ✅ 完全控制登录流程
- ✅ 易于扩展多种登录方式
- ✅ 与前后端分离架构兼容

**缺点：**
- ❌ 不使用Spring Security标准
- ❌ Token验证需要查询数据库/Redis
- ❌ 无法利用OAuth2生态

### 2.3 方案三：OAuth2 Resource Server + JWT（推荐）

**特点：**
```java
http.oauth2ResourceServer()
    .jwt(jwt -> jwt
        .decoder(jwtDecoder())
        .jwtAuthenticationConverter(jwtAuthenticationConverter())
    );
```

**优点：**
- ✅ Spring Security标准方案
- ✅ JWT无状态，不需要存储Session/Token
- ✅ 分布式友好，易于扩展
- ✅ 与OAuth2生态完全兼容
- ✅ 支持JWK，安全性高

**缺点：**
- ⚠️ 学习曲线稍陡
- ⚠️ 需要理解JWT和OAuth2概念

---

## 3. JWT vs 数据库Token方案

### 3.1 数据库Token方案（Yudao方案）

#### 工作原理：
```
客户端请求 → 携带Token
          ↓
    从Redis查询Token
          ↓
    Token存在且有效？
          ↓ Yes
    从Token获取用户信息
          ↓
    放入SecurityContext
```

#### 特点分析：

| 方面 | 说明 |
|------|------|
| **存储** | Redis（主） + MySQL（备份） |
| **验证方式** | 每次请求查询数据库 |
| **性能** | Redis性能高，但仍需网络IO |
| **可撤销性** | ✅ 强：可以即时撤销Token |
| **分布式** | ⚠️ 需要共享Redis |
| **扩展性** | ⚠️ 需要维护Token表 |

#### 优点：
- ✅ **强可撤销性**：删除数据库记录即可撤销Token
- ✅ **灵活性高**：可以随时修改Token属性
- ✅ **易于理解**：概念简单，类似Session

#### 缺点：
- ❌ **性能开销**：每次请求都需要查询数据库/Redis
- ❌ **存储成本**：需要存储大量Token记录
- ❌ **分布式依赖**：必须共享同一个Redis/数据库
- ❌ **扩展性限制**：Token数量增长影响性能

### 3.2 JWT方案（推荐）

#### 工作原理：
```
客户端请求 → 携带JWT
          ↓
    使用公钥验证签名
          ↓
    签名有效且未过期？
          ↓ Yes
    从JWT Claims获取用户信息
          ↓
    放入SecurityContext
```

#### 特点分析：

| 方面 | 说明 |
|------|------|
| **存储** | 不需要服务器存储 |
| **验证方式** | 本地验证签名，无需查询数据库 |
| **性能** | 极高，纯内存操作 |
| **可撤销性** | ⚠️ 弱：需要额外机制（黑名单等） |
| **分布式** | ✅ 完美支持，无状态 |
| **扩展性** | ✅ 优秀，无存储压力 |

#### JWT结构：

```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.signature
│────────── Header ──────────│───────────── Payload ────────────│─ Signature ─│
```

**Header（头部）：**
```json
{
  "alg": "RS256",  // RSA算法
  "typ": "JWT"
}
```

**Payload（载荷）：**
```json
{
  "sub": "1234567890",           // 用户ID
  "username": "admin",
  "authorities": ["ROLE_ADMIN"], // 权限信息
  "iat": 1516239022,             // 签发时间
  "exp": 1516242622              // 过期时间
}
```

**Signature（签名）：**
```
RSASHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  privateKey
)
```

#### 优点：
- ✅ **无状态**：不需要服务器存储任何信息
- ✅ **高性能**：本地验证，无数据库查询
- ✅ **分布式友好**：多个服务共享公钥即可
- ✅ **标准化**：RFC 7519标准，生态完善
- ✅ **可携带信息**：可以在Token中携带用户信息和权限

#### 缺点：
- ❌ **不可撤销**：Token签发后无法即时撤销（解决方案见下文）
- ❌ **载荷不能太大**：影响网络传输
- ❌ **时间同步**：服务器之间需要时间同步

#### Token撤销解决方案：

**方案1：短期Token + Refresh Token**
```
Access Token: 15分钟过期（存储在内存）
Refresh Token: 7天过期（存储在数据库，可撤销）
```

**方案2：黑名单机制**
```java
// 将需要撤销的Token ID存入Redis
public void revokeToken(String jti) {
    redisTemplate.opsForSet().add("token:blacklist", jti);
    redisTemplate.expire("token:blacklist:" + jti, expirationTime);
}

// 验证时检查黑名单
public boolean isTokenRevoked(String jti) {
    return redisTemplate.opsForSet().isMember("token:blacklist", jti);
}
```

**方案3：版本号机制**
```java
// 用户信息中存储Token版本号
@Entity
public class User {
    private Long tokenVersion; // 每次重置密码或登出时+1
}

// JWT中包含版本号
{
  "sub": "123",
  "tokenVersion": 5  // 验证时比对
}
```

### 3.3 方案对比总结

| 特性 | 数据库Token | JWT |
|------|------------|-----|
| **性能** | 中等（需要查询Redis） | 高（纯内存验证） |
| **可撤销性** | 强（即时生效） | 弱（需要额外机制） |
| **分布式** | 需要共享存储 | 完美支持 |
| **扩展性** | 一般（存储压力） | 优秀（无状态） |
| **实现复杂度** | 简单 | 中等 |
| **标准化** | 无 | 有（RFC标准） |
| **生态支持** | 有限 | 丰富 |

**推荐场景：**
- **使用数据库Token：** 如果需要强撤销能力（如金融、支付系统）
- **使用JWT：** 大多数场景，特别是分布式、高并发系统

---

## 4. 推荐实现方案

### 4.1 技术选型

基于以上分析，**推荐采用以下方案**：

```
✅ OAuth2 Resource Server + JWT + JWK (RS256)
```

**理由：**

1. **符合现代标准**：OAuth2是业界标准，Spring Security原生支持
2. **高性能**：JWT无状态，无需查询数据库
3. **分布式友好**：多个服务共享JWK即可
4. **易于扩展**：后续可以无缝集成OAuth2授权登录
5. **安全性高**：RSA非对称加密，JWK动态密钥管理

### 4.2 架构设计

```
┌─────────────┐
│  前端应用   │
└──────┬──────┘
       │ POST /api/auth/login {username, password}
       ↓
┌──────────────────────────────────┐
│  AuthController                  │
│  (手动登录端点，兼容多种登录)    │
└──────┬───────────────────────────┘
       │
       ↓
┌──────────────────────────────────┐
│  AuthenticationManager           │
│  (Spring Security标准认证管理器) │
└──────┬───────────────────────────┘
       │
       ↓
┌──────────────────────────────────┐
│  UserDetailsService              │
│  (加载用户信息)                  │
└──────┬───────────────────────────┘
       │
       ↓
┌──────────────────────────────────┐
│  JwtTokenProvider                │
│  (生成JWT，包含权限信息)         │
└──────┬───────────────────────────┘
       │
       │ 返回 JWT Token
       ↓
┌─────────────┐
│  前端应用   │
└──────┬──────┘
       │ 后续请求携带 Authorization: Bearer <JWT>
       ↓
┌──────────────────────────────────┐
│  JwtAuthenticationFilter         │
│  (Spring Security自动配置)       │
└──────┬───────────────────────────┘
       │
       ↓
┌──────────────────────────────────┐
│  JwtDecoder (使用JWK验证签名)    │
└──────┬───────────────────────────┘
       │
       ↓
┌──────────────────────────────────┐
│  JwtAuthenticationConverter      │
│  (从JWT提取权限信息)             │
└──────┬───────────────────────────┘
       │
       ↓
    SecurityContext (认证完成)
```

### 4.3 为什么不用Spring Security默认端点？

虽然推荐使用OAuth2 Resource Server + JWT，但**仍然建议保留自定义登录端点**，原因如下：

#### ❌ Spring Security默认端点的问题：

1. **formLogin主要为Session设计**
```java
http.formLogin()  // 主要用于Session based认证
```

2. **对JWT支持不友好**
   - formLogin默认处理逻辑不会生成JWT
   - 需要大量自定义才能返回JWT

3. **多种登录方式支持有限**
   - 账户密码、短信、社交登录难以统一处理
   - 需要分散到不同的端点

4. **前后端分离不友好**
   - 默认返回HTML响应
   - 需要自定义Handler才能返回JSON

#### ✅ 推荐方案：自定义登录端点 + OAuth2 Resource Server

```java
// 1. 自定义登录端点（灵活处理多种登录）
@PostMapping("/api/auth/login")
public Result<LoginResp> login(@RequestBody LoginReq req) {
    // 调用Spring Security的AuthenticationManager
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
    );
    
    // 生成JWT
    String jwt = jwtTokenProvider.generateToken(authentication);
    
    return Result.success(new LoginResp(jwt));
}

// 2. OAuth2 Resource Server验证JWT
http.oauth2ResourceServer()
    .jwt(jwt -> jwt.decoder(jwtDecoder()));
```

**这种方式的优势：**
- ✅ **登录端点灵活可控**：可以处理多种登录方式
- ✅ **使用Spring Security标准认证**：调用AuthenticationManager
- ✅ **JWT验证标准化**：使用OAuth2 Resource Server
- ✅ **易于扩展**：后续可以添加OAuth2授权登录

---

## 5. 具体实现步骤

### 5.1 Maven依赖

```xml
<dependencies>
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- OAuth2 Resource Server (包含JWT支持) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    
    <!-- JWT (Nimbus JOSE + JWT，Spring推荐) -->
    <dependency>
        <groupId>com.nimbusds</groupId>
        <artifactId>nimbus-jose-jwt</artifactId>
    </dependency>
</dependencies>
```

### 5.2 生成RSA密钥对

#### 方式1：使用Java代码生成（推荐）

```java
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

public class JwkGenerator {
    public static void main(String[] args) throws Exception {
        // 生成2048位RSA密钥对
        RSAKey rsaKey = new RSAKeyGenerator(2048)
                .keyID("nexus-jwt-key-1") // 密钥ID
                .generate();
        
        // 公钥（用于验证JWT）
        System.out.println("Public Key JWK:");
        System.out.println(rsaKey.toPublicJWK().toJSONString());
        
        // 私钥（用于签名JWT，需保密）
        System.out.println("\nPrivate Key JWK:");
        System.out.println(rsaKey.toJSONString());
    }
}
```

#### 方式2：使用OpenSSL生成

```bash
# 生成私钥
openssl genrsa -out private_key.pem 2048

# 从私钥生成公钥
openssl rsa -in private_key.pem -pubout -out public_key.pem

# 转换为PKCS#8格式（Spring需要）
openssl pkcs8 -topk8 -inform PEM -in private_key.pem -out private_key_pkcs8.pem -nocrypt
```

### 5.3 配置文件（application.yml）

```yaml
# JWT配置
jwt:
  # 私钥（用于签名，需保密，建议放在配置中心）
  private-key: |
    -----BEGIN PRIVATE KEY-----
    MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC...
    -----END PRIVATE KEY-----
  
  # 公钥（用于验证）
  public-key: |
    -----BEGIN PUBLIC KEY-----
    MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvGj...
    -----END PUBLIC KEY-----
  
  # Token过期时间（秒）
  access-token-expiration: 900  # 15分钟
  refresh-token-expiration: 604800  # 7天
  
  # 签发者
  issuer: nexus-boot
  
  # 受众
  audience: nexus-app

# Spring Security配置
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # JWK Set URI（如果使用JWK Set endpoint）
          # jwk-set-uri: http://localhost:8080/.well-known/jwks.json
          
          # 或者直接配置公钥
          public-key-location: classpath:public_key.pem
```

### 5.4 核心代码实现

#### （1）JWT配置类

```java
package com.nexus.framework.security.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Value("${jwt.private-key}")
    private String privateKeyStr;

    @Value("${jwt.public-key}")
    private String publicKeyStr;

    /**
     * JWT解码器（用于验证JWT）
     */
    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        RSAPublicKey publicKey = loadPublicKey(publicKeyStr);
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    /**
     * JWT编码器（用于生成JWT）
     */
    @Bean
    public JwtEncoder jwtEncoder() throws Exception {
        RSAPublicKey publicKey = loadPublicKey(publicKeyStr);
        RSAPrivateKey privateKey = loadPrivateKey(privateKeyStr);
        
        JWK jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("nexus-jwt-key-1")
                .build();
        
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    /**
     * 加载RSA公钥
     */
    private RSAPublicKey loadPublicKey(String keyStr) throws Exception {
        String publicKeyPEM = keyStr
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    /**
     * 加载RSA私钥
     */
    private RSAPrivateKey loadPrivateKey(String keyStr) throws Exception {
        String privateKeyPEM = keyStr
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}
```

#### （2）JWT Token提供者

```java
package com.nexus.framework.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * JWT Token提供者
 * 负责生成和刷新JWT Token
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.access-token-expiration:900}")
    private long accessTokenExpiration;

    @Value("${jwt.issuer:nexus-boot}")
    private String issuer;

    @Value("${jwt.audience:nexus-app}")
    private String audience;

    /**
     * 生成Access Token
     */
    public String generateAccessToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenExpiration);

        // 获取用户权限
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 构建JWT Claims
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(java.util.List.of(audience))
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(expiry)
                // ⚠️ 关键：手动添加权限信息到JWT
                .claim("authorities", authorities)
                // 可以添加更多自定义字段
                .claim("tokenType", "access")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * 生成Refresh Token（过期时间更长）
     */
    public String generateRefreshToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(604800); // 7天

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(java.util.List.of(audience))
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(expiry)
                .claim("tokenType", "refresh")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
```

#### （3）JWT认证转换器（从JWT提取权限）

```java
package com.nexus.framework.security.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * JWT认证转换器
 * 从JWT中提取权限信息并转换为Spring Security的Authentication对象
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // ⚠️ 关键：从JWT的authorities claim中提取权限
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        
        // 创建Authentication对象
        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * 从JWT中提取权限信息
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // 从JWT的authorities claim中提取
        String authoritiesStr = jwt.getClaimAsString("authorities");
        
        if (authoritiesStr == null || authoritiesStr.isEmpty()) {
            return Collections.emptyList();
        }

        // 将逗号分隔的权限字符串转换为GrantedAuthority集合
        return Arrays.stream(authoritiesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
```

#### （4）Security配置

```java
package com.nexus.framework.security.config;

import com.nexus.framework.security.jwt.JwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true) // 启用方法级权限控制
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    /**
     * Security过滤链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（使用JWT不需要CSRF保护）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 禁用Session（JWT是无状态的）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置URL权限
            .authorizeHttpRequests(auth -> auth
                // 允许登录接口匿名访问
                .requestMatchers("/api/auth/**").permitAll()
                // 允许Swagger文档访问
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            
            // ⚠️ 关键：配置OAuth2 Resource Server + JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    // 使用自定义的JWT认证转换器（提取权限信息）
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
            );

        return http.build();
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     * 用于手动触发认证（登录接口中使用）
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }
}
```

#### （5）用户详情服务

```java
package com.nexus.backend.admin.service.impl;

import com.nexus.backend.admin.entity.User;
import com.nexus.backend.admin.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * 用户详情服务
 * 用于Spring Security加载用户信息
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库加载用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 加载用户权限（这里假设User实体中有getRoles()方法）
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        // 返回Spring Security的UserDetails
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getStatus() != 1)
                .build();
    }
}
```

#### （6）登录控制器

```java
package com.nexus.backend.admin.controller;

import com.nexus.backend.admin.dto.LoginRequest;
import com.nexus.backend.admin.dto.LoginResponse;
import com.nexus.framework.security.jwt.JwtTokenProvider;
import com.nexus.framework.web.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 * 处理登录、登出、刷新Token等操作
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 账户密码登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // ⚠️ 关键：使用Spring Security的AuthenticationManager进行认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 生成JWT Token
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // 返回Token
        return Result.success(LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900) // 15分钟
                .build());
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestParam String refreshToken) {
        // TODO: 验证refresh token并生成新的access token
        return Result.success(null);
    }

    /**
     * 登出
     * 如果使用JWT黑名单机制，在这里将token加入黑名单
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        // TODO: 如果需要，将token加入黑名单
        return Result.success();
    }
}
```

#### （7）DTO类

```java
// LoginRequest.java
package com.nexus.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    // 可以添加验证码等字段
    private String captcha;
}

// LoginResponse.java
package com.nexus.backend.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
}
```

### 5.5 如何在业务代码中获取当前用户信息

#### 方式1：使用SecurityContextHolder

```java
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class SomeService {
    
    public void someMethod() {
        // 获取Authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 获取用户名
        String username = authentication.getName();
        
        // 获取JWT对象（可以访问所有claims）
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getClaimAsString("sub");
        
        // 获取权限
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    }
}
```

#### 方式2：使用@AuthenticationPrincipal注解（推荐）

```java
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    
    @GetMapping("/api/user/profile")
    public Result<UserProfile> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();
        String authorities = jwt.getClaimAsString("authorities");
        
        // 返回用户信息
        return Result.success(new UserProfile(username, authorities));
    }
}
```

#### 方式3：使用现有的SecurityContextUtils（需要适配）

```java
// 需要更新SecurityContextUtils以支持JWT
public class SecurityContextUtils {
    
    public static Long getLoginUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return null;
        }
        
        // 如果是JWT认证
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userId = jwt.getSubject(); // 或者从自定义claim获取
            return Long.parseLong(userId);
        }
        
        return null;
    }
}
```

---

## 6. 多种登录模式支持

### 6.1 架构设计

采用**统一的认证入口 + 多个认证提供者**的架构：

```
┌─────────────────────────────────────────────┐
│         AuthController (统一入口)          │
├─────────────────────────────────────────────┤
│  /api/auth/login        (账户密码)         │
│  /api/auth/sms-login    (短信验证码)       │
│  /api/auth/social-login (社交登录)         │
└─────────────┬───────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────────┐
│      AuthenticationManager (认证管理器)     │
└─────────────┬───────────────────────────────┘
              │
              ├────────────────────┬────────────────────┐
              ↓                    ↓                    ↓
┌───────────────────────┐  ┌──────────────────┐  ┌─────────────────┐
│DaoAuthenticationProvider│  │SmsAuthProvider  │  │OAuth2AuthProvider│
│  (账户密码)            │  │  (短信验证码)    │  │  (社交登录)      │
└───────────────────────┘  └──────────────────┘  └─────────────────┘
```

### 6.2 实现多个认证提供者

#### （1）短信验证码认证

```java
// SmsAuthenticationToken.java
public class SmsAuthenticationToken extends AbstractAuthenticationToken {
    private final String mobile;
    private final String code;

    public SmsAuthenticationToken(String mobile, String code) {
        super(null);
        this.mobile = mobile;
        this.code = code;
        setAuthenticated(false);
    }

    // ... getter方法
}

// SmsAuthenticationProvider.java
@Component
@RequiredArgsConstructor
public class SmsAuthenticationProvider implements AuthenticationProvider {

    private final SmsCodeService smsCodeService;
    private final UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken token = (SmsAuthenticationToken) authentication;
        
        // 验证短信验证码
        if (!smsCodeService.verify(token.getMobile(), token.getCode())) {
            throw new BadCredentialsException("验证码错误");
        }
        
        // 加载用户信息
        UserDetails userDetails = userDetailsService.loadUserByUsername(token.getMobile());
        
        // 返回已认证的Token
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```

#### （2）社交登录认证

```java
// SocialAuthenticationToken.java
public class SocialAuthenticationToken extends AbstractAuthenticationToken {
    private final String socialType; // 如：wechat, qq, github
    private final String code;
    private final String state;

    // ... 构造方法和getter
}

// SocialAuthenticationProvider.java
@Component
@RequiredArgsConstructor
public class SocialAuthenticationProvider implements AuthenticationProvider {

    private final SocialUserService socialUserService;
    private final UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SocialAuthenticationToken token = (SocialAuthenticationToken) authentication;
        
        // 通过社交平台的code获取用户信息
        SocialUser socialUser = socialUserService.getUserByCode(
                token.getSocialType(), token.getCode(), token.getState());
        
        // 查找或创建系统用户
        UserDetails userDetails = userDetailsService.loadUserByUsername(socialUser.getOpenId());
        
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SocialAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```

#### （3）配置多个认证提供者

```java
@Bean
public AuthenticationManager authenticationManager() {
    // 创建多个认证提供者
    DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
    daoProvider.setUserDetailsService(userDetailsService);
    daoProvider.setPasswordEncoder(passwordEncoder());

    // 返回支持多个Provider的AuthenticationManager
    return new ProviderManager(
            daoProvider,
            smsAuthenticationProvider,
            socialAuthenticationProvider
    );
}
```

#### （4）统一的登录控制器

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 账户密码登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        return buildLoginResponse(authentication);
    }

    /**
     * 短信验证码登录
     */
    @PostMapping("/sms-login")
    public Result<LoginResponse> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new SmsAuthenticationToken(
                        request.getMobile(),
                        request.getCode()
                )
        );
        return buildLoginResponse(authentication);
    }

    /**
     * 社交登录
     */
    @PostMapping("/social-login")
    public Result<LoginResponse> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new SocialAuthenticationToken(
                        request.getType(),
                        request.getCode(),
                        request.getState()
                )
        );
        return buildLoginResponse(authentication);
    }

    /**
     * 构建登录响应（复用）
     */
    private Result<LoginResponse> buildLoginResponse(Authentication authentication) {
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        return Result.success(LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .build());
    }
}
```

### 6.3 是否可以共用一个登录端点？

**答案：可以，但不推荐。**

#### 方案1：单一端点 + 类型字段（不推荐）

```java
@PostMapping("/login")
public Result<LoginResponse> login(@RequestBody UnifiedLoginRequest request) {
    Authentication authentication;
    
    switch (request.getLoginType()) {
        case "password":
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()));
            break;
        case "sms":
            authentication = authenticationManager.authenticate(
                    new SmsAuthenticationToken(
                            request.getMobile(), request.getCode()));
            break;
        case "social":
            authentication = authenticationManager.authenticate(
                    new SocialAuthenticationToken(
                            request.getSocialType(), request.getCode(), request.getState()));
            break;
        default:
            throw new IllegalArgumentException("不支持的登录类型");
    }
    
    return buildLoginResponse(authentication);
}
```

**缺点：**
- ❌ 请求参数不统一，需要传递很多可选字段
- ❌ 参数验证复杂，需要根据类型动态验证
- ❌ API文档混乱，难以理解
- ❌ 扩展性差，添加新登录方式需要修改代码

#### 方案2：多个端点（推荐）

```java
@PostMapping("/login")          // 账户密码
@PostMapping("/sms-login")      // 短信验证码
@PostMapping("/social-login")   // 社交登录
```

**优点：**
- ✅ 职责清晰，每个端点处理一种登录方式
- ✅ 参数验证简单，每个请求都有明确的DTO
- ✅ API文档清晰
- ✅ 易于扩展，添加新登录方式只需新增端点

---

## 7. 关键问题解答

### 7.1 授权信息如何放入JWT中？

**答案：需要手动添加。**

Spring Security默认生成的JWT**不会自动包含权限信息**，需要在生成JWT时手动添加：

```java
public String generateAccessToken(Authentication authentication) {
    // ⚠️ 手动添加权限信息
    String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

    JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(authentication.getName())
            // ⚠️ 关键：添加authorities claim
            .claim("authorities", authorities)
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
}
```

然后在JWT认证转换器中提取：

```java
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // ⚠️ 从authorities claim中提取权限
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }
}
```

### 7.2 同一个login端点是否支持2种登录模式？

**答案：可以，但不推荐。**

技术上可以实现（见6.3节），但有以下问题：
- 请求参数复杂
- 参数验证困难
- API文档混乱
- 扩展性差

**推荐：使用多个端点，每个端点处理一种登录方式。**

### 7.3 扩展授权登录是否会有影响？

**答案：使用推荐方案，不会有影响。**

采用**OAuth2 Resource Server + JWT + 多个AuthenticationProvider**的架构：

1. **当前阶段**：实现账户密码、短信验证码登录
2. **扩展阶段**：添加OAuth2授权登录
   - 添加新的`OAuth2AuthenticationProvider`
   - 添加新的端点`/api/auth/oauth2-login`
   - 不影响现有的登录方式

```java
// 扩展时只需添加新的Provider和端点
@Bean
public AuthenticationManager authenticationManager() {
    return new ProviderManager(
            daoProvider,           // 现有
            smsProvider,           // 现有
            oauth2Provider         // ⬅️ 新增
    );
}
```

### 7.4 JWT vs 数据库Token，如何选择？

**推荐：JWT**

| 场景 | 推荐方案 | 理由 |
|------|---------|------|
| **通用场景** | JWT | 高性能、分布式友好 |
| **高并发系统** | JWT | 无状态，减少数据库压力 |
| **微服务架构** | JWT | 服务间共享公钥即可 |
| **金融/支付系统** | 数据库Token | 需要强撤销能力 |
| **内部管理系统** | JWT + 黑名单 | 平衡性能和安全 |

---

## 8. 最终推荐方案总结

### 8.1 技术栈选择

```
✅ OAuth2 Resource Server (Spring Security标准)
✅ JWT + JWK (RS256签名算法)
✅ 自定义登录端点 + AuthenticationManager
✅ 多个AuthenticationProvider (支持多种登录方式)
✅ 短期Access Token + 长期Refresh Token
```

### 8.2 为什么不用yudao的方案？

| 对比项 | Yudao方案 | 推荐方案 |
|-------|----------|---------|
| **标准化** | 自定义实现 | Spring Security标准 |
| **性能** | 每次查询数据库/Redis | JWT本地验证 |
| **分布式** | 需要共享存储 | 完美支持 |
| **可撤销性** | 强（即时生效） | 中（需要额外机制） |
| **扩展性** | 一般 | 优秀 |
| **生态支持** | 有限 | 丰富（OAuth2生态） |

### 8.3 实施步骤

1. **Phase 1：基础设施**
   - ✅ 配置Maven依赖
   - ✅ 生成RSA密钥对
   - ✅ 配置JwtConfig和SecurityConfig

2. **Phase 2：账户密码登录**
   - ✅ 实现UserDetailsService
   - ✅ 实现JwtTokenProvider
   - ✅ 实现AuthController.login()

3. **Phase 3：JWT认证**
   - ✅ 配置OAuth2 Resource Server
   - ✅ 实现JwtAuthenticationConverter
   - ✅ 测试JWT验证流程

4. **Phase 4：扩展登录方式**
   - ✅ 实现SmsAuthenticationProvider
   - ✅ 实现SocialAuthenticationProvider
   - ✅ 添加对应的登录端点

5. **Phase 5：增强功能**
   - ⚠️ 实现Refresh Token机制
   - ⚠️ 实现JWT黑名单（可选）
   - ⚠️ 实现JWK Set Endpoint（可选）

### 8.4 核心代码清单

```
nexus-framework/
├── security/
│   ├── config/
│   │   ├── JwtConfig.java              # JWT编码器/解码器配置
│   │   └── SecurityConfig.java         # Spring Security配置
│   ├── jwt/
│   │   ├── JwtTokenProvider.java       # JWT生成器
│   │   └── JwtAuthenticationConverter.java  # JWT认证转换器
│   └── provider/
│       ├── SmsAuthenticationProvider.java    # 短信登录Provider
│       └── SocialAuthenticationProvider.java # 社交登录Provider

nexus-backend-admin/
├── controller/
│   └── AuthController.java            # 登录端点
├── service/
│   └── UserDetailsServiceImpl.java    # 用户详情服务
└── dto/
    ├── LoginRequest.java
    └── LoginResponse.java
```

---

## 9. 参考资源

### 官方文档
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [RFC 7519 - JWT](https://datatracker.ietf.org/doc/html/rfc7519)
- [RFC 7517 - JWK](https://datatracker.ietf.org/doc/html/rfc7517)

### 工具推荐
- [JWT.io](https://jwt.io/) - JWT在线调试工具
- [JWK Generator](https://mkjwk.org/) - JWK在线生成工具

---

## 10. 常见问题FAQ

### Q1: JWT被窃取怎么办？
**A:** 采用以下措施：
1. 使用HTTPS传输
2. 设置短过期时间（15分钟）
3. 实现Refresh Token机制
4. 实现JWT黑名单（可选）
5. 绑定IP或设备指纹（可选）

### Q2: JWT过期后如何刷新？
**A:** 使用Refresh Token机制：
```
Access Token (15分钟) + Refresh Token (7天)
当Access Token过期时，使用Refresh Token换取新的Access Token
Refresh Token存储在数据库，可以撤销
```

### Q3: 如何在前端存储JWT？
**A:** 推荐方案：
- Access Token：内存中（变量）
- Refresh Token：HttpOnly Cookie（最安全）
- ❌ 不推荐：localStorage（容易被XSS攻击）

### Q4: 如何处理用户权限变更？
**A:** JWT签发后无法修改，有以下方案：
1. 设置短过期时间（推荐）
2. 使用版本号机制
3. 实现权限变更通知机制

### Q5: 多个服务如何共享JWT验证？
**A:** 
1. 所有服务使用相同的公钥验证JWT
2. 或者提供JWK Set Endpoint，服务从该端点获取公钥

---

## 附录：完整示例项目结构

```
nexus-boot/
├── nexus-framework/
│   └── src/main/java/com/nexus/framework/
│       ├── security/
│       │   ├── config/
│       │   │   ├── JwtConfig.java
│       │   │   └── SecurityConfig.java
│       │   ├── jwt/
│       │   │   ├── JwtTokenProvider.java
│       │   │   ├── JwtAuthenticationConverter.java
│       │   │   └── JwtProperties.java
│       │   ├── provider/
│       │   │   ├── SmsAuthenticationToken.java
│       │   │   ├── SmsAuthenticationProvider.java
│       │   │   ├── SocialAuthenticationToken.java
│       │   │   └── SocialAuthenticationProvider.java
│       │   └── util/
│       │       └── SecurityContextUtils.java (更新版本)
│       └── web/
│           └── result/
│               └── Result.java
├── nexus-backend-admin/
│   └── src/main/java/com/nexus/backend/admin/
│       ├── controller/
│       │   └── AuthController.java
│       ├── service/
│       │   ├── UserDetailsServiceImpl.java
│       │   ├── SmsCodeService.java
│       │   └── SocialUserService.java
│       ├── dto/
│       │   ├── LoginRequest.java
│       │   ├── LoginResponse.java
│       │   ├── SmsLoginRequest.java
│       │   └── SocialLoginRequest.java
│       ├── entity/
│       │   └── User.java
│       └── mapper/
│           └── UserMapper.java
└── docs/
    └── SPRING_SECURITY_LOGIN_IMPLEMENTATION.md (本文档)
```

---

**文档版本：** 1.0
**创建日期：** 2025-01-04
**维护者：** Nexus Framework Team

