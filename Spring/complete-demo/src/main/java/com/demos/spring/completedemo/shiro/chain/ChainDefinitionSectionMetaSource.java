package com.demos.spring.completedemo.shiro.chain;

import com.demos.spring.completedemo.bean.PermissionDO;
import com.demos.spring.completedemo.bean.RoleDO;
import com.demos.spring.completedemo.service.PermissionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.config.Ini;
import org.springframework.beans.factory.FactoryBean;

import javax.annotation.Resource;
import java.util.List;

public class ChainDefinitionSectionMetaSource implements FactoryBean<Ini.Section> {

    @Resource
    private PermissionService permissionService;

    private String filterChainDefinitions;

    /**
     * 在shiro.xml文件中配置默认的url时注入该参数
     *
     * @param filterChainDefinitions
     */
    public void setFilterChainDefinitions(String filterChainDefinitions) {
        this.filterChainDefinitions = filterChainDefinitions;
    }

    public String getFilterChainDefinitions() {
        return filterChainDefinitions;
    }

    @Override
    public Ini.Section getObject() throws Exception {
        Ini ini = new Ini();
        ini.load(filterChainDefinitions);
        Ini.Section section = ini.getSection(Ini.DEFAULT_SECTION_NAME);

        List<PermissionDO> permissionList = permissionService.listAllPermissions();
        // 加载动态更新的过滤链
        for (PermissionDO permissionDO : permissionList) {
            if (StringUtils.isNotBlank(permissionDO.getDataUrl())) {
                // 添加为roles过滤链
                // 过滤链格式：url=filter1[param1,param2],filter2[param1,param2]
                section.put(permissionDO.getDataUrl(), getRolesChain(permissionDO.getRoleList()));
            }
        }
        return section;
    }

    @Override
    public Class<?> getObjectType() {
        return this.getClass();
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    private String getRolesChain(List<RoleDO> roleList) {
        StringBuilder sb = new StringBuilder();
        sb.append("roles[");
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
