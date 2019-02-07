/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/11
 * <p>
 * RxJava核心概念:
 * 1.核心类
 * -> Observable: 被观察者
 * -> Observer: 观察者
 * -> Subscriber: 扩展了观察者类
 * 2.被观察者方法:
 * -> Observable.subscribe(Observer): 订阅事件, 会把`Observer`转换成`Subscriber`来使用
 * 3.观察者方法
 * -> Observer.onNext(): 事件触发时回调
 * -> Observer.onCompleted(): 事件队列完结时回调
 * -> Observer.onError(): 发生异常时回调
 */
package com.demo.framework.rxjava;