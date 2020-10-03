package co.edu.uniquindio.empresa;

import java.io.Serializable;
import java.lang.String;
import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: Caracteristica
 *
 */
@Entity

public class Caracteristica implements Serializable {

	   
	@Id
	private int codigo_caracteristica;
	
	@Column(name = "nombre", nullable = false)
	private String nombre;
	
	@ManyToMany(cascade = CascadeType.ALL)
	private List<Vehiculo> vehiculos;
	
	private static final long serialVersionUID = 1L;

	public Caracteristica() {
		super();
	}   
	public int getCodigo_caracteristica() {
		return this.codigo_caracteristica;
	}

	public void setCodigo_caracteristica(int codigo_caracteristica) {
		this.codigo_caracteristica = codigo_caracteristica;
	}   
	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
   
}
