package com.example.cluster;

import com.example.etcd.proto.KVGrpc;
import com.example.etcd.proto.PutRequest;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;

public class KeyValueService {

    private final ManagedChannel channel;

    public KeyValueService(ManagedChannel channel) {
        this.channel = channel;
    }

    public void put(String key, String value) {
        var kvBlockingStub = KVGrpc.newBlockingStub(channel);

        var putRequest = PutRequest.newBuilder()
                .setKey(ByteString.copyFromUtf8(key))
                .setValue(ByteString.copyFromUtf8(value))
                .build();

        var putResponse = kvBlockingStub.put(putRequest);
        /*System.out.println("Put operation was successful");
        System.out.println("ClusterId: " + putResponse.getHeader().getClusterId());
        System.out.println("MemberId: " + putResponse.getHeader().getMemberId());
        System.out.println("Revision: " + putResponse.getHeader().getRevision());
        System.out.println("RaftTerm: " + putResponse.getHeader().getRaftTerm());*/
    }

}
