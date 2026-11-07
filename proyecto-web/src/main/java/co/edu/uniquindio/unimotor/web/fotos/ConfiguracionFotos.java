package co.edu.uniquindio.unimotor.web.fotos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Configuración de la carpeta donde se guardan las fotos de los vehículos.
 * <p>
 * La carpeta se resuelve en este orden: propiedad del sistema
 * {@code unimotor.fotos.carpeta}, variable de entorno {@code UNIMOTOR_FOTOS} y,
 * por defecto, {@code ${user.home}/unimotor/fotos}. Es una clase utilitaria
 * sin estado de CDI, por lo que se puede usar desde listeners y servlets sin
 * depender del orden de inicio.
 * </p>
 */
public final class ConfiguracionFotos {

	/** Propiedad del sistema JVM que define la carpeta de fotos. */
	public static final String PROPIEDAD_CARPETA = "unimotor.fotos.carpeta";

	/** Variable de entorno que define la carpeta de fotos. */
	public static final String VARIABLE_ENTORNO = "UNIMOTOR_FOTOS";

	/** URL base (relativa al contexto de la aplicación) con la que se sirven las fotos. */
	public static final String URL_BASE = "/fotos/";

	/** Patrón de nombres de archivo permitidos. */
	private static final Pattern NOMBRE_SEGURO = Pattern.compile("[A-Za-z0-9._-]+");

	/** Extensiones de imagen permitidas, en minúsculas. */
	private static final String[] EXTENSIONES = { "jpg", "jpeg", "png", "webp", "gif" };

	/** Carpeta ya resuelta (se calcula una sola vez). */
	private static volatile Path carpeta;

	private ConfiguracionFotos() {
	}

	/**
	 * Obtiene la carpeta de fotos, creándola si no existe.
	 *
	 * @return ruta absoluta y normalizada de la carpeta de fotos
	 * @throws IOException si la carpeta no se puede crear
	 */
	public static Path getCarpeta() throws IOException {
		Path resultado = carpeta;
		if (resultado == null) {
			synchronized (ConfiguracionFotos.class) {
				resultado = carpeta;
				if (resultado == null) {
					resultado = Paths.get(resolverTexto()).toAbsolutePath().normalize();
					Files.createDirectories(resultado);
					carpeta = resultado;
				}
			}
		}
		return resultado;
	}

	/**
	 * Determina el texto de la ruta según el orden de prioridad.
	 *
	 * @return ruta configurada como texto
	 */
	private static String resolverTexto() {
		String valor = System.getProperty(PROPIEDAD_CARPETA);
		if (valor == null || valor.trim().isEmpty()) {
			valor = System.getenv(VARIABLE_ENTORNO);
		}
		if (valor == null || valor.trim().isEmpty()) {
			valor = Paths.get(System.getProperty("user.home"), "unimotor", "fotos").toString();
		}
		return valor.trim();
	}

	/**
	 * Indica si un nombre de archivo es válido: solo {@code [A-Za-z0-9._-]},
	 * sin {@code ..} y con una extensión de imagen permitida.
	 *
	 * @param nombre nombre a validar
	 * @return {@code true} si el nombre es seguro
	 */
	public static boolean esNombreValido(String nombre) {
		if (nombre == null || nombre.isEmpty() || nombre.length() > 200) {
			return false;
		}
		if (!NOMBRE_SEGURO.matcher(nombre).matches() || nombre.contains("..") || nombre.startsWith(".")) {
			return false;
		}
		int punto = nombre.lastIndexOf('.');
		if (punto < 1 || punto == nombre.length() - 1) {
			return false;
		}
		String extension = nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
		for (String permitida : EXTENSIONES) {
			if (permitida.equals(extension)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Resuelve de forma segura un nombre de foto dentro de la carpeta configurada.
	 *
	 * @param nombre nombre del archivo
	 * @return ruta del archivo (que puede no existir) dentro de la carpeta, o
	 *         {@code null} si el nombre es inválido o intenta salir de la carpeta
	 * @throws IOException si la carpeta no se puede obtener
	 */
	public static Path resolver(String nombre) throws IOException {
		if (!esNombreValido(nombre)) {
			return null;
		}
		Path base = getCarpeta();
		Path ruta = base.resolve(nombre).normalize();
		if (!ruta.startsWith(base) || !base.equals(ruta.getParent())) {
			return null;
		}
		return ruta;
	}

	/**
	 * Genera un nombre único para una foto subida: un UUID más la extensión en minúsculas.
	 *
	 * @param nombreOriginal nombre original del archivo subido
	 * @return nombre único, con la forma {@code UUID.extension}
	 * @throws IllegalArgumentException si la extensión no es una imagen permitida
	 */
	public static String generarNombreUnico(String nombreOriginal) {
		String extension = null;
		if (nombreOriginal != null) {
			int punto = nombreOriginal.lastIndexOf('.');
			if (punto >= 0 && punto < nombreOriginal.length() - 1) {
				extension = nombreOriginal.substring(punto + 1).toLowerCase(Locale.ROOT);
			}
		}
		if (extension != null) {
			for (String permitida : EXTENSIONES) {
				if (permitida.equals(extension)) {
					return UUID.randomUUID().toString() + "." + extension;
				}
			}
		}
		throw new IllegalArgumentException("Extensión de imagen no permitida: " + nombreOriginal);
	}
}
