package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import co.edu.uniquindio.unimotor.entidades.Cliente;

/**
 * Bean de sesión que mantiene al cliente autenticado y expone el estado de
 * seguridad a la plantilla y a las páginas.
 */
@Named
@SessionScoped
public class SeguridadBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Cliente con sesión iniciada, o {@code null} si es un visitante. */
	private Cliente cliente;

	// TODO Fase 3.3: añadir iniciarSesion con ClienteEJB.autenticarCliente.

	/**
	 * Obtiene el cliente autenticado.
	 *
	 * @return el cliente en sesión o {@code null}
	 */
	public Cliente getCliente() {
		return cliente;
	}

	/**
	 * Establece el cliente autenticado.
	 *
	 * @param cliente el cliente en sesión o {@code null}
	 */
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	/**
	 * Indica si hay un cliente con sesión iniciada.
	 *
	 * @return {@code true} si hay un cliente autenticado
	 */
	public boolean isAutenticado() {
		return cliente != null;
	}

	/**
	 * Cierra la sesión invalidándola y redirige al inicio.
	 *
	 * @return la regla de navegación hacia el inicio con redirección
	 */
	public String cerrarSesion() {
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		return "/index?faces-redirect=true";
	}
}
