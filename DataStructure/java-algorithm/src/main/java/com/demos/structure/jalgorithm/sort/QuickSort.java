package com.demos.structure.jalgorithm.sort;

import java.util.Arrays;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/14
 * <p>
 * 快速排序: 不稳定排序
 * 基本思路: 选择一个基准元素, 将数组分成大小于基准元素的两部分, 此时基准元素在其排好序后的正确位置, 然后再用同样的方法递归进行
 * 时间复杂度: 最好: O(nlogn), 最坏: O(n^2), 平均: O(nlogn)
 * 空间复杂度: O(logn)
 */
public class QuickSort {

    public static void sort(int[] array) {
        if (array.length > 1) {
            quickSort(array, 0, array.length - 1);
        }
    }

    private static void quickSort(int[] array, int start, int end) {
        if (start < end) {
            int mid = getMiddleIndex(array, start, end);
            quickSort(array, 0, mid - 1);
            quickSort(array, mid + 1, end);
        }
    }

    /**
     * 以数组array的第一个元素为基准, 扫描数组, 将array分组
     *
     * @param array
     * @param start
     * @param end
     * @return
     */
    private static int getMiddleIndex(int[] array, int start, int end) {
        int temp = array[start];
        while (start < end) {
            while (start < end && array[end] >= temp) {
                end--;
            }
            // 将小于基准的值放到左边
            array[start] = array[end];
            while (start < end && array[start] <= temp) {
                start++;
            }
            // 将大于基准的值放到右边
            array[end] = array[start];
        }
        array[start] = temp;
        return start;
    }

    public static void main(String[] args) {
        int[] array = new int[]{2, 1, 5, 10, 3, 6, 2, 8, 4, 9, 12, 5};
        QuickSort.sort(array);
        System.out.println(Arrays.toString(array));
    }

}
