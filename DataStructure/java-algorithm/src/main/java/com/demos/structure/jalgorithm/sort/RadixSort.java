package com.demos.structure.jalgorithm.sort;

import java.util.Arrays;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/14
 * <p>
 * 基数排序
 * 基本思路: 将所有待比较数值统一为同样的数位长度, 数位较短的数前面补零; 从最低位开始, 依次进行排序, 从最低位一直排序到最高位, 数列就变成一个有序序列
 */
public class RadixSort {

    public static int[] sort(int[] array) {
        int maxLen = maxLength(array);
        return radixSort(array, 0, maxLen);
    }

    /**
     * 基数排序: 改进的桶排序, 即桶长度待排数组长度
     *
     * @param array
     * @param digit
     * @param maxLen
     * @return
     */
    private static int[] radixSort(int[] array, int digit, int maxLen) {
        if (digit >= maxLen) {
            return array;
        }
        final int radix = 10; // 基数
        int arrayLength = array.length;
        int[] count = new int[radix];
        int[] bucket = new int[arrayLength];
        // 统计将数组中的数字分配到桶中后，各个桶中的数字个数
        for (int i = 0; i < arrayLength; i++) {
            count[getDigit(array[i], digit)]++;
        }
        // 将各个桶中的数字个数，转化成各个桶中最后一个数字的下标索引
        for (int i = 1; i < radix; i++) {
            count[i] = count[i] + count[i - 1];
        }
        // 将原数组中的数字分配给辅助数组 bucket
        for (int i = arrayLength - 1; i >= 0; i--) {
            int number = array[i];
            int d = getDigit(number, digit);
            bucket[count[d] - 1] = number;
            count[d]--;
        }
        return radixSort(bucket, digit + 1, maxLen);
    }

    /**
     * 返回数组中最大数字的位数
     *
     * @param array
     * @return
     */
    private static int maxLength(int[] array) {
        int maxLen = 0;
        for (int item : array) {
            int length = getLength(item);
            if (maxLen < length) {
                maxLen = length;
            }
        }
        return maxLen;
    }

    /**
     * 计算一个数字共有多少位
     *
     * @param number
     * @return
     */
    private static int getLength(int number) {
        return String.valueOf(number).length();
    }

    /**
     * 获取数字num的d位数字[d从0开始]
     *
     * @param num
     * @param d
     * @return
     */
    private static int getDigit(int num, int d) {
        int radix = (int) Math.pow(10, d);
        return num / radix % 10;
    }

    public static void main(String[] args) {
        int[] array = new int[]{2, 1, 5, 10, 3, 6, 2, 8, 4, 9, 12, 5};
        array = RadixSort.sort(array);
        System.out.println(Arrays.toString(array));
    }

}
