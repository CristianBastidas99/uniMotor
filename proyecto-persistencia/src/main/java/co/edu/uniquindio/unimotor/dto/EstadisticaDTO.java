package co.edu.uniquindio.unimotor.dto;

import java.io.Serializable;

/**
 * Resultado de una consulta de estadística agrupada (GROUP BY), pensado para
 * alimentar los gráficos del administrador.
 * <p>
 * Se usa desde JPQL con una expresión constructora, por ejemplo:
 * {@code select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(v.tipo_vehiculo, count(v)) from Vehiculo v group by v.tipo_vehiculo}
 * <p>
 * {@code etiqueta} es el valor del eje de categorías (un enum, un nombre, un año...) convertido a texto;
 * {@code serie} es opcional y sirve para gráficos con varias series (p. ej. barras apiladas);
 * {@code valor} es el número (conteo, promedio, suma).
 */
public class EstadisticaDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String etiqueta;
	private String serie;
	private Number valor;

	public EstadisticaDTO() {
	}

	/** Etiqueta + valor (conteos, sumas, promedios). */
	public EstadisticaDTO(Object etiqueta, Number valor) {
		this(etiqueta, null, valor);
	}

	/** Etiqueta + serie + valor, para gráficos con varias series. */
	public EstadisticaDTO(Object etiqueta, Object serie, Number valor) {
		this.etiqueta = etiqueta == null ? null : String.valueOf(etiqueta);
		this.serie = serie == null ? null : String.valueOf(serie);
		this.valor = valor;
	}

	public String getEtiqueta() {
		return etiqueta;
	}

	public void setEtiqueta(String etiqueta) {
		this.etiqueta = etiqueta;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public Number getValor() {
		return valor;
	}

	public void setValor(Number valor) {
		this.valor = valor;
	}

	@Override
	public String toString() {
		return "EstadisticaDTO [etiqueta=" + etiqueta + (serie == null ? "" : ", serie=" + serie) + ", valor=" + valor + "]";
	}
}
