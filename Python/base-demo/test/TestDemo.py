# encoding:utf-8

# Python单元测试

import unittest

from test import HelloTest


# unittest.TestCase: 测试类
# unittest.main(): 运行测试, 实例化TestCase的子类, 并运行所有以'test'开头的方法
#
# failUnless(expr[, msg]): 表达式为False则失败, 可以给出提示信息
# assertEqual(x, y[, msg]): 两个值不同则失败

class HelloTestCase(unittest.TestCase):
    def testPlus(self):
        total = HelloTest.plus(2, 3)
        self.assertEqual(total, 5)

    if __name__ == '__main__':
        # 运行测试用例
        unittest.main()
