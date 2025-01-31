package org.example;

import java.io.*;
import java.net.*;

public class HttpServer {
    private static final int PORT = 35000;
    private static final String BASE_DIRECTORY = "web-server/src/main/java/recursos";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado en el puerto " + PORT + "...");

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
        String requestedFile = requestLine.split(" ")[1];

        // Verifica si la solicitud es para la API REST
        if (requestedFile.startsWith("/api/saludo")) {
            handleApiSaludo(writer, requestedFile);
        } else {
            serveStaticFile(out, writer, requestedFile);
        }

        writer.flush();
        in.close();
        clientSocket.close();
    }

    private static void handleApiSaludo(PrintWriter writer, String requestedFile) {
        String name = "Usuario";
        if (requestedFile.contains("?name=")) {
            name = requestedFile.split("\\?name=")[1].split("&")[0];
        }

        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println();
        writer.println("{\"name\": \"" + name + "\", \"mensaje\": \"Hola, " + name + "!\"}");
        writer.flush();
    }

    private static void serveStaticFile(OutputStream out, PrintWriter writer, String requestedFile) throws IOException {
        if (requestedFile.equals("/")) {
            requestedFile = "/index.html";
        }
        File file = new File(BASE_DIRECTORY + requestedFile);
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
}
