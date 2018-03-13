package com.demo.framework.curator.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/17
 * <p>
 * ZK客户端
 */
public class CuratorClient {

    public static final String ZK_ADDRESS = "localhost:2181";

    public static final String ZK_BASE_PATH = "/config/test";

    private static CuratorClient instance;

    private CuratorFramework client;

    private CuratorClient() {
        client = CuratorFrameworkFactory.newClient(ZK_ADDRESS, new ExponentialBackoffRetry(1000, 3));
        client.start();
        System.out.println("zk client start successfully!");
    }

    public static CuratorClient getInstance() {
        if (instance == null) {
            synchronized (CuratorClient.class) {
                if (instance == null) {
                    instance = new CuratorClient();
                }
            }
        }
        return instance;
    }

    public CuratorFramework getClient() {
        return client;
    }

    public List<String> children(String node) {
        try {
            return client.getChildren().forPath(ZK_BASE_PATH + node);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void create(String node, String value) {
        try {
            client.create().creatingParentsIfNeeded().forPath(ZK_BASE_PATH + node, value.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String get(String node) {
        try {
            return new String(client.getData().forPath(ZK_BASE_PATH + node));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void set(String node, String value) {
        try {
            client.setData().forPath(ZK_BASE_PATH + node, value.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(String node) {
        try {
            client.delete().forPath(ZK_BASE_PATH + node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
