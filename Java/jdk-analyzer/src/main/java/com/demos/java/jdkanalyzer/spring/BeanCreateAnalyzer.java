package com.demos.java.jdkanalyzer.spring;

import com.sun.istack.internal.Nullable;
import org.springframework.beans.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.*;
import org.springframework.core.MethodParameter;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/31
 *
 * ===== doCreateBean()之前: InstantiationAwareBeanPostProcessor.postProcessBeforeInstantiation(), 若返回不为null则不需要调用doCreateBean()
 * ===== doCreateBean流程:
 * -> 1.获取BeanWrapper:
 * === 1).单例: 只需要创建一次, 从缓存中取出并删除
 * === 2).非单例: 每次调用createBeanInstance创建, 方式: Supplier.get(), 工厂方法, 带参构造器(构造方法, 方法参数, 实例化策略), 默认构造器
 * ====== 实例化方法: 如果有覆盖方法(LookupOverride or ReplaceOverride)则使用CGLIB创建, 否则使用反射技术
 * -> 2.应用后置处理器MergedBeanDefinitionPostProcessor
 * -> 3.如果允许提前暴露: 暴露单例并应用后置处理器SmartInstantiationAwareBeanPostProcessor
 * -> 4.注入属性:
 * === 1).后置处理器: InstantiationAwareBeanPostProcessors.postProcessAfterInstantiation(), 返回false则终止属性注入
 * === 2).后置处理器: InstantiationAwareBeanPostProcessors.postProcessPropertyValues(), 返回null则终止属性注入; 典型应用: RequiredAnnotationBeanPostProcessor
 * === 3).byName(doGetBean递归初始化) or byType: 注册依赖bean, 以便顺序销毁避免出现空指针的情况
 * === 4).属性注入: applyPropertyValues
 * -> 5.初始化bean实例:
 * === 1).执行`Aware`方法, 顺序为: BeanNameAware >> BeanClassLoaderAware >> BeanFactoryAware
 * === 2).应用`BeanPostProcessor`的`postProcessBeforeInitialization`, 如: @PostConstruct
 * === 3).执行初始化方法: InitializingBean.afterPropertiesSet >> init-method
 * === 4).应用`BeanPostProcessor`的`postProcessAfterInitialization`
 */
public class BeanCreateAnalyzer {

    /**
     * 创建bean实例: 使用工厂方法或构造器
     */
    protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
            throws BeanCreationException {

        // 获取bean包装器
        BeanWrapper instanceWrapper = null;
        if (mbd.isSingleton()) {
            // 如果是单例: 从缓存中取出并删除(创建完之后就可以从缓存删除了)
            instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
        }
        if (instanceWrapper == null) {
            // 不是单例: 需多次创建, 每次创建调用createBeanInstance方法: factory-method或构造器创建
            instanceWrapper = createBeanInstance(beanName, mbd, args);
        }
        // 从bean包装器中取出bean实例和bean Class
        final Object bean = instanceWrapper.getWrappedInstance();
        Class<?> beanType = instanceWrapper.getWrappedClass();
        if (beanType != NullBean.class) {
            mbd.resolvedTargetType = beanType;
        }

        // 允许MergedBeanDefinitionPostProcessor后置处理器修改RootBeanDefinition
        synchronized (mbd.postProcessingLock) {
            if (!mbd.postProcessed) {
                try {
                    // 应用MergedBeanDefinitionPostProcessor的后置处理方法
                    applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
                }
                catch (Throwable ex) {
                    throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                            "Post-processing of merged bean definition failed", ex);
                }
                mbd.postProcessed = true;
            }
        }

        // 是否允许提前暴露: 单例 & 允许循环引用 & 当前singleton正在创建中, 提前暴露可用于解决属性的循环依赖
        boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
                isSingletonCurrentlyInCreation(beanName));
        if (earlySingletonExposure) {
            if (logger.isDebugEnabled()) {
                logger.debug("Eagerly caching bean '" + beanName +
                        "' to allow for resolving potential circular references");
            }
            // 允许提前暴露: 为避免循环依赖, 在bean初始化完成之前将创建bean的ObjectFactory加入缓存
            // 应用后置处理器`SmartInstantiationAwareBeanPostProcessor`: Spring在这里织入切面
            addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
        }

        // 初始化bean: 填充属性
        Object exposedObject = bean;
        try {
            // 属性注入
            populateBean(beanName, mbd, instanceWrapper);
            // 初始化bean实例: Aware, BeanPostProcessor, init-method等
            exposedObject = initializeBean(beanName, exposedObject, mbd);
        }
        catch (Throwable ex) {
            if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
                throw (BeanCreationException) ex;
            }
            else {
                throw new BeanCreationException(
                        mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
            }
        }

        if (earlySingletonExposure) {
            // 创建singleton缓存中查找bean实例
            Object earlySingletonReference = getSingleton(beanName, false);
            if (earlySingletonReference != null) {
                if (exposedObject == bean) {
                    // 在执行完初始化方法之后, exposedObject没有改变, 说明exposedObject没有被增强
                    exposedObject = earlySingletonReference;
                }
                // 检查依赖的bean
                else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
                    String[] dependentBeans = getDependentBeans(beanName);
                    Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
                    for (String dependentBean : dependentBeans) {
                        if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                            actualDependentBeans.add(dependentBean);
                        }
                    }
                    // 依赖的bean中有一部分没有创建完成: 存在循环依赖
                    if (!actualDependentBeans.isEmpty()) {
                        throw new BeanCurrentlyInCreationException(beanName,
                                "Bean with name '" + beanName + "' has been injected into other beans [" +
                                        StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
                                        "] in its raw version as part of a circular reference, but has eventually been " +
                                        "wrapped. This means that said other beans do not use the final version of the " +
                                        "bean. This is often the result of over-eager type matching - consider using " +
                                        "'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
                    }
                }
            }
        }

        // 注册DisposableBean
        try {
            registerDisposableBeanIfNecessary(beanName, bean, mbd);
        }
        catch (BeanDefinitionValidationException ex) {
            throw new BeanCreationException(
                    mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
        }

        return exposedObject;
    }

    /**
     * 创建对象实例:
     * -> Supplier.get()
     * -> factory-method
     * -> 带参构造器: 核心是选择: 1.构造方法, 2.方法参数, 3.实例化策略
     * -> 默认构造器(无参)
     */
    protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
        // 解析class
        Class<?> beanClass = resolveBeanClass(mbd, beanName);

        // 检查是否是public的class
        if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
        }

        // Supplier不为空则通过`Supplier.get()`创建对象
        Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
        if (instanceSupplier != null) {
            return obtainFromSupplier(instanceSupplier, beanName);
        }

        // 否则若factoryMethodName不为空, 使用工厂方法创建对象
        if (mbd.getFactoryMethodName() != null)  {
            return instantiateUsingFactoryMethod(beanName, mbd, args);
        }

        boolean resolved = false;
        boolean autowireNecessary = false;
        // 如果参数为null, 可以使用之前已经解析过的默认构造方法
        if (args == null) {
            synchronized (mbd.constructorArgumentLock) {
                if (mbd.resolvedConstructorOrFactoryMethod != null) {
                    resolved = true;
                    autowireNecessary = mbd.constructorArgumentsResolved;
                }
            }
        }
        if (resolved) {
            if (autowireNecessary) {
                return autowireConstructor(beanName, mbd, null, null);
            }
            else {
                return instantiateBean(beanName, mbd);
            }
        }

        // 选择带参构造器实例化对象
        Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
        if (ctors != null ||
                mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
                mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args))  {
            return autowireConstructor(beanName, mbd, ctors, args);
        }

        // 以上策略均未成功实例化对象, 最后使用默认构造器实例化对象: 无参构造器
        return instantiateBean(beanName, mbd);
    }

    /**
     * 使用默认构造函数实例化对象
     */
    protected BeanWrapper instantiateBean(final String beanName, final RootBeanDefinition mbd) {
        try {
            Object beanInstance;
            final BeanFactory parent = this;
            if (System.getSecurityManager() != null) {
                beanInstance = AccessController.doPrivileged((PrivilegedAction<Object>) () ->
                                getInstantiationStrategy().instantiate(mbd, beanName, parent),
                        getAccessControlContext());
            }
            else {
                beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, parent);
            }
            BeanWrapper bw = new BeanWrapperImpl(beanInstance);
            initBeanWrapper(bw);
            return bw;
        }
        catch (Throwable ex) {
            throw new BeanCreationException(
                    mbd.getResourceDescription(), beanName, "Instantiation of bean failed", ex);
        }
    }

    /**
     * 使用带参的构造方法实例化对象, 核心是选择: 1.构造方法, 2.方法参数, 3.实例化策略
     * -> explicitArgs为null时使用BeanDefinition中的构造方法, 并且解析之后会缓存构造方法和参数, 以便再次使用
     */
    public BeanWrapper autowireConstructor(final String beanName, final RootBeanDefinition mbd,
                                           Constructor<?>[] chosenCtors, final Object[] explicitArgs) {

        // 初始化BeanWrapper
        BeanWrapperImpl bw = new BeanWrapperImpl();
        this.beanFactory.initBeanWrapper(bw);

        Constructor<?> constructorToUse = null;
        org.springframework.beans.factory.support.ConstructorResolver.ArgumentsHolder argsHolderToUse = null;
        Object[] argsToUse = null;

        // 如果传入的构造方法参数不为空, 则直接使用该参数
        if (explicitArgs != null) {
            argsToUse = explicitArgs;
        }
        // 否则, 从缓存中查询构造方法参数
        else {
            Object[] argsToResolve = null;
            synchronized (mbd.constructorArgumentLock) {
                constructorToUse = (Constructor<?>) mbd.resolvedConstructorOrFactoryMethod;
                if (constructorToUse != null && mbd.constructorArgumentsResolved) {
                    // Found a cached constructor...
                    argsToUse = mbd.resolvedConstructorArguments;
                    if (argsToUse == null) {
                        argsToResolve = mbd.preparedConstructorArguments;
                    }
                }
            }
            if (argsToResolve != null) {
                argsToUse = resolvePreparedArguments(beanName, mbd, bw, constructorToUse, argsToResolve);
            }
        }

        // 参数未被缓存, 需要解析构造方法
        if (constructorToUse == null) {
            // ..., 步骤:
            // 1.从配置文件中解析构造方法参数
            // 2.选择需要的构造方法
            // 3.将解析出来的构造方法添加到缓存: 以便再次创建时可以直接从缓存获取
        }
        try {
            // 选择实例化策略创建对象
            final InstantiationStrategy strategy = beanFactory.getInstantiationStrategy();
            Object beanInstance;

            if (System.getSecurityManager() != null) {
                final Constructor<?> ctorToUse = constructorToUse;
                final Object[] argumentsToUse = argsToUse;
                beanInstance = AccessController.doPrivileged((PrivilegedAction<Object>) () ->
                                strategy.instantiate(mbd, beanName, beanFactory, ctorToUse, argumentsToUse),
                        beanFactory.getAccessControlContext());
            }
            else {
                beanInstance = strategy.instantiate(mbd, beanName, this.beanFactory, constructorToUse, argsToUse);
            }

            bw.setBeanInstance(beanInstance);
            return bw;
        }
        catch (Throwable ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Bean instantiation via constructor failed", ex);
        }
    }

    /**
     * 实例化策略创建对象:
     * -> 无覆盖方法(LookupOverride or ReplaceOverride), 直接使用反射创建对象
     * -> 否则: 使用AOP技术织入
     */
    public Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
                              final Constructor<?> ctor, @Nullable Object... args) {
        // 1.如果没有覆盖方法(lookup-method和replaced-method): 直接使用反射接口创建对象
        if (!bd.hasMethodOverrides()) {
            if (System.getSecurityManager() != null) {
                // use own privileged to change accessibility (when security is on)
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    ReflectionUtils.makeAccessible(ctor);
                    return null;
                });
            }
            return (args != null ? BeanUtils.instantiateClass(ctor, args) : BeanUtils.instantiateClass(ctor));
        }
        else {
            // 2.否则: 使用CGLIB
            return instantiateWithMethodInjection(bd, beanName, owner, ctor, args);
        }
    }

    /**
     * 提前暴露的后置处理: 使用已注册的`SmartInstantiationAwareBeanPostProcessor`
     * 这里提供了一个提前暴露object的扩展点: AOP功能就是在这里织入的
     */
    protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
        Object exposedObject = bean;
        if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            for (BeanPostProcessor bp : getBeanPostProcessors()) {
                if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
                    SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
                    exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
                }
            }
        }
        return exposedObject;
    }

    /* ==================== singleton缓存的三张map ==================== */
    // 缓存已创建的singleton实例
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    // 缓存创建工厂
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);
    // 缓存提前暴露的singleton实例(尚未完全初始化)
    private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);
    // 已注册的singleton的beanName集合
    private final Set<String> registeredSingletons = new LinkedHashSet<>(256);
    /**
     * 在bean初始化完成之前将创建bean的ObjectFactory加入缓存: 解决循环依赖问题
     */
    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(singletonFactory, "Singleton factory must not be null");
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) { // singleton尚未创建完成
                // 将创建bean的ObjectFactory加入缓存
                this.singletonFactories.put(beanName, singletonFactory);
                // 从提前暴露的singleton缓存中删除beanName
                this.earlySingletonObjects.remove(beanName);
                // 将beanName加入到已注册的singleton集合
                this.registeredSingletons.add(beanName);
            }
        }
    }

    /**
     * 属性注入:
     * -> 后置处理器: InstantiationAwareBeanPostProcessors.postProcessAfterInstantiation(), 返回false则终止属性注入
     * -> 后置处理器: InstantiationAwareBeanPostProcessors.postProcessPropertyValues(), 返回null则终止属性注入; 典型应用: RequiredAnnotationBeanPostProcessor
     * -> 属性注入: applyPropertyValues
     */
    protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
        // 若bean有属性需要填充但是bean包装器为空导致无法填充: 抛出异常
        if (bw == null) {
            if (mbd.hasPropertyValues()) {
                throw new BeanCreationException(
                        mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
            }
            else {
                // Skip property population phase for null instance.
                return;
            }
        }

        boolean continueWithPropertyPopulation = true;

        // 属性注入前调用后置处理器实例化后处理: InstantiationAwareBeanPostProcessors.postProcessAfterInstantiation
        // 处置处理过滤链: 一旦返回false则终止属性注入
        if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            for (BeanPostProcessor bp : getBeanPostProcessors()) {
                if (bp instanceof InstantiationAwareBeanPostProcessor) {
                    InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                    if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
                        // 后置处理方法返回false则终止属性注入
                        continueWithPropertyPopulation = false;
                        break;
                    }
                }
            }
        }

        // 后置处理方法返回false终止属性注入
        if (!continueWithPropertyPopulation) {
            return;
        }

        PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);

        if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME ||
                mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
            MutablePropertyValues newPvs = new MutablePropertyValues(pvs);

            // 以`byName`的方式装配属性
            if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
                autowireByName(beanName, mbd, bw, newPvs);
            }

            // 以`byType`的方式装配属性: 包括集合的注入
            if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
                autowireByType(beanName, mbd, bw, newPvs);
            }

            pvs = newPvs;
        }

        // 是否有`InstantiationAwareBeanPostProcessor`类型的后置处理器
        boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
        // 是否需要进行依赖检查: 简单属性或对象引用
        boolean needsDepCheck = (mbd.getDependencyCheck() != RootBeanDefinition.DEPENDENCY_CHECK_NONE);

        if (hasInstAwareBpps || needsDepCheck) {
            if (pvs == null) {
                pvs = mbd.getPropertyValues();
            }
            PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
            // 如果有`InstantiationAwareBeanPostProcessor`类型的后置处理器, 在注入属性(applyPropertyValues)前调用后置处理方法`postProcessPropertyValues`
            // 这里是一个扩展点: 可以修改属性
            // 处置处理过滤链: 一旦postProcessPropertyValues返回null则终止属性注入
            if (hasInstAwareBpps) {
                for (BeanPostProcessor bp : getBeanPostProcessors()) {
                    if (bp instanceof InstantiationAwareBeanPostProcessor) {
                        InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                        pvs = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
                        if (pvs == null) {
                            return;
                        }
                    }
                }
            }
            // 进行依赖检查: 检查依赖的所有bean是否已经被暴露出来
            if (needsDepCheck) {
                checkDependencies(beanName, mbd, filteredPds, pvs);
            }
        }
        // 到这里之前都属于准备属性阶段
        // 属性注入: 将属性注入到bean中: 使用反射
        if (pvs != null) {
            applyPropertyValues(beanName, mbd, bw, pvs);
        }
    }

    /**
     * byName注入: doGetBean(递归初始化) + registerDependentBean(注册依赖bean)
     */
    protected void autowireByName(
            String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

        // 寻找BeanWrapper中需要依赖注入的非简单类型属性(有class)
        String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
        for (String propertyName : propertyNames) {
            // 容器中是否存在propertyName对应的对象实例或BeanDefinition
            // 由于会从singleton缓存中进行查找, 因此可以解决循环依赖的问题
            if (containsBean(propertyName)) {
                // 调用`doGetBean`方法获取bean: 可能需要初始化该bean(递归初始化)
                Object bean = getBean(propertyName);
                pvs.add(propertyName, bean);
                // 注册依赖bean: 以便可以在依赖bean销毁之前先销毁该bean
                registerDependentBean(propertyName, beanName);
                if (logger.isDebugEnabled()) {
                    logger.debug("Added autowiring by name from bean name '" + beanName +
                            "' via property '" + propertyName + "' to bean named '" + propertyName + "'");
                }
            }
            else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Not autowiring property '" + propertyName + "' of bean '" + beanName +
                            "' by name: no matching bean found");
                }
            }
        }
    }

    /**
     * byType: 对于集合类型的注入, 需要将集合中所有bean注册依赖关系
     */
    protected void autowireByType(
            String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

        TypeConverter converter = getCustomTypeConverter();
        if (converter == null) {
            converter = bw;
        }

        Set<String> autowiredBeanNames = new LinkedHashSet<>(4);
        // 寻找BeanWrapper中需要依赖注入的非简单类型属性(有class)
        String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
        for (String propertyName : propertyNames) {
            try {
                PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
                // 不注入`Object`类型
                if (Object.class != pd.getPropertyType()) {
                    MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
                    // Do not allow eager init for type matching in case of a prioritized post-processor.
                    boolean eager = !PriorityOrdered.class.isInstance(bw.getWrappedInstance());
                    DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(methodParam, eager);
                    // 解析指定类型的对象
                    // 对于集合类型的注入, 这一步会把所有用到的beanName填充到autowiredBeanNames, 需要注册所有这些bean的依赖关系
                    Object autowiredArgument = resolveDependency(desc, beanName, autowiredBeanNames, converter);
                    if (autowiredArgument != null) {
                        pvs.add(propertyName, autowiredArgument);
                    }
                    // 注册所有用到的bean的依赖关系
                    for (String autowiredBeanName : autowiredBeanNames) {
                        registerDependentBean(autowiredBeanName, beanName);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Autowiring by type from bean name '" + beanName + "' via property '" +
                                    propertyName + "' to bean named '" + autowiredBeanName + "'");
                        }
                    }
                    autowiredBeanNames.clear();
                }
            }
            catch (BeansException ex) {
                throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, propertyName, ex);
            }
        }
    }

    /**
     * 注册依赖关系: 以便bean在销毁时可以先销毁依赖它的bean
     */
    public void registerDependentBean(String beanName, String dependentBeanName) {
        String canonicalName = canonicalName(beanName);

        synchronized (this.dependentBeanMap) {
            Set<String> dependentBeans =
                    this.dependentBeanMap.computeIfAbsent(canonicalName, k -> new LinkedHashSet<>(8));
            if (!dependentBeans.add(dependentBeanName)) {
                return;
            }
        }

        synchronized (this.dependenciesForBeanMap) {
            Set<String> dependenciesForBean =
                    this.dependenciesForBeanMap.computeIfAbsent(dependentBeanName, k -> new LinkedHashSet<>(8));
            dependenciesForBean.add(canonicalName);
        }
    }

    /**
     * 初始化bean实例, 流程:
     * -> 1.执行`Aware`方法, 顺序为: BeanNameAware >> BeanClassLoaderAware >> BeanFactoryAware
     * -> 2.应用`BeanPostProcessor`的`postProcessBeforeInitialization`, 如: @PostConstruct
     * -> 3.执行初始化方法: InitializingBean.afterPropertiesSet >> init-method
     * -> 4.应用`BeanPostProcessor`的`postProcessAfterInitialization`
     */
    protected Object initializeBean(final String beanName, final Object bean, @Nullable RootBeanDefinition mbd) {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                invokeAwareMethods(beanName, bean);
                return null;
            }, getAccessControlContext());
        }
        else {
            // 执行`Aware`方法, 顺序为: BeanNameAware >> BeanClassLoaderAware >> BeanFactoryAware
            invokeAwareMethods(beanName, bean);
        }

        Object wrappedBean = bean;
        if (mbd == null || !mbd.isSynthetic()) {
            // 应用`BeanPostProcessor`的`postProcessBeforeInitialization`, 如: @PostConstruct
            wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
        }

        try {
            // 执行初始化方法: InitializingBean.afterPropertiesSet >> init-method
            // 初始化方法只允许执行一次: 因此如果InitializingBean.afterPropertiesSet同时被标记为init-method, 该方法不会再次执行
            invokeInitMethods(beanName, wrappedBean, mbd);
        }
        catch (Throwable ex) {
            throw new BeanCreationException(
                    (mbd != null ? mbd.getResourceDescription() : null),
                    beanName, "Invocation of init method failed", ex);
        }
        if (mbd == null || !mbd.isSynthetic()) {
            // 应用`BeanPostProcessor`的`postProcessAfterInitialization`
            wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        }

        return wrappedBean;
    }

    /**
     * 执行`Aware`方法: BeanNameAware, BeanClassLoaderAware, BeanFactoryAware
     */
    private void invokeAwareMethods(final String beanName, final Object bean) {
        if (bean instanceof Aware) {
            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(beanName);
            }
            if (bean instanceof BeanClassLoaderAware) {
                ClassLoader bcl = getBeanClassLoader();
                if (bcl != null) {
                    ((BeanClassLoaderAware) bean).setBeanClassLoader(bcl);
                }
            }
            if (bean instanceof BeanFactoryAware) {
                ((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
            }
        }
    }

    /**
     * 执行初始化方法: InitializingBean.afterPropertiesSet >> init-method
     */
    protected void invokeInitMethods(String beanName, final Object bean, @Nullable RootBeanDefinition mbd)
            throws Throwable {

        boolean isInitializingBean = (bean instanceof InitializingBean);
        // 如果bean是InitializingBean, 则执行其`afterPropertiesSet`方法
        if (isInitializingBean && (mbd == null || !mbd.isExternallyManagedInitMethod("afterPropertiesSet"))) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
            }
            if (System.getSecurityManager() != null) {
                try {
                    AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                        ((InitializingBean) bean).afterPropertiesSet();
                        return null;
                    }, getAccessControlContext());
                }
                catch (PrivilegedActionException pae) {
                    throw pae.getException();
                }
            }
            else {
                ((InitializingBean) bean).afterPropertiesSet();
            }
        }

        if (mbd != null && bean.getClass() != NullBean.class) {
            String initMethodName = mbd.getInitMethodName();
            if (StringUtils.hasLength(initMethodName) &&
                    !(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
                    !mbd.isExternallyManagedInitMethod(initMethodName)) {
                // 执行自定义的`init-method`: 使用反射的方式
                // 如果`init-method`方法名为`afterPropertiesSet`并bean是InitializingBean, 则该方法已经执行过一次, 不允许重复执行
                invokeCustomInitMethod(beanName, bean, mbd);
            }
        }
    }

    /**
     * 调用所有注册的BeanPostProcessor的postProcessAfterInitialization()方法
     */
    protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
        return applyBeanPostProcessorsAfterInitialization(object, beanName);
    }

}
