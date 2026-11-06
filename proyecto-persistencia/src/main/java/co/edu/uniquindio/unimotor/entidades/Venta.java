package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 * Venta registrada de un vehículo a un cliente comprador. Funcionalidad extra de uniMotor que permite
 * llevar el historial de compras y las estadísticas de ventas.
 * <p>
 * Tabla: {@code Venta}. Relación uno a uno con {@link Vehiculo} (un vehículo se vende una sola vez) y
 * muchos a uno con {@link Cliente} (el comprador).
 */
@Entity
@NamedQueries({
		@NamedQuery(name = Venta.ESTADISTICA_VENTAS_POR_MES, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(FUNCTION('DATE_FORMAT', v.fecha, '%Y-%m'), count(v)) from Venta v group by FUNCTION('DATE_FORMAT', v.fecha, '%Y-%m') order by FUNCTION('DATE_FORMAT', v.fecha, '%Y-%m')") })

public class Venta implements Serializable {

	/**
	 * Identificador único autogenerado (clave primaria).
	 */
	@Id
	@Column(name = "codigo_venta")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long codigo_venta;

	/**
	 * Fecha y hora de la venta; obligatoria.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha", nullable = false)
	private Date fecha;

	/**
	 * Vehículo vendido; obligatorio (columna {@code codigo_vehiculo}).
	 */
	@JoinColumn(name = "codigo_vehiculo", nullable = false)
	@OneToOne
	private Vehiculo codigo_vehiculo;

	/**
	 * Cliente que compró el vehículo; obligatorio (columna {@code codigo_usuario}).
	 */
	@JoinColumn(name = "codigo_usuario", nullable = false)
	@ManyToOne
	private Cliente codigo_comprador;

	/**
	 * Versión de serialización.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * NamedQuery de estadística sin parámetros: número de ventas por mes, en orden cronológico.
	 * Usa la función {@code DATE_FORMAT} de MySQL. Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO}
	 * (etiqueta = mes con formato {@code yyyy-MM}, valor = conteo).
	 */
	public static final String ESTADISTICA_VENTAS_POR_MES = "Venta_ESTADISTICA_VENTAS_POR_MES";

	/**
	 * Constructor vacío requerido por JPA.
	 */
	public Venta() {
		super();
	}

	/**
	 * Crea una venta.
	 *
	 * @param fecha fecha de la venta
	 * @param codigo_vehiculo vehículo vendido
	 * @param codigo_comprador cliente comprador
	 */
	public Venta(Date fecha, Vehiculo codigo_vehiculo, Cliente codigo_comprador) {
		super();
		this.fecha = fecha;
		this.codigo_vehiculo = codigo_vehiculo;
		this.codigo_comprador = codigo_comprador;
	}

	/**
	 * Calcula el hash a partir del identificador {@code codigo_venta}.
	 *
	 * @return código hash de la venta
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo_venta == null) ? 0 : codigo_venta.hashCode());
		return result;
	}

	/**
	 * Dos ventas son iguales si son de la misma clase y tienen el mismo {@code codigo_venta}.
	 *
	 * @param obj objeto a comparar
	 * @return {@code true} si representan la misma venta
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Venta other = (Venta) obj;
		if (codigo_venta == null) {
			if (other.codigo_venta != null)
				return false;
		} else if (!codigo_venta.equals(other.codigo_venta))
			return false;
		return true;
	}

	/**
	 * Representación en texto de la venta.
	 *
	 * @return texto descriptivo del objeto
	 */
	@Override
	public String toString() {
		return "Venta [codigo_venta=" + codigo_venta + ", fecha=" + fecha + "]";
	}

	/**
	 * Obtiene el valor de {@code Codigo_venta}.
	 *
	 * @return valor de {@code Codigo_venta}
	 */
	public Long getCodigo_venta() {
		return this.codigo_venta;
	}

	/**
	 * Establece el valor de {@code Codigo_venta}.
	 *
	 * @param codigo_venta nuevo valor
	 */
	public void setCodigo_venta(Long codigo_venta) {
		this.codigo_venta = codigo_venta;
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
	 * Obtiene el valor de {@code Codigo_vehiculo}.
	 *
	 * @return valor de {@code Codigo_vehiculo}
	 */
	public Vehiculo getCodigo_vehiculo() {
		return this.codigo_vehiculo;
	}

	/**
	 * Establece el valor de {@code Codigo_vehiculo}.
	 *
	 * @param codigo_vehiculo nuevo valor
	 */
	public void setCodigo_vehiculo(Vehiculo codigo_vehiculo) {
		this.codigo_vehiculo = codigo_vehiculo;
	}

	/**
	 * Obtiene el comprador de la venta.
	 *
	 * @return cliente comprador
	 */
	public Cliente getCodigo_usuario() {
		return this.codigo_comprador;
	}

	/**
	 * Establece el comprador de la venta.
	 *
	 * @param codigo_usuario cliente comprador
	 */
	public void setCodigo_usuario(Cliente codigo_usuario) {
		this.codigo_comprador = codigo_usuario;
	}

}
