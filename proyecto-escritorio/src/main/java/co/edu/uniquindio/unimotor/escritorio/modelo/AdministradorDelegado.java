package co.edu.uniquindio.unimotor.escritorio.modelo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import co.edu.uniquindio.unimotor.dto.EstadisticaDTO;
import co.edu.uniquindio.unimotor.dto.PuntoDTO;
import co.edu.uniquindio.unimotor.ejb.AdministradorEJBRemote;
import co.edu.uniquindio.unimotor.entidades.Administrador;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.EnvioCorreoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * Delegado de negocio de la aplicación de escritorio: oculta la búsqueda JNDI del
 * EJB remoto {@link AdministradorEJBRemote} y le delega todas las operaciones.
 * La búsqueda se hace en el primer uso; si el servidor no responde se lanza
 * {@link ErrorConexionException} y se reintenta la búsqueda en la siguiente llamada.
 * <p>
 * El servidor se elige con la propiedad del sistema {@value #PROPIEDAD_SERVIDOR}:
 * <ul>
 * <li>una URL HTTP(S) de TomEE (por defecto {@value #SERVIDOR_POR_DEFECTO}); por ejemplo, para la demo
 * publicada: {@code -Dunimotor.servidor=https://<servicio>.onrender.com/tomee/ejb};</li>
 * <li>{@value #SERVIDOR_IIOP} para Payara por IIOP (puerto 3700, propiedades {@code org.omg.CORBA.*}).</li>
 * </ul>
 */
public class AdministradorDelegado implements AdministradorEJBRemote {

	/** Mensaje mostrado cuando no se puede hablar con el servidor. */
	private static final String MENSAJE_CONEXION = "No se pudo conectar con el servidor de uniMotor. Verifique que el servidor esté en ejecución.";

	/** Propiedad del sistema con la dirección del servidor. */
	public static final String PROPIEDAD_SERVIDOR = "unimotor.servidor";

	/** Servidor usado si no se indica otro: TomEE local con los EJB remotos por HTTP. */
	public static final String SERVIDOR_POR_DEFECTO = "http://localhost:8080/tomee/ejb";

	/** Valor de {@value #PROPIEDAD_SERVIDOR} para conectarse a Payara por IIOP. */
	public static final String SERVIDOR_IIOP = "iiop";

	/** Fábrica de contextos JNDI del cliente remoto de TomEE (OpenEJB). */
	private static final String FABRICA_TOMEE = "org.apache.openejb.client.RemoteInitialContextFactory";

	/** Tiempo máximo, en milisegundos, para comprobar que el servidor responde. */
	private static final int TIEMPO_ESPERA_MS = 3000;

	/** Instancia única del delegado. */
	private static AdministradorDelegado instancia;

	/** Referencia remota al EJB de administrador, nula hasta el primer uso. */
	private AdministradorEJBRemote administradorEJB;

	/**
	 * Operación remota que devuelve un resultado.
	 *
	 * @param <T> tipo del resultado
	 */
	@FunctionalInterface
	private interface Llamada<T> {

		/**
		 * Ejecuta la operación sobre el EJB.
		 *
		 * @param ejb referencia remota al EJB
		 * @return el resultado de la operación
		 * @throws Exception si la operación falla
		 */
		T ejecutar(AdministradorEJBRemote ejb) throws Exception;
	}

	/**
	 * Operación remota que no devuelve resultado.
	 */
	@FunctionalInterface
	private interface LlamadaVacia {

		/**
		 * Ejecuta la operación sobre el EJB.
		 *
		 * @param ejb referencia remota al EJB
		 * @throws Exception si la operación falla
		 */
		void ejecutar(AdministradorEJBRemote ejb) throws Exception;
	}

	/**
	 * Crea el delegado sin buscar todavía el EJB.
	 */
	private AdministradorDelegado() {
	}

	/**
	 * Obtiene la instancia única del delegado, creándola si no existe.
	 *
	 * @return la instancia única de {@code AdministradorDelegado}
	 */
	public static synchronized AdministradorDelegado getInstancia() {
		if (instancia == null) {
			instancia = new AdministradorDelegado();
		}
		return instancia;
	}

	/**
	 * Obtiene la referencia al EJB, buscándola por JNDI si aún no se tiene.
	 *
	 * @return la referencia remota al EJB
	 * @throws NamingException si la búsqueda JNDI falla
	 */
	private synchronized AdministradorEJBRemote obtenerEJB() throws NamingException {
		if (administradorEJB == null) {
			String servidor = getServidor();
			if (SERVIDOR_IIOP.equalsIgnoreCase(servidor)) {
				verificarServidor(System.getProperty("org.omg.CORBA.ORBInitialHost", "localhost"),
						Integer.parseInt(System.getProperty("org.omg.CORBA.ORBInitialPort", "3700")));
				administradorEJB = (AdministradorEJBRemote) new InitialContext().lookup(AdministradorEJBRemote.JNDI);
			} else {
				URI uri = URI.create(servidor);
				int puerto = uri.getPort() != -1 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
				verificarServidor(uri.getHost(), puerto);
				Properties propiedades = new Properties();
				propiedades.put(Context.INITIAL_CONTEXT_FACTORY, FABRICA_TOMEE);
				propiedades.put(Context.PROVIDER_URL, servidor);
				// El cliente de TomEE usa el nombre global sin el prefijo "java:"
				String nombre = AdministradorEJBRemote.JNDI.replaceFirst("^java:", "");
				administradorEJB = (AdministradorEJBRemote) new InitialContext(propiedades).lookup(nombre);
			}
		}
		return administradorEJB;
	}

	/**
	 * Devuelve la dirección del servidor configurada en {@value #PROPIEDAD_SERVIDOR}.
	 *
	 * @return la URL del servidor TomEE, o {@value #SERVIDOR_IIOP} para Payara
	 */
	public static String getServidor() {
		String servidor = System.getProperty(PROPIEDAD_SERVIDOR, SERVIDOR_POR_DEFECTO).trim();
		return servidor.isEmpty() ? SERVIDOR_POR_DEFECTO : servidor;
	}

	/**
	 * Comprueba rápidamente que el servidor acepte conexiones en el puerto indicado. Sin esta comprobación,
	 * con el servidor apagado la búsqueda JNDI por IIOP tarda cerca de 100 segundos en fallar.
	 *
	 * @param host nombre o dirección del servidor
	 * @param puerto puerto que debe aceptar conexiones
	 * @throws NamingException si el servidor no responde en el tiempo de espera
	 */
	private void verificarServidor(String host, int puerto) throws NamingException {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, puerto), TIEMPO_ESPERA_MS);
		} catch (IOException e) {
			NamingException error = new NamingException("El servidor " + host + ":" + puerto + " no responde");
			error.setRootCause(e);
			throw error;
		}
	}

	/**
	 * Descarta la referencia guardada para repetir la búsqueda en el siguiente uso.
	 */
	private synchronized void descartarEJB() {
		administradorEJB = null;
	}

	/**
	 * Ejecuta una operación remota traduciendo los fallos de conexión.
	 *
	 * @param <T> tipo del resultado
	 * @param llamada operación a ejecutar
	 * @return el resultado de la operación
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 * @throws AutenticacionException si las credenciales no son válidas
	 * @throws InformacionInvalidaException si la información es inválida
	 * @throws ElementoNoEncontradoException si el elemento no existe
	 * @throws EnvioCorreoException si falla el envío de correo
	 */
	private <T> T ejecutar(Llamada<T> llamada)
			throws AutenticacionException, InformacionInvalidaException, ElementoNoEncontradoException, EnvioCorreoException {
		try {
			return llamada.ejecutar(obtenerEJB());
		} catch (AutenticacionException | InformacionInvalidaException | ElementoNoEncontradoException
				| EnvioCorreoException e) {
			throw e;
		} catch (NamingException e) {
			descartarEJB();
			throw new ErrorConexionException(MENSAJE_CONEXION, e);
		} catch (RuntimeException e) {
			descartarEJB();
			throw new ErrorConexionException(MENSAJE_CONEXION, e);
		} catch (Exception e) {
			descartarEJB();
			throw new ErrorConexionException(MENSAJE_CONEXION, e);
		}
	}

	/**
	 * Ejecuta una consulta sin excepciones de negocio.
	 *
	 * @param <T> tipo del resultado
	 * @param llamada consulta a ejecutar
	 * @return el resultado de la consulta
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	private <T> T consultar(Llamada<T> llamada) {
		try {
			return ejecutar(llamada);
		} catch (AutenticacionException | InformacionInvalidaException | ElementoNoEncontradoException
				| EnvioCorreoException e) {
			throw new ErrorConexionException(MENSAJE_CONEXION, e);
		}
	}

	/**
	 * Autentica a un administrador delegando en el EJB remoto.
	 *
	 * @param email correo electrónico del administrador
	 * @param password contraseña del administrador
	 * @return el administrador autenticado
	 * @throws AutenticacionException si las credenciales no son válidas o el usuario no es administrador
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public Administrador autenticarAdministrador(String email, String password) throws AutenticacionException {
		try {
			return ejecutar(ejb -> ejb.autenticarAdministrador(email, password));
		} catch (InformacionInvalidaException | ElementoNoEncontradoException | EnvioCorreoException e) {
			throw new ErrorConexionException(MENSAJE_CONEXION, e);
		}
	}

	/**
	 * Recupera la contraseña de un administrador delegando en el EJB remoto.
	 *
	 * @param email correo electrónico del administrador
	 * @throws InformacionInvalidaException si el correo es nulo o está vacío
	 * @throws ElementoNoEncontradoException si no existe un administrador con ese correo
	 * @throws EnvioCorreoException si no se puede enviar el correo con la contraseña temporal
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public void recuperarPasswordAdministrador(String email)
			throws InformacionInvalidaException, ElementoNoEncontradoException, EnvioCorreoException {
		LlamadaVacia vacia = ejb -> ejb.recuperarPasswordAdministrador(email);
		try {
			ejecutar(ejb -> {
				vacia.ejecutar(ejb);
				return null;
			});
		} catch (AutenticacionException e) {
			throw new ErrorConexionException(MENSAJE_CONEXION, e);
		}
	}

	/**
	 * Obtiene la cantidad de vehículos por tipo.
	 *
	 * @return lista de estadísticas con la etiqueta y el valor de cada tipo
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorTipo() {
		return consultar(AdministradorEJBRemote::vehiculosPorTipo);
	}

	/**
	 * Obtiene la cantidad de vehículos por condición (nuevo o usado).
	 *
	 * @return lista de estadísticas con la etiqueta y el valor de cada condición
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorCondicion() {
		return consultar(AdministradorEJBRemote::vehiculosPorCondicion);
	}

	/**
	 * Obtiene la cantidad de vehículos por tipo de transmisión.
	 *
	 * @return lista de estadísticas con la etiqueta y el valor de cada transmisión
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorTransmision() {
		return consultar(AdministradorEJBRemote::vehiculosPorTransmision);
	}

	/**
	 * Obtiene la cantidad de vehículos por tipo de combustible.
	 *
	 * @return lista de estadísticas con la etiqueta y el valor de cada combustible
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorCombustible() {
		return consultar(AdministradorEJBRemote::vehiculosPorCombustible);
	}

	/**
	 * Obtiene la cantidad de vehículos por marca.
	 *
	 * @return lista de estadísticas con la etiqueta y el valor de cada marca
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorMarca() {
		return consultar(AdministradorEJBRemote::vehiculosPorMarca);
	}

	/**
	 * Obtiene el precio promedio de los vehículos por marca.
	 *
	 * @return lista de estadísticas con la etiqueta y el precio promedio de cada marca
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> precioPromedioPorMarca() {
		return consultar(AdministradorEJBRemote::precioPromedioPorMarca);
	}

	/**
	 * Obtiene la cantidad de vehículos por ciudad.
	 *
	 * @return lista de estadísticas con la etiqueta y el valor de cada ciudad
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorCiudad() {
		return consultar(AdministradorEJBRemote::vehiculosPorCiudad);
	}

	/**
	 * Obtiene la cantidad de vehículos por año del modelo.
	 *
	 * @return lista de estadísticas con la etiqueta y el valor de cada año
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorAnio() {
		return consultar(AdministradorEJBRemote::vehiculosPorAnio);
	}

	/**
	 * Obtiene los diez vehículos más buscados.
	 *
	 * @return lista de estadísticas con la etiqueta y el número de búsquedas de cada vehículo
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> topVehiculosBuscados() {
		return consultar(AdministradorEJBRemote::topVehiculosBuscados);
	}

	/**
	 * Obtiene la cantidad de clientes por ciudad.
	 *
	 * @return lista de estadísticas con la etiqueta y el valor de cada ciudad
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> clientesPorCiudad() {
		return consultar(AdministradorEJBRemote::clientesPorCiudad);
	}

	/**
	 * Obtiene la cantidad de preguntas realizadas por vehículo.
	 *
	 * @return lista de estadísticas con la etiqueta y el valor de cada vehículo
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> preguntasPorVehiculo() {
		return consultar(AdministradorEJBRemote::preguntasPorVehiculo);
	}

	/**
	 * Obtiene las características más frecuentes en los vehículos.
	 *
	 * @return lista de estadísticas con la etiqueta y la frecuencia de cada característica
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> caracteristicasFrecuentes() {
		return consultar(AdministradorEJBRemote::caracteristicasFrecuentes);
	}

	/**
	 * Obtiene la cantidad de ventas realizadas por mes.
	 *
	 * @return lista de estadísticas con la etiqueta y el valor de cada mes
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> ventasPorMes() {
		return consultar(AdministradorEJBRemote::ventasPorMes);
	}

	/**
	 * Obtiene el promedio de calificación de cada vendedor.
	 *
	 * @return lista de estadísticas con la etiqueta y el promedio de cada vendedor
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> promedioCalificacionPorVendedor() {
		return consultar(AdministradorEJBRemote::promedioCalificacionPorVendedor);
	}

	/**
	 * Obtiene la cantidad de vehículos por tipo y combustible, para barras apiladas.
	 *
	 * @return lista de estadísticas con el tipo como etiqueta, el combustible como serie y la cantidad como valor
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<EstadisticaDTO> combustiblePorTipo() {
		return consultar(AdministradorEJBRemote::combustiblePorTipo);
	}

	/**
	 * Obtiene los puntos de dispersión de precio contra kilometraje.
	 *
	 * @return lista de puntos con el kilometraje y el precio de cada vehículo
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<PuntoDTO> puntosPrecioKilometraje() {
		return consultar(AdministradorEJBRemote::puntosPrecioKilometraje);
	}

	/**
	 * Obtiene los puntos de burbuja de precio, año y número de búsquedas.
	 *
	 * @return lista de puntos con el año, el precio y el tamaño según las búsquedas de cada vehículo
	 * @throws ErrorConexionException si no se puede comunicar con el servidor
	 */
	@Override
	public List<PuntoDTO> puntosPrecioAnioBusquedas() {
		return consultar(AdministradorEJBRemote::puntosPrecioAnioBusquedas);
	}

}
