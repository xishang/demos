# Simple CAS Demo

## 启动方式

### 1.Main方法启动

### 2.`java -jar`命令启动

    java -jar cas-server.jar --spring.profiles.active=dev

### 3.mvn启动

    mvn spring-boot:run

## 核心url

### 1.登录页面

    url:    /login
    method: GET
    params:
        returnUrl:  应用系统url
    return:
        若已认证:   产生临时令牌token, 并重定向回应用系统
        若未认证:   返回登录页面
    
### 2.用户登录

    url:    /login
    method: POST
    params:
        username:   用户名
        password:   密码
        returnUrl:  应用系统url
    return:
        认证通过:   产生临时令牌token, 并重定向回应用系统
        认证失败:   返回登录页面
        
### 3.token认证

    url:    /verify
    method: POST
    params:
        token:      临时令牌
        localId:    本地sessionId
    return:
        认证结果, 若认证通过则包括用户认证信息
        
### 4.用户登出

    url:    /logout
    method: POST
    params:
        globalId:   全局sessionId(cas-server保存的用户session)
    return:
        登出结果