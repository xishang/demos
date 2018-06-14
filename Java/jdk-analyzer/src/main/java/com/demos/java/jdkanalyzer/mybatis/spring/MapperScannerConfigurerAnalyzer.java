package com.demos.java.jdkanalyzer.mybatis.spring;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/11
 *
 * MapperScannerConfigurer解析: 扫描Mapper接口所在的包并将所有Mapper注册到BeanFactory
 * -> InitializingBean.afterPropertiesSet(): 初始化方法, 校验`basePackage`是否为空
 * -> BeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry(): BeanFactory后置处理器, 扫描`basePackage`下的mapper接口
 * ===== 注意: BeanDefinitionRegistries发生BeanFactoryPostProcessor调用之前, 由此产生的问题:
 * -> 1.此时PropertyResourceConfigurer还没有加载属性, 因此所有的属性尚未解析, 包括在mybatis配置文件中使用的属性以及诸如DataSource配置使用的`${jdbc.url}`等重要属性
 * -> 2.由于此时像`${jdbc.url}`这样的属性值尚未被解析替换, 因此若直接设置`sqlSessionFactoryBean`会导致其提前被初始化, 此时会直接使用`${jdbc.url}`字面量而不是对应的属性值
 * -> 3.基于原因2, 选择注入`sqlSessionFactoryBeanName`, 在需要的时候才会初始化`sqlSessionFactoryBean`, 此时`${jdbc.url}`等属性值已被加载并解析替换
 * === 并且, 如果有多个DataSource, 选择类型自动注入的话很可能会失败
 * ===== 重要属性:
 * -> basePackage: Mapper接口路径
 * -> sqlSessionFactoryBeanName: `sqlSessionFactoryBean`的`beanName`, 为避免提前初始化, 这里不建议直接注入`sqlSessionFactoryBean`
 * -> processPropertyPlaceHolders: 是否提前处理属性值
 * ===== postProcessBeanDefinitionRegistry()处理流程:
 * -> 1.如果`processPropertyPlaceHolders`为true: 提前解析出`basePackage`等的属性值并更新到变量
 * -> 2.注册过滤器: include-filter, exclude-filter(过滤package-info)
 * -> 3.调用ClassPathMapperScanner.scan(basePackage)扫描mapper接口路径
 */
public class MapperScannerConfigurerAnalyzer implements BeanDefinitionRegistryPostProcessor, InitializingBean  {

    private String basePackage;

    private boolean addToConfig = true;

    private SqlSessionFactory sqlSessionFactory;

    private SqlSessionTemplate sqlSessionTemplate;

    private String sqlSessionFactoryBeanName;

    private String sqlSessionTemplateBeanName;

    private Class<? extends Annotation> annotationClass;

    private Class<?> markerInterface;

    private ApplicationContext applicationContext;

    private String beanName;

    private boolean processPropertyPlaceHolders;

    private BeanNameGenerator nameGenerator;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 校验`basePackage`是否为空
        notNull(this.basePackage, "Property 'basePackage' is required");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // left intentionally blank
    }

    /**
     * BeanFactoryPostProcessor: 扫描basePackage下的mapper
     * 在 AbstractApplicationContext.refresh() -> invokeBeanFactoryPostProcessors(beanFactory) 中调用
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        // 如果需要处理属性值: 则主动调用解析出`basePackage`等属性值
        if (this.processPropertyPlaceHolders) {
            processPropertyPlaceHolders();
        }

        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
        scanner.setAddToConfig(this.addToConfig);
        scanner.setAnnotationClass(this.annotationClass);
        scanner.setMarkerInterface(this.markerInterface);
        scanner.setSqlSessionFactory(this.sqlSessionFactory);
        scanner.setSqlSessionTemplate(this.sqlSessionTemplate);
        scanner.setSqlSessionFactoryBeanName(this.sqlSessionFactoryBeanName);
        scanner.setSqlSessionTemplateBeanName(this.sqlSessionTemplateBeanName);
        scanner.setResourceLoader(this.applicationContext);
        scanner.setBeanNameGenerator(this.nameGenerator);
        // 注册过滤器: 过滤掉`package-info`
        scanner.registerFilters();
        // 扫描`basePackage`
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    /**
     * BeanDefinitionRegistries发生BeanFactoryPostProcessor调用之前, 此时PropertyResourceConfigurer还没有加载属性
     * 该方法主动调用PropertyResourceConfigurer处理属性, 并解析出`basePackage`等扫描需要的属性值
     */
    private void processPropertyPlaceHolders() {
        // 找出所有`PropertyResourceConfigurer`
        Map<String, PropertyResourceConfigurer> prcs = applicationContext.getBeansOfType(PropertyResourceConfigurer.class);

        if (!prcs.isEmpty() && applicationContext instanceof ConfigurableApplicationContext) {
            BeanDefinition mapperScannerBean = ((ConfigurableApplicationContext) applicationContext)
                    .getBeanFactory().getBeanDefinition(beanName);

            // PropertyResourceConfigurer does not expose any methods to explicitly perform
            // property placeholder substitution. Instead, create a BeanFactory that just
            // contains this mapper scanner and post process the factory.
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            factory.registerBeanDefinition(beanName, mapperScannerBean);

            for (PropertyResourceConfigurer prc : prcs.values()) {
                prc.postProcessBeanFactory(factory);
            }

            PropertyValues values = mapperScannerBean.getPropertyValues();

            // 更新basePackage属性值
            this.basePackage = updatePropertyValue("basePackage", values);
            this.sqlSessionFactoryBeanName = updatePropertyValue("sqlSessionFactoryBeanName", values);
            this.sqlSessionTemplateBeanName = updatePropertyValue("sqlSessionTemplateBeanName", values);
        }
    }

}
