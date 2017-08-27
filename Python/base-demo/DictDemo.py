# encoding:utf-8

import copy

# 字典：python唯一内建的映射类型

# dict(): 创建字典 --------------------------------------------------------------------
dicDemo = dict([('name', 'sam'), ('age', 24)])
# 输出：{'age': 24, 'name': 'sam'}
print(dicDemo)
# 直接通过映射的形式
dictDemo = {'name': 'sam', 'age': 24}
print(dicDemo)
# 基本操作
# len(): 返回字典中键值对的数量 ---------------------------------------------------------
# 输出：2
print(len(dicDemo))
# d[k]: 根据键'k'取出对应的值 -----------------------------------------------------------
# d[k] = v: 将值v关联到键k上
# del d[k]: 删除键为k的值
# k in d: 键k是否在字典d中
dicDemo['phone'] = '123'
# 输出：{'phone': '123', 'age': 24, 'name': 'sam'}
print(dicDemo)
# 格式化字符串 -------------------------------------------------------------------------
tempStr = 'this is %(name)s, %(age)d years old, contact with %(phone)s'
# 输出：this is sam, 24 years old, contact with 123
print(tempStr % dicDemo)
# clear(): 清楚所有的项
dicDemo.clear()
# 输出：{}
print(dicDemo)
# copy(): 复制一个具有相同键值对的新字典(浅复制，对新字典中值得修改会影响原字典) ------------
dict1 = {'no': 1, 'person': {'name': 'sam'}}
dict2 = dict1.copy()
# 输出：{'person': {'name': 'sam'}, 'no': 1}
print(dict2)
dict2['person']['name'] = 'james'
# dict2关于person的改动影响了dict1
# 输出：{'person': {'name': 'james'}, 'no': 1}
print(dict1)
# copy.deepcopy(): 深度复制，新字典值改变不影响原字典 -------------------------------------
dict1 = {'no': 1, 'person': {'name': 'sam'}}
dict3 = copy.deepcopy(dict1)
dict3['person']['name'] = 'james'
# 输出：{'person': {'name': 'sam'}, 'no': 1}
print(dict1)
# fromkeys(): 使用给定的键创建新的字典，若提供了值，则新字典所有键的值均为该值，否则均为None --
dictDemo = {}.fromkeys(['name', 'address'], 'unknown')
# 输出：{'name': 'unknown', 'address': 'unknown'}
print(dictDemo)
dictDemo = {}.fromkeys(['name', 'address'])
# 输出：{'name': None, 'address': None}
print(dictDemo)
# get(): 字典取值(键不存在时返回None) -----------------------------------------------------
dictDemo = {}
# 输出：None
print(dictDemo.get('name'))
# items(): 将字典中所有项以列表方式返回 ----------------------------------------------------
# iteritems(): 返回字典的迭代器对象
dictDemo = {'name': 'sam', 'age': 24}
# 输出：[('age', 24), ('name', 'sam')]
print(dictDemo.items())
# keys(): 将字典中所有的键以列表方式返回 ---------------------------------------------------
# iterkeys(): 返回键的迭代器
dictDemo = {'name': 'sam', 'age': 24}
# 输出：['age', 'name']
print(dictDemo.keys())
# values(): 将字典中所有的值以列表方式返回 -------------------------------------------------
# itervalues(): 返回值得迭代器
dictDemo = {'name': 'sam', 'age': 24}
# 输出：[24, 'sam']
print(dictDemo.values())
# pop(): 返回给定键的值，并将该键值对从字典移除 ---------------------------------------------
dictDemo = {'name': 'sam', 'age': 24}
name = dictDemo.pop('name')
# 输出：{'age': 24}
print(dictDemo)
# 输出：sam
print(name)
# popitem(): 随机返回一个值，并删除该键值对 -------------------------------------------------
# setdefault(): 获取给定key对应的值 --------------------------------------------------------
# 若不存在则返回默认值，并设置该key的值为默认值(不传默认值则设置None)
dictDemo = {'name': 'sam', 'age': 24}
address = dictDemo.setdefault('address', 'shenzhen')
# 输出：{'age': 24, 'name': 'sam', 'address': 'shenzhen'}
print(dictDemo)
# 输出：shenzhen
print(address)
phone = dictDemo.setdefault('phone')
# 输出：{'phone': None, 'age': 24, 'name': 'sam', 'address': 'shenzhen'}
print(dictDemo)
# update(): 使用一个字典更新另一个字典(覆盖原项，添加新项) -----------------------------------
dictDemo = {'name': 'sam', 'age': 24}
updateDict = {'name': 'james', 'address': 'shenzhen'}
dictDemo.update(updateDict)
# 输出：{'age': 24, 'name': 'james', 'address': 'shenzhen'}
print(dictDemo)

