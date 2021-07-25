package com.example;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class WatcherDemo implements Watcher {
    private static final String ZOOKEEPER_ADDRESS = "192.168.1.195:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String TARGET_ZNODE = "/target_znode";
    private ZooKeeper zooKeeper;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        var watchersDemo = new WatcherDemo();

        watchersDemo.connectToZookeeper();
        watchersDemo.watchTargetZNode();
        watchersDemo.run();
        watchersDemo.close();
        System.out.println("Disconnected from ZooKeeper, exiting application");
    }

    private void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    private void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        zooKeeper.close();
    }

    private void watchTargetZNode() throws InterruptedException, KeeperException {
        var stat = zooKeeper.exists(TARGET_ZNODE, this);
        if(stat == null) {
            return;
        }

        var data = zooKeeper.getData(TARGET_ZNODE, this, stat);
        var children = zooKeeper.getChildren(TARGET_ZNODE, this);

        System.out.println("Data: " + new String(data) + " children: " + children);
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                if(event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to Zookeeper");
                } else {
                    System.out.println("Disconnected Zookeeper event");
                    synchronized (zooKeeper) {
                        zooKeeper.notifyAll();
                    }
                }
                break;
            case NodeCreated:
                System.out.println(TARGET_ZNODE + " was created");
                break;
            case NodeDeleted:
                System.out.println(TARGET_ZNODE + " was deleted");
                break;
            case NodeChildrenChanged:
                System.out.println(TARGET_ZNODE + " children changed");
                break;
        }

        try {
            watchTargetZNode();
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }
}
