import React from 'react';
import WrappedNormalLoginForm from './LoginForm';

import './LoginPage.css';

class LoginPage extends React.Component {
    render() {
        return (
            <div className="page-container">
                <div className="login-form-container">
                    <WrappedNormalLoginForm/>
                </div>
            </div>
        );
    }
}

export default LoginPage