package com.github.ratelimiter.config.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LimitProperty {
    private int requestsAllowed;
    private int timePeriodInMinutes;
}
