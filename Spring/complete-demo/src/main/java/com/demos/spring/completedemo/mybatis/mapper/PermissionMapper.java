package com.demos.spring.completedemo.mybatis.mapper;

import com.demos.spring.completedemo.bean.PermissionDO;
import com.demos.spring.completedemo.bean.PermissionQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface PermissionMapper {

    /**
     * 添加权限
     *
     * @param permission
     * @return
     */
    Integer insertPermission(PermissionDO permission);

    /**
     * 根据ID删除权限
     *
     * @param id
     * @return
     */
    Integer deletePermissionById(@Param("id") Long id);

    /**
     * 获取权限列表
     *
     * @param query
     * @return
     */
    List<PermissionDO> listPermission(PermissionQuery query);

    /**
     * 根据ID取出权限
     *
     * @param id
     * @return
     */
    PermissionDO getPermissionById(@Param("id") Long id);

    /**
     * 更新权限
     *
     * @param permission
     * @return
     */
    Integer updatePermission(PermissionDO permission);

    /**
     * 添加角色权限映射关系
     *
     * @param roleId
     * @param permissionIdSet
     * @return
     */
    Integer insertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIdSet") Set<Long> permissionIdSet);

    /**
     * 删除角色权限映射关系
     *
     * @param idSet
     * @return
     */
    Integer deleteRolePermissions(@Param("idSet") Set<Long> idSet);

}
