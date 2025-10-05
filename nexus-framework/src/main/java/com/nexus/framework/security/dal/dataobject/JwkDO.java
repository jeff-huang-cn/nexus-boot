package com.nexus.framework.security.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oauth2_jwk")
public class JwkDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String keyId;

    private String keyType;

    private String algorithm;

    private String publicKey;

    private String privateKey;

    private LocalDateTime createdTime;

    private LocalDateTime expiresAt;

    private Boolean isActive;
}
