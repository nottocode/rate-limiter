package com.github.ratelimiter.config.impl;

import com.github.ratelimiter.config.LimitConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AerospikeLimitConfig extends LimitConfig {

    private String namespace;
    private Map<String, Map<String, LimitProperty>> clientLimits;
}
