package com.demos.java.jdkanalyzer.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.support.ResourceEditorRegistrar;
import org.springframework.context.*;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.context.support.DefaultLifecycleProcessor;
import org.springframework.context.support.LiveBeansView;
import org.springframework.context.weaving.LoadTimeWeaverAwareProcessor;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/23
 *
 * ===== refresh()流程:
 * -> 1.初始化上下文环境: initPropertySources()钩子方法, 验证被标记为`required`的`property`是否存在
 * -> 2.获取BeanFactory: 加载BeanDefinition
 * -> 3.初始化BeanFactory: 设置ClassLoader, 添加ApplicationContextAwareProcessor后置处理器, 设置忽略自动注入的Aware接口
 * -> 4.postProcessBeanFactory()钩子方法: 如spring-mybatis在这里加载mapper
 * -> 5.创建并注册所有BeanFactoryPostProcessor
 * -> 6.注册Bean后置处理器BeanPostProcessor: PriorityOrdered, Ordered, 无序的BeanPostProcessor
 * === 注册后置处理器ApplicationListenerDetector在bean销毁时移除广播监听
 * -> 7.初始化上下文中的消息源: 即不同语言的消息(国际化)
 * -> 8.初始化上下文中的消息广播器: ApplicationEventMulticaster
 * -> 9.onRefresh()钩子方法: 可用于初始化特殊的bean
 * -> 10.找到所有的Listener并注册到消息广播中: ApplicationListener: onApplicationEvent()
 * -> 11.实例化剩下的所有非lazy-init单例bean
 * === 1).初始化条件: !abstract && singleton && !lazy-init
 * === 2).调用getBean()进行初始化: 最终会调用doGetBean()
 * === 3).初始化完成: 调用SmartInitializingSingleton.afterSingletonsInstantiated()
 * -> 12.完成刷新
 * === 1).清除资源缓存, 如ASM元数据
 * === 2).初始化当前上下文的LifecycleProcessor
 * === 3).传递刷新: 调用LifecycleProcessor的onRefresh方法
 * === 4).发布`ContextRefreshedEvent`事件
 * === 5).注册MBean
 */
public abstract class ApplicationContextAnalyzer {

    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            // 初始化上下文环境: 主要是初始化property
            // 1).initPropertySources()钩子方法: 子类可以进行各种初始化操作
            // 2).验证被标记为`required`的`property`是否存在
            prepareRefresh();

            // 获取BeanFactory: 通知子类刷新BeanFactory, 包括加载基本的BeanDefinition
            // XmlBeanDefinitionReader, DefaultBeanDefinitionDocumentReader, BeanDefinitionParserDelegate: 加载BeanDefinition
            ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

            // 初始化BeanFactory
            prepareBeanFactory(beanFactory);

            try {
                // 钩子方法: BeanFactory后置处理, BeanFactory初始化之后调用
                postProcessBeanFactory(beanFactory);

                // 从加载的Bean中找出所有未处理过的BeanFactory(如:SqlSessionFactory), 并执行初始化
                // 初始化并执行BeanFactoryPostProcessor后置处理器
                invokeBeanFactoryPostProcessors(beanFactory);

                // 注册Bean后置处理器BeanPostProcessor: PriorityOrdered, Ordered, 无序的BeanPostProcessor
                // 注册后置处理器ApplicationListenerDetector在bean销毁时移除广播监听
                registerBeanPostProcessors(beanFactory);

                // 初始化上下文中的消息源: 即不同语言的消息(国际化)
                initMessageSource();

                // 初始化上下文中的消息广播器: ApplicationEventMulticaster
                initApplicationEventMulticaster();

                // 钩子方法: 子类中可覆盖, 可用于初始化特殊的bean
                onRefresh();

                // 找到所有的Listener并注册到消息广播中: ApplicationListener: onApplicationEvent()
                registerListeners();

                // Instantiate all remaining (non-lazy-init) singletons.
                // 核心方法: 实例化剩下的所有非lazy-init单例bean
                finishBeanFactoryInitialization(beanFactory);

                // 完成刷新
                finishRefresh();
            }

            catch (BeansException ex) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Exception encountered during context initialization - " +
                            "cancelling refresh attempt: " + ex);
                }

                // Destroy already created singletons to avoid dangling resources.
                destroyBeans();

                // Reset 'active' flag.
                cancelRefresh(ex);

                // Propagate exception to caller.
                throw ex;
            }

            finally {
                // Reset common introspection caches in Spring's core, since we
                // might not ever need metadata for singleton beans anymore...
                resetCommonCaches();
            }
        }
    }

    /**
     * 准备上下文环境
     */
    protected void prepareRefresh() {
        this.startupDate = System.currentTimeMillis();
        this.closed.set(false);
        this.active.set(true);

        if (logger.isInfoEnabled()) {
            logger.info("Refreshing " + this);
        }

        // 钩子方法: 子类可以进行各种初始化操作
        initPropertySources();

        // 验证被标记为`required`的`property`是否存在
        getEnvironment().validateRequiredProperties();

        // Allow for the collection of early ApplicationEvents, to be published once the multicaster is available...
        this.earlyApplicationEvents = new LinkedHashSet<>();
    }

    /**
     * 刷新BeanFactory
     */
    protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
        // 交给子类方法来执行刷新操作
        refreshBeanFactory();
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        if (logger.isDebugEnabled()) {
            logger.debug("Bean factory for " + getDisplayName() + ": " + beanFactory);
        }
        return beanFactory;
    }

    /**
     * 刷新BeanFactory
     */
    protected final void refreshBeanFactory() throws BeansException {
        // BeanFactory已经存在, 必须关闭后才能再次刷新
        if (hasBeanFactory()) {
            // 销毁所有的singleton: 执行destroy-method或DisposableBean.destroy()
            destroyBeans();
            // 将BeanFactory置为null
            closeBeanFactory();
        }
        try {
            // 创建DefaultListableBeanFactory
            DefaultListableBeanFactory beanFactory = createBeanFactory();
            beanFactory.setSerializationId(getId());
            // 定制BeanFactory: 是否允许覆盖BeanDefinition, 是否允许循环依赖
            customizeBeanFactory(beanFactory);
            // 初始化BeanDefinitionReader, 加载BeanDefinition
            loadBeanDefinitions(beanFactory);
            synchronized (this.beanFactoryMonitor) {
                this.beanFactory = beanFactory;
            }
        }
        catch (IOException ex) {
            throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
        }
    }

    /**
     * 定制化BeanFactory
     */
    protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
        if (this.allowBeanDefinitionOverriding != null) {
            beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
        }
        if (this.allowCircularReferences != null) {
            beanFactory.setAllowCircularReferences(this.allowCircularReferences);
        }
    }

    /**
     * 销毁bean: 调用DisposableBean.destroy()和destroy-method
     */
    protected void destroyBeans() {
        getBeanFactory().destroySingletons();
    }

    protected final void closeBeanFactory() {
        synchronized (this.beanFactoryMonitor) {
            if (this.beanFactory != null) {
                this.beanFactory.setSerializationId(null);
                this.beanFactory = null;
            }
        }
    }

    /**
     * 配置FactoryBean
     */
    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // 设置ClassLoader
        beanFactory.setBeanClassLoader(getClassLoader());
        // 设置表达式语言处理器
        beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
        // 设置默认的PropertyEditor
        beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

        // 添加后置处理器ApplicationContextAwareProcessor: 用来设置各种Aware的值, 如: setApplicationContext
        beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
        // 设置需要忽略自动装配的接口
        beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
        beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
        beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
        beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
        beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
        beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

        // BeanFactory interface not registered as resolvable type in a plain factory.
        // MessageSource registered (and found for autowiring) as a bean.
        beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
        beanFactory.registerResolvableDependency(ResourceLoader.class, this);
        beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
        beanFactory.registerResolvableDependency(ApplicationContext.class, this);

        // 注册后置处理器ApplicationListenerDetector: MergedBeanDefinitionPostProcessor, 合并BeanDefinition时调用
        beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

        // Detect a LoadTimeWeaver and prepare for weaving, if found.
        if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
            beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
            // Set a temporary ClassLoader for type matching.
            beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
        }

        // Register default environment beans.
        if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
            beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
        }
        if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
            beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
        }
        if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
            beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
        }
    }

    /**
     * 初始化LifecycleProcessor
     */
    protected void initLifecycleProcessor() {
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
            this.lifecycleProcessor =
                    beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using LifecycleProcessor [" + this.lifecycleProcessor + "]");
            }
        }
        else {
            DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
            defaultProcessor.setBeanFactory(beanFactory);
            this.lifecycleProcessor = defaultProcessor;
            beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate LifecycleProcessor with name '" +
                        LIFECYCLE_PROCESSOR_BEAN_NAME +
                        "': using default [" + this.lifecycleProcessor + "]");
            }
        }
    }

    /**
     * BeanFactory初始化结束, 初始化剩下的所有非延迟加载的singleton
     */
    protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
        // Initialize conversion service for this context.
        if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
                beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
            beanFactory.setConversionService(
                    beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
        }

        // Register a default embedded value resolver if no bean post-processor
        // (such as a PropertyPlaceholderConfigurer bean) registered any before:
        // at this point, primarily for resolution in annotation attribute values.
        if (!beanFactory.hasEmbeddedValueResolver()) {
            beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
        }

        // Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
        String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
        for (String weaverAwareName : weaverAwareNames) {
            getBean(weaverAwareName);
        }

        // Stop using the temporary ClassLoader for type matching.
        beanFactory.setTempClassLoader(null);

        // 冻结所有缓存的BeanDefinition元数据, 不允许做进一步处理或修改
        beanFactory.freezeConfiguration();

        // 初始化所有非延迟加载的singleton
//        beanFactory.preInstantiateSingletons();
        preInstantiateSingletons();
    }

    /**
     * 初始化所有非延迟加载singleton
     */
    public void preInstantiateSingletons() throws BeansException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Pre-instantiating singletons in " + this);
        }

        // 取出所有已注册的BeanDefinition的beanName
        List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

        // 触发所有非延迟加载singleton的初始化
        for (String beanName : beanNames) {
            RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
            // 初始化条件: !abstract && singleton && !lazy-init
            // 调用getBean()进行初始化: 最终会调用doGetBean()
            if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
                if (isFactoryBean(beanName)) {
                    Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
                    if (bean instanceof FactoryBean) {
                        final FactoryBean<?> factory = (FactoryBean<?>) bean;
                        boolean isEagerInit;
                        if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
                            isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>)
                                            ((SmartFactoryBean<?>) factory)::isEagerInit,
                                    getAccessControlContext());
                        }
                        else {
                            isEagerInit = (factory instanceof SmartFactoryBean &&
                                    ((SmartFactoryBean<?>) factory).isEagerInit());
                        }
                        if (isEagerInit) {
                            getBean(beanName);
                        }
                    }
                }
                else {
                    getBean(beanName);
                }
            }
        }

        // 初始化完成: 调用SmartInitializingSingleton.afterSingletonsInstantiated()方法
        for (String beanName : beanNames) {
            Object singletonInstance = getSingleton(beanName);
            if (singletonInstance instanceof SmartInitializingSingleton) {
                final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
                if (System.getSecurityManager() != null) {
                    AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                        smartSingleton.afterSingletonsInstantiated();
                        return null;
                    }, getAccessControlContext());
                }
                else {
                    smartSingleton.afterSingletonsInstantiated();
                }
            }
        }
    }

    /**
     * 刷新context结束
     */
    protected void finishRefresh() {
        // 清除资源缓存, 如ASM元数据
        clearResourceCaches();

        // 初始化当前上下文的LifecycleProcessor
        initLifecycleProcessor();

        // 传递刷新: 调用LifecycleProcessor的onRefresh方法
        getLifecycleProcessor().onRefresh();

        // 发布`ContextRefreshedEvent`事件
        publishEvent(new ContextRefreshedEvent(this));

        // 注册MBean
        LiveBeansView.registerApplicationContext(this);
    }

}
