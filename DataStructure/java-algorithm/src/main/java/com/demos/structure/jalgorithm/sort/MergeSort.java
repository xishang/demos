package com.demos.structure.jalgorithm.sort;

import java.util.Arrays;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/14
 * <p>
 * 归并排序: 稳定排序
 * 基本思路: 有序数组排序, 递归到数组长度为1即为有序
 * 时间复杂度: 最好: O(n), 最坏: O(nlogn), 平均: O(nlogn)
 * 空间复杂度: O(n)
 */
public class MergeSort {

    public static void sort(int[] array) {
        if (array.length > 1) {
            mergeSort(array, 0, array.length - 1);
        }
    }

    private static void mergeSort(int[] array, int start, int end) {
        if (start < end) {
            int mid = (start + end) / 2;
            // 左边进行递归排序
            mergeSort(array, start, mid);
            // 右边进行递归排序
            mergeSort(array, mid + 1, end);
            // 合并数组
            merge(array, start, end, mid);
        }
    }

    /**
     * 合并数组
     *
     * @param array
     * @param start
     * @param end
     * @param mid
     */
    private static void merge(int[] array, int start, int end, int mid) {
        int[] tempArray = new int[array.length];
        System.arraycopy(array, 0, tempArray, 0, array.length);
        int leftIndex = start, rightIndex = mid + 1, arrayIndex = start;
        while (leftIndex <= mid && rightIndex <= end) {
            if (tempArray[leftIndex] < tempArray[rightIndex]) {
                array[arrayIndex++] = tempArray[leftIndex++];
            } else {
                array[arrayIndex++] = tempArray[rightIndex++];
            }
        }
        while (leftIndex <= mid) {
            array[arrayIndex++] = tempArray[leftIndex++];
        }
        while (rightIndex <= end) {
            array[arrayIndex++] = tempArray[rightIndex++];
        }
    }

    public static void main(String[] args) {
        int[] array = new int[]{2, 1, 5, 10, 3, 6, 2, 8, 4, 9, 12, 5};
        MergeSort.sort(array);
        System.out.println(Arrays.toString(array));
    }

}
