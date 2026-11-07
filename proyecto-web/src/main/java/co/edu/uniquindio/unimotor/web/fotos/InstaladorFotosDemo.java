package co.edu.uniquindio.unimotor.web.fotos;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Al iniciar la aplicación copia las fotos de demostración empaquetadas en
 * {@code /WEB-INF/fotos-demo/} a la carpeta de fotos configurada, sin
 * sobrescribir los archivos que ya existan.
 */
@WebListener
public class InstaladorFotosDemo implements ServletContextListener {

	private static final Logger LOG = Logger.getLogger(InstaladorFotosDemo.class.getName());

	/** Ruta dentro del WAR donde viven las fotos de demostración. */
	private static final String RUTA_DEMO = "/WEB-INF/fotos-demo/";

	/**
	 * Copia las fotos de demostración que falten. Cualquier error se registra
	 * como advertencia y no impide el arranque.
	 *
	 * @param evento evento de inicio del contexto
	 */
	@Override
	public void contextInitialized(ServletContextEvent evento) {
		try {
			ServletContext contexto = evento.getServletContext();
			Path carpeta = ConfiguracionFotos.getCarpeta();
			Set<String> recursos = contexto.getResourcePaths(RUTA_DEMO);
			int copiadas = 0;
			int omitidas = 0;
			if (recursos != null) {
				for (String recurso : recursos) {
					if (recurso.endsWith("/")) {
						continue;
					}
					String nombre = recurso.substring(recurso.lastIndexOf('/') + 1);
					Path destino = ConfiguracionFotos.resolver(nombre);
					if (destino == null && !"CREDITOS.md".equals(nombre)) {
						continue;
					}
					if (destino == null) {
						destino = carpeta.resolve(nombre);
					}
					if (Files.exists(destino)) {
						omitidas++;
						continue;
					}
					try (InputStream entrada = contexto.getResourceAsStream(recurso)) {
						if (entrada == null) {
							continue;
						}
						Files.copy(entrada, destino);
						copiadas++;
					} catch (IOException e) {
						LOG.log(Level.WARNING, "No se pudo copiar la foto de demostración " + nombre, e);
					}
				}
			}
			LOG.log(Level.INFO, "Carpeta de fotos: {0}. Fotos de demostración copiadas: {1}, omitidas: {2}",
					new Object[] { carpeta, copiadas, omitidas });
		} catch (Exception e) {
			LOG.log(Level.WARNING, "No se pudieron instalar las fotos de demostración", e);
		}
	}

	/**
	 * No realiza ninguna acción al detener la aplicación.
	 *
	 * @param evento evento de destrucción del contexto
	 */
	@Override
	public void contextDestroyed(ServletContextEvent evento) {
		// Nada que liberar
	}
}
