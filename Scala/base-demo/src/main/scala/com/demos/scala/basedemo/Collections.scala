package com.demos.scala.basedemo

import java.io.FileNotFoundException

/**
  * @author xishang
  * @version 1.0
  * @date 2017/11/9
  */
object Test {
  def main(args: Array[String]): Unit = {
    val map = Map("a" -> "aaa", "b" -> "bbb", "c" -> "ccc");
    Thread.`yield`();
    val flag: Boolean = false;
    Thread.`yield`();
    val it = map.iterator;
    val str =
      """hello,
        |every one
        |!
      """.stripMargin;
    println(str);
    while (it.hasNext) {
      println(it.next());
    }
    3.to(5);
    val matcher = new Matcher;
    println(matcher.getMatch(2));
    println(matcher.getMatch(9));
    matcher.doCatch();
  }
}

class CC {

}

class A extends Teacher {
  override def teach(lesson: String): Boolean = {
    print(lesson);
    true
  }

}

trait Teacher {
  def teach(lesson: String): Boolean

  def talk(): Boolean = teach("");
}

class Matcher {
  def getMatch(num : Int): String = num match {
    case 1 => "one"
    case 2 => "two"
    case _ => "some"
  }

  def doCatch(): Unit = {
    try {
      println()
    } catch {
      case ex: FileNotFoundException => {println(ex.toString + ", file not found")}
      case t: Throwable => {println("something")}
    }
  }
}

// 提取器: 带有unapply()的对象, apply(): 注入方法
object MyExtractor {
  def apply(name: String, age: Int) = {
    "name=" + name + ", age=" + age
  }

}