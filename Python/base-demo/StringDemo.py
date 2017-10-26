# encoding:utf-8

import string

# python字符串Demo

# 格式化字符：%[flag][width][.deg]type
# %: 标记转换说明符
# flag: 转换标志(可选)，'-'表示左对齐，'+'表示转换值加上正负号，' '表示正数之前保留空格，'0'表示转换值位数不够则用0填充
# width: 转换后字符串宽度(可选)
# .deg: 转换值为实数时的小数点位数(可选)
# type: 转换类型(详见下表)
# [转换类型]        [含义]
# [ d, i ]         [带符号的十进制整数]
# [ o ]            [不带符号的八进制]
# [ u ]            [不带符号的十进制]
# [ x ]            [不带符号的十六进制(小写)]
# [ X ]            [不带符号的十六进制(大写)]
# [ e ]            [科学计数法表示的浮点数(小写)]
# [ E ]            [科学计数法表示的浮点数(大写)]
# [ f, F ]         [十进制浮点数]
# [ g ]            [指数大于-4或者小于精度则和e相同，否则和f相同]
# [ G ]            [指数大于-4或者小于精度则和E相同，否则和F相同]
# [ C ]            [单字符]
# [ r ]            [字符串(使用repr转换任意python对象)]
# [ s ]            [字符串(使用str转换任意python对象)]

formatStr = 'dL=%-+5d, dR=%05d, end'
rep = (567, 321)
# 输出：dL=+567 , dR=00321, end
print(formatStr % rep)
formatStr = 'f=%6.3f, s=%.4f'
rep = (1.235, 1.235)
# 输出：f= 1.235, s=1.2350
print(formatStr % rep)

# string常量[2.x]
# string.digits             包含数字0~9的字符串
# string.ascii_letters      包含所有字母(大写和小写)的字符串
# string.ascii_lowercase    包含所有小写字母的字符串
# string.ascii_uppercase    包含所有大写字母的字符串
# string.printable          包含所有可打印字符的字符串
# string.punctuation        包含所有标点的字符串

# 输出：0123456789
print(string.digits)
# 输出：abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ
print(string.ascii_letters)

# find(): 查询子串并返回子串最左端索引，没有找到则返回-1 ----------------------------------------
longStr = 'this is a python class'
# 输出：2
print(longStr.find('is'))
# join(): 连接序列中的字符 --------------------------------------------------------------------
strArr = ('a', 'b', 'c')
# 输出：a-b-c
print("-".join(strArr))
# split(): 将字符串分割成列表
longStr = "hello-world"
# 输出：['hello', 'world']
print(longStr.split("-"))
# lower(): 返回小写字符串 ---------------------------------------------------------------------
# islower(): 字符串是否全部小写
# upper(): 返回大写字符串
# isupper(): 字符串是否全部大写
strMix = 'heLLO'
# 输出：False
print(strMix.islower())
# replace(): 替换字符串 -----------------------------------------------------------------------
longStr = 'This is a iis or iso'
# 输出：Th# # a i# or #o
print(longStr.replace("is", "#"))
# strip(): 去掉字符串两端的空格
# lstrip(): 去掉字符串左端的空格
# rstrip(): 去掉字符串右端的空格
strDemo = ' hello, world  '
# 输出：--hello, world--
print('--'+strDemo.strip()+'--')

