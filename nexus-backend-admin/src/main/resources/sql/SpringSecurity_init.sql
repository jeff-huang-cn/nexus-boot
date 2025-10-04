CREATE TABLE oauth2_registered_client (
  id varchar(100) NOT NULL,
  client_id varchar(100) NOT NULL,
  client_id_issued_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  client_secret varchar(200) DEFAULT NULL,
  client_secret_expires_at timestamp DEFAULT NULL,
  client_name varchar(200) NOT NULL,
  client_authentication_methods varchar(1000) NOT NULL,
  authorization_grant_types varchar(1000) NOT NULL,
  redirect_uris varchar(1000) DEFAULT NULL,
  post_logout_redirect_uris varchar(1000) DEFAULT NULL,
  scopes varchar(1000) NOT NULL,
  client_settings varchar(2000) NOT NULL,
  token_settings varchar(2000) NOT NULL,
  PRIMARY KEY (id)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*
IMPORTANT:
    If using PostgreSQL, update ALL columns defined with 'blob' to 'text',
    as PostgreSQL does not support the 'blob' data type.
*/
CREATE TABLE oauth2_authorization (
  id varchar(100) NOT NULL,
  registered_client_id varchar(100) NOT NULL,
  principal_name varchar(200) NOT NULL,
  authorization_grant_type varchar(100) NOT NULL,
  authorized_scopes varchar(1000) DEFAULT NULL,
  attributes blob DEFAULT NULL,
  state varchar(500) DEFAULT NULL,
  authorization_code_value blob DEFAULT NULL,
  authorization_code_issued_at timestamp DEFAULT NULL,
  authorization_code_expires_at timestamp DEFAULT NULL,
  authorization_code_metadata blob DEFAULT NULL,
  access_token_value blob DEFAULT NULL,
  access_token_issued_at timestamp DEFAULT NULL,
  access_token_expires_at timestamp DEFAULT NULL,
  access_token_metadata blob DEFAULT NULL,
  access_token_type varchar(100) DEFAULT NULL,
  access_token_scopes varchar(1000) DEFAULT NULL,
  oidc_id_token_value blob DEFAULT NULL,
  oidc_id_token_issued_at timestamp DEFAULT NULL,
  oidc_id_token_expires_at timestamp DEFAULT NULL,
  oidc_id_token_metadata blob DEFAULT NULL,
  refresh_token_value blob DEFAULT NULL,
  refresh_token_issued_at timestamp DEFAULT NULL,
  refresh_token_expires_at timestamp DEFAULT NULL,
  refresh_token_metadata blob DEFAULT NULL,
  user_code_value blob DEFAULT NULL,
  user_code_issued_at timestamp DEFAULT NULL,
  user_code_expires_at timestamp DEFAULT NULL,
  user_code_metadata blob DEFAULT NULL,
  device_code_value blob DEFAULT NULL,
  device_code_issued_at timestamp DEFAULT NULL,
  device_code_expires_at timestamp DEFAULT NULL,
  device_code_metadata blob DEFAULT NULL,
  PRIMARY KEY (id)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE oauth2_authorization_consent (
      registered_client_id varchar(100) NOT NULL,
      principal_name varchar(200) NOT NULL,
      authorities varchar(1000) NOT NULL,
      PRIMARY KEY (registered_client_id, principal_name)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



-- admin.oauth2_jwk definition

CREATE TABLE `oauth2_jwk` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
    `key_id` varchar(100) NOT NULL COMMENT '密钥唯一标识符，用于JWT头部的kid字段',
    `key_type` varchar(20) NOT NULL DEFAULT 'RSA' COMMENT '密钥类型，如RSA、EC等',
    `algorithm` varchar(20) NOT NULL DEFAULT 'RS256' COMMENT '签名算法，如RS256、ES256等',
    `public_key` text NOT NULL COMMENT '公钥内容，PEM格式',
    `private_key` text NOT NULL COMMENT '私钥内容，PEM格式',
    `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '密钥创建时间',
    `expires_at` timestamp NOT NULL COMMENT '密钥过期时间',
    `is_active` tinyint(1) DEFAULT '1' COMMENT '是否激活：1-激活，0-停用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_oauth2_jwk_key_id` (`key_id`),
    KEY `idx_oauth2_jwk_expires_at` (`expires_at`),
    KEY `idx_oauth2_jwk_is_active` (`is_active`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OAuth2 JWK密钥表';