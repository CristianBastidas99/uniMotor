package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import co.edu.uniquindio.unimotor.ejb.VehiculoEJB;
import co.edu.uniquindio.unimotor.entidades.Condicion;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;

/**
 * Datos de la página principal: los 10 vehículos más buscados, nuevos y usados.
 */
@Named
@ViewScoped
public class InicioBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(InicioBean.class.getName());

	@EJB
	private transient VehiculoEJB vehiculoEJB;

	private List<Vehiculo> topNuevos;
	private List<Vehiculo> topUsados;

	/** @return los 10 vehículos nuevos más buscados (lista vacía si falla la consulta) */
	public List<Vehiculo> getTopNuevos() {
		if (topNuevos == null) {
			topNuevos = cargar(Condicion.NUEVO);
		}
		return topNuevos;
	}

	/** @return los 10 vehículos usados más buscados (lista vacía si falla la consulta) */
	public List<Vehiculo> getTopUsados() {
		if (topUsados == null) {
			topUsados = cargar(Condicion.USADO);
		}
		return topUsados;
	}

	private List<Vehiculo> cargar(Condicion condicion) {
		try {
			return new ArrayList<>(vehiculoEJB.top10MasBuscados(condicion));
		} catch (Exception e) {
			LOG.log(Level.WARNING, "No se pudo cargar el top 10 de vehículos " + condicion, e);
			return new ArrayList<>();
		}
	}
}
