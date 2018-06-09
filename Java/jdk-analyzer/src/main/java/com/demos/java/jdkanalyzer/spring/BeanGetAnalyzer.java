package com.demos.java.jdkanalyzer.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/31
 *
 * ===== 一.doGetBean流程:
 * -> 1.解析beanName
 * -> 2.从singleton缓存中查找bean: 由于存在singleton允许提前暴露以解决循环依赖问题
 * -> 3.否则, 如果scope是prototype并且beanName正在创建中: 存在循环依赖, 抛出异常
 * -> 4.若parent factory不为空且自己不包括当前beanName的BeanDefinition: 从parent factory中获取bean实例
 * -> 5.若不是只检查类型(typeCheckOnly): 标记为已创建(将beanName加入alreadyCreated集合)
 * -> 6.合并父类的BeanDefinition, 若当前bean的类是abstract的, 无法创建bean实例
 * -> 7.确保depends-on的bean已经初始化, 若存在互为depends-on关系则抛出异常
 * -> 8.创建bean实例: `singleton`, `prototype`或其他scope(Scope.get())
 * -> 9.检查创建的bean实例是否是需要的类型: 若不是则尝试进行类型转换
 * ===== 二.createBean与BeanPostProcessor:
 * -> resolveBeforeInstantiation(): 提供创建proxy的机会, 也可以在这里修改RootBeanDefinition以改变bean
 * -> 若factory拥有`InstantiationAwareBeanPostProcessor`类型的BeanPostProcessor, 则调用其`postProcessBeforeInstantiation`方法创建实例
 * -> 若创建成功, 则调用`BeanPostProcessor`的`postProcessAfterInitialization()`方法
 * -> 若bean为空, 则调用`doCreateBean()`创建bean实例
 * ===== 三.singleton创建过程中的缓存map:
 * -> singletonObjects: beanName -> beanInstance, 缓存已创建的singleton实例
 * -> singletonFactories: beanName -> ObjectFactory(创建工厂: getObject()获取bean), 缓存创建工厂
 * -> earlySingletonObjects: beanName -> beanInstance, 缓存提前暴露的singleton实例(尚未完全初始化)
 */
public class BeanGetAnalyzer {

    /**
     * 获取bean实例
     */
    protected <T> T doGetBean(final String name, final Class<T> requiredType,
                              final Object[] args, boolean typeCheckOnly) throws BeansException {

        // 解析beanName
        final String beanName = transformedBeanName(name);
        Object bean;

        // 直接从singleton缓存中获取bean: 创建单例时允许提早曝光, 即在bean初始化之前就加入BeanFactory
        Object sharedInstance = getSingleton(beanName);
        if (sharedInstance != null && args == null) {
            if (logger.isDebugEnabled()) {
                if (isSingletonCurrentlyInCreation(beanName)) {
                    logger.debug("Returning eagerly cached instance of singleton bean '" + beanName +
                            "' that is not fully initialized yet - a consequence of a circular reference");
                }
                else {
                    logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
                }
            }
            bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
        }

        else {
            // 当前bean正在创建中: 存在循环依赖
            // 只有singleton会尝试解决循环依赖, 通过几个singleton提前暴露(缓存), prototype会直接抛出异常
            if (isPrototypeCurrentlyInCreation(beanName)) {
                throw new BeanCurrentlyInCreationException(beanName);
            }

            // 检查BeanDefinition是否已经注册
            BeanFactory parentBeanFactory = getParentBeanFactory();
            // parent factory不为空并且自己不包括当前beanName的BeanDefinition: 从parent factory中获取bean实例
            if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
                // 若不存在, 检查parentFactory中是否已存在
                String nameToLookup = originalBeanName(name);
                if (parentBeanFactory instanceof AbstractBeanFactory) {
                    return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
                            nameToLookup, requiredType, args, typeCheckOnly);
                }
                else if (args != null) {
                    // Delegation to parent with explicit args.
                    return (T) parentBeanFactory.getBean(nameToLookup, args);
                }
                else {
                    // No args -> delegate to standard getBean method.
                    return parentBeanFactory.getBean(nameToLookup, requiredType);
                }
            }

            // 如果不仅仅做类型检查则标记为已创建: 将beanName加入alreadyCreated集合
            if (!typeCheckOnly) {
                markBeanAsCreated(beanName);
            }

            try {
                // 合并父类的BeanDefinition
                final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                // 检查合并后的bean是否是`abstract`的: abstract Bean不能进行初始化
                checkMergedBeanDefinition(mbd, beanName, args);

                // 确保当前bean的depends-on的bean初始化
                String[] dependsOn = mbd.getDependsOn();
                if (dependsOn != null) {
                    for (String dep : dependsOn) {
                        // 当前bean和depends-on的bean互为depends-on的关系: 循环depends-on, 无法初始化
                        if (isDependent(beanName, dep)) {
                            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                    "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
                        }
                        // 注册depends-on的关系
                        registerDependentBean(dep, beanName);
                        try {
                            // 获取depends-on的bean: 该方法会调用doGetBean以确保depends-on的bean被初始化
                            getBean(dep);
                        }
                        catch (NoSuchBeanDefinitionException ex) {
                            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                    "'" + beanName + "' depends on missing bean '" + dep + "'", ex);
                        }
                    }
                }

                // 创建bean实例
                if (mbd.isSingleton()) {
                    // 使用ObjectFactory的方式创建: 支持延迟加载
                    sharedInstance = getSingleton(beanName, () -> {
                        try {
                            return createBean(beanName, mbd, args);
                        }
                        catch (BeansException ex) {
                            // Explicitly remove instance from singleton cache: It might have been put there
                            // eagerly by the creation process, to allow for circular reference resolution.
                            // Also remove any beans that received a temporary reference to the bean.
                            destroySingleton(beanName);
                            throw ex;
                        }
                    });
                    bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
                }

                else if (mbd.isPrototype()) {
                    // It's a prototype -> create a new instance.
                    Object prototypeInstance = null;
                    try {
                        beforePrototypeCreation(beanName);
                        prototypeInstance = createBean(beanName, mbd, args);
                    }
                    finally {
                        afterPrototypeCreation(beanName);
                    }
                    bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
                }

                else {
                    // 除`singleton`和`prototype`之外的其他scope: 包括自定义scope
                    String scopeName = mbd.getScope();
                    final Scope scope = this.scopes.get(scopeName);
                    if (scope == null) {
                        throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
                    }
                    try {
                        Object scopedInstance = scope.get(beanName, () -> {
                            beforePrototypeCreation(beanName);
                            try {
                                return createBean(beanName, mbd, args);
                            }
                            finally {
                                afterPrototypeCreation(beanName);
                            }
                        });
                        bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
                    }
                    catch (IllegalStateException ex) {
                        throw new BeanCreationException(beanName,
                                "Scope '" + scopeName + "' is not active for the current thread; consider " +
                                        "defining a scoped proxy for this bean if you intend to refer to it from a singleton",
                                ex);
                    }
                }
            }
            catch (BeansException ex) {
                cleanupAfterBeanCreationFailure(beanName);
                throw ex;
            }
        }

        // Check if required type matches the type of the actual bean instance.
        // 检查创建的bean实例是否是需要的类型: 若不是则尝试进行类型转换
        if (requiredType != null && !requiredType.isInstance(bean)) {
            try {
                T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
                if (convertedBean == null) {
                    throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
                }
                return convertedBean;
            }
            catch (TypeMismatchException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to convert bean '" + name + "' to required type '" +
                            ClassUtils.getQualifiedName(requiredType) + "'", ex);
                }
                throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
            }
        }
        return (T) bean;
    }

    public Object getSingleton(String beanName) {
        return getSingleton(beanName, true);
    }

    /* ==================== singleton缓存的三张map ==================== */
    // 缓存已创建的singleton实例
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    // 缓存创建工厂
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);
    // 缓存提前暴露的singleton实例(尚未完全初始化)
    private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);
    /**
     * 根据beanName获取singleton, singleton缓存的三张map:
     * -> singletonObjects: beanName -> beanInstance, 缓存已创建的singleton实例
     * -> singletonFactories: beanName -> ObjectFactory(创建工厂: getObject()获取bean), 缓存创建工厂
     * -> earlySingletonObjects: beanName -> beanInstance, 缓存提前暴露的singleton实例(尚未完全初始化)
     * ===== allowEarlyReference: 是否允许早期引用(提前暴露)
     */
    protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        // 从`已创建的singleton实例缓存(singletonObjects)`中查找
        Object singletonObject = this.singletonObjects.get(beanName);
        // 缓存中不存在且且当前singleton正在创建中
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            synchronized (this.singletonObjects) {
                // 从`提前暴露的singleton实例缓存(earlySingletonObjects)`中查找
                singletonObject = this.earlySingletonObjects.get(beanName);
                if (singletonObject == null && allowEarlyReference) {
                    // 仍然不存在: 该singleton尚未暴露, 若允许提前暴露, 则从`创建工厂缓存`取出ObjectFactory
                    ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        // ObjectFactory不为空: 创建bean实例, 并加入`提前暴露的singleton实例缓存`,同时从`创建工厂缓存`移除singleton的创建工厂
                        singletonObject = singletonFactory.getObject();
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        this.singletonFactories.remove(beanName);
                    }
                }
            }
        }
        return singletonObject;
    }

    /**
     * 返回原生的singleton实例: 从`已创建的singleton缓存`中获取或调用`ObjectFactory.getObject()`
     */
    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(beanName, "Bean name must not be null");
        synchronized (this.singletonObjects) {
            // 首先从`已创建的singleton缓存`中查询beanName: 若命中则直接返回
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                // 缓存未命中则进入创建流程
                if (this.singletonsCurrentlyInDestruction) {
                    throw new BeanCreationNotAllowedException(beanName,
                            "Singleton bean creation not allowed while singletons of this factory are in destruction " +
                                    "(Do not request a bean from a BeanFactory in a destroy method implementation!)");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
                }
                // singleton创建前回调
                beforeSingletonCreation(beanName);
                boolean newSingleton = false;
                boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
                if (recordSuppressedExceptions) {
                    this.suppressedExceptions = new LinkedHashSet<>();
                }
                try {
                    // 调用ObjectFactory.getObject()返回singleton实例
                    singletonObject = singletonFactory.getObject();
                    newSingleton = true;
                }
                catch (IllegalStateException ex) {
                    // Has the singleton object implicitly appeared in the meantime ->
                    // if yes, proceed with it since the exception indicates that state.
                    singletonObject = this.singletonObjects.get(beanName);
                    if (singletonObject == null) {
                        throw ex;
                    }
                }
                catch (BeanCreationException ex) {
                    if (recordSuppressedExceptions) {
                        for (Exception suppressedException : this.suppressedExceptions) {
                            ex.addRelatedCause(suppressedException);
                        }
                    }
                    throw ex;
                }
                finally {
                    if (recordSuppressedExceptions) {
                        this.suppressedExceptions = null;
                    }
                    afterSingletonCreation(beanName);
                }
                if (newSingleton) {
                    addSingleton(beanName, singletonObject);
                }
            }
            return singletonObject;
        }
    }

    /**
     * 创建bean实例
     * -> 1.InstantiationAwareBeanPostProcessor.postProcessBeforeInstantiation()
     * -> 2.doCreateBean()
     */
    protected Object createBean(String beanName, RootBeanDefinition mbd, Object[] args)
            throws BeanCreationException {

        if (logger.isDebugEnabled()) {
            logger.debug("Creating instance of bean '" + beanName + "'");
        }
        RootBeanDefinition mbdToUse = mbd;

        // Make sure bean class is actually resolved at this point, and
        // clone the bean definition in case of a dynamically resolved Class
        // which cannot be stored in the shared merged bean definition.
        // 解析bean Class
        Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
        if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
            mbdToUse = new RootBeanDefinition(mbd);
            mbdToUse.setBeanClass(resolvedClass);
        }

        // 处理覆盖方法: lookup-method和replaced-method, 这两类方法需要使用aop织入所需方法
        try {
            mbdToUse.prepareMethodOverrides();
        }
        catch (BeanDefinitionValidationException ex) {
            throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
                    beanName, "Validation of method overrides failed", ex);
        }

        try {
            // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
            // 提供一个扩展点: BeanPostProcessor可以在这里返回代理对象或修改RootBeanDefinition
            Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
            if (bean != null) {
                // 创建bean成功则直接返回
                return bean;
            }
        }
        catch (Throwable ex) {
            throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
                    "BeanPostProcessor before instantiation of bean failed", ex);
        }

        // 到这里说明`BeanPostProcessor`处理后并没有创建bean实例, 调用`doCreateBean()`方法创建bean实例
        // 通过反射`Constructor`或FactoryBean方法创建
        try {
            Object beanInstance = doCreateBean(beanName, mbdToUse, args);
            if (logger.isDebugEnabled()) {
                logger.debug("Finished creating instance of bean '" + beanName + "'");
            }
            return beanInstance;
        }
        catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
            // A previously detected exception with proper bean creation context already,
            // or illegal singleton state to be communicated up to DefaultSingletonBeanRegistry.
            throw ex;
        }
        catch (Throwable ex) {
            throw new BeanCreationException(
                    mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
        }
    }

    /**
     * 应用实例创建前处理器方法: InstantiationAwareBeanPostProcessor.postProcessBeforeInstantiation, 这里可作为扩展点返回proxy对象
     */
    protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
        Object bean = null;
        if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
            // Make sure bean class is actually resolved at this point.
            // 判断RootBeanDefinition是否拥有`InstantiationAwareBeanPostProcessor`类型的`BeanPostProcessor`
            // 拥有则调用其创建前回调`applyBeanPostProcessorsBeforeInstantiation`, 该方法返回一个bean实例
            if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
                Class<?> targetType = determineTargetType(beanName, mbd);
                if (targetType != null) {
                    bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
                    if (bean != null) {
                        // 如果bean不为空: 此时bean已经创建并初始化, 调用`BeanPostProcessor`的`postProcessAfterInitialization()`后置处理方法
                        bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
                    }
                }
            }
            mbd.beforeInstantiationResolved = (bean != null);
        }
        return bean;
    }

    /**
     * 从给定的beanInstance中获取bean(bean实例或工厂bean)
     */
    protected Object getObjectForBeanInstance(
            Object beanInstance, String name, String beanName, RootBeanDefinition mbd) {

        // 希望获取工厂bean而不是bean实例("&"开头)
        if (BeanFactoryUtils.isFactoryDereference(name)) {
            if (beanInstance instanceof NullBean) {
                return beanInstance;
            }
            // 希望获取工厂bean且beanInstance不是FactoryBean: 抛出异常
            if (!(beanInstance instanceof FactoryBean)) {
                throw new BeanIsNotAFactoryException(transformedBeanName(name), beanInstance.getClass());
            }
        }

        // 条件分解:
        // 1).!(beanInstance instanceof FactoryBean): beanInstance不是FactoryBean, 则可以直接返回bean实例
        // 2).用户希望获取工厂bean实例(name以"&"开头)
        // 这两种情况任意一种满足都可以直接返回
        if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {
            return beanInstance;
        }

        Object object = null;
        if (mbd == null) {
            // 从缓存中查询bean实例
            object = getCachedObjectForFactoryBean(beanName);
        }
        if (object == null) {
            // 到这里, 说明beanInstance是FactoryBean
            FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
            // RootBeanDefinition为空, 且当前factory包含beanName的BeanDefinition, 创建BeanDefinition
            if (mbd == null && containsBeanDefinition(beanName)) {
                mbd = getMergedLocalBeanDefinition(beanName);
            }
            boolean synthetic = (mbd != null && mbd.isSynthetic());
            // 从FactoryBean获取bean实例
            object = getObjectFromFactoryBean(factory, beanName, !synthetic);
        }
        return object;
    }

    /**
     * 从`FactoryBean`中获取bean实例
     * -> 通过自定义的方式简化bean实例的创建, 通过`getObject()`方法将bean暴露出来, 避免了使用传统的反射机制创建bean所需的大量配置信息
     */
    protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
        // bean是singleton且singleton缓存`singletonObjects`中已包含该bean
        if (factory.isSingleton() && containsSingleton(beanName)) {
            synchronized (getSingletonMutex()) {
                Object object = this.factoryBeanObjectCache.get(beanName);
                if (object == null) {
                    // 从FactoryBean获取对象: factory.getObject()
                    object = doGetObjectFromFactoryBean(factory, beanName);
                    // Only post-process and store if not put there already during getObject() call above
                    // (e.g. because of circular reference processing triggered by custom getBean calls)
                    Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
                    if (alreadyThere != null) {
                        object = alreadyThere;
                    }
                    else {
                        if (shouldPostProcess) {
                            if (isSingletonCurrentlyInCreation(beanName)) {
                                // Temporarily return non-post-processed object, not storing it yet..
                                return object;
                            }
                            // singleton创建之前的回调
                            beforeSingletonCreation(beanName);
                            try {
                                // 从FactoryBean返回singleton的后置处理: BeanPostProcessor.postProcessAfterInitialization
                                object = postProcessObjectFromFactoryBean(object, beanName);
                            }
                            catch (Throwable ex) {
                                throw new BeanCreationException(beanName,
                                        "Post-processing of FactoryBean's singleton object failed", ex);
                            }
                            finally {
                                // singleton创建之后的回调
                                afterSingletonCreation(beanName);
                            }
                        }
                        if (containsSingleton(beanName)) {
                            this.factoryBeanObjectCache.put(beanName, object);
                        }
                    }
                }
                return object;
            }
        }
        else {
            // 从FactoryBean获取对象: factory.getObject()
            Object object = doGetObjectFromFactoryBean(factory, beanName);
            if (shouldPostProcess) {
                try {
                    // 从FactoryBean返回singleton的后置处理
                    object = postProcessObjectFromFactoryBean(object, beanName);
                }
                catch (Throwable ex) {
                    throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
                }
            }
            return object;
        }
    }

    /**
     * 应用所有注册的BeanPostProcessor的postProcessAfterInitialization()方法
     */
    protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
        return applyBeanPostProcessorsAfterInitialization(object, beanName);
    }

    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
            throws BeansException {

        Object result = existingBean;
        for (BeanPostProcessor beanProcessor : getBeanPostProcessors()) {
            // 调用所有注册的BeanPostProcessor的postProcessAfterInitialization
            Object current = beanProcessor.postProcessAfterInitialization(result, beanName);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }


}
