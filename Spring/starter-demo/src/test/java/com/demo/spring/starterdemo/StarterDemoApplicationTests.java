package com.demo.spring.starterdemo;

import com.demo.spring.starterdemo.domain.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StarterDemoApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testGetUserByName() {
        User user = restTemplate.getForObject("/users/name/詹姆斯", User.class);
        Assert.assertEquals(user.getName(), "詹姆斯");
    }

    @Test
    public void testAddUser() {
        MultiValueMap map = new LinkedMultiValueMap();
        map.add("username", "u-" + System.currentTimeMillis());
        map.add("password", "123456");
        map.add("name", "");
        map.add("phone", "18987654321");
        int result = restTemplate.postForObject("/users", map, Integer.class);
        Assert.assertEquals(result, 1);
    }

}
