package com.demos.spring.completedemo.shiro.manage;

import com.demos.spring.completedemo.bean.PermissionDO;
import com.demos.spring.completedemo.bean.RoleDO;
import com.demos.spring.completedemo.service.PermissionService;
import com.demos.spring.completedemo.shiro.chain.ChainDefinitionSectionMetaSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Component
public class ShiroFilerChainManager {

    @Resource
    private ChainDefinitionSectionMetaSource metaSource;

    @Resource
    private ShiroFilterFactoryBean shiroFilterFactoryBean;

    @Resource
    private PermissionService permissionService;

    private String filterChainDefinitions;

    /**
     * 加载在spring-shiro.xml中配置的默认url过滤链
     */
    @PostConstruct
    public void init() {
        this.filterChainDefinitions = metaSource.getFilterChainDefinitions();
    }

    /**
     * 更新过滤链
     */
    public synchronized void updateFilterChains() {
        AbstractShiroFilter shiroFilter = null;
        try {
            shiroFilter = (AbstractShiroFilter) shiroFilterFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException("get ShiroFilter from shiroFilterFactoryBean error!");
        }
        PathMatchingFilterChainResolver resolver = (PathMatchingFilterChainResolver) shiroFilter
                .getFilterChainResolver();
        DefaultFilterChainManager manager = (DefaultFilterChainManager) resolver.getFilterChainManager();
        // 清空原来的过滤链
        manager.getFilterChains().clear();
        shiroFilterFactoryBean.getFilterChainDefinitionMap().clear();
        // 加载默认过滤链
        shiroFilterFactoryBean.setFilterChainDefinitions(filterChainDefinitions);
        List<PermissionDO> permissionList = permissionService.listAllPermissions();
        // 加载动态更新的过滤链
        for (PermissionDO permissionDO : permissionList) {
            String dataUrl = permissionDO.getDataUrl();
            if (StringUtils.isNotBlank(dataUrl)) {
                String rolesChain = getRolesChain(permissionDO.getRoleList());
                // 添加为roles过滤链
                manager.addToChain(dataUrl, "roles", rolesChain);
            }
        }
    }

    private String getRolesChain(List<RoleDO> roleList) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < roleList.size(); i++) {
            sb.append(roleList.get(i).getRoleKey());
            if (i != roleList.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

}
