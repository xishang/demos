package com.demos.java.basedemo.concurrent;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/21
 */
public class UnsafeTest {

    private static final Unsafe unsafe = getUnsafe();

    private static final Object obj = new Object();

    private static final Runnable r = () -> {
//        synchronized (obj) {
        try {
            Thread.sleep(3000);
            System.out.println("Unsafe.park()-------- start ---------");
            unsafe.park(false, 0);
//                obj.wait();
            System.out.println("Unsafe.park()-------- end ---------");
        } catch (Exception e) {
            System.out.println();
        }
//        }
        System.out.println("isInterrupted: " + Thread.currentThread().isInterrupted());
    };

    private static Unsafe getUnsafe() {
        try {
            Class<Unsafe> clazz = Unsafe.class;
//            Constructor[] constructor = clazz.getDeclaredConstructors();
//            Unsafe unsafe = (Unsafe) constructor[0].newInstance();
            Field field = clazz.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main1(String[] args) {
        System.out.println("测试开始");
        Thread t = new Thread(r);
        t.start();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Sleep 出错");
        }
        unsafe.unpark(t);
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            System.out.println("Sleep 出错");
        }
//        synchronized (obj) {
//            obj.notifyAll();
//        }
//        unsafe.unpark(t);
        t.interrupt();
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            System.out.println("Sleep 出错");
        }
        System.out.println("测试结果");
    }

    public static void main(String[] args) {
        String result = "00001161{H:01ZJCGDZZHDZZH10002220161102151515ZXYH201611020000014507                      1                    }<?xml version=\"1.0\" encoding=\"GBK\"?><Document><CommonRequestHeader><firstsysname></firstsysname><firstsysmemucode></firstsysmemucode><firstsysmemuname></firstsysmemuname><firstsysdate></firstsysdate><firstsystime></firstsystime><firstsysseq></firstsysseq><requesttrancode></requesttrancode><requestseq>ZJCGPAY34002</requestseq><xmlplatformnbr>100102</xmlplatformnbr><brno></brno><tellerno></tellerno><authtellerno></authtellerno><reviewtellrno></reviewtellrno><pageflag></pageflag><currpage></currpage><pagenum></pagenum><smssendyn>Y</smssendyn></CommonRequestHeader><Content><daccounttype>1</daccounttype><medumpswd></medumpswd><caccounttype>2</caccounttype><tranamt>100</tranamt><feeamt>0</feeamt><trancur>CNY</trancur><payercardnbr>6223077200000504594</payercardnbr><payercardname>**华</payercardname><payeecardnbr>6231460511000000418</payeecardnbr><payeecardname>**华</payeecardname><custpaper></custpaper><payeridtype>0</payeridtype><payerid>36042119805230027</payerid><payerphoneno>13509992848</payerphoneno><innerflag></innerflag></Content></Document>";
        System.out.println(result.substring(result.indexOf("}") + 1));
    }

}
