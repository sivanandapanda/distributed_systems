package com.example;

import com.example.proto.ChatMessage;
import com.example.proto.ChatMessageFromServer;
import com.example.proto.ChatServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.util.LinkedHashSet;

public class ChatGrpcService extends ChatServiceGrpc.ChatServiceImplBase {

    private static final LinkedHashSet<StreamObserver<ChatMessageFromServer>> observers = new LinkedHashSet<>();

    @Override
    public StreamObserver<ChatMessage> chat(StreamObserver<ChatMessageFromServer> responseObserver) {
        observers.add(responseObserver);

        return new StreamObserver<>() {
            @Override
            public void onNext(ChatMessage chatMessage) {
                var chatMessageFromServer = ChatMessageFromServer.newBuilder()
                        .setMessage(chatMessage)
                        //.setTimestamp(TimestampOrBuilder)
                        .build();
                observers.forEach(o -> o.onNext(chatMessageFromServer));
            }

            @Override
            public void onError(Throwable throwable) {
                observers.remove(responseObserver);
            }

            @Override
            public void onCompleted() {
                observers.remove(responseObserver);
            }
        };
    }
}
