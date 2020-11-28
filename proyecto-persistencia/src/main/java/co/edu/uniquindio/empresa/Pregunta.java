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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private VehiculoPersonaPK llave;

	@ManyToOne
	@MapsId("mi_vehiculo")
	@JoinColumn(name = "codigo_vehiculo")
	private Vehiculo vehiculo;

	@ManyToOne
	@MapsId("mi_usuario")
	@JoinColumn(name = "codigo_usuario")
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

	public Pregunta(Vehiculo vehiculo, Cliente cliente, Date fecha, String descripcion) {
		super();
		this.vehiculo = vehiculo;
		this.cliente = cliente;
		this.fecha = fecha;
		this.descripcion = descripcion;
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
