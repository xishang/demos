package com.demos.java.framework.asm;

import com.demos.java.framework.asm.method.AddMethodAdapter;
import com.demos.java.framework.asm.util.ClassLoaderUtils;
import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/15
 */
public class AddFieldTest {

    public static void main1(String[] args) throws Exception {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//        CheckClassAdapter ca = new CheckClassAdapter(cw);
//        AddFieldAdapter af = new AddFieldAdapter(cw, ACC_PUBLIC, "age", "I");
//        ChangeFieldAdapter cf = new ChangeFieldAdapter(af, ACC_PUBLIC | ACC_STATIC, "name");
//        RemoveFieldAdapter rf = new RemoveFieldAdapter(cf, "kk");

        AddMethodAdapter am = new AddMethodAdapter(cw);

        ClassReader cr = new ClassReader("com.demos.java.framework.asm.Teacher");
        cr.accept(am, SKIP_DEBUG);

        generateBasicClass(am);

        byte[] data = cw.toByteArray();
        File file = new File("/Users/xishang/script/Teacher.class");
        FileOutputStream out = new FileOutputStream(file);
        out.write(data);
        out.close();


        // 运行
        Class clazz = ClassLoaderUtils.findClass("C", cw.toByteArray());
        Object o = clazz.newInstance();
        Method m = clazz.getMethod("print1", String.class);
        m.invoke(o, "hello");
    }

    protected static void generateBasicClass(ClassVisitor cv) {
        FieldVisitor fv;
        MethodVisitor mv;
        cv.visit(V9, ACC_PUBLIC, "C", null, "java/lang/Object", null);
        cv.visitSource("C.java", null);
        fv = cv.visitField(ACC_PUBLIC, "f", "I", null, null);
        if (fv != null) {
            fv.visitEnd();
        }
//        mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
//        if (mv != null) {
//            mv.visitCode();
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
//                    "()V");
//            mv.visitInsn(RETURN);
//            mv.visitMaxs(1, 1);
//            mv.visitEnd();
//        }
        mv = cv.visitMethod(ACC_PUBLIC, "m", "()V", null, null);
        if (mv != null) {
            mv.visitCode();
            mv.visitLdcInsn(new Long(100));
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep",
                    "(J)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 1);
            mv.visitEnd();
        }
        cv.visitEnd();
    }

    /**
     * invoke* demo
     *
     * @param mv
     * @param exceptionClass
     * @param msg
     * @param argType
     * @param instruction
     * @param slot
     */
    private static void enhanceForThrowingException(MethodVisitor mv, Class<? extends Exception> exceptionClass, String msg, String argType, int instruction, int slot) {
        String exceptionClassNm = Type.getInternalName(exceptionClass);
        mv.visitTypeInsn(NEW, exceptionClassNm);
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitLdcInsn(msg);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
        mv.visitVarInsn(instruction, slot);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + argType + ")Ljava/lang/StringBuilder;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        mv.visitMethodInsn(INVOKESPECIAL, exceptionClassNm, "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
    }

    public static void test() {
        Object obj = new Teacher();
        MethodAccess ma = MethodAccess.get(Teacher.class);
        FieldAccess fa = FieldAccess.get(Teacher.class);
        ma.invoke(obj, "teach");
        Object name = fa.get(obj, "name");
    }

    public static void main(String[] args) {
    }

}
