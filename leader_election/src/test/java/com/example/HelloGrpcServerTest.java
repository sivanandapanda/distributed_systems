package com.example;

import com.example.proto.HelloRequest;
import com.example.proto.HelloServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;

class HelloGrpcServerTest {

    @Test
    void test() {
        var channel = ManagedChannelBuilder.forAddress("localhost", 9001)
                .usePlaintext()
                .build();

        HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);

        var helloRequest = HelloRequest.newBuilder()
                .setFirstName("John")
                .setLastName("Smith")
                .setAge(12)
                .addHobbies("games")
                .addHobbies("learning")
                .putBagOfTricks("architect", "imposter")
                .build();

        var helloResponse = stub.hello(helloRequest);

        System.out.println(helloResponse.getGreeting());

        channel.shutdown();
    }

}