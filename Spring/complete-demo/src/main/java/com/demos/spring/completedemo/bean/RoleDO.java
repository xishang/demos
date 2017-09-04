package com.demos.spring.completedemo.bean;

import java.util.Date;
import java.util.List;

public class RoleDO {

    private Long id;
    private String roleKey;
    private String roleName;
    private String roleDesc;
    private RoleDO parentRole;
    private Integer status;
    private Date createTime;
    private Date modifyTime;

    private List<PermissionDO> permissionList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleDesc() {
        return roleDesc;
    }

    public void setRoleDesc(String roleDesc) {
        this.roleDesc = roleDesc;
    }

    public RoleDO getParentRole() {
        return parentRole;
    }

    public void setParentRole(RoleDO parentRole) {
        this.parentRole = parentRole;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public List<PermissionDO> getPermissionList() {
        return permissionList;
    }

    public void setPermissionList(List<PermissionDO> permissionList) {
        this.permissionList = permissionList;
    }

}
