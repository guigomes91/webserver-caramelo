package br.com.dev;

import br.com.dev.config.MetricsConfig;
import br.com.dev.handler.WebServerHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Server {

    static void main(String[] args) throws Exception {
        boolean virtual = Boolean.parseBoolean(System.getProperty("virtual", "false"));
        boolean withLock = Boolean.parseBoolean(System.getProperty("lock", "false"));

        startWebServer(virtual, withLock);
    }

    private static void startWebServer(boolean virtual, boolean withLock) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", 8001), 0);

        setEndpointCarameloContext(withLock, httpServer);
        setEndpointMetrics(httpServer);

        if (virtual) {
            IO.println(">>> Using VIRTUAL THREADS");
            httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        } else {
            IO.println(">>> Using PLATFORM THREADS (fixed 200)");
            httpServer.setExecutor(Executors.newFixedThreadPool(200));
        }

        httpServer.start();
        IO.println("Server running on http://localhost:8001/caramelo");
    }

    private static void setEndpointCarameloContext(boolean withLock, HttpServer httpServer) {
        httpServer.createContext("/caramelo", new WebServerHandler(withLock));
    }

    private static void setEndpointMetrics(HttpServer httpServer) {
        httpServer.createContext("/metrics", exchange -> {
            StringBuilder response = new StringBuilder();

            MetricsConfig.registry.getMeters().forEach(meter -> {
                response.append(meter.getId()).append("\n");
            });

            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.toString().getBytes());
            exchange.close();
        });
    }
}