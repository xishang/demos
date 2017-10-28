# encoding:utf-8

import pymysql

# 打开数据库连接
db = pymysql.connect('localhost', 'root', '930721', 'iyuezu')

# 使用cursor()方法创建一个游标对象
cursor = db.cursor()

sql = 'select * from users'

try:
    # 执行SQL语句
    cursor.execute(sql)
    # 获取所有记录列表
    results = cursor.fetchall()
    for row in results:
        print(row)
    # 提交到数据库执行
    # db.commit()
except Exception as e:
    print('db error:', e)
    # 发生错误时回滚
    # db.rollback()
finally:
    # 关闭数据库连接
    db.close()

