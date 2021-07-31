package com.example;

import com.example.proto.ChatMessage;
import com.example.proto.ChatMessageFromServer;
import com.example.proto.ChatServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ChatGrpcServiceTest {

    @Test
    void test() throws InterruptedException {
        var channel = ManagedChannelBuilder.forAddress("localhost", 9001)
                .usePlaintext()
                .build();

        var stub = ChatServiceGrpc.newStub(channel);

        var chat = stub.chat(new StreamObserver<ChatMessageFromServer>() {
            @Override
            public void onNext(ChatMessageFromServer chatMessageFromServer) {
                System.out.println("ABC =>" + chatMessageFromServer.getMessage().getFrom() + " " + chatMessageFromServer.getMessage().getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {

            }
        });

        chat.onNext(ChatMessage.newBuilder()
                .setFrom("Abc")
                .setMessage("Hello from Abc!")
                .build());

        TimeUnit.SECONDS.sleep(2);
    }
}