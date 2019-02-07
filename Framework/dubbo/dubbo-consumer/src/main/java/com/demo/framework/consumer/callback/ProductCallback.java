package com.demo.framework.consumer.callback;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/19
 */
public interface ProductCallback {

    /**
     * 远程调用之前的后调: oninvoke(原参数1, 原参数2, ...)
     *
     * @param productName
     */
    void oninvoke(String productName);

    /**
     * 远程调用之后的后调: onreturn(返回值, 原参数1, 原参数2, ...)
     *
     * @param resId
     * @param productName
     */
    void onreturn(Long resId, String productName);

    /**
     * 出现异常时的回调: onthrow(原参数1, 原参数2, ...)
     *
     * @param productName
     */
    void onthrow(String productName);

}
