package com.demos.spring.testdemo.controller;

import com.demos.spring.testdemo.config.MvcConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by xishang on 2017/8/2.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration // 单元测试时开启一个web服务
@ContextConfiguration(classes = {MvcConfig.class})
public class BookControllerTest {

    @Autowired
    private WebApplicationContext wac;

    // MockMvc实现了对Http请求的模拟，能够直接使用网络的形式，转换到Controller的调用
    private MockMvc mockMvc;

    // @Before注解的方法在test方法执行之前被调用，相应的还有@After注解
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testGetBook() throws Exception {
        String res = this.mockMvc.perform(get("/book/三国演义").characterEncoding("UTF-8")/*若不设置characterEncoding，则传递中文时会乱码*/)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println(res);
    }

}
