package com.demos.structure.jalgorithm.list;

/**
 * 简单栈实现
 */
public class SimpleStack<T> {

    static class Node<T> {
        T data;
        Node next;
        public Node(T data, Node next) {
            this.data = data;
            this.next = next;
        }
    }

    private Node<T> top;
    private int size;

    public SimpleStack() {
        top = null;
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        top = null;
        size = 0;
    }

    // 入栈
    public void push(T data) {
        top = new Node<>(data, top);
        size++;
    }

    // 出栈
    public T pop() {
        if (size == 0) {
            return null;
        }
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Node node = top;
        sb.append(node.data.toString()).append(", ");
        while(node.next != null) {
            node = node.next;
            sb.append(node.data.toString()).append(", ");
        }
        return sb.substring(0, sb.length() - 2) + "]";
    }

}
