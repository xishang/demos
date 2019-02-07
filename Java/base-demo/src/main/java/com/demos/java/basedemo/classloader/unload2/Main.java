package com.demos.java.basedemo.classloader.unload2;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/9
 */
public class Main {

    public static void main(String... args) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InterruptedException {
        URL url = Main.class.getProtectionDomain().getCodeSource().getLocation();
        final String className = Main.class.getPackage().getName() + ".UtilityClass";
        {
            ClassLoader cl;
            Class clazz;

            for (int i = 0; i < 2; i++) {
                cl = new CustomClassLoader(url);
                clazz = cl.loadClass(className);
                loadClass(clazz);

                cl = new CustomClassLoader(url);
                clazz = cl.loadClass(className);
                loadClass(clazz);
                triggerGC();
            }
        }
        triggerGC();
    }

    private static void triggerGC() throws InterruptedException {
        System.out.println("\n-- Starting GC");
        System.gc();
        Thread.sleep(100);
        System.out.println("-- End of GC\n");
    }

    private static void loadClass(Class clazz) throws NoSuchFieldException,
            IllegalAccessException {
        final Field id = clazz.getDeclaredField("ID");
        id.setAccessible(true);
        id.get(null);
    }


    private static class CustomClassLoader extends URLClassLoader {
        public CustomClassLoader(URL url) {
            super(new URL[]{url}, null);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
            try {
                return super.loadClass(name, resolve);
            } catch (ClassNotFoundException e) {
                return Class.forName(name, resolve, Main.class.getClassLoader());
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            System.out.println(this.toString() + " - CL Finalized.");
        }
    }

}
