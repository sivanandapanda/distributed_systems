package com.example;

import com.example.cluster.management.LeaderElection;
import com.orbitz.consul.Consul;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Application {

    public static void main(String[] args) throws UnknownHostException {
        int serverPort = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        var webServer = new WebServer(serverPort);
        webServer.startServer();

        Consul client = Consul.builder().withUrl("http://192.168.1.195:8500").build();

        var serviceName = "leaderService";
        var tag = "leaderTag";
        var serviceId = UUID.randomUUID().toString();
        var leaderElection = new LeaderElection(client, serviceName, tag, serviceId, serverPort);
        leaderElection.registerService();

        var sessionId = leaderElection.createSession();
        leaderElection.renewSessionPeriodic(sessionId);

        new Thread(() -> {
            while (true) {
                if (leaderElection.isLeader(sessionId)) {
                    System.out.println(LocalDateTime.now() + " I am leader");
                } else {
                    System.out.println(LocalDateTime.now() +  " I am not the leader");
                }

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            webServer.stopServer();
            leaderElection.destroy();
            client.destroy();
        }));
    }

}
