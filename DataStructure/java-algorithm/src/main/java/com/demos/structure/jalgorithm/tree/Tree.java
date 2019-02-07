package com.demos.structure.jalgorithm.tree;

public interface Tree<T> extends Iterable<T> {

    boolean isEmpty();

    int size();

    void clear();

    boolean contains(T data);

    T findMin();

    T findMax();

    void insert(T data);

    void remove(T data);

}
