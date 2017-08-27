package com.demos.structure.jalgorithm.list;

import java.util.Iterator;

/**
 * 自定义LinkedList实现方式
 * @param <T>
 */
public class LinkedList<T> implements List<T> {

    static class Node<T> {
        T data;
        Node prev;
        Node next;
        public Node(T data, Node prev, Node next) {
            this.data = data;
            this.prev = prev;
            this.next = next;
        }
    }

    // 头节点
    private Node<T> startNode;
    // 尾节点
    private Node<T> endNode;
    // list的大小
    private int size;
    // 修改次数
    private int modCount;

    public LinkedList() {
        doClear();
        modCount = 0;
    }

    private void doClear() {
        startNode = new Node<>(null, null, null);
        endNode = new Node<>(null, startNode, null);
        startNode.next = endNode;
        size = 0;
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
        doClear();
        modCount++;
    }

    @Override
    public T get(int index) {
        checkIndex(index);
        Node<T> node = getNode(index);
        return node.data;
    }

    @Override
    public void set(int index, T data) {
        checkIndex(index);
        Node<T> node = getNode(index);
        node.data = data;
        modCount++;
    }

    @Override
    public void add(T data) {
        Node<T> newNode = new Node<>(data, endNode.prev, endNode);
        endNode.prev.next = newNode;
        endNode.prev = newNode;
        size++;
        modCount++;
    }

    @Override
    public void add(int index, T data) {
        checkIndex(index);
        Node<T> node = getNode(index);
        Node<T> newNode = new Node<>(data, node.prev, node);
        node.prev.next = newNode;
        node.prev = newNode;
        size++;
        modCount++;
    }

    @Override
    public T remove(int index) {
        checkIndex(index);
        Node<T> node = getNode(index);
        node.prev.next = node.next;
        node.next.prev = node.prev;
        size--;
        modCount++;
        return node.data;
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    // 检查index是否越界
    private void checkIndex(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
    }

    private Node<T> getNode(int index) {
        Node<T> node;
        // 从接近的一端开始遍历
        if (index < size / 2) {
            node = startNode;
            for (int i = 0; i < index + 1; i++) {
                node = node.next;
            }
        } else {
            node = endNode;
            for (int i = size; i > index; i--) {
                node = node.prev;
            }
        }
        return node;
    }

    class LinkedListIterator implements Iterator<T> {

        int current = 0;

        @Override
        public boolean hasNext() {
            return current < LinkedList.this.size;
        }

        @Override
        public T next() {
            return LinkedList.this.get(current++);
        }

        @Override
        public void remove() {
            LinkedList.this.remove(--current);
        }

    }

}
