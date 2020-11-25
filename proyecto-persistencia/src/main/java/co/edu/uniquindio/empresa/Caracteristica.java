package co.edu.uniquindio.empresa;

import java.io.Serializable;
import java.lang.String;
import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: Caracteristica
 *
 */
@Entity
@NamedQueries({
	@NamedQuery(name = Caracteristica.FIND_BY_ID, query = "select c from Caracteristica c where c.nombre = :nombre"),
	@NamedQuery(name = Caracteristica.GETALL, query = "select c from Caracteristica c") })

public class Caracteristica implements Serializable {

	@Id
	@Column(name = "codigo_caracteristica")
	private String codigo_caracteristica;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	@JoinTable(name = "caracteristica_vehiculo", joinColumns = @JoinColumn(name = "codigo_caracteristica"), inverseJoinColumns = @JoinColumn(name = "codigo_vehiculo"))
	@ManyToMany(cascade = CascadeType.ALL)
	private List<Vehiculo> vehiculos;

	private static final long serialVersionUID = 1L;
	public static final String GETALL = "Caracteristica_GetAll";
	public static final String FIND_BY_ID = "Caracteristica_findById";

	public Caracteristica() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo_caracteristica == null) ? 0 : codigo_caracteristica.hashCode());
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
		Caracteristica other = (Caracteristica) obj;
		if (codigo_caracteristica == null) {
			if (other.codigo_caracteristica != null)
				return false;
		} else if (!codigo_caracteristica.equals(other.codigo_caracteristica))
			return false;
		return true;
	}

	public String getCodigo_caracteristica() {
		return this.codigo_caracteristica;
	}

	public void setCodigo_caracteristica(String codigo_caracteristica) {
		this.codigo_caracteristica = codigo_caracteristica;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
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

}
