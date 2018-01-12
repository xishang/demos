package com.demos.java.designpatterns.behavioral.interpreter;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/8
 * <p>
 * 解释器模式: 定义一组文法规则, 并建立一个解释器来解释该语言中的句子
 * 优点:
 * 1.每一条文法规则表示为一个类, 通过继承机制, 易于改变和扩展文法
 * 2.添加新的解释表达式只需添加一个新的非终结符表达式或终结符表达式, 符合"开闭原则"
 * 缺点:
 * 1.复杂文法需要较多的文法规则类, 使系统难以维护
 * 2.文法解释执行, 且存在大量循环和递归调用, 导致执行效率较低
 * 使用场景: 正则表达式、XML文档解释等
 * <p>
 * 抽象表达式
 * <p>
 * 表达式文法规则
 * => expression ::= command * // 表达式, 一个表达式包含多条命令
 * => loop ::= 'loop number' expression  'end' // 循环表达式, 其中number为自然数
 * => primitive ::= 'print string'  | 'space' | 'break' // 基本命令, 其中string为字符串
 * => command ::= loop | primitive // 语句命令
 */
public abstract class AbstractExpression {

    /**
     * 解释语句
     *
     * @param context
     */
    public abstract void interpret(Context context);

    /**
     * 执行标记对应的命令
     */
    public abstract void execute();

}
