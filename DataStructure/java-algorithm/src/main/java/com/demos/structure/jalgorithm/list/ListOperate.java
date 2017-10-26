package com.demos.structure.jalgorithm.list;

public class ListOperate {

    /**
     * 计算表达式中缀转后缀：压栈计算
     * 支持字符: 0~9 . + - * / ( )
     * eg:  a+b*c+(d*e+f)*g => abc*+de*f+g*+
     * @param expressionQueue
     */
    public SimpleQueue<String> expressionMidPost(SimpleQueue<String> expressionQueue) {
        System.out.println(expressionQueue);
        SimpleQueue<String> out = new SimpleQueue<>();
        SimpleStack<String> expressionStack = new SimpleStack<>();
        String expression;
        while ((expression = expressionQueue.pop()) != null) {
            int priority = getPriority(expression);
            if (priority == 0) { // 数字直接入队
                out.offer(expression);
            } else if (priority == -1) { // ")"，"("之后的全部出栈入队
                String sExpression;
                while ((sExpression = expressionStack.pop()) != null) {
                    if (getPriority(sExpression) == 1 || getPriority(sExpression) == 2) {
                        out.offer(sExpression);
                    }
                    if (getPriority(sExpression) == 9) { // 找到"("，"()"已全部出栈入队
                        break;
                    }
                }
            } else if (priority == 9) { // "("，直接入栈
                expressionStack.push(expression);
            } else { // + - * /，优先级 >= current且不为"(" 出栈入队，"("只能被")"出栈
                String sExpression;
                while ((sExpression = expressionStack.pop()) != null && getPriority(sExpression) >= priority && getPriority(sExpression) != 9) {
                    out.offer(sExpression);
                }
                if (sExpression != null) {
                    expressionStack.push(sExpression);
                }
                expressionStack.push(expression);
            }
        }
        // 全部出栈入队
        while ((expression = expressionStack.pop()) != null) {
            out.offer(expression);
        }
        System.out.println(out);
        return out;
    }

    // 表达式优先级
    public int getPriority(String expression) {
        switch (expression) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            case "(":
                return 9;
            case ")":
                return -1;
            default:
                return 0;
        }
    }

    /**
     * 计算表达式结果
     * @param queue
     * @return
     */
    public float calculateResult(SimpleQueue<String> queue) {
        SimpleStack<String> stack = new SimpleStack<>();
        String expression;
        while ((expression = queue.pop()) != null) {
            if (getPriority(expression) == 0) { // 数字
                stack.push(expression);
            } else {
                float num1 = Float.parseFloat(stack.pop());
                float num2 = Float.parseFloat(stack.pop());
                switch (expression) {
                    case "+":
                        stack.push(String.valueOf(num2 + num1));
                        break;
                    case "-":
                        stack.push(String.valueOf(num2 - num1));
                        break;
                    case "*":
                        stack.push(String.valueOf(num2 * num1));
                        break;
                    case "/":
                        stack.push(String.valueOf(num2 / num1));
                        break;
                    default:
                        throw new RuntimeException("expression error");
                }
            }
        }
        return Float.parseFloat(stack.pop());
    }

    public float calculateExpression(String expression) {
        SimpleQueue<String> queue = new SimpleQueue<>();
        char[] eChar = expression.toCharArray();
        String numStr = "";
        for (char c : eChar) {
            if (c == '.' || (c >= '0' && c <= '9')) { // 数字
                numStr += c;
            } else {
                if (!numStr.equals("")) {
                    queue.offer(numStr);
                    numStr = "";
                }
                queue.offer(String.valueOf(c));
            }
        }
        return calculateResult(expressionMidPost(queue));
    }

    public static void main(String[] args) {
        ListOperate operate = new ListOperate();
        float result = operate.calculateExpression("(10+15-3*4)*(5*(1+2)/(2+3))+2*(5-1)");
        System.out.println("result: " + result);
    }

}
