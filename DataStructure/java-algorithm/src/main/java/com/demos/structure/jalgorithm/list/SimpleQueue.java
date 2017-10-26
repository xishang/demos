package com.demos.structure.jalgorithm.list;

/**
 * 简单的队列实现
 */
public class SimpleQueue<T> {

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

    private Node first;
    private Node last;
    private int size;

    public SimpleQueue() {
        doClear();
    }

    private void doClear() {
        first = new Node(null, null, null);
        last = new Node(null, first, null);
        first.next = last;
        size = 0;
    }

    public void clear() {
        doClear();
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    // 入队
    public void offer(T data) {
        Node node = new Node(data, last.prev, last);
        last.prev.next = node;
        last.prev = node;
        size++;
    }

    // 出队
    public T pop() {
        if (size == 0) {
            return null;
        }
        Node<T> node = first.next;
        first.next = node.next;
        node.next.prev = first;
        size--;
        return node.data;
    }

    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Node node = first.next;
        for (int i = 0; i < size; i++) {
            sb.append(node.data.toString()).append(", ");
            node = node.next;
        }
        return sb.substring(0, sb.length() - 2) + "]";
    }

}
