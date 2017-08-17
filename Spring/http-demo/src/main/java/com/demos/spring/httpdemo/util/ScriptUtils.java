package com.demos.spring.httpdemo.util;

import org.apache.commons.io.IOUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ScriptUtils {

    /**
     * 执行脚本文件
     * @param scriptFilePath 脚本文件路径
     * @param methodName 要执行的脚本文件方法
     * @param params 方法所需参数
     * @return 执行结果
     * @throws Exception
     */
    public static Object executeScript(String scriptFilePath, String methodName, Object...params) throws Exception {
        File file = new File(scriptFilePath);
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByName("js"); // 执行js脚本
        InputStream inputStream = new FileInputStream(file);
        String script = IOUtils.toString(inputStream);
        engine.eval(script);
        Invocable inv = (Invocable) engine;
        return inv.invokeFunction(methodName, params);
    }

}
