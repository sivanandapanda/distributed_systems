package com.example.cluster;

import com.example.etcd.proto.WatchCreateRequest;
import com.example.etcd.proto.WatchGrpc;
import com.example.etcd.proto.WatchRequest;
import com.example.etcd.proto.WatchResponse;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class WatchService {
    private final ManagedChannel channel;

    public WatchService(ManagedChannel channel) {
        this.channel = channel;
    }

    public void watch(String key) {
        var watchStub = WatchGrpc.newStub(channel);

        var watchRequest = WatchRequest.newBuilder().setCreateRequest(WatchCreateRequest.newBuilder().setKey(ByteString.copyFromUtf8(key)).build()).build();

        var watch = watchStub.watch(new StreamObserver<WatchResponse>() {
            @Override
            public void onNext(WatchResponse watchResponse) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        });

    }

}
