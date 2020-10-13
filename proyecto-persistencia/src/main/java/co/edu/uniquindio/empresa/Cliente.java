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
	
	@OneToMany(mappedBy = "codigo_dueño")
	private List<Vehiculo> vehiculos_Venta;
	
	@OneToMany(mappedBy = "codigo_comprador")
	private List<Venta> mis_compras;

	private static final long serialVersionUID = 1L;

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

	/**
	 * @return the ciudad
	
	public Ciudad getCiudad() {
		return codigo_ciudad;
	} */

	/**
	 * @param ciudad the ciudad to set
	 
	public void setCiudad(Ciudad ciudad) {
		this.codigo_ciudad = ciudad;
	}*/

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

}
