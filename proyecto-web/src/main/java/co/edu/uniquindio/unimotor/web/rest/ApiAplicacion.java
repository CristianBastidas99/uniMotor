package co.edu.uniquindio.unimotor.web.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Aplicación JAX-RS de la API REST pública y de solo lectura de uniMotor, publicada
 * bajo la ruta {@code /api}. Los recursos y proveedores anotados se descubren solos.
 */
@ApplicationPath("/api")
public class ApiAplicacion extends Application {
}
