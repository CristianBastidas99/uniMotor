package co.edu.uniquindio.empresa;

import java.io.Serializable;
import java.lang.String;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: Persona
 *
 */
@Entity

public class Persona implements Serializable {

	@Id
	@Column(name = "codigo_persona")
	private int codigo_persona;

	@Column(name = "nombre_completo", nullable = false)
	private String nombre_completo;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "direccion", nullable = false)
	private String direccion;

	@ElementCollection
	@CollectionTable(name = "telefonos_persona", joinColumns = @JoinColumn(name = "codigo_persona"))
	@Column(name = "telefonos")
	private Map<String, String> telefonos;

	@JoinColumn(name = "codigo_ciudad", nullable = false)
	@ManyToOne
	private Ciudad ciudad;
	
	@OneToMany(mappedBy = "persona")
	private List<Pregunta> preguntas;
	
	@ManyToMany(mappedBy = "personas")
	private List<Vehiculo> vehiculos_favoritos;

	private static final long serialVersionUID = 1L;

	public Persona() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ciudad == null) ? 0 : ciudad.hashCode());
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
		Persona other = (Persona) obj;
		if (ciudad == null) {
			if (other.ciudad != null)
				return false;
		} else if (!ciudad.equals(other.ciudad))
			return false;
		return true;
	}

	/**
	 * @return the telefonos
	 */
	public Map<String, String> getTelefonos() {
		return telefonos;
	}

	/**
	 * @param telefonos the telefonos to set
	 */
	public void setTelefonos(Map<String, String> telefonos) {
		this.telefonos = telefonos;
	}

	/**
	 * @return the ciudad
	 */
	public Ciudad getCiudad() {
		return ciudad;
	}

	/**
	 * @param ciudad the ciudad to set
	 */
	public void setCiudad(Ciudad ciudad) {
		this.ciudad = ciudad;
	}

	public int getCedula() {
		return this.codigo_persona;
	}

	public void setCedula(int cedula) {
		this.codigo_persona = cedula;
	}

	public String getNombre() {
		return this.nombre_completo;
	}

	public void setNombre(String nombre) {
		this.nombre_completo = nombre;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getClave() {
		return this.password;
	}

	public void setClave(String clave) {
		this.password = clave;
	}

	public String getDireccion() {
		return this.direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

}
