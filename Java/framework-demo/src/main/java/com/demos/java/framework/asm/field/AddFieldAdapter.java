package com.demos.java.framework.asm.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import static org.objectweb.asm.Opcodes.ASM6;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/15
 */
public class AddFieldAdapter extends ClassVisitor {

    private int fAcc;

    private String fName;

    private String fDesc;

    private boolean isFieldPresent;

    public AddFieldAdapter(ClassVisitor cv, int fAcc, String fName, String fDesc) {
        // api=Opcodes.ASM6, 用来进行版本控制, 该版本只支持[asm4.0, asm6.0]
        super(ASM6, cv);
        this.fAcc = fAcc;
        this.fName = fName;
        this.fDesc = fDesc;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (name.equals(fName)) {
            isFieldPresent = true;
        }
        return cv.visitField(access, name, desc, signature, value);
    }

    /**
     * 访问结束, 若字段不存在, 则添加
     */
    @Override
    public void visitEnd() {
        if (!isFieldPresent) {
            FieldVisitor fv = cv.visitField(fAcc, fName, fDesc, null, null);
            if (fv != null) {
                fv.visitEnd();
            }
        }
        cv.visitEnd();
    }

}
