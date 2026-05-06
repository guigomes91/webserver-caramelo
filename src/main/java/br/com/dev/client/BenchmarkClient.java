package br.com.dev.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BenchmarkClient {

    static void main(String[] args) throws Exception {

        int requests = Integer.parseInt(System.getProperty("requests", "1000"));
        int concurrency = Integer.parseInt(System.getProperty("concurrency", "500"));

        System.out.println("Requests: " + requests);
        System.out.println("Concurrency: " + concurrency);

        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        HttpClient client = HttpClient.newHttpClient();

        CountDownLatch latch = new CountDownLatch(requests);

        Instant start = Instant.now();

        for (int i = 0; i < requests; i++) {
            executor.submit(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8001/caramelo"))
                            .timeout(Duration.ofSeconds(5))
                            .GET()
                            .build();

                    client.send(request, HttpResponse.BodyHandlers.ofString());

                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Instant end = Instant.now();

        long time = Duration.between(start, end).toMillis();

        System.out.println("Total time: " + time + " ms");
        System.out.println("Throughput: " + (requests * 1000.0 / time) + " req/s");

        executor.shutdown();
    }
}