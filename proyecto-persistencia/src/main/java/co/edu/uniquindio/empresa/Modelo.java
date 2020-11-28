package co.edu.uniquindio.empresa;

import java.io.Serializable;
import java.lang.String;
import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: Modelo
 *
 */
@Entity
public class Modelo implements Serializable {

	@Id
	@Column(name = "codigo_modelo")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long codigo_modelo;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	@Column(name = "marca", nullable = false)
	private String marca;

	@OneToMany(mappedBy = "codigo_modelo")
	private List<Vehiculo> vehiculos;

	private static final long serialVersionUID = 1L;

	public Modelo() {
		super();
	}

	public Modelo(String nombre, String marca) {
		super();
		this.nombre = nombre;
		this.marca = marca;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo_modelo == null) ? 0 : codigo_modelo.hashCode());
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
		Modelo other = (Modelo) obj;
		if (codigo_modelo == null) {
			if (other.codigo_modelo != null)
				return false;
		} else if (!codigo_modelo.equals(other.codigo_modelo))
			return false;
		return true;
	}

	public Long getCodigo_modelo() {
		return this.codigo_modelo;
	}

	public void setCodigo_modelo(Long codigo_modelo) {
		this.codigo_modelo = codigo_modelo;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Vehiculo> getVehiculos() {
		return this.vehiculos;
	}

	public void setVehiculos(List<Vehiculo> vehiculos) {
		this.vehiculos = vehiculos;
	}

	/**
	 * @return the marca
	 */
	public String getMarca() {
		return marca;
	}

	/**
	 * @param marca the marca to set
	 */
	public void setMarca(String marca) {
		this.marca = marca;
	}

}
