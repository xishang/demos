# encoding:utf-8


def plus(num1, num2):
    return num1 + num2


def hello():
    print("hello, world")


# 测试hello()方法
# 注：在模块被导入时该方法也会执行
hello()

# __name__: 变量__name__在主程序中的值为'__main__'，在导入的模块中，值为被设定的模块名 -----------------
# 判断当__name__变量值为'__main__'时才执行，可以避免在被导入时执行测试方法
if __name__ == '__main__':
    hello()
