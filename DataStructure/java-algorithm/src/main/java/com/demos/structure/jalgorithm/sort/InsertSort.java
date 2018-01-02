package com.demos.structure.jalgorithm.sort;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/14
 * <p>
 * 插入排序: 稳定排序
 * 基本思想: 遍历数组, 将当前值插入前面已经排序的数组
 * 时间复杂度: 最好: O(n), 最坏: O(n^2), 平均: O(n^2)
 * 空间复杂度: O(1)
 */
public class InsertSort {

    /**
     * 直接插入排序: 遍历寻找插入位置
     * 时间复杂度: 最好: O(n), 最坏: O(n^2), 平均: O(n^2)
     *
     * @param array
     */
    public static void directSort(int[] array) {
        for (int i = 1; i < array.length; i++) {
            // 待插入元素
            int temp = array[i];
            int j;
            for (j = i - 1; j >= 0 && array[j] > temp; j--) {
                // 将大于temp的值往后移动一位
                array[j + 1] = array[j];
            }
            array[j + 1] = temp;
        }
    }

    /**
     * 二分插入排序: 使用二分查找寻找插入位置
     * 优势: 减少数据比较的次数, 适合大数组插入
     * 时间复杂度: 最好: O(nlogn), 最坏: O(n^2), 平均: O(n^2)
     *
     * @param array
     */
    public static void binarySort(int[] array) {
        for (int i = 1; i < array.length; i++) {
            // 待插入元素
            int temp = array[i];
            int start = 0, end = i, mid;
            while (start <= end) {
                mid = (start + end) / 2;
                if (array[mid] > temp) { // 插入前面部分
                    end = mid - 1;
                } else { // 插入前面部分
                    start = mid + 1;
                }
            }
            // 插入start位置
            int j;
            for (j = i; j > start; j--) {
                array[j] = array[j - 1];
            }
            array[start] = temp;
        }
    }

}
