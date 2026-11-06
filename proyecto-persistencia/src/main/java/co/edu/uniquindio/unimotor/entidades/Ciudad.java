package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

/**
 * Ciudad donde vive un cliente o donde está ubicado un vehículo publicado.
 * <p>
 * Tabla: {@code Ciudad}. Relaciones uno a muchos con {@link Cliente} y {@link Vehiculo}.
 */
@Entity
@NamedQueries({
		@NamedQuery(name = Ciudad.CIUDADES_ORDENADAS, query = "select c from Ciudad c order by c.nombre, c.departamento"),
		@NamedQuery(name = Ciudad.CIUDAD_POR_NOMBRE, query = "select c from Ciudad c where c.nombre = :nombre"),
		@NamedQuery(name = Ciudad.CIUDAD_POR_NOMBRE_Y_DEPARTAMENTO, query = "select c from Ciudad c where c.nombre = :nombre and c.departamento = :departamento") })

public class Ciudad implements Serializable {

	/**
	 * Identificador único autogenerado (clave primaria).
	 */
	@Id
	@Column(name = "codigo_ciudad")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long codigo_ciudad;

	/**
	 * Nombre de la ciudad; obligatorio, máximo 100 caracteres.
	 */
	@Column(name = "nombre", nullable = false, length = 100)
	private String nombre;

	/**
	 * Departamento al que pertenece la ciudad; opcional, máximo 100 caracteres.
	 * Distingue municipios homónimos de distintos departamentos.
	 */
	@Column(name = "departamento", length = 100)
	private String departamento;

	/**
	 * Clientes que residen en la ciudad (lado inverso de {@link Cliente#getCodigo_ciudad()}).
	 */
	@OneToMany(mappedBy = "codigo_ciudad")
	private List<Cliente> clientes;

	/**
	 * Vehículos ubicados en la ciudad (lado inverso de {@link Vehiculo#getCodigo_ciudad()}).
	 */
	@OneToMany(mappedBy = "codigo_ciudad")
	private List<Vehiculo> vehiculos;

	/**
	 * Versión de serialización.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * NamedQuery sin parámetros que lista todas las ciudades ordenadas alfabéticamente por nombre.
	 */
	public static final String CIUDADES_ORDENADAS = "Ciudad_CIUDADES_ORDENADAS";
	/**
	 * NamedQuery que busca una ciudad por nombre.
	 * Parámetro: {@code :nombre} (String). Devuelve la entidad {@link Ciudad}.
	 */
	public static final String CIUDAD_POR_NOMBRE = "Ciudad_CIUDAD_POR_NOMBRE";
	/**
	 * NamedQuery que busca una ciudad por nombre y departamento.
	 * Parámetros: {@code :nombre} (String) y {@code :departamento} (String). Devuelve la entidad {@link Ciudad}.
	 */
	public static final String CIUDAD_POR_NOMBRE_Y_DEPARTAMENTO = "Ciudad_CIUDAD_POR_NOMBRE_Y_DEPARTAMENTO";

	/**
	 * Constructor vacío requerido por JPA.
	 */
	public Ciudad() {
		super();
	}

	/**
	 * Crea una ciudad.
	 *
	 * @param nombre nombre de la ciudad
	 */
	public Ciudad(String nombre) {
		super();
		this.nombre = nombre;
	}

	/**
	 * Crea una ciudad indicando su departamento.
	 *
	 * @param nombre       nombre de la ciudad
	 * @param departamento departamento al que pertenece
	 */
	public Ciudad(String nombre, String departamento) {
		super();
		this.nombre = nombre;
		this.departamento = departamento;
	}

	/**
	 * Calcula el hash a partir del identificador {@code codigo_ciudad}.
	 *
	 * @return código hash de la ciudad
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo_ciudad == null) ? 0 : codigo_ciudad.hashCode());
		return result;
	}

	/**
	 * Dos ciudades son iguales si son de la misma clase y tienen el mismo {@code codigo_ciudad}.
	 *
	 * @param obj objeto a comparar
	 * @return {@code true} si representan la misma ciudad
	 */
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

	/**
	 * Obtiene el valor de {@code Codigo_ciudad}.
	 *
	 * @return valor de {@code Codigo_ciudad}
	 */
	public Long getCodigo_ciudad() {
		return this.codigo_ciudad;
	}

	/**
	 * Establece el valor de {@code Codigo_ciudad}.
	 *
	 * @param codigo_ciudad nuevo valor
	 */
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
	 * @return the departamento
	 */
	public String getDepartamento() {
		return departamento;
	}

	/**
	 * @param departamento the departamento to set
	 */
	public void setDepartamento(String departamento) {
		this.departamento = departamento;
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

	/**
	 * Representación en texto de la ciudad.
	 *
	 * @return texto descriptivo del objeto
	 */
	@Override
	public String toString() {
		return "Ciudad [codigo_ciudad=" + codigo_ciudad + ", nombre=" + nombre + ", departamento=" + departamento + "]";
	}

}
