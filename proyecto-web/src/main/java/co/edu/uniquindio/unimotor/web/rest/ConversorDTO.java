package co.edu.uniquindio.unimotor.web.rest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.edu.uniquindio.unimotor.entidades.Caracteristica;
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Modelo;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.web.fotos.ConfiguracionFotos;
import co.edu.uniquindio.unimotor.web.rest.dto.CaracteristicaDTO;
import co.edu.uniquindio.unimotor.web.rest.dto.CiudadDTO;
import co.edu.uniquindio.unimotor.web.rest.dto.ModeloDTO;
import co.edu.uniquindio.unimotor.web.rest.dto.VehiculoDetalleDTO;
import co.edu.uniquindio.unimotor.web.rest.dto.VehiculoResumenDTO;
import co.edu.uniquindio.unimotor.web.rest.dto.VendedorDTO;

/**
 * Convierte entidades en DTO de la API. Toda la lectura de relaciones perezosas ocurre
 * aquí, dentro de la petición, de modo que nunca se serializa una entidad. Es tolerante
 * a nulos y no expone datos personales.
 */
public final class ConversorDTO {

	private ConversorDTO() {
	}

	/**
	 * Convierte un vehículo en su resumen.
	 *
	 * @param v vehículo (puede ser {@code null})
	 * @return el resumen, o {@code null} si el vehículo es nulo
	 */
	public static VehiculoResumenDTO resumen(Vehiculo v) {
		if (v == null) {
			return null;
		}
		VehiculoResumenDTO d = new VehiculoResumenDTO();
		rellenarResumen(v, d);
		return d;
	}

	/**
	 * Convierte un vehículo en su detalle completo (sin datos sensibles).
	 *
	 * @param v vehículo (puede ser {@code null})
	 * @return el detalle, o {@code null} si el vehículo es nulo
	 */
	public static VehiculoDetalleDTO detalle(Vehiculo v) {
		if (v == null) {
			return null;
		}
		VehiculoDetalleDTO d = new VehiculoDetalleDTO();
		rellenarResumen(v, d);
		d.setDescripcion(v.getDescripcion());
		d.setColor(v.getColor());
		d.setCilindraje(v.getCilindraje());
		d.setNumeroPuertas(v.getNumero_puertas());
		String placa = v.getPlaca();
		d.setPlacaTerminacion(placa == null || placa.isEmpty() ? null : placa.substring(placa.length() - 1));
		List<String> fotos = new ArrayList<>();
		for (String f : listaSegura(v.getUrl_foto())) {
			if (f != null && !f.isEmpty()) {
				fotos.add(ConfiguracionFotos.URL_BASE + f);
			}
		}
		d.setFotos(fotos);
		List<String> nombres = new ArrayList<>();
		for (Caracteristica c : listaSegura(v.getCaracteristicas())) {
			if (c != null && c.getNombre() != null) {
				nombres.add(c.getNombre());
			}
		}
		Collections.sort(nombres, String.CASE_INSENSITIVE_ORDER);
		d.setCaracteristicas(nombres);
		d.setVendedor(vendedor(v.getCodigo_dueno()));
		d.setNumeroPreguntas(listaSegura(v.getPreguntas()).size());
		return d;
	}

	/**
	 * Convierte una lista de vehículos en resúmenes.
	 *
	 * @param lista vehículos (puede ser {@code null})
	 * @return lista de resúmenes, nunca nula
	 */
	public static List<VehiculoResumenDTO> resumenes(List<Vehiculo> lista) {
		List<VehiculoResumenDTO> r = new ArrayList<>();
		for (Vehiculo v : listaSegura(lista)) {
			if (v != null) {
				r.add(resumen(v));
			}
		}
		return r;
	}

	/**
	 * Convierte una ciudad.
	 *
	 * @param c ciudad (puede ser {@code null})
	 * @return el DTO, o {@code null} si es nula
	 */
	public static CiudadDTO ciudad(Ciudad c) {
		if (c == null) {
			return null;
		}
		CiudadDTO d = new CiudadDTO();
		d.setId(c.getCodigo_ciudad());
		d.setNombre(c.getNombre());
		d.setDepartamento(c.getDepartamento());
		return d;
	}

	/**
	 * Convierte una característica.
	 *
	 * @param c característica (puede ser {@code null})
	 * @return el DTO, o {@code null} si es nula
	 */
	public static CaracteristicaDTO caracteristica(Caracteristica c) {
		if (c == null) {
			return null;
		}
		CaracteristicaDTO d = new CaracteristicaDTO();
		d.setId(c.getCodigo_caracteristica());
		d.setNombre(c.getNombre());
		return d;
	}

	/**
	 * Convierte un modelo.
	 *
	 * @param m modelo (puede ser {@code null})
	 * @return el DTO, o {@code null} si es nulo
	 */
	public static ModeloDTO modelo(Modelo m) {
		if (m == null) {
			return null;
		}
		ModeloDTO d = new ModeloDTO();
		d.setId(m.getCodigo_modelo());
		d.setMarca(m.getMarca());
		d.setNombre(m.getNombre());
		return d;
	}

	/**
	 * Convierte una lista de ciudades.
	 *
	 * @param lista ciudades (puede ser {@code null})
	 * @return lista de DTO, nunca nula
	 */
	public static List<CiudadDTO> ciudades(List<Ciudad> lista) {
		List<CiudadDTO> r = new ArrayList<>();
		for (Ciudad c : listaSegura(lista)) {
			if (c != null) {
				r.add(ciudad(c));
			}
		}
		return r;
	}

	/**
	 * Convierte una lista de características.
	 *
	 * @param lista características (puede ser {@code null})
	 * @return lista de DTO, nunca nula
	 */
	public static List<CaracteristicaDTO> caracteristicas(List<Caracteristica> lista) {
		List<CaracteristicaDTO> r = new ArrayList<>();
		for (Caracteristica c : listaSegura(lista)) {
			if (c != null) {
				r.add(caracteristica(c));
			}
		}
		return r;
	}

	/**
	 * Convierte una lista de modelos.
	 *
	 * @param lista modelos (puede ser {@code null})
	 * @return lista de DTO, nunca nula
	 */
	public static List<ModeloDTO> modelos(List<Modelo> lista) {
		List<ModeloDTO> r = new ArrayList<>();
		for (Modelo m : listaSegura(lista)) {
			if (m != null) {
				r.add(modelo(m));
			}
		}
		return r;
	}

	private static void rellenarResumen(Vehiculo v, VehiculoResumenDTO d) {
		d.setId(v.getCodigo_vehiculo());
		Modelo m = v.getCodigo_modelo();
		d.setMarca(m == null ? null : m.getMarca());
		d.setModelo(m == null ? null : m.getNombre());
		d.setTitulo(titulo(v));
		d.setAnio(v.getYear());
		d.setPrecio(v.getPrecio());
		d.setKilometros(v.getKilometros());
		d.setTipo(nombre(v.getTipo_vehiculo()));
		d.setCondicion(nombre(v.getCondicion()));
		d.setCombustible(nombre(v.getTipo_combustible()));
		d.setTransmision(nombre(v.getTipo_trasmision()));
		d.setCiudad(textoCiudad(v.getCodigo_ciudad()));
		List<String> fotos = listaSegura(v.getUrl_foto());
		String primera = fotos.isEmpty() ? null : fotos.get(0);
		d.setFotoPrincipal(primera == null || primera.isEmpty() ? null : ConfiguracionFotos.URL_BASE + primera);
		d.setNumeroBusquedas(v.getNumero_busquedas());
		d.setActivo(v.isActivo());
		d.setVendido(v.getCodigo_venta() != null);
		Date fecha = v.getFecha_publicacion();
		d.setFechaPublicacion(fecha == null ? null : new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(fecha));
		d.setUrl("/detalleVehiculo.xhtml?id=" + v.getCodigo_vehiculo());
	}

	private static String titulo(Vehiculo v) {
		StringBuilder sb = new StringBuilder();
		Modelo m = v.getCodigo_modelo();
		if (m != null) {
			boolean incluyeMarca = m.getMarca() != null && m.getNombre() != null
					&& m.getNombre().toLowerCase().startsWith(m.getMarca().toLowerCase());
			if (m.getMarca() != null && !incluyeMarca) {
				sb.append(m.getMarca()).append(' ');
			}
			if (m.getNombre() != null) {
				sb.append(m.getNombre()).append(' ');
			}
		}
		sb.append(v.getYear());
		return sb.toString().trim();
	}

	private static String textoCiudad(Ciudad c) {
		if (c == null || c.getNombre() == null) {
			return null;
		}
		return c.getDepartamento() == null ? c.getNombre() : c.getNombre() + " (" + c.getDepartamento() + ")";
	}

	private static VendedorDTO vendedor(Cliente c) {
		if (c == null) {
			return null;
		}
		VendedorDTO d = new VendedorDTO();
		String completo = c.getNombre_completo() == null ? "" : c.getNombre_completo().trim();
		String[] partes = completo.isEmpty() ? new String[0] : completo.split("\\s+");
		if (partes.length == 0) {
			d.setNombre(null);
		} else if (partes.length == 1) {
			d.setNombre(partes[0]);
		} else {
			d.setNombre(partes[0] + " " + Character.toUpperCase(partes[partes.length - 1].charAt(0)) + ".");
		}
		d.setCiudad(textoCiudad(c.getCodigo_ciudad()));
		return d;
	}

	private static String nombre(Enum<?> e) {
		return e == null ? null : e.name();
	}

	private static <T> List<T> listaSegura(List<T> l) {
		return l == null ? Collections.<T>emptyList() : l;
	}
}
