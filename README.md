# PetCare 2.0 🐾 — Entrega 3: App nativa

Aplicación **nativa Android** desarrollada en **Kotlin + Jetpack Compose** con Android Studio para el curso **Aplicaciones Móviles (UPB)**.

**Tema elegido:** Control de mascotas (vacunación y desparasitación).

PetCare permite registrar mascotas, llevar su carné de vacunación y recibir alertas en el menú cuando una vacuna está **vencida** o **próxima** a vencer.

## ✨ Funcionalidades

**Identidad visual propia:** paleta jade + coral + girasol sobre fondo cálido, tipografías **Fredoka** (títulos) y **Nunito** (texto), patrón de huellas, tarjetas redondeadas, barra de navegación flotante, animaciones y modo oscuro completo.

- **Introducción (onboarding):** 3 páginas deslizables la primera vez que se abre la app, y pantalla de arranque con el logo.
- **Cuentas locales:** registro e inicio de sesión con validaciones e indicador de seguridad de la contraseña; contraseña guardada como hash SHA-256; la sesión se recuerda.
- **Inicio:** tarjeta "Próxima atención" con cuenta regresiva, accesos rápidos, carrusel de mascotas, resumen y lista de vacunas que necesitan atención.
- **Mascotas (CRUD):** foto desde la galería, color identificador, especie, raza, sexo, nacimiento, peso y notas. Listado en cuadrícula con búsqueda y filtro por especie.
- **Perfil de la mascota:** encabezado con su color, **salud del carné** (anillo de %), y pestañas **Vacunas** (línea de tiempo), **Peso** (gráfica de evolución) e **Info**.
- **Carné de vacunación:** sugerencias rápidas, atajos +1 mes / +6 meses / +1 año, estados *Vencida*, *Próxima*, *Al día* o *Sin refuerzo*, y **Deshacer** al eliminar.
- **Control de peso:** historial de mediciones y gráfica dibujada con Canvas.
- **Calendario mensual:** refuerzos programados y vacunas aplicadas con puntos de color; al tocar un día se ven sus vacunas.
- **Recordatorios:** notificación diaria (WorkManager) con las vacunas vencidas o próximas; botón para probarla.
- **Carné en PDF:** genera el carné con la foto y el historial y lo comparte (WhatsApp, correo, Drive…).
- **Ajustes:** perfil, tema claro/oscuro/sistema, anticipación de alertas (7/15/30/60 días), datos de ejemplo, borrar datos y cerrar sesión.
- **Créditos:** información del estudiante y tecnologías.

## 🏗️ Arquitectura

```
MVVM  ·  UI (Compose) → ViewModel (StateFlow) → Repositorio → Room / DataStore
```

```
PetCare/app/src/main/java/com/upb/petcare/
├── PetCareApp.kt            # Application + contenedor de dependencias
├── MainActivity.kt          # Tema y navegación
├── data/
│   ├── Entities.kt          # User, Pet, Vaccine, WeightEntry (tablas Room)
│   ├── Daos.kt              # Consultas SQL
│   ├── AppDatabase.kt       # Base de datos Room (v2, con migración desde v1)
│   ├── Repositories.kt      # AuthRepository, PetRepository
│   └── SettingsRepository.kt# DataStore (sesión, tema, alertas, recordatorios, onboarding)
├── ui/
│   ├── ViewModels.kt        # AuthViewModel, PetViewModel, SettingsViewModel
│   ├── navigation/NavGraph.kt # Rutas, transiciones y barra inferior flotante
│   ├── components/          # Avatar, huellas, anillo de progreso, gráfica de peso, campos, botones
│   ├── screens/             # Onboarding, login, registro, inicio, mascotas, detalle, formularios, calendario, ajustes, créditos
│   └── theme/Theme.kt       # Colores, tipografías Fredoka/Nunito, formas, modo oscuro
└── util/
    ├── PetUtils.kt          # Fechas, edad, estado de vacunas y salud del carné
    ├── PhotoStorage.kt      # Guarda y reduce la foto elegida
    ├── Reminders.kt         # Notificaciones diarias con WorkManager
    └── PdfExporter.kt       # Carné en PDF y compartir
```

**Tecnologías:** Kotlin 2.1 · Jetpack Compose · Material 3 · Room 2.7 · DataStore · Navigation Compose · ViewModel + Coroutines/Flow · WorkManager · Coil · Photo Picker · PdfDocument · SplashScreen · minSdk 26 · targetSdk 36. Fuentes Fredoka y Nunito (SIL Open Font License).

## ▶️ Cómo ejecutar

1. Abrir la carpeta `PetCare/` en **Android Studio**.
2. Esperar la sincronización de Gradle.
3. Ejecutar ▶ en un emulador o teléfono (Android 8.0+).
4. Crear una cuenta y, para la demo, ir a **Ajustes → Cargar datos de ejemplo**.

**Cuenta de demostración (emulador `PetCare_API36`):** correo `demo@upb.edu.co` · contraseña `123456` (ya tiene mascotas y vacunas de ejemplo).

Por línea de comandos:

```bash
cd PetCare
./gradlew assembleDebug      # APK de prueba: app/build/outputs/apk/debug/app-debug.apk
./gradlew bundleRelease      # AAB para Play Store: app/build/outputs/bundle/release/app-release.aab
```

## ✏️ Antes de entregar

1. **Datos del estudiante:** Juan Sebastián Quintero Hernández · ID 000492758 (se editan en el objeto `StudentInfo` de `ui/screens/SettingsScreens.kt`).
2. **Wireframe en papel:** dibujar las 9 pantallas a mano, tomar fotos y guardarlas en `wireframes/papel/`.
3. **Figma:** crear un archivo en Figma y arrastrar los `.svg` de `wireframes/digital/` (se importan como vectores editables). Usar `00_tablero_wireframes.png` como guía del flujo y conectar las pantallas en modo *Prototype*. Pegar el enlace de Figma aquí: `ENLACE_FIGMA`.

## 🚀 Publicación en Google Play

1. En Android Studio: **Build → Generate Signed App Bundle / APK → Android App Bundle**, crear un *keystore* nuevo (guárdalo bien, no se sube al repositorio).
   - Alternativa: crear `PetCare/keystore.properties` con `storeFile`, `storePassword`, `keyAlias`, `keyPassword` y ejecutar `./gradlew bundleRelease` (ya está configurado en `app/build.gradle.kts`).
2. En **Google Play Console** (cuenta de la invitación del curso): *Crear app* → nombre **PetCare**, idioma español, gratuita.
3. Completar la ficha: descripción, ícono 512×512, gráfico destacado 1024×500 y capturas (puedes usar las de `capturas/`).
4. Completar *Contenido de la app* (política de privacidad, clasificación de contenido, público objetivo, seguridad de los datos: la app guarda los datos **solo en el dispositivo**).
5. Subir `app-release.aab` a **Pruebas internas** o **Producción** y enviar a revisión.
