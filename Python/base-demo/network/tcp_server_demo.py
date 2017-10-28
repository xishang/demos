# encoding:utf-8

from socketserver import TCPServer, ThreadingMixIn, StreamRequestHandler


# 继承TCPServer, 并使用线程处理
class Server(TCPServer, ThreadingMixIn): pass


# 消息处理
class Handler(StreamRequestHandler):
    def handle(self):
        addr = self.request.getpeername()
        print('Got connection from ' + addr)
        self.wfile.write('Thank you for connecting'.encode('utf-8'))

# 创建服务
server = Server(('', 8080), Handler)
server.serve_forever()



