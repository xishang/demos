package com.demos.java.designpatterns.behavioral.interpreter;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/9
 * <p>
 * 非终结符表达式: 循环表达式
 */
public class LoopExpression extends AbstractExpression {

    // 循环次数
    private int number;

    // 循环语句中的表达式
    private AbstractExpression commandExpression;

    @Override
    public void interpret(Context context) {
        context.skipToken("LOOP");
        number = context.currentNumber();
        context.nextToken();
        // 循环语句中的表达式
        commandExpression = new ExecuteExpression();
        commandExpression.interpret(context);
    }

    /**
     * 执行循环中命令number次
     */
    @Override
    public void execute() {
        for (int i = 0; i < number; i++) {
            commandExpression.execute();
        }
    }

}
