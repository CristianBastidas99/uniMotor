package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Modelo;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.web.fotos.ConfiguracionFotos;

/**
 * Utilidades de formato para las páginas: etiquetas de enumerados, precios, kilometraje y
 * datos resumidos de un vehículo. Todos los métodos son seguros ante valores nulos.
 */
@Named
@ApplicationScoped
public class FormatoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Texto legible de un valor enumerado.
	 *
	 * @param valor valor del enumerado (puede ser {@code null})
	 * @return etiqueta en español, o cadena vacía si es nulo
	 */
	public String etiqueta(Enum<?> valor) {
		if (valor == null) {
			return "";
		}
		switch (valor.name()) {
		case "CARRO": return "Carro";
		case "MOTOCICLETA": return "Moto";
		case "CAMIONETA": return "Camioneta";
		case "CAMION": return "Camión";
		case "AUTOBUS": return "Autobús";
		case "CICLOMOTOR": return "Ciclomotor";
		case "REMOLQUE": return "Remolque";
		case "TRAILER": return "Tráiler";
		case "GASOLINA": return "Gasolina";
		case "BIOETANOL": return "Bioetanol";
		case "DISEL": return "Diésel";
		case "BIODISEL": return "Biodiésel";
		case "GASNATURAL": return "Gas natural";
		case "HIBRIDO": return "Híbrido";
		case "ELECTRICO": return "Eléctrico";
		case "AUTOMATICO": return "Automática";
		case "MECANICO": return "Mecánica";
		case "NUEVO": return "Nuevo";
		case "USADO": return "Usado";
		case "OTRO": return "Otro";
		default:
			String n = valor.name().toLowerCase(Locale.ROOT);
			return Character.toUpperCase(n.charAt(0)) + n.substring(1);
		}
	}

	private static String miles(long valor) {
		DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
		simbolos.setGroupingSeparator('.');
		return new DecimalFormat("#,##0", simbolos).format(valor);
	}

	/**
	 * Formatea un precio en pesos colombianos.
	 *
	 * @param valor precio (puede ser {@code null})
	 * @return por ejemplo "$ 45.900.000", o cadena vacía si es nulo
	 */
	public String precio(Double valor) {
		if (valor == null) {
			return "";
		}
		return "$ " + miles(Math.round(valor));
	}

	/**
	 * Formatea un kilometraje.
	 *
	 * @param km kilómetros recorridos
	 * @return por ejemplo "35.000 km"
	 */
	public String kilometros(int km) {
		return miles(km) + " km";
	}

	/**
	 * Título de un vehículo.
	 *
	 * @param v el vehículo (puede ser {@code null})
	 * @return "Marca Modelo Año", o cadena vacía si es nulo
	 */
	public String titulo(Vehiculo v) {
		if (v == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Modelo m = v.getCodigo_modelo();
		if (m != null) {
			// Algunos modelos ya incluyen la marca en su nombre ("Mazda 3"): no repetirla.
			boolean nombreIncluyeMarca = m.getMarca() != null && m.getNombre() != null
					&& m.getNombre().toLowerCase().startsWith(m.getMarca().toLowerCase());
			if (m.getMarca() != null && !nombreIncluyeMarca) {
				sb.append(m.getMarca()).append(' ');
			}
			if (m.getNombre() != null) {
				sb.append(m.getNombre()).append(' ');
			}
		}
		sb.append(v.getYear());
		return sb.toString().trim();
	}

	/**
	 * Valor definitivo de {@code src} de la foto principal de un vehículo.
	 *
	 * @param v el vehículo (puede ser {@code null})
	 * @return enlace externo tal cual, o contexto + "/fotos/nombre" si es local; {@code null} si no tiene fotos
	 */
	public String fotoPrincipal(Vehiculo v) {
		if (v == null) {
			return null;
		}
		try {
			List<String> fotos = v.getUrl_foto();
			if (fotos == null || fotos.isEmpty() || fotos.get(0) == null || fotos.get(0).isEmpty()) {
				return null;
			}
			return src(fotos.get(0));
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * Ciudad de un vehículo.
	 *
	 * @param v el vehículo (puede ser {@code null})
	 * @return "Nombre (Departamento)" o cadena vacía
	 */
	public String ciudad(Vehiculo v) {
		if (v == null) {
			return "";
		}
		Ciudad c = v.getCodigo_ciudad();
		if (c == null || c.getNombre() == null) {
			return "";
		}
		return c.getDepartamento() == null ? c.getNombre() : c.getNombre() + " (" + c.getDepartamento() + ")";
	}

	/**
	 * Valor definitivo de {@code src} de una foto, según el contexto de la petición actual.
	 *
	 * @param foto nombre de archivo local o enlace absoluto
	 * @return enlace listo para usar en {@code <img src>}, o {@code null} si la foto es nula o vacía
	 */
	public String src(String foto) {
		String contexto = "";
		try {
			FacesContext fc = FacesContext.getCurrentInstance();
			if (fc != null) {
				contexto = fc.getExternalContext().getRequestContextPath();
			}
		} catch (RuntimeException e) {
			contexto = "";
		}
		return ConfiguracionFotos.src(foto, contexto);
	}

}
