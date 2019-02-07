package com.demo.framework.rxjava;

import rx.Observable;
import rx.Observer;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/11
 */
public class Main {

    public static void main(String[] args) {
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("onNext: " + integer);
            }
        };
        Observable<Integer> observable = Observable.just(1, 2, 4);
        observable.subscribe(observer);
    }

}
