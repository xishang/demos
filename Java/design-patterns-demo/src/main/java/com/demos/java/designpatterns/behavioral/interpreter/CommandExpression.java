package com.demos.java.designpatterns.behavioral.interpreter;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/9
 * <p>
 * 非终结符表达式: 命令表达式, 执行循环(LOOP)或基本命令(PRINT, SPACE, BREAK)
 */
public class CommandExpression extends AbstractExpression {

    private AbstractExpression expression;

    @Override
    public void interpret(Context context) {
        if (context.currentToken().equals("LOOP")) { // 处理LOOP循环命令
            expression = new LoopExpression();
            expression.interpret(context);
        } else { // 处理基本命令
            expression = new PrimitiveExpression();
            expression.interpret(context);
        }
    }

    @Override
    public void execute() {
        expression.execute();
    }

}
