package org.example;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.BiConsumer;

public class HttpServer {
    private static final int PORT = 35000;
    private static String baseDirectory = "web-server/src/main/java/recursos"; // Ahora es dinámico
    private static final Map<String, BiConsumer<Request, Response>> routeHandlers = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado en el puerto " + PORT + "...");

        // Definir carpeta de archivos estáticos según la especificación
        staticfiles("web-server/src/main/java/recursos");

        // Registrar la ruta REST con `get()`
        get("/api/saludo", (req, res) -> {
            String name = req.getValues("name").orElse("Usuario");
            res.sendJson("{\"name\": \"" + name + "\", \"mensaje\": \"Hola, " + name + "!\"}");
        });

        // Registrar la ruta "/pi"
        get("/pi", (req, res) -> res.sendJson(String.valueOf(Math.PI)));


        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            } catch (IOException e) {
                System.err.println("Error al aceptar conexión: " + e.getMessage());
            }
        }
    }

    private static void handleRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();
        PrintWriter writer = new PrintWriter(out, true);

        String requestLine = in.readLine();
        if (requestLine == null) {
            return;
        }

        System.out.println("Petición recibida: " + requestLine);
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 2) {
            return;
        }

        String requestedFile = requestParts[1];

        // Separar ruta y parámetros
        String path = requestedFile.split("\\?")[0];
        Map<String, String> params = getQueryParams(requestedFile);

        // Crear `Request` y `Response`
        Request req = new Request(path, params);
        Response res = new Response(writer);

        // Verificar si la ruta tiene un manejador registrado
        if (routeHandlers.containsKey(path)) {
            routeHandlers.get(path).accept(req, res);
        } else {
            serveStaticFile(out, writer, path);
        }

        writer.flush();
        in.close();
        clientSocket.close();
    }

    public static void get(String path, BiConsumer<Request, Response> handler) {
        routeHandlers.put(path, handler);
    }

    public static void staticfiles(String path) {
        baseDirectory = path; // Ahora la carpeta de estáticos es configurable
        System.out.println("Archivos estáticos configurados en: " + baseDirectory);
    }

    private static Map<String, String> getQueryParams(String url) {
        Map<String, String> params = new HashMap<>();
        if (url.contains("?")) {
            String[] parts = url.split("\\?");
            if (parts.length > 1) {
                String[] queryParams = parts[1].split("&");
                for (String param : queryParams) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        params.put(keyValue[0], keyValue[1]);
                    }
                }
            }
        }
        return params;
    }

    private static void serveStaticFile(OutputStream out, PrintWriter writer, String requestedFile) throws IOException {
        if (requestedFile.equals("/")) {
            requestedFile = "/index.html";
        }
        File file = new File(baseDirectory + requestedFile);
        if (file.exists() && !file.isDirectory()) {
            sendResponse(out, file);
        } else {
            sendNotFound(out);
        }
    }

    private static void sendResponse(OutputStream out, File file) throws IOException {
        String contentType = getContentType(file.getName());
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileData = fileInputStream.readAllBytes();
        fileInputStream.close();

        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + fileData.length);
        writer.println();
        writer.flush();

        out.write(fileData);
        out.flush();
    }

    private static void sendNotFound(OutputStream out) throws IOException {
        String errorPage = """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Error 404 - No Encontrado</title>
            <link rel="stylesheet" href="/style.css">
        </head>
        <body class="error-404">
            <h1>Error 404</h1>
            <p>Parece que Anya no encuentra el archivo solicitado en el servidor.</p>
        </body>
        </html>
        """;

        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 404 Not Found");
        writer.println("Content-Type: text/html");
        writer.println("Content-Length: " + errorPage.length());
        writer.println();
        writer.flush();

        out.write(errorPage.getBytes());
        out.flush();
    }

    private static String getContentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    // Clase para manejar las peticiones
    public static class Request {
        private final String path;
        private final Map<String, String> queryParams;

        public Request(String path, Map<String, String> queryParams) {
            this.path = path;
            this.queryParams = queryParams;
        }

        public Optional<String> getValues(String key) {
            return Optional.ofNullable(queryParams.get(key));
        }

        public String getPath() {
            return path;
        }
    }

    // Clase para manejar las respuestas
    public static class Response {
        private final PrintWriter writer;

        public Response(PrintWriter writer) {
            this.writer = writer;
        }

        public void sendJson(String json) {
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: application/json");
            writer.println();
            writer.println(json);
            writer.flush();
        }
    }
}
