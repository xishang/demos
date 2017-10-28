# encoding:utf-8

from urllib import request

# with as语法 --------------------------------------------------------------------------------------------------
# with语句返回的对象必须有一个__enter__()方法和一个__exit__()方法
# 执行顺序:
# 1.__enter__()
# 2.with as 语句后的程序块, 该程序块正常执行或抛出异常, 都会调到__exit__()方法处执行
# 3.__exit__()

# request.urlopen(): 打开远程文件 -------------------------------------------------------------------------------
#
# read()、readline()、readlines()、close(): 读取文件
# info(): 返回HTTPMessage对象, 表示远程服务器返回的头信息
# getcode(): 返回Http状态码
# geturl(): 返回请求的url
with request.urlopen(r'http://www.baidu.com') as res:
    lines = res.readlines()
    index = 0
    for line in lines:
        index += 1
        print('第%d行: %s' % (index, line.decode('utf-8')))
    # print(res.read().decode('utf-8'))

# request.Request(): 包装请求信息 -------------------------------------------------------------------------------
data = {
    'name': 'james',
    'age': 30
}
headers = {
    'access_token': '123456'
}
# data不为空时会以POST方式请求
req = request.Request(url=r'http://www.abc.com', data=data, headers=headers)
# with request.urlopen(req) as res:
#     print(res.readline(
# ).decode('utf-8'))


# request.urlretrieve(): 获取远程文件 ----------------------------------------------------------------------------
filename, headers = request.urlretrieve(r'http://www.taobao.com', r'C:\Users\xs\Desktop\dats\taobao.html')
print('文件获取-------------')
print(filename)
print(headers)


