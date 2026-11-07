package co.edu.uniquindio.unimotor.web.config;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.annotation.FacesConfig;

/**
 * Activa JSF 2.3 con CDI en la aplicación web de uniMotor.
 * <p>
 * Con {@code @FacesConfig(version = JSF_2_3)} el contenedor habilita la
 * integración completa de JSF con CDI: el lenguaje de expresiones (EL)
 * resuelve directamente los beans CDI anotados con {@code @Named}, y es posible
 * inyectar con {@code @Inject} en convertidores, validadores y otros
 * artefactos de JSF.
 * </p>
 */
@FacesConfig(version = FacesConfig.Version.JSF_2_3)
@ApplicationScoped
public class ConfiguracionJsf {
}
