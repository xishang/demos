package com.demos.structure.jalgorithm.tree;

import java.util.Iterator;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/29
 * <p>
 * 红黑树: 一种自平衡排序二叉树
 * <p>
 * 性质:
 * 1.每个节点是红色或黑色的
 * 2.根节点是黑色的
 * 3.所有叶节点(NIL节点)是黑色的
 * 4.如果一个节点是红色的, 则它两个子节点都是黑色的(即: 在一条路径上不能出现相邻的两个红色节点)
 * 5.从任一节点到其每个叶子的所有简单路径都包含相同数目的黑色节点
 * <p>
 * 与AVL树的比较:
 * 1.AVL树比红黑树更平衡, 因此查询效率比红黑树高;
 * 但插入或删除数据时更容易引起树的unbalance, 导致更加频繁的reBalance, 在大量插入或删除的情况下红黑树比AVL树效率高
 * 2.在插入或删除引起unbalance时, 最坏情况下, AVL需要维护从被删node到root这条路径上所有node的平衡性, 因此需要旋转的量级O(logN);
 * 而RB-Tree最多只需3次旋转，因此只需要O(1)的复杂度
 */
public class RBTree<T> implements Tree<T> {

    private RBNode root;

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean contains(T data) {
        return false;
    }

    @Override
    public T findMin() {
        return null;
    }

    @Override
    public T findMax() {
        return null;
    }

    @Override
    public void insert(T data) {

    }

    @Override
    public void remove(T data) {

    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    /**
     * 红黑树节点
     */
    static class RBNode {
        Object value;
        RBNode parent;
        RBNode left;
        RBNode right;
        boolean red;

        RBNode(Object value) {
            this.value = value;
            this.red = true; // 默认为red节点, 因为如果直接插入black节点会破坏结构
        }
    }

}
