package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.framework.GetMapping;
import org.example.framework.RequestParam;
import org.example.framework.RestController;
import org.reflections.Reflections;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.*;
import java.util.*;

public class HttpServer {
    private static final int PORT = 35000;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_DIRECTORY = "web-server/src/main/java/recursos";
    private static final Map<String, Method> routeHandlers = new HashMap<>();
    private static final Map<String, Object> controllerInstances = new HashMap<>();

    public static void main(String[] args) throws Exception {
        loadRestControllers();
        startServer();
    }


    private static void startServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado en http://localhost:" + PORT);

        while (true) {
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 OutputStream out = clientSocket.getOutputStream();
                 PrintWriter writer = new PrintWriter(out, true)) {

                String requestLine = reader.readLine();
                if (requestLine == null) continue;
                System.out.println("Petición recibida: " + requestLine);

                String[] parts = requestLine.split(" ");
                if (parts.length < 2) continue;

                String requestedFile = parts[1];
                String path = requestedFile.split("\\?")[0];
                Map<String, String> params = getQueryParams(requestedFile);

                Request req = new Request(path, params);
                Response res = new Response(writer);

                if (routeHandlers.containsKey(path)) {
                    Method method = routeHandlers.get(path);
                    Object controller = controllerInstances.get(method.getDeclaringClass().getName());
                    Object[] args = resolveMethodArguments(method, req, res, params);
                    Object response = method.invoke(controller, args);
                    sendResponse(writer, response);
                } else {
                    serveStaticFile(out, writer, path);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadRestControllers() throws Exception {
        Reflections reflections = new Reflections("org.example.controllers");
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(RestController.class);

        for (Class<?> controller : controllers) {
            Object instance = controller.getDeclaredConstructor().newInstance();
            controllerInstances.put(controller.getName(), instance);

            for (Method method : controller.getDeclaredMethods()) {
                if (method.isAnnotationPresent(GetMapping.class)) {
                    String path = method.getAnnotation(GetMapping.class).value();
                    routeHandlers.put(path, method);
                    System.out.println("Registrado endpoint: " + path);
                }
            }
        }
    }

    private static Object[] resolveMethodArguments(Method method, Request req, Response res, Map<String, String> params) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (param.getType().equals(Request.class)) {
                args[i] = req;
            } else if (param.getType().equals(Response.class)) {
                args[i] = res;
            } else if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = param.getAnnotation(RequestParam.class);
                String paramName = requestParam.value();
                String defaultValue = requestParam.defaultValue();
                String paramValue = params.get(paramName);

                // Si el parámetro no está en la URL o está vacío, usa el valor por defecto
                if (paramValue == null || paramValue.isEmpty()) {
                    paramValue = defaultValue;
                }

                // Decodifica el valor del parámetro
                if (paramValue != null) {
                    try {
                        paramValue = URLDecoder.decode(paramValue, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                // Convertir el valor al tipo de parámetro correspondiente
                if (param.getType().equals(String.class)) {
                    args[i] = paramValue;
                } else if (param.getType().equals(int.class) || param.getType().equals(Integer.class)) {
                    args[i] = (paramValue != null) ? Integer.parseInt(paramValue) : 0;
                } else if (param.getType().equals(double.class) || param.getType().equals(Double.class)) {
                    args[i] = (paramValue != null) ? Double.parseDouble(paramValue) : 0.0;
                } else {
                    args[i] = null;
                }
            }
        }
        return args;
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
        byte[] fileData = new FileInputStream(file).readAllBytes();

        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + fileData.length);
        writer.println();
        writer.flush();
        out.write(fileData);
        out.flush();
    }

    private static void sendResponse(PrintWriter writer, Object response) throws IOException {
        if (response instanceof String) {
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/plain");
            writer.println("Content-Length: " + ((String) response).length());
            writer.println();
            writer.println(response);
        } else {
            String jsonResponse = objectMapper.writeValueAsString(response);
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: application/json");
            writer.println("Content-Length: " + jsonResponse.length());
            writer.println();
            writer.println(jsonResponse);
        }
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

    public static class Response {
        private final PrintWriter writer;

        public Response(PrintWriter writer) {
            this.writer = writer;
        }

        public void sendJson(Object object) throws IOException {
            String json = objectMapper.writeValueAsString(object);
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: application/json");
            writer.println();
            writer.println(json);
            writer.flush();
        }
    }
}
