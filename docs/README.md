# Documentación de uniMotor

## Diagrama entidad-relación

`diagrama-er.puml` (fuente PlantUML), `diagrama-er.png` y `diagrama-er.svg` muestran el esquema
real de la base `unimotor` (MySQL 8), generado por EclipseLink a partir de las entidades JPA de
`proyecto-persistencia`: 14 tablas con todas sus columnas, PK, FK, UNIQUE y relaciones.
Incluye la herencia JOINED USUARIO → ADMINISTRADOR / CLIENTE (columna `DTYPE`).

### Leyenda

| Color | Significado |
|---|---|
| Blanco | Entidad normal |
| Amarillo (`<<EC>>`) | Tabla generada por `@ElementCollection` (`foto_Vehiculo`, `telefono_usuario`) |
| Azul (`<<MM>>`) | Tabla intermedia de `@ManyToMany` (`caracteristica_vehiculo`, `favorito`) |
| Verde (`<<EXTRA>>`) | Funcionalidades extra: `VENTA` (extra 1) y `CALIFICACION` (extra 2) |

`*` junto a una columna indica NOT NULL.

### Regenerar

Requiere Java 8+ (no necesita Graphviz; usa el motor Smetana). Desde la raíz del repo:

```
mvn -q dependency:get -Dartifact=net.sourceforge.plantuml:plantuml:1.2023.13
java -jar ~/.m2/repository/net/sourceforge/plantuml/plantuml/1.2023.13/plantuml-1.2023.13.jar -charset UTF-8 -tpng -tsvg docs/diagrama-er.puml
```

### Origen

El esquema se consultó con `information_schema` (COLUMNS, KEY_COLUMN_USAGE, TABLE_CONSTRAINTS) sobre
la base creada por EclipseLink y se cruzó con las anotaciones de las entidades.
