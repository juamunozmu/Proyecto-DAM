# NachoQuest

**Explora. Descubre. Vive la UNAL.**

Juego 2D de exploración y retos ambientado en el campus real de la Universidad Nacional de Colombia,
Sede Bogotá. Proyecto del curso Desarrollo de Aplicaciones para Dispositivos Móviles (2026-II).

La exploración del campus es, al mismo tiempo, la mecánica principal, el contexto educativo y el
escenario de la historia: cada lugar emblemático propone un minijuego y unas preguntas sobre su
historia, y una narrativa por capítulos conecta todo.

## La historia: «El Olvido de la Ciudad Blanca»

Una bruma gris, el Olvido, está borrando la memoria del campus. Nacho, el espíritu guardián de la
universidad, recluta al jugador para recuperar un Fragmento de Memoria en cada edificio. Cada
capítulo entrega el ítem que abre el siguiente.

| Capítulo | Lugar | Minijuego | Ítem que entrega |
|---|---|---|---|
| 1. El Despertar | Plaza Santander (Plaza Che) | Rompecabezas 3×3 de la plaza | Carné Histórico UNAL |
| 2. Páginas en Blanco | Biblioteca Central Gabriel García Márquez | Letras borradas (ahorcado) | Regla de Takeuchi |
| 3. La Ecuación Perdida | Edificio Julio Garavito (Ingeniería, 401) | Circuito de piezas giratorias | Partitura Maestra |
| 4. La Última Sinfonía | Auditorio León de Greiff | Melodía para repetir (tipo Simon) | Brújula de Fritz Karsen |
| 5. El Último Rastro | Estadio Alfonso López Pumarejo | Tanda de penales contra el Olvido | Copa de la Memoria |

Tras el minijuego vienen 3 preguntas del guardián con temporizador, corazones y rachas, y un comodín
50:50 que se puede usar una vez por reto. El resultado da de 1 a 3 estrellas, XP, una insignia y un
coleccionable, y avisa si el jugador subió de nivel o desbloqueó un logro.

## Funcionalidades

- **Mapa del campus** (OpenStreetMap con osmdroid): 117 lugares, buscador por nombre, número de
  edificio o alias, filtros por categoría y agrupación de marcadores al alejar el zoom. La bruma
  del Olvido cubre los edificios de los capítulos pendientes y se despeja al completarlos.
- **GPS**: los capítulos se juegan a menos de 75 m del edificio. «Cómo llegar» traza una ruta a pie
  (OSRM) con distancia y tiempo, la recalcula si el jugador se desvía y avisa al llegar.
- **Retos**: línea de tiempo de los capítulos, que también se pueden jugar sin estar en el campus.
- **Retos de lugar**: 17 edificios emblemáticos que no son capítulos (Museo Leopoldo Rother,
  Observatorio, Hemeroteca, edificio Salmona, Conservatorio, etc.) tienen su propio reto de 3
  preguntas, sorteadas de un banco de 4 por lugar. Se marcan con un pin morado ❓, se juegan a menos
  de 75 m, dan XP y cuentan para los logros «Guía del campus» y «Enciclopedia andante». La pestaña
  Retos muestra el avance y permite repetir los ya superados. Las preguntas están en
  `domain/model/RetosLugares.kt`.
- **Trivia del día**: cinco preguntas nuevas cada día (las mismas para todos los jugadores),
  sacadas de un banco de 50, con racha de días seguidos que da XP extra.
- **Ranking**: tabla de posiciones con los jugadores que más XP tienen.
- **Monedas y tienda**: los capítulos (20-50), los retos de lugar (15) y la trivia del día (10)
  dan monedas la primera vez que se superan. En la tienda (desde el perfil) se compran:
  - *Ayudas* que quedan guardadas: comodín 50:50 extra, reloj de arena (+10 s), corazón extra y
    pista de minijuego. Si no hay ninguna guardada, se pagan con monedas en el momento de usarlas.
    El corazón sirve incluso justo después de perder el último.
  - *Pistas de minijuego*, hasta 3 por partida: el rompecabezas pone una pieza en su sitio, el
    ahorcado destapa una letra, el circuito gira una pieza del camino, la sinfonía toca la nota que
    sigue y los penales pasan a cámara lenta durante un tiro.
  - *Aspectos de Nacho* (gorra, birrete, gafas, gafas de sol, bufanda y camiseta dorada), uno por
    parte del cuerpo, que Nacho lleva en toda la app.

  El catálogo y las reglas de pago están en `domain/model/Tienda.kt`.
- **Exploración**: abrir la ficha de un lugar por primera vez lo descubre y da XP.
- **Sonido**: efectos sintetizados (no hay archivos de audio) en botones, minijuegos, preguntas y
  resultados; se apagan desde el menú del perfil.
- **Inventario**: objetos clave, insignias y coleccionables, con pistas para los que faltan.
- **Perfil**: nivel y XP, estadísticas, racha de trivia, progreso de la historia y logros. Desde su
  menú se puede reiniciar el progreso para volver a jugar la historia desde el prólogo.
- **Cuentas**: correo y contraseña (con recuperación por correo), Google y Apple.
- **Progreso en la nube**: se guarda en Cloud Firestore, funciona sin conexión y se sincroniza al
  volver la red.
- **Modo de pruebas**: el ícono 🛰️ del buscador del mapa (cambia a «DEV») y el enlace «Simular que
  estoy aquí» de cada lugar permiten probar los retos sin ir al campus.

## Arquitectura

- Kotlin, Jetpack Compose y Material 3.
- MVVM: un ViewModel por pantalla que expone `StateFlow`.
- Hilt para la inyección de dependencias (`di/AppModule.kt` provee Firebase).
- Navigation Compose con rutas tipadas (kotlinx.serialization).
- Firebase Authentication y Cloud Firestore. El acceso con Google usa Credential Manager.
- osmdroid y OSMBonusPack para el mapa, los marcadores y las rutas.

```
app/src/main/
├── assets/edificios_unal.json   Lugares del campus (coordenadas, categoría, alias)
└── java/com/unal/nachoquest/
    ├── di/                      Módulo de Hilt
    ├── domain/
    │   ├── model/               Historia, retos, ítems, logros y puntuación
    │   └── repository/          UserRepository: progreso del jugador en Firestore
    └── ui/
        ├── components/          Nacho, el Olvido, escenas de diálogo, efectos, sonidos y las
        │                        piezas con relieve (BotonJuego, tarjetaJuego)
        ├── navigation/          Navegación, barra inferior y escenas de la historia
        ├── screens/             splash, auth, map, challenges, quiz (y minijuegos), inventory, profile
        └── theme/               Paleta, tipografía (títulos con la fuente Jersey 15) y tema
```

La interfaz tiene aspecto de juego retro: los títulos y marcadores usan una fuente pixelada, a juego
con el sprite de Nacho, y los botones y tarjetas tienen un canto inferior que les da relieve.

El progreso de cada jugador vive en el documento `users/{uid}` de Firestore. Mientras hay sesión,
`UserRepository` lo escucha en tiempo real, y las escrituras solo envían los campos que cambian
(`arrayUnion`, `increment`), así que un perfil que todavía no ha cargado nunca sobrescribe el
progreso guardado.

## Compilar y ejecutar

Requisitos: Android Studio (o JDK 17 y el Android SDK con la plataforma 35). La app funciona desde
Android 8.0 (API 26).

```bash
./gradlew assembleDebug        # APK en app/build/outputs/apk/debug/
./gradlew installDebug         # instala en el dispositivo o emulador conectado
./gradlew testDebugUnitTest    # pruebas unitarias
```

Gradle 8.11 no arranca con un JDK más nuevo que el 21. Si el JDK por defecto del sistema es
posterior, hay que indicarle el 17: `JAVA_HOME=/ruta/al/jdk-17 ./gradlew assembleDebug`.

Las pruebas unitarias también revisan que la historia, el banco de preguntas, el catálogo de ítems y
`edificios_unal.json` sigan siendo coherentes. Conviene correrlas después de editar el json.

## Configuración de Firebase

El proyecto ya incluye `app/google-services.json` (proyecto `nachoquest`). En la consola de Firebase:

0. **Firestore Database → Crear base de datos** (modo de producción). Sin este paso el progreso
   solo queda en la caché del teléfono y el ranking no se comparte entre jugadores.
1. **Authentication → Sign-in method**: habilitar *Correo electrónico/contraseña* y *Google*.
2. **Google en cada computador**: registrar la huella SHA-1 de la llave de depuración de cada
   integrante en *Configuración del proyecto → Tus apps* (se obtiene con `./gradlew signingReport`).
   Después, descargar de nuevo `google-services.json`.
3. **Apple (opcional)**: requiere una cuenta del Apple Developer Program. Se habilita el proveedor
   *Apple* en Firebase con el Services ID, el Team ID y la llave. Mientras no esté habilitado, el
   botón «Continuar con Apple» muestra un aviso y el jugador puede entrar con correo o Google.
4. **Reglas de Firestore**: `firestore.rules` permite que cada jugador lea y escriba solo su propio
   documento, y que cualquier jugador con sesión lea el ranking (`ranking/{uid}`, que solo lleva
   nombre, XP, nivel y estrellas). Se publican con la CLI de Firebase:

   ```bash
   firebase deploy --only firestore:rules
   ```

## Documentos

En `docs/` están la propuesta del proyecto (Reto 1), la presentación, el modelo Canvas y los
mockups (Reto 2).

## Créditos

- Mapas: © colaboradores de OpenStreetMap. Rutas a pie: OSRM.
- Fuente de los títulos: [Jersey 15](https://fonts.google.com/specimen/Jersey+15), de Sarah
  Cadigan-Fried, bajo la licencia SIL Open Font License 1.1 (`docs/licencias/OFL-Jersey15.txt`).

## Equipo

- Daniel Fernando Mateus Vega
- Juan Esteban Muñoz Muñoz
- Santiago Ballén Leguízamo
