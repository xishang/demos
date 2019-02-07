package com.demos.java.basedemo.concurrent.parallel;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/4/23
 */
public class VarianceCompute {

    public static void main(String[] args) {
        Random random = new Random(10);
        double[] population = new double[100000000];
        for (int i = 0; i < population.length; i++) {
            population[i] = random.nextInt(100);
        }
        long time1 = System.currentTimeMillis();
        System.out.printf("varianceDirect = %f, time = %d\n", varianceDirect(population), System.currentTimeMillis() - time1);
        long time2 = System.currentTimeMillis();
        System.out.printf("varianceForkJoin = %f, time = %d\n", varianceForkJoin(population), System.currentTimeMillis() - time2);
        long time3 = System.currentTimeMillis();
        System.out.printf("varianceStream = %f, time = %d\n", varianceStream(population), System.currentTimeMillis() - time3);
    }

    /**
     * 过程式编程计算方差
     *
     * @param population
     * @return
     */
    public static double varianceDirect(double[] population) {
        double average = 0.0;
        for (double p : population) {
            average += p;
        }
        average /= population.length;

        double variance = 0.0;
        for (double p : population) {
            variance += (p - average) * (p - average);
        }
        return variance / population.length;
    }

    /**
     * fork/join框架计算方差
     *
     * @param population
     * @return
     */
    public static double varianceForkJoin(double[] population) {
        final ForkJoinPool forkJoinPool = new ForkJoinPool();
        double total = forkJoinPool.invoke(new ForkJoinCalculator
                (population, (numbers, start, end) -> {
                    double sum = 0;
                    for (int i = start; i < end; i++) {
                        sum += numbers[i];
                    }
                    return sum;
                }));
        final double average = total / population.length;
        double variance = forkJoinPool.invoke(new ForkJoinCalculator
                (population, (numbers, start, end) -> {
                    double sum = 0;
                    for (int i = start; i < end; i++) {
                        sum += (numbers[i] - average) * (numbers[i] - average);
                    }
                    return sum;
                }));
        return variance / population.length;
    }

    /**
     * stream api计算方差
     *
     * @param population
     * @return
     */
    public static double varianceStream(double[] population) {
        double average = Arrays.stream(population).parallel().average().orElse(0.0);
        double variance = Arrays.stream(population).parallel()
                .map(p -> (p - average) * (p - average))
                .sum() / population.length;
        return variance;
    }

}
