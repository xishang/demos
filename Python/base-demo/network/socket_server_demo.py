# encoding:utf-8

import socket

# 创建socket对象
s = socket.socket()

# 获取本地主机名
host = socket.gethostname()
print(host)

# 设置端口号
port = 8080

# 绑定端口
s.bind((host, port))

# 设置最大连接数，超过后排队
s.listen(5)

while True:
    # 建立客户端连接
    c, addr = s.accept()
    print('connecting with ', addr)
    # 信息编码后发送
    c.send('Thank you for connecting'.encode('utf-8'))
    c.close()
