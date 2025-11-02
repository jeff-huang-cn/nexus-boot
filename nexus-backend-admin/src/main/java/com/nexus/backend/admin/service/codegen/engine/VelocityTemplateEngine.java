package com.nexus.backend.admin.service.codegen.engine;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

/**
 * Velocity模板引擎
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Component
public class VelocityTemplateEngine {

    private VelocityEngine velocityEngine;

    @PostConstruct
    public void init() {
        // 创建Velocity引擎实例而不是使用静态方法
        velocityEngine = new VelocityEngine();

        // 创建Velocity配置
        Properties properties = new Properties();

        // 设置velocity资源加载器
        properties.setProperty("resource.loader", "classpath");
        properties.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

        // 禁用模板缓存，确保每次都加载最新模板
        properties.setProperty("resource.loader.classpath.cache", "false");
        properties.setProperty("resource.loader.classpath.modification_check_interval", "0");

        // 设置输入输出编码
        properties.setProperty("input.encoding", "UTF-8");
        properties.setProperty("output.encoding", "UTF-8");

        // 设置runtime日志 - 禁用日志输出
        properties.setProperty("runtime.log.logsystem.class",
                "org.apache.velocity.runtime.log.NullLogChute");

        // 初始化Velocity引擎实例
        velocityEngine.init(properties);
    }

    /**
     * 渲染模板
     *
     * @param templatePath 模板路径
     * @param context      模板变量
     * @return 渲染结果
     */
    public String render(String templatePath, Map<String, Object> context) {
        VelocityContext velocityContext = new VelocityContext();

        // 设置模板变量
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            velocityContext.put(entry.getKey(), entry.getValue());
        }

        // 渲染模板
        StringWriter writer = new StringWriter();
        velocityEngine.mergeTemplate(templatePath, "UTF-8", velocityContext, writer);

        return writer.toString();
    }

}
