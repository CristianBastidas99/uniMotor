package co.edu.uniquindio.unimotor.ejb;

import java.util.Arrays;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import co.edu.uniquindio.unimotor.entidades.Caracteristica;
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Condicion;
import co.edu.uniquindio.unimotor.entidades.Modelo;
import co.edu.uniquindio.unimotor.entidades.TipoCombustible;
import co.edu.uniquindio.unimotor.entidades.TipoTrasmision;
import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;

/**
 * EJB de solo lectura que entrega los catálogos usados en los formularios y filtros de la aplicación.
 */
@Stateless
@LocalBean
public class CatalogoEJB {

	@PersistenceContext
	private EntityManager entityManager;

	@EJB
	private CiudadEJB ciudadEJB;

	/**
	 * Crea el EJB de catálogos.
	 */
	public CatalogoEJB() {
		super();
	}

	/**
	 * Lista las ciudades ordenadas por nombre.
	 *
	 * @return lista de ciudades
	 */
	public List<Ciudad> listarCiudades() {
		return ciudadEJB.listarCiudades();
	}

	/**
	 * Lista todos los modelos ordenados por marca y nombre.
	 *
	 * @return lista de modelos
	 */
	public List<Modelo> listarModelos() {
		return entityManager.createNamedQuery(Modelo.GETALL, Modelo.class).getResultList();
	}

	/**
	 * Lista las marcas distintas en orden alfabético.
	 *
	 * @return lista de marcas
	 */
	public List<String> listarMarcas() {
		return entityManager.createNamedQuery(Modelo.MARCAS, String.class).getResultList();
	}

	/**
	 * Lista los modelos de una marca ordenados por nombre.
	 *
	 * @param marca nombre de la marca
	 * @return lista de modelos de la marca
	 */
	public List<Modelo> listarModelosPorMarca(String marca) {
		return entityManager.createNamedQuery(Modelo.MODELOS_POR_MARCA, Modelo.class)
				.setParameter("marca", marca).getResultList();
	}

	/**
	 * Lista las características ordenadas por nombre.
	 *
	 * @return lista de características
	 */
	public List<Caracteristica> listarCaracteristicas() {
		return entityManager.createNamedQuery(Caracteristica.CARACTERISTICAS_ORDENADAS, Caracteristica.class)
				.getResultList();
	}

	/**
	 * Lista los tipos de vehículo.
	 *
	 * @return valores del enumerado {@link TipoVehiculo}
	 */
	public List<TipoVehiculo> listarTiposVehiculo() {
		return Arrays.asList(TipoVehiculo.values());
	}

	/**
	 * Lista los tipos de combustible.
	 *
	 * @return valores del enumerado {@link TipoCombustible}
	 */
	public List<TipoCombustible> listarCombustibles() {
		return Arrays.asList(TipoCombustible.values());
	}

	/**
	 * Lista los tipos de transmisión.
	 *
	 * @return valores del enumerado {@link TipoTrasmision}
	 */
	public List<TipoTrasmision> listarTransmisiones() {
		return Arrays.asList(TipoTrasmision.values());
	}

	/**
	 * Lista las condiciones del vehículo.
	 *
	 * @return valores del enumerado {@link Condicion}
	 */
	public List<Condicion> listarCondiciones() {
		return Arrays.asList(Condicion.values());
	}

}
