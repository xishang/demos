package com.demos.java.designpatterns.behavioral.interpreter;

import java.util.StringTokenizer;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/8
 * <p>
 * 环境类: 用于存储和操作需要解释的语句，在本实例中每一个需要解释的单词可以称为一个动作标记(Action Token)或命令
 */
public class Context {

    // 使用StringTokenizer解析命令字符串
    private StringTokenizer tokenizer;

    // 当前字符串标记
    private String currentToken;

    public Context(String commandText) {
        tokenizer = new StringTokenizer(commandText);
        nextToken();
    }

    /**
     * 返回下一个标记
     *
     * @return
     */
    public String nextToken() {
        if (tokenizer.hasMoreTokens()) {
            currentToken = tokenizer.nextToken();
        } else {
            currentToken = null;
        }
        return currentToken;
    }

    /**
     * 返回当前的标记
     *
     * @return
     */
    public String currentToken() {
        return currentToken;
    }

    /**
     * 跳过一个标记
     *
     * @param token
     */
    public void skipToken(String token) {
        if (!token.equals(currentToken)) {
            System.err.println("错误提示: [" + currentToken + "]解释错误");
        }
        nextToken();
    }

    /**
     * 如果当前的标记是一个数字，则返回对应的数值
     *
     * @return
     */
    public int currentNumber() {
        int number = 0;
        try {
            number = Integer.parseInt(currentToken); //将字符串转换为整数
        } catch (NumberFormatException e) {
            System.err.println("错误提示：" + e);
        }
        return number;
    }

}
