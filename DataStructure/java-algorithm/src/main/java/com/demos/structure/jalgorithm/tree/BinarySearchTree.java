package com.demos.structure.jalgorithm.tree;

import java.util.Iterator;

/**
 * 二叉查找树
 *
 * @param <T>
 */
public class BinarySearchTree<T extends Comparable<T>> implements Tree<T> {

    static class BinaryNode<T> {
        T data;
        BinaryNode<T> parent;
        BinaryNode<T> left;
        BinaryNode<T> right;

        public BinaryNode(T data, BinaryNode<T> parent, BinaryNode<T> left, BinaryNode<T> right) {
            this.data = data;
            this.parent = parent;
            this.left = left;
            this.right = right;
        }
    }

    // 根节点
    BinaryNode<T> root;
    int size;
    int modCount;

    public BinarySearchTree() {
        root = null;
        size = 0;
        modCount = 0;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
        modCount++;
    }

    @Override
    public boolean contains(T data) {
        return getNode(root, data) != null;
    }

    private BinaryNode<T> getNode(BinaryNode<T> node, T data) {
        if (node.data.equals(data)) {
            return node;
        }
        BinaryNode<T> findNode;
        if (node.left != null && (findNode = getNode(node.left, data)) != null) {
            return findNode;
        }
        if (node.right != null && (findNode = getNode(node.right, data)) != null) {
            return findNode;
        }
        return null;
    }

    @Override
    public T findMin() {
        if (root == null) {
            throw new RuntimeException("tree is empty...");
        }
        BinaryNode<T> node = root;
        while (node.left != null) {
            node = node.left;
        }
        return node.data;
    }

    @Override
    public T findMax() {
        if (root == null) {
            throw new RuntimeException("tree is empty...");
        }
        BinaryNode<T> node = root;
        while (node.right != null) {
            node = node.right;
        }
        return node.data;
    }

    @Override
    public void insert(T data) {
        if (root == null) {
            root = new BinaryNode<>(data, null, null, null);
        } else {
            insert(root, data);
        }
        size++;
        modCount++;
    }

    private void insert(BinaryNode<T> node, T data) {
        if (data.compareTo(node.data) < 0) { // data小于当前节点
            if (node.left == null) { // 左节点为空，插入新节点
                node.left = new BinaryNode<>(data, node, null, null);
            } else { // 左节点不为空，继续匹配
                insert(node.left, data);
            }
        } else if (data.compareTo(node.data) > 0) { // data大于当前节点
            if (node.right == null) { // 右节点为空，插入新节点
                node.right = new BinaryNode<>(data, node, null, null);
            } else { // 继续匹配
                insert(node.right, data);
            }
        } else { // 相等，抛出异常
            throw new RuntimeException("current data already exists");
        }
    }

    // 移除一个节点，并用该节点右子树最小节点替换该节点
    @Override
    public void remove(T data) {
        if (root == null) {
            throw new RuntimeException("tree is empty");
        }
        BinaryNode<T> node = getNode(root, data);
        if (node == null) {
            throw new RuntimeException("data doesn't exist");
        }
        if (node.left == null && node.right == null) { // 没有子节点，直接移除
            if (node.parent.left == node) {
                node.parent.left = null;
            }
            if (node.parent.right == node) {
                node.parent.right = null;
            }
        }
        BinaryNode<T> replace; // 替换节点
        if (node.right != null) { // 取右子树最小节点替换该节点
            replace = node.right;
            while (replace.left != null) {
                replace = replace.left;
            }

        } else { // 右子树为空时，取左子树最大节点替换该节点
            replace = node.left;
            while (replace.right != null) {
                replace = replace.right;
            }
        }
        // 替换节点temp从其父节点删除
        if (replace.parent.left == replace) {
            replace.parent.left = null;
        }
        if (replace.parent.right == replace) {
            replace.parent.right = null;
        }
        // 替换
        if (node.parent.left == node) {
            node.parent.left = replace;
        }
        if (node.parent.right == node) {
            node.parent.right = replace;
        }
        replace.left = node.left;
        replace.right = node.right;
        size--;
        modCount++;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            BinaryNode<T> current = root;
            int index = 0;

            @Override
            public boolean hasNext() {
                return index == size;
            }

            @Override
            public T next() {
                if (current.left != null) {
                    current = current.left;
                } else if (current.right != null) {
                    current = current.right;
                } else {
                    while (current.parent.right == current) { // 当前节点已遍历且是右节点，说明其父节点也已遍历
                        current = current.parent;
                    }
                    if (current.parent == null) {
                        throw new RuntimeException("tree is empty");
                    }
                    current = current.parent.right;
                    if (current == null) {
                        throw new RuntimeException("tree is empty");
                    }
                }
                index++;
                return current.data;
            }
        };
    }

}
