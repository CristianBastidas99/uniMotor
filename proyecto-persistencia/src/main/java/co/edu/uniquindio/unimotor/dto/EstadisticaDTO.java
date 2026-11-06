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

	/**
	 * Versión de serialización.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Valor del eje de categorías (enum, nombre, año...) convertido a texto; puede ser {@code null}.
	 */
	private String etiqueta;
	/**
	 * Nombre de la serie para gráficos con varias series (p. ej. barras apiladas); {@code null} si el gráfico tiene una sola.
	 */
	private String serie;
	/**
	 * Valor numérico de la categoría (conteo, promedio o suma).
	 */
	private Number valor;

	/**
	 * Constructor vacío requerido para el uso como JavaBean.
	 */
	public EstadisticaDTO() {
	}

	/**
	 * Etiqueta + valor (conteos, sumas, promedios).
	 *
	 * @param etiqueta categoría del eje; se convierte a texto con {@link String#valueOf(Object)}
	 * @param valor valor numérico asociado
	 */
	public EstadisticaDTO(Object etiqueta, Number valor) {
		this(etiqueta, null, valor);
	}

	/**
	 * Etiqueta + serie + valor, para gráficos con varias series.
	 *
	 * @param etiqueta categoría del eje; se convierte a texto
	 * @param serie nombre de la serie; se convierte a texto
	 * @param valor valor numérico asociado
	 */
	public EstadisticaDTO(Object etiqueta, Object serie, Number valor) {
		this.etiqueta = etiqueta == null ? null : String.valueOf(etiqueta);
		this.serie = serie == null ? null : String.valueOf(serie);
		this.valor = valor;
	}

	/**
	 * Obtiene el valor de {@code Etiqueta}.
	 *
	 * @return valor de {@code Etiqueta}
	 */
	public String getEtiqueta() {
		return etiqueta;
	}

	/**
	 * Establece el valor de {@code Etiqueta}.
	 *
	 * @param etiqueta nuevo valor
	 */
	public void setEtiqueta(String etiqueta) {
		this.etiqueta = etiqueta;
	}

	/**
	 * Obtiene el valor de {@code Serie}.
	 *
	 * @return valor de {@code Serie}
	 */
	public String getSerie() {
		return serie;
	}

	/**
	 * Establece el valor de {@code Serie}.
	 *
	 * @param serie nuevo valor
	 */
	public void setSerie(String serie) {
		this.serie = serie;
	}

	/**
	 * Obtiene el valor de {@code Valor}.
	 *
	 * @return valor de {@code Valor}
	 */
	public Number getValor() {
		return valor;
	}

	/**
	 * Establece el valor de {@code Valor}.
	 *
	 * @param valor nuevo valor
	 */
	public void setValor(Number valor) {
		this.valor = valor;
	}

	/**
	 * Representación en texto con etiqueta, serie (si existe) y valor.
	 *
	 * @return texto descriptivo del objeto
	 */
	@Override
	public String toString() {
		return "EstadisticaDTO [etiqueta=" + etiqueta + (serie == null ? "" : ", serie=" + serie) + ", valor=" + valor + "]";
	}
}
