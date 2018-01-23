package com.demos.java.basedemo.invoke;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/16
 * <p>
 * MethodHandle: 方法句柄, 对Java中方法、构造方法和域的一个强类型的可执行的引用
 * => 通过方法句柄可以直接调用该句柄所引用的底层方法
 * => 方法句柄的类型完全由它的参数类型和返回值类型来确定, 而与它所引用的底层方法的名称和所在的类无关
 * 典型应用:
 * => 函数式编程: 一个方法句柄类似于一个函数指针, 通过传递方法句柄可进行函数式编程
 * => 柯里化(Currying): 把接受多个参数的函数变换成接受一个单一参数, 通过MethodHandles.insertArguments可以实现
 */
public class MethodHandleDemo {

    private static MethodHandles.Lookup lookup = MethodHandles.lookup();

    /**
     * 访问或修改域
     *
     * @throws Throwable
     */
    public static void invokeField() throws Throwable {
        // 获取或设置静态变量: 直接使用"getstatic"和"putstatic"指令, 不需要setter、getter方法
        MethodHandle staticGetter = lookup.findStaticGetter(Bird.class, "age", int.class);
        System.out.println("getstatic, age = " + staticGetter.invoke());
        MethodHandle staticSetter = lookup.findStaticSetter(Bird.class, "age", int.class);
        staticSetter.invoke(3); // 设置age=3
        System.out.println("after putstatic, Bird.age = " + Bird.age);

        // 获取或设置普通域: 直接使用"getfield"和"putfield"指令, 不需要setter、getter方法
        Bird sparrow = new Bird("sparrow");
        MethodHandle fieldGetter = lookup.findGetter(Bird.class, "name", String.class);
        System.out.println("getfield, name = " + fieldGetter.invoke(sparrow));
        MethodHandle fieldSetter = lookup.findSetter(Bird.class, "name", String.class);
        fieldSetter.invoke(sparrow, "little sparrow"); // 设置name=little sparrow
        System.out.println("after putfield, sparrow.name = " + sparrow.name);
    }

    /**
     * MethodType
     *
     * @throws Throwable
     */
    public static void methodType() throws Throwable {
        Bird bird = new Bird("sparrow");
        /* MethodType是不可变类型, 任何对MethodType实例的修改都会产生一个新的MethodType实例 */
        // 通过描述符获取MethodType
        MethodType sleepType = MethodType.fromMethodDescriptorString("()I", ClassLoader.getSystemClassLoader());
        // "invokevirtual"
        MethodHandle sleepHandle = lookup.findVirtual(Bird.class, "sleep", sleepType);
        /* ========== invokeExact:
        invokeExact()方法与直接调用底层方法是完全一样, invokeExact()的参数依次是方法接收者对象和实际参数列表
        invokeExact()方法在调用的时候要求严格的类型匹配, 因此此处必须进行[类型转换]
        1).sleepHandle.invokeExact(bird); -> 这种情况invokeExact会认为方法的返回类型是void, 因此报错
        2).Object obj = sleepHandle.invokeExact(bird); -> 这种情况invokeExact会认为方法的返回类型是Object而报错
         */
        int result = (int) sleepHandle.invokeExact(bird);
        /* ========== invoke:
        invoke()方法允许更加松散的调用方式, 它会尝试在调用的时候进行返回值和参数类型的转换工作, 这是通过MethodHandle类的asType方法来完成的
        asType方法的作用是把当前的方法句柄适配到新的MethodType上, 并产生一个新的方法句柄
        1).当方法句柄在调用时的类型与其声明的类型完全一致的时候, 调用invoke等同于调用invokeExact
        2).否则, invoke会先调用asType方法来尝试适配到调用时的类型. 如果适配成功, 调用可以继续, 否则会抛出相关的异常
         */
        sleepHandle.invoke(bird);
        /* ========== invokeWithArguments:
        invokeWithArguments()
         */
        // 生成返回值和参数类型都是Object类型的MethodType
        MethodType genericType = MethodType.genericMethodType(2);
        MethodHandle genericHandle = lookup.findVirtual(Bird.class, "generic", genericType);
        genericHandle.invokeWithArguments(bird, null, null);

        // 调用点为当前类(MethodHandleDemo), 没有对Bird的private方法(eat)的访问权限, 查找方法句柄的时候会抛出异常
        try {
            MethodHandle eatHandle = lookup.findSpecial(Bird.class, "eat", MethodType.methodType(void.class), MethodHandleDemo.class);
        } catch (IllegalAccessException e) {
            System.out.println("get eat() MethodHandle cause IllegalAccessException!");
        }
        /* ========== MethodHandle只会在查找时检查访问控制权限, 执行时不会进行检查:
        -> 与反射的区别: 反射API的Method类在每次调用invoke方法时都需要检查访问控制权限
        由于Bird类可以访问其私有方法, 因此查找过程是成功的
        通过获取Bird类返回的方法句柄可以成功访问Bird的私有方法: eat()
         */
        MethodHandle eatHandle = bird.getEatMethodHandle();
        /* ========== bindTo: 将调用者绑定到MethodHandle
        1).可以只公开某个方法, 而不必公开所属对象, 并将该方法句柄提供给客户端使用
        2).bindTo()只能对引用类型进行绑定, 如果要绑定基本类型, 可以使用wrap()将基本类型转换成包装类型, 如:
        -> MethodHandle mh = lookup.findVirtual(String.class, "substring", MethodType.methodType(String.class, int.class, int.class));
        -> mh = mh.asType(mh.type().wrap());
        -> mh = mh.bindTo("Hello World").bindTo(3); // bindTo()只是绑定第一个参数, 可以通过多次绑定来设置方法参数值
        -> System.out.println(mh.invoke(5));
         */
        eatHandle = eatHandle.bindTo(bird);
        // 已经绑定了调用者, 因此这里只需要传方法参数
        eatHandle.invoke();

        // 方法最后一个参数为int[]
        MethodType varargsType = MethodType.methodType(void.class, int.class, String.class, int[].class);
        MethodHandle varargsHandle = lookup.findVirtual(Bird.class, "varargs", varargsType);
        varargsHandle.invoke(bird, 1, "", new int[]{2, 3});
        // 将调用方式改为变长参数形式
        varargsHandle = varargsHandle.asVarargsCollector(int[].class);
        varargsHandle.invoke(bird, 1, "", 2, 3);
        /* 其他参数处理方式:
        1).MethodHandle.asCollector(arrayType, arrayLength); -> 只收集arrayLength个参数到数组类型arrayType
        2).MethodHandle.asSpreader(arrayType, arrayLength); -> 将数组参数中arrayLength个参数分配给声明的参数, 与asCollector相反
        3).MethodHandle.asFixedArity(); -> 方法的变长参数部分使用数组传入
         */
    }

    /**
     * 访问方法
     *
     * @throws Throwable
     */
    public static void methodHandle() throws Throwable {
        Bird bird = new Bird("sparrow");
        // ========== invokevirtual: 调用虚方法
        MethodType flyType = MethodType.methodType(void.class); // 通过返回+参数类型获取MethodType
        MethodHandle flyHandle = lookup.findVirtual(Bird.class, "fly", flyType);
        flyHandle = flyHandle.bindTo(bird);
        flyHandle.invoke();
        /* ========== invokespecial: 精确分派
        1).调用点必须有对目标类有适当的访问权限, 常用于调用自己的方法或父类方法
        2).最后一个参数为调用点类型, 为了对special方法进行访问权限验证
        与反射的区别: 反射的Method.invoke()方法在每次调用时都需要检查访问控制权限, 而方法句柄只在查找的时候进行检查
          */
        bird.invokeSpecial();
        // ========== invokestatic: 访问静态方法
        MethodType singType = MethodType.fromMethodDescriptorString("(Ljava/lang/String;)V", ClassLoader.getSystemClassLoader());
        MethodHandle singHandle = lookup.findStatic(Bird.class, "sing", singType);
        singHandle.invoke("Music");
        // ========== 获取构造方法
        MethodHandle constructHandle = lookup.findConstructor(Bird.class, singType);
        Bird ls = (Bird) constructHandle.invoke("little sparrow");
        System.out.println("构造方法创建Bird, name=" + ls.name);
    }

    /**
     * 反射转换MethodHandle
     *
     * @throws Throwable
     */
    public static void fromReflect() throws Throwable {
        Class<Bird> clazz = Bird.class;
        Bird sparrow = new Bird("sparrow");
        /* ========== Field -> MethodHandle
        包括普通域和静态域: Lookup.unreflectGetter, Lookup.unreflectSetter
         */
        Field staticField = clazz.getDeclaredField("age");
        MethodHandle staticGetter = lookup.unreflectGetter(staticField);
        System.out.println("static getter, age=" + staticGetter.invoke());
        MethodHandle staticSetter = lookup.unreflectSetter(staticField);
        staticSetter.invoke(2);
        System.out.println("after static setter, age=" + Bird.age);
        Field normalField = clazz.getDeclaredField("name");
        MethodHandle fieldGetter = lookup.unreflectGetter(normalField);
        System.out.println("field getter, name=" + fieldGetter.invoke(sparrow));
        MethodHandle fieldSetter = lookup.unreflectSetter(normalField);
        fieldSetter.invoke(sparrow, "little sparrow");
        System.out.println("after field setter, name=" + sparrow.name);
        // ========== Constructor -> MethodHandle: unreflectConstructor
        Constructor constructor = clazz.getConstructor(String.class);
        MethodHandle constructorHandle = lookup.unreflectConstructor(constructor);
        Bird littleSparrow = (Bird) constructorHandle.invoke("little sparrow");
        System.out.println("constructor bird, name=" + littleSparrow.name);
        /* ========== Method(Special) -> MethodHandle: unreflectSpecial
         与findSpecial类似, 调用点必须有对目标类有适当的访问权限, 最后也要传入一个调用点类型以验证访问权限
          */
        MethodHandle pmHandle = sparrow.unreflectEatMethod();
        pmHandle.invoke(sparrow);
        /* ========== Method(Other) -> MethodHandle: unreflectConstructor
         包括virtual方法和static方法
         reflect to virtual method
          */
        Method virtualMethod = clazz.getMethod("sleep");
        MethodHandle vmHandle = lookup.unreflect(virtualMethod);
        vmHandle.invoke(sparrow);
        // reflect to static method
        Method staticMethod = clazz.getMethod("sing", String.class);
        MethodHandle smHandle = lookup.unreflect(staticMethod);
        smHandle.invoke("Music");
    }

    /**
     * MethodHandles静态方法返回的工具类MethodHandle
     *
     * @throws Throwable
     */
    public static void fromMethodHandles() throws Throwable {
        // ========== 操作array
        int[] array = new int[]{0, 1, 2, 3, 4, 5};
        MethodHandle arrayElementGetter = MethodHandles.arrayElementGetter(int[].class);
        System.out.println("array[3] = " + arrayElementGetter.invoke(array, 3));
        MethodHandle arrayElementSetter = MethodHandles.arrayElementSetter(int[].class);
        arrayElementSetter.invoke(array, 3, 0);
        System.out.println(Arrays.toString(array));
        // ========== identity: 每次调用时返回其输入参数的值
        MethodHandle identityHandle = MethodHandles.identity(String.class);
        System.out.println("identity: param=hello, ret=" + identityHandle.invoke("hello"));
        // ========== constant: 每次调用时返回常数值
        MethodHandle constantHandle = MethodHandles.constant(String.class, "Constant value");
        System.out.println("constant: " + constantHandle.invoke());
    }

    /**
     * MethodHandle变换: 返回值和参数处理
     *
     * @throws Throwable
     */
    public static void methodHandleModify() throws Throwable {
        MethodType type = MethodType.methodType(String.class, int.class, int.class);
        MethodHandle ssHandle = lookup.findVirtual(String.class, "substring", type);
        System.out.println(ssHandle.invoke("Hello, World!", 2, 5));
        /* ========== dropArguments: 忽略起始位置pos起的valueTypes参数
         => 此时调用方法句柄的参数为: String.class, float.class, int.class, int.class, int.class
         => 实际上: 参数1,2(float.class, int.class)会被忽略, 仅起到一个占位符的作用
          */
        MethodHandle mHandle1 = MethodHandles.dropArguments(ssHandle, 1, float.class, int.class);
        System.out.println(mHandle1.invoke("Hello, World", 1f, 1, 2, 5));
        // ========== insertArguments: 为起始位置pos起的参数设置默认值, 这些参数在调用方法句柄时不需要再提供
        MethodHandle mHandle2 = MethodHandles.insertArguments(ssHandle, 1, 2, 5);
        System.out.println(mHandle2.invoke("Hello, World!"));
        /* ========== filterArguments: 对方法句柄的参数进行预处理, 再把预处理的结果作为实际调用时的参数, 预处理由其他MethodHandle来完成
        预处理方法句柄只能有一个参数, 且参数的类型必须匹配所要处理的参数的类型, 其返回值类型需要匹配原始方法句柄中对应的参数类型
        如果希望跳过某个参数的预处理, 可以将该位置处的预处理方法句柄设置为null
          */
        MethodHandle maxHandle = lookup.findStatic(Math.class, "max", MethodType.methodType(int.class, int.class, int.class));
        System.out.println(maxHandle.invoke(3, 5));
        // 预处理: String -> int, 预处理方法句柄为: returnType = int.class, paramType(接收者) = String.class
        MethodHandle lengthHandle = lookup.findVirtual(String.class, "length", MethodType.methodType(int.class));
        MethodHandle mHandle3 = MethodHandles.filterArguments(maxHandle, 0, lengthHandle, lengthHandle);
        System.out.println(mHandle3.invoke("Hey", "Hi"));
        // 将位置0的filter设置为null, 跳过对位置0的参数的预处理
        MethodHandle mHandle4 = MethodHandles.filterArguments(maxHandle, 0, null, lengthHandle);
        System.out.println(mHandle4.invoke(8, "Hello"));
        // ========== foldArguments: 对传入参数进行预处理, 并将处理结果作为实际调用的第一个参数
        MethodHandle sumHandle = lookup.findStatic(Integer.class, "sum", MethodType.methodType(int.class, int.class, int.class));
        MethodHandle mCombiner = MethodHandles.identity(int.class);
        MethodHandle mHandle5 = MethodHandles.foldArguments(sumHandle, mCombiner);
        System.out.println("5 + 5 = " + mHandle5.invoke(5));
        /* ========== permuteArguments: 对调用时的参数顺序进行重新排列, 再传递给原始的方法句柄来完成调用
        => 方法: MethodHandle permuteArguments(MethodHandle target, MethodType newType, int... reorder)
        -> target: 目标方法句柄
        -> newType: 重排序的新的方法句柄的方法类型
        -> reorder: 新的参数顺序(newType -> target.type())
          */
        MethodType compareType = MethodType.methodType(int.class, int.class, int.class);
        MethodHandle compareHandle = lookup.findStatic(Integer.class, "compare", compareType);
        System.out.println("normal: 8 compare 9 : " + compareHandle.invoke(8, 9));
        MethodHandle rcHandle = MethodHandles.permuteArguments(compareHandle, compareType, 1, 0);
        System.out.println("reverse: 8 compare 9 : " + rcHandle.invoke(8, 9));
        MethodHandle dcHandle = MethodHandles.permuteArguments(compareHandle, compareType, 1, 1);
        System.out.println("duplicate: 8 compare 9 : " + dcHandle.invoke(8, 9));
        // ========== catchException: 为目标方法指定异常处理的方法句柄
        ExceptionHandler parseEH = new ExceptionHandler();
        MethodType parseType = MethodType.methodType(int.class, String.class, String.class);
        // 目标方法需要先绑定所属对象, 否则调用时参数匹配不上
        MethodHandle parseHandle = lookup.findVirtual(ExceptionHandler.class, "parseAndAdd", parseType).bindTo(parseEH);
        // 异常处理方法类型: handler和target的返回类型必须相同, 可以在目标方法出错时把参数传入
        MethodType heType = MethodType.methodType(int.class, Exception.class);
        // MethodType heType = MethodType.methodType(int.class, Exception.class, String.class, String.class);
        // 异常处理方法如果不是static方法, 应该先绑定方法所属的对象
        MethodHandle heHandler = lookup.findVirtual(ExceptionHandler.class, "handleException", heType).bindTo(parseEH);
        MethodHandle hpHandle = MethodHandles.catchException(parseHandle, NumberFormatException.class, heHandler);
        hpHandle.invoke("abc", "def");
        /* ========== guardWithTest: 实现方法句柄层次上的条件判断语义
        => 方法: MethodHandle guardWithTest(MethodHandle test, MethodHandle target, MethodHandle fallback)
        -> test: 执行时会先进行test判断, 若结果为true则执行target, 否则执行fallback
         */
        MethodType testType = MethodType.methodType(boolean.class, int.class, int.class);
        MethodHandle testHandle = lookup.findStatic(MethodHandleDemo.class, "test", testType);
        MethodType methodType = MethodType.methodType(int.class, int.class, int.class);
        MethodHandle targetHandle = lookup.findStatic(Math.class, "max", methodType);
        MethodHandle fallbackHandle = lookup.findStatic(Math.class, "min", methodType);
        MethodHandle gHandle = MethodHandles.guardWithTest(testHandle, targetHandle, fallbackHandle);
        System.out.println("execute (8, 9) = " + gHandle.invoke(8, 9));
        System.out.println("execute (9, 8) = " + gHandle.invoke(9, 8));
        // ========== filterReturnValue: 处理返回值
        MethodHandle subHandle = lookup.findVirtual(String.class, "substring", MethodType.methodType(String.class, int.class));
        MethodHandle ucHandle = lookup.findVirtual(String.class, "toUpperCase", MethodType.methodType(String.class));
        MethodHandle frHandle = MethodHandles.filterReturnValue(subHandle, ucHandle);
        System.out.println(frHandle.invoke("Hello World!", 6));
        /* ========== invoker/exactInvoker: 元方法句柄
        可以在元方法句柄上添加统一的变换操作, 再用元方法句柄去调用其他方法句柄
        invoker方法的描述符: (Ljava/lang/String;II)Ljava/lang/String
        调用目标方法句柄时: 目标句柄需要传入的参数类型必须为(String, int, int), 其返回类型必须为String
          */
        MethodType invokerType = MethodType.methodType(String.class, String.class, int.class, int.class);
        /* invoker和exactInvoker用法类似, 但是exactInvoker对类型要求更严格
        例如: 上面的invokerType如果改为(String, Object, int, int), 则invoker仍然可以正常调用, 但是exactInvoker则会报错
          */
        MethodHandle invoker = MethodHandles.exactInvoker(invokerType);
        MethodType sssType = MethodType.methodType(String.class, int.class, int.class);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle sssHandle = lookup.findVirtual(String.class, "substring", sssType);
        MethodType dssType = MethodType.methodType(String.class, String.class, int.class, int.class);
        MethodHandle dssHandle = lookup.findStatic(MethodHandleDemo.class, "substr", dssType);
        // invoker.invoke(): 参数[0]-MethodHandle, 参数[1~3]作为MethodHandle的参数列表
        System.out.println("invoker: substring = " + invoker.invoke(sssHandle, "Hello", 1, 3));
        System.out.println("invoker: substr = " + invoker.invoke(dssHandle, "World", 1, 3));
        // invoker添加返回值过滤
        MethodHandle upHandle = lookup.findVirtual(String.class, "toUpperCase", MethodType.methodType(String.class));
        invoker = MethodHandles.filterReturnValue(invoker, upHandle);
        System.out.println("invoker: substring and upper = " + invoker.invoke(sssHandle, "Hello", 1, 3));
        System.out.println("invoker: substr and upper = " + invoker.invoke(dssHandle, "World", 1, 3));
    }

    /**
     * 其他方法句柄使用方法
     *
     * @throws Throwable
     */
    public static void other() throws Throwable {
        /* ========== MethodHandleProxies.asInterfaceInstance: 实现接口
        -> 接口: 只允许有一个方法, 因此只有一个方法句柄来处理方法调用
        -> 方法句柄: 必须满足接口方法的入参和返回类型(接口方法返回类型为void可以不限制方法句柄的返回类型)
          */
        MethodType implType = MethodType.methodType(String.class, String.class);
        MethodHandle implHandle = lookup.findStatic(MethodHandleDemo.class, "runImpl", implType);
        implHandle = implHandle.bindTo("附加参数");
        Runnable r = MethodHandleProxies.asInterfaceInstance(Runnable.class, implHandle);
        new Thread(r).start();
        /* 交换点: 多线程环境下控制方法句柄的一个开关, 这个开关只有两个状态: 有效和无效
        -> 初始状态: 有效, 允许且只允许发生一次状态改变(无效), 这种状态变化是全局和即时生效的, 使用同一个交换点的多个线程会即时观察到状态变化
        -> guardWithTest: 设置在交换点的不同状态下调用不同的方法句柄
          */
        SwitchPoint switchPoint = new SwitchPoint();
        MethodType methodType = MethodType.methodType(int.class, int.class, int.class);
        MethodHandle targetHandle = lookup.findStatic(Math.class, "max", methodType);
        MethodHandle fallbackHandle = lookup.findStatic(Math.class, "min", methodType);
        MethodHandle switchHandle = switchPoint.guardWithTest(targetHandle, fallbackHandle);
        System.out.println("SwitchPoint: on : (8, 9) = " + switchHandle.invoke(8, 9));
        SwitchPoint.invalidateAll(new SwitchPoint[]{switchPoint});
        System.out.println("SwitchPoint: off : (8, 9) = " + switchHandle.invoke(8, 9));
    }

    public static boolean test(int a, int b) {
        return a > b;
    }

    public static String runImpl(String content) {
        System.out.println("实现Runnable接口: content=" + content);
        return content;
    }

    public static String substr(String source, int start, int end) {
        return source.substring(start, end);
    }

    public static void main(String[] args) throws Throwable {
        // Lookup.lookupClass(): 返回调用者类型, 即当前类型
        System.out.println("lookup class is " + lookup.lookupClass().getName());
        // Lookup.lookupModes(): 查找模式(public protected private static ...)
        System.out.println("lookup modes is " + Modifier.toString(lookup.lookupModes()));
        invokeField();
        methodType();
        methodHandle();
        fromReflect();
        fromMethodHandles();
        methodHandleModify();
        other();
    }

}
