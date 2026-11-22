# Cobertura de la LÓGICA de la app (ViewModels, repositorios, modelo y conversiones).
#
# El reporte de JaCoCo del plugin de Android (createDebugUnitTestCoverageReport) mide TODAS las
# clases, también las pantallas de Compose, que no se prueban con pruebas unitarias. El plugin no
# permite excluir clases, así que este script lee el reporte XML, se queda solo con la lógica y
# calcula el porcentaje de líneas cubiertas por paquete y en total. No cambia la compilación.
#
# Uso (desde app-android/, después de generar el reporte):
#   .\gradlew.bat testDebugUnitTest createDebugUnitTestCoverageReport
#   .\cobertura-logica.ps1
# Qué cuenta como lógica y por qué: docs/pruebas.md.

param(
    [string]$Reporte = "app\build\reports\coverage\test\debug\report.xml",
    # Si el total queda bajo este porcentaje, el script termina con error (código 1).
    [double]$Minimo = 80
)

if (-not (Test-Path $Reporte)) {
    Write-Error "No existe $Reporte. Ejecuta antes: .\gradlew.bat testDebugUnitTest createDebugUnitTestCoverageReport"
    exit 1
}

$base = "cl/duoc/reservasalas/app"

# Archivos que SÍ son lógica: carpeta (paquete) y nombre del archivo (expresión regular).
$logica = @(
    @{ Paquete = "$base/data/repository";  Archivo = '.*' },
    @{ Paquete = "$base/data/modelo";      Archivo = '.*' },
    @{ Paquete = "$base/data/remote/dto";  Archivo = '.*' },
    @{ Paquete = "$base/data/remote";      Archivo = '^TokenInterceptor\.kt$' },
    @{ Paquete = "$base/data/local";       Archivo = 'DataStore\.kt$' },
    @{ Paquete = "$base/model";            Archivo = '.*' },
    @{ Paquete = "$base/viewmodel";        Archivo = '.*' },
    @{ Paquete = "$base/ui/pantallas/.+";  Archivo = 'View[Mm]o[dv]el\.kt$' },
    @{ Paquete = "$base/ui/navegacion";    Archivo = '^Rutas\.kt$' },
    @{ Paquete = "$base/notificaciones";   Archivo = '^SincronizadorRecordatorios\.kt$' }
)

# Clases que se descartan aunque estén en un archivo de lógica:
# - $$serializer y $Companion: código que genera Kotlin (serialización) y las Factory de los
#   ViewModel, que solo sacan objetos del AppContainer.
# - *DataStoreKt: la línea "val Context.xxxDataStore by preferencesDataStore(...)" necesita Android.
$clasesExcluidas = '\$\$serializer|\$Companion|DataStoreKt$'

# Métodos que se descuentan dentro de una clase de lógica: Kotlin guarda el código de la Factory
# de cada ViewModel (el bloque "initializer { ... }") como métodos de la misma clase.
$metodosExcluidos = '^(getFactory|[Ff]actory\$lambda.*)$'

[xml]$xml = Get-Content $Reporte -Raw
$porPaquete = @{}

foreach ($paquete in $xml.report.package) {
    foreach ($clase in $paquete.class) {
        $esLogica = $false
        foreach ($regla in $logica) {
            if ($paquete.name -match "^$($regla.Paquete)$" -and $clase.sourcefilename -match $regla.Archivo) {
                $esLogica = $true
                break
            }
        }
        if (-not $esLogica -or $clase.name -match $clasesExcluidas) { continue }

        $lineas = $clase.counter | Where-Object { $_.type -eq "LINE" }
        if (-not $lineas) { continue }
        $cubiertas = [int]$lineas.covered
        $faltan = [int]$lineas.missed

        # Se restan las líneas de los métodos excluidos (Factory).
        foreach ($metodo in $clase.method) {
            if ($metodo.name -notmatch $metodosExcluidos) { continue }
            $lineasMetodo = $metodo.counter | Where-Object { $_.type -eq "LINE" }
            if ($lineasMetodo) {
                $cubiertas -= [int]$lineasMetodo.covered
                $faltan -= [int]$lineasMetodo.missed
            }
        }

        $nombre = $paquete.name.Replace("$base/", "").Replace("/", ".")
        if (-not $porPaquete.ContainsKey($nombre)) { $porPaquete[$nombre] = @{ Cubiertas = 0; Faltan = 0 } }
        $porPaquete[$nombre].Cubiertas += $cubiertas
        $porPaquete[$nombre].Faltan += $faltan
    }
}

$totalCubiertas = 0
$totalFaltan = 0
$filas = foreach ($nombre in ($porPaquete.Keys | Sort-Object)) {
    $p = $porPaquete[$nombre]
    $totalCubiertas += $p.Cubiertas
    $totalFaltan += $p.Faltan
    [pscustomobject]@{
        Paquete   = $nombre
        Lineas    = $p.Cubiertas + $p.Faltan
        Cubiertas = $p.Cubiertas
        Porcentaje = [math]::Round(100 * $p.Cubiertas / [math]::Max(1, $p.Cubiertas + $p.Faltan), 1)
    }
}
$filas | Format-Table -AutoSize

$total = [math]::Round(100 * $totalCubiertas / [math]::Max(1, $totalCubiertas + $totalFaltan), 1)
Write-Output "TOTAL lógica: $totalCubiertas de $($totalCubiertas + $totalFaltan) líneas cubiertas = $total %"

if ($total -lt $Minimo) {
    Write-Output "Bajo el mínimo de $Minimo %."
    exit 1
}
