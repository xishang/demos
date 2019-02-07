package com.demos.java.basedemo.concurrent.parallel;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/4/23
 */
@FunctionalInterface
public interface SequentialCalculator {

    double compute(double[] numbers, int start, int end);

}
