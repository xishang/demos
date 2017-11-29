package com.demos.scala.basedemo

/**
  * @author xishang
  * @version 1.0
  * 2017/11/16
  * 控制结构和函数
  */
class Chapter2 {
}

object Chapter2 {
  def main(args: Array[String]): Unit = {
    /* --------------------- Scala所有语法结构都有值, 块{}的值为最后一个表达式的值 --------------------- */
    val num = {
      println("print some thing")
      2 + 3
    }
    println("num=" + num)
    /* --------------------- `if/else`取代java中的`?/:`, 且可以执行语句 --------------------- */
    val a = if (2 > 1) 1 else "0"
    /* --------------------- 没有`else`, 相当于`else ()` --------------------- */
    val b = if (1 > 2) 2
    var c: Int = 0
    if (1 == 1) c = 1 else c = -1
    println("a=" + a + ", b=" + b + ", c=" + c)
    /* --------------------- 赋值语句的值为Unit, Java和C++中赋值语句的值为被赋的那个值 --------------------- */
    val d = c = 10
    println("c=" + c + ", d=" + d)
    /* --------------------- while: 循环 --------------------- */
    while (c > 5) {
      println("while: c=" + c)
      c -= 3
    }
    /* --------------------- for(i <- 表达式): 遍历 --------------------- */
    for (i <- 0 to 3) // `to`包含上限, `until`不包含上限
      println("for: i=" + i)
    /* --------------------- for: 高级循环 --------------------- */
    for (i <- 1 until 3; j <- 5 to 7 if j % i == 0)
      println("for: i=" + i + ", j=" + j)
    /* --------------------- for+yield: 构建vector集合 --------------------- */
    val vector = for (i <- 0 until 3)
      yield i
    println(vector)
    /* --------------------- Scala没有`break`和`continue` --------------------- */

    /* --------------------- 调用带名参数函数 --------------------- */
    println(decorate("birthday"))
    println(sum(1, 2, 9))
    println(sum(5 to 10: _*)) // 使用`:_*`将区间当作参数序列处理
    /* --------------------- lazy: 懒值, 额外开销: 每次调用时检查该值是否初始化 --------------------- */
    if (a == -1) {
      /* --------------------- val: words1被定义时取值 --------------------- */
      val words1 = scala.io.Source.fromFile("/User/xishangs/temp/some.txt").mkString
      /* --------------------- lazy: 懒值, words2首次被调用时取值 --------------------- */
      lazy val words2 = scala.io.Source.fromFile("/User/xishangs/temp/some.txt").mkString
      /* --------------------- def: words3每次被调用时取值 --------------------- */
      def words3 = scala.io.Source.fromFile("/User/xishangs/temp/some.txt").mkString
      println(words1 + "\n" + words2 + "\n" + words3)
    }
    try {
      val e = div(5, 0)
      println("5 / 0 = " + e)
    } catch {
      case ie: IllegalArgumentException => ie.printStackTrace()
      case e: Exception => e.printStackTrace()
    } finally {
      println("div() end")
    }
  }

  /* --------------------- 定义函数, Java中没有函数, 只能通过静态方法模拟函数 --------------------- */
  def abs(x: Double): Double = if (x >= 0) x else -x

  /* --------------------- 默认参数和带名参数 --------------------- */
  def decorate(content: String, left: String = "---> ", right: String = " <---"): String = left + content + right

  /* --------------------- 变长参数 --------------------- */
  def sum(args: Int*) = {
    var result = 0
    for (arg <- args) {
      result += arg
    }
    result
  }

  /* --------------------- 过程: 不返回值 --------------------- */
  def printStr(str: String) { // 相当于`def printStr(str: String): Unit = {...}`
    println(str)
  }

  /* --------------------- 异常: 无受检异常 --------------------- */
  def div(x: Double, y: Double): Double = {
    if (y == 0)
      /* --------------------- throw语句的值为Nothing, 满足返回类型 --------------------- */
      throw new IllegalArgumentException("num is 0")
    else
      x / y
  }

}
