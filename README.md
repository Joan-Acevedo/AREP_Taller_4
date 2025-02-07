# Web Server - Proyecto AREP

## Autor

Escuela Colombiana de Ingeniería Julio Garavito

Arquitectura Empresarial

Joan S. Acevedo A.

---

## Descripción

Este proyecto es un **servidor web desarrollado en Java** que permite:
- Servir archivos estáticos (HTML, CSS, JS, imágenes, etc.).
- Manejar una API REST dinámica mediante funciones lambda.
- Definir rutas REST de manera flexible con el método `get()`.
- Configurar la ubicación de los archivos estáticos con `staticfiles()`.

El servidor escucha peticiones en el **puerto 35000** y responde tanto a solicitudes de archivos estáticos como a endpoints REST definidos dinámicamente.

---

## Instalación

### **1. Prerrequisitos**
Para ejecutar este proyecto, asegúrate de tener instalado lo siguiente:
- **Java 17 o superior**
- **Maven**
- **Un editor de texto o IDE como IntelliJ IDEA o VS Code**

### **2. Clonar el Repositorio**
Abre una terminal y ejecuta el siguiente comando para clonar el proyecto:
```sh
    git clone https://github.com/Joan-Acevedo/AREP_Taller_2.git
```

### **3. Compilar el Proyecto**
Ejecuta el siguiente comando dentro de la carpeta del proyecto:
```sh
    mvn clean package
```

Esto generará un archivo `.jar` en la carpeta `target/` que puedes ejecutar posteriormente.

---

## Ejecución del Servidor

Para iniciar el servidor, dirigete a la clase HttpServer y corre/ejecuta la clase.

Una vez iniciado, puedes acceder a la aplicación desde tu navegador ingresando a:

---

Para ver el index.html:
```
http://localhost:35000/
```

Para ver el numero Pi:

```
http://localhost:35000/pi
```

Para consultar una imagen:

```
http://localhost:35000/oP.png
```

Para ver una situación donde no se encuentra el recurso:

```
http://localhost:35000/cualquier_cosa
```

---

## Arquitectura del Código

El proyecto sigue una arquitectura modular, estructurada de la siguiente manera:

1. **Servidor HTTP (`HttpServer.java`)**
   - Maneja conexiones entrantes y analiza solicitudes HTTP.
   - Permite definir rutas REST con `get()` y funciones lambda.
   - Sirve archivos estáticos desde una carpeta configurable.

2. **Archivos Estáticos (`recursos/`)**
   - `index.html`: Página principal con un formulario interactivo.
   - `script.js`: Contiene la lógica para comunicarse con el servidor usando `fetch()`.
   - `style.css`: Contiene los estilos para la interfaz web.

3. **Pruebas (`test/HttpServerTest.java`)**
   - Contiene pruebas unitarias para verificar el correcto funcionamiento del servidor y los endpoints REST.

---

## API REST Implementada

### **1. Obtener un saludo personalizado**
**Endpoint:**
```
GET /api/saludo?name={nombre}
```
**Ejemplo de Uso:**
```
http://localhost:35000/api/saludo?name=Joan
```
**Respuesta JSON:**
```json
{
  "name": "Joan",
  "mensaje": "Hola, Joan!"
}
```

### **2. Obtener el valor de PI**
**Endpoint:**
```
GET /pi
```
**Ejemplo de Uso:**
```
http://localhost:35000/pi
```
**Respuesta:**
```
3.141592653589793
```

---

## Pruebas Realizadas

Se implementaron pruebas unitarias en la carpeta `test/` para validar el correcto funcionamiento del servidor.

### **Ejecutar las pruebas**
Para ejecutar las pruebas, usa el siguiente comando en la terminal:

Existe la posibilidad de que las pruebas fallen por problemas que tuve con Maven.
```sh
    mvn test
```

#### **Ejemplos de pruebas realizadas:**
- **Prueba de respuesta HTTP 200 en `/api/saludo`**.
- **Prueba de manejo de parámetros en `get()`**.
- **Prueba de respuesta correcta en `/pi`**.
- **Prueba de respuesta HTTP 404 para recursos inexistentes**.

---

## Conclusión

Este proyecto demuestra cómo construir un **servidor web en Java** flexible y eficiente con soporte para archivos estáticos y una API REST modular. Gracias a la implementación de funciones lambda, la gestión de rutas REST es más dinámica y fácil de extender en futuras mejoras.

