package com.demos.spring.completedemo.controller;

import com.demos.spring.completedemo.bean.ResponseResult;
import com.demos.spring.completedemo.bean.RoleDO;
import com.demos.spring.completedemo.bean.SimplePageInfo;
import com.demos.spring.completedemo.exception.ErrorEnum;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseResult<SimplePageInfo<RoleDO>> listRoles(int page, int size) {
        List<RoleDO> roleList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            RoleDO role = new RoleDO();
            role.setId((long) i);
            role.setRoleKey("roleKey:" + i);
            role.setRoleName("角色:" + i);
            role.setStatus(0);
            roleList.add(role);
        }
        SimplePageInfo<RoleDO> rolePage = new SimplePageInfo<>(roleList);
        rolePage.setPageNum(page);
        rolePage.setPageSize(size);
        rolePage.setPages(5);
        rolePage.setTotal(45);
        return new ResponseResult<>(ErrorEnum.SUCCESS, rolePage);
    }

}
