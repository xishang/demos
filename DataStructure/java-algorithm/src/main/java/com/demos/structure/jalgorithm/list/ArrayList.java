package com.demos.structure.jalgorithm.list;

import java.util.Iterator;

/**
 * 自定义ArrayList实现方式
 * @param <T>
 */
public class ArrayList<T> implements List<T> {

    private static final int DEFAULT_CAPACITY = 1 << 4;

    // 数据数组
    private T[] items;
    // 数据条数
    private int size;
    // 修改次数
    private int modCount;

    public ArrayList() {
        this(DEFAULT_CAPACITY);
    }

    public ArrayList(int capacity) {
        this.items = (T[]) new Object[capacity];
        this.size = 0;
        this.modCount = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        // 所有数据置为null
        for (int i = 0; i < size; i++) {
            items[i] = null;
        }
        size = 0;
        modCount++;
    }

    @Override
    public T get(int index) {
        checkIndex(index);
        return items[index];
    }

    @Override
    public void set(int index, T data) {
        checkIndex(index);
        items[index] = data;
        modCount++;
    }

    @Override
    public void add(T data) {
        ensureCapacity(size + 1);
        items[size++] = data;
        modCount++;
    }

    @Override
    public void add(int index, T data) {
        checkIndex(index);
        ensureCapacity(size + 1);
        for (int i = size; i > index; i--) {
            items[i] = items[i - 1];
        }
        items[index] = data;
        size++;
        modCount++;
    }

    @Override
    public T remove(int index) {
        checkIndex(index);
        T data = items[index];
        for (int i = index; i < size - 1; i++) {
            items[i] = items[i + 1];
        }
        items[size - 1] = null;
        size--;
        modCount++;
        return data;
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayListIterator();
    }

    // 检查index是否越界
    private void checkIndex(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
    }

    // 检查数组容量，若不满足要求则进行扩展
    private void ensureCapacity(int minSize) {
        if (minSize > items.length) {
            // 扩展长度：2 * old + 1
            int newCapacity = (items.length << 1) + 1;
            int expandCapacity = Math.max(minSize, newCapacity);
            expand(expandCapacity);
        }
    }

    // 扩展数组
    private void expand(int capacity) {
        T[] newItems = (T[]) new Object[capacity];
        // 数组拷贝
        System.arraycopy(items, 0, newItems, 0,
                Math.min(items.length, capacity));
        items = newItems;
    }

    // 泛型符号<T>已经在ArrayList中定义，因此ArrayListIterator不能再用泛型符号<T>，此处也无需定义泛型符号
    // 可以用别的符号：如<E>，但并不会用到，因为此处需要的是一个Iterator<T>的类型，作为ArrayList中iterator()方法的返回类型
    class ArrayListIterator implements java.util.Iterator<T> {

        int current = 0;

        @Override
        public boolean hasNext() {
            return current < ArrayList.this.size;
        }

        @Override
        public T next() {
            return ArrayList.this.get(current++);
        }

        @Override
        public void remove() {
            ArrayList.this.remove(--current);
        }

    }

}
