package com.github.ratelimiter.filters;

import com.aerospike.client.AerospikeClient;
import com.github.ratelimiter.Constants;
import com.github.ratelimiter.impl.Validator;
import com.github.ratelimiter.models.LimitConfig;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class RateLimiterFilter implements ContainerRequestFilter {

    private final ResourceInfo resourceInfo;
    private final LimitConfig limitConfig;
    private final Validator validator;

    public RateLimiterFilter(final ResourceInfo resourceInfo,
                             final AerospikeClient aerospikeClient,
                             final LimitConfig limitConfig) {
        this.resourceInfo = resourceInfo;
        this.limitConfig = limitConfig;
        this.validator = new Validator(aerospikeClient, limitConfig);
    }

    public void filter(ContainerRequestContext containerRequestContext) {
        try {
            val methodName = resourceInfo.getResourceMethod().getName();
            val clientHeader = containerRequestContext.getHeaders().get(Constants.CLIENT_KEY);

            val clientConfigs = limitConfig.getClientLimits();
            if (clientHeader != null && !clientHeader.isEmpty() && clientConfigs.containsKey(clientHeader.get(0))) {
                val clientKey = clientHeader.get(0);
                val inLimit = validator.validateLimit(methodName, clientKey);
                if (!inLimit) {
                    log.info("Request blocked by Rate-Limiter. Url({}), Method ({})",
                            containerRequestContext.getUriInfo().getAbsolutePath(), methodName);
                    containerRequestContext.abortWith(
                            Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
                }
            }
        } catch (Exception e) {
            log.error("Failed to evaluate rate limiting", e);
        }
    }
}
