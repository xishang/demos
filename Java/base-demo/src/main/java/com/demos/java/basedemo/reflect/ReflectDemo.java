package com.demos.java.basedemo.reflect;

import com.demos.java.basedemo.proxy.bean.HistoryTeacher;
import com.demos.java.basedemo.proxy.bean.Teacher;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/3
 * <p>
 * Java反射机制
 *
 * http://www.cnblogs.com/Eason-S/p/5851078.html
 */
public class ReflectDemo {

    /**
     * Java反射: Class
     * 成员: java.lang.reflect.Member
     * 实现类: Field, Method, Constructor
     * @throws Exception
     */
    public static void testClass() throws Exception {
        Class<?> clazz = Class.forName("com.demos.java.basedemo.reflect.Lesson");
        // Modifier: public, protected, private, abstract, static, final, ...
        int modifiers = clazz.getModifiers();
        // getModifiers()返回二进制值, 每一个二进制位对应一个修饰符
        System.out.println(Integer.toBinaryString(modifiers));
        // Modifier.toString()返回修饰符字符串
        System.out.println(Modifier.toString(modifiers));
    }

    /**
     * 测试: java.lang.reflect.Field
     * @throws Exception
     */
    public static void testField() throws Exception {
        Lesson lesson = new Lesson("history", 20, new HistoryTeacher());
        System.out.println(lesson);
        Class<?> clazz = lesson.getClass();
        // NoSuchFieldException, name为private, getField()无法获取
        // Field nameField = clazz.getField("name");
        Field countField = clazz.getDeclaredField("count");
        // IllegalAccessException, name为private, 不能直接获取
        // Object arg = countField.get(lesson);
        countField.setAccessible(true);
        // IllegalArgumentException, count为包装类Integer, 不能getInt()或setInt()
        // countField.getInt(lesson);
        // countField.setInt(lesson, 10);
        countField.set(lesson, new Integer(10));
        Field teacherField = clazz.getDeclaredField("teacher");
        teacherField.setAccessible(true);
        // 取出teacher对象并调用其teach()方法
        HistoryTeacher teacher = (HistoryTeacher) teacherField.get(lesson);
        teacher.teach("", 1l);
        System.out.println(lesson);
    }

    /**
     * 测试: java.lang.reflect.Method
     * @throws Exception
     */
    public static void testMethod() throws Exception {
        Lesson lesson = new Lesson("history", 20, new HistoryTeacher());
        System.out.println(lesson);
        Class<?> clazz = lesson.getClass();
        Method method = clazz.getMethod("teach", new Class[]{int.class});
        // 方法名
        System.out.println("Method Name: " + method.getName());
        // 修饰符
        System.out.println("Method Modifiers: " + Modifier.toString(method.getModifiers()));
        // 返回类型
        System.out.println(method.getReturnType().getName());
        // 参数类型列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        // 异常类型列表
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        // 注解列表
        Annotation[] annotations = method.getAnnotations();
        // 获取指定类型注解
        Resource resource = method.getAnnotation(Resource.class);
        // 是否存在注解
        boolean isResource = method.isAnnotationPresent(Resource.class);
        // 执行方法
        method.invoke(lesson, 10);
    }

    /**
     * 测试: java.lang.reflect.Constructor
     * @throws Exception
     */
    public static void testConstructor() throws Exception {
        Class<?> clazz = Lesson.class;
        // 根据参数列表获取构造方法
        Constructor constructor = clazz.getConstructor(new Class[]{String.class, Integer.class, Teacher.class});
        // 实例化对象
        Object obj = constructor.newInstance("history", 20, new HistoryTeacher());
        System.out.println(obj);
    }

    public static void main(String[] args) throws Exception {
        testClass();
        testField();
        testMethod();
        testConstructor();
    }

}
