package com.demos.structure.jalgorithm.list;

public interface List<T> extends Iterable<T> {

    int size();

    boolean isEmpty();

    void clear();

    T get(int index);

    void set(int index, T data);

    void add(T data);

    void add(int index, T data);

    T remove(int index);

}
