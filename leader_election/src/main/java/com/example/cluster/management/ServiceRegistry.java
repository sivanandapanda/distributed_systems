package com.example.cluster.management;

import org.apache.zookeeper.*;

import java.util.HashSet;
import java.util.List;

public class ServiceRegistry implements Watcher {
    private final ZooKeeper zooKeeper;
    private final static String REGISTRY_ZNODE = "/service_registry";

    private String currentZNode;
    private List<String> allServiceAddresses = null;

    public ServiceRegistry(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
        createServiceRegistryZNode();
        registerForUpdates();
    }

    public void registerToCluster(String metadata) throws InterruptedException, KeeperException {
        this.currentZNode = this.zooKeeper.create(REGISTRY_ZNODE + "/n_", metadata.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Registered to service registry " + metadata);
    }

    public void registerForUpdates() {
        try {
            this.updateAddresses();
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<String> getAllServiceAddresses() throws InterruptedException, KeeperException {
        if(allServiceAddresses == null) {
            updateAddresses();
        }

        return this.allServiceAddresses;
    }

    public void unregisterFromCluster() throws InterruptedException, KeeperException {
        if(currentZNode != null && zooKeeper.exists(currentZNode, false) != null) {
            zooKeeper.delete(currentZNode, -1);
        }
    }

    private void createServiceRegistryZNode() {
        try {
            if(zooKeeper.exists(REGISTRY_ZNODE, false) == null) {
                zooKeeper.create(REGISTRY_ZNODE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            System.err.println("Someone has already created zNode and this exception was thrown to avoid race condition.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateAddresses() throws InterruptedException, KeeperException {
        var workerNodes = zooKeeper.getChildren(REGISTRY_ZNODE, this);

        var addresses = new HashSet<String>(workerNodes.size());

        for (String workerNode : workerNodes) {
            var workerNodeFullPath = REGISTRY_ZNODE + "/" + workerNode;
            var stat= zooKeeper.exists(workerNodeFullPath, false);
            if(stat == null) {
                continue;
            }

            var addressInBytes = zooKeeper.getData(workerNodeFullPath, false, stat);
            addresses.add(new String(addressInBytes));
        }

        this.allServiceAddresses = List.copyOf(addresses);
        System.out.println("The cluster addresses are " + this.allServiceAddresses);
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            this.updateAddresses();
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

}
