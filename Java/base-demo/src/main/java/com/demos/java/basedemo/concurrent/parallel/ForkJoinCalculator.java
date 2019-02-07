package com.demos.java.basedemo.concurrent.parallel;

import java.util.concurrent.RecursiveTask;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/4/23
 */
public class ForkJoinCalculator extends RecursiveTask<Double> {

    public static final long THRESHOLD = 10000l;

    private final SequentialCalculator sequentialCalculator;
    private final double[] numbers;
    private final int start;
    private final int end;

    public ForkJoinCalculator(double[] numbers, SequentialCalculator sequentialCalculator) {
        this(numbers, 0, numbers.length, sequentialCalculator);
    }

    private ForkJoinCalculator(double[] numbers, int start, int end, SequentialCalculator
            sequentialCalculator) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
        this.sequentialCalculator = sequentialCalculator;
    }

    @Override
    protected Double compute() {
        int length = end - start;
        if (length <= THRESHOLD) {
            return sequentialCalculator.compute(numbers, start, end);
        }
        ForkJoinCalculator leftTask = new ForkJoinCalculator(numbers, start, start + length / 2, sequentialCalculator);
        ForkJoinCalculator rightTask = new ForkJoinCalculator(numbers, start + length / 2, end, sequentialCalculator);
//        invokeAll(leftTask, rightTask);
//        Double leftResult = leftTask.join();
//        Double rightResult = rightTask.join();
        leftTask.fork(); // 新线程中执行
        Double rightResult = rightTask.compute(); // 当前线程执行
        Double leftResult = leftTask.join();
        return leftResult + rightResult;
    }

}
