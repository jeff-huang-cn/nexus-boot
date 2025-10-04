package com.nexus.backend.admin.dal.dataobject;

import com.nexus.backend.admin.dal.dataobject.user.UserDO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserDO 测试类
 * 验证DO中的方法不会影响数据库操作
 */
class UserDOTest {

    @Test
    void testUserStatusMethods() {
        // 创建用户对象
        UserDO user = new UserDO();

        // 测试 isEnabled
        user.setStatus(0);
        assertTrue(user.isEnabled(), "status=0 应该是启用状态");

        user.setStatus(1);
        assertFalse(user.isEnabled(), "status=1 应该是停用状态");

        // 测试 isAccountNonExpired
        user.setExpiredTime(null);
        assertTrue(user.isAccountNonExpired(), "expiredTime=null 应该是未过期");

        user.setExpiredTime(LocalDateTime.now().plusDays(1));
        assertTrue(user.isAccountNonExpired(), "未来时间应该是未过期");

        user.setExpiredTime(LocalDateTime.now().minusDays(1));
        assertFalse(user.isAccountNonExpired(), "过去时间应该是已过期");

        // 测试 isAccountNonLocked
        user.setLockedTime(null);
        assertTrue(user.isAccountNonLocked(), "lockedTime=null 应该是未锁定");

        user.setLockedTime(LocalDateTime.now().plusHours(1));
        assertFalse(user.isAccountNonLocked(), "未来时间应该是已锁定");

        user.setLockedTime(LocalDateTime.now().minusHours(1));
        assertTrue(user.isAccountNonLocked(), "过去时间应该是已解锁");

        // 测试 lockAccount
        user.lockAccount(60);
        assertNotNull(user.getLockedTime());
        assertFalse(user.isAccountNonLocked(), "调用lockAccount后应该是锁定状态");

        // 测试 unlockAccount
        user.unlockAccount();
        assertNull(user.getLockedTime());
        assertTrue(user.isAccountNonLocked(), "调用unlockAccount后应该是未锁定状态");
    }

    @Test
    void testIsAccountAvailable() {
        UserDO user = new UserDO();
        user.setStatus(0);
        user.setExpiredTime(null);
        user.setLockedTime(null);
        user.setPasswordExpireTime(null);

        assertTrue(user.isAccountAvailable(), "所有条件都满足时账户应该可用");

        user.setStatus(1);
        assertFalse(user.isAccountAvailable(), "停用后账户应该不可用");
        assertEquals("账户已被停用", user.getUnavailableReason());
    }

    @Test
    void testGetUnavailableReason() {
        UserDO user = new UserDO();

        // 测试停用
        user.setStatus(1);
        assertEquals("账户已被停用", user.getUnavailableReason());

        // 测试过期
        user.setStatus(0);
        user.setExpiredTime(LocalDateTime.now().minusDays(1));
        assertEquals("账户已过期", user.getUnavailableReason());

        // 测试锁定
        user.setExpiredTime(null);
        user.setLockedTime(LocalDateTime.now().plusHours(1));
        assertEquals("账户已被锁定", user.getUnavailableReason());

        // 测试密码过期
        user.setLockedTime(null);
        user.setPasswordExpireTime(LocalDateTime.now().minusDays(1));
        assertEquals("密码已过期，请修改密码", user.getUnavailableReason());

        // 测试可用
        user.setPasswordExpireTime(null);
        assertNull(user.getUnavailableReason(), "账户可用时应该返回null");
    }
}
