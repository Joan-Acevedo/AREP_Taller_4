# Web Server - Proyecto AREP

## Autor

Escuela Colombiana de Ingeniería Julio Garavito

Arquitectura Empresarial 

Joan S. Acevedo A.

---

## Descripción
Este proyecto es un **servidor web básico** desarrollado en **Java**, que permite servir archivos estáticos 
(HTML, CSS, imágenes, etc.) y manejar una API REST sencilla para responder con mensajes personalizados.

---

## Instalación

### **1. Prerrequisitos**
Para ejecutar este proyecto, asegúrate de tener instalado lo siguiente:
- **Java 8 o superior**
- **Maven**
- **Un editor de texto o IDE como IntelliJ IDEA o VS Code**

### **2. Clonar el Repositorio**
Abre una terminal y ejecuta el siguiente comando para clonar el proyecto:
```sh
    git clone https://github.com/Joan-Acevedo/AREP_Taller_1.git
```

### **3. Compilar el Proyecto**
Ejecuta el siguiente comando dentro de la carpeta del proyecto:
```sh
    mvn clean package
```


---

## Ejecución del Servidor

Al iniciar el servidor este se ejecutará por el **puerto 35000**.

Una vez iniciado, puedes acceder a la aplicación desde tu navegador ingresando a:
```
http://localhost:35000/
```

---

##  Arquitectura del Código
El proyecto está basado en una arquitectura sencilla de **servidor HTTP** que sigue las siguientes funcionalidades:

1. **Servidor HTTP (`HttpServer.java`)**
    - Escucha peticiones en el puerto 35000.
    - Sirve archivos estáticos (HTML, CSS, JS, imágenes).
    - Responde a solicitudes API (`/api/saludo?name=nombre`).
   

2. **Archivos Estáticos (`recursos/`)**
    - `index.html`: Página principal.
    - `style.css`: Contiene los estilos, incluyendo el manejo del error 404.
    - `script.js`: Contiene la lógica para comunicarse con el servidor usando `fetch()`.

---

## API REST Implementada

### **Obtener un saludo personalizado**
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

---


## Conclusión
Este proyecto demuestra cómo construir un **servidor web en Java** con manejo de archivos estáticos y una 
API REST simple. También incorpora una página de error 404 personalizada.



