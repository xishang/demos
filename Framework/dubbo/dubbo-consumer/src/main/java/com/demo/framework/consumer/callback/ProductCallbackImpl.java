package com.demo.framework.consumer.callback;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/19
 */
public class ProductCallbackImpl implements ProductCallback {

    @Override
    public void oninvoke(String productName) {
        System.out.println("oninvoke: productName = " + productName);
    }

    @Override
    public void onreturn(Long resId, String productName) {
        System.out.println("onreturn: productName = " + productName +" , productId = " + resId);
    }

    @Override
    public void onthrow(String productName) {
        System.out.println("onthrow: productName = " + productName);
    }

}
