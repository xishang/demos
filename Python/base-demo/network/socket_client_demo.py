# encoding:utf-8

import socket

# 创建socket对象
s = socket.socket()

# 获取本地主机名
host = socket.gethostname()
print(host)

# 设置端口号
port = 8080

# 连接服务，指定主机和端口
s.connect((host, port))

# 接收1024字节的数据并解码
print(s.recv(1024).decode('utf-8'))

