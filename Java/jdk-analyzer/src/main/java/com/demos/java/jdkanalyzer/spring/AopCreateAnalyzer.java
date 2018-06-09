package com.demos.java.jdkanalyzer.spring;

import com.sun.istack.internal.Nullable;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.*;
import org.springframework.aop.aspectj.annotation.*;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.aop.framework.*;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.framework.autoproxy.ProxyCreationContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.DecoratingProxy;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/31
 *
 * ===== 一、AOP术语:
 * Advice: 通知, 如: BeforeAdvice, AfterAdvice, AroundAdvice等
 * PointCut: 切点
 * Aspect: 切面
 * JoinPoint: 连接点
 * Introduction: 引入
 * Weaving: 织入
 * ===== 二、<aspectj-autoproxy />标签解析:
 * -> 1.解析<aspectj-autoproxy />标签: AspectJAutoProxyBeanDefinitionParser
 * -> 2.注册AspectJ注解处理器: AnnotationAwareAspectJAutoProxyCreator, 该处理器继承了SmartInstantiationAwareBeanPostProcessor
 * -> 3.代理设置: proxy-target-class和expose-proxy
 * ===== 三、bean初始化创建代理对象:
 * -> 1.postProcessAfterInitialization(): 初始化完成后置处理器, 创建代理对象的入口
 * -> 2.找到所有的Advisor
 * === 1).查询父类中所有的Advisor
 * === 2).找到所有的beanName: @Component
 * === 3).遍历所有的beanName找出`@Aspect`
 * === 4).解析Aspect获取Advisor列表
 * -> 3.选出bean可用的Advisor
 * === 1).如果是引介增强: 根据ClassFilter判断`targetClass`是否匹配
 * === 2).否则: 判断切点是否匹配
 * -> 4.创建代理对象
 * === 1).判断是否强制代理目标类: CGLib
 * === 2).根据bean class判断使用CGLib代理目标类还是使用JDK动态代理代理目标接口
 * === 3).根据Advisor列表和目标对象选择AopProxy(JdkDynamicAopProxy or CglibAopProxy), 并调用getProxy()创建代理对象
 * ===== 四、JdkDynamicAopProxy.invoke()方法解析: 即: InvocationHandler.invoke()
 * -> 1.特殊方法处理: equals(), hashCode()等
 * -> 2.如果需要暴露代理(expose-proxy): 设置proxy到ThreadLocal
 * -> 3.获取方法的拦截器链: 若为空则直接调用目标方法, 否则封装到ReflectiveMethodInvocation并执行拦截器链
 * ===== 五、拦截器链调用: JdkDynamicAopProxy和CglibAopProxy最终都调用ReflectiveMethodInvocation.proceed()方法执行拦截器链
 */
public class AopCreateAnalyzer {

    /**
     * 解析<aspectj-autoproxy />标签: AspectJAutoProxyBeanDefinitionParser
     * 注册AspectJ注解处理器的BeanDefinition: AnnotationAwareAspectJAutoProxyCreator, 该处理器继承了SmartInstantiationAwareBeanPostProcessor
     */
    public static void registerAspectJAnnotationAutoProxyCreatorIfNecessary(
            ParserContext parserContext, Element sourceElement) {
        // 注册AspectJ注解处理器的BeanDefinition
        BeanDefinition beanDefinition = registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext.getRegistry(), parserContext.extractSource(sourceElement));
        // 代理设置: proxy-target-class和expose-proxy
        useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
        // 注册组件: beanName = org.springframework.aop.config.internalAutoProxyCreator
        registerComponentIfNecessary(beanDefinition, parserContext);
    }

    /**
     * 注册AspectJ注解处理器的BeanDefinition: AnnotationAwareAspectJAutoProxyCreator
     */
    public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry, Object source) {
        // 注册AspectJ注解处理器的BeanDefinition: AnnotationAwareAspectJAutoProxyCreator, 该处理器继承了SmartInstantiationAwareBeanPostProcessor
        return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry, source);
    }

    /**
     * 代理设置: proxy-target-class和expose-proxy
     */
    private static void useClassProxyingIfNecessary(BeanDefinitionRegistry registry, @org.springframework.lang.Nullable Element sourceElement) {
        if (sourceElement != null) {
            // 解析`proxy-target-class`属性: true表示强制使用CGLib动态代理
            boolean proxyTargetClass = Boolean.parseBoolean(sourceElement.getAttribute(PROXY_TARGET_CLASS_ATTRIBUTE));
            if (proxyTargetClass) {
                AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
            }
            // 解析`expose-proxy`属性: true表示暴露代理对象, 使用`AopContext.currentProxy()`操作代理对象
            boolean exposeProxy = Boolean.parseBoolean(sourceElement.getAttribute(EXPOSE_PROXY_ATTRIBUTE));
            if (exposeProxy) {
                AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
            }
        }
    }

    /**
     * bean初始化完成之后, 创建bean的代理
     */
    public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) throws BeansException {
        if (bean != null) {
            // Build a cache key for the given bean class and bean name
            Object cacheKey = getCacheKey(bean.getClass(), beanName);
            if (!this.earlyProxyReferences.contains(cacheKey)) {
                // 包装bean: 增强实现
                return wrapIfNecessary(bean, beanName, cacheKey);
            }
        }
        return bean;
    }

    /**
     * 包装bean返回代理对象
     */
    protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
        // bean已经处理: 直接返回
        if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
            return bean;
        }
        // bean无序增强: 直接返回
        if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
            return bean;
        }
        // 该bean是一个`基础设施类`或者`不需要自动代理`: 跳过
        if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
            this.advisedBeans.put(cacheKey, Boolean.FALSE);
            return bean;
        }

        // 查询bean可以应用的增强方法
        Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
        // 创建代理
        if (specificInterceptors != DO_NOT_PROXY) {
            this.advisedBeans.put(cacheKey, Boolean.TRUE);
            // 创建代理对象: 织入
            Object proxy = createProxy(
                    bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
            this.proxyTypes.put(cacheKey, proxy.getClass());
            return proxy;
        }

        this.advisedBeans.put(cacheKey, Boolean.FALSE);
        return bean;
    }

    /**
     * 查询bean所有的增强方法
     */
    protected Object[] getAdvicesAndAdvisorsForBean(
            Class<?> beanClass, String beanName, @org.springframework.lang.Nullable TargetSource targetSource) {

        List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
        if (advisors.isEmpty()) {
            return DO_NOT_PROXY;
        }
        return advisors.toArray();
    }

    /**
     * 查询所有符合条件的增强方法
     */
    protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
        //
        List<Advisor> candidateAdvisors = findCandidateAdvisors();
        List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
        // 钩子方法: 子类可以添加新的Advisor进行扩展
        extendAdvisors(eligibleAdvisors);
        // 对所有的Advisor进行排序: 决定调用代理对象时增强方法的执行顺序
        if (!eligibleAdvisors.isEmpty()) {
            eligibleAdvisors = sortAdvisors(eligibleAdvisors);
        }
        return eligibleAdvisors;
    }

    /**
     * 查找所有的增强方法: 这里分析使用注解的方式: AnnotationAwareAspectJAutoProxyCreator
     * @return
     */
    protected List<Advisor> findCandidateAdvisors() {
        // 调用父类方法查找所有增强方法: 主要是在xml配置文件中定义的增强方法, 在配置文件解析阶段已经加载到BeanFactory
        List<Advisor> advisors = super.findCandidateAdvisors();
        // 创建使用AspectJ注解的增强方法
        advisors.addAll(buildAspectJAdvisors());
//        if (this.aspectJAdvisorsBuilder != null) {
//            advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
//        }
        return advisors;
    }

    /**
     * 创建基于AspectJ注解的增强方法：
     * -> 1.找到所有的beanName: @Component
     * -> 2.遍历beanName找到`@Aspect`
     * -> 3.解析`Aspect`获取`List<Advisor>`
     */
    public List<Advisor> buildAspectJAdvisors() {
        List<String> aspectNames = this.aspectBeanNames;

        if (aspectNames == null) {
            synchronized (this) {
                aspectNames = this.aspectBeanNames;
                if (aspectNames == null) {
                    List<Advisor> advisors = new LinkedList<>();
                    aspectNames = new LinkedList<>();
                    // 获取所有的beanName: 所以Aspect需要有Component注解才能在这一步被找出来
                    String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                            this.beanFactory, Object.class, true, false);
                    // 遍历所有的beanName找出Aspect的增强
                    for (String beanName : beanNames) {
                        if (!isEligibleBean(beanName)) {
                            continue;
                        }
                        // We must be careful not to instantiate beans eagerly as in this case they
                        // would be cached by the Spring container but would not have been weaved.
                        Class<?> beanType = this.beanFactory.getType(beanName);
                        if (beanType == null) {
                            continue;
                        }
                        // 如果是`Aspect`则进行处理: 类上有Aspect注解
                        if (this.advisorFactory.isAspect(beanType)) {
                            aspectNames.add(beanName);
                            AspectMetadata amd = new AspectMetadata(beanType, beanName);
                            if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
                                MetadataAwareAspectInstanceFactory factory =
                                        new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
                                // 解析增强方法
                                List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
                                if (this.beanFactory.isSingleton(beanName)) {
                                    this.advisorsCache.put(beanName, classAdvisors);
                                }
                                else {
                                    this.aspectFactoryCache.put(beanName, factory);
                                }
                                advisors.addAll(classAdvisors);
                            }
                            else {
                                // Per target or per this.
                                if (this.beanFactory.isSingleton(beanName)) {
                                    throw new IllegalArgumentException("Bean with name '" + beanName +
                                            "' is a singleton, but aspect instantiation model is not singleton");
                                }
                                MetadataAwareAspectInstanceFactory factory =
                                        new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
                                this.aspectFactoryCache.put(beanName, factory);
                                advisors.addAll(this.advisorFactory.getAdvisors(factory));
                            }
                        }
                    }
                    this.aspectBeanNames = aspectNames;
                    return advisors;
                }
            }
        }

        if (aspectNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<Advisor> advisors = new LinkedList<>();
        for (String aspectName : aspectNames) {
            List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
            if (cachedAdvisors != null) {
                advisors.addAll(cachedAdvisors);
            }
            else {
                MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
                advisors.addAll(this.advisorFactory.getAdvisors(factory));
            }
        }
        return advisors;
    }

    /**
     * 从候选的Advisor中找出可以应用到给定bean的Advisor
     */
    protected List<Advisor> findAdvisorsThatCanApply(
            List<Advisor> candidateAdvisors, Class<?> beanClass, String beanName) {

        ProxyCreationContext.setCurrentProxiedBeanName(beanName);
        try {
            return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
        }
        finally {
            ProxyCreationContext.setCurrentProxiedBeanName(null);
        }
    }

    /**
     * 从候选的Advisor中找出可以应用到bean class的Advisor: 匹配切点
     */
    public static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> clazz) {
        if (candidateAdvisors.isEmpty()) {
            return candidateAdvisors;
        }
        List<Advisor> eligibleAdvisors = new LinkedList<>();
        for (Advisor candidate : candidateAdvisors) {
            // 先处理引介增强: 目标对象未实现的方法
            if (candidate instanceof IntroductionAdvisor && canApply(candidate, clazz)) {
                eligibleAdvisors.add(candidate);
            }
        }
        boolean hasIntroductions = !eligibleAdvisors.isEmpty();
        for (Advisor candidate : candidateAdvisors) {
            if (candidate instanceof IntroductionAdvisor) {
                // already processed
                continue;
            }
            // 处理普通增强
            if (canApply(candidate, clazz, hasIntroductions)) {
                eligibleAdvisors.add(candidate);
            }
        }
        return eligibleAdvisors;
    }

    /**
     * 判断`advisor`是否能应用到`targetClass`上
     */
    public static boolean canApply(Advisor advisor, Class<?> targetClass) {
        return canApply(advisor, targetClass, false);
    }

    /**
     * 判断`advisor`是否能应用到`targetClass`上
     */
    public static boolean canApply(Advisor advisor, Class<?> targetClass, boolean hasIntroductions) {
        // 如果是引介增强: 根据ClassFilter判断`targetClass`是否匹配
        if (advisor instanceof IntroductionAdvisor) {
            return ((IntroductionAdvisor) advisor).getClassFilter().matches(targetClass);
        }
        // 否则: 判断切点是否匹配
        else if (advisor instanceof PointcutAdvisor) {
            PointcutAdvisor pca = (PointcutAdvisor) advisor;
            return canApply(pca.getPointcut(), targetClass, hasIntroductions);
        }
        else {
            // It doesn't have a pointcut so we assume it applies.
            return true;
        }
    }

    /**
     * 创建代理对象
     */
    protected Object createProxy(Class<?> beanClass, @Nullable String beanName,
                                 @Nullable Object[] specificInterceptors, TargetSource targetSource) {

        if (this.beanFactory instanceof ConfigurableListableBeanFactory) {
            AutoProxyUtils.exposeTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName, beanClass);
        }

        // 每次创建一个新的代理工厂用来创建代理对象
        ProxyFactory proxyFactory = new ProxyFactory();
        // 复制当前工厂中的相关属性
        proxyFactory.copyFrom(this);

        // 判断是否强制代理目标类
        if (!proxyFactory.isProxyTargetClass()) {
            // 如果需要代理目标类: 使用CGLib
            if (shouldProxyTargetClass(beanClass, beanName)) {
                proxyFactory.setProxyTargetClass(true);
            }
            // 否则: 代理接口, 使用JDK动态代理
            else {
                evaluateProxyInterfaces(beanClass, proxyFactory);
            }
        }

        // 加入Advisor
        Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
        proxyFactory.addAdvisors(advisors);
        // 设置代理对象
        proxyFactory.setTargetSource(targetSource);
        // 钩子方法: 定制代理
        customizeProxyFactory(proxyFactory);

        proxyFactory.setFrozen(this.freezeProxy);
        if (advisorsPreFiltered()) {
            proxyFactory.setPreFiltered(true);
        }

        // 使用JdkDynamicAopProxy或CglibAopProxy创建代理对象
        return getProxy(getProxyClassLoader());
//        return proxyFactory.getProxy(getProxyClassLoader());
    }

    /**
     * 检查代理接口
     */
    protected void evaluateProxyInterfaces(Class<?> beanClass, ProxyFactory proxyFactory) {
        // 查询目标class的所有接口
        Class<?>[] targetInterfaces = ClassUtils.getAllInterfacesForClass(beanClass, getProxyClassLoader());
        boolean hasReasonableProxyInterface = false;
        for (Class<?> ifc : targetInterfaces) {
            if (!isConfigurationCallbackInterface(ifc) && !isInternalLanguageInterface(ifc) &&
                    ifc.getMethods().length > 0) {
                hasReasonableProxyInterface = true;
                break;
            }
        }
        // 有需要代理的接口则添加到ProxyFactory
        if (hasReasonableProxyInterface) {
            // Must allow for introductions; can't just set interfaces to the target's interfaces only.
            for (Class<?> ifc : targetInterfaces) {
                proxyFactory.addInterface(ifc);
            }
        }
        // 否则代理目标类: CGLib
        else {
            proxyFactory.setProxyTargetClass(true);
        }
    }

    /**
     * 1.创建AopProxy: JdkDynamicAopProxy or CglibAopProxy
     * 2.调用AopProxy.getProxy()创建代理对象
     */
    public Object getProxy(ClassLoader classLoader) {
        return createAopProxy().getProxy(classLoader);
    }

    /**
     * JdkDynamicAopProxy.invoke(): 即: InvocationHandler.invoke(), JdkDynamicAopProxy实现了AopProxy和InvocationHandler接口
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodInvocation invocation;
        Object oldProxy = null;
        boolean setProxyContext = false;

        TargetSource targetSource = this.advised.targetSource;
        Object target = null;

        try {
            // equals()方法
            if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {
                // The target does not implement the equals(Object) method itself.
                return equals(args[0]);
            }
            // hashCode()方法
            else if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
                // The target does not implement the hashCode() method itself.
                return hashCode();
            }
            else if (method.getDeclaringClass() == DecoratingProxy.class) {
                // There is only getDecoratedClass() declared -> dispatch to proxy config.
                return AopProxyUtils.ultimateTargetClass(this.advised);
            }
            else if (!this.advised.opaque && method.getDeclaringClass().isInterface() &&
                    method.getDeclaringClass().isAssignableFrom(Advised.class)) {
                // Service invocations on ProxyConfig with the proxy config...
                return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
            }

            Object retVal;

            // 如果需要暴露代理对象则设置proxy到ThreadLocal
            if (this.advised.exposeProxy) {
                // 设置proxy到ThreadLocal以便暴露proxy对象
                oldProxy = AopContext.setCurrentProxy(proxy);
                setProxyContext = true;
            }

            // Get as late as possible to minimize the time we "own" the target,
            // in case it comes from a pool.
            target = targetSource.getTarget();
            Class<?> targetClass = (target != null ? target.getClass() : null);

            // 获取该方法的拦截器链
            List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

            // 拦截器链为空则直接调用目标方法
            if (chain.isEmpty()) {
                // We can skip creating a MethodInvocation: just invoke the target directly
                // Note that the final invoker must be an InvokerInterceptor so we know it does
                // nothing but a reflective operation on the target, and no hot swapping or fancy proxying.
                Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
                retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
            }
            // 否则执行拦截器链
            else {
                // 将拦截器链封装到ReflectiveMethodInvocation并调用器proceed()方法
                retVal = proceed();
//                invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
//                retVal = invocation.proceed();
            }

            // Massage return value if necessary.
            Class<?> returnType = method.getReturnType();
            if (retVal != null && retVal == target &&
                    returnType != Object.class && returnType.isInstance(proxy) &&
                    !RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
                // Special case: it returned "this" and the return type of the method
                // is type-compatible. Note that we can't help if the target sets
                // a reference to itself in another returned object.
                retVal = proxy;
            }
            else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
                throw new AopInvocationException(
                        "Null return value from advice does not match primitive return type for: " + method);
            }
            return retVal;
        }
        finally {
            if (target != null && !targetSource.isStatic()) {
                // Must have come from TargetSource.
                targetSource.releaseTarget(target);
            }
            if (setProxyContext) {
                // Restore old proxy.
                AopContext.setCurrentProxy(oldProxy);
            }
        }
    }

    /**
     * 拦截器链执行
     */
    public Object proceed() throws Throwable {
        //	We start with an index of -1 and increment early.
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            return invokeJoinpoint();
        }

        // 获取下一个要执行的拦截器
        Object interceptorOrInterceptionAdvice =
                this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
        if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
            // Evaluate dynamic method matcher here: static part will already have
            // been evaluated and found to match.
            InterceptorAndDynamicMethodMatcher dm =
                    (InterceptorAndDynamicMethodMatcher) interceptorOrInterceptionAdvice;
            if (dm.methodMatcher.matches(this.method, this.targetClass, this.arguments)) {
                return dm.interceptor.invoke(this);
            }
            else {
                // 动态匹配失败: 跳过当前拦截器并执行拦截器链中的下一个
                return proceed();
            }
        }
        else {
            // 普通拦截器: 直接调用拦截器, 如: AspectJAroundAdvice, AspectJAfterAdvice等
            return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
        }
    }

}
