package com.demos.java.jdkanalyzer.mybatis;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.*;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.*;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/9
 * ===== 一、MyBatis基本用法:
 * -> 1.使用SqlSessionFactoryBuilder创建SqlSessionFactory
 * -> 2.从SqlSessionFactory中获取SqlSession
 * -> 3.从SqlSession中获取Mapper类
 * ===== 二、创建SqlSession:
 * -> 1.创建Transaction, 核心成员变量:
 * === 1).Connection: dataSource.getConnection()
 * === 2).DataSource: 数据源
 * === 3).TransactionIsolationLevel: 隔离级别
 * === 4).boolean autoCommit: 是否自动提交
 * -> 2.创建SQL执行器
 * -> 3.创建并返回DefaultSqlSession
 * ===== 三、创建SQL执行器:
 * -> 1.根据type创建执行器(BaseExecutor): BatchExecutor, ReuseExecutor or SimpleExecutor
 * -> 2.如果开启了二级缓存: 使用CachingExecutor包装BaseExecutor
 * -> 3.应用Interceptor.plugin()方法
 * ===== 四、BaseExecutor.query(): 涉及一级缓存的查询: localCacheScope = SESSION|STATEMENT
 * -> 1.如果需要清理缓存(一级缓存)则进行清理(直接调用clear: PerpetualCache: HashMap)
 * -> 2.从本地缓存(一级缓存)中查询结果
 * -> 3.如果缓存中不存在则从数据库进行查找, 同时将结果设置到缓存
 * -> 4.如果localCacheScope == STATEMENT, 则清理缓存
 * ===== 五、CachingExecutor.query(): 涉及二级缓存的查询: 同时需选择缓存驱逐策略: 装饰器模式(LRU, LIFO, SOFT, WEAK)
 * -> 1.构造缓存的key
 * -> 2.尝试获取MappedStatement中的Cache: CachingExecutor使用TransactionalCacheManager来管理缓存, 实际也是代理了MappedStatement中的Cache
 * -> 3.若Cache为空则直接使用代理的SQL执行器(Executor)执行查询(BaseExecutor: SimpleExecutor, ReuseExecutor, BatchExecutor)
 * -> 4.若Cache不为空: 如果当前statement需要清理缓存(flushCache="true")则清理掉Cache对应的缓存
 * -> 5.如果可以使用二级缓存(useCache="true")则尝试从缓存中查找
 * -> 6.如果缓存不存在则调用代理执行器执行查询, 并将结果设置到二级缓存
 */
public class ExecutorAnalyzer {

    /**
     * MyBatis基本用法:
     */
    public void basicApply() {
        InputStream inputStream = getClass().getResourceAsStream("/mybatis-config.xml");
        // 1.创建SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        // 2.创建SqlSession
        SqlSession session = sqlSessionFactory.openSession();
        try {
            // 3.从SqlSession中获取Mapper类
            session.getMapper(null);
//            UserMapper mapper = session.getMapper(UserMapper.class);
//            int count = mapper.selectCount();
        } finally {
            session.close();
        }
    }

    /**
     * SqlSessionFactoryBuilder.build(): 创建SqlSessionFactory
     * XMLConfigBuilder.build()创建mapper
     */
    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        SqlSessionFactory var5;
        try {
            XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
            var5 = this.build(parser.parse());
        } catch (Exception var14) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", var14);
        } finally {
            ErrorContext.instance().reset();
            try {
                inputStream.close();
            } catch (IOException var13) {
                ;
            }
        }
        return var5;
    }

    /**
     * 创建SqlSessionFactory
     */
    public SqlSessionFactory build(Configuration config) {
        return new DefaultSqlSessionFactory(config);
    }

    /**
     * 创建SqlSession:
     * -> 1.创建Transaction, 核心成员变量:
     * === 1).Connection: dataSource.getConnection()
     * === 2).DataSource: 数据源
     * === 3).TransactionIsolationLevel: 隔离级别
     * === 4).boolean autoCommmit: 是否自动提交
     * -> 2.创建SQL执行器
     * -> 3.创建并返回DefaultSqlSession
     */
    private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
        Transaction tx = null;

        DefaultSqlSession var8;
        try {
            Environment environment = this.configuration.getEnvironment();
            TransactionFactory transactionFactory = this.getTransactionFactoryFromEnvironment(environment);
            /* 创建Transaction, 核心成员:
            -> Connection: dataSource.getConnection()
            -> DataSource: 数据源
            -> TransactionIsolationLevel: 隔离级别
            -> boolean autoCommmit: 是否自动提交
            */
            tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
            // 核心: 创建SQL执行器
            Executor executor = newExecutor(tx, execType);
//            Executor executor = this.configuration.newExecutor(tx, execType);
            // 创建SqlSession: DefaultSqlSession
            var8 = new DefaultSqlSession(this.configuration, executor, autoCommit);
        } catch (Exception var12) {
            this.closeTransaction(tx);
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + var12, var12);
        } finally {
            ErrorContext.instance().reset();
        }

        return var8;
    }

    /**
     * 创建SQL执行器:
     * -> 1.根据type创建执行器(BaseExecutor): BatchExecutor, ReuseExecutor or SimpleExecutor
     * -> 2.如果开启了二级缓存: 使用CachingExecutor包装BaseExecutor
     * -> 3.应用Interceptor.plugin()方法
     */
    public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
        executorType = executorType == null ? this.defaultExecutorType : executorType;
        executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
        Object executor;
        if (ExecutorType.BATCH == executorType) {
            // BatchExecutor: 批量执行
            // 最终调用Statement.addBatch()和executeBatch()接口进行提交
            executor = new BatchExecutor(this, transaction);
        } else if (ExecutorType.REUSE == executorType) {
            // ReuseExecutor: 执行完SQL直接返回, 不调用Statement.close()
            executor = new ReuseExecutor(this, transaction);
        } else {
            // SimpleExecutor: 每次执行完SQL调用Statement.close()
            executor = new SimpleExecutor(this, transaction);
        }

        // 如果开启二级缓存: 使用CachingExecutor包装BaseExecutor
        if (this.cacheEnabled) {
            executor = new CachingExecutor((Executor)executor);
        }

        // 应用Interceptor.plugin()方法
        Executor executor = (Executor)this.interceptorChain.pluginAll(executor);
        return executor;
    }

    /**
     * BaseExecutor.query(): 涉及一级缓存的查询: localCacheScope = SESSION|STATEMENT
     * -> 1.如果需要清理缓存(一级缓存)则进行清理(直接调用clear: PerpetualCache: HashMap)
     * -> 2.从本地缓存(一级缓存)中查询结果
     * -> 3.如果缓存中不存在则从数据库进行查找, 同时将结果设置到缓存
     * -> 4.如果localCacheScope == STATEMENT, 则清理缓存
     */
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        // 如果需要清理缓存(localCacheScope设置为`STATEMENT`, 默认为`SESSION`), 同时需要保证queryStack=0以处理递归查询
        if (queryStack == 0 && ms.isFlushCacheRequired()) {
            clearLocalCache();
        }
        List<E> list;
        try {
            queryStack++;
            list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
            if (list != null) {
                handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
            } else {
                // 如果缓存中不存在则从数据库进行查找, 同时将结果设置到缓存
                list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
            }
        } finally {
            queryStack--;
        }
        if (queryStack == 0) {
            for (BaseExecutor.DeferredLoad deferredLoad : deferredLoads) {
                deferredLoad.load();
            }
            // issue #601
            deferredLoads.clear();
            // 如果localCacheScope == STATEMENT, 则清理调缓存
            if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
                // issue #482
                clearLocalCache();
            }
        }
        return list;
    }

    /**
     * CachingExecutor.query(): 涉及二级缓存的查询: 同时需选择缓存驱逐策略: 装饰器模式(LRU, LIFO, SOFT, WEAK)
     * -> 1.构造缓存的key
     * -> 2.尝试获取MappedStatement中的Cache: CachingExecutor使用TransactionalCacheManager来管理缓存, 实际也是代理了MappedStatement中的Cache
     * -> 3.若Cache为空则直接使用代理的SQL执行器(Executor)执行查询(BaseExecutor: SimpleExecutor, ReuseExecutor, BatchExecutor)
     * -> 4.若Cache不为空: 如果当前statement需要清理缓存(flushCache="true")则清理掉Cache对应的缓存
     * -> 5.如果可以使用二级缓存(useCache="true")则尝试从缓存中查找
     * -> 6.如果缓存不存在则调用代理执行器执行查询, 并将结果设置到二级缓存
     */
    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        // 构造缓存的key
        CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
        return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
    }

    /**
     * CachingExecutor.query()
     */
    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
            throws SQLException {
        Cache cache = ms.getCache();
        // 当前statement中配置了Cache则尝试从Cache查找
        if (cache != null) {
            // 如果执行当前statement需要清理缓存(flushCache="true")则清理掉Cache对应的缓存
            flushCacheIfRequired(ms);
            // 如果当前statement可以使用二级缓存(useCache="true")则尝试从缓存中查找
            if (ms.isUseCache() && resultHandler == null) {
                ensureNoOutParams(ms, boundSql);
                // 从缓存中进行查询
                List<E> list = (List<E>) tcm.getObject(cache, key);
                // 如果缓存中不存在则使用代理查询结果, 并更新到缓存
                if (list == null) {
                    list = delegate.<E> query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
                    // 设置缓存
                    tcm.putObject(cache, key, list); // issue #578 and #116
                }
                return list;
            }
        }
        // 否则直接使用代理的SQL执行器(Executor)执行查询
        return delegate.<E> query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
    }

}
