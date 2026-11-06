package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 * Pregunta que un cliente hace sobre un vehiculo, con su respuesta opcional.
 */
@Entity
public class Pregunta implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "codigo_pregunta")
	private Long codigo_pregunta;

	@ManyToOne
	@JoinColumn(name = "codigo_vehiculo", nullable = false)
	private Vehiculo vehiculo;

	@ManyToOne
	@JoinColumn(name = "codigo_usuario", nullable = false)
	private Cliente cliente;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha", nullable = false)
	private Date fecha;

	@Column(name = "descripcion", nullable = false, length = 500)
	private String descripcion;

	@Column(name = "respuesta", length = 1000)
	private String respuesta;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha_respuesta")
	private Date fecha_respuesta;

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

	public Long getCodigo_pregunta() {
		return codigo_pregunta;
	}

	public void setCodigo_pregunta(Long codigo_pregunta) {
		this.codigo_pregunta = codigo_pregunta;
	}

	public Vehiculo getVehiculo() {
		return this.vehiculo;
	}

	public void setVehiculo(Vehiculo vehiculo) {
		this.vehiculo = vehiculo;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
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

	public String getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}

	public Date getFecha_respuesta() {
		return fecha_respuesta;
	}

	public void setFecha_respuesta(Date fecha_respuesta) {
		this.fecha_respuesta = fecha_respuesta;
	}

	@Override
	public int hashCode() {
		return 31 + ((codigo_pregunta == null) ? 0 : codigo_pregunta.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Pregunta other = (Pregunta) obj;
		return codigo_pregunta == null ? other.codigo_pregunta == null : codigo_pregunta.equals(other.codigo_pregunta);
	}

	@Override
	public String toString() {
		return "Pregunta [codigo_pregunta=" + codigo_pregunta + ", fecha=" + fecha + ", descripcion=" + descripcion
				+ ", respuesta=" + respuesta + ", fecha_respuesta=" + fecha_respuesta + "]";
	}

}
