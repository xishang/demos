# encoding:utf-8


# 定义Person类
class Person(object):

    # __init__(): 构造方法，创建对象时调用 --------------------------------------------------------------
    def __init__(self, name):
        self.name = name

    # __del__(): 析构方法，垃圾回收之前调用（不可知） ----------------------------------------------------
    def __del__(self):
        print("Person '" + self.name + "' is collected")

    def get_name(self):
        return self.name

    def perform(self):
        print(self.name + " is performing")

# 创建对象时传入构造参数
james = Person("James")
# 输出：James
print(james.get_name())


# 定义Teacher类，继承Person
class Teacher(Person):

    # 类Teacher未定义自己的构造函数，可以复用父类Person的__init__() ----------------------------------------

    def talk(self):
        print("My name is '" + self.name + "', I am a teacher")

    def teach(self, lesson):
        print(self.name + " is teaching " + lesson)

mike = Teacher("mike")
# 输出：mike is teaching History
mike.teach("History")


# 定义Student类，继承Person
class Student(Person):

    def talk(self):
        print("My name is '" + self.name + "', I am a student")


# 定义Researcher类，继承Teacher和Student
# python多继承: 先继承的类会覆盖后继承的类的相同方法 ----------------------------------------------------
class Researcher(Teacher, Student):
    # 不定义新方法
    pass

philip = Researcher("philip")
# 输出：My name is 'philip', I am a teacher
philip.talk()


# 定义类ComputerResearcher类，继承Researcher
class ComputerResearcher(Researcher):

    # 定义构造方法，此时需要主动调用父类的构造方法 -------------------------------------------------------
    def __init__(self, *args):
        # super(): 查找所有超类(以及超类的超类)的特性，知道找到为止，否则抛出AttributeError ---------------
        super().__init__(*args)
        super().perform()

tony = ComputerResearcher("tony")

# 常用类型判断函数： -----------------------------------------------------------------------------------
# isinstance(object, class)     对象是否是类的实例
# issubclass(A, B)              类A是否是B的子类
# type(object)                  返回对象的类型

# 输出：True
print(isinstance(tony, Teacher))
# 输出：<class '__main__.ComputerResearcher'>
print(type(tony))

# python的多态性：基于行为，遵循规则即可，而不是基于超类或者接口 ------------------------------------------

# 基本的序列和映射规则 ---------------------------------------------------------------------------------
# __len__(self):                    len(s)          返回集合中项目的数量
# __getitem__(self, key):           s[key]          返回给定键key对应的值
# __setitem__(self, key, value):    s[key] = value  设置键key的值为value
# __delitem__(self, key):           del s[key]      删除键key对应的项


# 定义MyList，继承list，只能使用0~n访问:
class MyList(object):

    def __init__(self, size=10, user_generator=False):
        self.size = size
        self.user_generator = user_generator
        self.items = {}

    def check(self, key):
        if key < 0 or key >= self.size:
            raise IndexError
        return True

    def __getitem__(self, key):
        if self.check(key):
            return self.items[key]
        return None

    def __setitem__(self, key, value):
        if self.check(key):
            self.items[key] = value

    def __delitem__(self, key):
        if self.check(key):
            del self.items[key]

    # __iter__(): 迭代器方法，返回一个迭代器，实现了该方法的对象可以用于'for in'语句 -------------------------
    def __iter__(self):
        if self.user_generator:
            return self.generator()
        else:
            self.pos = 0
            return self

    # 生成器：返回一个迭代器，每次请求一个值时就会执行代码，直到遇到'yield'或'return'语句 ---------------------
    def generator(self):
        for key in self.items:
            yield self.items[key]

    # __next__(): 迭代方法，实现了该方法的对象就是迭代器 ----------------------------------------------------
    def __next__(self):
        while self.pos < self.size:
            if self.pos in self.items.keys():
                item = self.items[self.pos]
                self.pos += 1
                return item
            self.pos += 1
        # StopIteration: 迭代器没有值返回时抛出 ------------------------------------------------------------
        raise StopIteration

my_list = MyList(5, True)
my_list[0] = 3
my_list[1] = 4
my_list[0] = 2
# 输出：2
print(my_list[0])
# 输出：
#   2
#   4
for list_item in my_list:
    print(list_item)
# my_list[6] = 1


def conflict(state, next_value):
    num_len = len(state)
    for i in range(num_len):
        # 平行(0)或对角线(num_len - i)
        if (abs(state[i] - next_value)) in (0, num_len - i):
            return True
    return False


# 八皇后问题：生成器解法
def queens(num=8, state=()):
    for pos in range(num):
        if not conflict(state, pos):
            if len(state) == num - 1:
                yield (pos,)
            else:
                for result in queens(num, state + (pos,)):
                    yield (pos,) + result

for solution in list(queens(4)):
    print(solution)

