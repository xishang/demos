package com.demos.java.basedemo.concurrent;

import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/17
 * <p>
 * JDK关于RecursiveAction的例子: 排序
 */
public class SortTask extends RecursiveAction {

    private static final int THRESHOLD = 1000;

    private final long[] array;
    private final int lo, hi;

    public SortTask(long[] array, int lo, int hi) {
        this.array = array;
        this.lo = lo;
        this.hi = hi;
    }

    public SortTask(long[] array) {
        this(array, 0, array.length);
    }

    @Override
    protected void compute() {
        if (hi - lo < THRESHOLD)
            sortSequentially(lo, hi);
        else {
            int mid = (lo + hi) >>> 1;
            invokeAll(new SortTask(array, lo, mid), new SortTask(array, mid, hi));
            merge(lo, mid, hi);
        }
    }

    private void sortSequentially(int lo, int hi) {
        Arrays.sort(array, lo, hi);
    }

    private void merge(int lo, int mid, int hi) {
        long[] buf = Arrays.copyOfRange(array, lo, mid);
        for (int i = 0, j = lo, k = mid; i < buf.length; j++) {
            array[j] = (k == hi || buf[i] < array[k]) ? buf[i++] : array[k++];
        }
    }

}
