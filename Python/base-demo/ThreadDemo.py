# encoding:utf-8

# Python线程

import threading
import time

exitFlag = 0

# threading.Thread: 线程类 ----------------------------------------------------------------------------------
# start(): 启动线程活动
# join([time]): 等待至线程中止, 不调用该方法时会直接执行后面的程序
# isAlive(): 线程是否活动的
# getName(): 返回线程名
# setName(): 设置线程名

# threading.Lock(): 线程锁 ----------------------------------------------------------------------------------
# acquire(): 获取锁
# release(): 释放锁

thread_lock = threading.Lock()


class MyThread(threading.Thread):
    def __init__(self, threadID, name, delay):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.delay = delay

    def run(self):
        # 获取锁, 用于线程同步
        thread_lock.acquire()
        print("开始线程：" + self.name)
        print_time(self.name, self.delay, 5)
        print("退出线程：" + self.name)
        # 释放锁, 开启下一个线程
        thread_lock.release()


def print_time(threadName, delay, counter):
    while counter:
        if exitFlag:
            threadName.exit()
        time.sleep(delay)
        print("%s: %s" % (threadName, time.ctime(time.time())))
        counter -= 1


# 创建新线程
thread1 = MyThread(1, "Thread-1", 1)
thread2 = MyThread(2, "Thread-2", 2)

# 开启新线程
thread1.start()
thread2.start()
thread1.join()
thread2.join()
print("退出主线程")
