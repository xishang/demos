package com.demos.scala.basedemo

import scala.collection.mutable.ArrayBuffer

/**
  * @author xishang
  * @version 1.0
  * 2017/11/17
  * 数组相关操作
  */
class Chapter3 {

}

object Chapter3 {
  def main(args: Array[String]): Unit = {
    /* --------------------- 定长数组 --------------------- */
    val iarr = new Array[Int](2)
    iarr(1) = 10
    printArr(iarr)
    /* --------------------- 初始值 --------------------- */
    val narr = Array("a", "b", "c", 8)
    for (ba <- narr) {
      println(ba)
    }
    /* --------------------- 变长数组 --------------------- */
    val barr = ArrayBuffer[Int]()
    barr += 2
    barr += (3, 4)
    /* --------------------- `++`: 添加集合 --------------------- */
    barr ++= Array(5, 6)
    for (ba <- barr) {
      println(ba)
    }
    /* --------------------- 定长/变长转换 --------------------- */
    val sarr = barr.toArray // 变长数组->定长数组
    val varr = sarr.toBuffer // 定长数组->变长数组
    /* --------------------- 数组构建 --------------------- */
    val arr1 = Array(1, 2, 3, 4, 5)
    /* --------------------- yield构建: 定长->定长, 变长->变长 --------------------- */
    val arr2 = for (i <- arr1 if i % 2 == 1) yield i
    printArr(arr2)
    /* --------------------- filter构建 --------------------- */
    var arr3 = arr1.filter(_ % 2 == 1).map(2 * _)
    printArr(arr3)
  }

  def printArr(arr: Array[Int]): Unit = {
    println("---------------------print start---------------------")
    for (a <- arr) {
      println("print: " + a)
    }
  }

}
