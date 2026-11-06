package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.*;

/**
 * Administrador del portal uniMotor: usuario con permisos de gestión y acceso a las estadísticas.
 * <p>
 * Tabla: {@code Administrador} (herencia {@code JOINED} de {@link Usuario}, unida por {@code codigo_usuario}).
 */
@Entity
@NamedQueries({
		@NamedQuery(name = Administrador.FIND_BY_NOMBRE, query = "select admi from Administrador admi where admi.nombre_administrador = :nombre"),
		@NamedQuery(name = Administrador.GETALL, query = "select admi from Administrador admi") })
public class Administrador extends Usuario implements Serializable {

	/**
	 * Nombre del administrador; obligatorio, máximo 100 caracteres.
	 */
	@Column(name = "nombre_administrador", nullable = false, length = 100)
	private String nombre_administrador;

	/**
	 * Versión de serialización.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * NamedQuery sin parámetros que devuelve todos los {@link Administrador}.
	 */
	public static final String GETALL = "Administrador_GETALL";
	/**
	 * NamedQuery que busca administradores por nombre.
	 * Parámetro: {@code :nombre} (String, se compara con {@code nombre_administrador}).
	 * Devuelve entidades {@link Administrador}.
	 */
	public static final String FIND_BY_NOMBRE = "Administrador_FIND_BY_NOMBRE";

	/**
	 * Constructor vacío requerido por JPA.
	 */
	public Administrador() {
		super();
	}

	/**
	 * Crea un administrador.
	 *
	 * @param email correo electrónico (único)
	 * @param password contraseña
	 * @param nombre_administrador nombre del administrador
	 */
	public Administrador(String email, String password, String nombre_administrador) {
		super(email, password);
		this.nombre_administrador = nombre_administrador;
	}

	/**
	 * Obtiene el valor de {@code Nombre_administrador}.
	 *
	 * @return valor de {@code Nombre_administrador}
	 */
	public String getNombre_administrador() {
		return this.nombre_administrador;
	}

	/**
	 * Establece el valor de {@code Nombre_administrador}.
	 *
	 * @param nombre_administrador nuevo valor
	 */
	public void setNombre_administrador(String nombre_administrador) {
		this.nombre_administrador = nombre_administrador;
	}

	/**
	 * Representación en texto del administrador.
	 *
	 * @return texto descriptivo del objeto
	 */
	@Override
	public String toString() {
		return "Administrador [codigo_usuario=" + getCodigo_usuario() + ", email=" + getEmail()
				+ ", nombre_administrador=" + nombre_administrador + "]";
	}

}
