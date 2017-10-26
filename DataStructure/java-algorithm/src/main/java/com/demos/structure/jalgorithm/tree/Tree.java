package com.demos.structure.jalgorithm.tree;

import java.util.Iterator;

public interface Tree<T> extends Iterable<T> {

    boolean isEmpty();

    int size();

    void clear();

    boolean contains(T data);

    T findMin();

    T findMax();

    void insert(T data);

    void remove(T data);

    Iterator<T> preorderTraversal();

    Iterator<T> inorderTraversal();

    Iterator<T> postorderTraversal();

}
