package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 * Calificacion (1 a 5 estrellas) que un cliente le da a un vendedor. Un
 * cliente solo puede calificar una vez a cada vendedor.
 */
@Entity
@Table(name = "CALIFICACION", uniqueConstraints = @UniqueConstraint(columnNames = { "codigo_calificador",
		"codigo_vendedor" }))
@NamedQueries({
		@NamedQuery(name = Calificacion.CALIFICACIONES_VENDEDOR, query = "select c from Calificacion c where c.vendedor.codigo_usuario = :codigo"),
		@NamedQuery(name = Calificacion.PROMEDIO_VENDEDOR, query = "select avg(c.estrellas) from Calificacion c where c.vendedor.codigo_usuario = :codigo"),
		@NamedQuery(name = Calificacion.ESTADISTICA_PROMEDIO_POR_VENDEDOR, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(ve.nombre_completo, avg(c.estrellas)) from Calificacion c join c.vendedor ve group by ve.codigo_usuario, ve.nombre_completo order by avg(c.estrellas) desc") })
public class Calificacion implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "codigo_calificacion")
	private Long codigo_calificacion;

	@ManyToOne
	@JoinColumn(name = "codigo_calificador", nullable = false)
	private Cliente calificador;

	@ManyToOne
	@JoinColumn(name = "codigo_vendedor", nullable = false)
	private Cliente vendedor;

	@Column(name = "estrellas", nullable = false)
	private int estrellas;

	@Column(name = "comentario", length = 500)
	private String comentario;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha", nullable = false)
	private Date fecha;

	private static final long serialVersionUID = 1L;
	public static final String ESTADISTICA_PROMEDIO_POR_VENDEDOR = "Calificacion_ESTADISTICA_PROMEDIO_POR_VENDEDOR";
	public static final String CALIFICACIONES_VENDEDOR = "Calificacion_CALIFICACIONES_VENDEDOR";
	public static final String PROMEDIO_VENDEDOR = "Calificacion_PROMEDIO_VENDEDOR";

	public Calificacion() {
		super();
	}

	public Calificacion(Cliente calificador, Cliente vendedor, int estrellas, String comentario, Date fecha) {
		super();
		this.calificador = calificador;
		this.vendedor = vendedor;
		this.estrellas = estrellas;
		this.comentario = comentario;
		this.fecha = fecha;
	}

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

	public Long getCodigo_calificacion() {
		return codigo_calificacion;
	}

	public void setCodigo_calificacion(Long codigo_calificacion) {
		this.codigo_calificacion = codigo_calificacion;
	}

	public Cliente getCalificador() {
		return calificador;
	}

	public void setCalificador(Cliente calificador) {
		this.calificador = calificador;
	}

	public Cliente getVendedor() {
		return vendedor;
	}

	public void setVendedor(Cliente vendedor) {
		this.vendedor = vendedor;
	}

	public int getEstrellas() {
		return estrellas;
	}

	public void setEstrellas(int estrellas) {
		this.estrellas = estrellas;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	@Override
	public int hashCode() {
		return 31 + ((codigo_calificacion == null) ? 0 : codigo_calificacion.hashCode());
	}

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

	@Override
	public String toString() {
		return "Calificacion [codigo_calificacion=" + codigo_calificacion + ", estrellas=" + estrellas
				+ ", comentario=" + comentario + ", fecha=" + fecha + "]";
	}

}
