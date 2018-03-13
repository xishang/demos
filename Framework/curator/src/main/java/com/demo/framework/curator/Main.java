package com.demo.framework.curator;

import com.demo.framework.curator.client.CuratorClient;
import com.demo.framework.curator.watcher.NodeWatcher;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/17
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // 基本节点操作
        CuratorClient client = CuratorClient.getInstance();
        client.children("");
        client.create("/some", "abc");
        client.get("/some");
        client.set("/some", "def");
        client.delete("/some");
        // 节点监控
        NodeWatcher.setNodeWatcher(CuratorClient.ZK_BASE_PATH + "/data", System.out::println);
        NodeWatcher.setPathWatcher(CuratorClient.ZK_BASE_PATH, node -> {
            System.out.println("Receive event: "
                    + "type=[" + node.type + "]"
                    + ", path=[" + node.path + "]"
                    + ", data=[" + node.data + "]");
        });
        for (int i = 0; i < 10; i++) {
            Thread.sleep(10);
            CuratorClient.getInstance().set("/data", String.valueOf(i));
        }
    }

}
