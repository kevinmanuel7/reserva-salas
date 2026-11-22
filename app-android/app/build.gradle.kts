import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Interruptor de desarrollo: con "backend.local=true" en local.properties (que no se
// sube a GitHub) la app usa los microservicios del PC con adb reverse.
// Sin él, usa el servidor de AWS: así queda el repositorio y el APK que se entrega.
val propiedadesLocales = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
val usarBackendLocal = propiedadesLocales.getProperty("backend.local") == "true"
val urlAws = "http://54.204.140.199/api/v1/"

android {
    namespace = "cl.duoc.reservasalas.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.duoc.reservasalas.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // URL base de cada microservicio.
        buildConfigField("String", "URL_MS_USUARIOS", "\"${if (usarBackendLocal) "http://localhost:8081/api/v1/" else urlAws}\"")
        buildConfigField("String", "URL_MS_SALAS", "\"${if (usarBackendLocal) "http://localhost:8082/api/v1/" else urlAws}\"")
        buildConfigField("String", "URL_MS_RESERVAS", "\"${if (usarBackendLocal) "http://localhost:8083/api/v1/" else urlAws}\"")
        // API externa de feriados de Chile (Nager.Date). Pública: no lleva token ni clave.
        buildConfigField("String", "URL_API_FERIADOS", "\"https://date.nager.at/api/v3/\"")
    }

    signingConfigs {
        create("entrega") {
            // Llave creada SOLO para este proyecto académico. La pauta pide dejarla funcional
            // en el repositorio junto a su configuración. En un proyecto real, la llave y sus
            // contraseñas irían fuera de Git (por ejemplo, en variables de entorno).
            storeFile = file("../llave/reserva-salas.jks")
            storePassword = "proyectoapp"
            keyAlias = "reserva-salas"
            keyPassword = "proyectoapp"
        }
    }

    buildTypes {
        // Cobertura de las pruebas unitarias con JaCoCo (viene con el plugin de Android, no es un
        // plugin nuevo). Crea la tarea createDebugUnitTestCoverageReport. Ver docs/pruebas.md.
        debug {
            enableUnitTestCoverage = true
        }
        release {
            signingConfig = signingConfigs.getByName("entrega")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    // Solo para pruebas: reemplaza el hilo principal de Android en las pruebas del ViewModel.
    testImplementation(libs.kotlinx.coroutines.test)
    // Solo para pruebas: simula las APIs de Retrofit en las pruebas de los repositorios. No entra al APK.
    testImplementation(libs.mockk)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.mlkit.barcode.scanning)
}