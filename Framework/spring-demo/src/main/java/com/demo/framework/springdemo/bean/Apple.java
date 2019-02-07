package com.demo.framework.springdemo.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/25
 */
@Component
public class Apple implements BeanFactoryAware, BeanNameAware, ApplicationContextAware, InitializingBean {

    @PostConstruct
    public void init() {
        System.out.println("Apple @PostConstruct!");
    }

    private Orange orange;

    @Resource
    private Banana banana;

    @Resource
    public void setOrange(Orange orange) {
        System.out.println("Apple setOrange, class = " + orange.getClass().getName());
        this.orange = orange;
    }

    public Apple() {
        System.out.println("Apple Constructor!");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("Apple setBeanFactory: beanFactory = " + beanFactory.getClass().getName());
    }

    @Override
    public void setBeanName(String s) {
        System.out.println("Apple setBeanName: beanName = " + s);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("Apple setApplicationContext: applicationContext = " + applicationContext.getClass().getName());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Apple afterPropertiesSet");
    }

    @Cacheable
    public long getCacheThing() {
        return System.currentTimeMillis();
    }

}
