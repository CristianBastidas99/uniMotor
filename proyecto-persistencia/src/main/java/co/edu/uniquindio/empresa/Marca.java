package co.edu.uniquindio.empresa;

import java.io.Serializable;
import java.lang.String;
import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: Marca
 *
 */
@Entity

public class Marca implements Serializable {

	@Id
	@Column(name = "codigo_marca")
	private String codigo_marca;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	@OneToMany(mappedBy = "codigo_marca")
	private List<Vehiculo> vehiculos;

	private static final long serialVersionUID = 1L;

	public Marca() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo_marca == null) ? 0 : codigo_marca.hashCode());
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
		Marca other = (Marca) obj;
		if (codigo_marca == null) {
			if (other.codigo_marca != null)
				return false;
		} else if (!codigo_marca.equals(other.codigo_marca))
			return false;
		return true;
	}

	public String getCodigo_marca() {
		return this.codigo_marca;
	}

	public void setCodigo_marca(String codigo_marca) {
		this.codigo_marca = codigo_marca;
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
