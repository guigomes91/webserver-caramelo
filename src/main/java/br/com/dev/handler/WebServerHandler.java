package br.com.dev.handler;

import br.com.dev.config.MetricsConfig;
import br.com.dev.database.DatabaseService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WebServerHandler implements HttpHandler {

    private static final Semaphore semaphore = new Semaphore(20);
    private static final AtomicInteger activeRequests = new AtomicInteger(0);

    private final boolean withLock;

    public WebServerHandler(boolean withLock) {
        this.withLock = withLock;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int current = activeRequests.incrementAndGet();
        long start = System.nanoTime();

        try {
            String response;

            if (withLock) {
                semaphore.acquire();
                try {
                    response = processTask();
                } finally {
                    semaphore.release();
                }
            } else {
                response = processTask();
            }

            MetricsConfig.requestCounter.increment();

            response += " | active=" + current + " | thread=" + Thread.currentThread();

            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            activeRequests.decrementAndGet();
            long duration = System.nanoTime() - start;
            MetricsConfig.requestTimer.record(duration, TimeUnit.NANOSECONDS);
        }
    }

    private String processTask() throws InterruptedException {
        return DatabaseService.query();
    }
}