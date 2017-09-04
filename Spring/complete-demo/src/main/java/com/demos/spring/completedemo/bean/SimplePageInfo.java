package com.demos.spring.completedemo.bean;

import com.github.pagehelper.Page;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Created by xishang on 2017/8/14.
 * <p>
 * 取代com.github.pagehelper.PageInfo包装类，只保留必要的属性
 */
public class SimplePageInfo<T> implements Serializable {

    private int pageNum;    // 第几页
    private int pageSize;   // 每页记录数
    private int pages;      // 总页数
    private List<T> list;   // 结果集
    private int size;       // 当前页的数据条数
    private long total;     // 总记录数

    /**
     * PageHelper.startPage()将返回一个Page对象，该对象继承自ArrayList，包装了分页信息
     *
     * @param list
     */
    public SimplePageInfo(List<T> list) {
        if (list instanceof Page) {
            Page page = (Page) list;
            this.pageNum = page.getPageNum();
            this.pageSize = page.getPageSize();
            this.pages = page.getPages();
            this.list = page;
            this.size = page.size();
            this.total = page.getTotal();
        } else if (list instanceof Collection) {
            this.pageNum = 1;
            this.pageSize = list.size();
            this.pages = 1;
            this.list = list;
            this.size = list.size();
            this.total = (long) list.size();
        }
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

}
