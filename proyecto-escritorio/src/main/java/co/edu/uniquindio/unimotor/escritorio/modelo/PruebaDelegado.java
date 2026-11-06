package co.edu.uniquindio.unimotor.escritorio.modelo;

import java.util.List;

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
 * Delegado de negocio de la aplicacion de escritorio: oculta el lookup JNDI del
 * EJB remoto {@link AdministradorEJBRemote} y le delega todas las operaciones.
 * Se usa como singleton mediante {@link #pruebaDelegado}.
 */
public class PruebaDelegado implements AdministradorEJBRemote {

	/** Instancia unica del delegado. */
	public static PruebaDelegado pruebaDelegado = instancia();

	/** Referencia remota al EJB de administrador. */
	private AdministradorEJBRemote administradorEJB;

	/**
	 * Crea el delegado y busca el EJB remoto por su nombre JNDI.
	 */
	private PruebaDelegado() {
		try {
			administradorEJB = (AdministradorEJBRemote) new InitialContext().lookup(AdministradorEJBRemote.JNDI);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Obtiene la instancia unica del delegado, creandola si no existe.
	 *
	 * @return la instancia unica de {@code PruebaDelegado}
	 */
	private static PruebaDelegado instancia() {
		if (pruebaDelegado == null) {
			pruebaDelegado = new PruebaDelegado();
		}
		return pruebaDelegado;
	}

	/**
	 * Autentica a un administrador delegando en el EJB remoto.
	 *
	 * @param email    correo electronico del administrador
	 * @param password contrasena del administrador
	 * @return el administrador autenticado
	 * @throws AutenticacionException si las credenciales no son validas o el usuario no es administrador
	 * @see AdministradorEJBRemote#autenticarAdministrador(String, String)
	 */
	@Override
	public Administrador autenticarAdministrador(String email, String password) throws AutenticacionException {
		return administradorEJB.autenticarAdministrador(email, password);
	}

	/**
	 * Recupera la contrasena de un administrador delegando en el EJB remoto.
	 *
	 * @param email correo electronico del administrador
	 * @throws InformacionInvalidaException   si el correo es nulo o esta vacio
	 * @throws ElementoNoEncontradoException si no existe un administrador con ese correo
	 * @throws EnvioCorreoException           si no se puede enviar el correo con la contrasena temporal
	 * @see AdministradorEJBRemote#recuperarPasswordAdministrador(String)
	 */
	@Override
	public void recuperarPasswordAdministrador(String email)
			throws InformacionInvalidaException, ElementoNoEncontradoException, EnvioCorreoException {
		administradorEJB.recuperarPasswordAdministrador(email);
	}

	/**
	 * Obtiene la cantidad de vehiculos por tipo.
	 *
	 * @return lista de estadisticas con la etiqueta y el valor de cada tipo
	 * @see AdministradorEJBRemote#vehiculosPorTipo()
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorTipo() {
		return administradorEJB.vehiculosPorTipo();
	}

	/**
	 * Obtiene la cantidad de vehiculos por condicion (nuevo o usado).
	 *
	 * @return lista de estadisticas con la etiqueta y el valor de cada condicion
	 * @see AdministradorEJBRemote#vehiculosPorCondicion()
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorCondicion() {
		return administradorEJB.vehiculosPorCondicion();
	}

	/**
	 * Obtiene la cantidad de vehiculos por tipo de transmision.
	 *
	 * @return lista de estadisticas con la etiqueta y el valor de cada transmision
	 * @see AdministradorEJBRemote#vehiculosPorTransmision()
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorTransmision() {
		return administradorEJB.vehiculosPorTransmision();
	}

	/**
	 * Obtiene la cantidad de vehiculos por tipo de combustible.
	 *
	 * @return lista de estadisticas con la etiqueta y el valor de cada combustible
	 * @see AdministradorEJBRemote#vehiculosPorCombustible()
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorCombustible() {
		return administradorEJB.vehiculosPorCombustible();
	}

	/**
	 * Obtiene la cantidad de vehiculos por marca.
	 *
	 * @return lista de estadisticas con la etiqueta y el valor de cada marca
	 * @see AdministradorEJBRemote#vehiculosPorMarca()
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorMarca() {
		return administradorEJB.vehiculosPorMarca();
	}

	/**
	 * Obtiene el precio promedio de los vehiculos por marca.
	 *
	 * @return lista de estadisticas con la etiqueta y el precio promedio de cada marca
	 * @see AdministradorEJBRemote#precioPromedioPorMarca()
	 */
	@Override
	public List<EstadisticaDTO> precioPromedioPorMarca() {
		return administradorEJB.precioPromedioPorMarca();
	}

	/**
	 * Obtiene la cantidad de vehiculos por ciudad.
	 *
	 * @return lista de estadisticas con la etiqueta y el valor de cada ciudad
	 * @see AdministradorEJBRemote#vehiculosPorCiudad()
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorCiudad() {
		return administradorEJB.vehiculosPorCiudad();
	}

	/**
	 * Obtiene la cantidad de vehiculos por anio de fabricacion.
	 *
	 * @return lista de estadisticas con la etiqueta y el valor de cada anio
	 * @see AdministradorEJBRemote#vehiculosPorAnio()
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorAnio() {
		return administradorEJB.vehiculosPorAnio();
	}

	/**
	 * Obtiene los diez vehiculos mas buscados.
	 *
	 * @return lista de estadisticas con la etiqueta y el numero de busquedas de cada vehiculo
	 * @see AdministradorEJBRemote#topVehiculosBuscados()
	 */
	@Override
	public List<EstadisticaDTO> topVehiculosBuscados() {
		return administradorEJB.topVehiculosBuscados();
	}

	/**
	 * Obtiene la cantidad de clientes por ciudad.
	 *
	 * @return lista de estadisticas con la etiqueta y el valor de cada ciudad
	 * @see AdministradorEJBRemote#clientesPorCiudad()
	 */
	@Override
	public List<EstadisticaDTO> clientesPorCiudad() {
		return administradorEJB.clientesPorCiudad();
	}

	/**
	 * Obtiene la cantidad de preguntas realizadas por vehiculo.
	 *
	 * @return lista de estadisticas con la etiqueta y el valor de cada vehiculo
	 * @see AdministradorEJBRemote#preguntasPorVehiculo()
	 */
	@Override
	public List<EstadisticaDTO> preguntasPorVehiculo() {
		return administradorEJB.preguntasPorVehiculo();
	}

	/**
	 * Obtiene las caracteristicas mas frecuentes en los vehiculos.
	 *
	 * @return lista de estadisticas con la etiqueta y la frecuencia de cada caracteristica
	 * @see AdministradorEJBRemote#caracteristicasFrecuentes()
	 */
	@Override
	public List<EstadisticaDTO> caracteristicasFrecuentes() {
		return administradorEJB.caracteristicasFrecuentes();
	}

	/**
	 * Obtiene la cantidad de ventas realizadas por mes.
	 *
	 * @return lista de estadisticas con la etiqueta y el valor de cada mes
	 * @see AdministradorEJBRemote#ventasPorMes()
	 */
	@Override
	public List<EstadisticaDTO> ventasPorMes() {
		return administradorEJB.ventasPorMes();
	}

	/**
	 * Obtiene el promedio de calificacion de cada vendedor.
	 *
	 * @return lista de estadisticas con la etiqueta y el promedio de cada vendedor
	 * @see AdministradorEJBRemote#promedioCalificacionPorVendedor()
	 */
	@Override
	public List<EstadisticaDTO> promedioCalificacionPorVendedor() {
		return administradorEJB.promedioCalificacionPorVendedor();
	}

	/**
	 * Obtiene la cantidad de vehiculos por tipo y combustible, para barras apiladas.
	 *
	 * @return lista de estadisticas con el tipo como etiqueta, el combustible como serie y la cantidad como valor
	 * @see AdministradorEJBRemote#combustiblePorTipo()
	 */
	@Override
	public List<EstadisticaDTO> combustiblePorTipo() {
		return administradorEJB.combustiblePorTipo();
	}

	/**
	 * Obtiene los puntos de dispersion de precio contra kilometraje.
	 *
	 * @return lista de puntos con el kilometraje y el precio de cada vehiculo
	 * @see AdministradorEJBRemote#puntosPrecioKilometraje()
	 */
	@Override
	public List<PuntoDTO> puntosPrecioKilometraje() {
		return administradorEJB.puntosPrecioKilometraje();
	}

	/**
	 * Obtiene los puntos de burbuja de precio, anio y numero de busquedas.
	 *
	 * @return lista de puntos con el anio, el precio y el tamano segun las busquedas de cada vehiculo
	 * @see AdministradorEJBRemote#puntosPrecioAnioBusquedas()
	 */
	@Override
	public List<PuntoDTO> puntosPrecioAnioBusquedas() {
		return administradorEJB.puntosPrecioAnioBusquedas();
	}

}
