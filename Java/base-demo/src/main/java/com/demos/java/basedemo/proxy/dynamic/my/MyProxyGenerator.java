package com.demos.java.basedemo.proxy.dynamic.my;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/3
 * <p>
 * 自定义代理类生成器
 */
public class MyProxyGenerator {

    // 换行符
    public static final String LINE_SEPARATOR = "\r\n";
    // 动态代理类包名
    public static final String PROXY_CLASS_PACKAGE = "com.demos.proxy";
    // 动态代理类名前缀
    public static final String PROXY_CLASS_NAME_PREFIX = "$Proxy";
    // 动态代理类文件索引
    public static final AtomicLong INDEX_GENERATOR = new AtomicLong();
    // 动态代理生成文件临时目录
    public static final String PROXY_CLASS_FILE_PATH = "D:\\temp";

    /**
     * 生成代理类并加载到JVM
     * @param clazz
     * @param methods
     * @throws Exception
     */
    public static Class<?> generateAndLoadProxyClass(Class<?> clazz, Method[] methods) throws Exception {
        long index = INDEX_GENERATOR.getAndIncrement();
        // 代理类类名
        String className = PROXY_CLASS_NAME_PREFIX + index;
        String fileName = PROXY_CLASS_FILE_PATH + File.separator + className + ".java";
        FileWriter writer = null;
        try {
            // 生成.java文件
            writer = new FileWriter(new File(fileName));
            writer.write(generateClassCode(PROXY_CLASS_PACKAGE, className, clazz, methods));
            writer.flush();
            // 编译.java文件
            compileJavaFile(fileName);
            // 加载class到JVM
            String classPath = PROXY_CLASS_FILE_PATH + File.separator + className + ".class";
            Class<?> proxyClass = MyClassLoader.getInstance().findClass(classPath, PROXY_CLASS_PACKAGE + "." + className);
            return proxyClass;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * 编译.java文件
     * @param fileName
     * @throws IOException
     */
    private static void compileJavaFile(String fileName) throws IOException {
        compileByTools(fileName);
//        compileByExec(fileName);
    }

    /**
     * 使用Runtime执行javac命令
     * 注意: 需要指定classpath, 否则找不到依赖的类
     * 建议使用compileByTools()
     * @param fileName
     * @throws IOException
     */
    @Deprecated
    private static void compileByExec(String fileName) throws IOException {
        // 获取当前的classpath
        String classpath = MyProxyGenerator.class.getResource("/").getPath();
        // 运行命令: javac -classpath ${classpath} ${filepath}
        String command = "javac -classpath " + classpath + " " + fileName;
        Process process = Runtime.getRuntime().exec(command);
        // 等待执行, 并输出错误日志
        try {
            InputStream errorStream = process.getErrorStream();
            InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
            int exitVal = process.waitFor();
            System.out.println("Process exitValue: " + exitVal);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用JDK自带的JavaCompiler
     * @param fileName
     * @throws IOException
     */
    private static void compileByTools(String fileName) throws IOException {
        // 获取系统Java编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // 获取标准文件管理器实例
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        try {
            Iterable units = fileManager.getJavaFileObjects(fileName);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, null, null, units);
            task.call();
        } finally {
            fileManager.close();
        }
    }

    /**
     * 拼接 class 代码片段
     * @param packageName   代理类包名
     * @param clazz         要代理的类型
     * @return
     */
    private static String generateClassCode(String packageName, String className, Class<?> clazz, Method[] methods) throws Exception {

        StringBuilder classCodes = new StringBuilder();

        /*--------------------包名和依赖 start--------------------*/
        classCodes.append("package ").append(packageName).append(";").append(LINE_SEPARATOR);
        classCodes.append(LINE_SEPARATOR);
        classCodes.append("import java.lang.reflect.*;").append(LINE_SEPARATOR);
        classCodes.append(LINE_SEPARATOR);
        /*--------------------包名和依赖 start--------------------*/

        /*--------------------类定义 start--------------------*/
        classCodes.append("public class ").append(className);
        if (clazz.isInterface()) {
            classCodes.append(" implements ");
        } else {
            classCodes.append(" extends ");
        }
        classCodes.append(clazz.getName()).append(" {").append(LINE_SEPARATOR);
        classCodes.append(LINE_SEPARATOR);
        /*--------------------类定义 end--------------------*/

        /*--------------------声明变量InvocationHandler start--------------------*/
        classCodes.append("private InvocationHandler handler;").append(LINE_SEPARATOR);
        classCodes.append(LINE_SEPARATOR);
        /*--------------------声明变量InvocationHandler end--------------------*/

        /*--------------------声明代理方法 start--------------------*/
        for (int i = 0; i < methods.length; i++) {
            classCodes.append("private static Method m").append(i).append(";").append(LINE_SEPARATOR);
        }
        classCodes.append(LINE_SEPARATOR);
        /*--------------------声明代理方法 end--------------------*/

        /*--------------------代理方法对象初始化 start--------------------*/
        classCodes.append("static {").append(LINE_SEPARATOR);
        classCodes.append("    ").append("try {").append(LINE_SEPARATOR);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            classCodes.append("    ").append("    ").append("m").append(i).append(" = ").append(clazz.getName())
                    .append(".class.getMethod(\"").append(method.getName()).append("\", new Class[]{");
            // 方法参数
            Parameter[] params = method.getParameters();
            if (params.length != 0) {
                for (int j = 0; j < params.length; j++) {
                    if (j != 0) {
                        classCodes.append(", ");
                    }
                    Parameter param = params[j];
                    classCodes.append(param.getType().getName()).append(".class");
                }
            }
            classCodes.append("});").append(LINE_SEPARATOR);
        }
        classCodes.append("    ").append("} catch (NoSuchMethodException ne) {").append(LINE_SEPARATOR);
        classCodes.append("    ").append("    ").append("throw new NoSuchMethodError(ne.getMessage());").append(LINE_SEPARATOR);
        classCodes.append("    ").append("}").append(LINE_SEPARATOR);
        classCodes.append("}").append(LINE_SEPARATOR);
        classCodes.append(LINE_SEPARATOR);
        /*--------------------代理方法对象初始化 end--------------------*/

        /*--------------------定义构造函数 start--------------------*/
        classCodes.append("public ").append(className).append("(InvocationHandler handler) {").append(LINE_SEPARATOR);
        classCodes.append("    ").append("this.handler = handler;").append(LINE_SEPARATOR);
        classCodes.append("}").append(LINE_SEPARATOR);
        classCodes.append(LINE_SEPARATOR);
        /*--------------------定义构造函数 end--------------------*/

        /*--------------------填充其他函数 start--------------------*/
        classCodes.append(generateMethodCode(clazz, methods));
        /*--------------------填充其他函数 end--------------------*/

        // 类结束
        classCodes.append("}").append(LINE_SEPARATOR);

        return classCodes.toString();
    }

    /**
     * 拼接 method 代码片段
     * @param clazz
     * @param methods
     * @return
     * @throws Exception
     */
    private static String generateMethodCode(Class<?> clazz, Method[] methods) throws Exception {

        StringBuilder methodCodes = new StringBuilder();

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            // 返回类型
            String returnType = method.getReturnType().getName();
            // 参数列表
            Parameter[] params = method.getParameters();
            // 异常列表
            Class<?>[] exceptionTypes = method.getExceptionTypes();

            /*--------------------方法定义 start--------------------*/
            methodCodes.append("public ").append(returnType).append(" ").append(method.getName());
            methodCodes.append("(");
            // 填充参数
            if (params.length != 0) {
                for (int j = 0; j < params.length; j++) {
                    if (j != 0) {
                        methodCodes.append(", ");
                    }
                    Parameter param = params[j];
                    methodCodes.append(param.getType().getName()).append(" ").append(param.getName());
                }
            }
            methodCodes.append(")");
            // 填充异常
            if (exceptionTypes.length != 0) {
                methodCodes.append(" throws ");
                for (int j = 0; j < exceptionTypes.length; j++) {
                    if (j != 0) {
                        methodCodes.append(", ");
                    }
                    methodCodes.append(exceptionTypes[j].getName());
                }
            }
            methodCodes.append(" {").append(LINE_SEPARATOR);
            /*--------------------方法定义 end--------------------*/

            /*--------------------方法体 start--------------------*/
            methodCodes.append("    ").append("try {").append(LINE_SEPARATOR);
            // 方法参数
            methodCodes.append("    ").append("    ").append("Object[] args = new Object[]{");
            if (params.length != 0) {
                for (int j = 0; j < params.length; j++) {
                    if (j != 0) {
                        methodCodes.append(", ");
                    }
                    Parameter param = params[j];
                    methodCodes.append(param.getName());
                }
            }
            methodCodes.append("};").append(LINE_SEPARATOR);
            // 执行InvocationHandler.invoke()
            methodCodes.append("    ").append("    ").append("Object result = handler.invoke(this, m").append(i)
                    .append(", args);").append(LINE_SEPARATOR);
            // 返回结果
            if (!"void".equals(returnType)) {
                methodCodes.append("    ").append("    ").append("return (").append(returnType).append(") result;").append(LINE_SEPARATOR);
            }
            // 异常处理
            methodCodes.append("    ").append("} catch (Error|RuntimeException");
            for (Class<?> exceptionType : exceptionTypes) {
                methodCodes.append("|").append(exceptionType.getName());
            }
            methodCodes.append(" e) {").append(LINE_SEPARATOR);
            methodCodes.append("    ").append("    ").append("throw e;").append(LINE_SEPARATOR);
            methodCodes.append("    ").append("} catch (Throwable t) {").append(LINE_SEPARATOR);
            methodCodes.append("    ").append("    ").append("throw new UndeclaredThrowableException(t);").append(LINE_SEPARATOR);
            methodCodes.append("    ").append("}").append(LINE_SEPARATOR);
            /*--------------------方法体 end--------------------*/

            // 方法结束
            methodCodes.append("}").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }

        return methodCodes.toString();
    }

}
