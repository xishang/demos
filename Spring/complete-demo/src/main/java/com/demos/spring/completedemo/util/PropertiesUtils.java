package com.demos.spring.completedemo.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtils.class);

    private static Properties properties;

    static {
        properties = new Properties();
        // 读取配置文件
        loadPropertiesFile(new String[]{"config.properties"});
    }

    private static void loadPropertiesFile(String[] fileNames) {
        for (String fileName : fileNames) {
            final InputStream inputStream = PropertiesUtils.class.getClassLoader().getResourceAsStream(fileName);
            if (inputStream != null) {
                try {
                    properties.load(inputStream);
                } catch (IOException e) {
                    LOGGER.error("读取配置文件文件{}读取错误!", fileName);
                }
            } else {
                LOGGER.warn("配置文件文件{} 不存在!", fileName);
            }
        }
    }

    public static String getValue(String key) {
        String value = properties.getProperty(key);
        if (StringUtils.isBlank(value)) {
            LOGGER.warn("没有获取指定key的值，请确认资源文件中是否存在【{}】", key);
        }
        return value;
    }

    public static String getValue(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (StringUtils.isBlank(value)) {
            LOGGER.warn("没有获取指定key的值，请确认资源文件中是否存在【{}】", key);
            return defaultValue;
        }
        return value;
    }

}
