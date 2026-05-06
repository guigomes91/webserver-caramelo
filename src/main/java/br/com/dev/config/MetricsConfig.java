package br.com.dev.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MetricsConfig {

    public static final MeterRegistry registry = new SimpleMeterRegistry();

    public static final Timer requestTimer = Timer.builder("http.server.requests")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry);

    public static final Counter requestCounter =
            registry.counter("http.server.count");
}