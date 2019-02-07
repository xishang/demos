package com.demo.framework.springdemo.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/25
 */
@Component
public class PostB implements BeanPostProcessor, Ordered {

    public PostB() {
        System.out.println("PostB Constructor!");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Apple) {
            System.out.println("PostB BeforeInitialization, beanName = " + beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Apple) {
            System.out.println("PostB AfterInitialization, beanName = " + beanName);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
