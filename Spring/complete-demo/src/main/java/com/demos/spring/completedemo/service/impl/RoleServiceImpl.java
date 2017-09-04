package com.demos.spring.completedemo.service.impl;

import com.demos.spring.completedemo.bean.RoleDO;
import com.demos.spring.completedemo.bean.RoleQuery;
import com.demos.spring.completedemo.bean.SimplePageInfo;
import com.demos.spring.completedemo.mybatis.mapper.RoleMapper;
import com.demos.spring.completedemo.service.RoleService;
import com.demos.spring.completedemo.util.SystemConstant;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Override
    public void saveRole(RoleDO role) {
        role.setStatus(SystemConstant.ROLE_STATUS_NORMAL);
        Date curTime = new Date();
        role.setCreateTime(curTime);
        role.setModifyTime(curTime);
        roleMapper.insertRole(role);
    }

    @Override
    public int updateRole(RoleDO role) {
        return roleMapper.updateRole(role);
    }

    @Override
    public RoleDO getRoleById(Long id) {
        return roleMapper.getRoleById(id);
    }

    @Override
    public List<RoleDO> listRoleByUserId(Long userId) {
        RoleQuery query = new RoleQuery();
        query.setUserId(userId);
        return roleMapper.listRole(query);
    }

    @Override
    public SimplePageInfo<RoleDO> listRole(RoleQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<RoleDO> roleList = roleMapper.listRole(query);
        return new SimplePageInfo<>(roleList);
    }

    @Override
    public List<RoleDO> listAllRoles() {
        return roleMapper.listRole(new RoleQuery());
    }

    @Override
    public int insertUserRoles(Long userId, Set<Long> roleIdSet) {
        return roleMapper.insertUserRoles(userId, roleIdSet);
    }

    @Override
    public int deleteUserRoles(Set<Long> idSet) {
        return roleMapper.deleteUserRoles(idSet);
    }

}
