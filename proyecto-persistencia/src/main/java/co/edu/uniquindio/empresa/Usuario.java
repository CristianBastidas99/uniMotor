package co.edu.uniquindio.empresa;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: Usuario
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@MappedSuperclass
public class Usuario implements Serializable {

	@Id
	@Column(name = "codigo_usuario")
	private String codigo_usuario;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	private static final long serialVersionUID = 1L;
	public static final String AUTENTIFICAR_USUARO = "Usuario_Auntentificar";
	public static final String AUTENTIFICAR_EMAIL = "Usuario_AutentificarEmail";

	public Usuario() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo_usuario == null) ? 0 : codigo_usuario.hashCode());
		return result;
	}

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

	public String getCodigo_usuario() {
		return this.codigo_usuario;
	}

	public void setCodigo_usuario(String codigo_usuario) {
		this.codigo_usuario = codigo_usuario;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
