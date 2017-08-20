import React from 'react';
import ReactDOM from 'react-dom';
import {Router, Route, Link, IndexRoute} from 'react-router';
import LoginPage from './modules/LoginPage';
import MainPage from './modules/MainPage'
import UserManage from './modules/UserManage';
import RoleManage from './modules/RoleManage';
import PermManage from './modules/PermManage';

import './index.css';

import registerServiceWorker from './registerServiceWorker';

ReactDOM.render(
    <Router>
        {/*登录页*/}
        <Route path="/login" component={LoginPage}/>
        {/*主容器*/}
        <Route path="/" component={MainPage}>
            {/*子节点为路由url返回的组件，通过【主容器】的this.props.children控制显示*/}
            <IndexRoute component={UserManage}/>
            <Route path="userManage" component={UserManage}/>
            <Route path="roleManage" component={RoleManage}/>
            <Route path="permManage" component={PermManage}/>
        </Route>
    </Router>,
    document.getElementById('root'));

registerServiceWorker();
