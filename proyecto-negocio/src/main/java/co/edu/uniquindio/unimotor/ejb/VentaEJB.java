package co.edu.uniquindio.unimotor.ejb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.entidades.Venta;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.EnvioCorreoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * EJB que administra el registro de ventas de vehículos y la consulta del historial de compras y ventas.
 */
@Stateless
@LocalBean
public class VentaEJB {

	private static final Logger LOGGER = Logger.getLogger(VentaEJB.class.getName());

	@PersistenceContext
	private EntityManager entityManager;

	@EJB
	private CorreoEJB correoEJB;

	/**
	 * Crea el EJB de ventas.
	 */
	public VentaEJB() {
		super();
	}

	/**
	 * Registra la venta de un vehículo a un comprador. El vehículo queda inactivo y se avisa por correo al
	 * comprador; si el correo falla, la venta se conserva y solo se registra una advertencia en el log.
	 *
	 * @param codigoVehiculo identificador del vehículo vendido
	 * @param emailVendedor correo del vendedor (debe ser el dueño del vehículo)
	 * @param emailComprador correo del cliente comprador
	 * @return la venta persistida
	 * @throws ElementoNoEncontradoException si no existe el vehículo o el comprador
	 * @throws InformacionInvalidaException si faltan datos, quien vende no es el dueño, el vehículo ya fue vendido
	 *             o el comprador es el mismo vendedor
	 */
	public Venta registrarVenta(Long codigoVehiculo, String emailVendedor, String emailComprador)
			throws ElementoNoEncontradoException, InformacionInvalidaException {
		if (codigoVehiculo == null) {
			throw new InformacionInvalidaException("El vehículo es obligatorio.");
		}
		if (emailVendedor == null || emailVendedor.trim().isEmpty()) {
			throw new InformacionInvalidaException("El email del vendedor es obligatorio.");
		}
		if (emailComprador == null || emailComprador.trim().isEmpty()) {
			throw new InformacionInvalidaException("El email del comprador es obligatorio.");
		}
		String vendedor = emailVendedor.trim();
		String comprador = emailComprador.trim();

		Vehiculo vehiculo = entityManager.find(Vehiculo.class, codigoVehiculo);
		if (vehiculo == null) {
			throw new ElementoNoEncontradoException("No existe el vehículo con código " + codigoVehiculo + ".");
		}
		Cliente dueno = vehiculo.getCodigo_dueno();
		if (dueno == null || !vendedor.equals(dueno.getEmail())) {
			throw new InformacionInvalidaException("Solo el dueño puede registrar la venta del vehículo.");
		}
		if (vehiculo.getCodigo_venta() != null) {
			throw new InformacionInvalidaException("El vehículo ya fue vendido.");
		}
		Long ventasExistentes = entityManager
				.createQuery("select count(v) from Venta v where v.codigo_vehiculo.codigo_vehiculo = :codigo", Long.class)
				.setParameter("codigo", codigoVehiculo).getSingleResult();
		if (ventasExistentes != null && ventasExistentes > 0) {
			throw new InformacionInvalidaException("El vehículo ya fue vendido.");
		}
		List<Cliente> clientes = entityManager.createNamedQuery(Cliente.FIND_BY_EMAIL, Cliente.class)
				.setParameter("email", comprador).getResultList();
		if (clientes.isEmpty()) {
			throw new ElementoNoEncontradoException("No existe un cliente con el email " + comprador + ".");
		}
		Cliente clienteComprador = clientes.get(0);
		if (vendedor.equals(clienteComprador.getEmail()) || dueno.equals(clienteComprador)) {
			throw new InformacionInvalidaException("El comprador debe ser distinto del vendedor.");
		}

		Venta venta = new Venta(new Date(), vehiculo, clienteComprador);
		vehiculo.setCodigo_venta(venta);
		vehiculo.setActivo(false);
		if (clienteComprador.getMis_compras() == null) {
			clienteComprador.setMis_compras(new ArrayList<Venta>());
		}
		clienteComprador.getMis_compras().add(venta);
		entityManager.persist(venta);
		entityManager.flush();

		try {
			correoEJB.enviarEmail(PlantillasCorreo.asuntoVentaRegistrada(venta), PlantillasCorreo.ventaRegistrada(venta),
					clienteComprador.getEmail());
		} catch (EnvioCorreoException | InformacionInvalidaException e) {
			LOGGER.log(Level.WARNING, "La venta " + venta.getCodigo_venta()
					+ " se guardó, pero no se pudo avisar por correo al comprador: " + e.getMessage(), e);
		}
		return venta;
	}

	/**
	 * Lista las compras de un cliente, de la más reciente a la más antigua.
	 *
	 * @param emailComprador correo del comprador
	 * @return lista de ventas (vacía si el email no existe)
	 */
	public List<Venta> listarCompras(String emailComprador) {
		return entityManager.createNamedQuery(Venta.VENTAS_POR_COMPRADOR, Venta.class)
				.setParameter("email", emailComprador == null ? null : emailComprador.trim()).getResultList();
	}

	/**
	 * Lista las ventas realizadas por un vendedor, de la más reciente a la más antigua.
	 *
	 * @param emailVendedor correo del vendedor
	 * @return lista de ventas (vacía si el email no existe)
	 */
	public List<Venta> listarVentas(String emailVendedor) {
		return entityManager.createNamedQuery(Venta.VENTAS_POR_VENDEDOR, Venta.class)
				.setParameter("email", emailVendedor == null ? null : emailVendedor.trim()).getResultList();
	}

	/**
	 * Busca una venta por su identificador.
	 *
	 * @param codigoVenta identificador de la venta
	 * @return la venta encontrada
	 * @throws ElementoNoEncontradoException si el código es nulo o no existe la venta
	 */
	public Venta buscarVenta(Long codigoVenta) throws ElementoNoEncontradoException {
		Venta venta = codigoVenta == null ? null : entityManager.find(Venta.class, codigoVenta);
		if (venta == null) {
			throw new ElementoNoEncontradoException("No existe la venta con código " + codigoVenta + ".");
		}
		return venta;
	}

}
