package com.demos.structure.jalgorithm.sort;

import java.util.Arrays;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/14
 * <p>
 * 选择排序: 不稳定排序
 * 基本思路: 遍历数组, 依次找出最小的数并与开始的位互换
 * 时间复杂度: 最好: O(n^2), 最坏: O(n^2), 平均: O(n^2)
 * 空间复杂度: O(1)
 */
public class SelectionSort {

    public static void sort(int[] array) {
        for (int i = 0; i < array.length; i++) {
            int index = i;
            for (int j = i + 1; j < array.length; j++) {
                if (array[j] < array[index]) {
                    index = j; // 找出最小的index
                }
            }
            // 交换最小值
            int temp = array[i];
            array[i] = array[index];
            array[index] = temp;
        }
    }

    public static void main(String[] args) {
        int[] array = new int[]{2, 1, 5, 10, 3, 6, 2, 8, 4, 9, 12, 5};
        SelectionSort.sort(array);
        System.out.println(Arrays.toString(array));
    }

}
