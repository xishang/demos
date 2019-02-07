package com.demos.java.basedemo.lambda;

import java.util.Optional;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/1
 */
public class OptionalDemo {

    public static void main(String[] args) {
        String[] ss = new String[]{"a", null, "c"};
        for (String s : ss) {
            Optional<String> os = Optional.ofNullable(s);
            System.out.println(os.orElse("this is a null"));
            os.ifPresent(Object::hashCode);
        }
    }

}
