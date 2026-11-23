# NachoQuest

**Explora. Descubre. Vive la UNAL.**

Juego 2D de exploración y retos ambientado en el campus real de la Universidad Nacional de Colombia,
Sede Bogotá. Proyecto del curso Desarrollo de Aplicaciones para Dispositivos Móviles (2026-II).

La exploración del campus es, al mismo tiempo, la mecánica principal, el contexto educativo y el
escenario de la historia: cada lugar emblemático propone un minijuego y unas preguntas sobre su
historia, y una narrativa por capítulos conecta todo.

## La historia

La historia tiene dos actos y ocho capítulos. Cada capítulo entrega el ítem que abre el siguiente, y
el último de cada acto termina con un epílogo.

### Acto I: «El Olvido de la Ciudad Blanca»

Una bruma gris, el Olvido, está borrando la memoria del campus. Nacho, el espíritu guardián de la
universidad, recluta al jugador para recuperar un Fragmento de Memoria en cada edificio.

| Capítulo | Lugar | Minijuego | Ítem que entrega |
|---|---|---|---|
| 1. El Despertar | Plaza Santander (Plaza Che) | Rompecabezas 3×3 de la plaza | Carné Histórico UNAL |
| 2. Páginas en Blanco | Biblioteca Central Gabriel García Márquez | Letras borradas (ahorcado) | Regla de Takeuchi |
| 3. La Ecuación Perdida | Edificio Julio Garavito (Ingeniería, 401) | Circuito de piezas giratorias | Partitura Maestra |
| 4. La Última Sinfonía | Auditorio León de Greiff | Melodía para repetir (tipo Simon) | Brújula de Fritz Karsen |
| 5. El Último Rastro | Estadio Alfonso López Pumarejo | Tanda de penales contra el Olvido | Copa de la Memoria |

### Acto II: «Los Ecos del Olvido»

Al final del epílogo del Acto I, la Copa de la Memoria se agrieta. Dentro hay un mensaje: al perder
en el estadio, el Olvido no desapareció, sino que se partió en tres Ecos que se escondieron «donde la
gente guarda lo que más quiere». Si no se encuentran, la Copa se rompe y todo vuelve a borrarse. En
el último capítulo, el Olvido decide quedarse a recordar y se convierte en el Recuerdo.

| Capítulo | Lugar | Minijuego | Ítem que entrega |
|---|---|---|---|
| 6. Colores Robados | Museo de Arte UNAL (317) | La copia falsa: encontrar 5 diferencias entre el cuadro y la copia del Eco | Pincel Restaurador |
| 7. El Rebaño Perdido | Facultad de Medicina Veterinaria y de Zootecnia (501) | Parejas de memoria con 8 animales del potrero | Cencerro de la Memoria |
| 8. La Voz del Campus | Concha Acústica (761) | Laberinto en la bruma: solo se ve alrededor de Nacho | Copa Restaurada |

Quien ya había terminado el Acto I sigue directo con el capítulo 6: la Copa de la Memoria que tiene
en el inventario es la que lo abre. El Acto II suma tres insignias, tres coleccionables y el logro
«Guardián de la Memoria».

Tras el minijuego vienen 3 preguntas del guardián con temporizador, corazones y rachas, y un comodín
50:50 que se puede usar una vez por reto. El resultado da de 1 a 3 estrellas, XP, una insignia y un
coleccionable, y avisa si el jugador subió de nivel o desbloqueó un logro.

## Funcionalidades

- **Mapa del campus** (OpenStreetMap con osmdroid): 117 lugares, buscador por nombre, número de
  edificio o alias, filtros por categoría y agrupación de marcadores al alejar el zoom. La bruma
  del Olvido cubre los edificios de los capítulos pendientes y se despeja al completarlos.
  - *Límites*: el mapa solo se mueve dentro del campus con unos 250 m de margen, y el zoom mínimo se
    calcula según el ancho de la pantalla para que al alejarse se vea el campus de lado a lado sin
    que sobre ciudad (`ui/screens/map/LimitesMapa.kt`).
  - *Fotos reales*: al tocar uno de los lugares principales, la ficha muestra una foto real del
    edificio con el crédito de su autor y su licencia (`ui/screens/map/FotosLugares.kt`, imágenes en
    `res/drawable-nodpi/`). Hoy tienen foto la Plaza Che, la Biblioteca Central, el León de Greiff,
    el Museo Leopoldo Rother, el Edificio 401, el Edificio Yu Takeuchi y Veterinaria.
  - *Sin conexión*: cada imagen del mapa que se descarga queda guardada en el teléfono (hasta
    ~200 MB, válida por 30 días) y se muestra aunque no haya red; en ese caso aparece el aviso
    «Sin conexión · se muestra el mapa guardado». No se descarga nada por adelantado, porque las
    reglas del servidor de OpenStreetMap no permiten bajar zonas con zoom 17 o más para usarlas sin
    conexión: el campus queda disponible a medida que se recorre con red.
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
- **Galería de minijuegos** (solo en la compilación de debug): abre el minijuego de cualquier
  capítulo sin jugar la historia ni tocar Firebase, con pistas gratis. Con el emulador o el teléfono
  conectado:
  `adb shell am start -n com.unal.nachoquest/.GaleriaMinijuegosActivity` (o con `--ei capitulo 8`
  para ir directo a uno). Está en `app/src/debug/` y no se incluye en el release.

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

### Compilar el release

```bash
./gradlew assembleRelease      # APK en app/build/outputs/apk/release/
```

El release pasa por R8, que quita el código y los recursos que no se usan (pesa unos 4,6 MB frente a
los ~27 MB del debug). Las reglas para que Firebase, la navegación y el acceso con Google sigan
funcionando están en `app/proguard-rules.pro`. En el release también se probaron el inicio de sesión,
el mapa, los retos, la tienda y los aspectos de Nacho.

**Firma.** El release se firma con la llave que indique el archivo `keystore.properties` en la raíz
del proyecto. Ese archivo y la llave (`.jks`) **no se suben a Git** (están en `.gitignore`) y se
comparten por un canal privado entre los integrantes:

```properties
storeFile=C:/ruta/a/nachoquest-release.jks
storePassword=...
keyAlias=nachoquest
keyPassword=...
```

Sin `keystore.properties`, el release se firma con la llave de depuración: sirve para probarlo (y es
lo que hace la integración continua), pero el APK que se entregue debe salir con la llave del
proyecto. Si se pierde la llave no se puede publicar una actualización con la misma firma, así que
hay que guardar una copia.

Huella de la llave del release, para registrarla en Firebase (ver abajo):
`SHA-1 75:0A:DB:C9:F3:B4:1A:65:6C:52:B0:45:C3:D8:75:3A:A7:13:83:1B`

### Integración continua

`.github/workflows/android.yml` corre en GitHub Actions en cada push a `main` y en cada pull
request: ejecuta las pruebas unitarias y compila el APK de debug y el de release. El resultado
aparece en el pull request, y en la pestaña *Actions* se pueden descargar el APK de debug y el
reporte de pruebas. Es gratis porque el repositorio es público.

## Configuración de Firebase

El proyecto ya incluye `app/google-services.json` (proyecto `nachoquest`). En la consola de Firebase:

0. **Firestore Database → Crear base de datos** (modo de producción). Sin este paso el progreso
   solo queda en la caché del teléfono y el ranking no se comparte entre jugadores. *Hecho el
   22 de noviembre de 2026, con las reglas de `firestore.rules` publicadas desde la consola.* Lo
   que cada jugador había guardado antes en su teléfono se sube solo la próxima vez que abra la
   app con conexión, siempre que no la haya desinstalado (al desinstalarla se borra esa caché).
1. **Authentication → Sign-in method**: habilitar *Correo electrónico/contraseña* y *Google*.
2. **Google en cada computador**: registrar la huella SHA-1 de la llave de depuración de cada
   integrante en *Configuración del proyecto → Tus apps* (se obtiene con `./gradlew signingReport`).
   Para que Google funcione también en el APK de release, hay que registrar además la huella de la
   llave del release (está en «Compilar el release»). Después, descargar de nuevo
   `google-services.json`.
3. **Apple (opcional)**: requiere una cuenta del Apple Developer Program. Se habilita el proveedor
   *Apple* en Firebase con el Services ID, el Team ID y la llave. Mientras no esté habilitado, el
   botón «Continuar con Apple» muestra un aviso y el jugador puede entrar con correo o Google.
4. **Reglas de Firestore**: `firestore.rules` permite que cada jugador lea y escriba solo su propio
   documento, y que cualquier jugador con sesión lea el ranking (`ranking/{uid}`, que solo lleva
   nombre, XP, nivel y estrellas). Se publican con la CLI de Firebase:

   ```bash
   firebase deploy --only firestore:rules
   ```

## Pendientes

Trabajo identificado que todavía no se ha hecho, en orden de prioridad:

1. **Más fotos de edificios.** Ya hay 7 (ver «Mapa del campus»). Faltan, con foto ya elegida en
   Wikimedia Commons pero sin descargar: Estadio Alfonso López, Observatorio, Edificio Manuel Ancízar,
   Edificio Salmona (Posgrados de Ciencias Humanas), Sociología, Conservatorio, Instituto de Ciencias
   Naturales, Facultad de Derecho, Facultad de Medicina, Edificio Antonio Nariño e IGAC. No hay foto
   adecuada en Commons del Museo de Arte, la Concha Acústica (solo panorámicas muy estrechas), la
   Hemeroteca, el Edificio Uriel Gutiérrez, el Servicio Geológico ni el Edificio Santander. Para
   descargar de Commons hay que usar tamaños estándar (p. ej. 1280 px) y pausas largas, o devuelve
   error 429. Cada foto nueva va reducida a ~800 px en `res/drawable-nodpi/`, con su autor y
   licencia en `FotosLugares.kt` y en «Créditos».
2. **Pruebas de interfaz (Compose).** Las pruebas actuales cubren la lógica y los datos, pero ninguna
   la interfaz. Faltan pruebas instrumentadas, sin Firebase real, de: validación del acceso y del
   registro, el 50:50 y el +10 s en las preguntas, la compra en la tienda (con saldo y sin saldo) y
   la pista de algún minijuego. Correrlas también en GitHub Actions con un emulador es opcional
   (gratis en repos públicos, pero suma unos 10 minutos al workflow).
3. **Revisar los datos de las preguntas.** Algunas preguntas tienen datos que conviene que alguien
   del equipo confirme: en los retos de lugar (`RetosLugares.kt`) `conservatorio_1`, `icn_2`,
   `takeuchi_4` y `medicina_1`, y en el capítulo 1 (`BancoRetos.kt`) el año del mural del Che
   (`che_2`) y el edificio del costado oriental de la plaza (`che_5`).
4. **Subir el `targetSdk` a 35/36.** Solo es obligatorio para publicar en Google Play. Desde la API 35
   Android dibuja las apps de borde a borde, así que hay que revisar que ninguna pantalla quede
   debajo de las barras del sistema.

Hecho: el **mapa sin conexión** quedó resuelto con la caché de lo ya visto (ver «Mapa del campus» en
Funcionalidades). No se precarga el campus porque las reglas del servidor de OpenStreetMap no lo
permiten.

## Documentos

En `docs/` están la propuesta del proyecto (Reto 1), la presentación, el modelo Canvas y los
mockups (Reto 2).

## Créditos

- Mapas: © colaboradores de OpenStreetMap. Rutas a pie: OSRM.
- Fotos de los edificios, de Wikimedia Commons (reducidas a 800 px):
  - Plaza Che: Carlos25i, CC BY-SA 4.0 — `File:Plaza Che - Universidad Nacional de Colombia - Sede Bogotá - 2023.jpg`
  - Biblioteca Central: Smcamposa, CC BY-SA 3.0 — `File:Biblioteca Central UNAL.jpg`
  - Auditorio León de Greiff: JuanGris (Lucía Estévez), CC BY-SA 3.0 — `File:Auditorio León de Greiff. Universidad Nacional de Colombia.jpg`
  - Museo de Arquitectura Leopoldo Rother: MizhyMonroy, CC BY-SA 3.0 — `File:Museo de arquitectura Leopoldo Rother.jpg`
  - Edificio 401 (Ingeniería): Carlos25i, CC BY-SA 4.0 — `File:Universidad nacional de Colombia - Edificio Facultad de Ingeniería 401.jpg`
  - Edificio Yu Takeuchi: Gaaplex, CC BY-SA 4.0 — `File:Edificio Yu Takeuchi.jpg`
  - Facultad de Veterinaria: JuanGris (Lucía Estévez), CC BY-SA 3.0 — `File:Edificio de veterinaria, Universidad Nacional.JPG`
- Fuente de los títulos: [Jersey 15](https://fonts.google.com/specimen/Jersey+15), de Sarah
  Cadigan-Fried, bajo la licencia SIL Open Font License 1.1 (`docs/licencias/OFL-Jersey15.txt`).

## Equipo

- Daniel Fernando Mateus Vega
- Juan Esteban Muñoz Muñoz
- Santiago Ballén Leguízamo
