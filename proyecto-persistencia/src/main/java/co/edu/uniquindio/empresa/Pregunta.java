package co.edu.uniquindio.empresa;

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
	@MapsId("mi_usuario")
	@JoinColumn(name = "codigo_usuario", foreignKey = @ForeignKey(name = "FK_usuario"))
	private Cliente cliente;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha", nullable = false)
	private Date fecha;

	@Column(name = "descripcion")
	private String descripcion;

	private static final long serialVersionUID = 1L;

	public Pregunta() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((llave == null) ? 0 : llave.hashCode());
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
		Pregunta other = (Pregunta) obj;
		if (llave == null) {
			if (other.llave != null)
				return false;
		} else if (!llave.equals(other.llave))
			return false;
		return true;
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

	public Cliente getPersona() {
		return this.cliente;
	}

	public void setPersona(Cliente cliente) {
		this.cliente = cliente;
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

	/**
	 * @return the cliente
	 */
	public Cliente getCliente() {
		return cliente;
	}

	/**
	 * @param cliente the cliente to set
	 */
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

}
