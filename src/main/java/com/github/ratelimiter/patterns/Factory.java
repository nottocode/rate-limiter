package com.github.ratelimiter.patterns;

import com.aerospike.client.AerospikeClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Factory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static AerospikeClient cloneAerospikeClient(final AerospikeClient aerospikeClient) throws IOException {
        return objectMapper.readValue(objectMapper.writeValueAsString(aerospikeClient), AerospikeClient.class);
    }
}
