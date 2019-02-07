package com.demos.java.framework.asm.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import static org.objectweb.asm.Opcodes.ASM6;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/15
 */
public class RemoveFieldAdapter extends ClassVisitor {

    private String fieldName;

    public RemoveFieldAdapter(ClassVisitor cv, String fieldName) {
        super(ASM6, cv);
        this.fieldName = fieldName;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (name.equals(fieldName)) {
            // do not delegate to next visitor -> this removes the field
            return null;
        }
        return cv.visitField(access, name, desc, signature, value);
    }

    @Override
    public void visitEnd() {
        System.out.println();
        super.visitEnd();
    }
}
