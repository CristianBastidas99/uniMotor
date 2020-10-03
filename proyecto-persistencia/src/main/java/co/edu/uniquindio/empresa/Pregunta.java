package co.edu.uniquindio.empresa;

import co.edu.uniquindio.empresa.Persona;
import co.edu.uniquindio.empresa.Vehiculo;
import co.edu.uniquindio.empresa.VehiculoPersonaPK;
import java.io.Serializable;
import java.lang.String;
import java.util.Date;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: Pregunta
 *
 */
@Entity

public class Pregunta implements Serializable {

	@EmbeddedId
	private VehiculoPersonaPK llave;

	@ManyToOne
	@MapsId("mi_vehiculo")
	@JoinColumn(name = "codigo_vehiculo", foreignKey = @ForeignKey(name = "FK_vehiculo"))
	private Vehiculo vehiculo;

	@ManyToOne
	@MapsId("mi_persona")
	@JoinColumn(name = "codigo_persona", foreignKey = @ForeignKey(name = "FK_persona"))
	private Persona persona;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha", nullable = false)
	private Date fecha;

	@Column(name = "descripcion")
	private String descripcion;

	private static final long serialVersionUID = 1L;

	public Pregunta() {
		super();
	}

	public VehiculoPersonaPK getLlave() {
		return this.llave;
	}

	public void setLlave(VehiculoPersonaPK llave) {
		this.llave = llave;
	}

	public Vehiculo getVehiculo() {
		return this.vehiculo;
	}

	public void setVehiculo(Vehiculo vehiculo) {
		this.vehiculo = vehiculo;
	}

	public Persona getPersona() {
		return this.persona;
	}

	public void setPersona(Persona persona) {
		this.persona = persona;
	}

	public Date getFecha() {
		return this.fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

}
