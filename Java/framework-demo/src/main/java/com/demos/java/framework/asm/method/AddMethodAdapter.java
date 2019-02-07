package com.demos.java.framework.asm.method;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/15
 */
public class AddMethodAdapter extends ClassVisitor {

    private boolean isInterface;

    private boolean isMethodPresent;

    private boolean isExist = false;

    public AddMethodAdapter(ClassVisitor cv) {
        super(ASM6, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
        isInterface = (access & ACC_INTERFACE) != 0;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals("print1") && desc.equals("(Ljava/lang/String;)V")) {
            isMethodPresent = true;
        }
        return cv.visitMethod(access, name, desc, signature, exceptions);
    }

    /**
     * 添加方法
     * Java代码:
     * <p>
     * public void print1(String content) {
     * System.out.println(content);
     * }
     * <p>
     * class字节码:
     * <p>
     * public void print1(java.lang.String);
     * descriptor: (Ljava/lang/String;)V
     * flags: ACC_PUBLIC
     * Code:
     * stack=2, locals=2, args_size=2
     * 0: getstatic     #20                 // Field java/lang/System.out:Ljava/io/PrintStream;
     * 3: aload_1
     * 4: invokevirtual #27                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
     * 7: return
     */
    public void visitEnd() {
        if (!isExist && !isMethodPresent && !isInterface) {
            MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "print1", "(Ljava/lang/String;)V", null, null);
            mv.visitCode();
            // getstatic     #20                 // Field java/lang/System.out:Ljava/io/PrintStream;
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            // aload_1
            mv.visitVarInsn(ALOAD, 1);
            // invokevirtual #27                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            // return
            mv.visitInsn(RETURN);
            // stack=2, locals=2, args_size=2
            mv.visitMaxs(2, 2);
            mv.visitEnd();
            isExist = true;
        }
        cv.visitEnd();
    }

}
