import React from 'react';

import { Table } from 'antd';
import { post } from './request';

const columns = [{
    title: 'ID',
    dataIndex: 'id',
}, {
    title: 'Name',
    dataIndex: 'username',
    sorter: true,
    width: '20%',
}, {
    title: 'RealName',
    dataIndex: 'realName',
    width: '20%',
}, {
    title: 'Status',
    dataIndex: 'status',
}];

class UserManage extends React.Component {
    state = {
        data: [],
        pagination: {},
        loading: false,
    };
    handleTableChange = (pagination, filters, sorter) => {
        const pager = { ...this.state.pagination };
        pager.current = pagination.current;
        this.setState({
            pagination: pager,
        });
        this.fetch({
            page: pagination.current,
            size: pagination.pageSize
        });
    }
    fetch = (params = {}) => {
        console.log('params:', params);
        this.setState({ loading: true });
        post('http://localhost:8080/user/list?page=1&size=10').then((data) => {
            debugger;
            let page = data.data;
            const pagination = { ...this.state.pagination };
            pagination.total = 98;
            this.setState({
                loading: false,
                data: page.list,
                pagination : pagination
            });
        });
    }
    componentDidMount() {
        this.fetch();
    }
    render() {
        return (
            <Table columns={columns}
                   rowKey={record => record.registered}
                   dataSource={this.state.data}
                   pagination={this.state.pagination}
                   loading={this.state.loading}
                   onChange={this.handleTableChange}
            />
        );
    }
}

export default UserManage