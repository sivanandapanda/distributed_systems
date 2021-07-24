package com.example;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;

public class WebServer {

    private static final String HEALTH_ENDPOINT = "/health";

    private final int port;
    private HttpServer httpServer;

    public WebServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext healthContext = httpServer.createContext(HEALTH_ENDPOINT);
        healthContext.setHandler(this::handleHealthCheckRequest);

        httpServer.setExecutor(Executors.newFixedThreadPool(8));
        httpServer.start();
    }

    public void stopServer() {
        httpServer.stop(0);
    }

    public void handleHealthCheckRequest(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }
        String responseMessage = "Server is alive " + LocalDateTime.now();
        sendResponse(responseMessage.getBytes(), exchange);
    }

    private void sendResponse(byte[] bytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(bytes);
        responseBody.flush();
        responseBody.close();
    }
}