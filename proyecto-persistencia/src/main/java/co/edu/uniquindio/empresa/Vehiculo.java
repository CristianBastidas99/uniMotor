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

public class Vehiculo implements Serializable {

	@Id
	@Column(name = "codigo_vehiculo")
	private String codigo_vehiculo;

	@Column(name = "precio", nullable = false)
	private int precio;

	@Column(name = "descripcion", nullable = false)
	private String descripcion;

	@Column(name = "color", nullable = false)
	private String color;

	@Column(name = "year", nullable = false)
	private int year;

	@JoinColumn(name = "codigo_ciudad", nullable = false)
	@ManyToOne
	private Ciudad codigo_ciudad;

	@JoinColumn(name = "codigo_dueño", nullable = false)
	@ManyToOne
	private Cliente codigo_dueño;

	@JoinColumn(name = "codigo_venta")
	@OneToOne(mappedBy = "codigo_vehiculo")
	private Venta codigo_venta;

	@JoinColumn(name = "codigo_marca", nullable = false)
	@ManyToOne
	private Marca codigo_marca;

	@JoinColumn(name = "codigo_modelo", nullable = false)
	@ManyToOne
	private Modelo codigo_modelo;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_combustible", nullable = false)
	private TipoCombustible tipo_combustible;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_vehiculo", nullable = false)
	private TipoVehiculo tipo_vehiculo;

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

	public Vehiculo() {
		super();
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

	public String getCodigo_vehiculo() {
		return this.codigo_vehiculo;
	}

	public void setCodigo_vehiculo(String codigo_vehiculo) {
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
		return codigo_dueño;
	}

	/**
	 * @param codigo_dueño the codigo_dueño to set
	 */
	public void setCodigo_dueño(Cliente codigo_dueño) {
		this.codigo_dueño = codigo_dueño;
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
	 * @return the codigo_marca
	 */
	public Marca getCodigo_marca() {
		return codigo_marca;
	}

	/**
	 * @param codigo_marca the codigo_marca to set
	 */
	public void setCodigo_marca(Marca codigo_marca) {
		this.codigo_marca = codigo_marca;
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

}
