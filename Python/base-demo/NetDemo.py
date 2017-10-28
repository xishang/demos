# encoding:utf-8

# Python网络编程

import socket

s = socket.socket()

host = socket.gethostname()
print(host)

port = 1234

s.bind((host, port))

s.listen(5)
while True:
    c, addr = s.accept()
    print('connecting with ', addr)
    c.send('Thank you for connecting')
    c.close()










