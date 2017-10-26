# encoding:utf-8
from exception.myexception import InvalidNumberException

try:
    x = input("please input the first num:")
    x = int(x)
    if x == 0:
        # raise: 抛出异常 ---------------------------------------------------------------------
        raise InvalidNumberException
    y = input("please input the second num:")
    y = int(y)
    print("div result: ", x/y)
# except: 捕获异常 ----------------------------------------------------------------------------
except InvalidNumberException as e:
    print("InvalidNumberException:", e)
# except元组: 捕获多个异常 ---------------------------------------------------------------------
except (ZeroDivisionError, TypeError) as e:
    print("error:", e)
except Exception as e:
    print("error:", e)
# except+else: 无异常时跳转分支 ----------------------------------------------------------------
else:
    print("success!")
# finally: 正常或异常均要执行该分支 -------------------------------------------------------------
finally:
    print("end...")
