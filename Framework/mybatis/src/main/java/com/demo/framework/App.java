package com.demo.framework;

import com.demo.framework.mapper.UserMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        InputStream inputStream = App.class.getResourceAsStream("/mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        SqlSession session = sqlSessionFactory.openSession();
        try {
            UserMapper mapper = session.getMapper(UserMapper.class);
            int count = mapper.selectCount();
            System.out.println("count = " + count);
        } finally {
            session.close();
        }
    }
}
