import {hashHistory} from 'react-router';

export default function request(method, url, body) {
    method = method.toUpperCase();
    if (method === 'GET') {
        // fetch的GET不允许有body，参数只能放在url中
        body = undefined;
    } else {
        // && 运算符，返回第一个为false的值（没有就返回最后一个）
        // || 运算符，返回第一个为true的值（没有就返回最后一个）
        body = body && JSON.stringify(body);
    }
    return fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'Access-Token': sessionStorage.getItem('access_token') || '' // 从sessionStorage中获取access token
        },
        body: body
    }).then((response) => {
        if (response.status === 200) {
            return response.json();
        } else {
            // const token = response.headers.get('sid');
            // if (token) {
            //     sessionStorage.setItem('access_token', token);
            // }
            hashHistory.push('/login');
            return Promise.reject('Unauthorized');
        }
    }).catch(function (error) {
        console.log('request failed', error)
    });
}

export const get = url => request('GET', url);
export const post = (url, body) => request('POST', url, body);
export const put = (url, body) => request('PUT', url, body);
export const del = (url, body) => request('DELETE', url, body);