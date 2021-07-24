package com.example.cluster;

public class LeaderNotFoundException extends Exception {

    public LeaderNotFoundException(String message) {
        super(message);
    }
}
