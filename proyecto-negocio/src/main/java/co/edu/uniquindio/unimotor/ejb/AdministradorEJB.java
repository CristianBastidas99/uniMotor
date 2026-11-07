package co.edu.uniquindio.unimotor.ejb;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import co.edu.uniquindio.unimotor.dto.EstadisticaDTO;
import co.edu.uniquindio.unimotor.dto.PuntoDTO;
import co.edu.uniquindio.unimotor.entidades.Administrador;
import co.edu.uniquindio.unimotor.entidades.Calificacion;
import co.edu.uniquindio.unimotor.entidades.Caracteristica;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Pregunta;
import co.edu.uniquindio.unimotor.entidades.Usuario;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.entidades.Venta;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.EnvioCorreoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * EJB con las operaciones de administración expuestas a la aplicación de escritorio:
 * autenticación del administrador, recuperación de contraseña y estadísticas para los gráficos.
 */
@Stateless
public class AdministradorEJB implements AdministradorEJBRemote {

	/**
	 * Máximo de barras de los gráficos que pueden crecer sin límite con los datos
	 * (preguntas por vehículo y calificación por vendedor): se muestran los primeros.
	 */
	private static final int MAXIMO_BARRAS = 15;

	private static final String MENSAJE_CREDENCIALES = "El email o la contraseña son incorrectos.";

	@PersistenceContext
	private EntityManager entityManager;

	@EJB
	private ClienteEJB clienteEJB;

	/**
	 * Crea el EJB de administración.
	 */
	public AdministradorEJB() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Administrador autenticarAdministrador(String email, String password) throws AutenticacionException {
		if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
			throw new AutenticacionException(MENSAJE_CREDENCIALES);
		}
		TypedQuery<Usuario> query = entityManager.createNamedQuery(Usuario.AUTENTIFICAR_USUARIO, Usuario.class);
		query.setParameter("email", email.trim());
		query.setParameter("password", password);
		List<Usuario> usuarios = query.getResultList();
		if (usuarios.isEmpty() || !(usuarios.get(0) instanceof Administrador)) {
			throw new AutenticacionException(MENSAJE_CREDENCIALES);
		}
		return (Administrador) usuarios.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recuperarPasswordAdministrador(String email)
			throws InformacionInvalidaException, ElementoNoEncontradoException, EnvioCorreoException {
		if (email == null || email.trim().isEmpty()) {
			throw new InformacionInvalidaException("El email es obligatorio.");
		}
		TypedQuery<Usuario> query = entityManager.createNamedQuery(Usuario.AUTENTIFICAR_EMAIL, Usuario.class);
		query.setParameter("email", email.trim());
		List<Usuario> usuarios = query.getResultList();
		if (usuarios.isEmpty() || !(usuarios.get(0) instanceof Administrador)) {
			throw new ElementoNoEncontradoException("No existe un administrador con el email " + email + ".");
		}
		clienteEJB.recuperarPassword(email.trim());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorTipo() {
		return consultar(Vehiculo.ESTADISTICA_POR_TIPO, EstadisticaDTO.class, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorCondicion() {
		return consultar(Vehiculo.ESTADISTICA_POR_CONDICION, EstadisticaDTO.class, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorTransmision() {
		return consultar(Vehiculo.ESTADISTICA_POR_TRANSMISION, EstadisticaDTO.class, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorCombustible() {
		return consultar(Vehiculo.ESTADISTICA_POR_COMBUSTIBLE, EstadisticaDTO.class, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorMarca() {
		return consultar(Vehiculo.ESTADISTICA_POR_MARCA, EstadisticaDTO.class, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> precioPromedioPorMarca() {
		return consultar(Vehiculo.ESTADISTICA_PRECIO_PROMEDIO_POR_MARCA, EstadisticaDTO.class, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorCiudad() {
		return consultar(Vehiculo.ESTADISTICA_POR_CIUDAD, EstadisticaDTO.class, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> vehiculosPorAnio() {
		return consultar(Vehiculo.ESTADISTICA_POR_ANIO, EstadisticaDTO.class, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> topVehiculosBuscados() {
		return consultar(Vehiculo.ESTADISTICA_TOP_BUSCADOS, EstadisticaDTO.class, 10);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> clientesPorCiudad() {
		return consultar(Cliente.ESTADISTICA_CLIENTES_POR_CIUDAD, EstadisticaDTO.class, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> preguntasPorVehiculo() {
		return consultar(Pregunta.ESTADISTICA_PREGUNTAS_POR_VEHICULO, EstadisticaDTO.class, MAXIMO_BARRAS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> caracteristicasFrecuentes() {
		return consultar(Caracteristica.ESTADISTICA_CARACTERISTICAS_FRECUENTES, EstadisticaDTO.class, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> ventasPorMes() {
		return consultar(Venta.ESTADISTICA_VENTAS_POR_MES, EstadisticaDTO.class, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> promedioCalificacionPorVendedor() {
		return consultar(Calificacion.ESTADISTICA_PROMEDIO_POR_VENDEDOR, EstadisticaDTO.class, MAXIMO_BARRAS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EstadisticaDTO> combustiblePorTipo() {
		return consultar(Vehiculo.ESTADISTICA_COMBUSTIBLE_POR_TIPO, EstadisticaDTO.class, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PuntoDTO> puntosPrecioKilometraje() {
		return consultar(Vehiculo.PUNTOS_PRECIO_KILOMETRAJE, PuntoDTO.class, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PuntoDTO> puntosPrecioAnioBusquedas() {
		return consultar(Vehiculo.PUNTOS_PRECIO_ANIO_BUSQUEDAS, PuntoDTO.class, 0);
	}

	/**
	 * Ejecuta una consulta nombrada de estadísticas y devuelve una lista nueva, serializable.
	 *
	 * @param nombre nombre de la consulta nombrada
	 * @param tipo clase del DTO que devuelve la consulta
	 * @param maximo número máximo de resultados, o cero para no limitar
	 * @param <T> tipo del DTO
	 * @return lista nueva con los resultados
	 */
	private <T> List<T> consultar(String nombre, Class<T> tipo, int maximo) {
		TypedQuery<T> query = entityManager.createNamedQuery(nombre, tipo);
		if (maximo > 0) {
			query.setMaxResults(maximo);
		}
		return new ArrayList<>(query.getResultList());
	}

}
