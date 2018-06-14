package com.demos.java.jdkanalyzer.spring;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.*;
import org.springframework.web.context.request.async.AsyncWebRequest;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/14
 *
 * ===== FrameworkServlet.service(): 重写HttpServlet.service(), 使得PATCH请求也可以调用`processRequest()`进行处理
 * -> FrameworkServlet重写了HttpServlet的doGet(), doPost()等方法并统一调用processRequest()进行处理
 * ===== Framework.processRequest(): 处理请求
 * -> 1.获取之前的LocaleContext并使用当前request中的locale创建LocaleContext
 * -> 2.获取之前的RequestAttributes并使用当前的request和response创建ServletRequestAttributes
 * -> 3.注册异步处理拦截器
 * -> 4.将当前的request、localeContext和requestAttributes保存到当前线程(ThreadLocal)
 * -> 5.处理请求: doService()
 * -> 6.将之前的request、localeContext和requestAttributes设置到当前线程(ThreadLocal)
 * -> 7.广播请求处理事件
 * ===== DispatcherServlet.doService():
 * -> 1.保存request中的所有属性快照: 为了处理include请求, 这里先保存request中的所有属性, 处理请求完成之后将这些属性重新设置到request中
 * -> 2.设置request属性: WebApplicationContext, localeResolver, themeResolver, themeSource
 * -> 3.如果flashMapManager不为空, 处理FlashMap
 * -> 4.mvc请求分发处理: DispatcherServlet.doDispatch()
 * -> 5.将之前保存的属性快照重新设置到request中
 * ===== DispatcherServlet.doDispatch():
 * -> 1.判断是否时文件上传请求: POST && ContentType.startWith("multipart/")
 * -> 2.遍历handlerMappings, 调用getHandler()方法返回HandlerExecutionChain
 * === 如果没有匹配的HandlerExecutionChain: 返回404
 * -> 4.遍历handlerAdapters, 找到支持该handler的HandlerAdapter(support() == true)
 * -> 5.处理`last-modified`请求头: 本次请求为`GET`请求, 且自`last-modified`以来未发生过变更, 则直接返回
 * -> 6.遍历拦截器链调用`preHandle()`方法, 一旦某个拦截器返回false, 则会遍历拦截器调用`afterCompletion()`, 之后直接返回
 * -> 7.调用`HandlerAdapter.handle()`进行实际的业务处理, 返回`ModelAndView`对象, 若处理方法为`@ResponseBody`, 则这一步已经将结果写到response
 * -> 8.如果是异步请求, 直接返回, 返回前调用AsyncHandlerInterceptor.afterConcurrentHandlingStarted()方法
 * -> 9.如果处理请求返回的ModelAndView不为空: 使用viewNameTranslator从request中解析出viewName, 并将viewName设置到ModelAndView中
 * -> 10.遍历拦截器链调用`postHandle()`方法
 * -> 11.在之前的处理过程中, 所有的异常都会被捕获
 * -> 12.处理返回结果, 处理完成后遍历拦截器调用`afterCompletion()`
 * -> 13.若在处理返回结果时抛出异常, 也会遍历拦截器调用`afterCompletion()`
 * -> 14.如果是异步请求: 调用AsyncHandlerInterceptor.afterConcurrentHandlingStarted()方法
 * -> 15.如果是multipart请求: 清理multipart资源
 * ===== 处理返回结果:
 * -> 1.如果发生了异常: 则进行异常处理, 并包装成ModelAndView
 * -> 2.如果ModelAndView不为空, 则渲染返回结果
 * -> 3.如果是异步请求: 直接返回
 * -> 4.遍历拦截器调用`afterCompletion()`
 * ===== 渲染返回结果:
 * -> 1.遍历viewResolvers根据viewName解析View
 * -> 2.调用View.render()渲染返回结果
 */
public class DispatcherProcessAnalyzer {

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
     * FrameworkServlet.service(): 重写HttpServlet.service(), 使得PATCH请求也可以调用`processRequest()`进行处理
     * -> FrameworkServlet重写了HttpServlet的doGet(), doPost()等方法并统一调用processRequest()进行处理
     */
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 如果请求方法是PATCH或空, 调用processRequest()进行处理: HttpServlet中没有PATCH请求对应的doPatch()方法
        HttpMethod httpMethod = HttpMethod.resolve(request.getMethod());
        if (httpMethod == HttpMethod.PATCH || httpMethod == null) {
            processRequest(request, response);
        }
        // 否则: 调用HttpServlet.service(), HttpServlet.service()会根据请求方式将请求分发给doGet(), doPost()等方法
        // FrameworkServlet重写了doGet(), doPost()等方法并统一调用processRequest()进行处理
        else {
            super.service(request, response);
        }
    }

    /**
     * Framework.processRequest(): 处理请求
     * -> 1.获取之前的LocaleContext并使用当前request中的locale创建LocaleContext
     * -> 2.获取之前的RequestAttributes并使用当前的request和response创建ServletRequestAttributes
     * -> 3.注册异步处理拦截器
     * -> 4.将当前的request、localeContext和requestAttributes保存到当前线程(ThreadLocal)
     * -> 5.处理请求: doService()
     * -> 6.将之前的request、localeContext和requestAttributes设置到当前线程(ThreadLocal)
     * -> 7.广播请求处理事件
     */
    protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 请求开始时间
        long startTime = System.currentTimeMillis();
        Throwable failureCause = null;

        // 获取之前的LocaleContext并使用当前request中的locale创建LocaleContext
        LocaleContext previousLocaleContext = LocaleContextHolder.getLocaleContext();
        LocaleContext localeContext = buildLocaleContext(request);

        // 获取之前的RequestAttributes并使用当前的request和response创建ServletRequestAttributes
        RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes requestAttributes = buildRequestAttributes(request, response, previousAttributes);

        // 注册异步处理拦截器到异步管理器: 异步管理器保存在request中
        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
        asyncManager.registerCallableInterceptor(FrameworkServlet.class.getName(), new RequestBindingInterceptor());

        // 将当前的request、localeContext和requestAttributes保存到当前线程(ThreadLocal)
        initContextHolders(request, localeContext, requestAttributes);

        try {
            // 处理请求
            doService(request, response);
        }
        catch (ServletException | IOException ex) {
            failureCause = ex;
            throw ex;
        }
        catch (Throwable ex) {
            failureCause = ex;
            throw new NestedServletException("Request processing failed", ex);
        }

        finally {
            // 将之前的request、localeContext和requestAttributes设置到当前线程(ThreadLocal)
            resetContextHolders(request, previousLocaleContext, previousAttributes);
            if (requestAttributes != null) {
                requestAttributes.requestCompleted();
            }

            if (logger.isDebugEnabled()) {
                if (failureCause != null) {
                    this.logger.debug("Could not complete request", failureCause);
                }
                else {
                    if (asyncManager.isConcurrentHandlingStarted()) {
                        logger.debug("Leaving response open for concurrent processing");
                    }
                    else {
                        this.logger.debug("Successfully completed request");
                    }
                }
            }
            // 广播请求处理事件
            publishRequestHandledEvent(request, response, startTime, failureCause);
        }
    }

    /**
     * 使用request中的locale创建LocaleContext
     */
    protected LocaleContext buildLocaleContext(HttpServletRequest request) {
        return new SimpleLocaleContext(request.getLocale());
    }

    /**
     * 使用request和response创建ServletRequestAttributes
     */
    protected ServletRequestAttributes buildRequestAttributes(HttpServletRequest request,
                                                              @Nullable HttpServletResponse response, @Nullable RequestAttributes previousAttributes) {

        if (previousAttributes == null || previousAttributes instanceof ServletRequestAttributes) {
            return new ServletRequestAttributes(request, response);
        }
        else {
            return null;  // preserve the pre-bound RequestAttributes instance
        }
    }

    /**
     * DispatcherServlet.doService():
     * -> 1.保存request中的所有属性快照: 为了处理include请求, 这里先保存request中的所有属性, 处理请求完成之后将这些属性重新设置到request中
     * -> 2.设置request属性: WebApplicationContext, localeResolver, themeResolver, themeSource
     * -> 3.如果flashMapManager不为空, 处理FlashMap
     * -> 4.mvc请求分发处理: DispatcherServlet.doDispatch()
     * -> 5.将之前保存的属性快照重新设置到request中
     */
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (logger.isDebugEnabled()) {
            String resumed = WebAsyncUtils.getAsyncManager(request).hasConcurrentResult() ? " resumed" : "";
            logger.debug("DispatcherServlet with name '" + getServletName() + "'" + resumed +
                    " processing " + request.getMethod() + " request for [" + getRequestUri(request) + "]");
        }

        // 保存request中的所有属性快照: 为了处理include请求, 这里先保存request中的所有属性, 处理请求完成之后将这些属性重新设置到request中
        Map<String, Object> attributesSnapshot = null;
        if (WebUtils.isIncludeRequest(request)) {
            attributesSnapshot = new HashMap<>();
            Enumeration<?> attrNames = request.getAttributeNames();
            while (attrNames.hasMoreElements()) {
                String attrName = (String) attrNames.nextElement();
                if (this.cleanupAfterInclude || attrName.startsWith(DEFAULT_STRATEGIES_PREFIX)) {
                    attributesSnapshot.put(attrName, request.getAttribute(attrName));
                }
            }
        }

        // 设置request属性: WebApplicationContext, localeResolver, themeResolver, themeSource
        request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
        request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
        request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);
        request.setAttribute(THEME_SOURCE_ATTRIBUTE, getThemeSource());

        // 如果flashMapManager不为空, 处理FlashMap
        if (this.flashMapManager != null) {
            FlashMap inputFlashMap = this.flashMapManager.retrieveAndUpdate(request, response);
            if (inputFlashMap != null) {
                request.setAttribute(INPUT_FLASH_MAP_ATTRIBUTE, Collections.unmodifiableMap(inputFlashMap));
            }
            request.setAttribute(OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
            request.setAttribute(FLASH_MAP_MANAGER_ATTRIBUTE, this.flashMapManager);
        }

        try {
            // mvc请求分发处理: DispatcherServlet.doDispatch()
            doDispatch(request, response);
        }
        finally {
            if (!WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
                // 将之前保存的属性快照重新设置到request中
                if (attributesSnapshot != null) {
                    restoreAttributesAfterInclude(request, attributesSnapshot);
                }
            }
        }
    }

    /**
     * DispatcherServlet.doDispatch():
     * -> 1.判断是否时文件上传请求: POST && ContentType.startWith("multipart/")
     * -> 2.遍历handlerMappings, 调用getHandler()方法返回HandlerExecutionChain
     * === 如果没有匹配的HandlerExecutionChain: 返回404
     * -> 4.遍历handlerAdapters, 找到支持该handler的HandlerAdapter(support() == true)
     * -> 5.处理`last-modified`请求头: 本次请求为`GET`请求, 且自`last-modified`以来未发生过变更, 则直接返回
     * -> 6.遍历拦截器链调用`preHandle()`方法, 一旦某个拦截器返回false, 则会遍历拦截器调用`afterCompletion()`, 之后直接返回
     * -> 7.调用`HandlerAdapter.handle()`进行实际的业务处理, 返回`ModelAndView`对象, 若处理方法为`@ResponseBody`, 则这一步已经将结果写到response
     * -> 8.如果是异步请求, 直接返回, 返回前调用AsyncHandlerInterceptor.afterConcurrentHandlingStarted()方法
     * -> 9.如果处理请求返回的ModelAndView不为空: 使用viewNameTranslator从request中解析出viewName, 并将viewName设置到ModelAndView中
     * -> 10.遍历拦截器链调用`postHandle()`方法
     * -> 11.在之前的处理过程中, 所有的异常都会被捕获
     * -> 12.处理返回结果, 处理完成后遍历拦截器调用`afterCompletion()`
     * -> 13.若在处理返回结果时抛出异常, 也会遍历拦截器调用`afterCompletion()`
     * -> 14.如果是异步请求: 调用AsyncHandlerInterceptor.afterConcurrentHandlingStarted()方法
     * -> 15.如果是multipart请求: 清理multipart资源
     */
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpServletRequest processedRequest = request;
        HandlerExecutionChain mappedHandler = null;
        boolean multipartRequestParsed = false;

        // 从request中获取异步管理器
        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

        try {
            ModelAndView mv = null;
            Exception dispatchException = null;

            try {
                // 判断是否时文件上传请求: POST && ContentType.startWith("multipart/")
                processedRequest = checkMultipart(request);
                // 如果processedRequest != request: 说明processRequest在checkMultipart()方法中发生了改变, 该request是multipart
                multipartRequestParsed = (processedRequest != request);

                // 遍历handlerMappings, 调用getHandler()方法返回HandlerExecutionChain
                mappedHandler = getHandler(processedRequest);
                // 如果没有匹配的mappedHandler: 返回404
                if (mappedHandler == null) {
                    noHandlerFound(processedRequest, response);
                    return;
                }

                // 遍历handlerAdapters, 找到支持该handler的HandlerAdapter(support() == true)
                HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

                // 处理`last-modified`请求头
                String method = request.getMethod();
                boolean isGet = "GET".equals(method);
                // 本次请求为`GET`请求, 且自`last-modified`以来未发生过变更, 则直接返回
                if (isGet || "HEAD".equals(method)) {
                    long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Last-Modified value for [" + getRequestUri(request) + "] is: " + lastModified);
                    }
                    if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
                        return;
                    }
                }

                // 遍历拦截器链调用`preHandle()`方法, 一旦某个拦截器返回false, 则会遍历拦截器调用`afterCompletion()`, 之后直接返回
                if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                    return;
                }

                // 调用`HandlerAdapter.handle()`进行实际的业务处理, 返回`ModelAndView`对象
                // 若处理方法为`@ResponseBody`, 则这一步已经将结果写到response: RequestResponseBodyMethodProcessor.handleReturnValue()
                mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

                // 如果是异步请求, 直接返回
                // 返回前调用AsyncHandlerInterceptor.afterConcurrentHandlingStarted()方法, 而不是HandlerInterceptor的postHandle()和afterCompletion()方法
                if (asyncManager.isConcurrentHandlingStarted()) {
                    return;
                }

                // 如果处理请求返回的ModelAndView不为空: 使用viewNameTranslator从request中解析出viewName, 并将viewName设置到ModelAndView中
                applyDefaultViewName(processedRequest, mv);
                // 遍历拦截器链调用`postHandle()`方法
                mappedHandler.applyPostHandle(processedRequest, response, mv);
            }
            catch (Exception ex) {
                dispatchException = ex;
            }
            catch (Throwable err) {
                // As of 4.3, we're processing Errors thrown from handler methods as well,
                // making them available for @ExceptionHandler methods and other scenarios.
                dispatchException = new NestedServletException("Handler dispatch failed", err);
            }
            // 处理返回结果, 在之前的处理过程中, 所有的异常都会被捕获, 并在这里进行处理
            processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
        }
        catch (Exception ex) {
            // 遍历拦截器调用`afterCompletion()`
            triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
        }
        catch (Throwable err) {
            // 遍历拦截器调用`afterCompletion()`
            triggerAfterCompletion(processedRequest, response, mappedHandler,
                    new NestedServletException("Handler processing failed", err));
        }
        finally {
            // 如果是异步请求: 调用AsyncHandlerInterceptor.afterConcurrentHandlingStarted()方法, 而不是HandlerInterceptor的postHandle()和afterCompletion()方法
            if (asyncManager.isConcurrentHandlingStarted()) {
                // Instead of postHandle and afterCompletion
                if (mappedHandler != null) {
                    mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
                }
            }
            // 如果是multipart请求: 清理multipart资源
            else {
                // Clean up any resources used by a multipart request.
                if (multipartRequestParsed) {
                    cleanupMultipart(processedRequest);
                }
            }
        }
    }

    /**
     * 处理返回结果:
     * -> 1.如果发生了异常: 则进行异常处理, 并包装成ModelAndView
     * -> 2.如果ModelAndView不为空, 则渲染返回结果
     * -> 3.如果是异步请求: 直接返回
     * -> 4.遍历拦截器调用`afterCompletion()`
     */
    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
                                       @Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv,
                                       @Nullable Exception exception) throws Exception {

        boolean errorView = false;

        // 如果发生了异常: 则进行异常处理
        if (exception != null) {
            if (exception instanceof ModelAndViewDefiningException) {
                logger.debug("ModelAndViewDefiningException encountered", exception);
                mv = ((ModelAndViewDefiningException) exception).getModelAndView();
            }
            else {
                Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
                // 遍历handlerExceptionResolvers处理异常, 返回ModelAndView
                mv = processHandlerException(request, response, handler, exception);
                errorView = (mv != null);
            }
        }

        // 如果ModelAndView不为空, 则渲染返回结果
        if (mv != null && !mv.wasCleared()) {
            render(mv, request, response);
            if (errorView) {
                WebUtils.clearErrorRequestAttributes(request);
            }
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Null ModelAndView returned to DispatcherServlet with name '" + getServletName() +
                        "': assuming HandlerAdapter completed request handling");
            }
        }

        // 如果是异步请求: 直接返回
        if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
            // Concurrent handling started during a forward
            return;
        }

        // 遍历拦截器调用`afterCompletion()`
        if (mappedHandler != null) {
            mappedHandler.triggerAfterCompletion(request, response, null);
        }
    }

    /**
     * 渲染返回结果:
     * -> 1.遍历viewResolvers根据viewName解析View
     * -> 2.调用View.render()渲染返回结果
     */
    protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Determine locale for request and apply it to the response.
        Locale locale =
                (this.localeResolver != null ? this.localeResolver.resolveLocale(request) : request.getLocale());
        response.setLocale(locale);

        View view;
        String viewName = mv.getViewName();
        // 遍历viewResolvers根据viewName解析View
        if (viewName != null) {
            // We need to resolve the view name.
            view = resolveViewName(viewName, mv.getModelInternal(), locale, request);
            if (view == null) {
                throw new ServletException("Could not resolve view with name '" + mv.getViewName() +
                        "' in servlet with name '" + getServletName() + "'");
            }
        }
        else {
            // No need to lookup: the ModelAndView object contains the actual View object.
            view = mv.getView();
            if (view == null) {
                throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a " +
                        "View object in servlet with name '" + getServletName() + "'");
            }
        }

        // Delegate to the View object for rendering.
        if (logger.isDebugEnabled()) {
            logger.debug("Rendering view [" + view + "] in DispatcherServlet with name '" + getServletName() + "'");
        }
        // 调用View.render()渲染返回结果
        try {
            if (mv.getStatus() != null) {
                response.setStatus(mv.getStatus().value());
            }
            view.render(mv.getModelInternal(), request, response);
        }
        catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error rendering view [" + view + "] in DispatcherServlet with name '" +
                        getServletName() + "'", ex);
            }
            throw ex;
        }
    }

    /* ==================== RequestBody, ResponseBody分析 ==================== */
    /**
     * AbstractHandlerMethodAdapter.supports()
     */
    public final boolean supports(Object handler) {
        return (handler instanceof HandlerMethod && supportsInternal((HandlerMethod) handler));
    }

    /**
     * AbstractHandlerMethodAdapter.handle()
     */
    public final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        return handleInternal(request, response, (HandlerMethod) handler);
    }

    /**
     * AbstractHandlerMethodAdapter.getLastModified()
     */
    public final long getLastModified(HttpServletRequest request, Object handler) {
        return getLastModifiedInternal(request, (HandlerMethod) handler);
    }

    /**
     * RequestMappingHandlerAdapter.supportsInternal()
     */
    protected boolean supportsInternal(HandlerMethod handlerMethod) {
        return true;
    }

    /**
     * RequestMappingHandlerAdapter.getLastModifiedInternal()
     */
    protected long getLastModifiedInternal(HttpServletRequest request, HandlerMethod handlerMethod) {
        return -1;
    }

    /**
     * RequestMappingHandlerAdapter.handleInternal()
     */
    protected ModelAndView handleInternal(HttpServletRequest request,
                                          HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

        ModelAndView mav;
        checkRequest(request);

        // Execute invokeHandlerMethod in synchronized block if required.
        if (this.synchronizeOnSession) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object mutex = WebUtils.getSessionMutex(session);
                synchronized (mutex) {
                    mav = invokeHandlerMethod(request, response, handlerMethod);
                }
            }
            else {
                // No HttpSession available -> no mutex necessary
                mav = invokeHandlerMethod(request, response, handlerMethod);
            }
        }
        else {
            // No synchronization on session demanded at all...
            mav = invokeHandlerMethod(request, response, handlerMethod);
        }

        if (!response.containsHeader(HEADER_CACHE_CONTROL)) {
            if (getSessionAttributesHandler(handlerMethod).hasSessionAttributes()) {
                applyCacheSeconds(response, this.cacheSecondsForSessionAttributeHandlers);
            }
            else {
                prepareResponse(response);
            }
        }

        return mav;
    }

    /**
     * Check the given request for supported methods and a required session, if any.
     * @param request current HTTP request
     * @throws ServletException if the request cannot be handled because a check failed
     * @since 4.2
     */
    protected final void checkRequest(HttpServletRequest request) throws ServletException {
        // Check whether we should support the request method.
        String method = request.getMethod();
        if (this.supportedMethods != null && !this.supportedMethods.contains(method)) {
            throw new HttpRequestMethodNotSupportedException(method, this.supportedMethods);
        }

        // Check whether a session is required.
        if (this.requireSession && request.getSession(false) == null) {
            throw new HttpSessionRequiredException("Pre-existing session required but none found");
        }
    }

    /**
     * Prepare the given response according to the settings of this generator.
     * Applies the number of cache seconds specified for this generator.
     * @param response current HTTP response
     * @since 4.2
     */
    protected final void prepareResponse(HttpServletResponse response) {
        if (this.cacheControl != null) {
            applyCacheControl(response, this.cacheControl);
        }
        else {
            applyCacheSeconds(response, this.cacheSeconds);
        }
        if (this.varyByRequestHeaders != null) {
            for (String value : getVaryRequestHeadersToAdd(response, this.varyByRequestHeaders)) {
                response.addHeader("Vary", value);
            }
        }
    }

    /**
     * Invoke the {@link RequestMapping} handler method preparing a {@link ModelAndView}
     * if view resolution is required.
     * @since 4.2
     * @see #createInvocableHandlerMethod(HandlerMethod)
     */
    protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
                                               HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        try {
            WebDataBinderFactory binderFactory = getDataBinderFactory(handlerMethod);
            ModelFactory modelFactory = getModelFactory(handlerMethod, binderFactory);

            ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
            if (this.argumentResolvers != null) {
                invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
            }
            if (this.returnValueHandlers != null) {
                invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
            }
            invocableMethod.setDataBinderFactory(binderFactory);
            invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);

            ModelAndViewContainer mavContainer = new ModelAndViewContainer();
            mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
            modelFactory.initModel(webRequest, mavContainer, invocableMethod);
            mavContainer.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);

            AsyncWebRequest asyncWebRequest = WebAsyncUtils.createAsyncWebRequest(request, response);
            asyncWebRequest.setTimeout(this.asyncRequestTimeout);

            WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
            asyncManager.setTaskExecutor(this.taskExecutor);
            asyncManager.setAsyncWebRequest(asyncWebRequest);
            asyncManager.registerCallableInterceptors(this.callableInterceptors);
            asyncManager.registerDeferredResultInterceptors(this.deferredResultInterceptors);

            if (asyncManager.hasConcurrentResult()) {
                Object result = asyncManager.getConcurrentResult();
                mavContainer = (ModelAndViewContainer) asyncManager.getConcurrentResultContext()[0];
                asyncManager.clearConcurrentResult();
                if (logger.isDebugEnabled()) {
                    logger.debug("Found concurrent result value [" + result + "]");
                }
                invocableMethod = invocableMethod.wrapConcurrentResult(result);
            }

            invocableMethod.invokeAndHandle(webRequest, mavContainer);
            if (asyncManager.isConcurrentHandlingStarted()) {
                return null;
            }

            return getModelAndView(mavContainer, modelFactory, webRequest);
        }
        finally {
            webRequest.requestCompleted();
        }
    }

    /**
     * ServletInvocableHandlerMethod.invokeAndHandle()
     *
     * Invoke the method and handle the return value through one of the
     * configured {@link HandlerMethodReturnValueHandler}s.
     * @param webRequest the current request
     * @param mavContainer the ModelAndViewContainer for this request
     * @param providedArgs "given" arguments matched by type (not resolved)
     */
    public void invokeAndHandle(ServletWebRequest webRequest, ModelAndViewContainer mavContainer,
                                Object... providedArgs) throws Exception {

        Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs);
        setResponseStatus(webRequest);

        if (returnValue == null) {
            if (isRequestNotModified(webRequest) || getResponseStatus() != null || mavContainer.isRequestHandled()) {
                mavContainer.setRequestHandled(true);
                return;
            }
        }
        else if (StringUtils.hasText(getResponseStatusReason())) {
            mavContainer.setRequestHandled(true);
            return;
        }

        mavContainer.setRequestHandled(false);
        Assert.state(this.returnValueHandlers != null, "No return value handlers");
        try {
            this.returnValueHandlers.handleReturnValue(
                    returnValue, getReturnValueType(returnValue), mavContainer, webRequest);
        }
        catch (Exception ex) {
            if (logger.isTraceEnabled()) {
                logger.trace(getReturnValueHandlingErrorMessage("Error handling return value", returnValue), ex);
            }
            throw ex;
        }
    }

    /**
     * Invoke the method after resolving its argument values in the context of the given request.
     * <p>Argument values are commonly resolved through {@link HandlerMethodArgumentResolver}s.
     * The {@code providedArgs} parameter however may supply argument values to be used directly,
     * i.e. without argument resolution. Examples of provided argument values include a
     * {@link WebDataBinder}, a {@link SessionStatus}, or a thrown exception instance.
     * Provided argument values are checked before argument resolvers.
     * @param request the current request
     * @param mavContainer the ModelAndViewContainer for this request
     * @param providedArgs "given" arguments matched by type, not resolved
     * @return the raw value returned by the invoked method
     * @throws Exception raised if no suitable argument resolver can be found,
     * or if the method raised an exception
     */
    @Nullable
    public Object invokeForRequest(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
                                   Object... providedArgs) throws Exception {

        Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs);
        if (logger.isTraceEnabled()) {
            logger.trace("Invoking '" + ClassUtils.getQualifiedMethodName(getMethod(), getBeanType()) +
                    "' with arguments " + Arrays.toString(args));
        }
        Object returnValue = doInvoke(args);
        if (logger.isTraceEnabled()) {
            logger.trace("Method [" + ClassUtils.getQualifiedMethodName(getMethod(), getBeanType()) +
                    "] returned [" + returnValue + "]");
        }
        return returnValue;
    }

    /**
     * Get the method argument values for the current request.
     */
    private Object[] getMethodArgumentValues(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
                                             Object... providedArgs) throws Exception {

        MethodParameter[] parameters = getMethodParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
            args[i] = resolveProvidedArgument(parameter, providedArgs);
            if (args[i] != null) {
                continue;
            }
            if (this.argumentResolvers.supportsParameter(parameter)) {
                try {
                    args[i] = this.argumentResolvers.resolveArgument(
                            parameter, mavContainer, request, this.dataBinderFactory);
                    continue;
                }
                catch (Exception ex) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(getArgumentResolutionErrorMessage("Failed to resolve", i), ex);
                    }
                    throw ex;
                }
            }
            if (args[i] == null) {
                throw new IllegalStateException("Could not resolve method parameter at index " +
                        parameter.getParameterIndex() + " in " + parameter.getExecutable().toGenericString() +
                        ": " + getArgumentResolutionErrorMessage("No suitable resolver for", i));
            }
        }
        return args;
    }


    /*
     * ===== RequestResponseBodyMethodProcessor解析
     * -> GenericHttpMessageConverter接口方法:
     * === boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType); // 是否支持读取指定类型的参数
     * === T read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage); // 从请求中读取出指定类型的参数
     * === boolean canWrite(@Nullable Type type, Class<?> clazz, @Nullable MediaType mediaType); // 是否支持指定类型的数据写入
     * === void write(T t, @Nullable Type type, @Nullable MediaType contentType, HttpOutputMessage outputMessage); // 往输出中写入指定类型的数据
     */
    /**
     * 支持有@RequestBody注解的参数
     */
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestBody.class);
    }

    /**
     * 支持有@ResponseBody注解的返回值
     * @param returnType
     * @return
     */
    public boolean supportsReturnType(MethodParameter returnType) {
        return (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ResponseBody.class) ||
                returnType.hasMethodAnnotation(ResponseBody.class));
    }

    /**
     * Throws MethodArgumentNotValidException if validation fails.
     * @throws HttpMessageNotReadableException if {@link RequestBody#required()}
     * is {@code true} and there is no body content or if there is no suitable
     * converter to read the content with.
     */
    public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

        parameter = parameter.nestedIfOptional();
        Object arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
        String name = Conventions.getVariableNameForParameter(parameter);

        if (binderFactory != null) {
            WebDataBinder binder = binderFactory.createBinder(webRequest, arg, name);
            if (arg != null) {
                validateIfApplicable(binder, parameter);
                if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
                    throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
                }
            }
            if (mavContainer != null) {
                mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, binder.getBindingResult());
            }
        }

        return adaptArgumentIfNecessary(arg, parameter);
    }

    protected <T> Object readWithMessageConverters(NativeWebRequest webRequest, MethodParameter parameter,
                                                   Type paramType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {

        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");
        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(servletRequest);

        Object arg = readWithMessageConverters(inputMessage, parameter, paramType);
        if (arg == null && checkRequired(parameter)) {
            throw new HttpMessageNotReadableException("Required request body is missing: " +
                    parameter.getExecutable().toGenericString());
        }
        return arg;
    }

    protected boolean checkRequired(MethodParameter parameter) {
        RequestBody requestBody = parameter.getParameterAnnotation(RequestBody.class);
        return (requestBody != null && requestBody.required() && !parameter.isOptional());
    }

    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest)
            throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {

        mavContainer.setRequestHandled(true);
        ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

        // Try even with null return value. ResponseBodyAdvice could get involved.
        writeWithMessageConverters(returnValue, returnType, inputMessage, outputMessage);
    }

}
