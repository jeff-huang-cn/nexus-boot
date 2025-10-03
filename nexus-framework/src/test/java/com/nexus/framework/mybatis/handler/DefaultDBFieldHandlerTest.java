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

/**
 * DefaultDBFieldHandler 单元测试
 * 
 * @author nexus
 */
class DefaultDBFieldHandlerTest {

        private DefaultDBFieldHandler handler;

        @BeforeEach
        void setUp() {
                handler = new DefaultDBFieldHandler();
        }

        /**
         * 测试 INSERT 时自动填充所有字段（已登录场景）
         */
        @Test
        void testInsertFill_AutoFillAllFields_WhenLoggedIn() {
                // 模拟已登录用户
                try (MockedStatic<SecurityContextUtils> mockedStatic = Mockito.mockStatic(SecurityContextUtils.class)) {

                        mockedStatic.when(SecurityContextUtils::getLoginUserIdAsString)
                                        .thenReturn("100");

                        // 创建测试对象
                        TestBaseDO entity = new TestBaseDO();
                        MetaObject metaObject = SystemMetaObject.forObject(entity);

                        // 执行前验证
                        assertNull(entity.getDateCreated());
                        assertNull(entity.getLastUpdated());
                        assertNull(entity.getCreator());
                        assertNull(entity.getUpdater());

                        // 执行填充
                        handler.insertFill(metaObject);

                        // 验证所有字段都被填充
                        assertNotNull(entity.getDateCreated(), "dateCreated 应该被自动填充");
                        assertNotNull(entity.getLastUpdated(), "lastUpdated 应该被自动填充");
                        assertEquals("100", entity.getCreator(), "creator 应该是当前登录用户ID");
                        assertEquals("100", entity.getUpdater(), "updater 应该是当前登录用户ID");

                        // 验证时间合理性（应该接近当前时间）
                        LocalDateTime now = LocalDateTime.now();
                        assertTrue(entity.getDateCreated().isBefore(now.plusSeconds(1)));
                        assertTrue(entity.getDateCreated().isAfter(now.minusSeconds(5)));
                }
        }

        /**
         * 测试 INSERT 时不覆盖手动设置的值
         */
        @Test
        void testInsertFill_NotOverrideManualValue() {
                try (MockedStatic<SecurityContextUtils> mockedStatic = Mockito.mockStatic(SecurityContextUtils.class)) {

                        mockedStatic.when(SecurityContextUtils::getLoginUserIdAsString)
                                        .thenReturn("100");

                        // 手动设置部分字段
                        TestBaseDO entity = new TestBaseDO();
                        LocalDateTime manualTime = LocalDateTime.of(2020, 1, 1, 0, 0);
                        entity.setDateCreated(manualTime);
                        entity.setCreator("manual_user");

                        MetaObject metaObject = SystemMetaObject.forObject(entity);

                        // 执行填充
                        handler.insertFill(metaObject);

                        // 验证：手动设置的值不被覆盖
                        assertEquals(manualTime, entity.getDateCreated(),
                                        "手动设置的 dateCreated 不应该被覆盖");
                        assertEquals("manual_user", entity.getCreator(),
                                        "手动设置的 creator 不应该被覆盖");

                        // 验证：未设置的字段被自动填充
                        assertNotNull(entity.getLastUpdated(),
                                        "lastUpdated 应该被自动填充");
                        assertEquals("100", entity.getUpdater(),
                                        "updater 应该被自动填充");
                }
        }

        /**
         * 测试 INSERT 时未登录场景
         */
        @Test
        void testInsertFill_NotLoggedIn() {
                // 模拟未登录（返回 null）
                try (MockedStatic<SecurityContextUtils> mockedStatic = Mockito.mockStatic(SecurityContextUtils.class)) {

                        mockedStatic.when(SecurityContextUtils::getLoginUserIdAsString)
                                        .thenReturn(null);

                        TestBaseDO entity = new TestBaseDO();
                        MetaObject metaObject = SystemMetaObject.forObject(entity);

                        // 执行填充
                        handler.insertFill(metaObject);

                        // 验证：时间字段被填充
                        assertNotNull(entity.getDateCreated(),
                                        "dateCreated 应该被填充，即使未登录");
                        assertNotNull(entity.getLastUpdated(),
                                        "lastUpdated 应该被填充，即使未登录");

                        // 验证：用户字段不被填充
                        assertNull(entity.getCreator(),
                                        "creator 不应该被填充，因为未登录");
                        assertNull(entity.getUpdater(),
                                        "updater 不应该被填充，因为未登录");
                }
        }

        /**
         * 测试 UPDATE 时自动填充更新字段（已登录场景）
         */
        @Test
        void testUpdateFill_AutoFillUpdatedFields_WhenLoggedIn() {
                try (MockedStatic<SecurityContextUtils> mockedStatic = Mockito.mockStatic(SecurityContextUtils.class)) {

                        mockedStatic.when(SecurityContextUtils::getLoginUserIdAsString)
                                        .thenReturn("200");

                        TestBaseDO entity = new TestBaseDO();
                        // 模拟已有创建信息
                        entity.setDateCreated(LocalDateTime.of(2023, 1, 1, 0, 0));
                        entity.setCreator("100");

                        MetaObject metaObject = SystemMetaObject.forObject(entity);

                        // 执行前验证
                        assertNull(entity.getLastUpdated());
                        assertNull(entity.getUpdater());

                        // 执行填充
                        handler.updateFill(metaObject);

                        // 验证：只填充 lastUpdated 和 updater
                        assertNotNull(entity.getLastUpdated(),
                                        "lastUpdated 应该被填充");
                        assertEquals("200", entity.getUpdater(),
                                        "updater 应该是当前登录用户ID");

                        // 验证：创建信息不变
                        assertEquals(LocalDateTime.of(2023, 1, 1, 0, 0),
                                        entity.getDateCreated(), "dateCreated 不应该改变");
                        assertEquals("100", entity.getCreator(),
                                        "creator 不应该改变");
                }
        }

        /**
         * 测试 UPDATE 时不覆盖已有的更新字段
         */
        @Test
        void testUpdateFill_NotOverrideExistingUpdatedFields() {
                try (MockedStatic<SecurityContextUtils> mockedStatic = Mockito.mockStatic(SecurityContextUtils.class)) {

                        mockedStatic.when(SecurityContextUtils::getLoginUserIdAsString)
                                        .thenReturn("300");

                        TestBaseDO entity = new TestBaseDO();
                        // 手动设置更新信息
                        LocalDateTime manualUpdateTime = LocalDateTime.of(2024, 1, 1, 12, 0);
                        entity.setLastUpdated(manualUpdateTime);
                        entity.setUpdater("manual_updater");

                        MetaObject metaObject = SystemMetaObject.forObject(entity);

                        // 执行填充
                        handler.updateFill(metaObject);

                        // 验证：手动设置的值不被覆盖
                        assertEquals(manualUpdateTime, entity.getLastUpdated(),
                                        "手动设置的 lastUpdated 不应该被覆盖");
                        assertEquals("manual_updater", entity.getUpdater(),
                                        "手动设置的 updater 不应该被覆盖");
                }
        }

        /**
         * 测试 UPDATE 时未登录场景
         */
        @Test
        void testUpdateFill_NotLoggedIn() {
                try (MockedStatic<SecurityContextUtils> mockedStatic = Mockito.mockStatic(SecurityContextUtils.class)) {

                        mockedStatic.when(SecurityContextUtils::getLoginUserIdAsString)
                                        .thenReturn(null);

                        TestBaseDO entity = new TestBaseDO();
                        MetaObject metaObject = SystemMetaObject.forObject(entity);

                        // 执行填充
                        handler.updateFill(metaObject);

                        // 验证：时间被填充
                        assertNotNull(entity.getLastUpdated(),
                                        "lastUpdated 应该被填充，即使未登录");

                        // 验证：updater 不被填充
                        assertNull(entity.getUpdater(),
                                        "updater 不应该被填充，因为未登录");
                }
        }

        /**
         * 测试对任何对象都可以执行填充（只要有对应字段）
         */
        @Test
        void testInsertFill_WorksForAnyObjectWithFields() {
                try (MockedStatic<SecurityContextUtils> mockedStatic = Mockito.mockStatic(SecurityContextUtils.class)) {

                        mockedStatic.when(SecurityContextUtils::getLoginUserIdAsString)
                                        .thenReturn("999");

                        // 创建一个非 BaseDO 的对象（但有对应字段）
                        NonBaseDOEntity entity = new NonBaseDOEntity();
                        MetaObject metaObject = SystemMetaObject.forObject(entity);

                        // 执行填充
                        handler.insertFill(metaObject);

                        // 验证：只要有对应字段，都会被填充
                        assertNotNull(entity.getDateCreated(),
                                        "即使不是 BaseDO，只要有 dateCreated 字段也应该被填充");
                        assertNotNull(entity.getLastUpdated(),
                                        "即使不是 BaseDO，只要有 lastUpdated 字段也应该被填充");
                        assertEquals("999", entity.getCreator(),
                                        "即使不是 BaseDO，只要有 creator 字段也应该被填充");
                        assertEquals("999", entity.getUpdater(),
                                        "即使不是 BaseDO，只要有 updater 字段也应该被填充");
                }
        }

        /**
         * 测试用的 BaseDO 子类
         */
        static class TestBaseDO extends BaseDO {
                private Long id;

                public Long getId() {
                        return id;
                }

                public void setId(Long id) {
                        this.id = id;
                }
        }

        /**
         * 非 BaseDO 的测试类（但有对应的审计字段）
         */
        static class NonBaseDOEntity {
                private LocalDateTime dateCreated;
                private LocalDateTime lastUpdated;
                private String creator;
                private String updater;

                public LocalDateTime getDateCreated() {
                        return dateCreated;
                }

                public void setDateCreated(LocalDateTime dateCreated) {
                        this.dateCreated = dateCreated;
                }

                public LocalDateTime getLastUpdated() {
                        return lastUpdated;
                }

                public void setLastUpdated(LocalDateTime lastUpdated) {
                        this.lastUpdated = lastUpdated;
                }

                public String getCreator() {
                        return creator;
                }

                public void setCreator(String creator) {
                        this.creator = creator;
                }

                public String getUpdater() {
                        return updater;
                }

                public void setUpdater(String updater) {
                        this.updater = updater;
                }
        }
}
