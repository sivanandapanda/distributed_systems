package com.example;

import com.example.proto.HelloRequest;
import com.example.proto.HelloResponse;
import com.example.proto.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;

public class HelloGrpcServer extends HelloServiceGrpc.HelloServiceImplBase {

    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {

        var greeting = "Hello, " + request.getFirstName() + " " + request.getLastName();

        var helloResponse = HelloResponse.newBuilder().setGreeting(greeting).build();

        responseObserver.onNext(helloResponse);
        responseObserver.onCompleted();
    }
}
