package co.edu.uniquindio.empresa;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: Administrador
 *
 */
@Entity
@NamedQueries({
		@NamedQuery(name = Administrador.FIND_BY_ID, query = "select admi from Administrador admi where admi.nombre_administrador = :nombre"),
		@NamedQuery(name = Administrador.GETALL, query = "select admi from Administrador admi") })
public class Administrador extends Usuario implements Serializable {

	@Column(name = "nombre_administrador", nullable = false)
	private String nombre_administrador;

	private static final long serialVersionUID = 1L;
	public static final String GETALL = "Administrador_GetAll";
	public static final String FIND_BY_ID = "Administrador_findById";

	public Administrador() {
		super();
	}

	/**
	 * @param email
	 * @param password
	 * @param nombre_administrador
	 */
	public Administrador(String email, String password, String nombre_administrador) {
		super(email, password);
		this.nombre_administrador = nombre_administrador;
	}

	public String getNombre_administrador() {
		return this.nombre_administrador;
	}

	public void setNombre_administrador(String nombre_administrador) {
		this.nombre_administrador = nombre_administrador;
	}

}
