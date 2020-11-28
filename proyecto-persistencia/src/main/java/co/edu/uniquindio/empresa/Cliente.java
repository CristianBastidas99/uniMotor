package co.edu.uniquindio.empresa;

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
		@NamedQuery(name = Cliente.LISTA_FAVORITO_CLIENTE, query = "select f from Cliente c, IN (c.vehiculos_favoritos) f where c.email = :email"),
		@NamedQuery(name = Cliente.LISTA_FAVORITO_CLIENTE2, query = "select f, c.nombre_completo from Cliente c, IN (c.vehiculos_favoritos) f where c.email = :email"),
		@NamedQuery(name = Cliente.LISTA_FAVORITO_CLIENTE_JOIN, query = "select f from Cliente c join (c.vehiculos_favoritos) f where c.email = :email"),
		@NamedQuery(name = Cliente.LISTA_VEHICULOS_VENTA, query = "select c.email, f from Cliente c join (c.vehiculos_venta) f"),
		@NamedQuery(name = Cliente.LISTA_CIUDAD_CLIENTE, query = "select ci, c from Cliente c join (c.codigo_ciudad) ci ") 
		
})
public class Cliente extends Usuario implements Serializable {

	@Column(name = "nombre_completo", nullable = false)
	private String nombre_completo;

	@Column(name = "direccion", nullable = false)
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

	private static final long serialVersionUID = 1L;
	public static final String LISTA_FAVORITO_CLIENTE = "Cliente_ListaFavorito";
	public static final String LISTA_FAVORITO_CLIENTE2 = "Cliente_ListaFavorito2";
	public static final String LISTA_FAVORITO_CLIENTE_JOIN = "Cliente_ListaFavoritoJoin";
	public static final String LISTA_VEHICULOS_VENTA = "Cliente_ListaVehiculosVenta";
	public static final String LISTA_CIUDAD_CLIENTE = "Cliente_ListaCiudad";
	public static final String FIND_BY_ID = "Cliente_findById";
	public static final String GETALL = "Cliente_GetAll";

	public Cliente() {
		super();
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

	@Override
	public String toString() {
		return "Cliente [nombre_completo=" + nombre_completo + ", direccion=" + direccion + ", telefono=" + telefono.toString()
				+ ", codigo_ciudad=" + codigo_ciudad.getNombre() + "]";
	}

}
