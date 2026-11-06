package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.lang.String;
import java.util.List;

import javax.persistence.*;

/**
 * Característica o equipamiento que puede tener un vehículo (aire acondicionado, GPS, cojines...).
 * Sirve para filtrar vehículos en el portal.
 * <p>
 * Tabla: {@code Caracteristica}. Relación muchos a muchos con {@link Vehiculo} (tabla intermedia
 * {@code caracteristica_vehiculo}, de la cual {@link Vehiculo} es el lado propietario).
 */
@Entity
@NamedQueries({
		@NamedQuery(name = Caracteristica.GETALL, query = "select c from Caracteristica c"),
		@NamedQuery(name = Caracteristica.CARACTERISTICA_POR_NOMBRE, query = "select c from Caracteristica c where c.nombre = :nombre"),
		@NamedQuery(name = Caracteristica.CARACTERISTICAS_ORDENADAS, query = "select c from Caracteristica c order by c.nombre"),
		@NamedQuery(name = Caracteristica.ESTADISTICA_CARACTERISTICAS_FRECUENTES, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(c.nombre, count(v)) from Caracteristica c join c.vehiculos v group by c.nombre order by count(v) desc") })

public class Caracteristica implements Serializable {

	/**
	 * Identificador único autogenerado (clave primaria).
	 */
	@Id
	@Column(name = "codigo_caracteristica")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long codigo_caracteristica;

	/**
	 * Nombre de la característica; obligatorio, único, máximo 50 caracteres.
	 */
	@Column(name = "nombre", nullable = false, unique = true, length = 50)
	private String nombre;

	/**
	 * Vehículos que tienen esta característica (lado inverso de la relación muchos a muchos).
	 */
	@ManyToMany(mappedBy = "caracteristicas")
	private List<Vehiculo> vehiculos;

	/**
	 * Versión de serialización.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * NamedQuery que busca una característica por nombre.
	 * Parámetro: {@code :nombre} (String). Devuelve la entidad {@link Caracteristica}.
	 */
	public static final String CARACTERISTICA_POR_NOMBRE = "Caracteristica_CARACTERISTICA_POR_NOMBRE";
	/**
	 * NamedQuery sin parámetros que lista todas las características ordenadas alfabéticamente por nombre.
	 */
	public static final String CARACTERISTICAS_ORDENADAS = "Caracteristica_CARACTERISTICAS_ORDENADAS";
	/**
	 * NamedQuery de estadística sin parámetros: número de vehículos por característica, de mayor a menor.
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = nombre, valor = conteo).
	 */
	public static final String ESTADISTICA_CARACTERISTICAS_FRECUENTES = "Caracteristica_ESTADISTICA_CARACTERISTICAS_FRECUENTES";
	/**
	 * NamedQuery sin parámetros que devuelve todas las {@link Caracteristica}.
	 */
	public static final String GETALL = "Caracteristica_GETALL";

	/**
	 * Constructor vacío requerido por JPA.
	 */
	public Caracteristica() {
		super();
	}

	/**
	 * Crea una característica.
	 *
	 * @param nombre nombre único de la característica
	 */
	public Caracteristica(String nombre) {
		super();
		this.nombre = nombre;
	}

	/**
	 * Calcula el hash a partir del identificador {@code codigo_caracteristica}.
	 *
	 * @return código hash de la característica
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo_caracteristica == null) ? 0 : codigo_caracteristica.hashCode());
		return result;
	}

	/**
	 * Dos características son iguales si son de la misma clase y tienen el mismo {@code codigo_caracteristica}.
	 *
	 * @param obj objeto a comparar
	 * @return {@code true} si representan la misma característica
	 */
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

	/**
	 * Obtiene el valor de {@code Codigo_caracteristica}.
	 *
	 * @return valor de {@code Codigo_caracteristica}
	 */
	public Long getCodigo_caracteristica() {
		return this.codigo_caracteristica;
	}

	/**
	 * Establece el valor de {@code Codigo_caracteristica}.
	 *
	 * @param codigo_caracteristica nuevo valor
	 */
	public void setCodigo_caracteristica(Long codigo_caracteristica) {
		this.codigo_caracteristica = codigo_caracteristica;
	}

	/**
	 * Obtiene el valor de {@code Nombre}.
	 *
	 * @return valor de {@code Nombre}
	 */
	public String getNombre() {
		return this.nombre;
	}

	/**
	 * Establece el valor de {@code Nombre}.
	 *
	 * @param nombre nuevo valor
	 */
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

	/**
	 * Representación en texto de la característica.
	 *
	 * @return texto descriptivo del objeto
	 */
	@Override
	public String toString() {
		return "Caracteristica [codigo_caracteristica=" + codigo_caracteristica + ", nombre=" + nombre + "]";
	}

}
