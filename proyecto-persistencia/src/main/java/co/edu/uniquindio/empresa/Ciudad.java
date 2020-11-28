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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long codigo_ciudad;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	@OneToMany(mappedBy = "codigo_ciudad")
	private List<Cliente> clientes;

	@OneToMany(mappedBy = "codigo_ciudad")
	private List<Vehiculo> vehiculos;

	private static final long serialVersionUID = 1L;

	public Ciudad() {
		super();
	}

	public Ciudad(String nombre) {
		super();
		this.nombre = nombre;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo_ciudad == null) ? 0 : codigo_ciudad.hashCode());
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
		if (codigo_ciudad == null) {
			if (other.codigo_ciudad != null)
				return false;
		} else if (!codigo_ciudad.equals(other.codigo_ciudad))
			return false;
		return true;
	}

	public Long getCodigo_ciudad() {
		return this.codigo_ciudad;
	}

	public void setCodigo_ciudad(Long codigo_ciudad) {
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
	public List<Cliente> getPersonas() {
		return clientes;
	}

	/**
	 * @param clientes the personas to set
	 */
	public void setPersonas(List<Cliente> clientes) {
		this.clientes = clientes;
	}

	/**
	 * @return the clientes
	 */
	public List<Cliente> getClientes() {
		return clientes;
	}

	/**
	 * @param clientes the clientes to set
	 */
	public void setClientes(List<Cliente> clientes) {
		this.clientes = clientes;
	}

	/**
	 * @return the vehiculos
	 */
	public List<Vehiculo> getVehiculos() {
		return vehiculos;
	}

	/**
	 * @param vehiculos the vehiculos to set
	 */
	public void setVehiculos(List<Vehiculo> vehiculos) {
		this.vehiculos = vehiculos;
	}

	@Override
	public String toString() {
		return "Ciudad [codigo_ciudad=" + codigo_ciudad + ", nombre=" + nombre + "]";
	}

}
