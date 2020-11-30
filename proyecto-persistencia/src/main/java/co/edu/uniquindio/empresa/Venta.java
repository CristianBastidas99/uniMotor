package co.edu.uniquindio.empresa;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: Venta
 *
 */
@Entity

public class Venta implements Serializable {

	@Id
	@Column(name = "codigo_venta")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long codigo_venta;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha", nullable = false)
	private Date fecha;

	@JoinColumn(name = "codigo_vehiculo", nullable = false)
	@OneToOne
	private Vehiculo codigo_vehiculo;

	@JoinColumn(name = "codigo_usuario", nullable = false)
	@ManyToOne
	private Cliente codigo_comprador;

	private static final long serialVersionUID = 1L;

	public Venta() {
		super();
	}

	/**
	 * @param fecha
	 * @param codigo_vehiculo
	 * @param codigo_comprador
	 */
	public Venta(Date fecha, Vehiculo codigo_vehiculo, Cliente codigo_comprador) {
		super();
		this.fecha = fecha;
		this.codigo_vehiculo = codigo_vehiculo;
		this.codigo_comprador = codigo_comprador;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo_venta == null) ? 0 : codigo_venta.hashCode());
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
		Venta other = (Venta) obj;
		if (codigo_venta == null) {
			if (other.codigo_venta != null)
				return false;
		} else if (!codigo_venta.equals(other.codigo_venta))
			return false;
		return true;
	}

	public Long getCodigo_venta() {
		return this.codigo_venta;
	}

	public void setCodigo_venta(Long codigo_venta) {
		this.codigo_venta = codigo_venta;
	}

	public Date getFecha() {
		return this.fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Vehiculo getCodigo_vehiculo() {
		return this.codigo_vehiculo;
	}

	public void setCodigo_vehiculo(Vehiculo codigo_vehiculo) {
		this.codigo_vehiculo = codigo_vehiculo;
	}

	public Cliente getCodigo_usuario() {
		return this.codigo_comprador;
	}

	public void setCodigo_usuario(Cliente codigo_usuario) {
		this.codigo_comprador = codigo_usuario;
	}

}
