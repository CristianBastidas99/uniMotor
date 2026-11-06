package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 * Pregunta que un cliente hace sobre un vehiculo, con su respuesta opcional.
 */
@Entity
@NamedQueries({
		@NamedQuery(name = Pregunta.PREGUNTAS_POR_VEHICULO, query = "select p from Pregunta p where p.vehiculo.codigo_vehiculo = :codigo order by p.fecha desc"),
		@NamedQuery(name = Pregunta.PREGUNTAS_RECIBIDAS_POR_VENDEDOR, query = "select p from Pregunta p where p.vehiculo.codigo_dueno.email = :email order by p.fecha desc"),
		@NamedQuery(name = Pregunta.PREGUNTAS_SIN_RESPONDER_POR_VENDEDOR, query = "select p from Pregunta p where p.vehiculo.codigo_dueno.email = :email and p.respuesta is null order by p.fecha desc"),
		@NamedQuery(name = Pregunta.PREGUNTAS_HECHAS_POR_CLIENTE, query = "select p from Pregunta p where p.cliente.email = :email order by p.fecha desc"),
		@NamedQuery(name = Pregunta.ESTADISTICA_PREGUNTAS_POR_VEHICULO, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(concat(concat(concat(concat(m.marca, ' '), m.nombre), ' - '), v.placa), count(p)) from Pregunta p join p.vehiculo v join v.codigo_modelo m group by v.codigo_vehiculo, m.marca, m.nombre, v.placa order by count(p) desc") })
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
	public static final String PREGUNTAS_POR_VEHICULO = "Pregunta_PREGUNTAS_POR_VEHICULO";
	public static final String PREGUNTAS_RECIBIDAS_POR_VENDEDOR = "Pregunta_PREGUNTAS_RECIBIDAS_POR_VENDEDOR";
	public static final String PREGUNTAS_SIN_RESPONDER_POR_VENDEDOR = "Pregunta_PREGUNTAS_SIN_RESPONDER_POR_VENDEDOR";
	public static final String PREGUNTAS_HECHAS_POR_CLIENTE = "Pregunta_PREGUNTAS_HECHAS_POR_CLIENTE";
	public static final String ESTADISTICA_PREGUNTAS_POR_VEHICULO = "Pregunta_ESTADISTICA_PREGUNTAS_POR_VEHICULO";

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
