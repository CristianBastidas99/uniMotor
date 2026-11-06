package co.edu.uniquindio.unimotor.ejb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import co.edu.uniquindio.unimotor.entidades.Caracteristica;
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Condicion;
import co.edu.uniquindio.unimotor.entidades.Modelo;
import co.edu.uniquindio.unimotor.entidades.Pregunta;
import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.ElementoRepetidoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * EJB con las operaciones sobre los vehículos: publicación, modificación, baja, consulta y búsqueda.
 */
@Stateless
@LocalBean
public class VehiculoEJB {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Crea el EJB de vehículos.
	 */
	public VehiculoEJB() {
		super();
	}

	/**
	 * Resultado de la validación común de publicar y modificar: las entidades gestionadas ya resueltas.
	 */
	private static class DatosValidados {
		private String placa;
		private Modelo modelo;
		private Ciudad ciudad;
		private List<Caracteristica> caracteristicas;
	}

	/**
	 * Indica si ya existe un vehículo con la placa dada.
	 *
	 * @param placa placa a buscar
	 * @return {@code true} si existe un vehículo con esa placa, {@code false} en caso contrario
	 */
	public boolean existePlaca(String placa) {
		TypedQuery<Vehiculo> query = entityManager.createNamedQuery(Vehiculo.FIND_BY_PLACA, Vehiculo.class);
		query.setParameter("placa", placa);
		List<Vehiculo> vehiculos = query.getResultList();
		return !vehiculos.isEmpty();
	}

	/**
	 * Valida los datos comunes de publicar y modificar sin tocar ninguna entidad gestionada.
	 *
	 * @param vehiculo vehículo recibido
	 * @return placa normalizada y entidades gestionadas (modelo, ciudad y características)
	 * @throws ElementoNoEncontradoException si el modelo, la ciudad o alguna característica no existen
	 * @throws InformacionInvalidaException si falta algún dato obligatorio o es inválido
	 */
	private DatosValidados validarDatos(Vehiculo vehiculo) throws ElementoNoEncontradoException, InformacionInvalidaException {
		if (vehiculo == null) {
			throw new InformacionInvalidaException("El vehículo es obligatorio.");
		}
		if (vehiculo.getPlaca() == null || vehiculo.getPlaca().trim().isEmpty()) {
			throw new InformacionInvalidaException("La placa es obligatoria.");
		}
		if (vehiculo.getPrecio() == null || vehiculo.getPrecio() <= 0) {
			throw new InformacionInvalidaException("El precio debe ser mayor que cero.");
		}
		int anioMaximo = Calendar.getInstance().get(Calendar.YEAR) + 1;
		if (vehiculo.getYear() < 1900 || vehiculo.getYear() > anioMaximo) {
			throw new InformacionInvalidaException("El año debe estar entre 1900 y " + anioMaximo + ".");
		}
		if (vehiculo.getKilometros() < 0) {
			throw new InformacionInvalidaException("Los kilómetros no pueden ser negativos.");
		}
		if (vehiculo.getTipo_vehiculo() == null) {
			throw new InformacionInvalidaException("El tipo de vehículo es obligatorio.");
		}
		if (vehiculo.getTipo_combustible() == null) {
			throw new InformacionInvalidaException("El tipo de combustible es obligatorio.");
		}
		if (vehiculo.getTipo_trasmision() == null) {
			throw new InformacionInvalidaException("El tipo de transmisión es obligatorio.");
		}
		if (vehiculo.getCondicion() == null) {
			throw new InformacionInvalidaException("La condición es obligatoria.");
		}
		if (vehiculo.getCodigo_modelo() == null || vehiculo.getCodigo_modelo().getCodigo_modelo() == null) {
			throw new InformacionInvalidaException("El modelo es obligatorio.");
		}
		if (vehiculo.getCodigo_ciudad() == null || vehiculo.getCodigo_ciudad().getCodigo_ciudad() == null) {
			throw new InformacionInvalidaException("La ciudad es obligatoria.");
		}

		DatosValidados datos = new DatosValidados();
		datos.placa = vehiculo.getPlaca().trim().toUpperCase();
		datos.modelo = entityManager.find(Modelo.class, vehiculo.getCodigo_modelo().getCodigo_modelo());
		if (datos.modelo == null) {
			throw new ElementoNoEncontradoException("El modelo indicado no existe.");
		}
		datos.ciudad = entityManager.find(Ciudad.class, vehiculo.getCodigo_ciudad().getCodigo_ciudad());
		if (datos.ciudad == null) {
			throw new ElementoNoEncontradoException("La ciudad indicada no existe.");
		}
		datos.caracteristicas = new ArrayList<Caracteristica>();
		if (vehiculo.getCaracteristicas() != null) {
			for (Caracteristica c : vehiculo.getCaracteristicas()) {
				Caracteristica gestionada = null;
				if (c != null && c.getCodigo_caracteristica() != null) {
					gestionada = entityManager.find(Caracteristica.class, c.getCodigo_caracteristica());
				}
				if (gestionada == null) {
					throw new ElementoNoEncontradoException("Una de las características indicadas no existe.");
				}
				if (!datos.caracteristicas.contains(gestionada)) {
					datos.caracteristicas.add(gestionada);
				}
			}
		}
		return datos;
	}

	/**
	 * Publica un nuevo vehículo.
	 *
	 * @param vehiculo vehículo a publicar (con dueño, modelo, ciudad y características identificados por su id)
	 * @return el vehículo persistido
	 * @throws ElementoRepetidoException si la placa ya existe
	 * @throws ElementoNoEncontradoException si el dueño, el modelo, la ciudad o alguna característica no existen
	 * @throws InformacionInvalidaException si falta algún dato obligatorio o es inválido
	 */
	public Vehiculo publicarVehiculo(Vehiculo vehiculo)
			throws ElementoRepetidoException, ElementoNoEncontradoException, InformacionInvalidaException {
		DatosValidados datos = validarDatos(vehiculo);
		if (vehiculo.getCodigo_dueno() == null || vehiculo.getCodigo_dueno().getCodigo_usuario() == null) {
			throw new InformacionInvalidaException("El dueño es obligatorio.");
		}
		Cliente dueno = entityManager.find(Cliente.class, vehiculo.getCodigo_dueno().getCodigo_usuario());
		if (dueno == null) {
			throw new ElementoNoEncontradoException("El dueño indicado no existe.");
		}
		if (existePlaca(datos.placa)) {
			throw new ElementoRepetidoException("Ya existe un vehículo con la placa " + datos.placa + ".");
		}

		vehiculo.setPlaca(datos.placa);
		vehiculo.setCodigo_dueno(dueno);
		vehiculo.setCodigo_modelo(datos.modelo);
		vehiculo.setCodigo_ciudad(datos.ciudad);
		vehiculo.setCaracteristicas(datos.caracteristicas);
		vehiculo.setActivo(true);
		vehiculo.setNumero_busquedas(0);
		vehiculo.setCodigo_venta(null);
		entityManager.persist(vehiculo);

		for (Caracteristica c : datos.caracteristicas) {
			if (c.getVehiculos() == null) {
				c.setVehiculos(new ArrayList<Vehiculo>());
			}
			c.getVehiculos().add(vehiculo);
		}
		if (dueno.getVehiculos_venta() == null) {
			dueno.setVehiculos_venta(new ArrayList<Vehiculo>());
		}
		dueno.getVehiculos_venta().add(vehiculo);
		return vehiculo;
	}

	/**
	 * Obtiene un vehículo comprobando que pertenece al dueño indicado.
	 *
	 * @param codigoVehiculo id del vehículo
	 * @param emailDueno email del dueño esperado
	 * @param accion texto de la acción para el mensaje de error
	 * @return el vehículo gestionado
	 * @throws ElementoNoEncontradoException si el vehículo no existe
	 * @throws InformacionInvalidaException si el email no corresponde al dueño
	 */
	private Vehiculo obtenerDeDueno(Long codigoVehiculo, String emailDueno, String accion)
			throws ElementoNoEncontradoException, InformacionInvalidaException {
		if (codigoVehiculo == null) {
			throw new ElementoNoEncontradoException("El vehículo indicado no existe.");
		}
		Vehiculo gestionado = entityManager.find(Vehiculo.class, codigoVehiculo);
		if (gestionado == null) {
			throw new ElementoNoEncontradoException("El vehículo indicado no existe.");
		}
		String email = emailDueno == null ? null : emailDueno.trim();
		if (email == null || gestionado.getCodigo_dueno() == null || !email.equals(gestionado.getCodigo_dueno().getEmail())) {
			throw new InformacionInvalidaException("Solo el dueño puede " + accion + " el vehículo.");
		}
		return gestionado;
	}

	/**
	 * Modifica los datos editables de un vehículo (no cambia dueño, estado, búsquedas, fecha ni venta).
	 *
	 * @param vehiculo vehículo con los nuevos datos y el {@code codigo_vehiculo} del existente
	 * @param emailDueno email del dueño del vehículo
	 * @return el vehículo gestionado y actualizado
	 * @throws ElementoNoEncontradoException si el vehículo, el modelo, la ciudad o alguna característica no existen
	 * @throws ElementoRepetidoException si la nueva placa ya pertenece a otro vehículo
	 * @throws InformacionInvalidaException si quien modifica no es el dueño o hay datos inválidos
	 */
	public Vehiculo modificarVehiculo(Vehiculo vehiculo, String emailDueno)
			throws ElementoNoEncontradoException, ElementoRepetidoException, InformacionInvalidaException {
		if (vehiculo == null || vehiculo.getCodigo_vehiculo() == null) {
			throw new ElementoNoEncontradoException("El vehículo indicado no existe.");
		}
		Vehiculo gestionado = obtenerDeDueno(vehiculo.getCodigo_vehiculo(), emailDueno, "modificar");
		DatosValidados datos = validarDatos(vehiculo);
		if (!datos.placa.equals(gestionado.getPlaca()) && existePlaca(datos.placa)) {
			throw new ElementoRepetidoException("Ya existe un vehículo con la placa " + datos.placa + ".");
		}

		gestionado.setDescripcion(vehiculo.getDescripcion());
		gestionado.setColor(vehiculo.getColor());
		gestionado.setPlaca(datos.placa);
		gestionado.setPrecio(vehiculo.getPrecio());
		gestionado.setYear(vehiculo.getYear());
		gestionado.setCilindraje(vehiculo.getCilindraje());
		gestionado.setNumero_puertas(vehiculo.getNumero_puertas());
		gestionado.setKilometros(vehiculo.getKilometros());
		gestionado.setCodigo_ciudad(datos.ciudad);
		gestionado.setCodigo_modelo(datos.modelo);
		gestionado.setTipo_vehiculo(vehiculo.getTipo_vehiculo());
		gestionado.setTipo_combustible(vehiculo.getTipo_combustible());
		gestionado.setTipo_trasmision(vehiculo.getTipo_trasmision());
		gestionado.setCondicion(vehiculo.getCondicion());
		if (gestionado.getUrl_foto() == null) {
			gestionado.setUrl_foto(new ArrayList<String>());
		}
		if (gestionado.getUrl_foto() != vehiculo.getUrl_foto()) {
			List<String> fotos = vehiculo.getUrl_foto() == null ? new ArrayList<String>()
					: new ArrayList<String>(vehiculo.getUrl_foto());
			gestionado.getUrl_foto().clear();
			gestionado.getUrl_foto().addAll(fotos);
		}

		if (gestionado.getCaracteristicas() == null) {
			gestionado.setCaracteristicas(new ArrayList<Caracteristica>());
		}
		for (Caracteristica anterior : new ArrayList<Caracteristica>(gestionado.getCaracteristicas())) {
			if (!datos.caracteristicas.contains(anterior)) {
				gestionado.getCaracteristicas().remove(anterior);
				if (anterior.getVehiculos() != null) {
					anterior.getVehiculos().remove(gestionado);
				}
			}
		}
		for (Caracteristica nueva : datos.caracteristicas) {
			if (!gestionado.getCaracteristicas().contains(nueva)) {
				gestionado.getCaracteristicas().add(nueva);
				if (nueva.getVehiculos() == null) {
					nueva.setVehiculos(new ArrayList<Vehiculo>());
				}
				nueva.getVehiculos().add(gestionado);
			}
		}
		return gestionado;
	}

	/**
	 * Marca un vehículo como inactivo.
	 *
	 * @param codigoVehiculo id del vehículo
	 * @param emailDueno email del dueño
	 * @throws ElementoNoEncontradoException si el vehículo no existe
	 * @throws InformacionInvalidaException si quien invalida no es el dueño
	 */
	public void invalidarVehiculo(Long codigoVehiculo, String emailDueno)
			throws ElementoNoEncontradoException, InformacionInvalidaException {
		Vehiculo gestionado = obtenerDeDueno(codigoVehiculo, emailDueno, "invalidar");
		gestionado.setActivo(false);
	}

	/**
	 * Elimina un vehículo y sus dependencias (preguntas, favoritos y características).
	 *
	 * @param codigoVehiculo id del vehículo
	 * @param emailDueno email del dueño
	 * @throws ElementoNoEncontradoException si el vehículo no existe
	 * @throws InformacionInvalidaException si quien elimina no es el dueño o el vehículo tiene una venta registrada
	 */
	public void eliminarVehiculo(Long codigoVehiculo, String emailDueno)
			throws ElementoNoEncontradoException, InformacionInvalidaException {
		Vehiculo gestionado = obtenerDeDueno(codigoVehiculo, emailDueno, "eliminar");
		if (gestionado.getCodigo_venta() != null) {
			throw new InformacionInvalidaException("El vehículo tiene una venta registrada; use invalidar.");
		}

		if (gestionado.getPreguntas() != null) {
			for (Pregunta p : new ArrayList<Pregunta>(gestionado.getPreguntas())) {
				if (p.getCliente() != null && p.getCliente().getPreguntas() != null) {
					p.getCliente().getPreguntas().remove(p);
				}
				entityManager.remove(p);
			}
			gestionado.getPreguntas().clear();
		}
		if (gestionado.getClientes() != null) {
			for (Cliente c : new ArrayList<Cliente>(gestionado.getClientes())) {
				if (c.getVehiculos_favoritos() != null) {
					c.getVehiculos_favoritos().remove(gestionado);
				}
			}
			gestionado.getClientes().clear();
		}
		if (gestionado.getCaracteristicas() != null) {
			for (Caracteristica c : new ArrayList<Caracteristica>(gestionado.getCaracteristicas())) {
				if (c.getVehiculos() != null) {
					c.getVehiculos().remove(gestionado);
				}
			}
			gestionado.getCaracteristicas().clear();
		}
		if (gestionado.getCodigo_dueno() != null && gestionado.getCodigo_dueno().getVehiculos_venta() != null) {
			gestionado.getCodigo_dueno().getVehiculos_venta().remove(gestionado);
		}
		// Primero se borran las filas hijas (preguntas, favorito, caracteristica_vehiculo) y luego el vehículo
		entityManager.flush();
		entityManager.remove(gestionado);
	}

	/**
	 * Busca un vehículo por su id sin sumar búsquedas.
	 *
	 * @param codigoVehiculo id del vehículo
	 * @return el vehículo
	 * @throws ElementoNoEncontradoException si no existe
	 */
	public Vehiculo buscarVehiculo(Long codigoVehiculo) throws ElementoNoEncontradoException {
		Vehiculo v = codigoVehiculo == null ? null : entityManager.find(Vehiculo.class, codigoVehiculo);
		if (v == null) {
			throw new ElementoNoEncontradoException("El vehículo indicado no existe.");
		}
		return v;
	}

	/**
	 * Busca un vehículo por su id y suma una visita a su contador de búsquedas.
	 *
	 * @param codigoVehiculo id del vehículo
	 * @return el vehículo con el contador actualizado
	 * @throws ElementoNoEncontradoException si no existe
	 */
	public Vehiculo registrarVisita(Long codigoVehiculo) throws ElementoNoEncontradoException {
		Vehiculo v = buscarVehiculo(codigoVehiculo);
		v.setNumero_busquedas(v.getNumero_busquedas() + 1);
		return v;
	}

	/**
	 * Lista todos los vehículos (activos e inactivos) de un cliente.
	 *
	 * @param email email del dueño
	 * @return lista de vehículos del cliente
	 */
	public List<Vehiculo> listarVehiculosDeCliente(String email) {
		TypedQuery<Vehiculo> query = entityManager.createNamedQuery(Vehiculo.VEHICULOS_POR_EMAIL_DUENO, Vehiculo.class);
		query.setParameter("email", email == null ? null : email.trim());
		return query.getResultList();
	}

	/**
	 * Lista los vehículos activos.
	 *
	 * @return lista de vehículos activos
	 */
	public List<Vehiculo> listarVehiculosActivos() {
		return entityManager.createNamedQuery(Vehiculo.VEHICULOS_ACTIVOS, Vehiculo.class).getResultList();
	}

	/**
	 * Busca vehículos activos con filtros opcionales y suma una búsqueda a cada vehículo devuelto.
	 *
	 * @param tipo tipo de vehículo o {@code null}
	 * @param marca marca o {@code null}/vacía para no filtrar
	 * @param precioMin precio mínimo o {@code null}
	 * @param precioMax precio máximo o {@code null}
	 * @param anioMin año mínimo o {@code null}
	 * @param anioMax año máximo o {@code null}
	 * @return vehículos que cumplen los filtros
	 * @throws InformacionInvalidaException si algún rango está invertido
	 */
	public List<Vehiculo> buscarVehiculos(TipoVehiculo tipo, String marca, Double precioMin, Double precioMax,
			Integer anioMin, Integer anioMax) throws InformacionInvalidaException {
		if (precioMin != null && precioMax != null && precioMin > precioMax) {
			throw new InformacionInvalidaException("El precio mínimo no puede ser mayor que el máximo.");
		}
		if (anioMin != null && anioMax != null && anioMin > anioMax) {
			throw new InformacionInvalidaException("El año mínimo no puede ser mayor que el máximo.");
		}
		String marcaFiltro = marca == null || marca.trim().isEmpty() ? null : marca.trim();
		TypedQuery<Vehiculo> query = entityManager.createNamedQuery(Vehiculo.BUSQUEDA_COMBINADA, Vehiculo.class);
		query.setParameter("tipo", tipo);
		query.setParameter("marca", marcaFiltro);
		query.setParameter("precioMin", precioMin);
		query.setParameter("precioMax", precioMax);
		query.setParameter("anioMin", anioMin);
		query.setParameter("anioMax", anioMax);
		List<Vehiculo> resultado = query.getResultList();
		for (Vehiculo v : resultado) {
			v.setNumero_busquedas(v.getNumero_busquedas() + 1);
		}
		return resultado;
	}

	/**
	 * Filtra una lista de vehículos dejando los que tienen todas las características indicadas.
	 *
	 * @param vehiculos vehículos a filtrar
	 * @param codigosCaracteristicas ids de características exigidas; {@code null} o vacía no filtra
	 * @return vehículos (en el mismo orden) que tienen todas las características
	 */
	public List<Vehiculo> filtrarPorCaracteristicas(List<Vehiculo> vehiculos, List<Long> codigosCaracteristicas) {
		if (codigosCaracteristicas == null || codigosCaracteristicas.isEmpty() || vehiculos == null) {
			return vehiculos;
		}
		List<Vehiculo> resultado = new ArrayList<Vehiculo>();
		for (Vehiculo v : vehiculos) {
			Vehiculo recargado = v == null || v.getCodigo_vehiculo() == null ? null
					: entityManager.find(Vehiculo.class, v.getCodigo_vehiculo());
			if (recargado == null || recargado.getCaracteristicas() == null) {
				continue;
			}
			Set<Long> ids = new HashSet<Long>();
			for (Caracteristica c : recargado.getCaracteristicas()) {
				ids.add(c.getCodigo_caracteristica());
			}
			if (ids.containsAll(codigosCaracteristicas)) {
				resultado.add(v);
			}
		}
		return resultado;
	}

	/**
	 * Obtiene los diez vehículos activos más buscados de una condición.
	 *
	 * @param condicion condición del vehículo (nuevo o usado)
	 * @return hasta diez vehículos ordenados por número de búsquedas
	 * @throws InformacionInvalidaException si la condición es nula
	 */
	public List<Vehiculo> top10MasBuscados(Condicion condicion) throws InformacionInvalidaException {
		if (condicion == null) {
			throw new InformacionInvalidaException("La condición es obligatoria.");
		}
		TypedQuery<Vehiculo> query = entityManager.createNamedQuery(Vehiculo.TOP_BUSCADOS, Vehiculo.class);
		query.setParameter("condicion", condicion);
		query.setMaxResults(10);
		return query.getResultList();
	}

}
