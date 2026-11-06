package co.edu.uniquindio.unimotor.entidades;

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
		@NamedQuery(name = Caracteristica.GETALL, query = "select c from Caracteristica c"),
		@NamedQuery(name = Caracteristica.CARACTERISTICA_POR_NOMBRE, query = "select c from Caracteristica c where c.nombre = :nombre"),
		@NamedQuery(name = Caracteristica.CARACTERISTICAS_ORDENADAS, query = "select c from Caracteristica c order by c.nombre"),
		@NamedQuery(name = Caracteristica.ESTADISTICA_CARACTERISTICAS_FRECUENTES, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(c.nombre, count(v)) from Caracteristica c join c.vehiculos v group by c.nombre order by count(v) desc") })

public class Caracteristica implements Serializable {

	@Id
	@Column(name = "codigo_caracteristica")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long codigo_caracteristica;

	@Column(name = "nombre", nullable = false, unique = true, length = 50)
	private String nombre;

	@ManyToMany(mappedBy = "caracteristicas")
	private List<Vehiculo> vehiculos;

	private static final long serialVersionUID = 1L;
	public static final String CARACTERISTICA_POR_NOMBRE = "Caracteristica_CARACTERISTICA_POR_NOMBRE";
	public static final String CARACTERISTICAS_ORDENADAS = "Caracteristica_CARACTERISTICAS_ORDENADAS";
	public static final String ESTADISTICA_CARACTERISTICAS_FRECUENTES = "Caracteristica_ESTADISTICA_CARACTERISTICAS_FRECUENTES";
	public static final String GETALL = "Caracteristica_GETALL";

	public Caracteristica() {
		super();
	}

	public Caracteristica(String nombre) {
		super();
		this.nombre = nombre;
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

	public Long getCodigo_caracteristica() {
		return this.codigo_caracteristica;
	}

	public void setCodigo_caracteristica(Long codigo_caracteristica) {
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

	@Override
	public String toString() {
		return "Caracteristica [codigo_caracteristica=" + codigo_caracteristica + ", nombre=" + nombre + "]";
	}

}
