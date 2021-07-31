package com.example;

import com.example.proto.HelloRequest;
import com.example.proto.HelloResponse;
import com.example.proto.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;

public class HelloGrpcServer extends HelloServiceGrpc.HelloServiceImplBase {

    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {

        System.out.println(request);

        var greeting = "Hello, " + request.getFirstName() + " " + request.getLastName();

        responseObserver.onNext(HelloResponse.newBuilder().setGreeting(greeting).build());
        responseObserver.onCompleted();
    }
}
