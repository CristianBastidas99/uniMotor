package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.lang.String;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

/**
 * Cliente del portal uniMotor: persona que publica vehículos para vender, hace preguntas, marca
 * favoritos, compra y califica a otros vendedores.
 * <p>
 * Tabla: {@code Cliente} (herencia {@code JOINED} de {@link Usuario}, unida por {@code codigo_usuario});
 * los teléfonos se guardan en la tabla {@code telefono_usuario}.
 * Relaciones: muchos a uno con {@link Ciudad}; uno a muchos con {@link Pregunta}, {@link Venta} (compras) y
 * {@link Calificacion} (recibidas y hechas); muchos a muchos con {@link Vehiculo} (favoritos).
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

	/**
	 * Nombre completo del cliente; obligatorio, máximo 100 caracteres.
	 */
	@Column(name = "nombre_completo", nullable = false, length = 100)
	private String nombre_completo;

	/**
	 * Dirección de residencia; obligatoria, máximo 200 caracteres.
	 */
	@Column(name = "direccion", nullable = false, length = 200)
	private String direccion;

	/**
	 * Teléfonos del cliente como mapa (tipo, por ejemplo "celular", a número); se guarda en {@code telefono_usuario}.
	 */
	@ElementCollection
	@CollectionTable(name = "telefono_usuario", joinColumns = @JoinColumn(name = "codigo_usuario"))
	@Column(name = "telefono")
	private Map<String, String> telefono;

	/**
	 * Ciudad de residencia; obligatoria.
	 */
	@JoinColumn(name = "codigo_ciudad", nullable = false)
	@ManyToOne
	private Ciudad codigo_ciudad;

	/**
	 * Preguntas que ha hecho el cliente (lado inverso de {@link Pregunta#getCliente()}).
	 */
	@OneToMany(mappedBy = "cliente")
	private List<Pregunta> preguntas;

	/**
	 * Vehículos que el cliente marcó como favoritos (lado inverso de {@link Vehiculo#getClientes()}).
	 */
	@ManyToMany(mappedBy = "clientes")
	private List<Vehiculo> vehiculos_favoritos;

	/**
	 * Vehículos que el cliente tiene publicados para la venta (lado inverso de {@link Vehiculo#getCodigo_dueno()}).
	 */
	@OneToMany(mappedBy = "codigo_dueno")
	private List<Vehiculo> vehiculos_venta;

	/**
	 * Compras realizadas por el cliente (lado inverso de la relación con {@link Venta}).
	 */
	@OneToMany(mappedBy = "codigo_comprador")
	private List<Venta> mis_compras;

	/**
	 * Calificaciones que otros clientes le dieron como vendedor.
	 */
	@OneToMany(mappedBy = "vendedor")
	private List<Calificacion> calificaciones_recibidas;

	/**
	 * Calificaciones que este cliente le dio a otros vendedores.
	 */
	@OneToMany(mappedBy = "calificador")
	private List<Calificacion> calificaciones_hechas;

	/**
	 * Versión de serialización.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * NamedQuery que busca un cliente por correo.
	 * Parámetro: {@code :email} (String). Devuelve la entidad {@link Cliente}.
	 */
	public static final String FIND_BY_EMAIL = "Cliente_FIND_BY_EMAIL";
	/**
	 * NamedQuery de estadística sin parámetros: número de clientes por ciudad, de mayor a menor.
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = ciudad, valor = conteo).
	 */
	public static final String ESTADISTICA_CLIENTES_POR_CIUDAD = "Cliente_ESTADISTICA_CLIENTES_POR_CIUDAD";
	/**
	 * NamedQuery que lista los vehículos favoritos de un cliente (con {@code IN}).
	 * Parámetro: {@code :email} (String). Devuelve entidades {@link Vehiculo}.
	 */
	public static final String LISTA_FAVORITO_CLIENTE = "Cliente_LISTA_FAVORITO_CLIENTE";
	/**
	 * NamedQuery como {@link #LISTA_FAVORITO_CLIENTE} pero devolviendo también el nombre del cliente.
	 * Parámetro: {@code :email} (String). Devuelve filas {@code Object[]} (vehículo, nombre completo).
	 */
	public static final String LISTA_FAVORITO_CLIENTE2 = "Cliente_LISTA_FAVORITO_CLIENTE2";
	/**
	 * NamedQuery equivalente a {@link #LISTA_FAVORITO_CLIENTE} usando {@code JOIN}.
	 * Parámetro: {@code :email} (String). Devuelve entidades {@link Vehiculo}.
	 */
	public static final String LISTA_FAVORITO_CLIENTE_JOIN = "Cliente_LISTA_FAVORITO_CLIENTE_JOIN";
	/**
	 * NamedQuery sin parámetros que lista, por cada cliente, los vehículos que tiene en venta.
	 * Devuelve filas {@code Object[]} (email del cliente, vehículo).
	 */
	public static final String LISTA_VEHICULOS_VENTA = "Cliente_LISTA_VEHICULOS_VENTA";
	/**
	 * NamedQuery sin parámetros que devuelve cada cliente junto con su ciudad.
	 * Devuelve filas {@code Object[]} (ciudad, cliente).
	 */
	public static final String LISTA_CIUDAD_CLIENTE = "Cliente_LISTA_CIUDAD_CLIENTE";
	/**
	 * NamedQuery que busca un cliente por su identificador.
	 * Parámetro: {@code :codigo} (Long, {@code codigo_usuario}). Devuelve la entidad {@link Cliente}.
	 */
	public static final String FIND_BY_ID = "Cliente_FIND_BY_ID";
	/**
	 * NamedQuery sin parámetros que devuelve todos los {@link Cliente}.
	 */
	public static final String GETALL = "Cliente_GETALL";
	/**
	 * NamedQuery sin parámetros que devuelve la cantidad total de clientes ({@link Long}).
	 */
	public static final String CANTIDAD_DE_CLIENTES = "Cliente_CANTIDAD_DE_CLIENTES";

	/**
	 * Constructor vacío requerido por JPA.
	 */
	public Cliente() {
		super();
	}

	/**
	 * Crea un cliente.
	 *
	 * @param email correo electrónico (único)
	 * @param password contraseña
	 * @param nombre_completo nombre completo
	 * @param direccion dirección de residencia
	 * @param telefono teléfonos (tipo a número)
	 * @param codigo_ciudad ciudad de residencia
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
	 * Alias de {@link #getTelefono()}.
	 *
	 * @return teléfonos del cliente (tipo a número)
	 */
	public Map<String, String> getTelefonos() {
		return telefono;
	}

	/**
	 * Alias de {@link #setTelefono(Map)}.
	 *
	 * @param telefonos teléfonos del cliente (tipo a número)
	 */
	public void setTelefonos(Map<String, String> telefonos) {
		this.telefono = telefonos;
	}

	/**
	 * Alias de {@link #getNombre_completo()}.
	 *
	 * @return nombre completo del cliente
	 */
	public String getNombre() {
		return this.nombre_completo;
	}

	/**
	 * Alias de {@link #setNombre_completo(String)}.
	 *
	 * @param nombre nombre completo del cliente
	 */
	public void setNombre(String nombre) {
		this.nombre_completo = nombre;
	}

	/**
	 * Obtiene el valor de {@code Direccion}.
	 *
	 * @return valor de {@code Direccion}
	 */
	public String getDireccion() {
		return this.direccion;
	}

	/**
	 * Establece el valor de {@code Direccion}.
	 *
	 * @param direccion nuevo valor
	 */
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

	/**
	 * Obtiene el valor de {@code Calificaciones_recibidas}.
	 *
	 * @return valor de {@code Calificaciones_recibidas}
	 */
	public List<Calificacion> getCalificaciones_recibidas() {
		return calificaciones_recibidas;
	}

	/**
	 * Establece el valor de {@code Calificaciones_recibidas}.
	 *
	 * @param calificaciones_recibidas nuevo valor
	 */
	public void setCalificaciones_recibidas(List<Calificacion> calificaciones_recibidas) {
		this.calificaciones_recibidas = calificaciones_recibidas;
	}

	/**
	 * Obtiene el valor de {@code Calificaciones_hechas}.
	 *
	 * @return valor de {@code Calificaciones_hechas}
	 */
	public List<Calificacion> getCalificaciones_hechas() {
		return calificaciones_hechas;
	}

	/**
	 * Establece el valor de {@code Calificaciones_hechas}.
	 *
	 * @param calificaciones_hechas nuevo valor
	 */
	public void setCalificaciones_hechas(List<Calificacion> calificaciones_hechas) {
		this.calificaciones_hechas = calificaciones_hechas;
	}

	/**
	 * Representación en texto del cliente.
	 *
	 * @return texto descriptivo del objeto
	 */
	@Override
	public String toString() {
		return "Cliente [codigo_usuario=" + getCodigo_usuario() + ", email=" + getEmail() + ", nombre_completo="
				+ nombre_completo + ", direccion=" + direccion + "]";
	}

}
