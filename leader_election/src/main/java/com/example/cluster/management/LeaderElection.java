package com.example.cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;

public class LeaderElection implements Watcher {

    private static final String ELECTION_NAMESPACE = "/election";
    private final ZooKeeper zooKeeper;
    private final OnElectionCallback onElectionCallback;
    private String currentZnodeName;

    public LeaderElection(ZooKeeper zooKeeper, OnElectionCallback onElectionCallback) {
        this.zooKeeper = zooKeeper;
        this.onElectionCallback = onElectionCallback;
        createLeaderElectionZNode();
    }

    private void createLeaderElectionZNode() {
        try {
            if(zooKeeper.exists(ELECTION_NAMESPACE, false) == null) {
                zooKeeper.create(ELECTION_NAMESPACE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            System.err.println("Someone has already created zNode and this exception was thrown to avoid race condition.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void volunteerForLeadership() throws InterruptedException, KeeperException {
        var znodePrefix = ELECTION_NAMESPACE + "/c_";
        var znodeFullPath = zooKeeper.create(znodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        System.out.println(znodeFullPath);
        currentZnodeName = znodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
    }

    public void reElectLeader() throws InterruptedException, KeeperException {
        Stat predecessorStat = null;
        String predecessorZnodeName = "";

        while(predecessorStat == null) {
            var children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);

            Collections.sort(children);

            var smallestChild = children.get(0);

            if (smallestChild.equalsIgnoreCase(currentZnodeName)) {
                System.out.println("I am the leader");
                onElectionCallback.onElectedToBeLeader();
                return;
            } else {
                System.out.println("I am not the leader, " + smallestChild + " is the leader");
                int predecessorIndex = Collections.binarySearch(children, currentZnodeName) - 1;
                predecessorZnodeName = children.get(predecessorIndex);
                predecessorStat = zooKeeper.exists(ELECTION_NAMESPACE + "/" + predecessorZnodeName, this);
            }
        }

        onElectionCallback.onWorker();
        System.out.println("watching znode " + predecessorZnodeName);
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case NodeDeleted:
                try {
                    reElectLeader();
                } catch (InterruptedException | KeeperException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
