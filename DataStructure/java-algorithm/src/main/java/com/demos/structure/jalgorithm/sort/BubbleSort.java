package com.demos.structure.jalgorithm.sort;

import java.util.Arrays;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/14
 * <p>
 * 冒泡排序: 稳定排序
 * 基本思路: 大值下沉或小值上浮
 * 时间复杂度: 最好: O(n), 最坏: O(n^2), 平均: O(n^2)
 * 空间复杂度: O(1)
 */
public class BubbleSort {

    public static void sort(int[] array) {
        for (int i = 0; i < array.length; i++) {
            int swapCount = 0; // 交换次数
            for (int j = array.length - 1; j > i; j--) {
                int swap;
                // 从下往上传小值(冒泡), 也可从上往下传大值(沉底)
                if ((swap = array[j]) < array[j - 1]) {
                    array[j] = array[j - 1];
                    array[j - 1] = swap;
                    swapCount++;
                }
            }
            if (swapCount == 0) { // 交换次数为0, 已经完全有序, 数组返回
                return;
            }
        }
    }

    public static void main(String[] args) {
        int[] array = new int[]{2, 1, 5, 10, 3, 6, 2, 8, 4, 9, 12, 5};
        BubbleSort.sort(array);
        System.out.println(Arrays.toString(array));
    }

}
