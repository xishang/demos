package com.demos.structure.jalgorithm.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * AVL树
 *
 * @param <T>
 */
public class AvlTree<T extends Comparable<T>> implements Tree<T> {

    static class Node<T> {
        T data; // 节点数据
        Node<T> left; // 左节点
        Node<T> right; // 右节点
        int height; // 节点高度

        public Node(T data, Node<T> left, Node<T> right) {
            this.data = data;
            this.left = left;
            this.right = right;
            this.height = 0;
        }
    }

    private Node<T> root;
    private int size;
    private int modCount;

    public AvlTree() {
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
        return getNode(data, root) != null;
    }

    private Node<T> getNode(T data, Node<T> node) {
        if (node == null) {
            return null;
        }
        if (data.compareTo(node.data) == 0) {
            return node;
        }
        Node<T> findNode;
        if ((findNode = getNode(data, node.left)) != null) {
            return findNode;
        }
        if ((findNode = getNode(data, node.right)) != null) {
            return findNode;
        }
        return null;
    }

    @Override
    public T findMin() {
        Node<T> min = findMin(root);
        if (min == null) {
            throw new RuntimeException("tree is empty...");
        }
        return min.data;
    }

    private Node<T> findMin(Node<T> node) {
        if (node == null) {
            return null;
        }
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    @Override
    public T findMax() {
        Node<T> max = findMax(root);
        if (max == null) {
            throw new RuntimeException("tree is empty...");
        }
        return max.data;
    }

    private Node<T> findMax(Node<T> node) {
        if (node == null) {
            return null;
        }
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }

    @Override
    public void insert(T data) {
        root = insert(data, root);
        size++;
        modCount++;
    }

    private Node<T> insert(T data, Node<T> node) {
        if (node == null) {
            return new Node<>(data, null, null);
        }
        int compareResult = data.compareTo(node.data);
        if (compareResult < 0) {
            node.left = insert(data, node.left);
        } else if (compareResult > 0) {
            node.right = insert(data, node.right);
        }
        // 插入新的节点后进行平衡二叉树操作
        return balance(node);
    }

    // 平衡二叉树
    private Node<T> balance(Node<T> node) {
        // 左子树比右子树的深度差超过1，需要旋转左子树以平衡二叉树
        if (getHeight(node.left) - getHeight(node.right) > 1) {
            // 需要左旋且[左子树的左子树深度] >= [左子树右子树深度]，进行单左旋操作即可
            if (getHeight(node.left.left) >= getHeight(node.left.right)) {
                node = rotateLeftChild(node);
            } else { // 需要左旋且左子树的右子树深度较大，需要进行双左旋操作
                node = doubleRotateLeftChild(node);
            }
        } else if (getHeight(node.right) - getHeight(node.left) > 1) { // 右子树比左子树的深度差超过1，需要旋转右子树以平衡二叉树
            // 需要右旋且[右子树的右子树深度] >= [右子树左子树深度]，进行单右旋操作即可
            if (getHeight(node.right.right) >= getHeight(node.right.left)) {
                node = rotateRightChild(node);
            } else {  // 需要右旋且右子树的左子树深度较大，需要进行双右旋操作
                node = doubleRotateRightChild(node);
            }
        }
        node.height = Math.max(getHeight(node.left), getHeight(node.right)) + 1;
        return node;
    }

    // 旋转左子树
    private Node<T> rotateLeftChild(Node<T> node) {
        // node的左节点取代node作为新的top节点
        Node<T> newTop = node.left;
        node.left = newTop.right;
        newTop.right = node;
        node.height = Math.max(getHeight(node.left), getHeight(node.right)) + 1;
        newTop.height = Math.max(getHeight(newTop.left), getHeight(newTop.right)) + 1;
        return newTop;
    }

    // 旋转右子树
    private Node<T> rotateRightChild(Node<T> node) {
        // node的右节点取代node作为新的top节点
        Node<T> newTop = node.right;
        node.right = newTop.left;
        newTop.left = node;
        node.height = Math.max(getHeight(node.left), getHeight(node.right)) + 1;
        newTop.height = Math.max(getHeight(newTop.left), getHeight(newTop.right)) + 1;
        return newTop;
    }

    // 双旋转左子树
    private Node<T> doubleRotateLeftChild(Node<T> node) {
        // 左子树的右子树较深，需要先将左子树进行"旋转右子树"操作，在将本节点进行"旋转左子树"操作
        node.left = rotateRightChild(node.left);
        return rotateLeftChild(node);
    }

    // 双旋转右子树
    private Node<T> doubleRotateRightChild(Node<T> node) {
        node.right = rotateLeftChild(node.right);
        return rotateRightChild(node);
    }

    private int getHeight(Node<T> node) {
        return node == null ? -1 : node.height;
    }

    @Override
    public void remove(T data) {
        remove(data, root);
    }

    private Node<T> remove(T data, Node<T> node) {
        if (node == null) {
            return null;
        }
        int compareResult = data.compareTo(node.data);
        if (compareResult < 0) {
            node.left = remove(data, node.left);
        } else if (compareResult > 0) {
            node.right = remove(data, node.right);
        } else { // 值相等，删除当前节点
            if (node.left != null && node.right != null) {
                // 左子树和右子树均不为null，将右子树的最小值作为当前节点值，并删除右子树最小节点
                T rightMin = findMin(node.right).data;
                // 赋值：右子树最小值
                node.data = rightMin;
                // 删除右子树最小值对应节点
                node.right = remove(rightMin, node.right);
            } else {
                // 只有左子树或右子树，直接将左子树或右子树赋给当前节点
                node = node.left != null ? node.left : node.right;
            }
        }
        // 平衡删除节点后的二叉树
        return balance(node);
    }

    public Iterator<T> preorderTraversal() {
        List<T> list = preorderList(root, new ArrayList<>());
        return list.iterator();
    }

    // 简单起见，直接先序遍历二叉树，并将结果存入List中
    private List<T> preorderList(Node<T> node, List<T> list) {
        if (node == null) {
            return list;
        }
        // 先序遍历：当前节点-->左子树-->右子树
        // 1.先遍历当前节点
        list.add(node.data);
        // 2.遍历左子树
        preorderList(node.left, list);
        // 3.遍历右子树
        preorderList(node.right, list);
        return list;
    }

    public Iterator<T> inorderTraversal() {
        List<T> list = inorderList(root, new ArrayList<>());
        return list.iterator();
    }

    // 中序遍历：左子树-->当前节点-->右子树
    private List<T> inorderList(Node<T> node, List<T> list) {
        if (node == null) {
            return list;
        }
        inorderList(node.left, list);
        list.add(node.data);
        inorderList(node.right, list);
        return list;
    }

    public Iterator<T> postorderTraversal() {
        List<T> list = postorderList(root, new ArrayList<>());
        return list.iterator();
    }

    // 后序遍历：左子树-->右子树-->当前节点
    private List<T> postorderList(Node<T> node, List<T> list) {
        if (node == null) {
            return list;
        }
        postorderList(node.left, list);
        postorderList(node.right, list);
        list.add(node.data);
        return list;
    }

    @Override
    public Iterator<T> iterator() {
        return inorderTraversal();
    }

}
