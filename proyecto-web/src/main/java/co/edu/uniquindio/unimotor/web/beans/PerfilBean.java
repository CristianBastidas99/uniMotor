package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
import co.edu.uniquindio.unimotor.ejb.PreguntaEJB;
import co.edu.uniquindio.unimotor.ejb.VehiculoEJB;
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Pregunta;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.ElementoRepetidoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;
import co.edu.uniquindio.unimotor.web.beans.RegistroBean.TelefonoFila;
import co.edu.uniquindio.unimotor.web.util.Mensajes;

/**
 * Bean de la página del perfil del cliente: datos personales, contraseña,
 * eliminación de cuenta y resúmenes de vehículos y preguntas.
 */
@Named
@ViewScoped
public class PerfilBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Tipos de teléfono disponibles. */
	private static final List<String> TIPOS = Collections
			.unmodifiableList(Arrays.asList("Celular", "Casa", "Trabajo"));

	/** Cantidad de elementos que muestran los resúmenes. */
	private static final int RESUMEN = 5;

	@EJB
	private transient ClienteEJB clienteEJB;

	@EJB
	private transient VehiculoEJB vehiculoEJB;

	@EJB
	private transient PreguntaEJB preguntaEJB;

	@Inject
	private SeguridadBean seguridadBean;

	private String nombreCompleto;
	private String email;
	private String direccion;
	private Ciudad ciudad;
	private List<TelefonoFila> telefonos = new ArrayList<>();

	private String passwordActual;
	private String passwordNueva;
	private String passwordConfirmacion;

	private String passwordEliminar;

	private List<Vehiculo> vehiculosRecientes = new ArrayList<>();
	private int totalVehiculos;
	private List<Pregunta> preguntasRecientes = new ArrayList<>();
	private int totalPreguntas;
	private int preguntasSinResponder;

	/**
	 * Carga los datos del cliente en sesión y los resúmenes.
	 */
	@PostConstruct
	public void iniciar() {
		cargarDatos();
		cargarResumenes();
	}

	/**
	 * Copia los datos de la sesión al formulario.
	 */
	private void cargarDatos() {
		Cliente c = seguridadBean.getCliente();
		if (c == null) {
			return;
		}
		nombreCompleto = c.getNombre_completo();
		email = c.getEmail();
		direccion = c.getDireccion();
		ciudad = c.getCodigo_ciudad();
		telefonos = new ArrayList<>();
		Map<String, String> mapa = c.getTelefono();
		if (mapa != null) {
			for (String tipo : TIPOS) {
				String numero = mapa.get(tipo);
				if (numero != null) {
					TelefonoFila f = new TelefonoFila(tipo);
					f.setNumero(numero);
					telefonos.add(f);
				}
			}
		}
		if (telefonos.isEmpty()) {
			telefonos.add(new TelefonoFila("Celular"));
		}
	}

	/**
	 * Carga los resúmenes de vehículos publicados y preguntas recibidas.
	 */
	private void cargarResumenes() {
		Cliente c = seguridadBean.getCliente();
		if (c == null) {
			return;
		}
		List<Vehiculo> vehiculos = new ArrayList<>(vehiculoEJB.listarVehiculosDeCliente(c.getEmail()));
		Collections.sort(vehiculos, (a, b) -> {
			Date fa = a.getFecha_publicacion();
			Date fb = b.getFecha_publicacion();
			if (fa == null && fb == null) {
				return 0;
			}
			if (fa == null) {
				return 1;
			}
			if (fb == null) {
				return -1;
			}
			return fb.compareTo(fa);
		});
		totalVehiculos = vehiculos.size();
		vehiculosRecientes = new ArrayList<>(vehiculos.subList(0, Math.min(RESUMEN, vehiculos.size())));

		List<Pregunta> preguntas = preguntaEJB.listarPreguntasRecibidas(c.getEmail());
		totalPreguntas = preguntas.size();
		preguntasRecientes = new ArrayList<>(preguntas.subList(0, Math.min(RESUMEN, preguntas.size())));
		preguntasSinResponder = preguntaEJB.listarPreguntasSinResponder(c.getEmail()).size();
	}

	/**
	 * Estado legible de un vehículo: Vendido, Inactivo o Activo.
	 *
	 * @param v el vehículo
	 * @return el estado en español
	 */
	public String estado(Vehiculo v) {
		if (v == null) {
			return "";
		}
		if (v.getCodigo_venta() != null) {
			return "Vendido";
		}
		return v.isActivo() ? "Activo" : "Inactivo";
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
		Mensajes.advertencia("Teléfonos", "Ya agregó un teléfono de cada tipo (máximo 3).");
	}

	/**
	 * Quita una fila de teléfono, dejando al menos una.
	 *
	 * @param fila la fila a quitar
	 */
	public void quitarTelefono(TelefonoFila fila) {
		if (telefonos.size() <= 1) {
			Mensajes.advertencia("Teléfonos", "Debe conservar al menos un teléfono.");
			return;
		}
		telefonos.remove(fila);
	}

	/**
	 * Valida y arma el mapa tipo a número de teléfono.
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
				Mensajes.error("Teléfono repetido", "Solo puede indicar un teléfono por tipo.");
				return null;
			}
			mapa.put(f.getTipo(), num);
		}
		if (mapa.isEmpty()) {
			Mensajes.error("Teléfono obligatorio", "Indique al menos un número de teléfono.");
			return null;
		}
		return mapa;
	}

	/**
	 * Guarda los datos personales. Trabaja sobre una copia: la sesión solo se
	 * actualiza cuando el EJB acepta los cambios.
	 */
	public void guardarDatos() {
		Cliente actual = seguridadBean.getCliente();
		if (actual == null) {
			return;
		}
		Map<String, String> mapa = construirTelefonos();
		if (mapa == null) {
			return;
		}
		Cliente copia = new Cliente();
		copia.setCodigo_usuario(actual.getCodigo_usuario());
		copia.setEmail(email == null ? null : email.trim().toLowerCase(Locale.ROOT));
		copia.setNombre_completo(nombreCompleto == null ? null : nombreCompleto.trim());
		copia.setDireccion(direccion == null ? null : direccion.trim());
		copia.setCodigo_ciudad(ciudad);
		copia.setTelefono(mapa);
		try {
			Cliente guardado = clienteEJB.modificarCliente(copia);
			seguridadBean.actualizarCliente(guardado);
			cargarDatos();
			Mensajes.info("Datos actualizados", "Tus datos personales se guardaron correctamente.");
		} catch (ElementoRepetidoException e) {
			Mensajes.error("Correo repetido", "Ya existe una cuenta con ese correo.");
		} catch (ElementoNoEncontradoException | InformacionInvalidaException e) {
			Mensajes.error("No se pudo guardar", e.getMessage());
		}
	}

	/**
	 * Cambia la contraseña del cliente. Siempre limpia los campos.
	 */
	public void cambiarPassword() {
		try {
			if (passwordNueva == null || passwordNueva.length() < 4) {
				Mensajes.error("Contraseña", "La contraseña nueva debe tener al menos 4 caracteres.");
				return;
			}
			if (!passwordNueva.equals(passwordConfirmacion)) {
				Mensajes.error("Contraseña", "Las contraseñas no coinciden.");
				return;
			}
			clienteEJB.cambiarPassword(seguridadBean.getCliente().getEmail(), passwordActual, passwordNueva);
			Mensajes.info("Contraseña actualizada", "Tu contraseña se cambió correctamente.");
		} catch (AutenticacionException e) {
			Mensajes.error("Contraseña actual incorrecta", "La contraseña actual no es correcta.");
		} catch (InformacionInvalidaException e) {
			Mensajes.error("Contraseña inválida", e.getMessage());
		} finally {
			passwordActual = null;
			passwordNueva = null;
			passwordConfirmacion = null;
		}
	}

	/**
	 * Elimina la cuenta tras verificar la contraseña. Si se elimina, cierra la
	 * sesión del cliente y redirige al inicio.
	 *
	 * @return la navegación al inicio, o {@code null} si no se pudo eliminar
	 */
	public String eliminarCuenta() {
		Cliente actual = seguridadBean.getCliente();
		if (actual == null) {
			return null;
		}
		String correo = actual.getEmail();
		try {
			clienteEJB.autenticarCliente(correo, passwordEliminar);
			clienteEJB.eliminarCliente(correo);
		} catch (AutenticacionException e) {
			Mensajes.error("Contraseña incorrecta", "La contraseña no es correcta; la cuenta no se eliminó.");
			return null;
		} catch (InformacionInvalidaException | ElementoNoEncontradoException e) {
			Mensajes.error("No se pudo eliminar la cuenta", e.getMessage());
			return null;
		} finally {
			passwordEliminar = null;
		}
		// Se cierra la sesión del cliente. No se invalida la sesión HTTP para que el mensaje
		// conservado en el flash sobreviva a la redirección; se renueva el id de sesión.
		seguridadBean.setCliente(null);
		Object peticion = FacesContext.getCurrentInstance().getExternalContext().getRequest();
		if (peticion instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) peticion;
			if (req.getSession(false) != null) {
				req.changeSessionId();
			}
		}
		Mensajes.info("Tu cuenta fue eliminada", "Tus datos fueron borrados de uniMotor.");
		Mensajes.conservarTrasRedireccion();
		return "/index?faces-redirect=true";
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
	 * Obtiene la ciudad.
	 *
	 * @return la ciudad
	 */
	public Ciudad getCiudad() {
		return ciudad;
	}

	/**
	 * Establece la ciudad.
	 *
	 * @param ciudad la ciudad
	 */
	public void setCiudad(Ciudad ciudad) {
		this.ciudad = ciudad;
	}

	/**
	 * Obtiene la contraseña actual escrita.
	 *
	 * @return la contraseña actual
	 */
	public String getPasswordActual() {
		return passwordActual;
	}

	/**
	 * Establece la contraseña actual escrita.
	 *
	 * @param passwordActual la contraseña actual
	 */
	public void setPasswordActual(String passwordActual) {
		this.passwordActual = passwordActual;
	}

	/**
	 * Obtiene la contraseña nueva.
	 *
	 * @return la contraseña nueva
	 */
	public String getPasswordNueva() {
		return passwordNueva;
	}

	/**
	 * Establece la contraseña nueva.
	 *
	 * @param passwordNueva la contraseña nueva
	 */
	public void setPasswordNueva(String passwordNueva) {
		this.passwordNueva = passwordNueva;
	}

	/**
	 * Obtiene la confirmación de la contraseña nueva.
	 *
	 * @return la confirmación
	 */
	public String getPasswordConfirmacion() {
		return passwordConfirmacion;
	}

	/**
	 * Establece la confirmación de la contraseña nueva.
	 *
	 * @param passwordConfirmacion la confirmación
	 */
	public void setPasswordConfirmacion(String passwordConfirmacion) {
		this.passwordConfirmacion = passwordConfirmacion;
	}

	/**
	 * Obtiene la contraseña escrita para confirmar la eliminación.
	 *
	 * @return la contraseña
	 */
	public String getPasswordEliminar() {
		return passwordEliminar;
	}

	/**
	 * Establece la contraseña escrita para confirmar la eliminación.
	 *
	 * @param passwordEliminar la contraseña
	 */
	public void setPasswordEliminar(String passwordEliminar) {
		this.passwordEliminar = passwordEliminar;
	}

	/**
	 * Los 5 vehículos publicados más recientes.
	 *
	 * @return la lista de vehículos
	 */
	public List<Vehiculo> getVehiculosRecientes() {
		return vehiculosRecientes;
	}

	/**
	 * Total de vehículos publicados.
	 *
	 * @return el total
	 */
	public int getTotalVehiculos() {
		return totalVehiculos;
	}

	/**
	 * Las 5 preguntas recibidas más recientes.
	 *
	 * @return la lista de preguntas
	 */
	public List<Pregunta> getPreguntasRecientes() {
		return preguntasRecientes;
	}

	/**
	 * Total de preguntas recibidas.
	 *
	 * @return el total
	 */
	public int getTotalPreguntas() {
		return totalPreguntas;
	}

	/**
	 * Cantidad de preguntas recibidas sin responder.
	 *
	 * @return la cantidad
	 */
	public int getPreguntasSinResponder() {
		return preguntasSinResponder;
	}
}
