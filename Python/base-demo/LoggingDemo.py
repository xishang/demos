# encoding:utf-8

# Python日志打印

import logging

logging.basicConfig(level=logging.INFO, filename=r'C:\Users\xs\Desktop\dats\log.log')

logging.debug('This is a debug message')
logging.info('This is a info message')
logging.error('This is a error message')
