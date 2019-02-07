package com.demos.structure.jalgorithm.tree;

/**
 * 平衡二叉树-红黑树 红黑树性质： 1、根节点是黑色 2、节点颜色只能是红色或黑色的 3、红色节点的两个儿子只能是黑色的 4、NIL节点看作是黑色的
 * 5、任意节点到子孙的空节点的路径上的黑色节点数目相同
 *
 * @author 过路的守望
 *
 */
public class RedBlackTree {

    /*
     * NIL节点(无元素)
     */
    private RedBlackNode NIL = new RedBlackNode(Color.Black);
    /*
     * 红黑树的根
     */
    private RedBlackNode root = NIL;
    /*
     * 红黑树节点数目
     */
    private int size = 0;

    /**
     * 构造方法
     *
     * @param root
     */
    public RedBlackTree(RedBlackNode root) {
        this.root = root;
    }

    public RedBlackTree() {

    }

    /**
     * 测试
     *
     * @param args
     */
    public static void main(String[] args) {
//        RedBlackTree2 redBlackTree2 = new RedBlackTree2();
//        redBlackTree2.insert(6);
//        redBlackTree2.insert(4);
//        redBlackTree2.insert(5);
//        redBlackTree2.insert(7);
//        redBlackTree2.insert(3);
//        redBlackTree2.insert(8);
//        redBlackTree2.remove(6);
//        redBlackTree2.remove(3);
//        redBlackTree2.remove(8);
//        redBlackTree2.remove(7);
//        redBlackTree2.remove(4);
//        redBlackTree2.remove(5);
//        System.out.println("MAX:" + redBlackTree2.findMax());
//        System.out.println("MIN:" + redBlackTree2.findMin());
//        redBlackTree2.preOrder(redBlackTree2.root);
//        redBlackTree2.inOrder(redBlackTree2.root);
//        redBlackTree2.postOrder(redBlackTree2.root);
//        redBlackTree2.levelOrder(redBlackTree2.root);
    }

    /**
     * 红黑树中是否含有node节点
     *
     * @param node
     *            递归实现
     * @return
     */
    public boolean containsRec(RedBlackNode node, RedBlackNode root) {
        /*
         * 红黑树为空
         */
        if (root == NIL) {
            return false;
        }
        /*
         * 搜索右子树
         */
        if (node.value > root.value) {
            return contains(node, root.right);
        }
        /*
         * 搜索左子树
         */
        if (node.value < root.value) {
            return contains(node, root.left);
        }
        /*
         * 包含node节点
         */
        return true;

    }

    /**
     * 红黑树中是否含有node节点
     *
     * @param node
     * @return
     */
    public boolean contains(RedBlackNode node, RedBlackNode root) {
        /*
         * 红黑树为空
         */
        if (root == NIL) {
            return false;
        }
        while (node.value != root.value) {
            /*
             * 搜索右子树
             */
            if (node.value > root.value) {
                root = root.right;
            }
            /*
             * 搜索左子树
             */
            else {
                root = root.left;
            }
            /*
             * 未在红黑树中为找到node节点
             */
            if (root == NIL) {
                return false;
            }
        }
        return true;
    }

    /**
     * 返回红黑树中的最小节点
     *
     * @return
     */
    public int findMin() {
        RedBlackNode value = findMin(root);
        if (value == null) {
            return -1;
        } else {
            return value.value;
        }
    }

    /**
     * 在指定红黑树中搜寻最小节点 递归搜索
     *
     * @param root
     * @return
     */
    private RedBlackNode findMin(RedBlackNode root) {
        /*
         * 指定红黑树为空树
         */
        if (root == NIL) {
            return null;
        }
        /*
         * 如果红黑树的左子树非空，则搜索其左子树
         */
        if (root.left != NIL) {
            return findMin(root.left);
        }
        return root;
    }

    /**
     * 返回红黑树中的最大节点
     *
     * @return
     */
    public int findMax() {
        RedBlackNode value = findMax(root);
        if (value == null) {
            return -1;
        } else {
            return value.value;
        }
    }

    /**
     * 搜索指定红黑树中的最大节点 递归实现
     *
     * @param root
     * @return
     */
    private RedBlackNode findMax(RedBlackNode root) {
        /*
         * 红黑树为空
         */
        if (root == NIL) {
            return null;
        }
        /*
         * 搜索右子树
         */
        if (root.right != NIL) {
            return findMax(root.right);
        }
        return root;
    }

    /**
     * 得到祖父节点
     *
     * @param node
     * @return
     */
    private RedBlackNode getGrandParent(RedBlackNode node) {
        /* 父亲节点为空 */
        if (node.parent == null) {
            return null;
        }
        return node.parent.parent;
    }

    /**
     * 得到叔父节点
     *
     * @param node
     * @return
     */
    private RedBlackNode getUncle(RedBlackNode node) {
        RedBlackNode grandParent = getGrandParent(node);
        /*
         * 祖父节点为空
         */
        if (grandParent == null) {
            return null;
        }
        /*
         * 父亲节是祖父节点的左儿子
         */
        if (node.parent == grandParent.left) {
            return grandParent.right;
        }
        /*
         * 父亲节点时祖父节点的右儿子
         */
        else {
            return grandParent.left;
        }
    }

    /**
     * 左旋 RR型 注意更新root、rightChild、rightChild.left的父亲指针指向
     *
     * @param root
     * @return
     */
    private RedBlackNode rolateWithRightChild(RedBlackNode root) {
        /*
         * parent记录root的父亲节点
         */
        RedBlackNode parent = root.parent;
        /*
         * rightChild root节点的右儿子
         */
        RedBlackNode rightChild = root.right;
        /*
         * 如果rightChild的左儿子非空，更新rightChild.left的parent
         */
        if (rightChild.left != NIL) {
            rightChild.left.parent = root;
        }
        root.right = rightChild.left;
        rightChild.left = root;
        /*
         * 更新root的parent
         */
        root.parent = rightChild;
        /*
         * root节点为根节点时，根节点改变
         */
        if (parent == null) {
            this.root = rightChild;
        }
        /*
         * root节点为父亲的右儿子
         */
        else if (root == parent.right) {
            parent.right = rightChild;
        }
        /*
         * root节点为父亲的左儿子
         */
        else {
            parent.left = rightChild;
        }
        /*
         * 更新rightChild的parent
         */
        rightChild.parent = parent;
        return rightChild;

    }

    /**
     * 右旋 LL型
     *
     * @param root
     * @return
     */
    private RedBlackNode rolateWithLeftChild(RedBlackNode root) {
        /*
         * parent记录root的父亲节点
         */
        RedBlackNode parent = root.parent;
        /*
         * leftChild root节点的左儿子
         */
        RedBlackNode leftChild = root.left;
        root.left = leftChild.right;
        /*
         * 如果leftChild的右儿子非空，则更新leftChild.right的parent
         */
        if (leftChild.right != NIL) {
            leftChild.right.parent = root;
        }
        leftChild.right = root;
        /*
         * 更新root的parent
         */
        root.parent = leftChild;
        /*
         * 如果root节点之前是根节点，则根节点指向更新
         */
        if (parent == null) {
            this.root = leftChild;
        }
        /*
         * root节点为父亲的左儿子
         */
        else if (root == parent.left) {
            parent.left = leftChild;
        }
        /*
         * root节点为父亲的右儿子
         */
        else {
            parent.right = leftChild;
        }
        /*
         * 更新leftChild的parent
         */
        leftChild.parent = parent;
        return leftChild;
    }

    /**
     * 双旋转 RL型
     *
     * @param root
     * @return
     */
    private RedBlackNode doubleWithRightChild(RedBlackNode root) {
        /*
         * 先右旋再左旋
         */
        rolateWithLeftChild(root.right);
        return rolateWithRightChild(root);
    }

    /**
     * 双旋转 LR型
     *
     * @param root
     * @return
     */
    private RedBlackNode doubleWithLeftChild(RedBlackNode root) {
        /*
         * 先左旋再右旋
         */
        rolateWithRightChild(root.left);
        return rolateWithLeftChild(root);
    }

    /**
     * 插入值为value的节点
     *
     * @param value
     */
    public void insert(int value) {
        /*
         * 插入节点的左右儿子为NIL,颜色为红色
         */
        RedBlackNode node = new RedBlackNode(value, NIL, NIL, Color.Red);
        /*
         * 如果已经含有此节点
         */
        if (contains(node, root)) {
            return;
        }
        insert(node, root);
        /*
         * 红黑树节点数目增加一个
         */
        size++;
    }

    /**
     * 向红黑树中插入节点node
     *
     * @param node
     * @param root
     * @return
     */
    public void insert(RedBlackNode node, RedBlackNode root) {
        /*
         * 如果根节点为空，则将插入的节点染成黑色即可
         */
        if (this.root == NIL) {
            node.color = Color.Black;
            this.root = node;
        } else {
            /*
             * 记录插入节点的父亲
             */
            RedBlackNode parent = root;
            /*
             * 找到node节点应该插入的位置
             */
            while (node.value != root.value) {
                /*
                 * 更新parent
                 */
                parent = root;
                /*
                 * 插入到右子树
                 */
                if (node.value > root.value) {
                    root = root.right;
                    /*
                     * 到达NIl节点,作为右儿子插入
                     */
                    if (root == NIL) {
                        node.parent = parent;
                        parent.right = node;
                        break;
                    }
                }
                /* 插入到左子树 */
                else {
                    root = root.left;
                    /*
                     * 作为左儿子插入
                     */
                    if (root == NIL) {
                        node.parent = parent;
                        parent.left = node;
                        break;
                    }
                }

            }
            /*
             * 执行插入修复操作
             */
            insertFixUp(node);
        }
    }

    /**
     * node节点插入后进行修复
     *
     * @param node
     */
    private void insertFixUp(RedBlackNode node) {
        /*添加修复操作不会超过2次
         * node节点经过前一次处理后上升到根节点，颜色为红色，染成黑色，更新根节点指针
         */
        if (node.parent == null) {
            node.color = Color.Black;
            /*
             * 更新root引用
             */
            this.root = node;
            return;
        }
        /*
         * node节点的父亲颜色是黑色，无需调整
         */
        if (node.parent.color == Color.Black) {
            return;
        }
        /*
         * 得到node节点的叔父、父亲、祖父节点
         */
        RedBlackNode uncle = getUncle(node);
        RedBlackNode grandParent = getGrandParent(node);
        RedBlackNode parent = node.parent;
        /*
         * node节点的父节点、叔父节点颜色为红色，祖父节点为黑色 策略：将node节点的父节点、叔父节点颜色染成黑色，祖父节点染成红色。
         * 此时祖父节点可能与其父节点颜色冲突，递归解决
         */
        if (uncle.color == Color.Red) {
            node.parent.color = Color.Black;
            uncle.color = Color.Black;
            grandParent.color = Color.Red;
            /*
             * 递归修复grandParent
             */
            insertFixUp(grandParent);
        }
        /*
         * LL型 叔父节点是黑色 策略：将父亲节点染成黑色，祖父节点染成红色,右旋转
         */
        else if (node == parent.left && parent == grandParent.left) {
            parent.color = Color.Black;
            grandParent.color = Color.Red;
            rolateWithLeftChild(grandParent);
        }
        /*
         * RL型 叔父节点是黑色 策略：node节点染成黑色，祖父节点染成红色，先右旋转再左旋转
         */
        else if (node == parent.left && parent == grandParent.right) {
            node.color = Color.Black;
            grandParent.color = Color.Red;
            doubleWithRightChild(grandParent);
        }
        /*
         * RR型 叔父节点黑色策略：将父亲节点染成黑色、祖父节点染成红色，左旋转
         */
        else if (node == parent.right && parent == grandParent.right) {
            parent.color = Color.Black;
            grandParent.color = Color.Red;
            rolateWithRightChild(grandParent);
        }
        /*
         * LR型 叔父节点黑色 策略：node节点染成黑色，祖父节点染成红色，先左旋，再右旋
         */
        else {
            node.color = Color.Black;
            grandParent.color = Color.Red;
            doubleWithLeftChild(grandParent);
        }
    }

    /**
     * 删除值为value的节点
     *
     * @param val
     */
    public void remove(int val) {
        RedBlackNode node = new RedBlackNode(val, Color.Red);
        remove(node, root);
    }

    /**
     * 删除root中的节点node
     *
     * @param node
     * @param root
     * @return
     */
    private RedBlackNode remove(RedBlackNode node, RedBlackNode root) {
        /*
         * 红黑树为空
         */
        if (root == NIL) {
            return null;
        }
        /*
         * 节点在右子树
         */
        if (node.value > root.value) {
            root.right = remove(node, root.right);
        }
        /*
         * 节点在左子树
         */
        if (node.value < root.value) {
            root.left = remove(node, root.left);
        }
        if (node.value == root.value) {
            /*
             * 待删除节点的左右子树非空
             */
            if (root.left != NIL && root.right != NIL) {
                /*
                 * 用右子树的最小值节点替代待删除的节点
                 */
                RedBlackNode replace = findMin(root.right);
                root.value = replace.value;
                /*
                 * 问题转化为删除右子树中的replace节点
                 */
                root.right = remove(replace, root.right);
            }
            /*
             * 待删除节点只有左子树或只有右子树或无左右子树
             */
            else {
                /*
                 * 被删除节点的父节点
                 */
                RedBlackNode parent = root.parent;
                /*
                 * 被删除的节点
                 */
                RedBlackNode deleteNode = root;

                /*
                 * 被删除节点的位置尤其儿子取代
                 */
                root = (root.left != NIL) ? root.left : root.right;
                /*
                 * 如果被删除节点是根节点,将后继节点染黑后作为新的根节点
                 */
                if (parent == null) {
                    deleteNode.left.parent = parent;
                    deleteNode.right.parent = parent;
                    root.color = Color.Black;
                    this.root = root;
                } else {
                    /*
                     * node节点是作为parent的左儿子还是右儿子
                     */
                    boolean isLeftChild = false;
                    if (deleteNode == parent.left) {
                        isLeftChild = true;
                    }
                    /*
                     * 被删除节点的儿子的父亲指向祖父
                     */
                    root.parent = parent;
                    /*
                     * 将root接到其祖父
                     */
                    if (isLeftChild) {
                        parent.left = root;
                    } else {
                        parent.right = root;
                    }
                    /*
                     * 修复被删除节点
                     */
                    removeFixUp(root, deleteNode);
                    /*
                     * 清除deleteNode的所有引用
                     */
                    deleteNode.parent = null;
                    deleteNode.left = null;
                    deleteNode.right = null;
                }
            }
        }
        /*
         * 将NIL节点的父亲置为null，NIL节点的颜色在修复过程中可能会被染成红色，将其恢复成黑色
         */
        NIL.parent = null;
        NIL.color = Color.Black;

        /*
         * 红黑树节点数目减少一个
         */
        size--;
        return root;
    }

    /**
     * 删除node的父节点后进行修复红黑树性质的操作
     * 修复操作旋转次数不会超过3次
     * @param node
     */
    private void removeFixUp(RedBlackNode node, RedBlackNode deleteNode) {
        /*
         * 如果被删除节点是根节点，直接将node节点颜色染黑让其作为新的根节点
         */
        if (deleteNode.parent == null) {
            node.color = Color.Black;
            this.root = node;
            return;
        }
        /*
         * 如果被删除节点的颜色是红色的，对红黑树的五条性质不造成影响，无需处理
         */
        if (deleteNode.color == Color.Red) {
            return;
        }
        /*
         * 如果被删除节点的后继节点颜色为红色，将其染成黑色既可
         */
        if (node.color == Color.Red) {
            node.color = Color.Black;
            return;
        }
        /*
         * 如果被删除节点的后继节点颜色为黑色，那么从被删除节点父亲节点到被删除节点NIL节点的路径上将少一个黑色节点
         */
        if (node.color == Color.Black) {
            /*
             * 得到node的叔叔节点，叔叔节点的左儿子、右儿子,祖父
             */
            RedBlackNode uncle = getBrother(node);
            RedBlackNode uncleLeftChild = uncle.left;
            RedBlackNode uncleRightChild = uncle.right;
            RedBlackNode grandParent = node.parent;
            while (true) {

                /*
                 * node节点现在是其祖父的左儿子
                 */
                if (node == grandParent.left) {
                    /*
                     * 状态-1 如果叔叔节点颜色是红色
                     * 策略：将祖父节点染成红色，叔叔节点染成染成黑色后左旋，使其装态转化为2或3或4以便进一步调整
                     */
                    if (uncle.color == Color.Red) {
                        uncle.color = Color.Black;
                        grandParent.color = Color.Red;
                        rolateWithRightChild(grandParent);
                        /*
                         * 更新node指向
                         */
                        node = grandParent.right;
                        grandParent = node.parent;
                        uncle = getBrother(node);
                        uncleLeftChild = uncle.left;
                        uncleRightChild = uncle.right;
                    }
                    /*
                     * 状态-2
                     * 叔叔节点为黑色，其左右儿子也为黑色(此时叔叔节点可能为NIL节点，如果其为NIL节点，其左右儿子将为NULL
                     * ，要注意空指针判断) 策略：将叔叔节点染成红色，现在祖父是多了一重黑色，向状态1，3，4转化
                     */
                    else if (isBlack(uncleLeftChild)
                            && isBlack(uncleRightChild)) {
                        /*
                         * 更新叔叔节点颜色
                         */
                        uncle.color = Color.Red;
                        /*
                         * 更新node指向
                         */
                        node = grandParent;
                        grandParent = node.parent;
                        /*
                         * node现在指向了根节点
                         */
                        if (grandParent == null) {
                            node.color = Color.Black;
                            this.root = node;
                            break;
                        }
                        uncle = getBrother(node);
                        uncleLeftChild = uncle.left;
                        uncleRightChild = uncle.right;
                        /*
                         * 当前节点是红黑型的节点，将当前节点颜色染成黑色，调整完成
                         */
                        if (grandParent.color == Color.Red) {
                            grandParent.color = Color.Black;
                            break;
                        }
                    }
                    /*
                     * 状态-3 叔叔节点颜色为黑色，其左儿子颜色为红色，右儿子颜色为黑色
                     * 策略：将叔叔节点的左儿子染成黑色，叔叔节点染成红色，右旋向状态4转化
                     */
                    else if (uncleLeftChild.color == Color.Red
                            && uncleRightChild.color == Color.Black) {
                        uncle.color = Color.Red;
                        uncleLeftChild.color = Color.Black;
                        rolateWithLeftChild(uncle);
                    }
                    /*
                     * 状态-4 叔叔节点颜色为黑色，右儿子颜色为红色，左儿子颜色任意
                     * 策略：将uncle节点颜色与祖父节点颜色互换，叔叔节点右儿子染成黑色，左旋转调整完成。
                     * 此次操作是祖父的左子树路径增加了一个黑色节点，但叔叔节点右子树少了一个黑色节点，把叔叔节点的右儿子染成黑色弥补
                     */
                    else {
                        Color temp = uncle.color;
                        uncle.color = grandParent.color;
                        grandParent.color = temp;
                        uncleRightChild.color = Color.Black;
                        rolateWithRightChild(grandParent);
                        /*
                         * 调整完成
                         */
                        break;

                    }
                }
                /*
                 * node节点现在使其祖父的右儿子 镜像对称的四种情形
                 */
                if (node == grandParent.right) {
                    /*
                     * 状态-1 如果叔叔节点颜色是红色
                     * 策略：将祖父节点染成红色，叔叔节点染成黑色后右旋，使其状态转化为2或3或4以便进一步调整
                     */
                    if (uncle.color == Color.Red) {
                        uncle.color = Color.Black;
                        grandParent.color = Color.Red;
                        rolateWithLeftChild(grandParent);
                        /*
                         * 更新node指向
                         */
                        node = grandParent.left;
                        grandParent = node.parent;
                        uncle = getBrother(node);
                        uncleLeftChild = uncle.left;
                        uncleRightChild = uncle.right;
                    }
                    /*
                     * 状态-2 叔叔节点为黑色，叔叔节点的左右儿子颜色为黑色
                     * 策略：将叔叔节点染成红色，现在祖父节点多了一重黑色。如果祖父节点自身颜色为红色
                     * ，将祖父节点染成黑色，调整结束。否则修改node指向使其指向祖父
                     */
                    else if (isBlack(uncleRightChild)
                            && isBlack(uncleLeftChild)) {
                        uncle.color = Color.Red;
                        /*
                         * 修改node指向
                         */
                        node = grandParent;
                        grandParent = node.parent;
                        /*
                         * node节点提升到了根节点位置
                         */
                        if (grandParent == null) {
                            node.color = Color.Black;
                            this.root = node;
                            break;
                        }
                        uncle = getBrother(node);
                        uncleLeftChild = uncle.left;
                        uncleRightChild = uncle.right;
                        /* 如果祖父节点自身颜色为红色，将祖父节点染成黑色 ，调整完成 */
                        if (node.color == Color.Red) {
                            node.color = Color.Black;
                            break;
                        }
                    }
                    /*
                     * 状态-3 叔叔节点为黑色，其左儿子为黑色，右儿子为红色
                     * 策略：将叔叔节点染成红色，右儿子染成黑色，右旋向状态-4转换
                     */
                    else if (uncleLeftChild.color == Color.Black
                            && uncleRightChild.color == Color.Red) {
                        uncle.color = Color.Red;
                        uncleRightChild.color = Color.Black;
                        rolateWithRightChild(uncle);
                    }
                    /*
                     * 状态-4 叔叔节点为黑色，左儿子为红色，右儿子颜色任意
                     * 策略：将叔叔节点的颜色与祖父颜色互换，叔叔节点的左儿子染成黑色，右旋。调整完成。
                     */
                    else {
                        Color temp = uncle.color;
                        uncle.color = grandParent.color;
                        grandParent.color = temp;
                        uncleLeftChild.color = Color.Black;
                        rolateWithLeftChild(grandParent);
                        /*
                         * 调整完成
                         */
                        break;
                    }
                }
            }
        }

        /* 最后再次把根节点染成黑色（应为node可能上升到根节点 处 */
        this.root.color = Color.Black;
    }

    /**
     * node颜色为黑色或是null节点是认为其实黑色
     *
     * @param node
     * @return
     */
    private boolean isBlack(RedBlackNode node) {
        if (node == null || node.color == Color.Black) {
            return true;
        }
        return false;
    }

    /**
     * 得到node节点兄弟节点
     *
     * @param node
     * @return
     */
    private RedBlackNode getBrother(RedBlackNode node) {
        /*
         * 当前节点是根节点
         */
        if (node.parent == null) {
            return null;
        }
        /*
         * 兄弟节点是右儿子
         */
        if (node == node.parent.left) {
            return node.parent.right;
        }
        /*
         * 兄弟节点是左儿子
         */
        return node.parent.left;
    }

    /*
     * 获取当前红黑树的节点数目
     */
    public int getSize() {
        return size;
    }

    /**
     * 非递归先序遍历红黑树
     *
     * @param root
     */
    public void preOrder(RedBlackNode root) {
        /*
         * 红黑树节点数目为零
         */
//        if (root == NIL) {
//            return;
//        }
//        System.out.println("先序遍历：");
//        Stack<RedBlackNode> stack = new Stack<RedBlackNode>();
//        stack.push(root);
//        while (!stack.isEmpty()) {
//            root = stack.pop();
//            System.out.print(root.value + " ");
//            if (root.right != NIL) {
//                stack.push(root.right);
//            }
//            if (root.left != NIL) {
//                stack.push(root.left);
//            }
//        }
//        System.out.println();
    }

    /**
     * 非递归中序遍历红黑树
     *
     * @param root
     */
    public void inOrder(RedBlackNode root) {
//        if (root == NIL) {
//            return;
//        }
//        System.out.println("中序遍历：");
//        Stack<RedBlackNode> stack = new Stack<RedBlackNode>();
//        while (root != NIL || !stack.isEmpty()) {
//            while (root != NIL) {
//                stack.push(root);
//                root = root.left;
//            }
//            root = stack.pop();
//            System.out.print(root.value + " ");
//            root = root.right;
//        }
//        System.out.println();
    }

    /**
     * 非递归后序遍历红黑树
     *
     * @param root
     */
    public void postOrder(RedBlackNode root) {
//        if (root == NIL) {
//            return;
//        }
//        System.out.println("后序遍历：");
//        RedBlackNode pre = NIL;
//        Stack<RedBlackNode> stack = new Stack<RedBlackNode>();
//        while (root != NIL || !stack.isEmpty()) {
//            while (root != NIL) {
//                stack.push(root);
//                root = root.left;
//            }
//            root = stack.peek();
//            while (root.right == NIL || root.right == pre) {
//                System.out.print(root.value + " ");
//                stack.pop();
//                pre = root;
//                if (stack.isEmpty()) {
//                    System.out.println();
//                    return;
//                }
//                root = stack.peek();
//            }
//            root = root.right;
//        }
    }

    /**
     * 层序遍历红黑树
     *
     * @param root
     */
    public void levelOrder(RedBlackNode root) {
//        if (root == NIL) {
//            return;
//        }
//        System.out.println("层序遍历:");
//        Queue<RedBlackNode> queue = new ArrayDeque<RedBlackNode>();
//        queue.offer(root);
//        while (!queue.isEmpty()) {
//            root = queue.poll();
//            System.out.print(root.value + "--" + root.color + "  ");
//            if (root.left != NIL) {
//                queue.offer(root.left);
//            }
//            if (root.right != NIL) {
//                queue.offer(root.right);
//            }
//        }
//        System.out.println();
    }

    /**
     * 红黑树节点类
     *
     * @author 过路的守望
     *
     */
    class RedBlackNode {
        /*
         * @value 节点值
         *
         * @color 节点颜色
         *
         * @left @right 节点左右儿子
         *
         * @parent 父节点
         */
        int value;
        Color color;
        RedBlackNode left;
        RedBlackNode right;
        RedBlackNode parent;

        public RedBlackNode(int value, RedBlackNode left, RedBlackNode right,
                            Color color) {
            this.value = value;
            this.left = left;
            this.right = right;
            this.color = color;
        }

        public RedBlackNode(int value, Color color) {
            this.value = value;
            this.color = color;
        }

        /*
         * 用来构造NIL节点
         */
        public RedBlackNode(Color color) {
            this.color = color;
        }
    }

    /*
     * 枚举类-节点颜色
     */
    enum Color {
        Black, Red;
    }

}


