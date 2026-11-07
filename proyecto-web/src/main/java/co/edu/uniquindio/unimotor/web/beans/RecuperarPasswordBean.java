package co.edu.uniquindio.unimotor.web.beans;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import co.edu.uniquindio.unimotor.ejb.ClienteEJB;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.EnvioCorreoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;
import co.edu.uniquindio.unimotor.web.util.Mensajes;

/**
 * Bean de la página de recuperación de contraseña. Siempre muestra el mismo
 * mensaje para no revelar qué correos están registrados.
 */
@Named
@RequestScoped
public class RecuperarPasswordBean {

	@EJB
	private ClienteEJB clienteEJB;

	/** Email al que se enviará la contraseña temporal. */
	private String email;

	/**
	 * Solicita el envío de una contraseña temporal si el email pertenece a un
	 * cliente.
	 *
	 * @return la regla de navegación hacia el login, o {@code null} si falla el envío
	 */
	public String recuperar() {
		try {
			clienteEJB.buscarCliente(email);
			clienteEJB.recuperarPassword(email);
		} catch (ElementoNoEncontradoException | InformacionInvalidaException e) {
			// No se revela si el correo existe: se muestra el mismo mensaje.
		} catch (EnvioCorreoException e) {
			Mensajes.error("No se pudo enviar el correo", "Intente más tarde.");
			return null;
		}
		Mensajes.info("Solicitud recibida",
				"Si el correo está registrado como cliente, recibirá una contraseña temporal en unos minutos.");
		Mensajes.conservarTrasRedireccion();
		return "/login?faces-redirect=true";
	}

	/**
	 * Obtiene el email escrito.
	 *
	 * @return el email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Establece el email escrito.
	 *
	 * @param email el email
	 */
	public void setEmail(String email) {
		this.email = email;
	}
}
