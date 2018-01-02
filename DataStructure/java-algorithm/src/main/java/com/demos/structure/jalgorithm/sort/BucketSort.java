package com.demos.structure.jalgorithm.sort;

import java.util.Arrays;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/15
 * <p>
 * 桶排序: 自然数排序, 非常快, 非常耗空间
 * 基本思想: 新建一个 [Max(array)+1] 长度且值都为0的数组, 称为空桶, 将待排数组中的数字作为桶的索引, 该处的值加1, 然后遍历桶, 取出非0的索引值
 */
public class BucketSort {

    public static int[] sort(int[] array) {
        if (array.length > 1) {
            array = bucketSort(array);
        }
        return array;
    }

    /**
     * 桶排序
     *
     * @param array
     * @return
     */
    public static int[] bucketSort(int[] array) {
        int maxNum = getMax(array);
        // bucket(桶)的长度为(maxNum+1), 即索引为: 0 ~ maxNum
        int bucketLen = maxNum + 1;
        int[] bucket = new int[bucketLen];
        for (int item : array) {
            bucket[item]++;
        }
        int index = 0;
        for (int i = 0; i < bucket.length; i++) {
            while (bucket[i]-- > 0) {
                array[index++] = i;
            }
        }
        return array;
    }

    /**
     * 取出数组中的最大值
     *
     * @param array
     * @return
     */
    private static int getMax(int[] array) {
        int max = 0;
        for (int item : array) {
            if (max < item) {
                max = item;
            }
        }
        return max;
    }

    public static void main(String[] args) {
        int[] array = new int[]{2, 1, 5, 10, 3, 6, 2, 8, 4, 9, 12, 5};
        array = BucketSort.sort(array);
        System.out.println(Arrays.toString(array));
    }

}
