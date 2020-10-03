package co.edu.uniquindio.empresa;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: Ciudad
 *
 */
@Entity

public class Ciudad implements Serializable {

	@Id
	@Column(name = "codigo_ciudad")
	private int codigo_ciudad;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	@OneToMany(mappedBy = "ciudad")
	private List<Persona> personas;

	private static final long serialVersionUID = 1L;

	public Ciudad() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + codigo_ciudad;
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
		Ciudad other = (Ciudad) obj;
		if (codigo_ciudad != other.codigo_ciudad)
			return false;
		return true;
	}

	public int getCodigo_ciudad() {
		return this.codigo_ciudad;
	}

	public void setCodigo_ciudad(int codigo_ciudad) {
		this.codigo_ciudad = codigo_ciudad;
	}

	/**
	 * @return the nombre
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * @param nombre the nombre to set
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * @return the personas
	 */
	public List<Persona> getPersonas() {
		return personas;
	}

	/**
	 * @param personas the personas to set
	 */
	public void setPersonas(List<Persona> personas) {
		this.personas = personas;
	}

}
