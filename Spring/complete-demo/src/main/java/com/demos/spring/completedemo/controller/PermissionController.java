package com.demos.spring.completedemo.controller;

import com.demos.spring.completedemo.bean.PermissionDO;
import com.demos.spring.completedemo.bean.ResponseResult;
import com.demos.spring.completedemo.bean.SimplePageInfo;
import com.demos.spring.completedemo.exception.ErrorEnum;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/permission")
public class PermissionController {

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseResult<SimplePageInfo<PermissionDO>> listPermissions(int page, int size) {
        List<PermissionDO> permissionList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            PermissionDO permission = new PermissionDO();
            permission.setId((long) i);
            permission.setPermissionKey("permKey:" + i);
            permission.setPermissionName("权限:" + i);
            permission.setStatus(0);
            permissionList.add(permission);
        }
        SimplePageInfo<PermissionDO> permissionPage = new SimplePageInfo<>(permissionList);
        permissionPage.setPageNum(page);
        permissionPage.setPageSize(size);
        permissionPage.setPages(5);
        permissionPage.setTotal(45);
        return new ResponseResult<>(ErrorEnum.SUCCESS, permissionPage);
    }

}
