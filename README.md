# Extreme Disasters 1.0.0 — Etapa 1

Proyecto Forge para Minecraft Java Edition 1.20.1, Forge 47.4.10 y Java 17. Esta entrega implementa la primera etapa descrita en el diseño: un tornado móvil, un embudo 3D animado, fuerzas de viento sobre entidades, comandos administrativos y bloques físicos que se desprenden, orbitan, salen disparados, dañan y vuelven a colocarse o se convierten en un objeto.

La versión es una base jugable, no la lista completa de catástrofes del texto. Todavía no incluye tornado de fuego, protección de zonas, sirenas, objetos propios, huracanes, volcanes, terremotos, agua ni clima dinámico. Los bloques con entidad (por ejemplo, cofres), líquidos, bloques irrompibles y límites del mundo quedan excluidos de la recogida.

## Arquitectura

- `ExtremeDisasters`: punto de entrada, registros y configuración Forge.
- `DisasterConfig`: ajustes del servidor.
- `DisasterCommands`: comandos `/disaster`, autocompletado de literales y permisos administrativos.
- `ModEntities`: registro de las entidades de tornado y escombros.
- `TornadoEntity`: movimiento, fases de aparición/disipación, fuerzas de viento y recogida controlada.
- `TornadoDebrisEntity`: estado de bloque sincronizado, órbita, lanzamiento, impacto y colocación segura.
- `ClientModEvents`, `TornadoRenderer` y `TornadoDebrisRenderer`: registro de renderizado y formas 3D del tornado/bloques.
- `src/main/resources/META-INF/mods.toml`: metadatos y compatibilidad declarada.

El servidor decide qué bloques se quitan, mueve entidades, aplica impactos y decide dónde vuelve cada bloque. Al recoger un bloque se guarda su estado, se comprueba que no haya una pared sólida entre el tornado y el bloque, se quita del mundo y solo entonces se crea el escombro; si no se puede crear la entidad, se restaura el bloque. El sistema limita los escombros por tornado y nunca recoge bloques con datos de entidad de bloque. Al aterrizar busca tres posiciones legales cercanas; si no encuentra una y permanece dentro del mundo, entrega el bloque como objeto.

## Crear el JAR con GitHub Actions

Este entorno no dispone de Java 17/Gradle ni de acceso de red para descargarlos, por eso no pude compilar aquí. El proyecto incluye `.github/workflows/build.yml`: al subirlo a un repositorio GitHub, la acción configura Java 17 y Gradle 8.1.1, compila con ForgeGradle y publica un artefacto descargable.

1. Descarga la carpeta `ExtremeDisasters` y crea un repositorio nuevo en GitHub.
2. Sube el contenido de esta carpeta al repositorio, conservando la carpeta oculta `.github/workflows`.
3. Abre la pestaña **Actions** y espera a que termine **Build Forge mod** en verde.
4. Abre esa ejecución y descarga el artefacto **ExtremeDisasters-1.0.0**. Dentro estará `ExtremeDisasters-1.0.0.jar`.
5. Instala Forge 1.20.1 (47.x), copia el `.jar` en `.minecraft/mods/` y abre el juego con el perfil Forge.

En GitHub puedes subir los archivos desde **Add file → Upload files**; asegúrate de incluir el flujo de trabajo `.github/workflows/build.yml`. También puedes abrir la carpeta con GitHub Desktop y publicarla como repositorio.

## Comandos

- `/disaster help`
- `/disaster info`
- `/disaster tornado spawn normal`
- `/disaster tornado spawn normal intensity 2`
- `/disaster tornado list`
- `/disaster tornado clear`

Crear y borrar tornados requiere nivel de permiso 2. `clear` y `list` se aplican a la dimensión actual.

## Configuración

Después de iniciar una vez, los ajustes aparecen en `serverconfig/extremedis-server.toml` del mundo. Permiten desactivar el mod o la recogida de bloques y ajustar radio, duración, límite de escombros, arrastre y daño por impacto.

## Compatibilidad

- Minecraft: 1.20.1
- Forge: 47.x (proyecto configurado para 47.4.10)
- Java: 17
- Dependencias adicionales: ninguna

El `.jar` solo debe considerarse verificado después de que GitHub Actions termine correctamente y Forge lo cargue en el juego.
