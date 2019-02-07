package com.demos.java.jdkanalyzer.tomcat;

import org.apache.tomcat.util.net.SocketStatus;
import org.apache.tomcat.util.net.SocketWrapper;

import java.net.Socket;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/26
 */
public class SocketProcessorAnalyzer implements Runnable {

    protected SocketWrapper<Socket> socket = null;
    protected SocketStatus status = null;

    Handler handler;

    @Override
    public void run() {
        boolean launch = false;
        synchronized (socket) {
            try {
                // state初始值: OPEN, state用于指导处理socket
                SocketState state = SocketState.OPEN;
                // SSL握手前的钩子方法
                handler.beforeHandshake(socket);
                try {
                    // SSL握手
                    serverSocketFactory.handshake(socket.getSocket());
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    if (log.isDebugEnabled()) {
                        log.debug(sm.getString("endpoint.err.handshake"), t);
                    }
                    // Tell to close the socket
                    state = SocketState.CLOSED;
                }

                // 若state不是`CLOSED`: 使用handler处理socket
                if ((state != SocketState.CLOSED)) {
                    if (status == null) {
                        state = handler.process(socket, SocketStatus.OPEN_READ);
                    } else {
                        state = handler.process(socket, status);
                    }
                }
                // 若state为CLOSED, 关闭socket
                if (state == SocketState.CLOSED) {
                    // Close socket
                    if (log.isTraceEnabled()) {
                        log.trace("Closing socket:" + socket);
                    }
                    // 释放一个连接许可
                    countDownConnection();
                    // 关闭socket
                    try {
                        socket.getSocket().close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
                // 需要保持长连接
                else if (state == SocketState.OPEN ||
                        state == SocketState.UPGRADING ||
                        state == SocketState.UPGRADED) {
                    socket.setKeptAlive(true);
                    socket.access();
                    launch = true;
                } else if (state == SocketState.LONG) {
                    socket.access();
                    waitingRequests.add(socket);
                }
            } finally {
                // 长连接: 将socket再次加入线程池等待处理
                if (launch) {
                    try {
                        getExecutor().execute(new SocketProcessor(socket, SocketStatus.OPEN_READ));
                    } catch (RejectedExecutionException x) {
                        // 加入线程池失败: 使用handler处理连接断开, 然后释放一个连接许可
                        log.warn("Socket reprocessing request was rejected for:" + socket, x);
                        try {
                            //unable to handle connection at this time
                            handler.process(socket, SocketStatus.DISCONNECT);
                        } finally {
                            countDownConnection();
                        }


                    } catch (NullPointerException npe) {
                        if (running) {
                            log.error(sm.getString("endpoint.launch.fail"),
                                    npe);
                        }
                    }
                }
            }
        }
        socket = null;
        // Finish up this request
    }

    /**
     * Socket处理器
     */
    class Handler<S> {

        public SocketState process(SocketWrapper<S> wrapper, SocketStatus status) {
            if (wrapper == null) {
                // Nothing to do. Socket has been closed.
                return SocketState.CLOSED;
            }

            S socket = wrapper.getSocket();
            if (socket == null) {
                // Nothing to do. Socket has been closed.
                return SocketState.CLOSED;
            }

            // 从`socket->processor`map中取出socket对应的processor
            Processor<S> processor = connections.get(socket);
            // 如果处理状态为DISCONNECT, 且processor为null, 则可以关闭socket
            if (status == SocketStatus.DISCONNECT && processor == null) {
                // Nothing to do. Endpoint requested a close and there is no
                // longer a processor associated with this socket.
                return SocketState.CLOSED;
            }

            wrapper.setAsync(false);
            // 标记当前线程正在处理一个连接
            ContainerThreadMarker.set();

            try {
                if (processor == null) {
                    // 从processor栈中取出一个复用
                    processor = recycledProcessors.pop();
                }
                if (processor == null) {
                    // BIO, 创建Http11Processor
                    processor = createProcessor();
                }

                // 配置SSL支持
                initSsl(wrapper, processor);

                // 根据传入的status进行处理, 如: Processor.process()
                SocketState state = SocketState.CLOSED;
                Iterator<DispatchType> dispatches = null;
                do {
                    if (dispatches != null) {
                        // 将socket->processor存入map
                        connections.put(socket, processor);
                        DispatchType nextDispatch = dispatches.next();
                        if (processor.isUpgrade()) {
                            state = processor.upgradeDispatch(
                                    nextDispatch.getSocketStatus());
                        } else {
                            state = processor.asyncDispatch(
                                    nextDispatch.getSocketStatus());
                        }
                    } else if (processor.isComet()) {
                        state = processor.event(status);
                    } else if (processor.isUpgrade()) {
                        state = processor.upgradeDispatch(status);
                    } else if (status == SocketStatus.DISCONNECT) {
                        // Comet and upgrade need to see DISCONNECT but the
                        // others don't. NO-OP and let socket close.
                    } else if (processor.isAsync() || state == SocketState.ASYNC_END) {
                        state = processor.asyncDispatch(status);
                        if (state == SocketState.OPEN) {
                            // release() won't get called so in case this request
                            // takes a long time to process, remove the socket from
                            // the waiting requests now else the async timeout will
                            // fire
                            getProtocol().endpoint.removeWaitingRequest(wrapper);
                            // There may be pipe-lined data to read. If the data
                            // isn't processed now, execution will exit this
                            // loop and call release() which will recycle the
                            // processor (and input buffer) deleting any
                            // pipe-lined data. To avoid this, process it now.
                            state = processor.process(wrapper);
                        }
                    } else if (status == SocketStatus.OPEN_WRITE) {
                        // Extra write event likely after async, ignore
                        state = SocketState.LONG;
                    } else {
                        state = processor.process(wrapper);
                    }

                    if (state != SocketState.CLOSED && processor.isAsync()) {
                        state = processor.asyncPostProcess();
                    }

                    if (state == SocketState.UPGRADING) {
                        // Get the HTTP upgrade handler
                        UpgradeToken upgradeToken = processor.getUpgradeToken();
                        HttpUpgradeHandler httpUpgradeHandler = upgradeToken.getHttpUpgradeHandler();
                        // Retrieve leftover input
                        ByteBuffer leftoverInput = processor.getLeftoverInput();
                        // Release the Http11 processor to be re-used
                        release(wrapper, processor, false, false);
                        // Create the upgrade processor
                        processor = createUpgradeProcessor(
                                wrapper, leftoverInput, upgradeToken);
                        // Mark the connection as upgraded
                        wrapper.setUpgraded(true);
                        // Associate with the processor with the connection
                        connections.put(socket, processor);
                        // Initialise the upgrade handler (which may trigger
                        // some IO using the new protocol which is why the lines
                        // above are necessary)
                        // This cast should be safe. If it fails the error
                        // handling for the surrounding try/catch will deal with
                        // it.
                        if (upgradeToken.getInstanceManager() == null) {
                            httpUpgradeHandler.init((WebConnection) processor);
                        } else {
                            ClassLoader oldCL = upgradeToken.getContextBind().bind(false, null);
                            try {
                                httpUpgradeHandler.init((WebConnection) processor);
                            } finally {
                                upgradeToken.getContextBind().unbind(false, oldCL);
                            }
                        }
                    }
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("Socket: [" + wrapper +
                                "], Status in: [" + status +
                                "], State out: [" + state + "]");
                    }
                    if (dispatches == null || !dispatches.hasNext()) {
                        // Only returns non-null iterator if there are
                        // dispatches to process.
                        dispatches = wrapper.getIteratorAndClearDispatches();
                    }
                } while (state == SocketState.ASYNC_END ||
                        state == SocketState.UPGRADING ||
                        dispatches != null && state != SocketState.CLOSED);

                if (state == SocketState.LONG) {
                    // In the middle of processing a request/response. Keep the
                    // socket associated with the processor. Exact requirements
                    // depend on type of long poll
                    connections.put(socket, processor);
                    longPoll(wrapper, processor);
                } else if (state == SocketState.OPEN) {
                    // In keep-alive but between requests. OK to recycle
                    // processor. Continue to poll for the next request.
                    connections.remove(socket);
                    release(wrapper, processor, false, true);
                } else if (state == SocketState.SENDFILE) {
                    // Sendfile in progress. If it fails, the socket will be
                    // closed. If it works, the socket either be added to the
                    // poller (or equivalent) to await more data or processed
                    // if there are any pipe-lined requests remaining.
                    connections.put(socket, processor);
                } else if (state == SocketState.UPGRADED) {
                    // Don't add sockets back to the poller if this was a
                    // non-blocking write otherwise the poller may trigger
                    // multiple read events which may lead to thread starvation
                    // in the connector. The write() method will add this socket
                    // to the poller if necessary.
                    if (status != SocketStatus.OPEN_WRITE) {
                        longPoll(wrapper, processor);
                    }
                } else {
                    // Connection closed. OK to recycle the processor. Upgrade
                    // processors are not recycled.
                    connections.remove(socket);
                    if (processor.isUpgrade()) {
                        UpgradeToken upgradeToken = processor.getUpgradeToken();
                        HttpUpgradeHandler httpUpgradeHandler = upgradeToken.getHttpUpgradeHandler();
                        InstanceManager instanceManager = upgradeToken.getInstanceManager();
                        if (instanceManager == null) {
                            httpUpgradeHandler.destroy();
                        } else {
                            ClassLoader oldCL = upgradeToken.getContextBind().bind(false, null);
                            try {
                                httpUpgradeHandler.destroy();
                            } finally {
                                try {
                                    instanceManager.destroyInstance(httpUpgradeHandler);
                                } catch (Throwable e) {
                                    ExceptionUtils.handleThrowable(e);
                                    getLog().error(sm.getString("abstractConnectionHandler.error"), e);
                                }
                                upgradeToken.getContextBind().unbind(false, oldCL);
                            }
                        }
                    } else {
                        release(wrapper, processor, true, false);
                    }
                }
                return state;
            } catch (java.net.SocketException e) {
                // SocketExceptions are normal
                getLog().debug(sm.getString(
                        "abstractConnectionHandler.socketexception.debug"), e);
            } catch (java.io.IOException e) {
                // IOExceptions are normal
                getLog().debug(sm.getString(
                        "abstractConnectionHandler.ioexception.debug"), e);
            }
            // Future developers: if you discover any other
            // rare-but-nonfatal exceptions, catch them here, and log as
            // above.
            catch (Throwable e) {
                ExceptionUtils.handleThrowable(e);
                // any other exception or error is odd. Here we log it
                // with "ERROR" level, so it will show up even on
                // less-than-verbose logs.
                getLog().error(
                        sm.getString("abstractConnectionHandler.error"), e);
            } finally {
                ContainerThreadMarker.clear();
            }

            // Make sure socket/processor is removed from the list of current
            // connections
            connections.remove(socket);
            // Don't try to add upgrade processors back into the pool
            if (processor != null && !processor.isUpgrade()) {
                release(wrapper, processor, true, false);
            }
            return SocketState.CLOSED;
        }

    }

}
