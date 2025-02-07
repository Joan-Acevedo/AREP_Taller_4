package org.example;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpServerTest {
    private static Thread serverThread;

    @BeforeAll
    static void startServer() {
        serverThread = new Thread(() -> {
            try {
                HttpServer.main(new String[]{});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        serverThread.start();

        // Esperar a que el servidor se inicie completamente
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    @AfterAll
    static void stopServer() {
        serverThread.interrupt();
    }

    private String sendGetRequest(String path) throws IOException {
        URL url = new URL("http://localhost:35000" + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    @Test
    @Order(1)
    void testPiEndpoint() throws IOException {
        String response = sendGetRequest("/pi");
        assertEquals("3.141592653589793", response);
    }

    @Test
    @Order(2)
    void testSaludoEndpoint() throws IOException {
        String response = sendGetRequest("/api/saludo?name=Carlos");
        assertTrue(response.contains("\"name\": \"Carlos\""));
        assertTrue(response.contains("\"mensaje\": \"Hola, Carlos!\""));
    }

    @Test
    @Order(3)
    void testStaticFile() throws IOException {
        String response = sendGetRequest("/index.html");
        assertTrue(response.contains("<title>Servidor Web en Java</title>"));
    }

    @Test
    @Order(4)
    void testNotFound() {
        Exception exception = assertThrows(IOException.class, () -> sendGetRequest("/archivo-inexistente.txt"));
        assertTrue(exception.getMessage().contains("Server returned HTTP response code: 404"));
    }
}
