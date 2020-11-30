package co.edu.uniquindio.empresa;

import java.io.Serializable;
import java.lang.String;
import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: Vehiculo
 *
 */
@Entity
@NamedQueries({
	@NamedQuery(name = Vehiculo.FIND_BY_ID, query = "select v from Vehiculo v where v.codigo_vehiculo = :codigo"),
	@NamedQuery(name = Vehiculo.GETALL, query = "select v from Vehiculo v"),
	@NamedQuery(name = Vehiculo.FIND_BY_PLACA, query = "select v from Vehiculo v where v.placa = :placa"),
	@NamedQuery(name = Vehiculo.GET_ALL_COLOR, query = "select DISTINCT v.color from Vehiculo v"),
	@NamedQuery(name = Vehiculo.FIND_BY_COLOR, query = "select v from Vehiculo v where v.color = :color"),
	@NamedQuery(name = Vehiculo.FIND_BY_MIN_PRECIO, query = "select v from Vehiculo v where v.precio > :precio"),
	@NamedQuery(name = Vehiculo.FIND_BY_MAX_PRECIO, query = "select v from Vehiculo v where v.precio < :precio"),
	@NamedQuery(name = Vehiculo.FIND_BY_RANGO_PRECIO, query = "select v from Vehiculo v where v.precio between :precioMin and :precioMax"),
	@NamedQuery(name = Vehiculo.FIND_BY_ASCENDENTE_PRECIO, query = "select v from Vehiculo v order by v.precio ASC"),
	@NamedQuery(name = Vehiculo.FIND_BY_DESCENDENTE_PRECIO, query = "select v from Vehiculo v order by v.precio DESC")
})
public class Vehiculo implements Serializable {

	@Id
	@Column(name = "codigo_vehiculo")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long codigo_vehiculo;

	@Column(name = "descripcion", nullable = false)
	private String descripcion;

	@Column(name = "color", nullable = false)
	private String color;

	@Column(name = "placa", nullable = false)
	private String placa;

	@Column(name = "precio", nullable = false)
	private int precio;

	@Column(name = "year", nullable = false)
	private int year;

	@Column(name = "cilindraje", nullable = false)
	private int cilindraje;

	@Column(name = "numero_puertas")
	private int numero_puertas;

	@Column(name = "kilometros", nullable = false)
	private int kilometros;

	@JoinColumn(name = "codigo_ciudad", nullable = false)
	@ManyToOne
	private Ciudad codigo_ciudad;

	@JoinColumn(name = "codigo_dueno", nullable = false)
	@ManyToOne
	private Cliente codigo_dueno;

	@JoinColumn(name = "codigo_venta")
	@OneToOne(mappedBy = "codigo_vehiculo")
	private Venta codigo_venta;

	@JoinColumn(name = "codigo_modelo", nullable = false)
	@ManyToOne
	private Modelo codigo_modelo;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_combustible", nullable = false)
	private TipoCombustible tipo_combustible;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_vehiculo", nullable = false)
	private TipoVehiculo tipo_vehiculo;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_trasmision", nullable = false)
	private TipoTrasmision tipo_trasmision;

	@Enumerated(EnumType.STRING)
	@Column(name = "condicion", nullable = false)
	private Condicion condicion;

	@OneToMany(mappedBy = "vehiculo")
	private List<Pregunta> preguntas;

	@ElementCollection
	@CollectionTable(name = "foto_Vehiculo", joinColumns = @JoinColumn(name = "codigo_vehiculo"))
	@Column(name = "url_foto")
	private List<String> url_foto;

	@JoinTable(name = "favorito", joinColumns = @JoinColumn(name = "codigo_vehiculo"), inverseJoinColumns = @JoinColumn(name = "codigo_persona"))
	@ManyToMany(cascade = CascadeType.ALL)
	private List<Cliente> clientes;

	@ManyToMany(mappedBy = "vehiculos")
	private List<Caracteristica> caracteristicas;

	private static final long serialVersionUID = 1L;
	public static final String FIND_BY_ID = "Vehiculo_findById";
	public static final String GETALL = "Vehiculo_GetAll";
	public static final String FIND_BY_PLACA = "Vehiculo_findByPlaca";
	public static final String GET_ALL_COLOR = "Vehiculo_GetAllColor";
	public static final String FIND_BY_COLOR = "Vehiculo_findByColor";
	public static final String FIND_BY_MIN_PRECIO = "Vehiculo_findByMinPrecio";
	public static final String FIND_BY_MAX_PRECIO = "Vehiculo_findByMaxPrecio";
	public static final String FIND_BY_RANGO_PRECIO = "Vehiculo_findByRangoPrecio";
	public static final String FIND_BY_ASCENDENTE_PRECIO = "Vehiculo_findByAscendentePrecio";
	public static final String FIND_BY_DESCENDENTE_PRECIO = "Vehiculo_findByDescendentePrecio";

	public Vehiculo() {
		super();
	}

	/**
	 * @param descripcion
	 * @param color
	 * @param placa
	 * @param precio
	 * @param year
	 * @param cilindraje
	 * @param numero_puertas
	 * @param kilometros
	 * @param codigo_ciudad
	 * @param codigo_dueno
	 * @param codigo_venta
	 * @param codigo_modelo
	 * @param tipo_combustible
	 * @param tipo_vehiculo
	 * @param tipo_trasmision
	 * @param condicion
	 */
	public Vehiculo(String descripcion, String color, String placa, int precio, int year, int cilindraje,
			int numero_puertas, int kilometros, Ciudad codigo_ciudad, Cliente codigo_dueno, Venta codigo_venta,
			Modelo codigo_modelo, TipoCombustible tipo_combustible, TipoVehiculo tipo_vehiculo,
			TipoTrasmision tipo_trasmision, Condicion condicion) {
		super();
		this.descripcion = descripcion;
		this.color = color;
		this.placa = placa;
		this.precio = precio;
		this.year = year;
		this.cilindraje = cilindraje;
		this.numero_puertas = numero_puertas;
		this.kilometros = kilometros;
		this.codigo_ciudad = codigo_ciudad;
		this.codigo_dueno = codigo_dueno;
		this.codigo_venta = codigo_venta;
		this.codigo_modelo = codigo_modelo;
		this.tipo_combustible = tipo_combustible;
		this.tipo_vehiculo = tipo_vehiculo;
		this.tipo_trasmision = tipo_trasmision;
		this.condicion = condicion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo_vehiculo == null) ? 0 : codigo_vehiculo.hashCode());
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
		Vehiculo other = (Vehiculo) obj;
		if (codigo_vehiculo == null) {
			if (other.codigo_vehiculo != null)
				return false;
		} else if (!codigo_vehiculo.equals(other.codigo_vehiculo))
			return false;
		return true;
	}

	public Long getCodigo_vehiculo() {
		return this.codigo_vehiculo;
	}

	public void setCodigo_vehiculo(Long codigo_vehiculo) {
		this.codigo_vehiculo = codigo_vehiculo;
	}

	public int getPrecio() {
		return this.precio;
	}

	public void setPrecio(int precio) {
		this.precio = precio;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getColor() {
		return this.color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getYear() {
		return this.year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * @return the codigo_ciudad
	 */
	public Ciudad getCodigo_ciudad() {
		return codigo_ciudad;
	}

	/**
	 * @param codigo_ciudad the codigo_ciudad to set
	 */
	public void setCodigo_ciudad(Ciudad codigo_ciudad) {
		this.codigo_ciudad = codigo_ciudad;
	}

	/**
	 * @return the codigo_dueño
	 */
	public Cliente getCodigo_dueño() {
		return codigo_dueno;
	}

	/**
	 * @param codigo_dueño the codigo_dueño to set
	 */
	public void setCodigo_dueño(Cliente codigo_dueño) {
		this.codigo_dueno = codigo_dueño;
	}

	/**
	 * @return the codigo_venta
	 */
	public Venta getCodigo_venta() {
		return codigo_venta;
	}

	/**
	 * @param codigo_venta the codigo_venta to set
	 */
	public void setCodigo_venta(Venta codigo_venta) {
		this.codigo_venta = codigo_venta;
	}

	/**
	 * @return the codigo_modelo
	 */
	public Modelo getCodigo_modelo() {
		return codigo_modelo;
	}

	/**
	 * @param codigo_modelo the codigo_modelo to set
	 */
	public void setCodigo_modelo(Modelo codigo_modelo) {
		this.codigo_modelo = codigo_modelo;
	}

	/**
	 * @return the tipo_combustible
	 */
	public TipoCombustible getTipo_combustible() {
		return tipo_combustible;
	}

	/**
	 * @param tipo_combustible the tipo_combustible to set
	 */
	public void setTipo_combustible(TipoCombustible tipo_combustible) {
		this.tipo_combustible = tipo_combustible;
	}

	/**
	 * @return the tipo_vehiculo
	 */
	public TipoVehiculo getTipo_vehiculo() {
		return tipo_vehiculo;
	}

	/**
	 * @param tipo_vehiculo the tipo_vehiculo to set
	 */
	public void setTipo_vehiculo(TipoVehiculo tipo_vehiculo) {
		this.tipo_vehiculo = tipo_vehiculo;
	}

	/**
	 * @return the preguntas
	 */
	public List<Pregunta> getPreguntas() {
		return preguntas;
	}

	/**
	 * @param preguntas the preguntas to set
	 */
	public void setPreguntas(List<Pregunta> preguntas) {
		this.preguntas = preguntas;
	}

	/**
	 * @return the url_foto
	 */
	public List<String> getUrl_foto() {
		return url_foto;
	}

	/**
	 * @param url_foto the url_foto to set
	 */
	public void setUrl_foto(List<String> url_foto) {
		this.url_foto = url_foto;
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
	 * @return the caracteristicas
	 */
	public List<Caracteristica> getCaracteristicas() {
		return caracteristicas;
	}

	/**
	 * @param caracteristicas the caracteristicas to set
	 */
	public void setCaracteristicas(List<Caracteristica> caracteristicas) {
		this.caracteristicas = caracteristicas;
	}

	/**
	 * @return the placa
	 */
	public String getPlaca() {
		return placa;
	}

	/**
	 * @param placa the placa to set
	 */
	public void setPlaca(String placa) {
		this.placa = placa;
	}

	/**
	 * @return the cilindraje
	 */
	public int getCilindraje() {
		return cilindraje;
	}

	/**
	 * @param cilindraje the cilindraje to set
	 */
	public void setCilindraje(int cilindraje) {
		this.cilindraje = cilindraje;
	}

	/**
	 * @return the numero_puertas
	 */
	public int getNumero_puertas() {
		return numero_puertas;
	}

	/**
	 * @param numero_puertas the numero_puertas to set
	 */
	public void setNumero_puertas(int numero_puertas) {
		this.numero_puertas = numero_puertas;
	}

	/**
	 * @return the kilometros
	 */
	public int getKilometros() {
		return kilometros;
	}

	/**
	 * @param kilometros the kilometros to set
	 */
	public void setKilometros(int kilometros) {
		this.kilometros = kilometros;
	}

	/**
	 * @return the codigo_dueno
	 */
	public Cliente getCodigo_dueno() {
		return codigo_dueno;
	}

	/**
	 * @param codigo_dueno the codigo_dueno to set
	 */
	public void setCodigo_dueno(Cliente codigo_dueno) {
		this.codigo_dueno = codigo_dueno;
	}

	/**
	 * @return the tipo_trasmision
	 */
	public TipoTrasmision getTipo_trasmision() {
		return tipo_trasmision;
	}

	/**
	 * @param tipo_trasmision the tipo_trasmision to set
	 */
	public void setTipo_trasmision(TipoTrasmision tipo_trasmision) {
		this.tipo_trasmision = tipo_trasmision;
	}

	/**
	 * @return the condicion
	 */
	public Condicion getCondicion() {
		return condicion;
	}

	/**
	 * @param condicion the condicion to set
	 */
	public void setCondicion(Condicion condicion) {
		this.condicion = condicion;
	}

	@Override
	public String toString() {
		return "Vehiculo [codigo_vehiculo=" + codigo_vehiculo + ", descripcion=" + descripcion + ", placa=" + placa
				+ "]";
	}

}
