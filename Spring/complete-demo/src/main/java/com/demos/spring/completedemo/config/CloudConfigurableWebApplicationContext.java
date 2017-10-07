package com.demos.spring.completedemo.config;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * 自定义spring容器，使用 spring cloud config server
 */
public class CloudConfigurableWebApplicationContext extends XmlWebApplicationContext {

    @Override
    protected ConfigurableEnvironment createEnvironment() {
        return new CloudEnvironment();
    }

}
