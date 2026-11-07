# Carga las variables del .env en esta terminal y levanta el microservicio.
# Uso (desde la carpeta del microservicio): .\iniciar.ps1
Get-Content .env | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object {
    $nombre, $valor = $_ -split '=', 2
    Set-Item -Path "env:$nombre" -Value $valor
}
.\mvnw.cmd spring-boot:run