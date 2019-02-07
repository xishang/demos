package com.demos.java.jdkanalyzer.tomcat;

import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapper;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/26
 *
 * Acceptor接收socket:
 * -> 1.循环直到接收到`shutdown`命令
 * -> 2.调用LimitLatch.countUpOrAwait()获取连接许可, 如果达到`maxConnections`连接限制则阻塞
 * -> 3.接受socket连接: ServerSocket.accept()
 * -> 4.如果连接失败, 释放一个许可(LimitLatch), 并阻塞一段时间(errorDelay), 以避免cpu空转且打印大量无用的log
 * -> 5.配置socket属性, 如: 接受缓冲区大小, 发送缓冲区大小, 是否keep-alive, 超时时间等
 * -> 6.将socket封装到SocketProcessor, 并加入线程池等待处理
 * -> 7.若处理失败, 释放一个许可, 并立即关闭socket
 */
public class JIoEndPointAnalyzer {

    /**
     * 后台线程, 监听端口接受Socket连接请求
     */
    protected class Acceptor extends AbstractEndpoint.Acceptor {

        @Override
        public void run() {

            int errorDelay = 0;

            // 循环直到接收到`shutdown`命令
            while (running) {

                try {
                    // 调用LimitLatch.countUpOrAwait()获取连接许可, 如果达到`maxConnections`连接限制则阻塞
                    countUpOrAwaitConnection();

                    Socket socket = null;
                    try {
                        // 接受socket连接: ServerSocket.accept()
                        socket = serverSocketFactory.acceptSocket(serverSocket);
                    } catch (IOException ioe) {
                        // 连接失败, 释放一个许可: LimitLatch
                        countDownConnection();
                        // 阻塞一段时间(errorDelay), 以避免cpu空转且打印大量无用的log: 如连接数不够用
                        errorDelay = handleExceptionWithDelay(errorDelay);
                        // re-throw
                        throw ioe;
                    }
                    // 连接成功, 将失败阻塞时间重置为0
                    errorDelay = 0;

                    // 配置socket属性, 如: 接受缓冲区大小, 发送缓冲区大小, 是否keep-alive, 超时时间等
                    if (running && !paused && setSocketOptions(socket)) {
                        // 将socket封装到SocketProcessor, 并加入线程池等待处理
                        if (!processSocket(socket)) {
                            // 处理失败, 释放一个许可
                            countDownConnection();
                            // 立即关闭socket
                            closeSocket(socket);
                        }
                    } else {
                        // 配置失败, 释放一个许可
                        countDownConnection();
                        // 立即关闭socket
                        closeSocket(socket);
                    }
                } catch (IOException x) {
                    if (running) {
                        log.error(sm.getString("endpoint.accept.fail"), x);
                    }
                } catch (NullPointerException npe) {
                    if (running) {
                        log.error(sm.getString("endpoint.accept.fail"), npe);
                    }
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    log.error(sm.getString("endpoint.accept.fail"), t);
                }
            }
        }
    }

    /**
     * 处理新的socket, 放入线程池等待处理
     */
    protected boolean processSocket(Socket socket) {
        // Process the request from this socket
        try {
            SocketWrapper<Socket> wrapper = new SocketWrapper<>(socket);
            // 允许的最大长连接数
            wrapper.setKeepAliveLeft(getMaxKeepAliveRequests());
            // 是否是SSL协议
            wrapper.setSecure(isSSLEnabled());
            // During shutdown, executor may be null - avoid NPE
            if (!running) {
                return false;
            }
            // 创建SocketProcessor并加入线程池等待处理
            getExecutor().execute(new SocketProcessor(wrapper));
        } catch (RejectedExecutionException x) {
            log.warn("Socket processing request was rejected for:" + socket, x);
            return false;
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            // This means we got an OOM or similar creating a thread, or that
            // the pool and its queue are full
            log.error(sm.getString("endpoint.process.fail"), t);
            return false;
        }
        return true;
    }

}
