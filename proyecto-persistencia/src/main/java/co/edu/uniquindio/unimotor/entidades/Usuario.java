package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.*;

/**
 * Usuario registrado en uniMotor: es la raíz de la jerarquía de usuarios del portal de compra/venta
 * de vehículos y guarda las credenciales de acceso.
 * <p>
 * Tabla: {@code Usuario}. Usa herencia {@code JOINED}: {@link Cliente} y {@link Administrador}
 * guardan sus datos propios en tablas aparte unidas por la clave primaria {@code codigo_usuario}.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
		@NamedQuery(name = Usuario.AUTENTIFICAR_USUARIO, query = "select u from Usuario u where u.email = :email and u.password = :password"),
		@NamedQuery(name = Usuario.AUTENTIFICAR_EMAIL, query = "select u from Usuario u where u.email = :email"),
		@NamedQuery(name = Usuario.GETALL, query = "select u from Usuario u")
		
})
public class Usuario implements Serializable {

	/**
	 * Identificador único autogenerado (clave primaria).
	 */
	@Id
	@Column(name = "codigo_usuario", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long codigo_usuario;

	/**
	 * Correo electrónico; obligatorio, único y de máximo 100 caracteres. Se usa como nombre de usuario.
	 */
	@Column(name = "email", nullable = false, unique = true, length = 100)
	private String email;

	/**
	 * Contraseña de acceso; obligatoria, máximo 100 caracteres.
	 */
	@Column(name = "password", nullable = false, length = 100)
	private String password;

	/**
	 * Versión de serialización.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * NamedQuery que busca un usuario por credenciales.
	 * Parámetros: {@code :email} (String) y {@code :password} (String).
	 * Devuelve la entidad {@link Usuario} coincidente (o ninguna si las credenciales son inválidas).
	 */
	public static final String AUTENTIFICAR_USUARIO = "Usuario_AUTENTIFICAR_USUARIO";
	/**
	 * NamedQuery que busca un usuario por correo.
	 * Parámetro: {@code :email} (String). Devuelve el {@link Usuario} con ese correo.
	 */
	public static final String AUTENTIFICAR_EMAIL = "Usuario_AUTENTIFICAR_EMAIL";
	/**
	 * NamedQuery sin parámetros que devuelve todos los {@link Usuario} (clientes y administradores).
	 */
	public static final String GETALL = "Usuario_GETALL";

	/**
	 * Constructor vacío requerido por JPA.
	 */
	public Usuario() {
		super();
	}

	/**
	 * Crea un usuario con sus credenciales.
	 *
	 * @param email correo electrónico (único)
	 * @param password contraseña
	 */
	public Usuario(String email, String password) {
		super();
		this.email = email;
		this.password = password;
	}

	/**
	 * Calcula el hash a partir del identificador {@code codigo_usuario}.
	 *
	 * @return código hash del usuario
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo_usuario == null) ? 0 : codigo_usuario.hashCode());
		return result;
	}

	/**
	 * Dos usuarios son iguales si son de la misma clase y tienen el mismo {@code codigo_usuario}.
	 *
	 * @param obj objeto a comparar
	 * @return {@code true} si representan el mismo usuario
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		if (codigo_usuario == null) {
			if (other.codigo_usuario != null)
				return false;
		} else if (!codigo_usuario.equals(other.codigo_usuario))
			return false;
		return true;
	}

	/**
	 * Obtiene el valor de {@code Codigo_usuario}.
	 *
	 * @return valor de {@code Codigo_usuario}
	 */
	public Long getCodigo_usuario() {
		return this.codigo_usuario;
	}

	/**
	 * Establece el valor de {@code Codigo_usuario}.
	 *
	 * @param codigo_usuario nuevo valor
	 */
	public void setCodigo_usuario(Long codigo_usuario) {
		this.codigo_usuario = codigo_usuario;
	}

	/**
	 * Obtiene el valor de {@code Email}.
	 *
	 * @return valor de {@code Email}
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 * Establece el valor de {@code Email}.
	 *
	 * @param email nuevo valor
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Obtiene el valor de {@code Password}.
	 *
	 * @return valor de {@code Password}
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * Establece el valor de {@code Password}.
	 *
	 * @param password nuevo valor
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Representación en texto del usuario (sin la contraseña).
	 *
	 * @return texto descriptivo del objeto
	 */
	@Override
	public String toString() {
		return "Usuario [codigo_usuario=" + codigo_usuario + ", email=" + email + "]";
	}

}
