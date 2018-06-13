package com.demos.java.jdkanalyzer.mybatis.spring;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Assert.state;
import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.hasLength;
import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/11
 *
 * ===== SqlSessionFactoryBean解析: 创建SqlSessionFactory
 * -> InitializingBean.afterPropertiesSet(): 初始化方法, 调用buildSqlSessionFactory()解析出SqlSessionFactory
 * -> FactoryBean.getObject: 使用FactoryBean创建singleton, 返回sqlSessionFactory
 * ===== 1).常用属性:
 * -> configLocation: `mybatis-config.xml`配置文件资源
 * -> mapperLocations: mapper.xml文件资源: 支持模式匹配
 * -> dataSource: 数据源
 * ===== 2).Configuration配置属性: SqlSessionFactoryBean既可使用configLocation(mybatis-config.xml)创建Configuration, 也可直接注入属性设置Configuration, 如:
 * -> typeAliasesPackage: 要扫描的别名包路径, 可用",; \t\n"分隔符分隔
 * -> typeHandlersPackage: 要扫描的类型转换器包路径, 可用",; \t\n"分隔符分隔
 * -> plugins[]: Interceptor列表
 * ===== 3).创建SqlSessionFactory流程:
 * -> 1.检查`configuration`(注入Configuration), `configLocation`(使用mybatis-config.xml), 或默认方式(创建空的Configuration对象)创建Configuration
 * -> 2.解析`typeAliasesPackage`, `typeHandlersPackage`等属性设置Configuration
 * -> 3.如果xmlConfigBuilder不为空, 调用`xmlConfigBuilder.parse()`初始化configuration
 * === 由于在第1步通过`configLocation`创建Configuration时只把新创建的空Configuration引用设置到了当前的configuration, 并没有初始化
 * -> 4.解析`mapperLocations`: 遍历mapperLocations并创建XMLMapperBuilder解析mapper资源
 * -> 5.使用初始化好的configuration创建`DefaultSqlSessionFactory`并返回
 */
public class SqlSessionFactoryBeanAnalyzer implements FactoryBean<SqlSessionFactory>, InitializingBean, ApplicationListener<ApplicationEvent> {

    // `mybatis-config.xml`配置文件资源
    private Resource configLocation;
    // mybatis的Configuration
    private Configuration configuration;
    // mapper.xml文件资源: 支持模式匹配, 如: 'classpath*:/mappers/**/*.xml'
    private Resource[] mapperLocations;
    // 数据源
    private DataSource dataSource;

    private TransactionFactory transactionFactory;

    private Properties configurationProperties;

    private SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();

    private SqlSessionFactory sqlSessionFactory;

    //EnvironmentAware requires spring 3.1
    private String environment = SqlSessionFactoryBean.class.getSimpleName();

    private boolean failFast;

    private Interceptor[] plugins;

    private TypeHandler<?>[] typeHandlers;

    private String typeHandlersPackage;

    private Class<?>[] typeAliases;

    private String typeAliasesPackage;

    private Class<?> typeAliasesSuperType;

    //issue #19. No default provider.
    private DatabaseIdProvider databaseIdProvider;

    private Class<? extends VFS> vfs;

    private Cache cache;

    private ObjectFactory objectFactory;

    private ObjectWrapperFactory objectWrapperFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public SqlSessionFactory getObject() throws Exception {
        if (this.sqlSessionFactory == null) {
            afterPropertiesSet();
        }

        return this.sqlSessionFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends SqlSessionFactory> getObjectType() {
        return this.sqlSessionFactory == null ? SqlSessionFactory.class : this.sqlSessionFactory.getClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (failFast && event instanceof ContextRefreshedEvent) {
            // fail-fast -> check all statements are completed
            this.sqlSessionFactory.getConfiguration().getMappedStatementNames();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(dataSource, "Property 'dataSource' is required");
        notNull(sqlSessionFactoryBuilder, "Property 'sqlSessionFactoryBuilder' is required");
        state((configuration == null && configLocation == null) || !(configuration != null && configLocation != null),
                "Property 'configuration' and 'configLocation' can not specified with together");

        this.sqlSessionFactory = buildSqlSessionFactory();
    }

    /**
     * 创建SqlSessionFactory:
     * -> 1.检查`configuration`(注入Configuration), `configLocation`(使用mybatis-config.xml), 或默认方式(创建空的Configuration对象)创建Configuration
     * -> 2.解析`typeAliasesPackage`, `typeHandlersPackage`等属性设置Configuration
     * -> 3.如果xmlConfigBuilder不为空, 调用`xmlConfigBuilder.parse()`初始化configuration
     * === 由于在第1步通过`configLocation`创建Configuration时只把新创建的空Configuration引用设置到了当前的configuration, 并没有初始化
     * -> 4.解析`mapperLocations`解析mapper.xml
     * -> 5.使用初始化好的configuration创建`DefaultSqlSessionFactory`并返回
     */
    protected SqlSessionFactory buildSqlSessionFactory() throws IOException {

        Configuration configuration;

        XMLConfigBuilder xmlConfigBuilder = null;
        // 如果直接注入了Configuration则使用注入的Configuration
        if (this.configuration != null) {
            configuration = this.configuration;
            if (configuration.getVariables() == null) {
                configuration.setVariables(this.configurationProperties);
            } else if (this.configurationProperties != null) {
                configuration.getVariables().putAll(this.configurationProperties);
            }
        }
        // 否则如果`configLocation`不为空, 则解析指定的xml配置文件生成Configuration
        else if (this.configLocation != null) {
            xmlConfigBuilder = new XMLConfigBuilder(this.configLocation.getInputStream(), null, this.configurationProperties);
            // 这里只是将xmlConfigBuilder中configuration的引用设置到当前的configuration, 还没真正进行configuration的初始化
            configuration = xmlConfigBuilder.getConfiguration();
        }
        // 否则: 'configuration'和'configLocation'都不存在, 创建默认的Configuration
        // SqlSessionFactoryBean提供了很多配置参数, 因此可以不提供'configuration'和'configLocation', 而是直接提供Configuration的属性
        else {
            LOGGER.debug(() -> "Property 'configuration' or 'configLocation' not specified, using default MyBatis Configuration");
            configuration = new Configuration();
            if (this.configurationProperties != null) {
                configuration.setVariables(this.configurationProperties);
            }
        }

        // 如果`objectFactory`不为空, 则设置到Configuration. 通常在不提供'configuration'和'configLocation'的情况下会提供这些配置参数
        if (this.objectFactory != null) {
            configuration.setObjectFactory(this.objectFactory);
        }

        if (this.objectWrapperFactory != null) {
            configuration.setObjectWrapperFactory(this.objectWrapperFactory);
        }

        if (this.vfs != null) {
            configuration.setVfsImpl(this.vfs);
        }

        if (hasLength(this.typeAliasesPackage)) {
            String[] typeAliasPackageArray = tokenizeToStringArray(this.typeAliasesPackage,
                    ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            for (String packageToScan : typeAliasPackageArray) {
                configuration.getTypeAliasRegistry().registerAliases(packageToScan,
                        typeAliasesSuperType == null ? Object.class : typeAliasesSuperType);
                LOGGER.debug(() -> "Scanned package: '" + packageToScan + "' for aliases");
            }
        }

        if (!isEmpty(this.typeAliases)) {
            for (Class<?> typeAlias : this.typeAliases) {
                configuration.getTypeAliasRegistry().registerAlias(typeAlias);
                LOGGER.debug(() -> "Registered type alias: '" + typeAlias + "'");
            }
        }

        if (!isEmpty(this.plugins)) {
            for (Interceptor plugin : this.plugins) {
                configuration.addInterceptor(plugin);
                LOGGER.debug(() -> "Registered plugin: '" + plugin + "'");
            }
        }

        if (hasLength(this.typeHandlersPackage)) {
            String[] typeHandlersPackageArray = tokenizeToStringArray(this.typeHandlersPackage,
                    ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            for (String packageToScan : typeHandlersPackageArray) {
                configuration.getTypeHandlerRegistry().register(packageToScan);
                LOGGER.debug(() -> "Scanned package: '" + packageToScan + "' for type handlers");
            }
        }

        if (!isEmpty(this.typeHandlers)) {
            for (TypeHandler<?> typeHandler : this.typeHandlers) {
                configuration.getTypeHandlerRegistry().register(typeHandler);
                LOGGER.debug(() -> "Registered type handler: '" + typeHandler + "'");
            }
        }

        if (this.databaseIdProvider != null) {//fix #64 set databaseId before parse mapper xmls
            try {
                configuration.setDatabaseId(this.databaseIdProvider.getDatabaseId(this.dataSource));
            } catch (SQLException e) {
                throw new NestedIOException("Failed getting a databaseId", e);
            }
        }

        // cache不为空则添加cache: 这里是二级缓存, 一个cache只能对应一个namespace(即:mapper)
        if (this.cache != null) {
            configuration.addCache(this.cache);
        }

        // 如果xmlConfigBuilder不为空, 则进行configuration的初始化
        if (xmlConfigBuilder != null) {
            try {
                xmlConfigBuilder.parse();
                LOGGER.debug(() -> "Parsed configuration file: '" + this.configLocation + "'");
            } catch (Exception ex) {
                throw new NestedIOException("Failed to parse config resource: " + this.configLocation, ex);
            } finally {
                ErrorContext.instance().reset();
            }
        }

        if (this.transactionFactory == null) {
            this.transactionFactory = new SpringManagedTransactionFactory();
        }

        configuration.setEnvironment(new Environment(this.environment, this.transactionFactory, this.dataSource));

        // mapperLocations不为空, 解析所有mapper
        if (!isEmpty(this.mapperLocations)) {
            for (Resource mapperLocation : this.mapperLocations) {
                if (mapperLocation == null) {
                    continue;
                }

                try {
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperLocation.getInputStream(),
                            configuration, mapperLocation.toString(), configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                } catch (Exception e) {
                    throw new NestedIOException("Failed to parse mapping resource: '" + mapperLocation + "'", e);
                } finally {
                    ErrorContext.instance().reset();
                }
                LOGGER.debug(() -> "Parsed mapper file: '" + mapperLocation + "'");
            }
        } else {
            LOGGER.debug(() -> "Property 'mapperLocations' was not specified or no matching resources found");
        }

        return this.sqlSessionFactoryBuilder.build(configuration);
    }

}
