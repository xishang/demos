package com.demo.framework.api;

import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/19
 */
public interface ProductService {

    Long addProduct(String productName);

    List<String> getProductNames();

}
