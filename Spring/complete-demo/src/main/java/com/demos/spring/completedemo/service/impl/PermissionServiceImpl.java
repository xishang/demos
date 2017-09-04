package com.demos.spring.completedemo.service.impl;

import com.demos.spring.completedemo.bean.PermissionDO;
import com.demos.spring.completedemo.bean.PermissionQuery;
import com.demos.spring.completedemo.bean.SimplePageInfo;
import com.demos.spring.completedemo.mybatis.mapper.PermissionMapper;
import com.demos.spring.completedemo.service.PermissionService;
import com.demos.spring.completedemo.util.SystemConstant;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private PermissionMapper permissionMapper;

    @Override
    public void savePermission(PermissionDO permissionDO) {
        permissionDO.setStatus(SystemConstant.PERMISSION_STATUS_NORMAL);
        Date curTime = new Date();
        permissionDO.setCreateTime(curTime);
        permissionDO.setModifyTime(curTime);
        permissionMapper.insertPermission(permissionDO);
    }

    @Override
    public int updatePermission(PermissionDO permissionDO) {
        return permissionMapper.updatePermission(permissionDO);
    }

    @Override
    public PermissionDO getPermissionById(Long id) {
        return permissionMapper.getPermissionById(id);
    }

    @Override
    public List<PermissionDO> listPermissionByUserId(Long userId) {
        PermissionQuery query = new PermissionQuery();
        query.setUserId(userId);
        return permissionMapper.listPermission(query);
    }

    @Override
    public List<PermissionDO> listPermissionByRoleId(Long roleId) {
        PermissionQuery query = new PermissionQuery();
        query.setRoleId(roleId);
        return permissionMapper.listPermission(query);
    }

    @Override
    public SimplePageInfo<PermissionDO> listPermission(PermissionQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<PermissionDO> permissionList = permissionMapper.listPermission(query);
        return new SimplePageInfo<>(permissionList);
    }

    @Override
    public List<PermissionDO> listAllPermissions() {
        return permissionMapper.listPermission(new PermissionQuery());
    }

    @Override
    public int insertRolePermissions(Long roleId, Set<Long> permissionIdSet) {
        return permissionMapper.insertRolePermissions(roleId, permissionIdSet);
    }

    @Override
    public int deleteRolePermissions(Set<Long> idSet) {
        return permissionMapper.deleteRolePermissions(idSet);
    }

}
