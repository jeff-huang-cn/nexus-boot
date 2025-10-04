package com.nexus.framework.security.config;

import com.nexus.framework.security.filter.JwtAuthenticationFilter;
import com.nexus.framework.security.handler.JwtAuthenticationFailureHandler;
import com.nexus.framework.security.handler.JwtAuthenticationSuccessHandler;
import com.nexus.framework.security.service.UserDetailsServiceImpl;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationSuccessHandler successHandler;
    private final JwtAuthenticationFailureHandler failureHandler;

    public SecurityConfig(JwtAuthenticationSuccessHandler successHandler,
            JwtAuthenticationFailureHandler failureHandler) {
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() throws NoSuchAlgorithmException {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey resKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        return new ImmutableJWKSet<>(new JWKSet(resKey));
    }

    @Bean
    public KeyPair generateRsaKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // 密钥长度
        return keyPairGenerator.generateKeyPair();
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        // 创建JWT处理器
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

        // 配置签名验证的密钥选择器（从JWK源中获取公钥）
        JWSKeySelector<SecurityContext> jwsKeySelector = new JWSVerificationKeySelector<>(
                JWSAlgorithm.RS256, // 使用RS256算法
                jwkSource);
        jwtProcessor.setJWSKeySelector(jwsKeySelector);

        return new NimbusJwtDecoder(jwtProcessor);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsServiceImpl userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UserDetailsServiceImpl userDetailsService)
            throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return auth.build();
    }

    /**
     * 配置安全过滤链（核心配置）
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
                // 1. 关闭CSRF（JWT不需要）
                .csrf(AbstractHttpConfigurer::disable)
                // 2. 配置会话管理（无状态，不创建会话）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 3. 配置登录端点（前后端分离，只处理POST，不生成登录页面）
                .formLogin(form -> form
                        .loginProcessingUrl("/login") // 只处理POST /login
                        .successHandler(successHandler) // 登录成功返回JSON
                        .failureHandler(failureHandler) // 登录失败返回JSON
                        .permitAll())
                // 禁用默认登录页面的重定向
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 未认证时返回401 JSON，不重定向到登录页
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
                        }))
                // 4. 配置URL权限规则
                .authorizeHttpRequests(auth -> auth
                        // 允许测试端点
                        .requestMatchers("/test/**").permitAll()
                        // 允许登录端点（OPTIONS请求已由CorsFilter处理）
                        .requestMatchers("/login").permitAll()
                        // 其他接口需要认证
                        .anyRequest().authenticated())
                // 5. 添加JWT验证过滤器（在用户名密码过滤器之前执行）
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtDecoder),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
