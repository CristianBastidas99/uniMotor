package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import co.edu.uniquindio.unimotor.ejb.ClienteEJB;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.web.util.Mensajes;

/**
 * Bean de sesión que mantiene al cliente autenticado y expone el estado de
 * seguridad a la plantilla y a las páginas.
 */
@Named
@SessionScoped
public class SeguridadBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private ClienteEJB clienteEJB;

	/** Cliente con sesión iniciada, o {@code null} si es un visitante. */
	private Cliente cliente;

	/** Email escrito en el formulario de inicio de sesión. */
	private String email;

	/** Contraseña escrita en el formulario; se limpia siempre tras el intento. */
	private String password;

	/** Ruta protegida a la que se debe volver tras iniciar sesión. */
	private String origen;

	/**
	 * Intenta iniciar sesión con el email y la contraseña del formulario.
	 *
	 * @return la regla de navegación (redirección) si va bien; {@code null} si falla
	 */
	public String iniciarSesion() {
		FacesContext fc = FacesContext.getCurrentInstance();
		try {
			Cliente autenticado = clienteEJB.autenticarCliente(email, password);
			// Evita la fijación de sesión: nuevo id de sesión tras autenticar.
			Object peticion = fc.getExternalContext().getRequest();
			if (peticion instanceof HttpServletRequest) {
				HttpServletRequest req = (HttpServletRequest) peticion;
				if (req.getSession(false) != null) {
					req.changeSessionId();
				}
			}
			this.cliente = autenticado;
			Mensajes.info("Bienvenido", "Bienvenido, " + autenticado.getNombre_completo());
			Mensajes.conservarTrasRedireccion();
			return destinoTrasLogin();
		} catch (AutenticacionException e) {
			Mensajes.error("No se pudo iniciar sesión", e.getMessage());
			return null;
		} finally {
			this.password = null;
		}
	}

	/**
	 * Calcula el destino tras iniciar sesión: {@code origen} si es una ruta
	 * segura, o el inicio en caso contrario.
	 */
	private String destinoTrasLogin() {
		if (esRutaSegura(origen)) {
			String sep = origen.contains("?") ? "&" : "?";
			return origen + sep + "faces-redirect=true";
		}
		return "/index?faces-redirect=true";
	}

	/**
	 * Indica si una ruta de origen es segura. Solo se aceptan rutas internas
	 * bajo {@code /usuario/} o exactamente {@code /detalleVehiculo.xhtml?id=N}
	 * (N numérico), sin {@code //}, barra invertida, dos puntos ni
	 * {@code ..}; así se evitan redirecciones abiertas hacia sitios externos.
	 *
	 * @param ruta la ruta a evaluar
	 * @return {@code true} si la ruta es segura
	 */
	static boolean esRutaSegura(String ruta) {
		if (ruta != null && ruta.matches("^/detalleVehiculo\\.xhtml\\?id=\\d{1,19}$")) {
			return true;
		}
		return ruta != null && ruta.startsWith("/usuario/") && !ruta.contains("//") && !ruta.contains("\\")
				&& !ruta.contains(":") && !ruta.contains("..");
	}

	/**
	 * Recarga el cliente de la sesión desde la base de datos.
	 */
	public void refrescarCliente() {
		if (cliente == null) {
			return;
		}
		try {
			cliente = clienteEJB.buscarCliente(cliente.getEmail());
		} catch (ElementoNoEncontradoException e) {
			Mensajes.error("No se pudo actualizar", e.getMessage());
		}
	}

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
	 * Obtiene el email del formulario de acceso.
	 *
	 * @return el email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Establece el email del formulario de acceso.
	 *
	 * @param email el email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Obtiene la contraseña del formulario de acceso.
	 *
	 * @return la contraseña
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Establece la contraseña del formulario de acceso.
	 *
	 * @param password la contraseña
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Obtiene la ruta de origen a la que volver tras el acceso.
	 *
	 * @return la ruta de origen
	 */
	public String getOrigen() {
		return origen;
	}

	/**
	 * Establece la ruta de origen a la que volver tras el acceso.
	 *
	 * @param origen la ruta de origen
	 */
	public void setOrigen(String origen) {
		this.origen = origen;
	}

	/**
	 * Cierra la sesión invalidándola y redirige al inicio.
	 *
	 * @return la regla de navegación hacia el inicio con redirección
	 */
	public String cerrarSesion() {
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		ec.invalidateSession();
		return "/index?faces-redirect=true";
	}
}
