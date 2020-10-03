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
	private int codigo_vehiculo;
	
	@Column(name = "precio", nullable = false)
	private int precio;
	
	@Column(name = "descripcion", nullable = false)
	private String descripcion;
	
	@Column(name = "color", nullable = false)
	private String color;
	
	@Column(name = "year", nullable = false)
	private int year;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_combustible")
	private TipoCombustible tipo_combustible;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_vehiculo")
	private TipoVehiculo tipo_vehiculo;
	
	@OneToMany(mappedBy = "vehiculo")
	private List<Pregunta> preguntas;
	
	@ElementCollection
	@CollectionTable(name = "foto_Vehiculo", joinColumns = @JoinColumn(name = "codigo_vehiculo"))
	@Column(name = "foto")
	private List<String> foto;
	
	@JoinTable(name = "favorito", joinColumns = @JoinColumn(name = "codigo_vehiculo"), inverseJoinColumns = @JoinColumn(name = "codigo_persona"))
	@ManyToMany(cascade = CascadeType.ALL)
	private List<Persona> personas;
	
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
		result = prime * result + codigo_vehiculo;
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
		if (codigo_vehiculo != other.codigo_vehiculo)
			return false;
		return true;
	}

	public int getCodigo_vehiculo() {
		return this.codigo_vehiculo;
	}

	public void setCodigo_vehiculo(int codigo_vehiculo) {
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

}
