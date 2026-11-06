package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.lang.String;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: Persona
 *
 */
@Entity
@NamedQueries({
		@NamedQuery(name = Cliente.FIND_BY_ID, query = "select c from Cliente c where c.codigo_usuario = :codigo"),
		@NamedQuery(name = Cliente.GETALL, query = "select c from Cliente c"),
		@NamedQuery(name = Cliente.CANTIDAD_DE_CLIENTES, query = "select count(c) from Cliente c"),
		@NamedQuery(name = Cliente.LISTA_FAVORITO_CLIENTE, query = "select f from Cliente c, IN (c.vehiculos_favoritos) f where c.email = :email"),
		@NamedQuery(name = Cliente.LISTA_FAVORITO_CLIENTE2, query = "select f, c.nombre_completo from Cliente c, IN (c.vehiculos_favoritos) f where c.email = :email"),
		@NamedQuery(name = Cliente.LISTA_FAVORITO_CLIENTE_JOIN, query = "select f from Cliente c join (c.vehiculos_favoritos) f where c.email = :email"),
		@NamedQuery(name = Cliente.LISTA_VEHICULOS_VENTA, query = "select c.email, f from Cliente c join (c.vehiculos_venta) f"),
		@NamedQuery(name = Cliente.LISTA_CIUDAD_CLIENTE, query = "select ci, c from Cliente c join (c.codigo_ciudad) ci "),
		@NamedQuery(name = Cliente.FIND_BY_EMAIL, query = "select c from Cliente c where c.email = :email"),
		@NamedQuery(name = Cliente.ESTADISTICA_CLIENTES_POR_CIUDAD, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(ci.nombre, count(c)) from Cliente c join c.codigo_ciudad ci group by ci.nombre order by count(c) desc") })
public class Cliente extends Usuario implements Serializable {

	@Column(name = "nombre_completo", nullable = false, length = 100)
	private String nombre_completo;

	@Column(name = "direccion", nullable = false, length = 200)
	private String direccion;

	@ElementCollection
	@CollectionTable(name = "telefono_usuario", joinColumns = @JoinColumn(name = "codigo_usuario"))
	@Column(name = "telefono")
	private Map<String, String> telefono;

	@JoinColumn(name = "codigo_ciudad", nullable = false)
	@ManyToOne
	private Ciudad codigo_ciudad;

	@OneToMany(mappedBy = "cliente")
	private List<Pregunta> preguntas;

	@ManyToMany(mappedBy = "clientes")
	private List<Vehiculo> vehiculos_favoritos;

	@OneToMany(mappedBy = "codigo_dueno")
	private List<Vehiculo> vehiculos_venta;

	@OneToMany(mappedBy = "codigo_comprador")
	private List<Venta> mis_compras;

	@OneToMany(mappedBy = "vendedor")
	private List<Calificacion> calificaciones_recibidas;

	@OneToMany(mappedBy = "calificador")
	private List<Calificacion> calificaciones_hechas;

	private static final long serialVersionUID = 1L;
	public static final String FIND_BY_EMAIL = "Cliente_FIND_BY_EMAIL";
	public static final String ESTADISTICA_CLIENTES_POR_CIUDAD = "Cliente_ESTADISTICA_CLIENTES_POR_CIUDAD";
	public static final String LISTA_FAVORITO_CLIENTE = "Cliente_LISTA_FAVORITO_CLIENTE";
	public static final String LISTA_FAVORITO_CLIENTE2 = "Cliente_LISTA_FAVORITO_CLIENTE2";
	public static final String LISTA_FAVORITO_CLIENTE_JOIN = "Cliente_LISTA_FAVORITO_CLIENTE_JOIN";
	public static final String LISTA_VEHICULOS_VENTA = "Cliente_LISTA_VEHICULOS_VENTA";
	public static final String LISTA_CIUDAD_CLIENTE = "Cliente_LISTA_CIUDAD_CLIENTE";
	public static final String FIND_BY_ID = "Cliente_FIND_BY_ID";
	public static final String GETALL = "Cliente_GETALL";
	public static final String CANTIDAD_DE_CLIENTES = "Cliente_CANTIDAD_DE_CLIENTES";

	public Cliente() {
		super();
	}

	/**
	 * @param email
	 * @param password
	 * @param nombre_completo
	 * @param direccion
	 * @param telefono
	 * @param codigo_ciudad
	 */
	public Cliente(String email, String password, String nombre_completo, String direccion,
			Map<String, String> telefono, Ciudad codigo_ciudad) {
		super(email, password);
		this.nombre_completo = nombre_completo;
		this.direccion = direccion;
		this.telefono = telefono;
		this.codigo_ciudad = codigo_ciudad;
	}

	/**
	 * @return the telefonos
	 */
	public Map<String, String> getTelefonos() {
		return telefono;
	}

	/**
	 * @param telefonos the telefonos to set
	 */
	public void setTelefonos(Map<String, String> telefonos) {
		this.telefono = telefonos;
	}

	public String getNombre() {
		return this.nombre_completo;
	}

	public void setNombre(String nombre) {
		this.nombre_completo = nombre;
	}

	public String getDireccion() {
		return this.direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	/**
	 * @return the nombre_completo
	 */
	public String getNombre_completo() {
		return nombre_completo;
	}

	/**
	 * @param nombre_completo the nombre_completo to set
	 */
	public void setNombre_completo(String nombre_completo) {
		this.nombre_completo = nombre_completo;
	}

	/**
	 * @return the telefono
	 */
	public Map<String, String> getTelefono() {
		return telefono;
	}

	/**
	 * @param telefono the telefono to set
	 */
	public void setTelefono(Map<String, String> telefono) {
		this.telefono = telefono;
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
	 * @return the vehiculos_favoritos
	 */
	public List<Vehiculo> getVehiculos_favoritos() {
		return vehiculos_favoritos;
	}

	/**
	 * @param vehiculos_favoritos the vehiculos_favoritos to set
	 */
	public void setVehiculos_favoritos(List<Vehiculo> vehiculos_favoritos) {
		this.vehiculos_favoritos = vehiculos_favoritos;
	}

	/**
	 * @return the vehiculos_venta
	 */
	public List<Vehiculo> getVehiculos_venta() {
		return vehiculos_venta;
	}

	/**
	 * @param vehiculos_venta the vehiculos_venta to set
	 */
	public void setVehiculos_venta(List<Vehiculo> vehiculos_venta) {
		this.vehiculos_venta = vehiculos_venta;
	}

	/**
	 * @return the mis_compras
	 */
	public List<Venta> getMis_compras() {
		return mis_compras;
	}

	/**
	 * @param mis_compras the mis_compras to set
	 */
	public void setMis_compras(List<Venta> mis_compras) {
		this.mis_compras = mis_compras;
	}

	public List<Calificacion> getCalificaciones_recibidas() {
		return calificaciones_recibidas;
	}

	public void setCalificaciones_recibidas(List<Calificacion> calificaciones_recibidas) {
		this.calificaciones_recibidas = calificaciones_recibidas;
	}

	public List<Calificacion> getCalificaciones_hechas() {
		return calificaciones_hechas;
	}

	public void setCalificaciones_hechas(List<Calificacion> calificaciones_hechas) {
		this.calificaciones_hechas = calificaciones_hechas;
	}

	@Override
	public String toString() {
		return "Cliente [codigo_usuario=" + getCodigo_usuario() + ", email=" + getEmail() + ", nombre_completo="
				+ nombre_completo + ", direccion=" + direccion + "]";
	}

}
