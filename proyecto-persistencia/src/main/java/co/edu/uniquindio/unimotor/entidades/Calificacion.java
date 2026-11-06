package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 * Calificación (de 1 a 5 estrellas) que un cliente le da a otro cliente que actúa como vendedor.
 * Funcionalidad extra de uniMotor para dar reputación a los vendedores.
 * <p>
 * Tabla: {@code CALIFICACION}, con restricción única sobre ({@code codigo_calificador},
 * {@code codigo_vendedor}): un cliente solo puede calificar una vez a cada vendedor.
 * Relaciones: muchos a uno con {@link Cliente} como calificador y como vendedor.
 */
@Entity
@Table(name = "CALIFICACION", uniqueConstraints = @UniqueConstraint(columnNames = { "codigo_calificador",
		"codigo_vendedor" }))
@NamedQueries({
		@NamedQuery(name = Calificacion.CALIFICACION_POR_CALIFICADOR_Y_VENDEDOR, query = "select c from Calificacion c where c.calificador.email = :emailCalificador and c.vendedor.email = :emailVendedor"),
		@NamedQuery(name = Calificacion.CALIFICACIONES_POR_EMAIL_VENDEDOR, query = "select c from Calificacion c where c.vendedor.email = :email order by c.fecha desc"),
		@NamedQuery(name = Calificacion.PROMEDIO_POR_EMAIL_VENDEDOR, query = "select avg(c.estrellas) from Calificacion c where c.vendedor.email = :email"),
		@NamedQuery(name = Calificacion.CALIFICACIONES_VENDEDOR, query = "select c from Calificacion c where c.vendedor.codigo_usuario = :codigo"),
		@NamedQuery(name = Calificacion.PROMEDIO_VENDEDOR, query = "select avg(c.estrellas) from Calificacion c where c.vendedor.codigo_usuario = :codigo"),
		@NamedQuery(name = Calificacion.ESTADISTICA_PROMEDIO_POR_VENDEDOR, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(ve.nombre_completo, avg(c.estrellas)) from Calificacion c join c.vendedor ve group by ve.codigo_usuario, ve.nombre_completo order by avg(c.estrellas) desc") })
public class Calificacion implements Serializable {

	/**
	 * Identificador único autogenerado (clave primaria).
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "codigo_calificacion")
	private Long codigo_calificacion;

	/**
	 * Cliente que emite la calificación; obligatorio (columna {@code codigo_calificador}).
	 */
	@ManyToOne
	@JoinColumn(name = "codigo_calificador", nullable = false)
	private Cliente calificador;

	/**
	 * Cliente calificado en su rol de vendedor; obligatorio (columna {@code codigo_vendedor}).
	 */
	@ManyToOne
	@JoinColumn(name = "codigo_vendedor", nullable = false)
	private Cliente vendedor;

	/**
	 * Puntaje de 1 a 5; obligatorio. Se valida en {@link #validar()}.
	 */
	@Column(name = "estrellas", nullable = false)
	private int estrellas;

	/**
	 * Comentario opcional de la calificación; máximo 500 caracteres.
	 */
	@Column(name = "comentario", length = 500)
	private String comentario;

	/**
	 * Fecha y hora de la calificación; obligatoria (se asigna automáticamente si viene vacía).
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha", nullable = false)
	private Date fecha;

	/**
	 * Versión de serialización.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * NamedQuery de estadística sin parámetros: promedio de estrellas por vendedor, ordenado de mayor a menor.
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = nombre del vendedor, valor = promedio).
	 */
	public static final String ESTADISTICA_PROMEDIO_POR_VENDEDOR = "Calificacion_ESTADISTICA_PROMEDIO_POR_VENDEDOR";
	/**
	 * NamedQuery que lista las calificaciones recibidas por un vendedor.
	 * Parámetro: {@code :codigo} (Long, {@code codigo_usuario} del vendedor). Devuelve entidades {@link Calificacion}.
	 */
	public static final String CALIFICACIONES_VENDEDOR = "Calificacion_CALIFICACIONES_VENDEDOR";
	/**
	 * NamedQuery que calcula el promedio de estrellas de un vendedor.
	 * Parámetro: {@code :codigo} (Long, {@code codigo_usuario} del vendedor). Devuelve un {@link Double} (nulo si no tiene calificaciones).
	 */
	public static final String PROMEDIO_VENDEDOR = "Calificacion_PROMEDIO_VENDEDOR";
	/**
	 * NamedQuery que busca la calificación de un calificador a un vendedor (par único).
	 * Parámetros: {@code :emailCalificador} y {@code :emailVendedor} (String). Devuelve entidades {@link Calificacion}.
	 */
	public static final String CALIFICACION_POR_CALIFICADOR_Y_VENDEDOR = "Calificacion_CALIFICACION_POR_CALIFICADOR_Y_VENDEDOR";
	/**
	 * NamedQuery que lista las calificaciones recibidas por un vendedor, de la más reciente a la más antigua.
	 * Parámetro: {@code :email} (String, correo del vendedor). Devuelve entidades {@link Calificacion}.
	 */
	public static final String CALIFICACIONES_POR_EMAIL_VENDEDOR = "Calificacion_CALIFICACIONES_POR_EMAIL_VENDEDOR";
	/**
	 * NamedQuery que calcula el promedio de estrellas de un vendedor según su correo.
	 * Parámetro: {@code :email} (String). Devuelve un {@link Double} (nulo si no tiene calificaciones).
	 */
	public static final String PROMEDIO_POR_EMAIL_VENDEDOR = "Calificacion_PROMEDIO_POR_EMAIL_VENDEDOR";

	/**
	 * Constructor vacío requerido por JPA.
	 */
	public Calificacion() {
		super();
	}

	/**
	 * Crea una calificación.
	 *
	 * @param calificador cliente que califica
	 * @param vendedor cliente calificado
	 * @param estrellas puntaje de 1 a 5
	 * @param comentario comentario opcional
	 * @param fecha fecha de la calificación (si es {@code null} se asigna la actual al persistir)
	 */
	public Calificacion(Cliente calificador, Cliente vendedor, int estrellas, String comentario, Date fecha) {
		super();
		this.calificador = calificador;
		this.vendedor = vendedor;
		this.estrellas = estrellas;
		this.comentario = comentario;
		this.fecha = fecha;
	}

	/**
	 * Callback de ciclo de vida ({@code @PrePersist} y {@code @PreUpdate}): asigna la fecha actual si no hay
	 * una y verifica que las estrellas estén entre 1 y 5.
	 *
	 * @throws IllegalArgumentException si {@code estrellas} está fuera del rango 1 a 5
	 */
	@PrePersist
	@PreUpdate
	private void validar() {
		if (fecha == null) {
			fecha = new Date();
		}
		if (estrellas < 1 || estrellas > 5) {
			throw new IllegalArgumentException("Las estrellas deben estar entre 1 y 5: " + estrellas);
		}
	}

	/**
	 * Obtiene el valor de {@code Codigo_calificacion}.
	 *
	 * @return valor de {@code Codigo_calificacion}
	 */
	public Long getCodigo_calificacion() {
		return codigo_calificacion;
	}

	/**
	 * Establece el valor de {@code Codigo_calificacion}.
	 *
	 * @param codigo_calificacion nuevo valor
	 */
	public void setCodigo_calificacion(Long codigo_calificacion) {
		this.codigo_calificacion = codigo_calificacion;
	}

	/**
	 * Obtiene el valor de {@code Calificador}.
	 *
	 * @return valor de {@code Calificador}
	 */
	public Cliente getCalificador() {
		return calificador;
	}

	/**
	 * Establece el valor de {@code Calificador}.
	 *
	 * @param calificador nuevo valor
	 */
	public void setCalificador(Cliente calificador) {
		this.calificador = calificador;
	}

	/**
	 * Obtiene el valor de {@code Vendedor}.
	 *
	 * @return valor de {@code Vendedor}
	 */
	public Cliente getVendedor() {
		return vendedor;
	}

	/**
	 * Establece el valor de {@code Vendedor}.
	 *
	 * @param vendedor nuevo valor
	 */
	public void setVendedor(Cliente vendedor) {
		this.vendedor = vendedor;
	}

	/**
	 * Obtiene el valor de {@code Estrellas}.
	 *
	 * @return valor de {@code Estrellas}
	 */
	public int getEstrellas() {
		return estrellas;
	}

	/**
	 * Establece el valor de {@code Estrellas}.
	 *
	 * @param estrellas nuevo valor
	 */
	public void setEstrellas(int estrellas) {
		this.estrellas = estrellas;
	}

	/**
	 * Obtiene el valor de {@code Comentario}.
	 *
	 * @return valor de {@code Comentario}
	 */
	public String getComentario() {
		return comentario;
	}

	/**
	 * Establece el valor de {@code Comentario}.
	 *
	 * @param comentario nuevo valor
	 */
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	/**
	 * Obtiene el valor de {@code Fecha}.
	 *
	 * @return valor de {@code Fecha}
	 */
	public Date getFecha() {
		return fecha;
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
	 * Calcula el hash a partir del identificador {@code codigo_calificacion}.
	 *
	 * @return código hash de la calificación
	 */
	@Override
	public int hashCode() {
		return 31 + ((codigo_calificacion == null) ? 0 : codigo_calificacion.hashCode());
	}

	/**
	 * Dos calificaciones son iguales si son de la misma clase y tienen el mismo {@code codigo_calificacion}.
	 *
	 * @param obj objeto a comparar
	 * @return {@code true} si representan la misma calificación
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Calificacion other = (Calificacion) obj;
		return codigo_calificacion == null ? other.codigo_calificacion == null
				: codigo_calificacion.equals(other.codigo_calificacion);
	}

	/**
	 * Representación en texto de la calificación.
	 *
	 * @return texto descriptivo del objeto
	 */
	@Override
	public String toString() {
		return "Calificacion [codigo_calificacion=" + codigo_calificacion + ", estrellas=" + estrellas
				+ ", comentario=" + comentario + ", fecha=" + fecha + "]";
	}

}
