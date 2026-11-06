package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.lang.String;
import java.util.List;

import javax.persistence.*;

/**
 * Modelo de vehículo (por ejemplo "Mazda 3"): combina marca y nombre del modelo y se reutiliza
 * en todos los vehículos publicados de ese tipo.
 * <p>
 * Tabla: {@code Modelo}. Relación uno a muchos con {@link Vehiculo}.
 */
@Entity
@NamedQueries({
		@NamedQuery(name = Modelo.GETALL, query = "select m from Modelo m order by m.marca, m.nombre"),
		@NamedQuery(name = Modelo.MARCAS, query = "select distinct m.marca from Modelo m order by m.marca"),
		@NamedQuery(name = Modelo.MODELOS_POR_MARCA, query = "select m from Modelo m where m.marca = :marca order by m.nombre") })
public class Modelo implements Serializable {

	/**
	 * Identificador único autogenerado (clave primaria).
	 */
	@Id
	@Column(name = "codigo_modelo")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long codigo_modelo;

	/**
	 * Nombre del modelo; obligatorio, máximo 50 caracteres.
	 */
	@Column(name = "nombre", nullable = false, length = 50)
	private String nombre;

	/**
	 * Marca del modelo; obligatoria, máximo 50 caracteres.
	 */
	@Column(name = "marca", nullable = false, length = 50)
	private String marca;

	/**
	 * Vehículos de este modelo (lado inverso de {@link Vehiculo#getCodigo_modelo()}).
	 */
	@OneToMany(mappedBy = "codigo_modelo")
	private List<Vehiculo> vehiculos;

	/**
	 * Versión de serialización.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * NamedQuery sin parámetros que devuelve todos los {@link Modelo} ordenados por marca y nombre.
	 */
	public static final String GETALL = "Modelo_GETALL";
	/**
	 * NamedQuery sin parámetros que devuelve las marcas distintas (String), ordenadas alfabéticamente.
	 */
	public static final String MARCAS = "Modelo_MARCAS";
	/**
	 * NamedQuery que lista los modelos de una marca ordenados por nombre.
	 * Parámetro: {@code :marca} (String). Devuelve entidades {@link Modelo}.
	 */
	public static final String MODELOS_POR_MARCA = "Modelo_MODELOS_POR_MARCA";

	/**
	 * Constructor vacío requerido por JPA.
	 */
	public Modelo() {
		super();
	}

	/**
	 * Crea un modelo.
	 *
	 * @param nombre nombre del modelo
	 * @param marca marca del modelo
	 */
	public Modelo(String nombre, String marca) {
		super();
		this.nombre = nombre;
		this.marca = marca;
	}

	/**
	 * Calcula el hash a partir del identificador {@code codigo_modelo}.
	 *
	 * @return código hash del modelo
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo_modelo == null) ? 0 : codigo_modelo.hashCode());
		return result;
	}

	/**
	 * Dos modelos son iguales si son de la misma clase y tienen el mismo {@code codigo_modelo}.
	 *
	 * @param obj objeto a comparar
	 * @return {@code true} si representan el mismo modelo
	 */
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

	/**
	 * Obtiene el valor de {@code Codigo_modelo}.
	 *
	 * @return valor de {@code Codigo_modelo}
	 */
	public Long getCodigo_modelo() {
		return this.codigo_modelo;
	}

	/**
	 * Establece el valor de {@code Codigo_modelo}.
	 *
	 * @param codigo_modelo nuevo valor
	 */
	public void setCodigo_modelo(Long codigo_modelo) {
		this.codigo_modelo = codigo_modelo;
	}

	/**
	 * Representación en texto del modelo.
	 *
	 * @return texto descriptivo del objeto
	 */
	@Override
	public String toString() {
		return "Modelo [codigo_modelo=" + codigo_modelo + ", nombre=" + nombre + ", marca=" + marca + "]";
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
	 * Obtiene el valor de {@code Vehiculos}.
	 *
	 * @return valor de {@code Vehiculos}
	 */
	public List<Vehiculo> getVehiculos() {
		return this.vehiculos;
	}

	/**
	 * Establece el valor de {@code Vehiculos}.
	 *
	 * @param vehiculos nuevo valor
	 */
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
