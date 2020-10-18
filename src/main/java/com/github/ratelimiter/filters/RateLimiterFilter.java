package com.github.ratelimiter.filters;

import com.github.ratelimiter.Constants;
import com.github.ratelimiter.Evaluator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
@AllArgsConstructor
public class RateLimiterFilter implements ContainerRequestFilter {

    private final ResourceInfo resourceInfo;
    private final Evaluator evaluator;

    public void filter(ContainerRequestContext containerRequestContext) {
        try {
            val methodName = resourceInfo.getResourceMethod().getName();
            val clientHeader = containerRequestContext.getHeaders().get(Constants.CLIENT_KEY);

            if (clientHeader != null && !clientHeader.isEmpty()) {
                val clientKey = clientHeader.get(0);
                val inLimit = evaluator.validate(methodName, clientKey);
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
