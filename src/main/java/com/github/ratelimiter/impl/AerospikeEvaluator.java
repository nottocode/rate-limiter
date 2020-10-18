package com.github.ratelimiter.impl;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.ResultCode;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;
import com.github.ratelimiter.Constants;
import com.github.ratelimiter.Evaluator;
import com.github.ratelimiter.config.impl.AerospikeLimitConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@AllArgsConstructor
public class AerospikeEvaluator implements Evaluator {

    private final AerospikeClient aerospikeClient;
    private final AerospikeLimitConfig limitConfig;

    public boolean validate(final String methodName, final String clientKey) {
        if (limitConfig == null
                || limitConfig.getClientLimits() == null
                || !limitConfig.getClientLimits().containsKey(clientKey)
                || !limitConfig.getClientLimits().get(clientKey).containsKey(methodName)) {
            return true;
        }

        Record record = null;

        long currentNumberOfRequests = 1L;
        val clientConfigs = limitConfig.getClientLimits();
        val clientMethodConfig = clientConfigs.get(clientKey).get(methodName);
        val numberOfRequestsAllowed = clientMethodConfig.getRequestsAllowed();
        val timePeriod = clientMethodConfig.getTimePeriodInMinutes();

        val namespace = limitConfig.getNamespace();
        val key = new Key(namespace, methodName, clientKey);

        val writePolicy = new WritePolicy();
        try {
            writePolicy.expiration = timePeriod * 60;
            writePolicy.recordExistsAction = RecordExistsAction.CREATE_ONLY;
            val newBin = new Bin(Constants.BIN_NAME, 1L);
            aerospikeClient.put(writePolicy, key, newBin);
        } catch (AerospikeException e) {
            if (e.getResultCode() == ResultCode.KEY_EXISTS_ERROR) {
                writePolicy.expiration = -2;
                writePolicy.recordExistsAction = RecordExistsAction.UPDATE_ONLY;
                val updateBin = new Bin(Constants.BIN_NAME, 1L);
                record = aerospikeClient.operate(writePolicy, key, Operation.add(updateBin), Operation.get());
                currentNumberOfRequests = record.getLong(Constants.BIN_NAME);
            }
        }

        if (record != null) {
            return currentNumberOfRequests <= numberOfRequestsAllowed;
        }
        return true;
    }
}
