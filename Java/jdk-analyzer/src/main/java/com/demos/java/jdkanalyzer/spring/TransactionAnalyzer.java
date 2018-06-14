package com.demos.java.jdkanalyzer.spring;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Constants;
import org.springframework.core.MethodClassKey;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.lang.Nullable;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.interceptor.*;
import org.springframework.transaction.support.*;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/11
 *
 * ===== 1.事务配置:
 * -> TransactionAttributeSource: 提供事务属性(TransactionAttribute), 包括隔离级别、传播行为、超时时间等, 由TransactionInterceptor调用
 * -> TransactionInterceptor: 事务拦截器, 提供事务功能
 * -> TransactionAttributeSourceAdvisor: 事务增强Advisor, 负责事务的AOP织入
 * ===== 2.事务管理:
 * -> PlatformTransactionManager: 事务管理器
 * -> TransactionDefinition: 事务属性(隔离级别、传播行为等), 常用TransactionAttribute类
 * -> TransactionStatus: 事务运行状态
 * ===== 3.声明式事务使用示例(transactionManager使用依赖注入):
    // 创建TransactionDefinition
    TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    // 从PlatformTransactionManager获取TransactionStatus
    TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
    try {
        ... // 业务处理
        transactionManager.commit(transactionStatus); // 事务提交
    } catch (Exception e) {
        transactionManager.rollback(transactionStatus); // 事务回滚
    }
 * ===== 4.事务注解三种方式
 * -> spring: org.springframework.transaction.annotation.Transactional: 最常使用
 * -> ejb3: javax.ejb.TransactionAttribute
 * -> jta1.2: javax.transaction.Transactional
 * ===== 5.执行事务方法:
 * -> 1.获取事务管理器: PlatformTransactionManager, 如常用的DataSourceTransactionManager
 * -> 2.解析事务属性: 优先从Class获取, 若不存在再从Method获取属性信息(三种注解方式: 常用Spring的Transactional)
 * -> 3.创建事务信息TransactionInfo并设置到当前线程(ThreadLocal), 同时TransactionInfo会保存旧的事务信息以便恢复之前的事务(处理嵌套事务)
 * -> 4.执行目标方法
 * -> 5.若抛出异常, 则检查是否需要回滚, 如果需要回滚则执行事务回滚, 否则提交当前事务
 * -> 6.若方法正常返回则提交事务
 * -> 7.将原来的事务信息设置到当前线程以便恢复之前的事务(处理嵌套事务)
 * ===== 6.开启事务流程:
 * -> 1.获取数据库连接
 * -> 2.设置并返回当前的隔离级别
 * -> 3.取消自动提交事务, 使用手动提交
 * -> 4.准备事务连接: 如果设置了事务为`readOnly`, 则会执行SQL语句: SET TRANSACTION READ ONLY
 * -> 5.设置状态表明当前正在事务中
 * -> 6.设置超时时间(timeout)
 * -> 7.保存连接的holder到当前线程
 * ===== 7.七种传播行为:
 * -> PROPAGATION_REQUIRED: 需要一个事务, 如果当前存在事务, 就加入该事务, 否则创建一个新事务(最常使用)
 * -> PROPAGATION_SUPPORTS: 支持当前事务, 如果当前存在事务, 就加入该事务, 否则以非事务运行
 * -> PROPAGATION_MANDATORY: 当前必须存在事务, 如果当前存在事务, 就加入该事务, 否则抛出异常
 * -> PROPAGATION_REQUIRES_NEW: 需要一个新事务, 无论当前是否存在事务, 都会创建一个新事务
 * -> PROPAGATION_NOT_SUPPORTED: 不支持事务, 以非事务方式运行, 如果当前存在事务, 就把当前事务挂起
 * -> PROPAGATION_NEVER: 禁止事务, 以非事务方式运行, 如果当前存在事务, 则抛出异常
 * -> PROPAGATION_NESTED: 嵌套事务, 如果当前存在事务, 则在嵌套事务内运行, 否则创建一个新事务
 * === 当前存在事务的处理方式:
 * -> PROPAGATION_NEVER: 不允许存在事务, 抛出异常
 * -> PROPAGATION_NOT_SUPPORTED: 不支持事务, 将当前事务挂起
 * -> PROPAGATION_REQUIRES_NEW: 需要新的事务, 挂起当前事务, 并开启一个新的事务
 * -> PROPAGATION_NESTED: 嵌套事务
 * -> PROPAGATION_SUPPORTS or PROPAGATION_REQUIRED: 加入当前事务, 如果当前事务的隔离级别和自己的不一致, 则抛出异常
 */
public class TransactionAnalyzer {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        // 注册广播监听: internalTransactionalEventListenerFactory
        registerTransactionalEventListenerFactory(parserContext);
        String mode = element.getAttribute("mode");
        if ("aspectj".equals(mode)) {
            // mode="aspectj"
            registerTransactionAspect(element, parserContext);
        }
        else {
            // mode="proxy"
            AopAutoProxyConfigurer.configureAutoProxyCreator(element, parserContext);
        }
        return null;
    }

    /**
     * 内部类: 配置事务
     * -> TransactionAttributeSource: 提供事务属性(TransactionAttribute), 包括隔离级别、传播行为、超时时间等, 由TransactionInterceptor调用
     * -> TransactionInterceptor: 事务拦截器, 提供事务功能
     * -> TransactionAttributeSourceAdvisor: 事务增强Advisor, 负责事务的AOP织入
     */
    private static class AopAutoProxyConfigurer {

        public static void configureAutoProxyCreator(Element element, ParserContext parserContext) {
            AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);

            String txAdvisorBeanName = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME;
            if (!parserContext.getRegistry().containsBeanDefinition(txAdvisorBeanName)) {
                Object eleSource = parserContext.extractSource(element);

                // TransactionAttributeSource: 提供事务属性(TransactionAttribute), 包括隔离级别、传播行为、超时时间等, 由TransactionInterceptor调用
                RootBeanDefinition sourceDef = new RootBeanDefinition(
                        "org.springframework.transaction.annotation.AnnotationTransactionAttributeSource");
                sourceDef.setSource(eleSource);
                sourceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                // 使用生成的beanName注册bean
                String sourceName = parserContext.getReaderContext().registerWithGeneratedName(sourceDef);

                // TransactionInterceptor: 事务拦截器, 提供事务功能
                RootBeanDefinition interceptorDef = new RootBeanDefinition(TransactionInterceptor.class);
                interceptorDef.setSource(eleSource);
                interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                registerTransactionManager(element, interceptorDef);
                interceptorDef.getPropertyValues().add("transactionAttributeSource", new RuntimeBeanReference(sourceName));
                String interceptorName = parserContext.getReaderContext().registerWithGeneratedName(interceptorDef);

                // TransactionAttributeSourceAdvisor: 事务增强Advisor, 负责事务的AOP织入
                RootBeanDefinition advisorDef = new RootBeanDefinition(BeanFactoryTransactionAttributeSourceAdvisor.class);
                advisorDef.setSource(eleSource);
                advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                advisorDef.getPropertyValues().add("transactionAttributeSource", new RuntimeBeanReference(sourceName));
                advisorDef.getPropertyValues().add("adviceBeanName", interceptorName);
                if (element.hasAttribute("order")) {
                    advisorDef.getPropertyValues().add("order", element.getAttribute("order"));
                }
                parserContext.getRegistry().registerBeanDefinition(txAdvisorBeanName, advisorDef);

                CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), eleSource);
                compositeDef.addNestedComponent(new BeanComponentDefinition(sourceDef, sourceName));
                compositeDef.addNestedComponent(new BeanComponentDefinition(interceptorDef, interceptorName));
                compositeDef.addNestedComponent(new BeanComponentDefinition(advisorDef, txAdvisorBeanName));
                parserContext.registerComponent(compositeDef);
            }
        }
    }

    /**
     * 1.TransactionAttributeSource.getTransactionAttribute(): 解析事务属性
     * 首先尝试从缓存中获取
     */
    public TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }

        // 首先尝试从缓存中获取事务属性
        Object cacheKey = getCacheKey(method, targetClass);
        Object cached = this.attributeCache.get(cacheKey);
        if (cached != null) {
            // Value will either be canonical value indicating there is no transaction attribute,
            // or an actual transaction attribute.
            if (cached == NULL_TRANSACTION_ATTRIBUTE) {
                return null;
            }
            else {
                return (TransactionAttribute) cached;
            }
        }
        // 缓存中不存在则从Method或Class中解析出事务属性: 优先从方法中解析
        else {
            // 解析事务属性
            TransactionAttribute txAttr = computeTransactionAttribute(method, targetClass);
            // 事务属性不为空则放入缓存
            if (txAttr == null) {
                this.attributeCache.put(cacheKey, NULL_TRANSACTION_ATTRIBUTE);
            }
            else {
                String methodIdentification = ClassUtils.getQualifiedMethodName(method, targetClass);
                if (txAttr instanceof DefaultTransactionAttribute) {
                    ((DefaultTransactionAttribute) txAttr).setDescriptor(methodIdentification);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding transactional method '" + methodIdentification + "' with attribute: " + txAttr);
                }
                this.attributeCache.put(cacheKey, txAttr);
            }
            return txAttr;
        }
    }

    /**
     * 事务属性的缓存key
     */
    protected Object getCacheKey(Method method, @Nullable Class<?> targetClass) {
        return new MethodClassKey(method, targetClass);
    }

    /**
     * 从Class或Method中解析出事务属性: 优先从方法中解析
     */
    protected TransactionAttribute computeTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
        // 是否设置只允许`public`的方法使用事务
        if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
            return null;
        }

        // The method may be on an interface, but we need attributes from the target class.
        // If the target class is null, the method will be unchanged.
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

        // 首先尝试从方法中解析事务属性
        TransactionAttribute txAttr = findTransactionAttribute(specificMethod);
        if (txAttr != null) {
            return txAttr;
        }

        // 方法中没有则从类上解析事务属性
        txAttr = findTransactionAttribute(specificMethod.getDeclaringClass());
        if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {
            return txAttr;
        }

        if (specificMethod != method) {
            // Fallback is to look at the original method.
            txAttr = findTransactionAttribute(method);
            if (txAttr != null) {
                return txAttr;
            }
            // Last fallback is the class of the original method.
            txAttr = findTransactionAttribute(method.getDeclaringClass());
            if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {
                return txAttr;
            }
        }

        return null;
    }

    /**
     * 解析方法上的事务注解
     */
    protected TransactionAttribute findTransactionAttribute(Method method) {
        return determineTransactionAttribute(method);
    }

    /**
     * 解析类上的事务注解
     */
    protected TransactionAttribute findTransactionAttribute(Class<?> clazz) {
        return determineTransactionAttribute(clazz);
    }

    /**
     * 解析事务属性, 支持三种注解方式:
     * -> spring: org.springframework.transaction.annotation.Transactional: 最常使用
     * -> ejb3: javax.ejb.TransactionAttribute
     * -> jta1.2: javax.transaction.Transactional
     */
    protected TransactionAttribute determineTransactionAttribute(AnnotatedElement ae) {
        for (TransactionAnnotationParser annotationParser : this.annotationParsers) {
            TransactionAttribute attr = annotationParser.parseTransactionAnnotation(ae);
            if (attr != null) {
                return attr;
            }
        }
        return null;
    }

    /**
     * SpringTransactionAnnotationParser.parseTransactionAnnotation(): 解析spring的事务注解
     */
    public TransactionAttribute parseTransactionAnnotation(AnnotatedElement ae) {
        AnnotationAttributes attributes = AnnotatedElementUtils.findMergedAnnotationAttributes(
                ae, Transactional.class, false, false);
        if (attributes != null) {
            return parseTransactionAnnotation(attributes);
        }
        else {
            return null;
        }
    }

    /**
     * 解析@Transactional注解
     */
    protected TransactionAttribute parseTransactionAnnotation(AnnotationAttributes attributes) {
        RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
        Propagation propagation = attributes.getEnum("propagation");
        rbta.setPropagationBehavior(propagation.value());
        Isolation isolation = attributes.getEnum("isolation");
        rbta.setIsolationLevel(isolation.value());
        rbta.setTimeout(attributes.getNumber("timeout").intValue());
        rbta.setReadOnly(attributes.getBoolean("readOnly"));
        rbta.setQualifier(attributes.getString("value"));
        ArrayList<RollbackRuleAttribute> rollBackRules = new ArrayList<>();
        Class<?>[] rbf = attributes.getClassArray("rollbackFor");
        for (Class<?> rbRule : rbf) {
            RollbackRuleAttribute rule = new RollbackRuleAttribute(rbRule);
            rollBackRules.add(rule);
        }
        String[] rbfc = attributes.getStringArray("rollbackForClassName");
        for (String rbRule : rbfc) {
            RollbackRuleAttribute rule = new RollbackRuleAttribute(rbRule);
            rollBackRules.add(rule);
        }
        Class<?>[] nrbf = attributes.getClassArray("noRollbackFor");
        for (Class<?> rbRule : nrbf) {
            NoRollbackRuleAttribute rule = new NoRollbackRuleAttribute(rbRule);
            rollBackRules.add(rule);
        }
        String[] nrbfc = attributes.getStringArray("noRollbackForClassName");
        for (String rbRule : nrbfc) {
            NoRollbackRuleAttribute rule = new NoRollbackRuleAttribute(rbRule);
            rollBackRules.add(rule);
        }
        rbta.getRollbackRules().addAll(rollBackRules);
        return rbta;
    }

    /**
     * 2.TransactionInterceptor.invoke(): 事务拦截器
     */
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        // Work out the target class: may be {@code null}.
        // The TransactionAttributeSource should be passed the target class
        // as well as the method, which may be from an interface.
        Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);

        // Adapt to TransactionAspectSupport's invokeWithinTransaction...
        return invokeWithinTransaction(invocation.getMethod(), targetClass, invocation::proceed);
    }

    /**
     * 执行事务方法:
     * -> 1.获取事务管理器: PlatformTransactionManager, 如常用的DataSourceTransactionManager
     * -> 2.解析事务属性: 优先从Class获取, 若不存在再从Method获取属性信息()
     * === 事务属性, 支持三种注解方式:
     * === spring: org.springframework.transaction.annotation.Transactional: 最常使用
     * === ejb3: javax.ejb.TransactionAttribute
     * === jta1.2: javax.transaction.Transactional
     * -> 2.创建事务信息TransactionInfo并设置到当前线程(ThreadLocal), 同时TransactionInfo会保存旧的事务信息以便恢复之前的事务(处理嵌套事务)
     * -> 3.执行目标方法
     * -> 4.若抛出异常, 则检查是否需要回滚, 如果需要回滚则执行事务回滚, 否则提交当前事务
     * -> 5.若方法正常返回则提交事务
     * -> 6.将原来的事务信息设置到当前线程以便恢复之前的事务(处理嵌套事务)
     */
    protected Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
                                             final InvocationCallback invocation) throws Throwable {

        // If the transaction attribute is null, the method is non-transactional.
        // 调用TransactionAttributeSource.getTransactionAttribute()方法获取事务的属性: 若事务属性为null, 说明该方法不是事务型方法
        TransactionAttributeSource tas = getTransactionAttributeSource();
        final TransactionAttribute txAttr = (tas != null ? tas.getTransactionAttribute(method, targetClass) : null);
        // 获取BeanFactory中的事务管理器(TransactionalManager), 如常用的DataSourceTransactionManager
        final PlatformTransactionManager tm = determineTransactionManager(txAttr);
        // 构造方法唯一标识: 对于类是: 类全限定名, 对于方法是: 类全限定名.方法名
        final String joinpointIdentification = methodIdentification(method, targetClass, txAttr);

        // 常用的DataSourceTransactionManager就会进入这个分支
        if (txAttr == null || !(tm instanceof CallbackPreferringPlatformTransactionManager)) {
            // 创建事务信息并设置到当前线程(ThreadLocal)
            TransactionInfo txInfo = createTransactionIfNecessary(tm, txAttr, joinpointIdentification);
            Object retVal = null;
            try {
                // This is an around advice: Invoke the next interceptor in the chain.
                // This will normally result in a target object being invoked.
                // 执行目标方法
                retVal = invocation.proceedWithInvocation();
            }
            catch (Throwable ex) {
                // 执行事务方法抛出异常: 如果当前异常需要回滚则执行事务回滚, 否则提交当前事务
                completeTransactionAfterThrowing(txInfo, ex);
                throw ex;
            }
            finally {
                cleanupTransactionInfo(txInfo);
            }
            commitTransactionAfterReturning(txInfo);
            return retVal;
        }

        // 使用回调式事务管理器进行事务处理: CallbackPreferringPlatformTransactionManager
        else {
            final ThrowableHolder throwableHolder = new ThrowableHolder();

            // It's a CallbackPreferringPlatformTransactionManager: pass a TransactionCallback in.
            try {
                Object result = ((CallbackPreferringPlatformTransactionManager) tm).execute(txAttr, status -> {
                    // 创建事务信息
                    TransactionInfo txInfo = prepareTransactionInfo(tm, txAttr, joinpointIdentification, status);
                    try {
                        // 执行目标方法
                        return invocation.proceedWithInvocation();
                    }
                    catch (Throwable ex) {
                        // 如果当前异常需要回滚, 则抛出运行时异常
                        if (txAttr.rollbackOn(ex)) {
                            // 抛出运行时异常以便回滚
                            if (ex instanceof RuntimeException) {
                                throw (RuntimeException) ex;
                            }
                            // 不是运行时异常则包装成运行时异常抛出
                            else {
                                throw new ThrowableHolderException(ex);
                            }
                        }
                        // 否则将异常设置到holder
                        else {
                            // A normal return value: will lead to a commit.
                            throwableHolder.throwable = ex;
                            return null;
                        }
                    }
                    finally {
                        cleanupTransactionInfo(txInfo);
                    }
                });

                // 如果holder中的异常不为空则抛出
                if (throwableHolder.throwable != null) {
                    throw throwableHolder.throwable;
                }
                return result;
            }
            catch (ThrowableHolderException ex) {
                throw ex.getCause();
            }
            catch (TransactionSystemException ex2) {
                if (throwableHolder.throwable != null) {
                    logger.error("Application exception overridden by commit exception", throwableHolder.throwable);
                    ex2.initApplicationException(throwableHolder.throwable);
                }
                throw ex2;
            }
            catch (Throwable ex2) {
                if (throwableHolder.throwable != null) {
                    logger.error("Application exception overridden by commit exception", throwableHolder.throwable);
                }
                throw ex2;
            }
        }
    }

    /**
     * 返回TransactionManager
     */
    @Nullable
    protected PlatformTransactionManager determineTransactionManager(@Nullable TransactionAttribute txAttr) {
        // Do not attempt to lookup tx manager if no tx attributes are set
        if (txAttr == null || this.beanFactory == null) {
            return getTransactionManager();
        }

        String qualifier = txAttr.getQualifier();
        if (StringUtils.hasText(qualifier)) {
            return determineQualifiedTransactionManager(this.beanFactory, qualifier);
        }
        else if (StringUtils.hasText(this.transactionManagerBeanName)) {
            return determineQualifiedTransactionManager(this.beanFactory, this.transactionManagerBeanName);
        }
        else {
            PlatformTransactionManager defaultTransactionManager = getTransactionManager();
            if (defaultTransactionManager == null) {
                defaultTransactionManager = this.transactionManagerCache.get(DEFAULT_TRANSACTION_MANAGER_KEY);
                if (defaultTransactionManager == null) {
                    defaultTransactionManager = this.beanFactory.getBean(PlatformTransactionManager.class);
                    this.transactionManagerCache.putIfAbsent(
                            DEFAULT_TRANSACTION_MANAGER_KEY, defaultTransactionManager);
                }
            }
            return defaultTransactionManager;
        }
    }

    /**
     * 获取事务管理器: TransactionalManager, 常用事务管理器有: DataSourceTransactionManager
     */
    private PlatformTransactionManager determineQualifiedTransactionManager(BeanFactory beanFactory, String qualifier) {
        PlatformTransactionManager txManager = this.transactionManagerCache.get(qualifier);
        if (txManager == null) {
            txManager = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
                    beanFactory, PlatformTransactionManager.class, qualifier);
            this.transactionManagerCache.putIfAbsent(qualifier, txManager);
        }
        return txManager;
    }

    /**
     * 构造方法唯一标识: 对于类是: 类全限定名, 对于方法是: 类全限定名.方法名
     */
    private String methodIdentification(Method method, @Nullable Class<?> targetClass,
                                        @Nullable TransactionAttribute txAttr) {

        String methodIdentification = methodIdentification(method, targetClass);
        if (methodIdentification == null) {
            if (txAttr instanceof DefaultTransactionAttribute) {
                methodIdentification = ((DefaultTransactionAttribute) txAttr).getDescriptor();
            }
            if (methodIdentification == null) {
                methodIdentification = ClassUtils.getQualifiedMethodName(method, targetClass);
            }
        }
        return methodIdentification;
    }

    /**
     * 创建事务信息
     */
    protected TransactionInfo createTransactionIfNecessary(@Nullable PlatformTransactionManager tm,
                                                                                    @Nullable TransactionAttribute txAttr, final String joinpointIdentification) {

        // If no name specified, apply method identification as transaction name.
        if (txAttr != null && txAttr.getName() == null) {
            txAttr = new DelegatingTransactionAttribute(txAttr) {
                @Override
                public String getName() {
                    return joinpointIdentification;
                }
            };
        }

        TransactionStatus status = null;
        if (txAttr != null) {
            if (tm != null) {
                status = tm.getTransaction(txAttr);
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping transactional joinpoint [" + joinpointIdentification +
                            "] because no transaction manager has been configured");
                }
            }
        }
        return prepareTransactionInfo(tm, txAttr, joinpointIdentification, status);
    }

    /**
     * 创建事务信息, 并保存到当前线程(ThreadLocal)
     */
    protected TransactionInfo prepareTransactionInfo(@Nullable PlatformTransactionManager tm,
                                                                              @Nullable TransactionAttribute txAttr, String joinpointIdentification,
                                                                              @Nullable TransactionStatus status) {

        TransactionInfo txInfo = new TransactionInfo(tm, txAttr, joinpointIdentification);
        if (txAttr != null) {
            // We need a transaction for this method...
            if (logger.isTraceEnabled()) {
                logger.trace("Getting transaction for [" + txInfo.getJoinpointIdentification() + "]");
            }
            // The transaction manager will flag an error if an incompatible tx already exists.
            txInfo.newTransactionStatus(status);
        }
        else {
            // The TransactionInfo.hasTransaction() method will return false. We created it only
            // to preserve the integrity of the ThreadLocal stack maintained in this class.
            if (logger.isTraceEnabled())
                logger.trace("Don't need to create transaction for [" + joinpointIdentification +
                        "]: This method isn't transactional.");
        }

        // We always bind the TransactionInfo to the thread, even if we didn't create
        // a new transaction here. This guarantees that the TransactionInfo stack
        // will be managed correctly even if no transaction was created by this aspect.
        // 将事务信息保存到当前线程: ThreadLocal
        txInfo.bindToThread();
        return txInfo;
    }

    /**
     * 方法正常返回则提交事务
     */
    protected void commitTransactionAfterReturning(@Nullable TransactionInfo txInfo) {
        if (txInfo != null && txInfo.getTransactionStatus() != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Completing transaction for [" + txInfo.getJoinpointIdentification() + "]");
            }
            txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
        }
    }

    /**
     * 事务方法异常处理: 如果当前异常需要回滚则执行事务回滚, 否则提交当前事务
     */
    protected void completeTransactionAfterThrowing(@Nullable TransactionInfo txInfo, Throwable ex) {
        if (txInfo != null && txInfo.getTransactionStatus() != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Completing transaction for [" + txInfo.getJoinpointIdentification() +
                        "] after exception: " + ex);
            }
            // 如果当前异常需要回滚则执行事务回滚
            if (txInfo.transactionAttribute != null && txInfo.transactionAttribute.rollbackOn(ex)) {
                try {
                    txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());
                }
                catch (TransactionSystemException ex2) {
                    logger.error("Application exception overridden by rollback exception", ex);
                    ex2.initApplicationException(ex);
                    throw ex2;
                }
                catch (RuntimeException | Error ex2) {
                    logger.error("Application exception overridden by rollback exception", ex);
                    throw ex2;
                }
            }
            // 否则提交当前事务
            else {
                // We don't roll back on this exception.
                // Will still roll back if TransactionStatus.isRollbackOnly() is true.
                try {
                    txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
                }
                catch (TransactionSystemException ex2) {
                    logger.error("Application exception overridden by commit exception", ex);
                    ex2.initApplicationException(ex);
                    throw ex2;
                }
                catch (RuntimeException | Error ex2) {
                    logger.error("Application exception overridden by commit exception", ex);
                    throw ex2;
                }
            }
        }
    }

    /**
     * 将原来的事务信息设置到当前线程: 主要是为了处理嵌套事务
     */
    protected void cleanupTransactionInfo(@Nullable TransactionInfo txInfo) {
        if (txInfo != null) {
            txInfo.restoreThreadLocalStatus();
        }
    }

    /**
     * 保存事务信息
     */
    protected final class TransactionInfo {

        // 事务管理器
        private final PlatformTransactionManager transactionManager;
        // 事务属性
        private final TransactionAttribute transactionAttribute;
        // 方法标识
        private final String joinpointIdentification;
        // 事务运行状态
        private TransactionStatus transactionStatus;

        // 之前的事务信息
        private TransactionInfo oldTransactionInfo;

        public TransactionInfo(@Nullable PlatformTransactionManager transactionManager,
                               @Nullable TransactionAttribute transactionAttribute, String joinpointIdentification) {
            this.transactionManager = transactionManager;
            this.transactionAttribute = transactionAttribute;
            this.joinpointIdentification = joinpointIdentification;
        }

        /**
         * Return a String representation of this joinpoint (usually a Method call)
         * for use in logging.
         */
        public String getJoinpointIdentification() {
            return this.joinpointIdentification;
        }

        public void newTransactionStatus(@Nullable TransactionStatus status) {
            this.transactionStatus = status;
        }

        @Nullable
        public TransactionStatus getTransactionStatus() {
            return this.transactionStatus;
        }

        /**
         * 将事务信息保存到当前线程: ThreadLocal
         */
        private void bindToThread() {
            // Expose current TransactionStatus, preserving any existing TransactionStatus
            // for restoration after this transaction is complete.
            this.oldTransactionInfo = transactionInfoHolder.get();
            transactionInfoHolder.set(this);
        }

        /**
         * 将旧的事务设置到当前线程
         */
        private void restoreThreadLocalStatus() {
            // Use stack to restore old transaction TransactionInfo.
            // Will be null if none was set.
            transactionInfoHolder.set(this.oldTransactionInfo);
        }

    }

    /**
     * 将检查异常包装成运行时异常
     */
    @SuppressWarnings("serial")
    private static class ThrowableHolderException extends RuntimeException {

        public ThrowableHolderException(Throwable throwable) {
            super(throwable);
        }

        @Override
        public String toString() {
            return getCause().toString();
        }
    }

    /**
     * PlatformTransactionManager解析 =======================
     * 获取事务信息: 根据传播行为进行处理
     */
    public final TransactionStatus getTransaction(@Nullable TransactionDefinition definition) throws TransactionException {
        Object transaction = doGetTransaction();

        // Cache debug flag to avoid repeated checks.
        boolean debugEnabled = logger.isDebugEnabled();

        if (definition == null) {
            // Use defaults if no transaction definition given.
            definition = new DefaultTransactionDefinition();
        }

        if (isExistingTransaction(transaction)) {
            // Existing transaction found -> check propagation behavior to find out how to behave.
            return handleExistingTransaction(definition, transaction, debugEnabled);
        }

        // Check definition settings for new transaction.
        if (definition.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) {
            throw new InvalidTimeoutException("Invalid transaction timeout", definition.getTimeout());
        }

        // PROPAGATION_MANDATORY: 必须存在事务且当前不存在事务, 抛出异常
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
            throw new IllegalTransactionStateException(
                    "No existing transaction found for transaction marked with propagation 'mandatory'");
        }
        else if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
                definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
                definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
            AbstractPlatformTransactionManager.SuspendedResourcesHolder suspendedResources = suspend(null);
            if (debugEnabled) {
                logger.debug("Creating new transaction with name [" + definition.getName() + "]: " + definition);
            }
            try {
                boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
                DefaultTransactionStatus status = newTransactionStatus(
                        definition, transaction, true, newSynchronization, debugEnabled, suspendedResources);
                doBegin(transaction, definition);
                prepareSynchronization(status, definition);
                return status;
            }
            catch (RuntimeException | Error ex) {
                resume(null, suspendedResources);
                throw ex;
            }
        }
        else {
            // Create "empty" transaction: no actual transaction, but potentially synchronization.
            if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT && logger.isWarnEnabled()) {
                logger.warn("Custom isolation level specified but no actual transaction initiated; " +
                        "isolation level will effectively be ignored: " + definition);
            }
            boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
            return prepareTransactionStatus(definition, null, true, newSynchronization, debugEnabled, null);
        }
    }

    /**
     * 处理当前存在事务的情况:
     * === 7种传播行为:
     * -> PROPAGATION_REQUIRED: 需要一个事务, 如果当前存在事务, 就加入该事务, 否则创建一个新事务(最常使用)
     * -> PROPAGATION_SUPPORTS: 支持当前事务, 如果当前存在事务, 就加入该事务, 否则以非事务运行
     * -> PROPAGATION_MANDATORY: 当前必须存在事务, 如果当前存在事务, 就加入该事务, 否则抛出异常
     * -> PROPAGATION_REQUIRES_NEW: 需要一个新事务, 无论当前是否存在事务, 都会创建一个新事务
     * -> PROPAGATION_NOT_SUPPORTED: 不支持事务, 以非事务方式运行, 如果当前存在事务, 就把当前事务挂起
     * -> PROPAGATION_NEVER: 禁止事务, 以非事务方式运行, 如果当前存在事务, 则抛出异常
     * -> PROPAGATION_NESTED: 嵌套事务, 如果当前存在事务, 则在嵌套事务内运行, 否则创建一个新事务
     * === 当前存在事务的处理方式:
     * -> PROPAGATION_NEVER: 不允许存在事务, 抛出异常
     * -> PROPAGATION_NOT_SUPPORTED: 不支持事务, 将当前事务挂起
     * -> PROPAGATION_REQUIRES_NEW: 需要新的事务, 挂起当前事务, 并开启一个新的事务
     * -> PROPAGATION_NESTED: 嵌套事务
     * -> PROPAGATION_SUPPORTS or PROPAGATION_REQUIRED: 加入当前事务, 如果当前事务的隔离级别和自己的不一致, 则抛出异常
     */
    private TransactionStatus handleExistingTransaction(
            TransactionDefinition definition, Object transaction, boolean debugEnabled)
            throws TransactionException {

        // PROPAGATION_NEVER: 不允许存在事务, 抛出异常
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER) {
            throw new IllegalTransactionStateException(
                    "Existing transaction found for transaction marked with propagation 'never'");
        }

        // PROPAGATION_NOT_SUPPORTED: 不支持事务, 将当前事务挂起
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
            if (debugEnabled) {
                logger.debug("Suspending current transaction");
            }
            Object suspendedResources = suspend(transaction);
            boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
            return prepareTransactionStatus(
                    definition, null, false, newSynchronization, debugEnabled, suspendedResources);
        }

        // PROPAGATION_REQUIRES_NEW: 需要新的事务, 挂起当前事务, 并开启一个新的事务
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {
            if (debugEnabled) {
                logger.debug("Suspending current transaction, creating new transaction with name [" +
                        definition.getName() + "]");
            }
            SuspendedResourcesHolder suspendedResources = suspend(transaction);
            try {
                boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
                DefaultTransactionStatus status = newTransactionStatus(
                        definition, transaction, true, newSynchronization, debugEnabled, suspendedResources);
                doBegin(transaction, definition);
                prepareSynchronization(status, definition);
                return status;
            }
            catch (RuntimeException | Error beginEx) {
                resumeAfterBeginException(transaction, suspendedResources, beginEx);
                throw beginEx;
            }
        }

        // PROPAGATION_NESTED: 嵌套事务
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
            if (!isNestedTransactionAllowed()) {
                throw new NestedTransactionNotSupportedException(
                        "Transaction manager does not allow nested transactions by default - " +
                                "specify 'nestedTransactionAllowed' property with value 'true'");
            }
            if (debugEnabled) {
                logger.debug("Creating nested transaction with name [" + definition.getName() + "]");
            }
            if (useSavepointForNestedTransaction()) {
                // Create savepoint within existing Spring-managed transaction,
                // through the SavepointManager API implemented by TransactionStatus.
                // Usually uses JDBC 3.0 savepoints. Never activates Spring synchronization.
                DefaultTransactionStatus status =
                        prepareTransactionStatus(definition, transaction, false, false, debugEnabled, null);
                status.createAndHoldSavepoint();
                return status;
            }
            else {
                // Nested transaction through nested begin and commit/rollback calls.
                // Usually only for JTA: Spring synchronization might get activated here
                // in case of a pre-existing JTA transaction.
                boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
                DefaultTransactionStatus status = newTransactionStatus(
                        definition, transaction, true, newSynchronization, debugEnabled, null);
                doBegin(transaction, definition);
                prepareSynchronization(status, definition);
                return status;
            }
        }

        // Assumably PROPAGATION_SUPPORTS or PROPAGATION_REQUIRED.
        if (debugEnabled) {
            logger.debug("Participating in existing transaction");
        }
        // PROPAGATION_SUPPORTS or PROPAGATION_REQUIRED: 加入当前事务, 如果当前事务的隔离级别和自己的不一致, 则抛出异常
        if (isValidateExistingTransaction()) {
            if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
                Integer currentIsolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
                if (currentIsolationLevel == null || currentIsolationLevel != definition.getIsolationLevel()) {
                    Constants isoConstants = DefaultTransactionDefinition.constants;
                    throw new IllegalTransactionStateException("Participating transaction with definition [" +
                            definition + "] specifies isolation level which is incompatible with existing transaction: " +
                            (currentIsolationLevel != null ?
                                    isoConstants.toCode(currentIsolationLevel, DefaultTransactionDefinition.PREFIX_ISOLATION) :
                                    "(unknown)"));
                }
            }
            if (!definition.isReadOnly()) {
                if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                    throw new IllegalTransactionStateException("Participating transaction with definition [" +
                            definition + "] is not marked as read-only but existing transaction is");
                }
            }
        }
        boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
        return prepareTransactionStatus(definition, transaction, false, newSynchronization, debugEnabled, null);
    }

    /**
     * Create a new TransactionStatus for the given arguments,
     * also initializing transaction synchronization as appropriate.
     * @see #newTransactionStatus
     * @see #prepareTransactionStatus
     */
    protected final DefaultTransactionStatus prepareTransactionStatus(
            TransactionDefinition definition, @Nullable Object transaction, boolean newTransaction,
            boolean newSynchronization, boolean debug, @Nullable Object suspendedResources) {

        DefaultTransactionStatus status = newTransactionStatus(
                definition, transaction, newTransaction, newSynchronization, debug, suspendedResources);
        prepareSynchronization(status, definition);
        return status;
    }

    /**
     * Initialize transaction synchronization as appropriate.
     */
    protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
        if (status.isNewSynchronization()) {
            TransactionSynchronizationManager.setActualTransactionActive(status.hasTransaction());
            TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(
                    definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT ?
                            definition.getIsolationLevel() : null);
            TransactionSynchronizationManager.setCurrentTransactionReadOnly(definition.isReadOnly());
            TransactionSynchronizationManager.setCurrentTransactionName(definition.getName());
            TransactionSynchronizationManager.initSynchronization();
        }
    }

    /**
     * DataSourceTransactionManager解析 =======================
     * 获取当前事务
     */
    protected Object doGetTransaction() {
        DataSourceTransactionObject txObject = new DataSourceTransactionObject();
        txObject.setSavepointAllowed(isNestedTransactionAllowed());
        ConnectionHolder conHolder =
                (ConnectionHolder) TransactionSynchronizationManager.getResource(obtainDataSource());
        txObject.setConnectionHolder(conHolder, false);
        return txObject;
    }

    /**
     * 当前是否存在事务: 判断事务状态是否是active
     */
    protected boolean isExistingTransaction(Object transaction) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        return (txObject.hasConnectionHolder() && txObject.getConnectionHolder().isTransactionActive());
    }

    /**
     * 开始一个事务: 设置隔离级别(isolation):
     * -> 1.获取数据库连接
     * -> 2.设置并返回当前的隔离级别
     * -> 3.取消自动提交事务, 使用手动提交
     * -> 4.准备事务连接: 如果设置了事务为`readOnly`, 则会执行SQL语句: SET TRANSACTION READ ONLY
     * -> 5.设置状态表明当前正在事务中
     * -> 6.设置超时时间(timeout)
     * -> 7.保存连接的holder到当前线程
     */
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        Connection con = null;

        try {
            if (!txObject.hasConnectionHolder() ||
                    txObject.getConnectionHolder().isSynchronizedWithTransaction()) {
                Connection newCon = obtainDataSource().getConnection();
                if (logger.isDebugEnabled()) {
                    logger.debug("Acquired Connection [" + newCon + "] for JDBC transaction");
                }
                txObject.setConnectionHolder(new ConnectionHolder(newCon), true);
            }

            // 获取数据库连接
            txObject.getConnectionHolder().setSynchronizedWithTransaction(true);
            con = txObject.getConnectionHolder().getConnection();

            // 设置并返回当前的隔离级别
            Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
            txObject.setPreviousIsolationLevel(previousIsolationLevel);

            // 取消自动提交事务, 使用手动提交
            if (con.getAutoCommit()) {
                txObject.setMustRestoreAutoCommit(true);
                if (logger.isDebugEnabled()) {
                    logger.debug("Switching JDBC Connection [" + con + "] to manual commit");
                }
                con.setAutoCommit(false);
            }

            // 准备事务连接: 如果设置了事务为`readOnly`, 则会执行SQL语句: SET TRANSACTION READ ONLY
            prepareTransactionalConnection(con, definition);
            // 设置状态表明当前正在事务中
            txObject.getConnectionHolder().setTransactionActive(true);

            // 设置超时时间(timeout)
            int timeout = determineTimeout(definition);
            if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
                txObject.getConnectionHolder().setTimeoutInSeconds(timeout);
            }

            // 保存连接的holder到当前线程
            if (txObject.isNewConnectionHolder()) {
                TransactionSynchronizationManager.bindResource(obtainDataSource(), txObject.getConnectionHolder());
            }
        }

        catch (Throwable ex) {
            if (txObject.isNewConnectionHolder()) {
                DataSourceUtils.releaseConnection(con, obtainDataSource());
                txObject.setConnectionHolder(null, false);
            }
            throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction", ex);
        }
    }

    protected Object doSuspend(Object transaction) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        txObject.setConnectionHolder(null);
        return TransactionSynchronizationManager.unbindResource(obtainDataSource());
    }

    protected void doResume(@Nullable Object transaction, Object suspendedResources) {
        TransactionSynchronizationManager.bindResource(obtainDataSource(), suspendedResources);
    }

    protected void doCommit(DefaultTransactionStatus status) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
        Connection con = txObject.getConnectionHolder().getConnection();
        if (status.isDebug()) {
            logger.debug("Committing JDBC transaction on Connection [" + con + "]");
        }
        try {
            con.commit();
        }
        catch (SQLException ex) {
            throw new TransactionSystemException("Could not commit JDBC transaction", ex);
        }
    }

    protected void doRollback(DefaultTransactionStatus status) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
        Connection con = txObject.getConnectionHolder().getConnection();
        if (status.isDebug()) {
            logger.debug("Rolling back JDBC transaction on Connection [" + con + "]");
        }
        try {
            con.rollback();
        }
        catch (SQLException ex) {
            throw new TransactionSystemException("Could not roll back JDBC transaction", ex);
        }
    }

    protected void doSetRollbackOnly(DefaultTransactionStatus status) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
        if (status.isDebug()) {
            logger.debug("Setting JDBC transaction [" + txObject.getConnectionHolder().getConnection() +
                    "] rollback-only");
        }
        txObject.setRollbackOnly();
    }

    protected void doCleanupAfterCompletion(Object transaction) {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;

        // Remove the connection holder from the thread, if exposed.
        if (txObject.isNewConnectionHolder()) {
            TransactionSynchronizationManager.unbindResource(obtainDataSource());
        }

        // Reset connection.
        Connection con = txObject.getConnectionHolder().getConnection();
        try {
            if (txObject.isMustRestoreAutoCommit()) {
                con.setAutoCommit(true);
            }
            DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
        }
        catch (Throwable ex) {
            logger.debug("Could not reset JDBC Connection after transaction", ex);
        }

        if (txObject.isNewConnectionHolder()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Releasing JDBC Connection [" + con + "] after transaction");
            }
            DataSourceUtils.releaseConnection(con, this.dataSource);
        }

        txObject.getConnectionHolder().clear();
    }


    /**
     * 准备事务连接: 如果设置了事务为`readOnly`, 则会执行SQL语句: SET TRANSACTION READ ONLY
     */
    protected void prepareTransactionalConnection(Connection con, TransactionDefinition definition)
            throws SQLException {

        if (isEnforceReadOnly() && definition.isReadOnly()) {
            Statement stmt = con.createStatement();
            try {
                stmt.executeUpdate("SET TRANSACTION READ ONLY");
            }
            finally {
                stmt.close();
            }
        }
    }


    /**
     * 保存数据库连接
     */
    private static class DataSourceTransactionObject extends JdbcTransactionObjectSupport {

        private boolean newConnectionHolder;

        private boolean mustRestoreAutoCommit;

        public void setConnectionHolder(@Nullable ConnectionHolder connectionHolder, boolean newConnectionHolder) {
            super.setConnectionHolder(connectionHolder);
            this.newConnectionHolder = newConnectionHolder;
        }

        public boolean isNewConnectionHolder() {
            return this.newConnectionHolder;
        }

        public void setMustRestoreAutoCommit(boolean mustRestoreAutoCommit) {
            this.mustRestoreAutoCommit = mustRestoreAutoCommit;
        }

        public boolean isMustRestoreAutoCommit() {
            return this.mustRestoreAutoCommit;
        }

        public void setRollbackOnly() {
            getConnectionHolder().setRollbackOnly();
        }

        @Override
        public boolean isRollbackOnly() {
            return getConnectionHolder().isRollbackOnly();
        }

        @Override
        public void flush() {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationUtils.triggerFlush();
            }
        }
    }

}
