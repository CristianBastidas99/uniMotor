package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import co.edu.uniquindio.unimotor.ejb.CatalogoEJB;
import co.edu.uniquindio.unimotor.ejb.VehiculoEJB;
import co.edu.uniquindio.unimotor.entidades.Caracteristica;
import co.edu.uniquindio.unimotor.entidades.Condicion;
import co.edu.uniquindio.unimotor.entidades.TipoCombustible;
import co.edu.uniquindio.unimotor.entidades.TipoTrasmision;
import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;
import co.edu.uniquindio.unimotor.web.util.Mensajes;

/**
 * Bean de la página de resultados. La búsqueda en base de datos se hace una sola vez al cargar
 * la página (porque suma una búsqueda a cada vehículo devuelto); los filtros adicionales, el
 * orden y la paginación trabajan en memoria sobre esa lista base.
 */
@Named
@ViewScoped
public class ResultadosBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String ORDEN_RECIENTES = "recientes";

	@EJB
	private VehiculoEJB vehiculoEJB;

	@EJB
	private CatalogoEJB catalogoEJB;

	@Inject
	private BuscadorBean buscadorBean;

	@Inject
	private FormatoBean formatoBean;

	/** Criterios copiados del buscador en la primera carga. */
	private TipoVehiculo tipo;
	private String marca;
	private Double precioMin;
	private Double precioMax;
	private Integer anioMin;
	private Integer anioMax;

	/** Resultado de la búsqueda (lista base). */
	private List<Vehiculo> todos = new ArrayList<>();

	/** Lista mostrada: base con filtros adicionales y orden. */
	private List<Vehiculo> resultados = new ArrayList<>();

	private List<Long> caracteristicasSeleccionadas = new ArrayList<>();
	private TipoCombustible combustible;
	private TipoTrasmision transmision;
	private Condicion condicion;
	private Integer kilometrajeMaximo;
	private String orden = ORDEN_RECIENTES;

	/** Índice del primer elemento de la página mostrada por el DataView. */
	private int primero;

	private List<Caracteristica> caracteristicas;
	private List<TipoCombustible> combustibles;
	private List<TipoTrasmision> transmisiones;
	private List<Condicion> condiciones;

	/**
	 * Carga los catálogos de los filtros una sola vez.
	 */
	@PostConstruct
	public void cargarCatalogos() {
		caracteristicas = catalogoEJB.listarCaracteristicas();
		combustibles = catalogoEJB.listarCombustibles();
		transmisiones = catalogoEJB.listarTransmisiones();
		condiciones = catalogoEJB.listarCondiciones();
	}

	/**
	 * Copia los criterios del buscador (ya cargados por los parámetros de la URL) y ejecuta
	 * la búsqueda una única vez. Se invoca solo en la primera carga de la vista.
	 */
	public void inicializar() {
		tipo = buscadorBean.getTipo();
		marca = buscadorBean.getMarca();
		precioMin = buscadorBean.getPrecioMin();
		precioMax = buscadorBean.getPrecioMax();
		anioMin = buscadorBean.getAnioMin();
		anioMax = buscadorBean.getAnioMax();
		try {
			todos = new ArrayList<>(vehiculoEJB.buscarVehiculos(tipo, marca, precioMin, precioMax, anioMin, anioMax));
		} catch (InformacionInvalidaException e) {
			Mensajes.error("Criterios inválidos", e.getMessage());
			todos = new ArrayList<>();
		}
		aplicarFiltros();
	}

	/**
	 * Recalcula la lista mostrada a partir de la lista base, con los filtros adicionales y el
	 * orden elegidos, sin consultar de nuevo la búsqueda. Vuelve a la primera página.
	 */
	public void aplicarFiltros() {
		List<Vehiculo> lista = new ArrayList<>(todos);
		if (caracteristicasSeleccionadas != null && !caracteristicasSeleccionadas.isEmpty()) {
			lista = new ArrayList<>(vehiculoEJB.filtrarPorCaracteristicas(lista, caracteristicasSeleccionadas));
		}
		List<Vehiculo> filtrada = new ArrayList<>();
		for (Vehiculo v : lista) {
			if (combustible != null && combustible != v.getTipo_combustible()) {
				continue;
			}
			if (transmision != null && transmision != v.getTipo_trasmision()) {
				continue;
			}
			if (condicion != null && condicion != v.getCondicion()) {
				continue;
			}
			if (kilometrajeMaximo != null && v.getKilometros() > kilometrajeMaximo) {
				continue;
			}
			filtrada.add(v);
		}
		Collections.sort(filtrada, comparadorOrden());
		resultados = filtrada;
		primero = 0;
	}

	/**
	 * Quita los filtros adicionales y recalcula la lista.
	 */
	public void limpiarFiltros() {
		caracteristicasSeleccionadas = new ArrayList<>();
		combustible = null;
		transmision = null;
		condicion = null;
		kilometrajeMaximo = null;
		aplicarFiltros();
	}

	private Comparator<Vehiculo> comparadorOrden() {
		String o = orden == null ? ORDEN_RECIENTES : orden;
		switch (o) {
		case "precioAsc":
			return Comparator.comparing(Vehiculo::getPrecio, Comparator.nullsLast(Comparator.<Double>naturalOrder()));
		case "precioDesc":
			return Comparator.comparing(Vehiculo::getPrecio, Comparator.nullsLast(Comparator.<Double>reverseOrder()));
		case "anioDesc":
			return Comparator.comparingInt(Vehiculo::getYear).reversed();
		case "anioAsc":
			return Comparator.comparingInt(Vehiculo::getYear);
		case "kmAsc":
			return Comparator.comparingInt(Vehiculo::getKilometros);
		default:
			return Comparator.comparing(Vehiculo::getFecha_publicacion,
					Comparator.nullsLast(Comparator.<Date>reverseOrder()));
		}
	}

	/**
	 * Opciones del selector de orden.
	 *
	 * @return lista de opciones con etiquetas en español
	 */
	public List<SelectItem> getOpcionesOrden() {
		List<SelectItem> items = new ArrayList<>();
		items.add(new SelectItem(ORDEN_RECIENTES, "Más recientes"));
		items.add(new SelectItem("precioAsc", "Precio: menor a mayor"));
		items.add(new SelectItem("precioDesc", "Precio: mayor a menor"));
		items.add(new SelectItem("anioDesc", "Año: más nuevos primero"));
		items.add(new SelectItem("anioAsc", "Año: más antiguos primero"));
		items.add(new SelectItem("kmAsc", "Menor kilometraje"));
		return items;
	}

	/**
	 * Número de vehículos mostrados tras los filtros.
	 *
	 * @return cantidad de resultados
	 */
	public int getTotal() {
		return resultados.size();
	}

	/**
	 * Texto con los criterios de la búsqueda.
	 *
	 * @return resumen legible o "Todos los vehículos" si no hay criterios
	 */
	public String getResumenCriterios() {
		List<String> partes = new ArrayList<>();
		if (tipo != null) {
			partes.add(formatoBean.etiqueta(tipo));
		}
		if (marca != null && !marca.trim().isEmpty()) {
			partes.add(marca.trim());
		}
		if (precioMin != null && precioMax != null) {
			partes.add(formatoBean.precio(precioMin) + " – " + formatoBean.precio(precioMax));
		} else if (precioMin != null) {
			partes.add("Desde " + formatoBean.precio(precioMin));
		} else if (precioMax != null) {
			partes.add("Hasta " + formatoBean.precio(precioMax));
		}
		if (anioMin != null && anioMax != null) {
			partes.add(anioMin + "–" + anioMax);
		} else if (anioMin != null) {
			partes.add("Desde " + anioMin);
		} else if (anioMax != null) {
			partes.add("Hasta " + anioMax);
		}
		if (partes.isEmpty()) {
			return "Todos los vehículos";
		}
		return String.join(" · ", partes);
	}

	/**
	 * Catálogo de características.
	 *
	 * @return características ordenadas
	 */
	public List<Caracteristica> getCaracteristicas() {
		return caracteristicas;
	}

	/**
	 * Catálogo de combustibles.
	 *
	 * @return combustibles
	 */
	public List<TipoCombustible> getCombustibles() {
		return combustibles;
	}

	/**
	 * Catálogo de transmisiones.
	 *
	 * @return transmisiones
	 */
	public List<TipoTrasmision> getTransmisiones() {
		return transmisiones;
	}

	/**
	 * Catálogo de condiciones.
	 *
	 * @return condiciones
	 */
	public List<Condicion> getCondiciones() {
		return condiciones;
	}

	/**
	 * Vehículos mostrados.
	 *
	 * @return lista filtrada y ordenada
	 */
	public List<Vehiculo> getResultados() {
		return resultados;
	}

	/**
	 * Códigos de las características elegidas.
	 *
	 * @return lista de códigos
	 */
	public List<Long> getCaracteristicasSeleccionadas() {
		return caracteristicasSeleccionadas;
	}

	/**
	 * Establece las características elegidas.
	 *
	 * @param caracteristicasSeleccionadas códigos elegidos
	 */
	public void setCaracteristicasSeleccionadas(List<Long> caracteristicasSeleccionadas) {
		this.caracteristicasSeleccionadas = caracteristicasSeleccionadas;
	}

	/**
	 * Combustible elegido.
	 *
	 * @return combustible o {@code null}
	 */
	public TipoCombustible getCombustible() {
		return combustible;
	}

	/**
	 * Establece el combustible elegido.
	 *
	 * @param combustible combustible
	 */
	public void setCombustible(TipoCombustible combustible) {
		this.combustible = combustible;
	}

	/**
	 * Transmisión elegida.
	 *
	 * @return transmisión o {@code null}
	 */
	public TipoTrasmision getTransmision() {
		return transmision;
	}

	/**
	 * Establece la transmisión elegida.
	 *
	 * @param transmision transmisión
	 */
	public void setTransmision(TipoTrasmision transmision) {
		this.transmision = transmision;
	}

	/**
	 * Condición elegida.
	 *
	 * @return condición o {@code null}
	 */
	public Condicion getCondicion() {
		return condicion;
	}

	/**
	 * Establece la condición elegida.
	 *
	 * @param condicion condición
	 */
	public void setCondicion(Condicion condicion) {
		this.condicion = condicion;
	}

	/**
	 * Kilometraje máximo elegido.
	 *
	 * @return kilómetros o {@code null}
	 */
	public Integer getKilometrajeMaximo() {
		return kilometrajeMaximo;
	}

	/**
	 * Establece el kilometraje máximo.
	 *
	 * @param kilometrajeMaximo kilómetros
	 */
	public void setKilometrajeMaximo(Integer kilometrajeMaximo) {
		this.kilometrajeMaximo = kilometrajeMaximo;
	}

	/**
	 * Orden elegido.
	 *
	 * @return clave del orden
	 */
	public String getOrden() {
		return orden;
	}

	/**
	 * Establece el orden.
	 *
	 * @param orden clave del orden
	 */
	public void setOrden(String orden) {
		this.orden = orden;
	}

	/**
	 * Índice del primer elemento de la página actual.
	 *
	 * @return índice
	 */
	public int getPrimero() {
		return primero;
	}

	/**
	 * Establece el índice del primer elemento de la página actual.
	 *
	 * @param primero índice
	 */
	public void setPrimero(int primero) {
		this.primero = primero;
	}
}
