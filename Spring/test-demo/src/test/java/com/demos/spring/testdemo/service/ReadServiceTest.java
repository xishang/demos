package com.demos.spring.testdemo.service;

import com.demos.spring.testdemo.bean.Book;
import com.demos.spring.testdemo.config.MvcConfig;
import com.demos.spring.testdemo.service.impl.ReadServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.mockito.Mockito.*;

/**
 * Created by xishang on 2017/8/2.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {MvcConfig.class})
public class ReadServiceTest {

    // @InjectMocks 加 MockitoAnnotations.initMocks(this); 实现使用 @Mock 替换掉真实调用
    // 使用 @Resource 会调用真实接口
    @InjectMocks
    private ReadService readService = new ReadServiceImpl();

    @Mock
    private BookService bookService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Book book = new Book();
        book.setName("Mock Book");
        book.setContent("---------------Mock Content---------------");
        when(bookService.getBook(anyString())).thenReturn(book);
    }

    @Test
    public void testRead() {
        readService.read("三国演义");
        readService.read("三国演义");
        // 验证是否调用过 bookService.getBook("{任意参数}")，且调用了2次
        verify(bookService, times(2)).getBook(anyString());
        // 验证是否调用过 bookService.getBook("三国")，相当于 verify(bookService, times(1)).getBook("三国演义"); 因此此处会报错
        verify(bookService).getBook(eq("三国演义"));
    }

}
