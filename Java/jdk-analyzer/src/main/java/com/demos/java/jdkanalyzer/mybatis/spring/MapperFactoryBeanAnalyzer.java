package com.demos.java.jdkanalyzer.mybatis.spring;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionHolder;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.SqlSessionUtils;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.springframework.util.Assert.notNull;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/11
 *
 * ===== MapperFactoryBean解析: 创建Mapper
 * -> 继承关系 -> SqlSessionDaoSupport -> DaoSupport -> InitializingBean, 在`afterPropertiesSet()`方法中将`Mapper`添加到`Configuration`
 * -> FactoryBean.getObject: 使用FactoryBean创建singleton, 返回Mapper(mapperInterface对象)
 * === 检查`Mapper`创建所需属性, 并将Mapper添加到Configuration:
 * -> 1.检查'sqlSessionFactory'或'sqlSessionTemplate'是否存在: 如果不存在则无法创建SqlSession
 * -> 2.检查要代理的Mapper接口类是否存在
 * -> 3.从SqlSession中取出Configuration, 并添加Mapper接口(Configuration.addMapper
 * === SqlSessionTemplate: 代理SqlSession, 内置SqlSessionInterceptor: 在执行方法后手动调用SqlSession.commit()
 */
public class MapperFactoryBeanAnalyzer<T> implements FactoryBean<T> {

    private SqlSessionTemplate sqlSessionTemplate;

    private Class<T> mapperInterface;

    private boolean addToConfig = true;

    /**
     * 设置`SqlSessionFactory`属性: 用来创建`SqlSessionTemplate`
     */
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        if (this.sqlSessionTemplate == null || sqlSessionFactory != this.sqlSessionTemplate.getSqlSessionFactory()) {
            this.sqlSessionTemplate = createSqlSessionTemplate(sqlSessionFactory);
        }
    }

    /**
     * 创建`SqlSessionTemplate`
     */
    protected SqlSessionTemplate createSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * 直接注入`SqlSessionTemplate`
     */
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    /**
     * 返回sqlSessionTemplate, 用于`getMapper()`
     */
    public SqlSession getSqlSession() {
        return this.sqlSessionTemplate;
    }

    /**
     * 初始化方法: 将`Mapper`添加到`Configuration`
     */
    public final void afterPropertiesSet() throws IllegalArgumentException, BeanInitializationException {
        this.checkDaoConfig();
    }

    /**
     * 检查`Mapper`创建所需属性, 并将Mapper添加到Configuration:
     * -> 1.检查'sqlSessionFactory'或'sqlSessionTemplate'是否存在: 如果不存在则无法创建SqlSession
     * -> 2.检查要代理的Mapper接口类是否存在
     * -> 3.从SqlSession中取出Configuration, 并添加Mapper接口(Configuration.addMapper)
     */
    protected void checkDaoConfig() {
        // 必须注入'sqlSessionFactory'或'sqlSessionTemplate'
        notNull(this.sqlSessionTemplate, "Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required");
        // mapperInterface必须存在: 以便添加对应的Mapper
        notNull(this.mapperInterface, "Property 'mapperInterface' is required");
        // 从SqlSession中取出Configuration, 以便添加Mapper
        Configuration configuration = getSqlSession().getConfiguration();
        if (this.addToConfig && !configuration.hasMapper(this.mapperInterface)) {
            try {
                // 将Mapper添加到Configuration
                configuration.addMapper(this.mapperInterface);
            } catch (Exception e) {
                logger.error("Error while adding the mapper '" + this.mapperInterface + "' to configuration.", e);
                throw new IllegalArgumentException(e);
            } finally {
                ErrorContext.instance().reset();
            }
        }
    }

    @Override
    public T getObject() throws Exception {
        return getSqlSession().getMapper(this.mapperInterface);
    }

    @Override
    public Class<T> getObjectType() {
        return this.mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /* ------------------------------ SqlSessionTemplate ------------------------------ */

    private final SqlSessionFactory sqlSessionFactory;
    private final ExecutorType executorType;
    private final SqlSession sqlSessionProxy;
    private final PersistenceExceptionTranslator exceptionTranslator;

    /**
     * Constructs a Spring managed {@code SqlSession} with the given
     * {@code SqlSessionFactory} and {@code ExecutorType}.
     * A custom {@code SQLExceptionTranslator} can be provided as an
     * argument so any {@code PersistenceException} thrown by MyBatis
     * can be custom translated to a {@code RuntimeException}
     * The {@code SQLExceptionTranslator} can also be null and thus no
     * exception translation will be done and MyBatis exceptions will be
     * thrown
     *
     * @param sqlSessionFactory a factory of SqlSession
     * @param executorType an executor type on session
     * @param exceptionTranslator a translator of exception
     */
    public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
                              PersistenceExceptionTranslator exceptionTranslator) {

        notNull(sqlSessionFactory, "Property 'sqlSessionFactory' is required");
        notNull(executorType, "Property 'executorType' is required");

        this.sqlSessionFactory = sqlSessionFactory;
        this.executorType = executorType;
        this.exceptionTranslator = exceptionTranslator;
        this.sqlSessionProxy = (SqlSession) newProxyInstance(
                SqlSessionFactory.class.getClassLoader(),
                new Class[] { SqlSession.class },
                new SqlSessionInterceptor());
    }

    /**
     * SqlSessionTemplate代理Mybatis的Mapper的增强: 在执行方法后手动调用SqlSession.commit()
     */
    private class SqlSessionInterceptor implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            SqlSession sqlSession = getSqlSession(
                    sqlSessionFactory,
                    executorType,
                    exceptionTranslator);
            try {
                Object result = method.invoke(sqlSession, args);
                if (!isSqlSessionTransactional(sqlSession, sqlSessionFactory)) {
                    // force commit even on non-dirty sessions because some databases require
                    // a commit/rollback before calling close()
                    sqlSession.commit(true);
                }
                return result;
            } catch (Throwable t) {
                Throwable unwrapped = unwrapThrowable(t);
                if (exceptionTranslator != null && unwrapped instanceof PersistenceException) {
                    // release the connection to avoid a deadlock if the translator is no loaded. See issue #22
                    closeSqlSession(sqlSession, sqlSessionFactory);
                    sqlSession = null;
                    Throwable translated = exceptionTranslator.translateExceptionIfPossible((PersistenceException) unwrapped);
                    if (translated != null) {
                        unwrapped = translated;
                    }
                }
                throw unwrapped;
            } finally {
                if (sqlSession != null) {
                    closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        }
    }

    /**
     * Gets an SqlSession from Spring Transaction Manager or creates a new one if needed.
     * Tries to get a SqlSession out of current transaction. If there is not any, it creates a new one.
     * Then, it synchronizes the SqlSession with the transaction if Spring TX is active and
     * <code>SpringManagedTransactionFactory</code> is configured as a transaction manager.
     *
     * @param sessionFactory a MyBatis {@code SqlSessionFactory} to create new sessions
     * @param executorType The executor type of the SqlSession to create
     * @param exceptionTranslator Optional. Translates SqlSession.commit() exceptions to Spring exceptions.
     * @return an SqlSession managed by Spring Transaction Manager
     * @throws TransientDataAccessResourceException if a transaction is active and the
     *             {@code SqlSessionFactory} is not using a {@code SpringManagedTransactionFactory}
     * @see SpringManagedTransactionFactory
     */
    public static SqlSession getSqlSession(SqlSessionFactory sessionFactory, ExecutorType executorType, PersistenceExceptionTranslator exceptionTranslator) {

        notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);
        notNull(executorType, NO_EXECUTOR_TYPE_SPECIFIED);

        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);

        SqlSession session = sessionHolder(executorType, holder);
        if (session != null) {
            return session;
        }

        LOGGER.debug(() -> "Creating a new SqlSession");
        session = sessionFactory.openSession(executorType);

        registerSessionHolder(sessionFactory, executorType, exceptionTranslator, session);

        return session;
    }

    private static SqlSession sessionHolder(ExecutorType executorType, SqlSessionHolder holder) {
        SqlSession session = null;
        if (holder != null && holder.isSynchronizedWithTransaction()) {
            if (holder.getExecutorType() != executorType) {
                throw new TransientDataAccessResourceException("Cannot change the ExecutorType when there is an existing transaction");
            }

            holder.requested();

            LOGGER.debug(() -> "Fetched SqlSession [" + holder.getSqlSession() + "] from current transaction");
            session = holder.getSqlSession();
        }
        return session;
    }

    /**
     * Register session holder if synchronization is active (i.e. a Spring TX is active).
     *
     * Note: The DataSource used by the Environment should be synchronized with the
     * transaction either through DataSourceTxMgr or another tx synchronization.
     * Further assume that if an exception is thrown, whatever started the transaction will
     * handle closing / rolling back the Connection associated with the SqlSession.
     *
     * @param sessionFactory sqlSessionFactory used for registration.
     * @param executorType executorType used for registration.
     * @param exceptionTranslator persistenceExceptionTranslator used for registration.
     * @param session sqlSession used for registration.
     */
    private static void registerSessionHolder(SqlSessionFactory sessionFactory, ExecutorType executorType,
                                              PersistenceExceptionTranslator exceptionTranslator, SqlSession session) {
        SqlSessionHolder holder;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            Environment environment = sessionFactory.getConfiguration().getEnvironment();

            if (environment.getTransactionFactory() instanceof SpringManagedTransactionFactory) {
                LOGGER.debug(() -> "Registering transaction synchronization for SqlSession [" + session + "]");

                holder = new SqlSessionHolder(session, executorType, exceptionTranslator);
                TransactionSynchronizationManager.bindResource(sessionFactory, holder);
                TransactionSynchronizationManager.registerSynchronization(new SqlSessionUtils.SqlSessionSynchronization(holder, sessionFactory));
                holder.setSynchronizedWithTransaction(true);
                holder.requested();
            } else {
                if (TransactionSynchronizationManager.getResource(environment.getDataSource()) == null) {
                    LOGGER.debug(() -> "SqlSession [" + session + "] was not registered for synchronization because DataSource is not transactional");
                } else {
                    throw new TransientDataAccessResourceException(
                            "SqlSessionFactory must be using a SpringManagedTransactionFactory in order to use Spring transaction synchronization");
                }
            }
        } else {
            LOGGER.debug(() -> "SqlSession [" + session + "] was not registered for synchronization because synchronization is not active");
        }

    }

    /**
     * Returns if the {@code SqlSession} passed as an argument is being managed by Spring
     *
     * @param session a MyBatis SqlSession to check
     * @param sessionFactory the SqlSessionFactory which the SqlSession was built with
     * @return true if session is transactional, otherwise false
     */
    public static boolean isSqlSessionTransactional(SqlSession session, SqlSessionFactory sessionFactory) {
        notNull(session, NO_SQL_SESSION_SPECIFIED);
        notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);

        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);

        return (holder != null) && (holder.getSqlSession() == session);
    }

    /**
     * Checks if {@code SqlSession} passed as an argument is managed by Spring {@code TransactionSynchronizationManager}
     * If it is not, it closes it, otherwise it just updates the reference counter and
     * lets Spring call the close callback when the managed transaction ends
     *
     * @param session a target SqlSession
     * @param sessionFactory a factory of SqlSession
     */
    public static void closeSqlSession(SqlSession session, SqlSessionFactory sessionFactory) {
        notNull(session, NO_SQL_SESSION_SPECIFIED);
        notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);

        SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        if ((holder != null) && (holder.getSqlSession() == session)) {
            LOGGER.debug(() -> "Releasing transactional SqlSession [" + session + "]");
            holder.released();
        } else {
            LOGGER.debug(() -> "Closing non transactional SqlSession [" + session + "]");
            session.close();
        }
    }

}
