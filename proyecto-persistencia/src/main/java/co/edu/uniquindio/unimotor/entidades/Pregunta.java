package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 * Pregunta que un cliente le hace al vendedor sobre un vehículo publicado, con una respuesta opcional.
 * <p>
 * Tabla: {@code Pregunta}. Relaciones muchos a uno con {@link Vehiculo} y con {@link Cliente} (quien pregunta).
 */
@Entity
@NamedQueries({
		@NamedQuery(name = Pregunta.PREGUNTAS_POR_VEHICULO, query = "select p from Pregunta p where p.vehiculo.codigo_vehiculo = :codigo order by p.fecha desc"),
		@NamedQuery(name = Pregunta.PREGUNTAS_RECIBIDAS_POR_VENDEDOR, query = "select p from Pregunta p where p.vehiculo.codigo_dueno.email = :email order by p.fecha desc"),
		@NamedQuery(name = Pregunta.PREGUNTAS_SIN_RESPONDER_POR_VENDEDOR, query = "select p from Pregunta p where p.vehiculo.codigo_dueno.email = :email and p.respuesta is null order by p.fecha desc"),
		@NamedQuery(name = Pregunta.PREGUNTAS_HECHAS_POR_CLIENTE, query = "select p from Pregunta p where p.cliente.email = :email order by p.fecha desc"),
		@NamedQuery(name = Pregunta.ESTADISTICA_PREGUNTAS_POR_VEHICULO, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(concat(concat(concat(concat(m.marca, ' '), m.nombre), ' - '), v.placa), count(p)) from Pregunta p join p.vehiculo v join v.codigo_modelo m group by v.codigo_vehiculo, m.marca, m.nombre, v.placa order by count(p) desc") })
public class Pregunta implements Serializable {

	/**
	 * Identificador único autogenerado (clave primaria).
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "codigo_pregunta")
	private Long codigo_pregunta;

	/**
	 * Vehículo sobre el que se pregunta; obligatorio.
	 */
	@ManyToOne
	@JoinColumn(name = "codigo_vehiculo", nullable = false)
	private Vehiculo vehiculo;

	/**
	 * Cliente que hace la pregunta; obligatorio (columna {@code codigo_usuario}).
	 */
	@ManyToOne
	@JoinColumn(name = "codigo_usuario", nullable = false)
	private Cliente cliente;

	/**
	 * Fecha y hora en que se hizo la pregunta; obligatoria.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha", nullable = false)
	private Date fecha;

	/**
	 * Texto de la pregunta; obligatorio, máximo 500 caracteres.
	 */
	@Column(name = "descripcion", nullable = false, length = 500)
	private String descripcion;

	/**
	 * Respuesta del vendedor; opcional (nula mientras no se responda), máximo 1000 caracteres.
	 */
	@Column(name = "respuesta", length = 1000)
	private String respuesta;

	/**
	 * Fecha y hora de la respuesta; nula si aún no se responde.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha_respuesta")
	private Date fecha_respuesta;

	/**
	 * Versión de serialización.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * NamedQuery que lista las preguntas de un vehículo, de la más reciente a la más antigua.
	 * Parámetro: {@code :codigo} (Long, {@code codigo_vehiculo}). Devuelve entidades {@link Pregunta}.
	 */
	public static final String PREGUNTAS_POR_VEHICULO = "Pregunta_PREGUNTAS_POR_VEHICULO";
	/**
	 * NamedQuery que lista las preguntas hechas sobre los vehículos de un vendedor, de la más reciente a la más antigua.
	 * Parámetro: {@code :email} (String, correo del dueño del vehículo). Devuelve entidades {@link Pregunta}.
	 */
	public static final String PREGUNTAS_RECIBIDAS_POR_VENDEDOR = "Pregunta_PREGUNTAS_RECIBIDAS_POR_VENDEDOR";
	/**
	 * NamedQuery que lista las preguntas pendientes (sin respuesta) de los vehículos de un vendedor.
	 * Parámetro: {@code :email} (String, correo del dueño del vehículo). Devuelve entidades {@link Pregunta}.
	 */
	public static final String PREGUNTAS_SIN_RESPONDER_POR_VENDEDOR = "Pregunta_PREGUNTAS_SIN_RESPONDER_POR_VENDEDOR";
	/**
	 * NamedQuery que lista las preguntas que ha hecho un cliente, de la más reciente a la más antigua.
	 * Parámetro: {@code :email} (String, correo del cliente). Devuelve entidades {@link Pregunta}.
	 */
	public static final String PREGUNTAS_HECHAS_POR_CLIENTE = "Pregunta_PREGUNTAS_HECHAS_POR_CLIENTE";
	/**
	 * NamedQuery de estadística sin parámetros: número de preguntas por vehículo, de mayor a menor.
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = "marca modelo - placa", valor = conteo).
	 */
	public static final String ESTADISTICA_PREGUNTAS_POR_VEHICULO = "Pregunta_ESTADISTICA_PREGUNTAS_POR_VEHICULO";

	/**
	 * Constructor vacío requerido por JPA.
	 */
	public Pregunta() {
		super();
	}

	/**
	 * Crea una pregunta aún sin respuesta.
	 *
	 * @param vehiculo vehículo sobre el que se pregunta
	 * @param cliente cliente que pregunta
	 * @param fecha fecha de la pregunta
	 * @param descripcion texto de la pregunta
	 */
	public Pregunta(Vehiculo vehiculo, Cliente cliente, Date fecha, String descripcion) {
		super();
		this.vehiculo = vehiculo;
		this.cliente = cliente;
		this.fecha = fecha;
		this.descripcion = descripcion;
	}

	/**
	 * Obtiene el valor de {@code Codigo_pregunta}.
	 *
	 * @return valor de {@code Codigo_pregunta}
	 */
	public Long getCodigo_pregunta() {
		return codigo_pregunta;
	}

	/**
	 * Establece el valor de {@code Codigo_pregunta}.
	 *
	 * @param codigo_pregunta nuevo valor
	 */
	public void setCodigo_pregunta(Long codigo_pregunta) {
		this.codigo_pregunta = codigo_pregunta;
	}

	/**
	 * Obtiene el valor de {@code Vehiculo}.
	 *
	 * @return valor de {@code Vehiculo}
	 */
	public Vehiculo getVehiculo() {
		return this.vehiculo;
	}

	/**
	 * Establece el valor de {@code Vehiculo}.
	 *
	 * @param vehiculo nuevo valor
	 */
	public void setVehiculo(Vehiculo vehiculo) {
		this.vehiculo = vehiculo;
	}

	/**
	 * Obtiene el valor de {@code Cliente}.
	 *
	 * @return valor de {@code Cliente}
	 */
	public Cliente getCliente() {
		return cliente;
	}

	/**
	 * Establece el valor de {@code Cliente}.
	 *
	 * @param cliente nuevo valor
	 */
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	/**
	 * Obtiene el valor de {@code Fecha}.
	 *
	 * @return valor de {@code Fecha}
	 */
	public Date getFecha() {
		return this.fecha;
	}

	/**
	 * Establece el valor de {@code Fecha}.
	 *
	 * @param fecha nuevo valor
	 */
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	/**
	 * Obtiene el valor de {@code Descripcion}.
	 *
	 * @return valor de {@code Descripcion}
	 */
	public String getDescripcion() {
		return this.descripcion;
	}

	/**
	 * Establece el valor de {@code Descripcion}.
	 *
	 * @param descripcion nuevo valor
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/**
	 * Obtiene el valor de {@code Respuesta}.
	 *
	 * @return valor de {@code Respuesta}
	 */
	public String getRespuesta() {
		return respuesta;
	}

	/**
	 * Establece el valor de {@code Respuesta}.
	 *
	 * @param respuesta nuevo valor
	 */
	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}

	/**
	 * Obtiene el valor de {@code Fecha_respuesta}.
	 *
	 * @return valor de {@code Fecha_respuesta}
	 */
	public Date getFecha_respuesta() {
		return fecha_respuesta;
	}

	/**
	 * Establece el valor de {@code Fecha_respuesta}.
	 *
	 * @param fecha_respuesta nuevo valor
	 */
	public void setFecha_respuesta(Date fecha_respuesta) {
		this.fecha_respuesta = fecha_respuesta;
	}

	/**
	 * Calcula el hash a partir del identificador {@code codigo_pregunta}.
	 *
	 * @return código hash de la pregunta
	 */
	@Override
	public int hashCode() {
		return 31 + ((codigo_pregunta == null) ? 0 : codigo_pregunta.hashCode());
	}

	/**
	 * Dos preguntas son iguales si son de la misma clase y tienen el mismo {@code codigo_pregunta}.
	 *
	 * @param obj objeto a comparar
	 * @return {@code true} si representan la misma pregunta
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Pregunta other = (Pregunta) obj;
		return codigo_pregunta == null ? other.codigo_pregunta == null : codigo_pregunta.equals(other.codigo_pregunta);
	}

	/**
	 * Representación en texto de la pregunta.
	 *
	 * @return texto descriptivo del objeto
	 */
	@Override
	public String toString() {
		return "Pregunta [codigo_pregunta=" + codigo_pregunta + ", fecha=" + fecha + ", descripcion=" + descripcion
				+ ", respuesta=" + respuesta + ", fecha_respuesta=" + fecha_respuesta + "]";
	}

}
