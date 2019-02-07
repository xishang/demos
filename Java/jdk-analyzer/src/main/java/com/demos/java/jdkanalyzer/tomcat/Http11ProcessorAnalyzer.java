package com.demos.java.jdkanalyzer.tomcat;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/26
 */
public class Http11ProcessorAnalyzer<S> {

    /**
     * Process pipelined HTTP requests using the specified input and output
     * streams.
     *
     * @param socketWrapper Socket from which the HTTP requests will be read
     *               and the HTTP responses will be written.
     *
     * @throws IOException error during an I/O operation
     */
    public SocketState process(SocketWrapper<S> socketWrapper)
            throws IOException {
        RequestInfo rp = request.getRequestProcessor();
        rp.setStage(org.apache.coyote.Constants.STAGE_PARSE);

        // Setting up the I/O
        setSocketWrapper(socketWrapper);
        getInputBuffer().init(socketWrapper, endpoint);
        getOutputBuffer().init(socketWrapper, endpoint);

        // Flags
        keepAlive = true;
        comet = false;
        openSocket = false;
        sendfileInProgress = false;
        readComplete = true;
        if (endpoint.getUsePolling()) {
            keptAlive = false;
        } else {
            keptAlive = socketWrapper.isKeptAlive();
        }

        if (disableKeepAlive()) {
            socketWrapper.setKeepAliveLeft(0);
        }

        while (!getErrorState().isError() && keepAlive && !comet && !isAsync() &&
                upgradeToken == null && !endpoint.isPaused()) {

            // Parsing the request header
            try {
                setRequestLineReadTimeout();

                if (!getInputBuffer().parseRequestLine(keptAlive)) {
                    if (handleIncompleteRequestLineRead()) {
                        break;
                    }
                }

                if (endpoint.isPaused()) {
                    // 503 - Service unavailable
                    response.setStatus(503);
                    setErrorState(ErrorState.CLOSE_CLEAN, null);
                } else {
                    keptAlive = true;
                    // Set this every time in case limit has been changed via JMX
                    request.getMimeHeaders().setLimit(endpoint.getMaxHeaderCount());
                    request.getCookies().setLimit(getMaxCookieCount());
                    // Currently only NIO will ever return false here
                    if (!getInputBuffer().parseHeaders()) {
                        // We've read part of the request, don't recycle it
                        // instead associate it with the socket
                        openSocket = true;
                        readComplete = false;
                        break;
                    }
                    if (!disableUploadTimeout) {
                        setSocketTimeout(connectionUploadTimeout);
                    }
                }
            } catch (IOException e) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug(
                            sm.getString("http11processor.header.parse"), e);
                }
                setErrorState(ErrorState.CLOSE_NOW, e);
                break;
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                UserDataHelper.Mode logMode = userDataHelper.getNextMode();
                if (logMode != null) {
                    String message = sm.getString(
                            "http11processor.header.parse");
                    switch (logMode) {
                        case INFO_THEN_DEBUG:
                            message += sm.getString(
                                    "http11processor.fallToDebug");
                            //$FALL-THROUGH$
                        case INFO:
                            getLog().info(message, t);
                            break;
                        case DEBUG:
                            getLog().debug(message, t);
                    }
                }
                // 400 - Bad Request
                response.setStatus(400);
                setErrorState(ErrorState.CLOSE_CLEAN, t);
                getAdapter().log(request, response, 0);
            }

            if (!getErrorState().isError()) {
                // Setting up filters, and parse some request headers
                rp.setStage(org.apache.coyote.Constants.STAGE_PREPARE);
                try {
                    prepareRequest();
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    if (getLog().isDebugEnabled()) {
                        getLog().debug(sm.getString(
                                "http11processor.request.prepare"), t);
                    }
                    // 500 - Internal Server Error
                    response.setStatus(500);
                    setErrorState(ErrorState.CLOSE_CLEAN, t);
                    getAdapter().log(request, response, 0);
                }
            }

            if (maxKeepAliveRequests == 1) {
                keepAlive = false;
            } else if (maxKeepAliveRequests > 0 &&
                    socketWrapper.decrementKeepAlive() <= 0) {
                keepAlive = false;
            }

            // Process the request in the adapter
            if (!getErrorState().isError()) {
                try {
                    rp.setStage(org.apache.coyote.Constants.STAGE_SERVICE);
                    getAdapter().service(request, response);
                    // Handle when the response was committed before a serious
                    // error occurred.  Throwing a ServletException should both
                    // set the status to 500 and set the errorException.
                    // If we fail here, then the response is likely already
                    // committed, so we can't try and set headers.
                    if(keepAlive && !getErrorState().isError() && !isAsync() &&
                            statusDropsConnection(response.getStatus())) {
                        setErrorState(ErrorState.CLOSE_CLEAN, null);
                    }
                    setCometTimeouts(socketWrapper);
                } catch (InterruptedIOException e) {
                    setErrorState(ErrorState.CLOSE_NOW, e);
                } catch (HeadersTooLargeException e) {
                    getLog().error(sm.getString("http11processor.request.process"), e);
                    // The response should not have been committed but check it
                    // anyway to be safe
                    if (response.isCommitted()) {
                        setErrorState(ErrorState.CLOSE_NOW, e);
                    } else {
                        response.reset();
                        response.setStatus(500);
                        setErrorState(ErrorState.CLOSE_CLEAN, e);
                        response.setHeader("Connection", "close"); // TODO: Remove
                    }
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    getLog().error(sm.getString("http11processor.request.process"), t);
                    // 500 - Internal Server Error
                    response.setStatus(500);
                    setErrorState(ErrorState.CLOSE_CLEAN, t);
                    getAdapter().log(request, response, 0);
                }
            }

            // Finish the handling of the request
            rp.setStage(org.apache.coyote.Constants.STAGE_ENDINPUT);

            if (!isAsync() && !comet) {
                if (getErrorState().isError()) {
                    // If we know we are closing the connection, don't drain
                    // input. This way uploading a 100GB file doesn't tie up the
                    // thread if the servlet has rejected it.
                    getInputBuffer().setSwallowInput(false);
                } else {
                    // Need to check this again here in case the response was
                    // committed before the error that requires the connection
                    // to be closed occurred.
                    checkExpectationAndResponseStatus();
                }
                endRequest();
            }

            rp.setStage(org.apache.coyote.Constants.STAGE_ENDOUTPUT);

            // If there was an error, make sure the request is counted as
            // and error, and update the statistics counter
            if (getErrorState().isError()) {
                response.setStatus(500);
            }

            if (!isAsync() && !comet || getErrorState().isError()) {
                request.updateCounters();
                if (getErrorState().isIoAllowed()) {
                    getInputBuffer().nextRequest();
                    getOutputBuffer().nextRequest();
                }
            }

            if (!disableUploadTimeout) {
                if(endpoint.getSoTimeout() > 0) {
                    setSocketTimeout(endpoint.getSoTimeout());
                } else {
                    setSocketTimeout(0);
                }
            }

            rp.setStage(org.apache.coyote.Constants.STAGE_KEEPALIVE);

            if (breakKeepAliveLoop(socketWrapper)) {
                break;
            }
        }

        rp.setStage(org.apache.coyote.Constants.STAGE_ENDED);

        if (getErrorState().isError() || endpoint.isPaused()) {
            return SocketState.CLOSED;
        } else if (isAsync() || comet) {
            return SocketState.LONG;
        } else if (isUpgrade()) {
            return SocketState.UPGRADING;
        } else {
            if (sendfileInProgress) {
                return SocketState.SENDFILE;
            } else {
                if (openSocket) {
                    if (readComplete) {
                        return SocketState.OPEN;
                    } else {
                        return SocketState.LONG;
                    }
                } else {
                    return SocketState.CLOSED;
                }
            }
        }
    }

}
