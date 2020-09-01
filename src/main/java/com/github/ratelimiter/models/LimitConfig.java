package com.github.ratelimiter.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LimitConfig {

    private String namespace;
    private Map<String, LimitProperty> clientLimits;
}
