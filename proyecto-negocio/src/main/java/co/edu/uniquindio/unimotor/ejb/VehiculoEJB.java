package co.edu.uniquindio.unimotor.ejb;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import co.edu.uniquindio.unimotor.entidades.Vehiculo;

/**
 * EJB con las operaciones sobre los vehículos.
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

}
