package com.demos.spring.completedemo.util;

public class SystemConstant {

    // 用户状态 0:正常, 1:锁定, 2:已删除
    public static final Integer USER_STATUS_NORMAL = 0;
    public static final Integer USER_STATUS_LOCKED = 1;
    public static final Integer USER_STATUS_DELETED = 2;

    // 角色状态 0:正常, 1:禁用, 2:删除
    public static final Integer ROLE_STATUS_NORMAL = 0;
    public static final Integer ROLE_STATUS_FORBIDDEN = 1;
    public static final Integer ROLE_STATUS_DELETED = 2;

    // 权限状态 0:正常, 1:禁用, 2:删除
    public static final Integer PERMISSION_STATUS_NORMAL = 0;
    public static final Integer PERMISSION_STATUS_FORBIDDEN = 1;
    public static final Integer PERMISSION_STATUS_DELETED = 2;

}
