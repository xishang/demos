package com.demos.java.basedemo.lambda;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Java8新特性
 *
 *
 * lambda表达式: () -> {}
 * 注: 若只有一个形参，可以省略圆括号; 若代码块只有一个语句，可以去掉花括号，并返回语句的值
 * 目标类型：函数式接口(只包含一个抽象方法的接口)
 *
 *
 * Stream: 对集合(Collection)对象功能的增强, 类似一个高级版本的Iterable
 * 作用: 对集合对象进行各种非常便利、高效的聚合操作(aggregate operation), 或者大批量数据操作(bulk data operation)
 * 效率: 同时提供串行和并行两种模式进行汇聚操作, 并发模式能够充分利用多核处理器的优势, 使用"fork/join"并行方式来拆分任务和加速处理过程
 *
 * 生成Stream Source方式:
 * 1.Collection.stream()
 * 2.Collection.parallelStream()
 * 3.Arrays.stream(T array)
 * 4.Stream.of(T t) or Stream.of(T... values)
 * 5.java.io.BufferedReader.lines()
 * 6.java.util.stream.IntStream.range()
 * 7.Pattern.splitAsStream()
 * 8.其他
 *
 * 三种包装类型Stream: IntStream, LongStream, DoubleStream
 * 效率高于Stream<Integer>, Stream<Long>, Stream<Double>
 *
 * Stream操作类型:
 * 1.intermediate: 一个流可以后面跟随零个或多个intermediate操作. 其目的主要是打开流, 做出某种程度的数据映射/过滤, 然后返回一个新的流,
 *  交给下一个操作使用, 这类操作都是惰性化的(lazy), 仅仅调用到这类方法， 并没有真正开始流的遍历
 * 2.terminal: 一个流只能有一个terminal操作, 当这个操作执行后, 流就被使用"光"了, 无法再被操作, 所以这必定是流的最后一个操作.
 *  terminal操作的执行, 才会真正开始流的遍历, 并且会生成一个结果, 或者一个side-effect
 * 3.short-circuiting: 当操作一个无限大的Stream, 而又希望在有限时间内完成操作时, 则在管道内拥有一个short-circuiting操作是必要非充分条件
 *
 * Stream的操作:
 * 1.intermediate类型: map(mapToInt, flatMap等), filter, distinct, sorted, peek, limit, skip, parallel, sequential, unordered
 * 2.terminal类型: forEach, forEachOrdered, toArray, reduce, collect, min, max, count, anyMatch, allMatch, noneMatch, findFirst,
 *  findAny, iterator
 * 3.short-circuiting类型: anyMatch, allMatch, noneMatch, findFirst, findAny, limit
 */
public class LambdaDemo {

    // 取代匿名内部类
    public void runnable() {
        new Thread(() -> System.out.println("---------------------runnable接口---------------------")).start();
    }

    // 列表迭代
    public void iterate() {
        List<String> words = Arrays.asList("hello", "lambda");
        // System.out::println 等价于 str -> System.out.println(str)
        words.forEach(System.out::println);
    }

    // Stream操作
    public void stream() {
        List<Person> sourceList = Person.generateList(20);
        // 取男性最大年龄, 典型场景: filter->map->reduce
        int maleMaxAge = sourceList.parallelStream()
                .filter(p -> p.getSex() == 0)
                .map(Person::getAge)
                .reduce(Integer::max)
                .get();
        System.out.println("男性最大年龄: " + maleMaxAge);
        // Collectors集合转换: Collectors.toList(), Collectors.toSet(), Collectors.toCollection(ArrayList::new), ...
        List<Person> adultList = sourceList.parallelStream()
                .filter(p -> p.getAge() > 18)
                .collect(Collectors.toCollection(ArrayList::new));
        // Collectors的reduction操作:
        // groupingBy: 按字段分组
        // partitionBy: 特殊的groupingBy, 按true/false分组
        Map<Boolean, List<Person>> personGroups = sourceList.parallelStream()
                .limit(10)/*取前10个*/
                .collect(Collectors.partitioningBy(p -> p.getSex() == 0));
    }

    public static void main(String[] args) {
        LambdaDemo demo = new LambdaDemo();
        demo.runnable();
        demo.iterate();
        demo.stream();
    }

}
