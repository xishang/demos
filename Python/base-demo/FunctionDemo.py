# encoding:utf-8


def fibs(num):
    result = [0, 1]
    for i in range(num):
        result.append(result[-2] + result[-1])
    return result

print(fibs(8))


# 关键字参数与参数默认值 ----------------------------------------------------------
def say_hello(greeting='Hello', name='World'):
    print('%s, %s' % (greeting, name))

# 输出：Hello, James
say_hello(name="James")


# 收集参数 -----------------------------------------------------------------------
#   *params:    将剩余参数收集为元组
#   **params:   将剩余参数收集为字典
def print_names(greeting, *names, **desc):
    for name in names:
        print('%s, %s' % (greeting, name))
    print(desc)

# 输出：
#       Hi, James
#       Hi, Mike
#       Hi, Philip
#       {'name': 'Simon', 'age': 24}
print_names("Hi", "James", "Mike", "Philip", name="Simon", age=24)

content = {'name': 'Simon', 'greeting': 'Hi'}
# 参数收集逆过程：将字典解析为关键字参数，或将序列解析为可变参数 ----------------------
say_hello(**content)


# 作用域：内部作用域（如：函数）不能影响全局变量 -------------------------------------
def change_x():
    x = 10


# 局部作用域中改变全局变量 ---------------------------------------------------------
# 1.globals()['param']: 获取全局变量
# 2.声明全局变量: global param，然后直接修改全局变量param
def change_global_x():
    global x
    x = 15

x = 5
change_x()
# 输出：5
print(x)
change_global_x()
# 输出：15
print(x)


# nonlocal: 外部作用域 --------------------------------------------------------------
def change_local():
    p_a = 10
    p_b = 10

    def change_param():
        p_a = 12
        # 使用外部作用域的变量
        nonlocal p_b
        p_b = 12
    change_param()
    # 输出：10
    print(p_a)
    # 输出：12
    print(p_b)

change_local()

# lambda表达式：lambda *params : operate(params) ------------------------------------
# python常用函数：
# map(func, seq[, seq, ......])     对序列中的每个元素应用函数
# filter(func, seq)                 返回其函数为真的元素的列表
# reduce(func, seq[, initial])      将序列前两个元素与给定函数联合使用，并将返回值与第三个元素继续联合使用，直到序列处理完毕
# sum(seq)                          返回序列所有元素的和
# apply(func[, args[, kwargs]])     调用函数，可以提供参数
numbers = [1, 3, 4, 8, 10, 15]
filter_numbers = filter(lambda num: num % 2 == 0, numbers)
for number in filter_numbers:
    print(number)
