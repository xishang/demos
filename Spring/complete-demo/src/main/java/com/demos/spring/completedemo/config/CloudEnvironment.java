package com.demos.spring.completedemo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * Spring Cloud Config Environment
 */
public class CloudEnvironment extends StandardServletEnvironment {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        super.customizePropertySources(propertySources);
        try {
            // 加载启动配置文件
            propertySources.addLast(new ResourcePropertySource("resource", "classpath:bootstrap.properties"));
            propertySources.addLast(initConfigServicePropertySourceLocator(this));
        } catch (Exception e) {
            logger.error("failed to create cloud environment", e);
        }
    }

    private PropertySource<?> initConfigServicePropertySourceLocator(Environment environment) {
        ConfigClientProperties configClientProperties = new ConfigClientProperties(environment);

        /* ConfigServicePropertySourceLocator可以使用默认字段查找这些值，此处可以不用显式设置
        configClientProperties.setUri(PropertiesUtils.getValue("spring.cloud.config.uri"));
        configClientProperties.setName(PropertiesUtils.getValue("spring.cloud.config.name"));
        configClientProperties.setLabel(PropertiesUtils.getValue("spring.cloud.config.label"));
        configClientProperties.setProfile(PropertiesUtils.getValue("spring.cloud.config.profile"));*/

        configClientProperties.setUri(environment.getProperty("spring.cloud.config.uri"));
        configClientProperties.setUsername(environment.getProperty("spring.cloud.config.username"));
        configClientProperties.setPassword(environment.getProperty("spring.cloud.config.password"));
        ConfigServicePropertySourceLocator configServicePropertySourceLocator = new ConfigServicePropertySourceLocator(configClientProperties);
        return configServicePropertySourceLocator.locate(environment);
    }

}
