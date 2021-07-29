package com.example;

import com.example.cluster.KeyValueService;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class Application {

    private static final long OPERATION_TIMEOUT = 5;
    private static final String ETCD_SERVER_URL = "http://127.0.0.1:2379";

    public static void main(String[] args) throws InterruptedException, IOException {
        //startGrpcServer();

        var channel = ManagedChannelBuilder.forAddress("localhost", 2379)
                .usePlaintext()
                .build();

        new KeyValueService(channel).put("Zm9v", "server");
    }

    private static void startGrpcServer() throws IOException, InterruptedException {
        var server = ServerBuilder.forPort(9001).addService(new HelloGrpcServer()).build();

        server.start();
        server.awaitTermination();
    }
}
