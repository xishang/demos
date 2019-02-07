package com.demos.java.basedemo;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/7/14
 */
public class JdbcTest {

    public static void main(String[] args) throws Exception {
        try {
            DriverManager.registerDriver(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
        Class.forName("com.mysql.jdbc.Driver");
        System.out.println();
        DriverManager.getConnection("jdbc:mysql://localhost/test");
    }

}
