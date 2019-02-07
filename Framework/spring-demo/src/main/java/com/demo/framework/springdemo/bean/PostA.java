package com.demo.framework.springdemo.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/25
 */
@Component
public class PostA implements InstantiationAwareBeanPostProcessor, Ordered {

    public PostA() {
        System.out.println("PostA Constructor!");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Apple) {
            System.out.println("PostA BeforeInitialization!, beanName = " + beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Apple) {
            System.out.println("PostA AfterInitialization, beanName = " + beanName);
        }
        return bean;
    }

    @Nullable
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        if (beanClass == Apple.class) {
            System.out.println("PostA BeforeInstantiation, beanName = " + beanName);
        }
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        if (bean instanceof Apple) {
            System.out.println("PostA AfterInstantiation, beanName = " + beanName);
        }
        return true;
    }

    @Nullable
    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        if (bean instanceof Apple) {
            System.out.println("PostA PropertyValues, beanName = " + beanName);
        }
        return pvs;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
