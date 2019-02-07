package com.demos.java.basedemo.classloader;

import com.vdurmont.emoji.EmojiParser;

import java.util.StringTokenizer;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/25
 */
public class Main {

    /**
     * BootstrapClassLoader、ExtClassLoader、AppClassLoader加载class的路径
     */
    public static void loaderPath() {
        printProperty("sun.boot.class.path");
        printProperty("java.ext.dirs");
        // 可以在执行时指定classpath, 如: java -cp . com.demo.Main, 默认(直接: java com.demo.Main)classpath为当前路径(.)
        printProperty("java.class.path");
        // Thread ContextClassLoader
        System.out.println("Thread ContextClassLoader -> " + Thread.currentThread().getContextClassLoader().getClass());
    }

    static void printProperty(String key) {
        System.out.println(key);
        StringTokenizer tokenizer = new StringTokenizer(System.getProperty(key), ":");
        while (tokenizer.hasMoreTokens()) {
            System.out.println(">> " + tokenizer.nextToken());
        }
        System.out.println();
    }

    public static void main(String[] args) throws Throwable {
//        loaderPath();
        String name = "Philip\uD83C\uDF34";
        String name1 = EmojiParser.parseToAliases(name);
        System.out.println("name1="+name1);
        String name2 = EmojiParser.parseToAliases(name1);
        System.out.println("name2=" + name2);
        String origin = EmojiParser.parseToUnicode(name2);
        System.out.println("origin=" +origin);
        String origin2 = EmojiParser.parseToUnicode(origin);
        System.out.println("origin2="+origin2);
    }

}
