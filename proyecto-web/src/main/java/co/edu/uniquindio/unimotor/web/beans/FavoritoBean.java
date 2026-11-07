package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import co.edu.uniquindio.unimotor.ejb.ClienteEJB;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.ElementoRepetidoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;
import co.edu.uniquindio.unimotor.web.util.Mensajes;

/**
 * Bean de la vista de detalle que gestiona el botón de favoritos del vehículo
 * mostrado.
 */
@Named
@ViewScoped
public class FavoritoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private ClienteEJB clienteEJB;

	@Inject
	private SeguridadBean seguridadBean;

	@Inject
	private DetalleVehiculoBean detalleVehiculoBean;

	/** Estado calculado una sola vez; {@code null} mientras no se ha consultado. */
	private Boolean favorito;

	/**
	 * Indica si el vehículo está en los favoritos del cliente en sesión. Se
	 * consulta una vez por vista y luego se mantiene al alternar.
	 *
	 * @return {@code true} si es favorito; {@code false} si no o sin sesión
	 */
	public boolean isFavorito() {
		if (!seguridadBean.isAutenticado() || detalleVehiculoBean.getId() == null) {
			return false;
		}
		if (favorito == null) {
			favorito = clienteEJB.esFavorito(seguridadBean.getCliente().getEmail(), detalleVehiculoBean.getId());
		}
		return favorito;
	}

	/**
	 * Agrega el vehículo a favoritos o lo quita, según su estado actual.
	 */
	public void alternar() {
		if (!seguridadBean.isAutenticado()) {
			Mensajes.advertencia("Sesión requerida", "Inicia sesión para guardar vehículos en favoritos.");
			return;
		}
		String email = seguridadBean.getCliente().getEmail();
		Long codigo = detalleVehiculoBean.getId();
		try {
			if (isFavorito()) {
				clienteEJB.quitarFavorito(email, codigo);
				favorito = Boolean.FALSE;
				Mensajes.info("Quitado de favoritos", "El vehículo ya no está en tus favoritos.");
			} else {
				clienteEJB.agregarFavorito(email, codigo);
				favorito = Boolean.TRUE;
				Mensajes.info("Agregado a favoritos", "El vehículo se guardó en tus favoritos.");
			}
		} catch (ElementoRepetidoException e) {
			favorito = Boolean.TRUE;
			Mensajes.advertencia("Ya estaba en favoritos", e.getMessage());
		} catch (ElementoNoEncontradoException | InformacionInvalidaException e) {
			favorito = null;
			Mensajes.error("No se pudo actualizar favoritos", e.getMessage());
		}
	}

	/**
	 * Construye la dirección de inicio de sesión con regreso a esta página.
	 *
	 * @return ruta relativa al login con el parámetro {@code origen} codificado
	 */
	public String getUrlLogin() {
		return urlLogin(detalleVehiculoBean.getId());
	}

	/**
	 * Construye la dirección de login con regreso al detalle de un vehículo.
	 *
	 * @param id código del vehículo
	 * @return ruta relativa al login con el parámetro {@code origen} codificado
	 */
	static String urlLogin(Long id) {
		String origen = "/detalleVehiculo.xhtml?id=" + id;
		try {
			return "login.xhtml?origen=" + URLEncoder.encode(origen, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
}
