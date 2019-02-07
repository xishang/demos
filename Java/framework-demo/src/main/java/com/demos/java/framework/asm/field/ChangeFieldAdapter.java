package com.demos.java.framework.asm.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import static org.objectweb.asm.Opcodes.ASM6;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/15
 */
public class ChangeFieldAdapter extends ClassVisitor {

    private int fAcc;

    private String fName;

    public ChangeFieldAdapter(ClassVisitor cv, int fAcc, String fName) {
        super(ASM6, cv);
        this.fAcc = fAcc;
        this.fName = fName;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (name.equals(fName)) { // 要修改的字段
            return cv.visitField(fAcc, name, desc, signature, value);
        }
        return cv.visitField(access, name, desc, signature, value);
    }

    @Override
    public void visitEnd() {
        System.out.println();
    }
}
