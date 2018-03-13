package com.demo.framework.curator.watcher;

import com.demo.framework.curator.client.CuratorClient;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.util.function.Consumer;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/1/17
 */
public class NodeWatcher {

    /**
     * 设置节点监听
     *
     * @param node
     * @param consumer
     * @throws Exception
     */
    public static void setNodeWatcher(String node, Consumer<String> consumer) throws Exception {
        NodeCache watcher = new NodeCache(CuratorClient.getInstance().getClient(), node, false);
        watcher.getListenable().addListener(() -> {
            new Thread(() -> {
                // zookeeper监听器每次监听完需要重新设置, 为避免监听事件处理过久导致漏掉其他的监听事件, 将事件处理放到异步线程中执行
                consumer.accept(new String(watcher.getCurrentData().getData()));
            }).start();
        });
        // 如果为true则首次不会触发监听事件
        watcher.start(true);
    }

    /**
     * 设置路径监听
     *
     * @param path
     * @param consumer
     * @throws Exception
     */
    public static void setPathWatcher(String path, Consumer<NodeData> consumer) throws Exception {
        PathChildrenCache watcher = new PathChildrenCache(CuratorClient.getInstance().getClient(), path, true);
        watcher.getListenable().addListener((client, event) -> {
            ChildData data = event.getData();
            if (data != null) {
                // 异步处理, 理由同上
                new Thread(() -> {
                    consumer.accept(new NodeData(event.getType(), data.getPath(), new String(data.getData())));
                }).start();
            }
        });
        watcher.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
    }

    /**
     * 节点数据访问类
     */
    public static class NodeData {
        public final PathChildrenCacheEvent.Type type;
        public final String path;
        public final String data;

        public NodeData(PathChildrenCacheEvent.Type type, String path, String data) {
            this.type = type;
            this.path = path;
            this.data = data;
        }
    }

}
