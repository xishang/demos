package com.demos.spring.completedemo.service;

import com.demos.spring.completedemo.bean.PermissionDO;
import com.demos.spring.completedemo.bean.PermissionQuery;
import com.demos.spring.completedemo.bean.SimplePageInfo;

import java.util.List;
import java.util.Set;

public interface PermissionService {

    void savePermission(PermissionDO permissionDO);

    int updatePermission(PermissionDO permissionDO);

    PermissionDO getPermissionById(Long id);

    List<PermissionDO> listPermissionByUserId(Long userId);

    List<PermissionDO> listPermissionByRoleId(Long roleId);

    SimplePageInfo<PermissionDO> listPermission(PermissionQuery query);

    List<PermissionDO> listAllPermissions();

    int insertRolePermissions(Long roleId, Set<Long> permissionIdSet);

    int deleteRolePermissions(Set<Long> idSet);

}
