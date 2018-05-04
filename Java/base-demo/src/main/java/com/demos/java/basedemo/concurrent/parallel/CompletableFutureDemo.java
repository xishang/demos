package com.demos.java.basedemo.concurrent.parallel;

import java.util.concurrent.*;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/5/4
 */
public class CompletableFutureDemo {

    /**
     * CompletableFuture: 异步编排工具, 完美支持lambda
     */
    public static void completableFuture() {
        /* ---------- JDK1.5新增的Future接口只能以阻塞(get)或轮询(isDone)的方式获取结果 */
        Future<String> future = Executors.newSingleThreadExecutor().submit(() -> "future callable");
        try {
            System.out.println("future get: " + future.get());
        } catch (Exception e) {
        }
        /* ---------- CompletableFuture简单示例: completedFuture()直接提供结果, thenAccept()同步消费结果 */
        CompletableFuture.completedFuture("direct string!").thenAccept(System.out::println);
        /* ---------- supplyAsync: 构造一个简单的异步计算, 也可指定Executor执行 */
        // 若不指定则使用ForkJoinPool.commonPool(), 该线程池会在程序结束时自动终结, 若要等待任务完成需调用ForkJoinPool.awaitQuiescence()
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "supply something!");
        /* ---------- thenAcceptAsync: 异步消费结果(使用ForkJoinPool) */
        completableFuture.thenAcceptAsync(str -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            System.out.println("thenAcceptAsync: " + str);
        });
        System.out.println("main thread!");
        /* ---------- thenApplyAsync/thenApply: 流程编排, 输出作为下一个计算的输入 */
        CompletableFuture.supplyAsync(() -> {
            System.out.println("compose supply: 1");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            return 1;
        }).thenApplyAsync(r -> { // thenApplyAsync: Integer -> Double
            System.out.println("thenApplyAsync: r=" + r);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            return r * 1.0D;
        }).thenAccept(r -> { // thenAccept: 消费上一个计算的输出
            System.out.println("thenAccept: r=" + r);
            throw new NullPointerException();
        }).exceptionally(t -> { // exceptionally: 错误处理, 这里可以进行恢复(即: 降级处理)
            System.out.println("异常处理(exceptionally): t=" + t.getClass().getName());
            return null;
        });
        /* ---------- allOf/anyOf */
        CompletableFuture<Double> cf1 = CompletableFuture.completedFuture(1.0D);
        CompletableFuture<Double> cf2 = CompletableFuture.completedFuture(2.0D);
        CompletableFuture<Double> cf3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("get cf3 should after 3 seconds!");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            return 3.0D;
        });
        /* ---------- whenComplete: 完成时进行消费, 也可使用thenAccept/thenAcceptAsync进行消费 */
        CompletableFuture.allOf(cf1, cf2, cf3).whenComplete((r, t) -> {
            System.out.println("allOf whenComplete!");
            try {
                System.out.println("average: " + (cf1.get() + cf2.get() + cf3.get()) / 3);
            } catch (Exception e) {
            }
        });

        CompletableFuture<Integer> comb1 = intFuture(3, 3);
        CompletableFuture<Integer> comb2 = intFuture(2, 2);
        /* ---------- thenCombine: 合并任务(有返回值), cf + cf2 -> cf3 */
        comb1.thenCombine(comb2, (u, v) -> (u + v) / 2.0D).thenAccept(d -> {
            System.out.println("average value = " + d);
        });
        /* ---------- thenAcceptBoth: 合并消费(无返回值) */
        CompletableFuture<Integer> both1 = intFuture(3, 3);
        CompletableFuture<Integer> both2 = intFuture(2, 2);
        both1.thenAcceptBoth(both2, (u, v) -> {
            System.out.println("first value = " + u + ", second value = " + v);
        });

        /* ---------- thenCompose: flatMap(扁平化), value +(传入) CompletableFuture -> newValue */
        // 与thenApply区别: thenApply传入Function, thenCompose传入Function得到CompletableFuture并执行
        CompletableFuture<CompletableFuture<Double>> applyMap = intFuture(2, 2).thenApply(CompletableFutureDemo::twice);
        CompletableFuture<Double> composeFlatMap = intFuture(3, 3).thenCompose(CompletableFutureDemo::twice);
        try {
            System.out.println("apply get get = " + applyMap.get().get());
            System.out.println("compose get = " + composeFlatMap.get());
        } catch (Exception e) {
        }

        /* ---------- acceptEither: 消费先返回的Future(无返回值), applyToEither有返回值 */
        CompletableFuture<Integer> either1 = intFuture(2, 2);
        CompletableFuture<Integer> either2 = intFuture(3, 3);
        either1.acceptEither(either2, i -> {
            System.out.println("acceptEither fast num = " + i);
        });

        /* ---------- 退出程序前调用awaitQuiescence等待ForkJoinPool中的任务完成 */
        ForkJoinPool.commonPool().awaitQuiescence(5, TimeUnit.SECONDS);
    }

    private static CompletableFuture<Integer> intFuture(int num, int wait) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.printf("num [%d] after %d seconds\n", num, wait);
            try {
                Thread.sleep(wait * 1000);
            } catch (InterruptedException e) {
            }
            return num;
        });
    }

    private static CompletableFuture<Double> twice(int num) {
        return CompletableFuture.supplyAsync(() -> 2.0D * num);
    }

    public static void main(String[] args) {
        completableFuture();
    }

}
