package co.edu.uniquindio.empresa;

import java.io.Serializable;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: VehiculoPersonaPK
 *
 */
@Embeddable
public class VehiculoPersonaPK implements Serializable {

	private Long mi_vehiculo;
	private Long mi_usuario;
	private static final long serialVersionUID = 1L;

	public VehiculoPersonaPK() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mi_usuario == null) ? 0 : mi_usuario.hashCode());
		result = prime * result + ((mi_vehiculo == null) ? 0 : mi_vehiculo.hashCode());
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
		VehiculoPersonaPK other = (VehiculoPersonaPK) obj;
		if (mi_usuario == null) {
			if (other.mi_usuario != null)
				return false;
		} else if (!mi_usuario.equals(other.mi_usuario))
			return false;
		if (mi_vehiculo == null) {
			if (other.mi_vehiculo != null)
				return false;
		} else if (!mi_vehiculo.equals(other.mi_vehiculo))
			return false;
		return true;
	}

	public Long getCodigo_vehiculo() {
		return this.mi_vehiculo;
	}

	public void setCodigo_vehiculo(Long codigo_vehiculo) {
		this.mi_vehiculo = codigo_vehiculo;
	}

	public Long getCodigo_persona() {
		return this.mi_usuario;
	}

	public void setCodigo_persona(Long codigo_persona) {
		this.mi_usuario = codigo_persona;
	}

}
