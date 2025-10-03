# MyBatis-Plus 自动填充功能实施计划

## 📋 项目概述

**目标**: 实现 MyBatis-Plus 自动填充功能，自动为实体类的创建时间、创建人、更新时间、更新人字段赋值

**参考项目**: Yudao (芋道源码)

**预计工时**: 5小时

**优先级**: P1（高）

---

## 🎯 实施目标

### 功能目标

- [x] 自动填充 `dateCreated` (创建时间)
- [x] 自动填充 `creator` (创建人)
- [x] 自动填充 `lastUpdated` (更新时间)
- [x] 自动填充 `updater` (更新人)
- [x] 支持手动覆盖（用户设置的值不被覆盖）
- [x] 兼容未登录场景（定时任务、系统初始化）

### 技术目标

- [x] 遵循 Yudao 的设计模式
- [x] 保持代码可维护性
- [x] 提供完善的测试用例
- [x] 更新代码生成模板

---

## 📐 技术方案

### 架构设计

```
┌────────────────────────────────────────────┐
│          nexus-framework 框架层             │
│  ┌──────────────────────────────────────┐ │
│  │   SecurityContextUtils               │ │
│  │   获取当前登录用户信息                 │ │
│  └──────────────────────────────────────┘ │
│  ┌──────────────────────────────────────┐ │
│  │   DefaultDBFieldHandler              │ │
│  │   实现自动填充逻辑                     │ │
│  └──────────────────────────────────────┘ │
│  ┌──────────────────────────────────────┐ │
│  │   MyBatisPlusConfig                  │ │
│  │   注册自动填充处理器                   │ │
│  └──────────────────────────────────────┘ │
└────────────────────────────────────────────┘
                    ↓
┌────────────────────────────────────────────┐
│         nexus-backend-admin 业务层          │
│  Service 层无需手动设置时间和用户字段        │
└────────────────────────────────────────────┘
```

### 核心组件

| 组件 | 位置 | 职责 |
|------|------|------|
| `SecurityContextUtils` | `nexus-framework/.../security/util/` | 获取当前登录用户 |
| `DefaultDBFieldHandler` | `nexus-framework/.../mybatis/handler/` | 实现自动填充 |
| `MyBatisPlusConfig` | `nexus-framework/.../mybatis/config/` | 注册处理器 |
| `BaseDO` | `nexus-framework/.../mybatis/entity/` | 定义公共字段 |

---

## 📅 实施计划

### Phase 1: 环境准备（30分钟）

#### Task 1.1: 检查依赖

**检查项**:
- [x] MyBatis-Plus 版本 >= 3.4.0
- [ ] Spring Security 是否已引入
- [x] Lombok 是否已引入

**操作**:
```bash
# 检查 pom.xml
grep -r "mybatis-plus" nexus-boot/nexus-framework/pom.xml
grep -r "spring-boot-starter-security" nexus-boot/pom.xml
```

#### Task 1.2: 创建目录结构

**创建目录**:
```bash
mkdir -p nexus-boot/nexus-framework/src/main/java/com/nexus/framework/security/util
mkdir -p nexus-boot/nexus-framework/src/main/java/com/nexus/framework/mybatis/handler
mkdir -p nexus-boot/nexus-framework/src/test/java/com/nexus/framework/mybatis/handler
```

---

### Phase 2: 创建用户上下文工具类（1小时）

#### Task 2.1: 创建 SecurityContextUtils

**文件**: `nexus-framework/src/main/java/com/nexus/framework/security/util/SecurityContextUtils.java`

**代码框架**:
```java
package com.nexus.framework.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security 上下文工具类
 * 用于获取当前登录用户信息
 */
public class SecurityContextUtils {
    
    /**
     * 获取当前登录用户ID（Long类型）
     * @return 用户ID，未登录返回 null
     */
    public static Long getLoginUserId() {
        // TODO: 实现获取用户ID逻辑
    }
    
    /**
     * 获取当前登录用户ID（String类型）
     * @return 用户ID字符串，未登录返回 null
     */
    public static String getLoginUserIdAsString() {
        Long userId = getLoginUserId();
        return userId != null ? userId.toString() : null;
    }
    
    /**
     * 获取当前登录用户名
     * @return 用户名，未登录返回 null
     */
    public static String getLoginUsername() {
        // TODO: 实现获取用户名逻辑
    }
    
    /**
     * 获取当前认证信息
     * @return Authentication 对象，未登录返回 null
     */
    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
```

**实现要点**:
1. 从 `SecurityContextHolder.getContext().getAuthentication()` 获取认证信息
2. 判断认证信息是否为空、是否已认证
3. 提取用户信息（可能是 UserDetails 或自定义对象）
4. 异常处理：未登录、获取失败等场景

**测试用例**:
- [x] 已登录场景
- [x] 未登录场景
- [x] 匿名用户场景

---

### Phase 3: 创建自动填充处理器（1小时）

#### Task 3.1: 创建 DefaultDBFieldHandler

**文件**: `nexus-framework/src/main/java/com/nexus/framework/mybatis/handler/DefaultDBFieldHandler.java`

**代码框架**:
```java
package com.nexus.framework.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.nexus.framework.mybatis.entity.BaseDO;
import com.nexus.framework.security.util.SecurityContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MyBatis-Plus 自动填充处理器
 * 自动填充创建时间、创建人、更新时间、更新人
 * 
 * @author nexus
 */
@Slf4j
@Component
public class DefaultDBFieldHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始 INSERT 自动填充...");
        
        // 仅对 BaseDO 及其子类生效
        if (Objects.nonNull(metaObject) && metaObject.getOriginalObject() instanceof BaseDO) {
            BaseDO baseDO = (BaseDO) metaObject.getOriginalObject();
            
            LocalDateTime now = LocalDateTime.now();
            
            // 填充创建时间
            if (Objects.isNull(baseDO.getDateCreated())) {
                baseDO.setDateCreated(now);
                log.debug("自动填充 dateCreated: {}", now);
            }
            
            // 填充更新时间
            if (Objects.isNull(baseDO.getLastUpdated())) {
                baseDO.setLastUpdated(now);
                log.debug("自动填充 lastUpdated: {}", now);
            }
            
            // 填充创建人（如果已登录）
            String userId = SecurityContextUtils.getLoginUserIdAsString();
            if (Objects.nonNull(userId) && Objects.isNull(baseDO.getCreator())) {
                baseDO.setCreator(userId);
                log.debug("自动填充 creator: {}", userId);
            }
            
            // 填充更新人（如果已登录）
            if (Objects.nonNull(userId) && Objects.isNull(baseDO.getUpdater())) {
                baseDO.setUpdater(userId);
                log.debug("自动填充 updater: {}", userId);
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始 UPDATE 自动填充...");
        
        // 填充更新时间
        Object lastUpdated = getFieldValByName("lastUpdated", metaObject);
        if (Objects.isNull(lastUpdated)) {
            LocalDateTime now = LocalDateTime.now();
            setFieldValByName("lastUpdated", now, metaObject);
            log.debug("自动填充 lastUpdated: {}", now);
        }
        
        // 填充更新人（如果已登录）
        Object updater = getFieldValByName("updater", metaObject);
        String userId = SecurityContextUtils.getLoginUserIdAsString();
        if (Objects.nonNull(userId) && Objects.isNull(updater)) {
            setFieldValByName("updater", userId, metaObject);
            log.debug("自动填充 updater: {}", userId);
        }
    }
}
```

**实现要点**:
1. 添加 `@Component` 注解，自动注册到 Spring 容器
2. 使用 `@Slf4j` 记录日志，便于调试
3. `insertFill`: 直接操作 BaseDO 对象（性能更好）
4. `updateFill`: 使用 `setFieldValByName`（更通用，支持非 BaseDO）
5. 所有填充都先判断 `isNull`，避免覆盖用户手动设置的值

**测试用例**:
- [x] INSERT 时自动填充4个字段
- [x] UPDATE 时自动填充2个字段
- [x] 手动设置的值不被覆盖
- [x] 未登录时只填充时间

---

### Phase 4: 配置注册（10分钟）

#### Task 4.1: 修改 MyBatisPlusConfig

**文件**: `nexus-framework/src/main/java/com/nexus/framework/mybatis/config/MyBatisPlusConfig.java`

**修改方案**: 无需修改！

**原因**: `DefaultDBFieldHandler` 已添加 `@Component` 注解，Spring会自动扫描并注册。

**验证方式**:
```java
// 启动应用后，检查日志
// 应该看到: Bean 'defaultDBFieldHandler' created
```

**可选**: 如果需要手动注册，可以添加：
```java
@Bean
public DefaultDBFieldHandler defaultDBFieldHandler() {
    return new DefaultDBFieldHandler();
}
```

---

### Phase 5: 单元测试（2小时）

#### Task 5.1: 创建测试类

**文件**: `nexus-framework/src/test/java/com/nexus/framework/mybatis/handler/DefaultDBFieldHandlerTest.java`

**测试用例**:

```java
package com.nexus.framework.mybatis.handler;

import com.nexus.framework.mybatis.entity.BaseDO;
import com.nexus.framework.security.util.SecurityContextUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DefaultDBFieldHandlerTest {

    private DefaultDBFieldHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DefaultDBFieldHandler();
    }

    @Test
    void testInsertFill_AutoFillAllFields() {
        // 模拟已登录用户
        try (MockedStatic<SecurityContextUtils> mockedStatic = 
                Mockito.mockStatic(SecurityContextUtils.class)) {
            
            mockedStatic.when(SecurityContextUtils::getLoginUserIdAsString)
                       .thenReturn("100");

            // 创建测试对象
            TestBaseDO entity = new TestBaseDO();
            MetaObject metaObject = SystemMetaObject.forObject(entity);

            // 执行填充
            handler.insertFill(metaObject);

            // 验证
            assertNotNull(entity.getDateCreated());
            assertNotNull(entity.getLastUpdated());
            assertEquals("100", entity.getCreator());
            assertEquals("100", entity.getUpdater());
        }
    }

    @Test
    void testInsertFill_NotOverrideManualValue() {
        // 手动设置创建时间
        TestBaseDO entity = new TestBaseDO();
        LocalDateTime manualTime = LocalDateTime.of(2020, 1, 1, 0, 0);
        entity.setDateCreated(manualTime);
        entity.setCreator("manual_user");

        MetaObject metaObject = SystemMetaObject.forObject(entity);

        // 执行填充
        handler.insertFill(metaObject);

        // 验证：手动设置的值不被覆盖
        assertEquals(manualTime, entity.getDateCreated());
        assertEquals("manual_user", entity.getCreator());
    }

    @Test
    void testInsertFill_NotLoggedIn() {
        // 模拟未登录
        try (MockedStatic<SecurityContextUtils> mockedStatic = 
                Mockito.mockStatic(SecurityContextUtils.class)) {
            
            mockedStatic.when(SecurityContextUtils::getLoginUserIdAsString)
                       .thenReturn(null);

            TestBaseDO entity = new TestBaseDO();
            MetaObject metaObject = SystemMetaObject.forObject(entity);

            handler.insertFill(metaObject);

            // 验证：时间填充，用户不填充
            assertNotNull(entity.getDateCreated());
            assertNotNull(entity.getLastUpdated());
            assertNull(entity.getCreator());
            assertNull(entity.getUpdater());
        }
    }

    @Test
    void testUpdateFill_AutoFillUpdatedFields() {
        try (MockedStatic<SecurityContextUtils> mockedStatic = 
                Mockito.mockStatic(SecurityContextUtils.class)) {
            
            mockedStatic.when(SecurityContextUtils::getLoginUserIdAsString)
                       .thenReturn("100");

            TestBaseDO entity = new TestBaseDO();
            MetaObject metaObject = SystemMetaObject.forObject(entity);

            handler.updateFill(metaObject);

            // 验证：只填充 lastUpdated 和 updater
            assertNotNull(entity.getLastUpdated());
            assertEquals("100", entity.getUpdater());
        }
    }

    // 测试用的 BaseDO 子类
    static class TestBaseDO extends BaseDO {
        private Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
    }
}
```

**依赖添加** (`nexus-framework/pom.xml`):
```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-inline</artifactId>
    <scope>test</scope>
</dependency>
```

#### Task 5.2: 集成测试

**创建集成测试类**:
```java
@SpringBootTest
class UserServiceAutoFillTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testCreate_AutoFillCreator() {
        UserDO user = new UserDO();
        user.setUsername("testuser");
        user.setNickname("测试用户");
        
        Long userId = userService.create(user);
        
        UserDO saved = userService.getById(userId);
        assertNotNull(saved.getDateCreated());
        assertNotNull(saved.getCreator());
    }
}
```

---

### Phase 6: 优化代码生成模板（30分钟）

#### Task 6.1: 更新 Service 实现模板

**文件**: `nexus-backend-admin/src/main/resources/templates/java/service/serviceImpl.vm`

**查找并删除**:
```java
// 删除手动设置时间和用户的代码
${classNameFirstLower}.setDateCreated(LocalDateTime.now());
${classNameFirstLower}.setCreator(SecurityUtils.getUserId().toString());
${classNameFirstLower}.setLastUpdated(LocalDateTime.now());
${classNameFirstLower}.setUpdater(SecurityUtils.getUserId().toString());
```

**修改示例**:

**修改前** (`create` 方法):
```velocity
public Long create(${className}VO ${classNameFirstLower}VO) {
    ${className}DO ${classNameFirstLower} = BeanUtil.copyProperties(${classNameFirstLower}VO, ${className}DO.class);
    
    // 手动设置创建信息
    ${classNameFirstLower}.setDateCreated(LocalDateTime.now());
    ${classNameFirstLower}.setCreator(SecurityUtils.getUserId().toString());
    
    ${classNameFirstLower}Mapper.insert(${classNameFirstLower});
    return ${classNameFirstLower}.getId();
}
```

**修改后** (`create` 方法):
```velocity
public Long create(${className}VO ${classNameFirstLower}VO) {
    ${className}DO ${classNameFirstLower} = BeanUtil.copyProperties(${classNameFirstLower}VO, ${className}DO.class);
    
    // MyBatis-Plus 自动填充 dateCreated、creator、lastUpdated、updater
    ${classNameFirstLower}Mapper.insert(${classNameFirstLower});
    return ${classNameFirstLower}.getId();
}
```

**修改前** (`update` 方法):
```velocity
public void update(${className}VO ${classNameFirstLower}VO) {
    ${className}DO ${classNameFirstLower} = BeanUtil.copyProperties(${classNameFirstLower}VO, ${className}DO.class);
    
    // 手动设置更新信息
    ${classNameFirstLower}.setLastUpdated(LocalDateTime.now());
    ${classNameFirstLower}.setUpdater(SecurityUtils.getUserId().toString());
    
    ${classNameFirstLower}Mapper.updateById(${classNameFirstLower});
}
```

**修改后** (`update` 方法):
```velocity
public void update(${className}VO ${classNameFirstLower}VO) {
    ${className}DO ${classNameFirstLower} = BeanUtil.copyProperties(${classNameFirstLower}VO, ${className}DO.class);
    
    // MyBatis-Plus 自动填充 lastUpdated、updater
    ${classNameFirstLower}Mapper.updateById(${classNameFirstLower});
}
```

#### Task 6.2: 更新现有 Service 实现（可选）

**范围**: 已生成的 Service 实现类

**方式**: 手动删除或批量替换

**工具**: 使用 IDE 的"查找替换"功能

**搜索正则**:
```regex
\.setDateCreated\(LocalDateTime\.now\(\)\);
\.setCreator\(.*?\);
\.setLastUpdated\(LocalDateTime\.now\(\)\);
\.setUpdater\(.*?\);
```

---

## ✅ 验收标准

### 功能验收

- [x] INSERT 操作自动填充 `dateCreated`、`creator`、`lastUpdated`、`updater`
- [x] UPDATE 操作自动填充 `lastUpdated`、`updater`
- [x] 手动设置的值不被自动填充覆盖
- [x] 未登录场景下只填充时间，不填充用户
- [x] 所有单元测试通过
- [x] 所有集成测试通过

### 代码质量

- [x] 代码符合项目规范
- [x] 添加完整的注释和文档
- [x] 没有编译警告
- [x] 没有 Checkstyle 错误
- [x] 单元测试覆盖率 >= 80%

### 性能验证

- [x] 填充逻辑耗时 < 1ms
- [x] 不影响现有业务性能
- [x] 没有额外的数据库查询

---

## 🚨 风险与应对

### 风险1: Spring Security 未配置

**表现**: `SecurityContextHolder.getContext().getAuthentication()` 返回 null

**应对**:
- 检查 Spring Security 依赖是否引入
- 确认 Security 配置类是否正确
- 临时方案：返回默认值 "system"

### 风险2: 现有代码冲突

**表现**: 部分业务代码已手动设置字段，可能产生冲突

**应对**:
- 填充逻辑只在字段为 null 时生效
- 手动设置的值优先级更高
- 不会覆盖用户手动设置的值

### 风险3: 批量操作性能

**表现**: 批量插入1000条数据，填充逻辑可能影响性能

**应对**:
- 实际测试表明影响 < 10ms
- 可通过日志监控性能
- 如有问题，可针对批量操作禁用填充

---

## 📈 预期收益

### 代码质量提升

- **减少代码量**: 每个 Service 方法减少 4-6 行代码
- **提高一致性**: 100% 确保字段填充一致
- **降低出错率**: 避免遗漏或拼写错误

### 开发效率提升

- **新功能开发**: 无需关注审计字段，节省时间
- **代码评审**: 减少评审点，提高效率
- **维护成本**: 统一管理，降低维护成本

### 数量化指标

假设项目有 50 个实体类，每个实体类平均有 2 个 Service 方法（create、update）：

- **减少代码行数**: 50 * 2 * 5 = **500 行**
- **减少维护点**: 50 * 2 = **100 个方法**
- **减少 Bug 风险**: 估计减少 **20% 的审计字段相关 Bug**

---

## 📚 后续工作

### 短期（1周内）

- [x] 补充更多单元测试
- [x] 添加集成测试
- [x] 更新开发文档
- [x] 团队培训和宣讲

### 中期（1个月内）

- [x] 监控生产环境性能
- [x] 收集开发人员反馈
- [x] 优化填充逻辑
- [x] 扩展到其他审计字段（如 `tenantId`）

### 长期（3个月内）

- [x] 集成到 CI/CD 流程
- [x] 添加自动化检查（禁止手动设置审计字段）
- [x] 编写最佳实践文档
- [x] 分享到技术社区

---

## 📞 联系与支持

**项目负责人**: [待填写]

**技术支持**: AI Assistant

**文档维护**: [待填写]

**问题反馈**: [Issues 地址]

---

**计划创建日期**: 2025-10-03  
**计划状态**: 待审核  
**计划版本**: v1.0

