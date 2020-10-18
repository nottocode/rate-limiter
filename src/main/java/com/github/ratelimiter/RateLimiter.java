package com.github.ratelimiter;

import com.github.ratelimiter.filters.RateLimiterFilter;
import lombok.AllArgsConstructor;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
@AllArgsConstructor
public class RateLimiter implements DynamicFeature {

    private final Evaluator evaluator;

    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {
        if (resourceInfo.getResourceMethod().getAnnotation(RateLimited.class) != null) {
            featureContext.register(new RateLimiterFilter(resourceInfo, evaluator));
        }
    }
}
