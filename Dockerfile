# ==========================================================
#  uniMotor - Imagen Docker para publicar la demo (Render u otro servicio)
#
#  Etapa 1: compila el EAR con Maven y JDK 8 (solo los modulos que lo forman).
#  Etapa 2: lo despliega en TomEE 8 Plume (Java EE 8 con EclipseLink y Mojarra).
#
#  La base de datos y el correo se configuran con variables de entorno:
#  ver despliegue/tomee/iniciar.sh y la seccion "Demo publicada" del README.
# ==========================================================

# ---------- Etapa 1: compilacion ----------
FROM maven:3.9-eclipse-temurin-8 AS compilacion
WORKDIR /fuente
COPY . .
RUN mvn -B -q -pl proyecto-ear -am package -DskipTests

# ---------- Etapa 2: servidor ----------
FROM tomee:8.0.16-jre8-Temurin-plume

ENV TZ=America/Bogota
WORKDIR /usr/local/tomee

# Driver de MySQL, sin las aplicaciones de ejemplo de TomEE, y carpeta apps/ para el EAR
RUN rm -rf webapps/* \
 && mkdir -p apps webapps/ROOT \
 && curl -fsSL -o lib/mysql-connector-j-8.0.33.jar \
      https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar

# EJB remotos por HTTP para el escritorio y lista blanca de serializacion
COPY despliegue/tomee/system.properties.extra /tmp/system.properties.extra
RUN sed -i '/^tomee.serialization.class.blacklist/d' conf/system.properties \
 && cat /tmp/system.properties.extra >> conf/system.properties \
 && rm /tmp/system.properties.extra

# Modo Production de JSF, redireccion de / a /proyecto-web/ y script de arranque
COPY despliegue/tomee/context.xml conf/context.xml
COPY despliegue/tomee/ROOT/index.html webapps/ROOT/index.html
COPY despliegue/tomee/iniciar.sh /usr/local/bin/iniciar-unimotor.sh
RUN sed -i 's/\r$//' /usr/local/bin/iniciar-unimotor.sh && chmod +x /usr/local/bin/iniciar-unimotor.sh

COPY --from=compilacion /fuente/proyecto-ear/target/proyecto-ear-0.0.1-SNAPSHOT.ear apps/proyecto-ear.ear

EXPOSE 8080
CMD ["iniciar-unimotor.sh"]
