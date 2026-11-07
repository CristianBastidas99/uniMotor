package co.edu.uniquindio.unimotor.web.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import co.edu.uniquindio.unimotor.ejb.VehiculoEJB;
import co.edu.uniquindio.unimotor.entidades.Condicion;
import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;
import co.edu.uniquindio.unimotor.web.rest.dto.VehiculoDetalleDTO;
import co.edu.uniquindio.unimotor.web.rest.dto.VehiculoResumenDTO;

/**
 * Recurso REST de solo lectura con los vehículos publicados. Ninguna operación altera los contadores de
 * búsquedas ni de visitas, por eso no usa {@code buscarVehiculos} ni {@code registrarVisita} del EJB.
 */
@RequestScoped
@Path("/vehiculos")
@Produces(MediaType.APPLICATION_JSON)
public class VehiculoRest {

	private static final int LIMITE_MAXIMO = 500;

	@EJB
	private VehiculoEJB vehiculoEJB;

	/**
	 * Crea el recurso.
	 */
	public VehiculoRest() {
		super();
	}

	/**
	 * Lista los vehículos activos y no vendidos, del más reciente al más antiguo, con filtros opcionales
	 * aplicados en memoria.
	 *
	 * @param tipo nombre del tipo de vehículo (sin distinguir mayúsculas)
	 * @param marca marca (sin distinguir mayúsculas)
	 * @param condicion {@code NUEVO} o {@code USADO}
	 * @param precioMin precio mínimo
	 * @param precioMax precio máximo
	 * @param anioMin año mínimo
	 * @param anioMax año máximo
	 * @param limite tamaño de página (1 a 500); si falta se devuelven todos
	 * @param pagina número de página desde 1
	 * @return respuesta con la lista de resúmenes y la cabecera {@code X-Total-Count}
	 */
	@GET
	public Response listar(@QueryParam("tipo") String tipo, @QueryParam("marca") String marca,
			@QueryParam("condicion") String condicion, @QueryParam("precioMin") String precioMin,
			@QueryParam("precioMax") String precioMax, @QueryParam("anioMin") String anioMin,
			@QueryParam("anioMax") String anioMax, @QueryParam("limite") String limite,
			@QueryParam("pagina") String pagina) {
		TipoVehiculo tipoFiltro = enumero(TipoVehiculo.class, "tipo", tipo);
		Condicion condicionFiltro = enumero(Condicion.class, "condicion", condicion);
		String marcaFiltro = vacio(marca) ? null : marca.trim();
		Double pMin = decimal("precioMin", precioMin);
		Double pMax = decimal("precioMax", precioMax);
		Integer aMin = entero("anioMin", anioMin);
		Integer aMax = entero("anioMax", anioMax);
		if (pMin != null && pMax != null && pMin > pMax) {
			throw new BadRequestException("El parámetro precioMin no puede ser mayor que precioMax.");
		}
		if (aMin != null && aMax != null && aMin > aMax) {
			throw new BadRequestException("El parámetro anioMin no puede ser mayor que anioMax.");
		}
		Integer lim = entero("limite", limite);
		Integer pag = entero("pagina", pagina);
		if (lim != null && (lim < 1 || lim > LIMITE_MAXIMO)) {
			throw new BadRequestException("El parámetro limite debe estar entre 1 y " + LIMITE_MAXIMO + ".");
		}
		if (pag != null && pag < 1) {
			throw new BadRequestException("El parámetro pagina debe ser mayor o igual que 1.");
		}

		List<Vehiculo> filtrados = new ArrayList<Vehiculo>();
		for (Vehiculo v : vehiculoEJB.listarVehiculosActivos()) {
			if (v.getCodigo_venta() != null) {
				continue;
			}
			if (tipoFiltro != null && v.getTipo_vehiculo() != tipoFiltro) {
				continue;
			}
			if (condicionFiltro != null && v.getCondicion() != condicionFiltro) {
				continue;
			}
			if (marcaFiltro != null && (v.getCodigo_modelo() == null || v.getCodigo_modelo().getMarca() == null
					|| !v.getCodigo_modelo().getMarca().equalsIgnoreCase(marcaFiltro))) {
				continue;
			}
			double precio = v.getPrecio() == null ? 0 : v.getPrecio();
			if (pMin != null && precio < pMin) {
				continue;
			}
			if (pMax != null && precio > pMax) {
				continue;
			}
			if (aMin != null && v.getYear() < aMin) {
				continue;
			}
			if (aMax != null && v.getYear() > aMax) {
				continue;
			}
			filtrados.add(v);
		}

		int total = filtrados.size();
		List<Vehiculo> seleccion = filtrados;
		if (lim != null || pag != null) {
			int tam = lim == null ? LIMITE_MAXIMO : lim;
			long desde = (long) ((pag == null ? 1 : pag) - 1) * tam;
			int ini = (int) Math.min(desde, total);
			int fin = (int) Math.min((long) ini + tam, total);
			seleccion = filtrados.subList(ini, fin);
		}
		List<VehiculoResumenDTO> cuerpo = ConversorDTO.resumenes(seleccion);
		return Response.ok(cuerpo).header("X-Total-Count", total).build();
	}

	/**
	 * Entrega los diez vehículos activos más buscados de una condición, sin modificar los contadores.
	 *
	 * @param condicion {@code NUEVO} o {@code USADO} (obligatoria)
	 * @return lista de resúmenes
	 */
	@GET
	@Path("top")
	public List<VehiculoResumenDTO> top(@QueryParam("condicion") String condicion) {
		if (vacio(condicion)) {
			throw new BadRequestException("El parámetro condicion es obligatorio (NUEVO o USADO).");
		}
		Condicion c = enumero(Condicion.class, "condicion", condicion);
		try {
			return ConversorDTO.resumenes(vehiculoEJB.top10MasBuscados(c));
		} catch (InformacionInvalidaException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	/**
	 * Entrega el detalle de un vehículo, esté activo, inactivo o vendido, sin sumar visita.
	 *
	 * @param id id del vehículo (entero positivo)
	 * @return detalle del vehículo
	 * @throws ElementoNoEncontradoException si no existe (el mapeador responde 404)
	 */
	@GET
	@Path("{id}")
	public VehiculoDetalleDTO detalle(@PathParam("id") String id) throws ElementoNoEncontradoException {
		Long codigo;
		try {
			codigo = Long.valueOf(id.trim());
		} catch (NumberFormatException e) {
			throw new BadRequestException("El id del vehículo debe ser un número entero positivo.");
		}
		if (codigo <= 0) {
			throw new BadRequestException("El id del vehículo debe ser un número entero positivo.");
		}
		return ConversorDTO.detalle(vehiculoEJB.buscarVehiculo(codigo));
	}

	private static boolean vacio(String s) {
		return s == null || s.trim().isEmpty();
	}

	private static <E extends Enum<E>> E enumero(Class<E> clase, String nombre, String valor) {
		if (vacio(valor)) {
			return null;
		}
		for (E e : clase.getEnumConstants()) {
			if (e.name().equalsIgnoreCase(valor.trim())) {
				return e;
			}
		}
		StringBuilder validos = new StringBuilder();
		for (E e : clase.getEnumConstants()) {
			validos.append(validos.length() == 0 ? "" : ", ").append(e.name());
		}
		throw new BadRequestException("El parámetro " + nombre + " no es válido. Valores permitidos: " + validos + ".");
	}

	private static Double decimal(String nombre, String valor) {
		if (vacio(valor)) {
			return null;
		}
		double d;
		try {
			d = Double.parseDouble(valor.trim());
		} catch (NumberFormatException e) {
			throw new BadRequestException("El parámetro " + nombre + " debe ser un número.");
		}
		if (Double.isNaN(d) || Double.isInfinite(d)) {
			throw new BadRequestException("El parámetro " + nombre + " debe ser un número.");
		}
		if (d < 0) {
			throw new BadRequestException("El parámetro " + nombre + " no puede ser negativo.");
		}
		return d;
	}

	private static Integer entero(String nombre, String valor) {
		if (vacio(valor)) {
			return null;
		}
		int n;
		try {
			n = Integer.parseInt(valor.trim());
		} catch (NumberFormatException e) {
			throw new BadRequestException("El parámetro " + nombre + " debe ser un número entero.");
		}
		if (n < 0) {
			throw new BadRequestException("El parámetro " + nombre + " no puede ser negativo.");
		}
		return n;
	}
}
