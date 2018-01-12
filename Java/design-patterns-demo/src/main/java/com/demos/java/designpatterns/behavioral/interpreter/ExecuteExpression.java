package com.demos.java.designpatterns.behavioral.interpreter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/9
 * <p>
 * 非终结符表达式: 执行表达式(入口), 执行多个命令表达式(command)
 */
public class ExecuteExpression extends AbstractExpression {

    // 定义一个集合用于存储多条命令
    private List<AbstractExpression> list = new ArrayList<>();

    /**
     * 循环处理Context中的标记
     *
     * @param context
     */
    @Override
    public void interpret(Context context) {
        while (true) {
            if (context.currentToken() == null) {
                // 如果已经没有任何标记, 则退出解释
                break;
            } else if (context.currentToken().equals("END")) {
                // 如果标记为END, 则不解释END并结束本次解释过程, 等待execute()被调用
                context.skipToken("END");
                break;
            } else {
                // 如果为其他标记, 则解释标记并将其加入命令集合
                AbstractExpression commandExpression = new CommandExpression();
                commandExpression.interpret(context);
                list.add(commandExpression);
            }
        }
    }

    /**
     * 循环执行命令集合中的每一条命令
     */
    @Override
    public void execute() {
        for (AbstractExpression expression : list) {
            expression.execute();
        }
    }

}
