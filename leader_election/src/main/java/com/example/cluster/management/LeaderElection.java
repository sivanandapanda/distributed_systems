package com.example.cluster.management;

import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.Session;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LeaderElection {

    private final String tag;
    private final Consul client;
    private final String serviceId;
    private final String serviceName;
    private final int serverPort;

    private final ScheduledExecutorService executorService;

    public LeaderElection(Consul client, String serviceName, String tag, String serviceId, int serverPort) {
        this.client = client;
        this.serviceName = serviceName;
        this.tag = tag;
        this.serviceId = serviceId;
        this.serverPort = serverPort;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void registerService() throws UnknownHostException {
        var healthCheckEndPoint = String.format("http://%s:%d/health", InetAddress.getLocalHost().getCanonicalHostName(), serverPort);

        Registration service = ImmutableRegistration.builder()
                .id(serviceId)
                .name(serviceName)
                .port(8080)
                .check(Registration.RegCheck.http(healthCheckEndPoint, 5))
                .tags(Collections.singletonList(tag))
                .meta(Collections.singletonMap("version", "1.0"))
                .build();

        client.agentClient().register(service);
    }

    public String createSession() {
        final Session session = ImmutableSession.builder()
                .name(String.format("service/%s/leader", serviceName))
                .behavior("delete")
                .ttl("10s")
                .build();
        return client.sessionClient().createSession(session).getId();
    }

    public void renewSessionPeriodic(String sessionId) {
        this.executorService.scheduleAtFixedRate(() -> {
            var sessionInfo = client.sessionClient().renewSession(sessionId);

            if(sessionInfo.isPresent()) {
                System.out.println(LocalDateTime.now() + " Renew session " + sessionInfo.get());
            } else {
                System.out.println(LocalDateTime.now() + " Renew session failed for " + sessionId);
            }

        }, 5, 5, TimeUnit.SECONDS);
    }

    public boolean isLeader(String sessionId) {
        return client.keyValueClient().acquireLock(String.format("service/%s/leader", serviceName), serviceName, sessionId);
    }

    public void destroy() {
        this.executorService.shutdownNow();
    }
}
