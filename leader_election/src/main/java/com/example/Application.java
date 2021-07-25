package com.example;

import io.etcd.jetcd.*;
import io.etcd.jetcd.election.CampaignResponse;
import io.etcd.jetcd.kv.GetResponse;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Application {

    //private static final String ETCD_SERVER_URL = "http://192.168.1.16:2379";
    private static final long OPERATION_TIMEOUT = 5;
    private static final String ETCD_SERVER_URL = "http://127.0.0.1:2379";

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        // create client
        Client client = Client.builder().endpoints(ETCD_SERVER_URL).build();
        KV kvClient = client.getKVClient();

        ByteSequence key = ByteSequence.from("test_key".getBytes());
        ByteSequence value = ByteSequence.from("test_value".getBytes());

        // put the key-value
        kvClient.put(key, value).get();

        // get the CompletableFuture
        CompletableFuture<GetResponse> getFuture = kvClient.get(key);

        // get the value from CompletableFuture
        GetResponse response = getFuture.get();
        System.out.println(response);

        var electionClient = client.getElectionClient();
        var leaseClient = client.getLeaseClient();

        ByteSequence electionName = ByteSequence.from("/leader-election/", StandardCharsets.UTF_8);

        // register lease
        long leaseId = leaseClient.grant(10).get().getID();
        System.out.println("LeaseID " + leaseId);
        // start new campaign
        ByteSequence firstProposal = ByteSequence.from("proposal1", StandardCharsets.UTF_8);

        CampaignResponse campaignResponse = electionClient.campaign(electionName, leaseId, firstProposal).get(OPERATION_TIMEOUT, TimeUnit.SECONDS);

        System.out.println(campaignResponse.getLeader().getKey());
        System.out.println(campaignResponse.getLeader().getLease());
        System.out.println(campaignResponse.getLeader().getName());

        if(electionName.getBytes() == campaignResponse.getLeader().getName().toByteArray()) {
            System.out.println("I am leader");
        } else {
            System.out.println("I am not leader");
        }
    }
}
