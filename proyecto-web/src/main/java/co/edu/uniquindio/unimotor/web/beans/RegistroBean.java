package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import co.edu.uniquindio.unimotor.ejb.ClienteEJB;
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.excepciones.ElementoRepetidoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;
import co.edu.uniquindio.unimotor.web.util.Mensajes;

/**
 * Bean de la página de registro de clientes.
 */
@Named
@ViewScoped
public class RegistroBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Tipos de teléfono disponibles (coinciden con los de los datos existentes). */
	private static final List<String> TIPOS = Collections
			.unmodifiableList(Arrays.asList("Celular", "Casa", "Trabajo"));

	/** Longitud mínima de la contraseña. */
	private static final int MINIMO_PASSWORD = 4;

	/**
	 * Fila editable de teléfono: tipo y número.
	 */
	public static class TelefonoFila implements Serializable {

		private static final long serialVersionUID = 1L;

		private String tipo;
		private String numero;

		/**
		 * Crea una fila.
		 *
		 * @param tipo tipo de teléfono
		 */
		public TelefonoFila(String tipo) {
			this.tipo = tipo;
		}

		/**
		 * Obtiene el tipo.
		 *
		 * @return el tipo de teléfono
		 */
		public String getTipo() {
			return tipo;
		}

		/**
		 * Establece el tipo.
		 *
		 * @param tipo el tipo de teléfono
		 */
		public void setTipo(String tipo) {
			this.tipo = tipo;
		}

		/**
		 * Obtiene el número.
		 *
		 * @return el número
		 */
		public String getNumero() {
			return numero;
		}

		/**
		 * Establece el número.
		 *
		 * @param numero el número
		 */
		public void setNumero(String numero) {
			this.numero = numero;
		}
	}

	@EJB
	private transient ClienteEJB clienteEJB;

	@Inject
	private SeguridadBean seguridadBean;

	private String nombreCompleto;
	private String email;
	private String password;
	private String confirmacion;
	private String direccion;
	private Ciudad ciudad;
	private List<TelefonoFila> telefonos = new ArrayList<>();

	/**
	 * Inicia el formulario con una fila de teléfono celular.
	 */
	@PostConstruct
	public void iniciar() {
		telefonos.add(new TelefonoFila("Celular"));
	}

	/**
	 * Añade una fila de teléfono con el primer tipo que aún no se usa.
	 */
	public void agregarTelefono() {
		Set<String> usados = new HashSet<>();
		for (TelefonoFila f : telefonos) {
			usados.add(f.getTipo());
		}
		for (String tipo : TIPOS) {
			if (!usados.contains(tipo)) {
				telefonos.add(new TelefonoFila(tipo));
				return;
			}
		}
		Mensajes.advertencia("Teléfonos", "Ya agregaste un teléfono de cada tipo (máximo 3).");
	}

	/**
	 * Quita una fila de teléfono, dejando al menos una.
	 *
	 * @param fila la fila a quitar
	 */
	public void quitarTelefono(TelefonoFila fila) {
		if (telefonos.size() <= 1) {
			Mensajes.advertencia("Teléfonos", "Debes conservar al menos un teléfono.");
			return;
		}
		telefonos.remove(fila);
	}

	/**
	 * Valida los datos, registra al cliente e inicia su sesión.
	 *
	 * @return la navegación al inicio si todo sale bien; {@code null} para permanecer en la página
	 */
	public String registrar() {
		if (password == null || password.length() < MINIMO_PASSWORD) {
			Mensajes.error("Contraseña", "La contraseña debe tener al menos " + MINIMO_PASSWORD + " caracteres.");
			return null;
		}
		if (!password.equals(confirmacion)) {
			Mensajes.error("Contraseña", "Las contraseñas no coinciden.");
			return null;
		}
		Map<String, String> mapa = construirTelefonos();
		if (mapa == null) {
			return null;
		}
		String correo = email.trim().toLowerCase(Locale.ROOT);
		if (clienteEJB.existeEmail(correo)) {
			Mensajes.error("Correo repetido", "Ya existe una cuenta con ese correo.");
			return null;
		}
		Cliente nuevo = new Cliente();
		nuevo.setEmail(correo);
		nuevo.setPassword(password);
		nuevo.setNombre_completo(nombreCompleto.trim());
		nuevo.setDireccion(direccion.trim());
		nuevo.setCodigo_ciudad(ciudad);
		nuevo.setTelefono(mapa);
		try {
			Cliente registrado = clienteEJB.registrarCliente(nuevo);
			seguridadBean.setCliente(registrado);
			FacesContext fc = FacesContext.getCurrentInstance();
			((HttpServletRequest) fc.getExternalContext().getRequest()).changeSessionId();
			Mensajes.info("Registro exitoso", "Bienvenido a uniMotor, " + registrado.getNombre_completo() + ".");
			Mensajes.conservarTrasRedireccion();
			return "/index?faces-redirect=true";
		} catch (ElementoRepetidoException e) {
			Mensajes.error("Correo repetido", "Ya existe una cuenta con ese correo.");
		} catch (InformacionInvalidaException e) {
			Mensajes.error("Datos inválidos", e.getMessage());
		}
		return null;
	}

	/**
	 * Valida las filas de teléfono y arma el mapa tipo a número.
	 *
	 * @return el mapa, o {@code null} si hay errores (ya informados)
	 */
	private Map<String, String> construirTelefonos() {
		Map<String, String> mapa = new HashMap<>();
		for (TelefonoFila f : telefonos) {
			String num = f.getNumero() == null ? "" : f.getNumero().trim();
			if (num.isEmpty()) {
				continue;
			}
			if (!num.matches("\\d{7,10}")) {
				Mensajes.error("Teléfono inválido", "El teléfono " + f.getTipo().toLowerCase(Locale.ROOT)
						+ " debe tener solo dígitos, entre 7 y 10.");
				return null;
			}
			if (mapa.containsKey(f.getTipo())) {
				Mensajes.error("Teléfono repetido", "Solo puedes indicar un teléfono por tipo.");
				return null;
			}
			mapa.put(f.getTipo(), num);
		}
		if (mapa.isEmpty()) {
			Mensajes.error("Teléfono obligatorio", "Indica al menos un número de teléfono.");
			return null;
		}
		return mapa;
	}

	/**
	 * Tipos de teléfono disponibles.
	 *
	 * @return la lista de tipos
	 */
	public List<String> getTiposTelefono() {
		return TIPOS;
	}

	/**
	 * Filas de teléfono del formulario.
	 *
	 * @return las filas
	 */
	public List<TelefonoFila> getTelefonos() {
		return telefonos;
	}

	/**
	 * Obtiene el nombre completo.
	 *
	 * @return el nombre completo
	 */
	public String getNombreCompleto() {
		return nombreCompleto;
	}

	/**
	 * Establece el nombre completo.
	 *
	 * @param nombreCompleto el nombre completo
	 */
	public void setNombreCompleto(String nombreCompleto) {
		this.nombreCompleto = nombreCompleto;
	}

	/**
	 * Obtiene el correo.
	 *
	 * @return el correo
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Establece el correo.
	 *
	 * @param email el correo
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Obtiene la contraseña.
	 *
	 * @return la contraseña
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Establece la contraseña.
	 *
	 * @param password la contraseña
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Obtiene la confirmación de la contraseña.
	 *
	 * @return la confirmación
	 */
	public String getConfirmacion() {
		return confirmacion;
	}

	/**
	 * Establece la confirmación de la contraseña.
	 *
	 * @param confirmacion la confirmación
	 */
	public void setConfirmacion(String confirmacion) {
		this.confirmacion = confirmacion;
	}

	/**
	 * Obtiene la dirección.
	 *
	 * @return la dirección
	 */
	public String getDireccion() {
		return direccion;
	}

	/**
	 * Establece la dirección.
	 *
	 * @param direccion la dirección
	 */
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	/**
	 * Obtiene la ciudad elegida.
	 *
	 * @return la ciudad
	 */
	public Ciudad getCiudad() {
		return ciudad;
	}

	/**
	 * Establece la ciudad elegida.
	 *
	 * @param ciudad la ciudad
	 */
	public void setCiudad(Ciudad ciudad) {
		this.ciudad = ciudad;
	}
}
