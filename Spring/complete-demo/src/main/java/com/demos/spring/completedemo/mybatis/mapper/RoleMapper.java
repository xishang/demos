package com.demos.spring.completedemo.mybatis.mapper;

import com.demos.spring.completedemo.bean.RoleDO;
import com.demos.spring.completedemo.bean.RoleQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface RoleMapper {

    /**
     * 添加角色
     *
     * @param role
     * @return
     */
    Integer insertRole(RoleDO role);

    /**
     * 根据ID删除角色
     *
     * @param id
     * @return
     */
    Integer deleteRoleById(@Param("id") Long id);

    /**
     * 获取角色列表
     *
     * @param query
     * @return
     */
    List<RoleDO> listRole(RoleQuery query);

    /**
     * 根据ID取出角色
     *
     * @param id
     * @return
     */
    RoleDO getRoleById(@Param("id") Long id);

    /**
     * 根据ID更新角色
     *
     * @param role
     * @return
     */
    Integer updateRole(RoleDO role);

    /**
     * 添加用户与角色映射关系
     *
     * @param userId    用户ID
     * @param roleIdSet 角色ID集合
     * @return
     */
    Integer insertUserRoles(@Param("userId") Long userId, @Param("roleIdSet") Set<Long> roleIdSet);

    /**
     * 删除用户角色映射关系
     *
     * @param idSet 用户角色映射关系ID集合
     * @return
     */
    Integer deleteUserRoles(@Param("idSet") Set<Long> idSet);

}
