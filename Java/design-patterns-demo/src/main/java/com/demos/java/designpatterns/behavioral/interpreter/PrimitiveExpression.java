package com.demos.java.designpatterns.behavioral.interpreter;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/9
 * <p>
 * 终结符表达式: 基本命令表达式
 */
public class PrimitiveExpression extends AbstractExpression {

    // 基本命令: PRINT, BREAK or SPACE
    private String name;

    // 如果是PRINT, text为要打印的字符串
    private String text;

    @Override
    public void interpret(Context context) {
        name = context.currentToken();
        context.skipToken(name);
        if (!name.equals("PRINT") && !name.equals("BREAK") && !name.equals("SPACE")) {
            System.err.println("非法命令");
        }
        if (name.equals("PRINT")) {
            text = context.currentToken();
            context.nextToken();
        }
    }

    @Override
    public void execute() {
        if (name.equals("PRINT")) {
            System.out.print(text);
        } else if (name.equals("SPACE")) {
            System.out.print(" ");
        } else if (name.equals("BREAK")) {
            System.out.println();
        }
    }

}
