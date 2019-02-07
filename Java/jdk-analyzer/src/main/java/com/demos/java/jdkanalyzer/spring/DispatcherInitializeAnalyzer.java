package com.demos.java.jdkanalyzer.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.*;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.ServletContextResourceLoader;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.*;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/13
 *
 * ========== ContextLoaderListener解析:
 * ===== 初始化WebApplicationContext:
 * -> 1.从ServletContext查找WebApplicationContext(ROOT), 如果已经存在则抛出异常: 不允许重复创建
 * -> 2.如果context为空, 实例化ConfigurableWebApplicationContext
 * -> 3.如果context还没有激活: 则刷新该context
 * -> 4.设置parent context
 * -> 5.配置(解析init-param)并刷新context
 * -> 6.将context设置到ServletContext
 * -> 7.配置class loader -> context关系
 * ===== 配置并刷新WebApplicationContext:
 * -> 1.获取init-param: contextId, 并设置ID: wac.setId()
 * -> 2.将ServletContext设置到WebApplicationContext
 * -> 3.获取init-param: contextConfigLocation, 并设置config路径: wac.setConfigLocation()
 * -> 4.初始化环境变量的属性: Properties
 * -> 5.执行ApplicationContextInitializer回调
 * -> 6.刷新WebApplicationContext
 *
 * ========== DispatcherServlet解析:
 * ===== 继承关系: DispatcherServlet -> FrameworkServlet -> HttpServletBean -> HttpServlet(servlet规范: doGet, doPost, ...)
 * ===== 初始化流程:
 * -> 1.HttpServletBean.init()
 * -> 2.FrameworkServlet.initServletBean()
 * -> 3.FrameworkServlet.initWebApplicationContext()
 * -> 4.DispatcherServlet.onRefresh()
 * -> 5.DispatcherServlet.initStrategies()
 * ===== FrameworkServlet.initWebApplicationContext():
 * -> 1.从ServletContext获取ROOT WebApplicationContext
 * -> 2.如果webApplicationContext已经在构造函数中注入: 则使用注入的context, 若该context的parent为空, 则设置rootContext为其parent
 * -> 3.否则: 从ServletContext查找context
 * -> 4.否则: 自行创建context(XmlWebApplicationContext)
 * -> 5.onRefresh()钩子方法: 在DispatcherServlet中用来初始化各种策略对象
 * ===== configureAndRefreshWebApplicationContext(): 配置并刷新WebApplicationContext
 * -> 1.设置变量: id, ServletContext, ServletConfig, namespace
 * -> 2.添加ApplicationListener
 * -> 3.初始化环境变量properties
 * -> 4.refresh()之前的后置处理方法
 * -> 5.应用ApplicationContextInitializer
 * -> 6.刷新WebApplicationContext
 * ===== createWebApplicationContext(): 实例化WebApplicationContext:
 * -> 1.获取WebApplicationContext的实现类
 * -> 2.使用默认构造方法实例化WebApplicationContext
 * -> 3.设置环境配置
 * -> 4.设置传入的context为parent context
 * -> 5.设置配置文件路径configLocation
 * -> 6.配置并刷新WebApplicationContext
 * ======== DispatcherServlet.initStrategies():
 * ===== 一、初始化MultipartResolver: beanName = multipartResolver
 * -> MultipartResolver接口方法:
 * === boolean isMultipart(HttpServletRequest request); // 请求中是否包含"multipart/form-data"的内容
 * === MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException; // 从请求中解析出multipart类型的文件或参数
 * === void cleanupMultipart(MultipartHttpServletRequest request); // 请求multipart资源
 * ===== 二、初始化LocaleResolver: beanName = localeResolver
 * -> LocaleResolver接口方法:
 * === Locale resolveLocale(HttpServletRequest request); // 从请求中解析出locale
 * === void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Locale locale); // 设置locale
 * ===== 三、初始化ThemeResolver: beanName = themeResolver
 * -> ThemeResolver接口方法:
 * === String resolveThemeName(HttpServletRequest request); // 从请求中解析出主题名
 * === void setThemeName(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable String themeName); // 设置主题名
 * ===== 四、初始化HandlerMappings:
 * -> 1.如果需要获取所有HandlerMapping, 则从Application中找到所有的HandlerMapping, 包括parent context中的HandlerMapping
 * -> 2.如果只使用一个确定的HandlerMapping, 从ApplicationContext中查找: beanName = handlerMapping
 * -> 3.至少需要一个HandlerMapping, 如果没有则使用默认的HandlerMapping(BeanNameUrlHandlerMapping)
 * -> HandlerMapping接口方法:
 * === HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception; // 根据request中返回一个HandlerExecutionChain
 * -> 常用HandlerMapping类型:
 * === RequestMappingHandlerMapping
 * === SimpleUrlHandlerMapping
 * === BeanNameUrlHandlerMapping
 * -> HandlerExecutionChain成员变量:
 * === Object handler: 请求处理器: 常用类型: HandlerMethod, HttpRequestHandler, Servlet或任何自定义的类型
 * === HandlerInterceptor[] interceptors: 拦截器(preHandle, postHandle, afterCompletion)
 * -> HandlerMethod(用于@RequestMapping注解)成员变量:
 * === Class<?> beanType: 方法所属的类
 * === Object bean: 实际对象
 * === Method method: 方法
 * === MethodParameter[] parameters: 方法参数
 * ===== 五、初始化HandlerAdapters:
 * -> 1.如果需要获取所有HandlerAdapter, 则从Application中找到所有的HandlerAdapter, 包括parent context中的HandlerAdapter
 * -> 2.如果只使用一个确定的HandlerAdapter, 从ApplicationContext中查找: beanName = handlerAdapter
 * -> 3.至少需要一个HandlerAdapter, 如果没有则使用默认的HandlerAdapter(SimpleControllerHandlerAdapter)
 * -> HandlerAdapter接口方法:
 * === boolean supports(Object handler); // 是否支持给定的handler实例
 * === ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception; // 使用给定的handler处理请求
 * === long getLastModified(HttpServletRequest request, Object handler); // 根据request返回last-modified值, 可返回-1表示不支持
 * -> 常用HandlerAdapter类型:
 * === RequestMappingHandlerAdapter
 * === HttpRequestHandlerAdapter
 * === SimpleControllerHandlerAdapter
 * ===== 六、初始化HandlerExceptionResolver:
 * -> 1.如果需要获取所有HandlerExceptionResolver, 则从Application中找到所有的HandlerExceptionResolver, 包括parent context中的HandlerExceptionResolver
 * -> 2.如果只使用一个确定的HandlerExceptionResolver, 从ApplicationContext中查找: beanName = handlerExceptionResolver
 * -> 3.至少需要一个HandlerExceptionResolver, 如果没有则使用默认的HandlerExceptionResolver
 * -> HandlerExceptionResolver接口方法:
 * === ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex); // 处理异常: 并用handler处理结果
 * -> 常用HandlerExceptionResolver类型:
 * === ExceptionHandlerExceptionResolver
 * === HandlerExceptionResolverComposite
 * === ResponseStatusExceptionResolver
 * === DefaultHandlerExceptionResolver
 * === ApplicationSimpleMappingExceptionResolver
 * ===== 七、初始化RequestToViewNameTranslator:
 * -> RequestToViewNameTranslator接口方法:
 * === String getViewName(HttpServletRequest request) throws Exception; // 根据request解析出viewName
 * ===== 八、初始化ViewResolvers:
 * -> 1.如果需要获取所有ViewResolver, 则从Application中找到所有的ViewResolver, 包括parent context中的ViewResolver
 * -> 2.如果只使用一个确定的ViewResolver, 从ApplicationContext中查找: beanName = viewResolver
 * -> 3.至少需要一个ViewResolver, 如果没有则使用默认的ViewResolver(InternalResourceViewResolver)
 * -> ViewResolver接口方法:
 * === View resolveViewName(String viewName, Locale locale) throws Exception; // 根据viewName解析出View
 * -> 常用ViewResolver类型:
 * === FreeMarkerViewResolver
 * === InternalResourceViewResolver
 * === ViewResolverComposite
 * ===== 九、初始化FlashMapManager:
 * -> FlashMapManager接口方法:
 * === FlashMap retrieveAndUpdate(HttpServletRequest request, HttpServletResponse response); // 根据给定request找到之前request保存的FlashMap
 * === void saveOutputFlashMap(FlashMap flashMap, HttpServletRequest request, HttpServletResponse response); // 保存给定的FlashMap
 */
public class DispatcherInitializeAnalyzer {

    /* ==================== ContextLoaderListener init ==================== */

    /**
     * 初始化WebApplicationContext:
     * -> 1.从ServletContext查找WebApplicationContext(ROOT), 如果已经存在则抛出异常: 不允许重复创建
     * -> 2.如果context为空, 实例化ConfigurableWebApplicationContext
     * -> 3.如果context还没有激活: 则刷新该context
     * -> 4.设置parent context
     * -> 5.配置(解析init-param)并刷新context
     * -> 6.将context设置到ServletContext
     * -> 7.配置class loader -> context关系
     */
    public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
        // 从ServletContext查找WebApplicationContext(ROOT), 如果已经存在则抛出异常: 不允许重复创建
        if (servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) != null) {
            throw new IllegalStateException(
                    "Cannot initialize context because there is already a root application context present - " +
                            "check whether you have multiple ContextLoader* definitions in your web.xml!");
        }

        Log logger = LogFactory.getLog(ContextLoader.class);
        servletContext.log("Initializing Spring root WebApplicationContext");
        if (logger.isInfoEnabled()) {
            logger.info("Root WebApplicationContext: initialization started");
        }
        long startTime = System.currentTimeMillis();

        try {
            // 如果context为空, 实例化ConfigurableWebApplicationContext
            if (this.context == null) {
                this.context = createWebApplicationContext(servletContext);
            }
            if (this.context instanceof ConfigurableWebApplicationContext) {
                ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) this.context;
                // 如果context还没有激活: 则刷新该context
                if (!cwac.isActive()) {
                    // 设置parent context
                    if (cwac.getParent() == null) {
                        // The context instance was injected without an explicit parent ->
                        // determine parent for root web application context, if any.
                        ApplicationContext parent = loadParentContext(servletContext);
                        cwac.setParent(parent);
                    }
                    // 配置(解析init-param)并刷新context
                    configureAndRefreshWebApplicationContext(cwac, servletContext);
                }
            }
            // 将context设置到ServletContext
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.context);

            // 配置class loader -> context关系
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl == ContextLoader.class.getClassLoader()) {
                currentContext = this.context;
            }
            else if (ccl != null) {
                currentContextPerThread.put(ccl, this.context);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Published root WebApplicationContext as ServletContext attribute with name [" +
                        WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
            }
            if (logger.isInfoEnabled()) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                logger.info("Root WebApplicationContext: initialization completed in " + elapsedTime + " ms");
            }

            return this.context;
        }
        catch (RuntimeException ex) {
            logger.error("Context initialization failed", ex);
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ex);
            throw ex;
        }
        catch (Error err) {
            logger.error("Context initialization failed", err);
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, err);
            throw err;
        }
    }

    /**
     * 实例化ConfigurableWebApplicationContext
     */
    protected WebApplicationContext createWebApplicationContext(ServletContext sc) {
        // 找到需要的WebApplicationContext实现类
        Class<?> contextClass = determineContextClass(sc);
        if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
            throw new ApplicationContextException("Custom context class [" + contextClass.getName() +
                    "] is not of type [" + ConfigurableWebApplicationContext.class.getName() + "]");
        }
        // 使用默认构造器实例化ConfigurableWebApplicationContext
        return (ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
    }

    /**
     * 配置并刷新WebApplicationContext:
     * -> 1.获取init-param: contextId, 并设置ID: wac.setId()
     * -> 2.将ServletContext设置到WebApplicationContext
     * -> 3.获取init-param: contextConfigLocation, 并设置config路径: wac.setConfigLocation()
     * -> 4.初始化环境变量的属性: Properties
     * -> 5.执行ApplicationContextInitializer回调
     * -> 6.刷新WebApplicationContext
     */
    protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac, ServletContext sc) {
        if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
            // 获取init-param: contextId
            String idParam = sc.getInitParameter(CONTEXT_ID_PARAM);
            if (idParam != null) {
                wac.setId(idParam);
            }
            else {
                // Generate default id...
                wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
                        ObjectUtils.getDisplayString(sc.getContextPath()));
            }
        }

        // 将ServletContext设置到WebApplicationContext
        wac.setServletContext(sc);
        // 获取init-param: contextConfigLocation
        String configLocationParam = sc.getInitParameter(CONFIG_LOCATION_PARAM);
        if (configLocationParam != null) {
            wac.setConfigLocation(configLocationParam);
        }

        // The wac environment's #initPropertySources will be called in any case when the context
        // is refreshed; do it eagerly here to ensure servlet property sources are in place for
        // use in any post-processing or initialization that occurs below prior to #refresh
        // 初始化环境变量的属性: Properties
        ConfigurableEnvironment env = wac.getEnvironment();
        if (env instanceof ConfigurableWebEnvironment) {
            ((ConfigurableWebEnvironment) env).initPropertySources(sc, null);
        }

        // 执行ApplicationContextInitializer回调
        customizeContext(sc, wac);
        // 刷新WebApplicationContext
        wac.refresh();
    }

    /* ==================== DispatcherServlet init ==================== */

    private MultipartResolver multipartResolver;
    private LocaleResolver localeResolver;
    private ThemeResolver themeResolver;
    private List<HandlerMapping> handlerMappings;
    private List<HandlerAdapter> handlerAdapters;
    private List<HandlerExceptionResolver> handlerExceptionResolvers;
    private RequestToViewNameTranslator viewNameTranslator;
    private FlashMapManager flashMapManager;
    private List<ViewResolver> viewResolvers;

    /**
     * HttpServletBean.init(): 将init参数设置到properties
     */
    public final void init() throws ServletException {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing servlet '" + getServletName() + "'");
        }

        // 将init参数设置到properties
        PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties);
        if (!pvs.isEmpty()) {
            try {
                BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
                ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
                bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, getEnvironment()));
                initBeanWrapper(bw);
                bw.setPropertyValues(pvs, true);
            }
            catch (BeansException ex) {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to set bean properties on servlet '" + getServletName() + "'", ex);
                }
                throw ex;
            }
        }

        // 初始化Servlet
        initServletBean();

        if (logger.isDebugEnabled()) {
            logger.debug("Servlet '" + getServletName() + "' configured successfully");
        }
    }

    /**
     * FrameworkServlet.initServletBean():
     */
    protected final void initServletBean() throws ServletException {
        getServletContext().log("Initializing Spring FrameworkServlet '" + getServletName() + "'");
        if (this.logger.isInfoEnabled()) {
            this.logger.info("FrameworkServlet '" + getServletName() + "': initialization started");
        }
        long startTime = System.currentTimeMillis();

        try {
            // 初始化WebApplicationContext
            this.webApplicationContext = initWebApplicationContext();
            // 钩子方法
            initFrameworkServlet();
        }
        catch (ServletException | RuntimeException ex) {
            this.logger.error("Context initialization failed", ex);
            throw ex;
        }

        if (this.logger.isInfoEnabled()) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            this.logger.info("FrameworkServlet '" + getServletName() + "': initialization completed in " +
                    elapsedTime + " ms");
        }
    }

    /**
     * FrameworkServlet.initWebApplicationContext():
     * -> 1.从ServletContext获取ROOT WebApplicationContext
     * -> 2.如果webApplicationContext已经在构造函数中注入: 则使用注入的context, 若该context的parent为空, 则设置rootContext为其parent
     * -> 3.否则: 从ServletContext查找context
     * -> 4.否则: 自行创建context(XmlWebApplicationContext)
     * -> 5.onRefresh()钩子方法: 在DispatcherServlet中用来初始化各种策略对象
     */
    protected WebApplicationContext initWebApplicationContext() {
        // 从ServletContext获取ROOT WebApplicationContext
        WebApplicationContext rootContext =
                WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        WebApplicationContext wac = null;

        // 如果webApplicationContext已经在构造函数中注入: 则使用注入的context
        if (this.webApplicationContext != null) {
            // A context instance was injected at construction time -> use it
            wac = this.webApplicationContext;
            if (wac instanceof ConfigurableWebApplicationContext) {
                ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) wac;
                // 若注入的context尚未激活: 配置并刷新context
                if (!cwac.isActive()) {
                    // 如果context的parent为空: 将其parent设置为rootContext
                    if (cwac.getParent() == null) {
                        // The context instance was injected without an explicit parent -> set
                        // the root application context (if any; may be null) as the parent
                        cwac.setParent(rootContext);
                    }
                    // 配置并刷新WebApplicationContext
                    configureAndRefreshWebApplicationContext(cwac);
                }
            }
        }
        // 否则: 从ServletContext查找context
        if (wac == null) {
            // No context instance was injected at construction time -> see if one
            // has been registered in the servlet context. If one exists, it is assumed
            // that the parent context (if any) has already been set and that the
            // user has performed any initialization such as setting the context id
            wac = findWebApplicationContext();
        }
        // 否则: 自行创建context(XmlWebApplicationContext)
        if (wac == null) {
            // No context instance is defined for this servlet -> create a local one
            wac = createWebApplicationContext(rootContext);
        }

        // onRefresh()钩子方法: 在DispatcherServlet中用来初始化各种策略对象
        if (!this.refreshEventReceived) {
            // Either the context is not a ConfigurableApplicationContext with refresh
            // support or the context injected at construction time had already been
            // refreshed -> trigger initial onRefresh manually here.
            onRefresh(wac);
        }

        if (this.publishContext) {
            // Publish the context as a servlet context attribute.
            String attrName = getServletContextAttributeName();
            getServletContext().setAttribute(attrName, wac);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Published WebApplicationContext of servlet '" + getServletName() +
                        "' as ServletContext attribute with name [" + attrName + "]");
            }
        }

        return wac;
    }

    /**
     * configureAndRefreshWebApplicationContext(): 配置并刷新WebApplicationContext
     * -> 1.设置变量: id, ServletContext, ServletConfig, namespace
     * -> 2.添加ApplicationListener
     * -> 3.初始化环境变量properties
     * -> 4.refresh()之前的后置处理方法
     * -> 5.应用ApplicationContextInitializer
     * -> 6.刷新WebApplicationContext
     */
    protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac) {
        // 设置id
        if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
            // The application context id is still set to its original default value
            // -> assign a more useful id based on available information
            if (this.contextId != null) {
                wac.setId(this.contextId);
            }
            else {
                // Generate default id...
                wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
                        ObjectUtils.getDisplayString(getServletContext().getContextPath()) + '/' + getServletName());
            }
        }

        // 设置ServletContext
        wac.setServletContext(getServletContext());
        // 设置ServletConfig
        wac.setServletConfig(getServletConfig());
        // 设置namespace
        wac.setNamespace(getNamespace());
        // 添加ApplicationListener
        wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener()));

        // 初始化环境变量properties
        ConfigurableEnvironment env = wac.getEnvironment();
        if (env instanceof ConfigurableWebEnvironment) {
            ((ConfigurableWebEnvironment) env).initPropertySources(getServletContext(), getServletConfig());
        }

        // refresh()之前的后置处理方法
        postProcessWebApplicationContext(wac);
        // 应用ApplicationContextInitializer
        applyInitializers(wac);
        // 刷新WebApplicationContext
        wac.refresh();
    }

    /**
     * 从ServletContext中获取WebApplicationContext
     */
    protected WebApplicationContext findWebApplicationContext() {
        String attrName = getContextAttribute();
        if (attrName == null) {
            return null;
        }
        WebApplicationContext wac =
                WebApplicationContextUtils.getWebApplicationContext(getServletContext(), attrName);
        if (wac == null) {
            throw new IllegalStateException("No WebApplicationContext found: initializer not registered?");
        }
        return wac;
    }

    /**
     * 实例化WebApplicationContext
     */
    protected WebApplicationContext createWebApplicationContext(@Nullable WebApplicationContext parent) {
        return createWebApplicationContext((ApplicationContext) parent);
    }

    /**
     * createWebApplicationContext(): 实例化WebApplicationContext:
     * -> 1.获取WebApplicationContext的实现类
     * -> 2.使用默认构造方法实例化WebApplicationContext
     * -> 3.设置环境配置
     * -> 4.设置传入的context为parent context
     * -> 5.设置配置文件路径configLocation
     * -> 6.配置并刷新WebApplicationContext
     */
    protected WebApplicationContext createWebApplicationContext(@Nullable ApplicationContext parent) {
        // 获取WebApplicationContext的实现类
        Class<?> contextClass = getContextClass();
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Servlet with name '" + getServletName() +
                    "' will try to create custom WebApplicationContext context of class '" +
                    contextClass.getName() + "'" + ", using parent context [" + parent + "]");
        }
        if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
            throw new ApplicationContextException(
                    "Fatal initialization error in servlet with name '" + getServletName() +
                            "': custom WebApplicationContext class [" + contextClass.getName() +
                            "] is not of type ConfigurableWebApplicationContext");
        }
        // 使用默认构造方法实例化WebApplicationContext, 后续流程跟注入WebApplicationContext的方式类似
        ConfigurableWebApplicationContext wac =
                (ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
        // 设置环境配置
        wac.setEnvironment(getEnvironment());
        // 设置传入的context为parent context
        wac.setParent(parent);
        // 设置配置文件路径configLocation
        String configLocation = getContextConfigLocation();
        if (configLocation != null) {
            wac.setConfigLocation(configLocation);
        }
        // 配置并刷新WebApplicationContext
        configureAndRefreshWebApplicationContext(wac);

        return wac;
    }

    /**
     * DispatcherServlet.onRefresh(): 刷新ApplicationContext
     */
    protected void onRefresh(ApplicationContext context) {
        initStrategies(context);
    }

    /**
     * DispatcherServlet.onRefresh(): 使用context初始化策略对象
     */
    protected void initStrategies(ApplicationContext context) {
        initMultipartResolver(context);
        initLocaleResolver(context);
        initThemeResolver(context);
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initHandlerExceptionResolvers(context);
        initRequestToViewNameTranslator(context);
        initViewResolvers(context);
        initFlashMapManager(context);
    }

    /**
     * 初始化MultipartResolver: beanName = multipartResolver
     * -> MultipartResolver接口方法:
     * === boolean isMultipart(HttpServletRequest request); // 请求中是否包含"multipart/form-data"的内容
     * === MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException; // 从请求中解析出multipart类型的文件或参数
     * === void cleanupMultipart(MultipartHttpServletRequest request); // 请求multipart资源
     */
    private void initMultipartResolver(ApplicationContext context) {
        // 从ApplicationContext中查找MultipartResolver, beanName = multipartResolver
        try {
            this.multipartResolver = context.getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using MultipartResolver [" + this.multipartResolver + "]");
            }
        }
        // 若不存在则设置为null
        catch (NoSuchBeanDefinitionException ex) {
            // Default is no multipart resolver.
            this.multipartResolver = null;
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate MultipartResolver with name '" + MULTIPART_RESOLVER_BEAN_NAME +
                        "': no multipart request handling provided");
            }
        }
    }

    /**
     * 初始化LocaleResolver: beanName = localeResolver
     * -> LocaleResolver接口方法:
     * === Locale resolveLocale(HttpServletRequest request); // 从请求中解析出locale
     * === void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Locale locale); // 设置locale
     */
    private void initLocaleResolver(ApplicationContext context) {
        // 从ApplicationContext中查找LocaleResolver, beanName = localeResolver
        try {
            this.localeResolver = context.getBean(LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using LocaleResolver [" + this.localeResolver + "]");
            }
        }
        // 若不存在则设置为默认的LocaleResolver(AcceptHeaderLocaleResolver)
        catch (NoSuchBeanDefinitionException ex) {
            // We need to use the default.
            this.localeResolver = getDefaultStrategy(context, LocaleResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate LocaleResolver with name '" + LOCALE_RESOLVER_BEAN_NAME +
                        "': using default [" + this.localeResolver + "]");
            }
        }
    }

    /**
     * 初始化ThemeResolver: beanName = themeResolver
     * -> ThemeResolver接口方法:
     * === String resolveThemeName(HttpServletRequest request); // 从请求中解析出主题名
     * === void setThemeName(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable String themeName); // 设置主题名
     */
    private void initThemeResolver(ApplicationContext context) {
        // 从ApplicationContext中查找ThemeResolver, beanName = themeResolver
        try {
            this.themeResolver = context.getBean(THEME_RESOLVER_BEAN_NAME, ThemeResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using ThemeResolver [" + this.themeResolver + "]");
            }
        }
        // 若不存在则设置为默认的ThemeResolver(FixedThemeResolver)
        catch (NoSuchBeanDefinitionException ex) {
            // We need to use the default.
            this.themeResolver = getDefaultStrategy(context, ThemeResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate ThemeResolver with name '" + THEME_RESOLVER_BEAN_NAME +
                        "': using default [" + this.themeResolver + "]");
            }
        }
    }

    /**
     * 初始化HandlerMappings:
     * -> 1.如果需要获取所有HandlerMapping, 则从Application中找到所有的HandlerMapping, 包括parent context中的HandlerMapping
     * -> 2.如果只使用一个确定的HandlerMapping, 从ApplicationContext中查找: beanName = handlerMapping
     * -> 3.至少需要一个HandlerMapping, 如果没有则使用默认的HandlerMapping(BeanNameUrlHandlerMapping)
     * -> HandlerMapping接口方法:
     * === HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception; // 根据request中返回一个HandlerExecutionChain
     * -> 常用HandlerMapping类型:
     * === RequestMappingHandlerMapping
     * === SimpleUrlHandlerMapping
     * === BeanNameUrlHandlerMapping
     * -> HandlerExecutionChain成员变量:
     * === Object handler: 请求处理器: 常用类型: HandlerMethod, HttpRequestHandler, Servlet或任何自定义的类型
     * === HandlerInterceptor[] interceptors: 拦截器(preHandle, postHandle, afterCompletion)
     * -> HandlerMethod(用于@RequestMapping注解)成员变量:
     * === Class<?> beanType: 方法所属的类
     * === Object bean: 实际对象
     * === Method method: 方法
     * === MethodParameter[] parameters: 方法参数
     */
    private void initHandlerMappings(ApplicationContext context) {
        this.handlerMappings = null;

        // 如果需要获取所有HandlerMapping, 则从Application中找到所有的HandlerMapping, 包括parent context中的HandlerMapping
        if (this.detectAllHandlerMappings) {
            // Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerMapping> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerMappings = new ArrayList<>(matchingBeans.values());
                // We keep HandlerMappings in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerMappings);
            }
        }
        // 如果只使用一个确定的HandlerMapping, 从ApplicationContext中查找: beanName = handlerMapping
        else {
            try {
                HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
                this.handlerMappings = Collections.singletonList(hm);
            }
            catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerMapping later.
            }
        }

        // 至少需要一个HandlerMapping, 如果没有则使用默认的HandlerMapping(BeanNameUrlHandlerMapping)
        if (this.handlerMappings == null) {
            this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerMappings found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    /**
     * 初始化HandlerAdapters:
     * -> 1.如果需要获取所有HandlerAdapter, 则从Application中找到所有的HandlerAdapter, 包括parent context中的HandlerAdapter
     * -> 2.如果只使用一个确定的HandlerAdapter, 从ApplicationContext中查找: beanName = handlerAdapter
     * -> 3.至少需要一个HandlerAdapter, 如果没有则使用默认的HandlerAdapter(SimpleControllerHandlerAdapter)
     * -> HandlerAdapter接口方法:
     * === boolean supports(Object handler); // 是否支持给定的handler实例
     * === ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception; // 使用给定的handler处理请求
     * === long getLastModified(HttpServletRequest request, Object handler); // 根据request返回last-modified值, 可返回-1表示不支持
     * -> 常用HandlerAdapter类型:
     * === RequestMappingHandlerAdapter
     * === HttpRequestHandlerAdapter
     * === SimpleControllerHandlerAdapter
     */
    private void initHandlerAdapters(ApplicationContext context) {
        this.handlerAdapters = null;

        // 如果需要获取所有HandlerAdapter, 则从Application中找到所有的HandlerAdapter, 包括parent context中的HandlerAdapter
        if (this.detectAllHandlerAdapters) {
            // Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerAdapter> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerAdapters = new ArrayList<>(matchingBeans.values());
                // We keep HandlerAdapters in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerAdapters);
            }
        }
        // 如果只使用一个确定的HandlerAdapter, 从ApplicationContext中查找: beanName = handlerAdapter
        else {
            try {
                HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
                this.handlerAdapters = Collections.singletonList(ha);
            }
            catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerAdapter later.
            }
        }

        // 至少需要一个HandlerAdapter, 如果没有则使用默认的HandlerAdapter(SimpleControllerHandlerAdapter)
        if (this.handlerAdapters == null) {
            this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerAdapters found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    /**
     * 初始化HandlerExceptionResolver:
     * -> 1.如果需要获取所有HandlerExceptionResolver, 则从Application中找到所有的HandlerExceptionResolver, 包括parent context中的HandlerExceptionResolver
     * -> 2.如果只使用一个确定的HandlerExceptionResolver, 从ApplicationContext中查找: beanName = handlerExceptionResolver
     * -> 3.至少需要一个HandlerExceptionResolver, 如果没有则使用默认的HandlerExceptionResolver
     * -> HandlerExceptionResolver接口方法:
     * === ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex); // 处理异常: 并用handler处理结果
     * -> 常用HandlerExceptionResolver类型:
     * === ExceptionHandlerExceptionResolver
     * === HandlerExceptionResolverComposite
     * === ResponseStatusExceptionResolver
     * === DefaultHandlerExceptionResolver
     * === ApplicationSimpleMappingExceptionResolver
     */
    private void initHandlerExceptionResolvers(ApplicationContext context) {
        this.handlerExceptionResolvers = null;

        // 如果需要获取所有HandlerExceptionResolver, 则从Application中找到所有的HandlerExceptionResolver, 包括parent context中的HandlerExceptionResolver
        if (this.detectAllHandlerExceptionResolvers) {
            // Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils
                    .beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerExceptionResolvers = new ArrayList<>(matchingBeans.values());
                // We keep HandlerExceptionResolvers in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerExceptionResolvers);
            }
        }
        // 如果只使用一个确定的HandlerExceptionResolver, 从ApplicationContext中查找: beanName = handlerExceptionResolver
        else {
            try {
                HandlerExceptionResolver her =
                        context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
                this.handlerExceptionResolvers = Collections.singletonList(her);
            }
            catch (NoSuchBeanDefinitionException ex) {
                // Ignore, no HandlerExceptionResolver is fine too.
            }
        }

        // 至少需要一个HandlerExceptionResolver, 如果没有则使用默认的HandlerExceptionResolver
        if (this.handlerExceptionResolvers == null) {
            this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerExceptionResolvers found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    /**
     * 初始化RequestToViewNameTranslator:
     * -> RequestToViewNameTranslator接口方法:
     * === String getViewName(HttpServletRequest request) throws Exception; // 根据request解析出viewName
     */
    private void initRequestToViewNameTranslator(ApplicationContext context) {
        // 从ApplicationContext中查找RequestToViewNameTranslator, beanName = viewNameTranslator
        try {
            this.viewNameTranslator =
                    context.getBean(REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME, RequestToViewNameTranslator.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using RequestToViewNameTranslator [" + this.viewNameTranslator + "]");
            }
        }
        // 若不存在则设置为默认的RequestToViewNameTranslator(DefaultRequestToViewNameTranslator)
        catch (NoSuchBeanDefinitionException ex) {
            // We need to use the default.
            this.viewNameTranslator = getDefaultStrategy(context, RequestToViewNameTranslator.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate RequestToViewNameTranslator with name '" +
                        REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME + "': using default [" + this.viewNameTranslator +
                        "]");
            }
        }
    }

    /**
     * 初始化ViewResolvers:
     * -> 1.如果需要获取所有ViewResolver, 则从Application中找到所有的ViewResolver, 包括parent context中的ViewResolver
     * -> 2.如果只使用一个确定的ViewResolver, 从ApplicationContext中查找: beanName = viewResolver
     * -> 3.至少需要一个ViewResolver, 如果没有则使用默认的ViewResolver(InternalResourceViewResolver)
     * -> ViewResolver接口方法:
     * === View resolveViewName(String viewName, Locale locale) throws Exception; // 根据viewName解析出View
     * -> 常用ViewResolver类型:
     * === FreeMarkerViewResolver
     * === InternalResourceViewResolver
     * === ViewResolverComposite
     */
    private void initViewResolvers(ApplicationContext context) {
        this.viewResolvers = null;

        // 如果需要获取所有ViewResolver, 则从Application中找到所有的ViewResolver, 包括parent context中的ViewResolver
        if (this.detectAllViewResolvers) {
            // Find all ViewResolvers in the ApplicationContext, including ancestor contexts.
            Map<String, ViewResolver> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ViewResolver.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.viewResolvers = new ArrayList<>(matchingBeans.values());
                // We keep ViewResolvers in sorted order.
                AnnotationAwareOrderComparator.sort(this.viewResolvers);
            }
        }
        // 如果只使用一个确定的ViewResolver, 从ApplicationContext中查找: beanName = viewResolver
        else {
            try {
                ViewResolver vr = context.getBean(VIEW_RESOLVER_BEAN_NAME, ViewResolver.class);
                this.viewResolvers = Collections.singletonList(vr);
            }
            catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default ViewResolver later.
            }
        }

        // 至少需要一个ViewResolver, 如果没有则使用默认的ViewResolver(InternalResourceViewResolver)
        if (this.viewResolvers == null) {
            this.viewResolvers = getDefaultStrategies(context, ViewResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No ViewResolvers found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    /**
     * 初始化FlashMapManager:
     * -> FlashMapManager接口方法:
     * === FlashMap retrieveAndUpdate(HttpServletRequest request, HttpServletResponse response); // 根据给定request找到之前request保存的FlashMap
     * === void saveOutputFlashMap(FlashMap flashMap, HttpServletRequest request, HttpServletResponse response); // 保存给定的FlashMap
     */
    private void initFlashMapManager(ApplicationContext context) {
        // 从ApplicationContext中查找FlashMapManager, beanName = flashMapManager
        try {
            this.flashMapManager = context.getBean(FLASH_MAP_MANAGER_BEAN_NAME, FlashMapManager.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using FlashMapManager [" + this.flashMapManager + "]");
            }
        }
        // 若不存在则设置为默认的FlashMapManager(DefaultFlashMapManager)
        catch (NoSuchBeanDefinitionException ex) {
            // We need to use the default.
            this.flashMapManager = getDefaultStrategy(context, FlashMapManager.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate FlashMapManager with name '" +
                        FLASH_MAP_MANAGER_BEAN_NAME + "': using default [" + this.flashMapManager + "]");
            }
        }
    }

}
