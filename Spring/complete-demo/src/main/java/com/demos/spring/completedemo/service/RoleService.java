package com.demos.spring.completedemo.service;

import com.demos.spring.completedemo.bean.RoleDO;
import com.demos.spring.completedemo.bean.RoleQuery;
import com.demos.spring.completedemo.bean.SimplePageInfo;

import java.util.List;
import java.util.Set;

public interface RoleService {

    void saveRole(RoleDO role);

    int updateRole(RoleDO role);

    RoleDO getRoleById(Long id);

    List<RoleDO> listRoleByUserId(Long userId);

    SimplePageInfo<RoleDO> listRole(RoleQuery query);

    List<RoleDO> listAllRoles();

    int insertUserRoles(Long userId, Set<Long> roleIdSet);

    int deleteUserRoles(Set<Long> idSet);

}
