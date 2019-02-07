package com.demos.structure.jalgorithm.tree;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/12/14
 * <p>
 * 红黑树: 更常被使用
 * 与AVL树的比较:
 * 1.AVL树比红黑树更平衡, 因此查询效率比红黑树高;
 * 但插入或删除数据时更容易引起树的unbalance, 导致更加频繁的rebalance, 在大量插入或删除的情况下红黑树比AVL树效率高
 * 2.在插入或删除引起unbalance时, 最坏情况下, AVL需要维护从被删node到root这条路径上所有node的平衡性, 因此需要旋转的量级O(logN);
 * 而RB-Tree最多只需3次旋转，因此只需要O(1)的复杂度
 */
public class RedBlackTree2<T extends Comparable<? super T>> {

    public static final int BLACK = 1;
    public static final int RED = 0;

    static class RedBlackNode<T> {

        private T element; // data
        private RedBlackNode<T> left; // left child
        private RedBlackNode<T> right; // right child
        private int color; // color

        RedBlackNode(T element) {
            this(element, null, null);
        }

        RedBlackNode(T element, RedBlackNode<T> left, RedBlackNode<T> right) {
            this.element = element;
            this.left = left;
            this.right = right;
            this.color = RedBlackTree2.BLACK;
        }

    }

    private RedBlackNode<T> header;
    private RedBlackNode<T> nullNode;

    /**
     * 单旋转
     * Internal routine that performs a single or double rotation.
     * Because the result is attached to the parent, there are four cases.
     * Called by handleReorient.
     *
     * @param item
     * @param parent
     * @return
     */
    private RedBlackNode<T> rotate(T item, RedBlackNode<T> parent) {
        if (compare(item, parent) < 0) {
            return parent.left = compare(item, parent.left) < 0 ?
                    rotateWithLeftChild(parent.left) : // LL
                    rotateWithRightChild(parent.left); // LR
        } else {
            return parent.right = compare(item, parent.right) < 0 ?
                    rotateWithLeftChild(parent.right) : // RL
                    rotateWithRightChild(parent.right); // RR
        }
    }

    private int compare(T item, RedBlackNode<T> node) {
        if (node == header) {
            return 1;
        } else {
            return item.compareTo(node.element);
        }
    }

    /**
     * todo
     *
     * @param node
     * @return
     */
    private RedBlackNode<T> rotateWithLeftChild(RedBlackNode<T> node) {
        return node;
    }

    /**
     * todo
     *
     * @param node
     * @return
     */
    private RedBlackNode<T> rotateWithRightChild(RedBlackNode<T> node) {
        return node;
    }

    /* ------------------------ 双旋转 ------------------------ */

    // Used in insert routine and its helpers
    private RedBlackNode<T> current;
    private RedBlackNode<T> parent;
    private RedBlackNode<T> grant;
    private RedBlackNode<T> great;

    /**
     * Internal routine that is called during an insertion if a node has two red children.
     * Performs flip and rotations.
     *
     * @param item
     */
    private void handleReorient(T item) {
        current.color = RED;
        current.left.color = BLACK;
        current.right.color = BLACK;
        if (parent.color == RED) { // have to rotate
            grant.color = RED;
            if ((compare(item, grant)) != compare(item, parent)) {
                parent = rotate(item, grant); // start dbl rotate
            }
            current = rotate(item, great);
            current.color = BLACK;
        }
        header.right.color = BLACK; // make root black
    }

    public void insert(T item) {
        current = parent = grant = header;
        nullNode.element = item;
        while (compare(item, current) != 0) {
            great = grant;
            grant = parent;
            parent = current;
            current = compare(item, current) < 0 ? current.left : current.right;
            // check if two red children; fix if so
            if (current.left.color == RED && current.right.color == RED) {
                handleReorient(item);
            }
        }
        // insertion fails if already present
        if (current != nullNode) {
            return;
        }
        current = new RedBlackNode<>(item, nullNode, nullNode);
        // attach to parent
        if (compare(item, parent) < 0) {
            parent.left = current;
        } else {
            parent.right = current;
        }
        handleReorient(item);
    }

}
