function sendRequest() {
    let name = document.getElementById("name").value;

    if (!name) {
        alert("Por favor, ingresa un nombre");
        return;
    }

    fetch(`/api/saludo?name=${encodeURIComponent(name)}`)
        .then(response => response.text())
        .then(data => {
            document.getElementById("response").innerText = data;
        })
        .catch(error => {
            console.error("Error en la petición:", error);
            document.getElementById("response").innerText = "Error al obtener la respuesta.";
        });

}
