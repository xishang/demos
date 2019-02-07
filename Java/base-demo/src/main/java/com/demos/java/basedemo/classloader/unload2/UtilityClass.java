package com.demos.java.basedemo.classloader.unload2;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/9
 */
class UtilityClass {

    static final String ID = Integer.toHexString(System.identityHashCode(UtilityClass.class));

    private static final Object FINAL = new Object() {
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            System.out.println(ID + " Finalized.");
        }
    };

    static {
        System.out.println(ID + " Initialising");
    }
}
