package com.demos.java.designpatterns.behavioral;

import com.demos.java.designpatterns.behavioral.chain_of_responsibility.*;
import com.demos.java.designpatterns.behavioral.command.*;
import com.demos.java.designpatterns.behavioral.interpreter.AbstractExpression;
import com.demos.java.designpatterns.behavioral.interpreter.Context;
import com.demos.java.designpatterns.behavioral.interpreter.ExecuteExpression;
import com.demos.java.designpatterns.behavioral.mediator.Button;
import com.demos.java.designpatterns.behavioral.mediator.ConcreteMediator;
import com.demos.java.designpatterns.behavioral.mediator.List;
import com.demos.java.designpatterns.behavioral.mediator.Text;
import com.demos.java.designpatterns.behavioral.memento.Caretaker;
import com.demos.java.designpatterns.behavioral.memento.Memento;
import com.demos.java.designpatterns.behavioral.memento.Originator;
import com.demos.java.designpatterns.behavioral.observer.ConcreteObserverA;
import com.demos.java.designpatterns.behavioral.observer.ConcreteObserverB;
import com.demos.java.designpatterns.behavioral.observer.ConcreteSubject;
import com.demos.java.designpatterns.behavioral.strategy.ChildrenPrice;
import com.demos.java.designpatterns.behavioral.strategy.MovieTicket;
import com.demos.java.designpatterns.behavioral.strategy.StudentPrice;
import com.demos.java.designpatterns.behavioral.strategy.VipPrice;
import com.demos.java.designpatterns.behavioral.template_method.Account;
import com.demos.java.designpatterns.behavioral.template_method.CurrentAccount;
import com.demos.java.designpatterns.behavioral.visitor.*;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/25
 * <p>
 * 行为型
 */
public class Main {

    public static void main(String[] args) {
        // 模版方法模式
        Account account = new CurrentAccount(1000.00d);
        account.calculateInterest();
        // 责任链模式
        Approver cm = new Chairman("James");
        Approver gm = new GeneralManager("Jack", cm);
        Approver dm = new DepartmentManager("Tom", gm);
        dm.processRequest(new PurchaseRequest(800.0d, 1, "购买显示器"));
        dm.processRequest(new PurchaseRequest(6000.0d, 1, "购买电脑"));
        dm.processRequest(new PurchaseRequest(12000.0d, 1, "购买服务器"));
        // 命令模式
        Light light = new Light();
        TurnOnCommand turnOnCommand = new TurnOnCommand(light);
        TurnOffCommand turnOffCommand = new TurnOffCommand(light);
        Switch s = new Switch();
        s.setTurnOnCommand(turnOnCommand);
        s.setTurnOffCommand(turnOffCommand);
        s.turnOn();
        s.turnOff();
        MacroCommand macroCommand = new MacroCommand();
        macroCommand.addCommand(turnOnCommand);
        macroCommand.addCommand(turnOffCommand);
        macroCommand.execute();
        // 策略模式
        MovieTicket vipTicket = new MovieTicket(new VipPrice());
        System.out.println("VIP票价格: " + vipTicket.realPrice(100.0d));
        MovieTicket studentTicket = new MovieTicket(new StudentPrice());
        studentTicket.realPrice(100.0d);
        System.out.println("学生票价格: " + studentTicket.realPrice(100.0d));
        MovieTicket childrenTicket = new MovieTicket(new ChildrenPrice());
        System.out.println("儿童票价格: " + childrenTicket.realPrice(100.0d));
        // 状态模式
        com.demos.java.designpatterns.behavioral.state.Account sAccount = new com.demos.java.designpatterns.behavioral.state.Account("StateAccount");
        sAccount.setAmount(1000);
        sAccount.setAmount(-2000);
        // 中介者模式
        ConcreteMediator mediator = new ConcreteMediator();
        Button button = new Button();
        button.setMediator(mediator);
        List list = new List();
        list.setMediator(mediator);
        Text text = new Text();
        text.setMediator(mediator);
        mediator.setButton(button);
        mediator.setList(list);
        mediator.setText(text);
        button.update();
        button.notifyChanged();
        // 观察者模式
        ConcreteSubject subject = new ConcreteSubject();
        subject.attach(new ConcreteObserverA());
        subject.attach(new ConcreteObserverB());
        subject.setStatus(1);
        // 解释器模式
        String commandText = "LOOP 2 PRINT Hi SPACE SPACE PRINT Man BREAK END PRINT Hello SPACE SPACE PRINT You BREAK";
        Context context = new Context(commandText);
        AbstractExpression node = new ExecuteExpression();
        node.interpret(context);
        node.execute();
        // 备忘录模式
        Caretaker caretaker = new Caretaker();
        Originator originator = new Originator();
        originator.setState("On");
        caretaker.addMemento(originator.createMemento());
        originator.setState("Off");
        caretaker.addMemento(originator.createMemento());
        System.out.println("当前状态: " + originator.getState());
        originator.restoreMemento(caretaker.getMemento(0));
        System.out.println("索引0状态: " + originator.getState());
        // 访问者模式
        ObjectStructure structure = new ObjectStructure();
        structure.addElement(new Engine());
        structure.addElement(new Wheel());
        structure.accept(new PriceVisitor());
        structure.accept(new PerformanceVisitor());
    }

}
