package com.demos.java.framework.asm.component;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;

import java.util.HashSet;
import java.util.Set;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/16
 */
public class ClassNameRecordingRemapper extends Remapper {

    private final Set<? super String> classNames;

    public ClassNameRecordingRemapper(Set<? super String> classNames) {
        this.classNames = classNames;
    }

    @Override
    public String mapType(String type) {
        classNames.add(type);
        return type;
    }

    public Set<String> findClassNames(byte[] bytecode) {
        Set<String> classNames = new HashSet<String>();

        ClassReader classReader = new ClassReader(bytecode);
        ClassWriter classWriter = new ClassWriter(classReader, 0);

        ClassNameRecordingRemapper remapper = new ClassNameRecordingRemapper(classNames);
//        classReader.accept(remapper, 0);

        return classNames;
    }

}
