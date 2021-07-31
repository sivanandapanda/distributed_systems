package com.example;

import com.example.cluster.KeyValueService;
import com.example.cluster.WatchService;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Application {

    public static void main(String[] args) throws InterruptedException {
        //startGrpcServer();

        var channel = ManagedChannelBuilder.forAddress("localhost", 2379)
                .usePlaintext()
                .build();

        var key = "Zm9v";

        new WatchService(channel).watch(key);
        new KeyValueService(channel).put(key, UUID.randomUUID().toString());

        TimeUnit.SECONDS.sleep(200);
    }

    private static void startGrpcServer() throws IOException, InterruptedException {
        var server = ServerBuilder.forPort(9001)
                .addService(new HelloGrpcServer())
                .addService(new ChatGrpcService())
                .build();

        server.start();
        server.awaitTermination();
    }
}
