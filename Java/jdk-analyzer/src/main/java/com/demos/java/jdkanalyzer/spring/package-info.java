/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/6
 * <p>
 * Spring源码解析
 * <p>
 * ===== 1:
 * -> AliasRegistry(I): 定义对`alias`的简单增、删、改等动作
 * -> SimpleAliasRegistry: 主要使用`map`作为`alias`的缓存, 是`AliasRegistry`接口的简单实现
 * -> SingletonBeanRegistry(I): 定义对单例的注册及获取
 * -> DefaultSingletonBeanRegistry: 对接口`SingletonBeanRegistry`的实现
 * -> FactoryBeanRegistrySupport: 继承`DefaultSingletonBeanRegistry`, 增加了对`FactoryBean`的特殊处理
 * -> BeanFactory(I): 定义获取`Bean`及`Bean`的各种属性
 * -> HierarchicalBeanFactory(I): 继承`BeanFactory`, 增加了获取`ParentFactory`的接口
 * -> BeanDefinitionRegistry(I): 继承`AliasRegistry`, 定义了对`BeanDefinition`的各种操作
 * ===== 2:
 * -> ConfigurableBeanFactory(I): 提供配置`BeanFactory`的各种方法
 * -> ListableBeanFactory(I): 提供获取`Bean`的配置清单的各种方法
 * -> AbstractBeanFactory: 继承`FactoryBeanRegistrySupport`并实现了`ConfigurableBeanFactory`接口
 * -> AutowireCapableBeanFactory(I): 提供`Bean`的`创建`、`自动注入`、`初始化`以及应用`后处理器`的接口
 * -> AbstractAutowireCapableBeanFactory: 继承`AbstractBeanFactory`并实现了`AutowireCapableBeanFactory`接口
 * -> ConfigurableListableBeanFactory(I): `BeanFactory`配置清单, 指定了`忽略类型`等
 * ===== 核心类: `DefaultListableBeanFactory`
 * -> 继承`AbstractAutowireCapableBeanFactory`并实现了`ConfigurableListableBeanFactory`和`BeanDefinitionRegistry`接口
 * ===== 3: 核心--BeanDefinition
 * == BeanDefinition(I): 核心接口, 定义获取`Bean`的各种配置信息、元数据等方法
 * == AbstractBeanDefinition: 继承`BeanMetadataAttributeAccessor`并实现`BeanDefinition`, 提供了各种操作`Bean`的`配置信息`和`元数据`的方法
 * ===== 4:
 * -> ResourceLoader(I): 定义资源加载器, 根据给定资源文件地址返回`Resource`
 * -> BeanDefinitionReader(I): 定义读取资源文件并转换为`BeanDefinition`的各种方法
 * -> EnvironmentCapable(I): 定义获取`Environment`的方法
 * -> DocumentLoader(I): 定义从资源文件加载并转换为`Document`的方法
 * -> AbstractBeanDefinitionReader: 实现了`BeanDefinitionReader`和`EnvironmentCapable`接口
 * -> XmlBeanDefinitionReader: 继承`AbstractBeanDefinitionReader`, 从`XML`配置文件读取并注册`BeanDefinition`
 * -> BeanDefinitionDocumentReader(I): 定义读取`Document`并注册`BeanDefinition`的功能
 * -> BeanDefinitionParserDelegate: 代理解析`Element`的各种方法
 */
package com.demos.java.jdkanalyzer.spring;