package com.demos.java.jdkanalyzer.mybatis;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.builder.*;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.builder.annotation.MethodResolver;
import org.apache.ibatis.builder.xml.XMLIncludeTransformer;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/12
 * ===== 关键类:
 * -> Core: Configuration, SqlSessionFactory, SqlSession, MappedStatement, MapperProxy, MapperMethod
 * -> Builder: XMLConfigurationBuilder, XMLMapperBuilder, XMLStatementBuilder
 * -> BaseExecutor: SimpleExecutor, ReuseExecutor, BatchExecutor
 * -> CachingExecutor
 * -> eviction Cache: LRU, FIFO, SOFT, WEAK
 * ===== 一、解析xml配置文件: XMLConfigurationBuilder.parse()
 * -> properties: 配置属性值
 * -> settings: 全局设置, 如: cacheEnabled, useGeneratedKeys, mapUnderscoreToCamelCase等
 * -> typeAliases: 为Java类设置别名(typeAlias), 也可直接指定包名(package)
 * -> plugins: Interceptor, 如: PageHelper
 * === Interceptor允许你在已映射语句执行过程中的某一点进行拦截调用, 包括:
 * === 1).Executor(update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
 * === 2).ParameterHandler(getParameterObject, setParameters)
 * === 3).ResultSetHandler(handleResultSets, handleOutputParameters)
 * === 4).StatementHandler(prepare, parameterize, batch, update, query)
 * === 拦截标签: @Intercepts({@Signature(type= Executor.class, method = "update", args = {MappedStatement.class,Object.class})})
 * -> typeHandlers: 类型处理器, 实现`TypeHandler`接口或继承`BaseTypeHandler`
 * -> mappers: mapper, 可指定<mapper>(解析mapper资源)或<package>(解析Mapper接口)
 * ===== 二、解析mapper资源: XMLMapperBuilder.parse()
 * -> 对于mapper的解析: 不管是根据资源(xml)还是根据接口类, 最终都会互相调用对方的接口以加载完整的mapper接口类和资源文件
 * === 1).XMLMapperBuilder.parse(): 先加载并解析mapper.xml资源, 然后根据namespace解析mapper接口并调用configuration.addMapper()进行注册
 * === 2).Configuration.addMapper(): 注册mapper, 如果mapper.xml资源还没有注册到configuration, 则会在mapper接口类文件夹下查找mapper.xml文件并加载
 * -> 1.判断mapper资源是否已加载: 若未加载则解析mapper资源
 * -> 2.解析mapper子元素: cache, resultMap, sql, select, insert, ...
 * === 1).解析namespace: mapper接口的类全限定名
 * === 2).解析<cache-ref>: 使用其他mapper的缓存: <cache-ref namespace="com.someone.application.data.SomeMapper"/>
 * === 3).解析<cache>: 缓存配置
 * === 4).解析<parameterMap>: 参数
 * === 5).解析<resultMap>: 返回值
 * === 6).解析<sql>: sql引用
 * === 7).解析statement: select|insert|update|delete: XMLStatementBuilder.parseStatementNode()
 * ===== 三、缓存创建流程:
 * -> 1.缓存实现类的类名或别名: 默认为PerpetualCache
 * -> 2.根据类名或别名(type)解析出缓存实现类的class对象
 * -> 3.缓存回收策略的类名或别名: 默认为LRU, mybatis提供了四种回收策略
 * === 1).LRU – 最近最少使用的: 移除最长时间不被使用的对象
 * === 2).FIFO – 先进先出: 按对象进入缓存的顺序来移除它们
 * === 3).SOFT – 软引用: 移除基于垃圾回收器状态和软引用规则的对象
 * === 4).WEAK – 弱引用: 更积极地移除基于垃圾收集器状态和弱引用规则的对象
 * -> 4.根据回收策略类名或别名解析出驱逐策略实现类的对象: 采用了装饰器模式
 * -> 5.解析flushInterval: 刷新间隔(毫秒), 默认情况不设置刷新间隔, 缓存仅仅调用语句时刷新
 * -> 6.解析缓存条目数: 如LRUCache会设置LinkedHashMap的size为这个值, 超过后会进行驱逐
 * -> 7.解析其他属性值: Properties
 * -> 8.创建缓存: id为namespace
 * -> 9.将缓存注册到configuration: namespace -> cache
 * -> 10.设置当前缓存为新创建的缓存
 * ===== 四、将Resource(mapper.xml)和Mapper接口绑定:
 * -> 1.获取namespace, 并根据namespace解析mapper接口类
 * -> 2.如果configuration中还没有这个Mapper, 则添加到configuration
 * -> 3.将namespace标记为已加载, 并调用Configuration.addMapper()注册mapper接口类
 * ===== 五、解析mapper接口类:
 * -> 1.如果对应的resource尚未加载: 加载mapper.xml资源, 并调用XMLMapperBuilder.parse()解析
 * -> 2.标记resource为已加载
 * -> 3.解析Mapper接口类上的`CacheNamespace`注解: 缓存配置
 * -> 4.解析Mapper接口类上的`CacheNamespaceRef`注解: 缓存引用配置
 * -> 5.解析Mapper接口内的方法上的@Select、@Insert、@Update、@Delete注解: statement
 * ===== 六、Mapper代理类: MapperProxy.invoke():
 * -> 从`Method -> MapperMethod`缓存中取出MapperMethod
 * -> 调用`MapperMethod.execute()`执行SQL方法
 * ===== 七、MapperMethod.execute():
 * -> INSERT, UPDATE, DELETE: 返回影响的行数
 * -> SELECT: 返回查询结果
 * -> FLUSH: 执行批量查询
 */
public class MapperBuilderAnalyzer {

    // BaseBuilder成员变量
    protected Configuration configuration;
    protected TypeAliasRegistry typeAliasRegistry;
    protected TypeHandlerRegistry typeHandlerRegistry;

    private XPathParser parser;
    private MapperBuilderAssistant builderAssistant;
    private Map<String, XNode> sqlFragments;
    private String resource;

    /**
     * 根据`mapper.xml`解析Mapper
     * 对于mapper的解析: 不管是根据资源(xml)还是根据接口类, 最终都会互相调用对方的接口以加载完整的mapper接口类和资源文件
     * -> XMLMapperBuilder.parse(): 先加载并解析mapper.xml资源, 然后根据namespace解析mapper接口并调用configuration.addMapper()进行注册
     * -> Configuration.addMapper(): 注册mapper, 如果mapper.xml资源还没有注册到configuration, 则会在mapper接口类文件夹下查找mapper.xml文件并加载
     */
    public void parse() {
        // 如果configuration尚未加载这个mapper.xml资源, 则进行加载
        // resource: 资源(mapper.xml)路径
        // namespace: mapper接口的全限定名
        if (!configuration.isResourceLoaded(resource)) {
            // 解析mapper子元素: cache, resultMap, sql, select, insert, ...
            configurationElement(parser.evalNode("/mapper"));
            // 将mapper资源添加到已加载集合
            configuration.addLoadedResource(resource);
            // 绑定mapper资源和接口
            bindMapperForNamespace();
        }

        parsePendingResultMaps();
        parsePendingCacheRefs();
        parsePendingStatements();

        //解析在configurationElement函数中处理resultMap时其extends属性指向的父对象还没被处理的<resultMap>节点
        parsePendingResultMaps();
        //解析在configurationElement函数中处理cache-ref时其指向的对象不存在的<cache>节点(如果cache-ref先于其指向的cache节点加载就会出现这种情况)
        parsePendingCacheRefs();
        //同上，如果cache没加载的话处理statement时也会抛出异常
        parsePendingStatements();
    }

    /**
     * 解析mapper.xml资源
     */
    private void configurationElement(XNode context) {
        try {
            // 解析namespace属性
            String namespace = context.getStringAttribute("namespace");
            if (namespace == null || namespace.equals("")) {
                throw new BuilderException("Mapper's namespace cannot be empty");
            }
            // 设置当前namespace
            builderAssistant.setCurrentNamespace(namespace);
            // 解析<cache-ref>: 使用其他mapper的缓存
            cacheRefElement(context.evalNode("cache-ref"));
            // 解析<cache>: 缓存配置
            cacheElement(context.evalNode("cache"));
            // 解析<parameterMap>: 参数
            parameterMapElement(context.evalNodes("/mapper/parameterMap"));
            // 解析<resultMap>: 返回值
            resultMapElements(context.evalNodes("/mapper/resultMap"));
            // 解析<sql>: sql引用
            sqlElement(context.evalNodes("/mapper/sql"));
            // 解析statement: select|insert|update|delete
            buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing Mapper XML. Cause: " + e, e);
        }
    }

    /**
     * 解析statement
     */
    private void buildStatementFromContext(List<XNode> list) {
        if (configuration.getDatabaseId() != null) {
            buildStatementFromContext(list, configuration.getDatabaseId());
        }
        buildStatementFromContext(list, null);
    }

    /**
     * 解析statement
     */
    private void buildStatementFromContext(List<XNode> list, String requiredDatabaseId) {
        for (XNode context : list) {
            final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, context, requiredDatabaseId);
            try {
                // 使用XMLStatementBuilder解析statement
                statementParser.parseStatementNode();
            } catch (IncompleteElementException e) {
                configuration.addIncompleteStatement(statementParser);
            }
        }
    }

    /**
     * XMLStatementBuilder.parseStatementNode(): 解析statement: select|insert|update|delete
     */
    public void parseStatementNode() {
        String id = context.getStringAttribute("id");
        String databaseId = context.getStringAttribute("databaseId");

        if (!databaseIdMatchesCurrent(id, databaseId, this.requiredDatabaseId)) {
            return;
        }

        Integer fetchSize = context.getIntAttribute("fetchSize");
        Integer timeout = context.getIntAttribute("timeout");
        String parameterMap = context.getStringAttribute("parameterMap");
        String parameterType = context.getStringAttribute("parameterType");
        Class<?> parameterTypeClass = resolveClass(parameterType);
        String resultMap = context.getStringAttribute("resultMap");
        String resultType = context.getStringAttribute("resultType");
        String lang = context.getStringAttribute("lang");
        LanguageDriver langDriver = getLanguageDriver(lang);

        Class<?> resultTypeClass = resolveClass(resultType);
        String resultSetType = context.getStringAttribute("resultSetType");
        StatementType statementType = StatementType.valueOf(context.getStringAttribute("statementType", StatementType.PREPARED.toString()));
        ResultSetType resultSetTypeEnum = resolveResultSetType(resultSetType);

        String nodeName = context.getNode().getNodeName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        boolean flushCache = context.getBooleanAttribute("flushCache", !isSelect);
        boolean useCache = context.getBooleanAttribute("useCache", isSelect);
        boolean resultOrdered = context.getBooleanAttribute("resultOrdered", false);

        // Include Fragments before parsing
        XMLIncludeTransformer includeParser = new XMLIncludeTransformer(configuration, builderAssistant);
        includeParser.applyIncludes(context.getNode());

        // Parse selectKey after includes and remove them.
        processSelectKeyNodes(id, parameterTypeClass, langDriver);

        // Parse the SQL (pre: <selectKey> and <include> were parsed and removed)
        SqlSource sqlSource = langDriver.createSqlSource(configuration, context, parameterTypeClass);
        String resultSets = context.getStringAttribute("resultSets");
        String keyProperty = context.getStringAttribute("keyProperty");
        String keyColumn = context.getStringAttribute("keyColumn");
        KeyGenerator keyGenerator;
        String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);
        if (configuration.hasKeyGenerator(keyStatementId)) {
            keyGenerator = configuration.getKeyGenerator(keyStatementId);
        } else {
            keyGenerator = context.getBooleanAttribute("useGeneratedKeys",
                    configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType))
                    ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
        }

        builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType,
                fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClass,
                resultSetTypeEnum, flushCache, useCache, resultOrdered,
                keyGenerator, keyProperty, keyColumn, databaseId, langDriver, resultSets);
    }

    private void parsePendingResultMaps() {
        Collection<ResultMapResolver> incompleteResultMaps = configuration.getIncompleteResultMaps();
        synchronized (incompleteResultMaps) {
            Iterator<ResultMapResolver> iter = incompleteResultMaps.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().resolve();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // ResultMap is still missing a resource...
                }
            }
        }
    }

    private void parsePendingCacheRefs() {
        Collection<CacheRefResolver> incompleteCacheRefs = configuration.getIncompleteCacheRefs();
        synchronized (incompleteCacheRefs) {
            Iterator<CacheRefResolver> iter = incompleteCacheRefs.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().resolveCacheRef();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // Cache ref is still missing a resource...
                }
            }
        }
    }

    private void parsePendingStatements() {
        Collection<XMLStatementBuilder> incompleteStatements = configuration.getIncompleteStatements();
        synchronized (incompleteStatements) {
            Iterator<XMLStatementBuilder> iter = incompleteStatements.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().parseStatementNode();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // Statement is still missing a resource...
                }
            }
        }
    }

    /**
     * 解析<cache-ref>: 缓存引用
     * 示例: <cache-ref namespace="com.someone.application.data.SomeMapper"/>
     */
    private void cacheRefElement(XNode context) {
        if (context != null) {
            // 解析要使用的缓存的namespace
            configuration.addCacheRef(builderAssistant.getCurrentNamespace(), context.getStringAttribute("namespace"));
            CacheRefResolver cacheRefResolver = new CacheRefResolver(builderAssistant, context.getStringAttribute("namespace"));
            try {
                // 解析缓存引用: 实际会调用`MapperBuilderAssistant.useCacheRef()`
                // 由于一个cache只能被一个mapper的namespace绑定, 因此使用其他mapper的缓存需根据其namespace进行查找
                cacheRefResolver.resolveCacheRef();
            } catch (IncompleteElementException e) {
                configuration.addIncompleteCacheRef(cacheRefResolver);
            }
        }
    }

    /**
     * 根据namespace返回缓存: 使用其他mapper的缓存, 根据namespace查找, 若不为空则直接返回
     * 示例: <cache-ref namespace="com.someone.application.data.SomeMapper"/>, 可以在定义的Mapper内使用SomeMapper中定义的缓存
     */
    public Cache useCacheRef(String namespace) {
        if (namespace == null) {
            throw new BuilderException("cache-ref element requires a namespace attribute.");
        }
        try {
            unresolvedCacheRef = true;
            // 根据namespace查找缓存, 若不为则直接返回
            Cache cache = configuration.getCache(namespace);
            if (cache == null) {
                throw new IncompleteElementException("No cache for namespace '" + namespace + "' could be found.");
            }
            // 设置当前缓存为namespace对应mapper的缓存
            currentCache = cache;
            unresolvedCacheRef = false;
            return cache;
        } catch (IllegalArgumentException e) {
            throw new IncompleteElementException("No cache for namespace '" + namespace + "' could be found.", e);
        }
    }

    /**
     * 解析<cache>: 缓存
     * -> 1.缓存实现类的类名或别名: 默认为PerpetualCache
     * -> 2.根据类名或别名(type)解析出缓存实现类的class对象
     * -> 3.缓存回收策略的类名或别名: 默认为LRU, mybatis提供了四种回收策略
     * === 1).LRU – 最近最少使用的: 移除最长时间不被使用的对象
     * === 2).FIFO – 先进先出: 按对象进入缓存的顺序来移除它们
     * === 3).SOFT – 软引用: 移除基于垃圾回收器状态和软引用规则的对象
     * === 4).WEAK – 弱引用: 更积极地移除基于垃圾收集器状态和弱引用规则的对象
     * -> 4.根据回收策略类名或别名解析出驱逐策略实现类的对象: 采用了装饰器模式
     * -> 5.解析flushInterval: 刷新间隔(毫秒), 默认情况不设置刷新间隔, 缓存仅仅调用语句时刷新
     * -> 6.解析缓存条目数: 如LRUCache会设置LinkedHashMap的size为这个值, 超过后会进行驱逐
     * -> 7.解析其他属性值: Properties
     * -> 8.创建缓存: id为namespace
     * -> 9.将缓存注册到configuration: namespace -> cache
     * -> 10.设置当前缓存为新创建的缓存
     */
    private void cacheElement(XNode context) throws Exception {
        if (context != null) {
            // 缓存实现类的类名或别名: 默认为PerpetualCache
            String type = context.getStringAttribute("type", "PERPETUAL");
            // 根据类名或别名(type)解析出缓存实现类的class对象
            Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);
            /* 缓存回收策略的类名或别名: 默认为LRU, mybatis提供了四种回收策略
            -> LRU – 最近最少使用的: 移除最长时间不被使用的对象
            -> FIFO – 先进先出: 按对象进入缓存的顺序来移除它们
            -> SOFT – 软引用: 移除基于垃圾回收器状态和软引用规则的对象
            -> WEAK – 弱引用: 更积极地移除基于垃圾收集器状态和弱引用规则的对象
             */
            String eviction = context.getStringAttribute("eviction", "LRU");
            // 根据回收策略类名或别名解析出驱逐策略实现类的对象: 采用了装饰器模式
            Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);
            // 解析flushInterval: 刷新间隔(毫秒), 默认情况不设置刷新间隔, 缓存仅仅调用语句时刷新
            Long flushInterval = context.getLongAttribute("flushInterval");
            // 解析缓存条目数: 如LRUCache会设置LinkedHashMap的size为这个值, 超过后会进行驱逐
            Integer size = context.getIntAttribute("size");
            // 缓存是否只读
            boolean readWrite = !context.getBooleanAttribute("readOnly", false);
            boolean blocking = context.getBooleanAttribute("blocking", false);
            // 其他属性
            Properties props = context.getChildrenAsProperties();
            // 创建缓存
            builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
        }
    }

    /**
     * 根据配置创建缓存
     */
    public Cache useNewCache(Class<? extends Cache> typeClass,
                             Class<? extends Cache> evictionClass,
                             Long flushInterval,
                             Integer size,
                             boolean readWrite,
                             boolean blocking,
                             Properties props) {
        // 创建缓存: id为当前的namespace
        Cache cache = new CacheBuilder(currentNamespace)
                .implementation(valueOrDefault(typeClass, PerpetualCache.class))
                .addDecorator(valueOrDefault(evictionClass, LruCache.class))
                .clearInterval(flushInterval)
                .size(size)
                .readWrite(readWrite)
                .blocking(blocking)
                .properties(props)
                .build();
        // 将缓存注册到configuration: namespace -> cache
        configuration.addCache(cache);
        // 设置当前缓存为新创建的缓存
        currentCache = cache;
        return cache;
    }

    private void parameterMapElement(List<XNode> list) throws Exception {
        for (XNode parameterMapNode : list) {
            String id = parameterMapNode.getStringAttribute("id");
            String type = parameterMapNode.getStringAttribute("type");
            Class<?> parameterClass = resolveClass(type);
            List<XNode> parameterNodes = parameterMapNode.evalNodes("parameter");
            List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
            for (XNode parameterNode : parameterNodes) {
                String property = parameterNode.getStringAttribute("property");
                String javaType = parameterNode.getStringAttribute("javaType");
                String jdbcType = parameterNode.getStringAttribute("jdbcType");
                String resultMap = parameterNode.getStringAttribute("resultMap");
                String mode = parameterNode.getStringAttribute("mode");
                String typeHandler = parameterNode.getStringAttribute("typeHandler");
                Integer numericScale = parameterNode.getIntAttribute("numericScale");
                ParameterMode modeEnum = resolveParameterMode(mode);
                Class<?> javaTypeClass = resolveClass(javaType);
                JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
                @SuppressWarnings("unchecked")
                Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
                ParameterMapping parameterMapping = builderAssistant.buildParameterMapping(parameterClass, property, javaTypeClass, jdbcTypeEnum, resultMap, modeEnum, typeHandlerClass, numericScale);
                parameterMappings.add(parameterMapping);
            }
            builderAssistant.addParameterMap(id, parameterClass, parameterMappings);
        }
    }

    private void resultMapElements(List<XNode> list) throws Exception {
        for (XNode resultMapNode : list) {
            try {
                resultMapElement(resultMapNode);
            } catch (IncompleteElementException e) {
                // ignore, it will be retried
            }
        }
    }

    private ResultMap resultMapElement(XNode resultMapNode) throws Exception {
        return resultMapElement(resultMapNode, Collections.<ResultMapping> emptyList());
    }

    private ResultMap resultMapElement(XNode resultMapNode, List<ResultMapping> additionalResultMappings) throws Exception {
        ErrorContext.instance().activity("processing " + resultMapNode.getValueBasedIdentifier());
        String id = resultMapNode.getStringAttribute("id",
                resultMapNode.getValueBasedIdentifier());
        String type = resultMapNode.getStringAttribute("type",
                resultMapNode.getStringAttribute("ofType",
                        resultMapNode.getStringAttribute("resultType",
                                resultMapNode.getStringAttribute("javaType"))));
        String extend = resultMapNode.getStringAttribute("extends");
        Boolean autoMapping = resultMapNode.getBooleanAttribute("autoMapping");
        Class<?> typeClass = resolveClass(type);
        Discriminator discriminator = null;
        List<ResultMapping> resultMappings = new ArrayList<ResultMapping>();
        resultMappings.addAll(additionalResultMappings);
        List<XNode> resultChildren = resultMapNode.getChildren();
        for (XNode resultChild : resultChildren) {
            if ("constructor".equals(resultChild.getName())) {
                processConstructorElement(resultChild, typeClass, resultMappings);
            } else if ("discriminator".equals(resultChild.getName())) {
                discriminator = processDiscriminatorElement(resultChild, typeClass, resultMappings);
            } else {
                List<ResultFlag> flags = new ArrayList<ResultFlag>();
                if ("id".equals(resultChild.getName())) {
                    flags.add(ResultFlag.ID);
                }
                resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
            }
        }
        ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, extend, discriminator, resultMappings, autoMapping);
        try {
            return resultMapResolver.resolve();
        } catch (IncompleteElementException  e) {
            configuration.addIncompleteResultMap(resultMapResolver);
            throw e;
        }
    }

    private void processConstructorElement(XNode resultChild, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {
        List<XNode> argChildren = resultChild.getChildren();
        for (XNode argChild : argChildren) {
            List<ResultFlag> flags = new ArrayList<ResultFlag>();
            flags.add(ResultFlag.CONSTRUCTOR);
            if ("idArg".equals(argChild.getName())) {
                flags.add(ResultFlag.ID);
            }
            resultMappings.add(buildResultMappingFromContext(argChild, resultType, flags));
        }
    }

    private Discriminator processDiscriminatorElement(XNode context, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {
        String column = context.getStringAttribute("column");
        String javaType = context.getStringAttribute("javaType");
        String jdbcType = context.getStringAttribute("jdbcType");
        String typeHandler = context.getStringAttribute("typeHandler");
        Class<?> javaTypeClass = resolveClass(javaType);
        @SuppressWarnings("unchecked")
        Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
        Map<String, String> discriminatorMap = new HashMap<String, String>();
        for (XNode caseChild : context.getChildren()) {
            String value = caseChild.getStringAttribute("value");
            String resultMap = caseChild.getStringAttribute("resultMap", processNestedResultMappings(caseChild, resultMappings));
            discriminatorMap.put(value, resultMap);
        }
        return builderAssistant.buildDiscriminator(resultType, column, javaTypeClass, jdbcTypeEnum, typeHandlerClass, discriminatorMap);
    }

    private void sqlElement(List<XNode> list) throws Exception {
        if (configuration.getDatabaseId() != null) {
            sqlElement(list, configuration.getDatabaseId());
        }
        sqlElement(list, null);
    }

    private void sqlElement(List<XNode> list, String requiredDatabaseId) throws Exception {
        for (XNode context : list) {
            String databaseId = context.getStringAttribute("databaseId");
            String id = context.getStringAttribute("id");
            id = builderAssistant.applyCurrentNamespace(id, false);
            if (databaseIdMatchesCurrent(id, databaseId, requiredDatabaseId)) {
                sqlFragments.put(id, context);
            }
        }
    }

    private boolean databaseIdMatchesCurrent(String id, String databaseId, String requiredDatabaseId) {
        if (requiredDatabaseId != null) {
            if (!requiredDatabaseId.equals(databaseId)) {
                return false;
            }
        } else {
            if (databaseId != null) {
                return false;
            }
            // skip this fragment if there is a previous one with a not null databaseId
            if (this.sqlFragments.containsKey(id)) {
                XNode context = this.sqlFragments.get(id);
                if (context.getStringAttribute("databaseId") != null) {
                    return false;
                }
            }
        }
        return true;
    }

    private ResultMapping buildResultMappingFromContext(XNode context, Class<?> resultType, List<ResultFlag> flags) throws Exception {
        String property;
        if (flags.contains(ResultFlag.CONSTRUCTOR)) {
            property = context.getStringAttribute("name");
        } else {
            property = context.getStringAttribute("property");
        }
        String column = context.getStringAttribute("column");
        String javaType = context.getStringAttribute("javaType");
        String jdbcType = context.getStringAttribute("jdbcType");
        String nestedSelect = context.getStringAttribute("select");
        String nestedResultMap = context.getStringAttribute("resultMap",
                processNestedResultMappings(context, Collections.<ResultMapping> emptyList()));
        String notNullColumn = context.getStringAttribute("notNullColumn");
        String columnPrefix = context.getStringAttribute("columnPrefix");
        String typeHandler = context.getStringAttribute("typeHandler");
        String resultSet = context.getStringAttribute("resultSet");
        String foreignColumn = context.getStringAttribute("foreignColumn");
        boolean lazy = "lazy".equals(context.getStringAttribute("fetchType", configuration.isLazyLoadingEnabled() ? "lazy" : "eager"));
        Class<?> javaTypeClass = resolveClass(javaType);
        @SuppressWarnings("unchecked")
        Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
        return builderAssistant.buildResultMapping(resultType, property, column, javaTypeClass, jdbcTypeEnum, nestedSelect, nestedResultMap, notNullColumn, columnPrefix, typeHandlerClass, flags, resultSet, foreignColumn, lazy);
    }

    private String processNestedResultMappings(XNode context, List<ResultMapping> resultMappings) throws Exception {
        if ("association".equals(context.getName())
                || "collection".equals(context.getName())
                || "case".equals(context.getName())) {
            if (context.getStringAttribute("select") == null) {
                ResultMap resultMap = resultMapElement(context, resultMappings);
                return resultMap.getId();
            }
        }
        return null;
    }

    /**
     * 将Resource(mapper.xml)和Mapper接口绑定:
     * -> 1.获取namespace, 并根据namespace解析mapper接口类
     * -> 2.如果configuration中还没有这个Mapper, 则添加到configuration
     * -> 3.将namespace标记为已加载, 并调用Configuration.addMapper()注册mapper接口类
     */
    private void bindMapperForNamespace() {
        // 获取namespace: 即Mapper接口的类全限定名
        String namespace = builderAssistant.getCurrentNamespace();
        if (namespace != null) {
            Class<?> boundType = null;
            // 根据类名找到class对象
            try {
                boundType = Resources.classForName(namespace);
            } catch (ClassNotFoundException e) {
                //ignore, bound type is not required
            }
            // 将Mapper接口添加到configuration
            if (boundType != null) {
                // 如果configuration中还没有这个Mapper, 则添加到configuration
                // 使用Spring加载mybatis配置时: 可能会多次调用绑定接口, 因此这个进行判断, 只有在尚未绑定的情况才允许绑定
                if (!configuration.hasMapper(boundType)) {
                    configuration.addLoadedResource("namespace:" + namespace);
                    // 将mapper接口加入configuration: 实际会调用`MapperRegistry.addMapper()`
                    configuration.addMapper(boundType);
                }
            }
        }
    }

    /**
     * MapperRegistry.addMapper: 注册mapper
     */
    public <T> void addMapper(Class<T> type) {
        if (type.isInterface()) {
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            boolean loadCompleted = false;
            try {
                // 将Mapper接口注册到knownMappers中: 使用MapperProxyFactory创建代理对象MapperProxy
                knownMappers.put(type, new MapperProxyFactory<T>(type));
                // It's important that the type is added before the parser is run
                // otherwise the binding may automatically be attempted by the
                // mapper parser. If the type is already known, it won't try.
                MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
                parser.parse();
                loadCompleted = true;
            } finally {
                // 若解析mapper失败, 则从knownMappers中移除Mapper接口
                if (!loadCompleted) {
                    knownMappers.remove(type);
                }
            }
        }
    }

    /**
     * Mapper代理类: MapperProxy.invoke():
     * -> 从`Method -> MapperMethod`缓存中取出MapperMethod
     * -> 调用`MapperMethod.execute()`执行SQL方法
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            } else if (isDefaultMethod(method)) {
                return invokeDefaultMethod(proxy, method, args);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
        // 从`Method -> MapperMethod`缓存中取出MapperMethod
        final MapperMethod mapperMethod = cachedMapperMethod(method);
        // 调用`MapperMethod.execute()`执行SQL方法
        return mapperMethod.execute(sqlSession, args);
    }

    /**
     * MapperMethod.execute():
     * -> INSERT, UPDATE, DELETE: 返回影响的行数
     * -> SELECT: 返回查询结果
     * -> FLUSH: 执行批量查询
     */
    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result;
        switch (command.getType()) {
            // 如果是
            case INSERT: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.insert(command.getName(), param));
                break;
            }
            case UPDATE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.update(command.getName(), param));
                break;
            }
            case DELETE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.delete(command.getName(), param));
                break;
            }
            case SELECT:
                if (method.returnsVoid() && method.hasResultHandler()) {
                    executeWithResultHandler(sqlSession, args);
                    result = null;
                } else if (method.returnsMany()) {
                    result = executeForMany(sqlSession, args);
                } else if (method.returnsMap()) {
                    result = executeForMap(sqlSession, args);
                } else if (method.returnsCursor()) {
                    result = executeForCursor(sqlSession, args);
                } else {
                    Object param = method.convertArgsToSqlCommandParam(args);
                    result = sqlSession.selectOne(command.getName(), param);
                }
                break;
            case FLUSH:
                result = sqlSession.flushStatements();
                break;
            default:
                throw new BindingException("Unknown execution method for: " + command.getName());
        }
        if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
            throw new BindingException("Mapper method '" + command.getName()
                    + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
        }
        return result;
    }

    /**
     * 解析mapper接口类:
     * -> 1.如果对应的resource尚未加载: 加载mapper.xml资源, 并调用XMLMapperBuilder.parse()解析
     * -> 2.标记resource为已加载
     * -> 3.解析Mapper接口类上的`CacheNamespace`注解: 缓存配置
     * -> 4.解析Mapper接口类上的`CacheNamespaceRef`注解: 缓存引用配置
     * -> 5.解析Mapper接口内的方法上的@Select、@Insert、@Update、@Delete注解: statement
     */
    public void parse() {
        String resource = type.toString();
        if (!configuration.isResourceLoaded(resource)) {
            loadXmlResource();
            configuration.addLoadedResource(resource);
            assistant.setCurrentNamespace(type.getName());
            parseCache();
            parseCacheRef();
            Method[] methods = type.getMethods();
            for (Method method : methods) {
                try {
                    // 解析方法上的statement: 使用@Select、@Update等注解定义的statement
                    if (!method.isBridge()) {
                        parseStatement(method);
                    }
                } catch (IncompleteElementException e) {
                    configuration.addIncompleteMethod(new MethodResolver(this, method));
                }
            }
        }
        parsePendingMethods();
    }

    private void loadXmlResource() {
        // Spring may not know the real resource name so we check a flag
        // to prevent loading again a resource twice
        // this flag is set at XMLMapperBuilder#bindMapperForNamespace
        if (!configuration.isResourceLoaded("namespace:" + type.getName())) {
            String xmlResource = type.getName().replace('.', '/') + ".xml";
            InputStream inputStream = null;
            try {
                inputStream = Resources.getResourceAsStream(type.getClassLoader(), xmlResource);
            } catch (IOException e) {
                // ignore, resource is not required
            }
            if (inputStream != null) {
                XMLMapperBuilder xmlParser = new XMLMapperBuilder(inputStream, assistant.getConfiguration(), xmlResource, configuration.getSqlFragments(), type.getName());
                xmlParser.parse();
            }
        }
    }

}
