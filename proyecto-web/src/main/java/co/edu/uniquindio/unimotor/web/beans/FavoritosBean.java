package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import co.edu.uniquindio.unimotor.ejb.ClienteEJB;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.web.util.Mensajes;

/**
 * Bean de la página "Mis favoritos".
 */
@Named
@ViewScoped
public class FavoritosBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private ClienteEJB clienteEJB;

	@Inject
	private SeguridadBean seguridadBean;

	private List<Vehiculo> favoritos = new ArrayList<Vehiculo>();

	/** Carga inicial de favoritos. */
	@PostConstruct
	public void init() {
		recargar();
	}

	private void recargar() {
		try {
			favoritos = clienteEJB.listarFavoritos(seguridadBean.getCliente().getEmail());
		} catch (ElementoNoEncontradoException e) {
			favoritos = new ArrayList<Vehiculo>();
			Mensajes.error("No se pudieron cargar los favoritos", e.getMessage());
		}
	}

	/**
	 * Vehículos favoritos del cliente.
	 *
	 * @return lista de favoritos
	 */
	public List<Vehiculo> getFavoritos() {
		return favoritos;
	}

	/**
	 * Indica si un vehículo sigue disponible (activo y sin venta).
	 *
	 * @param v vehículo
	 * @return {@code true} si está disponible
	 */
	public boolean disponible(Vehiculo v) {
		return v.isActivo() && v.getCodigo_venta() == null;
	}

	/**
	 * Quita un vehículo de favoritos y recarga la lista.
	 *
	 * @param v vehículo a quitar
	 */
	public void quitar(Vehiculo v) {
		try {
			clienteEJB.quitarFavorito(seguridadBean.getCliente().getEmail(), v.getCodigo_vehiculo());
			Mensajes.info("Favorito quitado", "El vehículo se quitó de tus favoritos.");
		} catch (ElementoNoEncontradoException e) {
			Mensajes.error("No se pudo quitar", e.getMessage());
		}
		recargar();
	}
}
