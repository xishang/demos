# encoding:utf-8

# Python标准库Demo

import os
import random
import shelve
import re
import fileinput
from heapq import *
from test import HelloTest

# dir(): 将对象的所有特性以及模块的所有函数、类、变量等列出 ------------------------------------------
for n in dir(HelloTest):
    print(n)

# sys模块 ----------------------------------------------------------------------------------------
# argv          命令行参数, 包括脚本名称
# exit([arg])   退出当前程序, 可选参数为给定返回值或错误信息
# modules       映射模块名字到载入模块的字典
# path          查找模块所在目录的目录名列表
# platform      类似sunos5或者win32的平台标识符
# stdin         标准输入流——一个类文件(file-like)对象
# stdout        标准输出流——一个类文件对象
# stderr        标准错误流——一个类文件对象

# os模块 -----------------------------------------------------------------------------------------
# environ       对环境变量进行映射
# system(arg)   在子shell中执行操作系统命令
# sep           路径中的分隔符
# pathsep       分隔路径的分隔符
# linesep       行分隔符('\n', '\r', '\r\n')
# urandom(n)    返回n字节的加密强随机数据

# 打开网易云音乐
# os.system(r'D:\"Program Files (x86)"\Netease\CloudMusic\cloudmusic.exe')

# 文件和流 ----------------------------------------------------------------------------------------
# open(name[, mode[, buffering]]) ----------------------------------------------------------------
# 1.文件模式(mode属性)
# 'r'   读模式
# 'w'   写模式
# 'a'   追加模式
# 'b'   二进制模式
# '+'   读/写模式
# 2.缓冲(buffering属性): 0:无缓冲, 1:有缓冲, >1的数字:缓冲区大小, -1:默认缓冲区大小

# read([bytes])     读取bytes个字节, 没有参数则读取所有内容 -----------------------------------------
# readline()        读取一行
# readlines()       读取所有的行(可迭代)
# write(arg)        写入内容
# 写入内容(追加模式), 需要显示调用f.close()
# 写文件时使用try/finally语句, 确保文件流被关闭
try:
    f = open(r'C:\Users\xs\Desktop\dats\test.txt', 'a+')
    f.write('this is 1st line\n')
    f.write('this is 2nd line\n')
finally:
    f.close()
# 读取内容
f = open(r'C:\Users\xs\Desktop\dats\test.txt', 'r')
for line in f.readlines():
    print(line)
# 管式输出:|, 前一个命令的标准输出作为后一个命令的标准输入
# cat test.txt | python test.py | sort

# fileinput模块 ----------------------------------------------------------------------------------
# input(files[, inplace[, backup]])     遍历多个输入流中的行
# filename()                            返回当前文件的名称
# lineno()                              返回当前(累计)的行数
# filelineno()                          返回当前处理文件的当前行数
# isfirstline()                         检查当前行是否是文件的第一行
# isstdin()                             检查当前文件是否是sys.stdin
# nextfile()                            关闭当前文件, 移动到下一个文件
# close()                               关闭序列
for line in fileinput.input(r'C:\Users\xs\Desktop\dats\test.txt'):
    print(line)

# set: 集合, 集合中只能包含不可变的值 --------------------------------------------------------------
# add(arg)      添加元素
# remove(arg)   移除元素
# union(arg)    取并集, 类似'|'运算符
# copy()        拷贝集合

# frozenset(arg): 构建集合的不可变副本, 可作为元素添加到集合中 --------------------------------------

a = set(range(5))
b = set([3, 6])
# |: {0, 1, 2, 3, 4, 6}
c = a | b
# &: {3}
d = a & b
# ^: {0, 1, 2, 4, 6}
e = a ^ b
# -: {0, 1, 2, 4}
f = a - b
# 集合没有'+'运算
print(c, d, e, f)

# heap: 堆, 优先队列的一种 ------------------------------------------------------------------------
# heappush(heap, x)         将x入堆
# heappop(heap)             将堆中最小的元素弹出, 一般都是索引为0的位置, 同时会确保剩余最小元素占据这个位置
# heapify(heap)             将heap属性强制应用到任意一个列表, 对非heappush()构建的堆操作前应使用该函数
# heapreplace(heap, x)      将堆中最小的元素弹出, 同时将x入堆
# nlargest(n, iter)         返回iter中第n大的元素
# nsmallest(n, iter)        返回iter中第n小的元素

heap = []
for n in range(4):
    heappush(heap, n)
heappush(heap, 1.2)
print(heap)

# time模块 ----------------------------------------------------------------------------------------
# asctime([tuple])              将时间元组转换为时间字符串
# localtime([secs])             将秒数转换为日期元组, 以本地时间为准
# mktime(tuple)                 将时间元组转换为本地时间
# sleep(secs)                   休眠secs秒
# strptime(string[, format])    将字符串解析为时间元组
# time()                        当前时间(新纪元开始后的秒数, 以UTC为准)

# random模块 --------------------------------------------------------------------------------------
# random()                          返回(0, 1]之间的随机实数
# getrandbits(n)                    以长整型形式返回n个随机位
# uniform(a, b)                     返回[a, b)之间的随机实数
# randrange([start, ]stop[, step])  返回range(start, stop, step)中的随机数
# choice(seq)                       从序列seq中返回随机元素
# shuffle(seq[, random])            将给定序列seq进行随机移位
# sample(seq, n)                    从序列seq中选择n个随机且独立的元素

nums = range(1, 11)
types = ['A', 'B', 'C', 'D']
decks = ['%s%s' % (a, b) for a in nums for b in types]
random.shuffle(decks)
print(decks)

# shelve: 字典型存储方案 ---------------------------------------------------------------------------
test_dat = shelve.open(filename=r'C:\Users\xs\Desktop\dats\test.dat')
test_dat['name'] = 'james'
test_dat['age'] = '30'
test_dat['friends'] = ['a', 'b']
# shelve为赋值时写入, 因此此处的'd'不会被保存, 可在open()函数设置参数writeback=True设置在关闭shelf时才写回磁盘
test_dat['friends'].append('d')
print(test_dat)

# re模块 ------------------------------------------------------------------------------------------
# compile(pattern[, flags])             根据包含正则表达式的字符串创建模式对象
# search(pattern, string[, flags])      在字符串中寻找模式
# match(pattern, string[, flags])       在字符串的开始处匹配模式
# split(pattern, string[, maxsplit=0])  根据模式的匹配项来分割字符串
# findall(pattern, string)              列出字符串中模式的所有匹配项
# sub(pat, repl, string[, count=0])     将字符串中所有pat的匹配项用repl替换
# escape(string)                        将字符串中所有特殊正则表达式字符转义

# 匹配对象和组: group索引从0开始, 组0就是整个模式 ----------------------------------------------------
# group([index1, ...])          获取指定子模式(组)的匹配项
# start([group])                返回给定组的匹配项的开始位置
# end([group])                  返回给定组的匹配项的结束位置
# span([group])                 返回一个组的开始和结束位置

m = re.match(r'www\.(.*)\..{3}', 'www.python.org')
# 输出：python
print(m.group(1))
# 输出：4
print(m.start(1))

# 非贪婪模式: 在重复运算符后面加上'?'

# eval(): 计算字符串值 ------------------------------------------------------------------------------
# exec(): 执行字符串代码 ----------------------------------------------------------------------------
# 范围内执行: 不影响外部程序, 常用于执行模板代码 ------------------------------------------------------
scope = {}
print(eval('2+3', scope))

command_str = '''
for i in range(5):
    print("iter index: %d" % i)
'''
exec(command_str, scope)




