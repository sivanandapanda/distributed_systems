package com.example.v2;

import java.util.UUID;

public class Application2 {

    public static void main(String[] args) {
        var serviceName = "leaderService";
        var tag = "leaderTag";
        var instanceName = UUID.randomUUID().toString();
        System.out.println("Starting leader election for node " + instanceName);

        var leaderElection = new LeaderElection2("http://192.168.1.195:8500", serviceName, tag, instanceName);

        var sessionId = leaderElection.createSession();

        System.out.println("Try to acquire " + sessionId);

        var sessionAcquired = leaderElection.acquireSession(sessionId);

        if(sessionAcquired) {
            System.out.println("I am leader");
        } else {
            System.out.println("I am not leader");
        }
    }

}
