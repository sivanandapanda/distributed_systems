package com.example.v2;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

public class LeaderElection2 {

    private final String consulServer;
    private final String serviceName;
    private final String tag;
    private final String nodeName;

    public LeaderElection2(String consulServer, String serviceName, String tag, String nodeName) {
        this.consulServer = consulServer;
        this.serviceName = serviceName;
        this.tag = tag;
        this.nodeName = nodeName;
    }

    public String createSession() {
        try {
            Jsonb jsonb = JsonbBuilder.create();
            var metaData = new ServiceMetaData(serviceName, nodeName);
            //var serialized = jsonb.toJson(metaData);

            String serialized  = "{" +
                    "  \"LockDelay\": \"5s\"," +
                    "  \"Name\": \"my-service-lock\"," +
                    "  \"Node\": \"foobar\"," +
                    //"  \"Checks\": [\"a\", \"b\", \"c\"]," +
                    "  \"Behavior\": \"release\"," +
                    "  \"TTL\": \"10s\"" +
                    "}";

            var httpRequest = HttpRequest.newBuilder().uri(new URI(consulServer + "/v1/session/create"))
                    //.version(HttpClient.Version.HTTP_2)
                    //.headers("key1", "value1", "key2", "value2")
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(serialized.getBytes()))
                    .build();

            return HttpClient
                    .newBuilder()
                    .proxy(ProxySelector.getDefault())
                    .build()
                    .sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray())
                    .thenApply(HttpResponse::body)
                    .thenApply(String::new)
                    .thenApply(responseBody -> jsonb.fromJson(responseBody, Session.class).getID())
                    .get();
        } catch (URISyntaxException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean acquireSession(String sessionId) {
        try {
            Jsonb jsonb = JsonbBuilder.create();
            var metaData = new NodeMetadata(nodeName, tag);
            var serialized = jsonb.toJson(metaData);

            var httpRequest = HttpRequest.newBuilder().uri(new URI(consulServer + "/v1/kv/lead?acquire="+sessionId))
                    .version(HttpClient.Version.HTTP_2)
                    //.headers("key1", "value1", "key2", "value2")
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(serialized.getBytes()))
                    .build();

            return HttpClient
                    .newBuilder()
                    .proxy(ProxySelector.getDefault())
                    .build()
                    .sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray())
                    .thenApply(HttpResponse::body)
                    .thenApply(String::new)
                    .thenApply(Boolean::parseBoolean)
                    .get();
        } catch (URISyntaxException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public static class NodeMetadata {
        String nodeName;
        String tag;

        private NodeMetadata(String nodeName, String tag) {
            this.nodeName = nodeName;
            this.tag = tag;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    public static class ServiceMetaData {
        String Name;
        String Node;
        //String tag;
        String TTL = "15s";
        String LockDelay = "5s";
        String Behavior = "release";

        private ServiceMetaData(String name, String Node) {
            this.Name = name;
            this.Node = Node;
        }

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }

        public String getNode() {
            return Node;
        }

        public void setNode(String node) {
            Node = node;
        }

        public String getTTL() {
            return TTL;
        }

        public void setTTL(String TTL) {
            this.TTL = TTL;
        }

        public String getLockDelay() {
            return LockDelay;
        }

        public void setLockDelay(String lockDelay) {
            LockDelay = lockDelay;
        }

        public String getBehavior() {
            return Behavior;
        }

        public void setBehavior(String behavior) {
            Behavior = behavior;
        }
    }

    public static class Session {
        String ID;

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }
    }

}
