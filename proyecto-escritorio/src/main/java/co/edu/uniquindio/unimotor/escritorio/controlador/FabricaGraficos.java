package co.edu.uniquindio.unimotor.escritorio.controlador;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import co.edu.uniquindio.unimotor.dto.EstadisticaDTO;
import co.edu.uniquindio.unimotor.dto.PuntoDTO;
import co.edu.uniquindio.unimotor.ejb.AdministradorEJBRemote;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.shape.Ellipse;
import javafx.util.StringConverter;

/**
 * Clase de utilidad, sin estado, que construye los gráficos estadísticos de la ventana principal
 * a partir de los datos que entrega el servidor. Ofrece diecisiete gráficos de diez tipos distintos:
 * pastel, barras, barras horizontales, barras apiladas, líneas, áreas, dispersión y burbujas.
 */
public final class FabricaGraficos {

	/**
	 * Nombres de los gráficos, en el orden en que aparecen en el menú.
	 */
	public static final List<String> NOMBRES = Collections.unmodifiableList(Arrays.asList(
			"Vehículos por tipo",
			"Nuevos vs. usados",
			"Transmisión",
			"Combustible",
			"Vehículos por marca",
			"Precio promedio por marca",
			"Vehículos por ciudad",
			"Combustible por tipo",
			"Publicaciones por año del modelo",
			"Ventas por mes",
			"Precio vs. kilometraje",
			"Precio vs. año (tamaño = búsquedas)",
			"Top 10 más buscados",
			"Clientes por ciudad",
			"Preguntas por vehículo",
			"Características más frecuentes",
			"Calificación promedio por vendedor"));

	/** Ancho máximo, en píxeles, de cada barra. */
	private static final double ANCHO_MAXIMO_BARRA = 140;

	/**
	 * Nombres legibles de los valores de los enumerados.
	 */
	private static final Map<String, String> ETIQUETAS = new HashMap<String, String>();

	static {
		ETIQUETAS.put("CAMION", "Camión");
		ETIQUETAS.put("AUTOBUS", "Autobús");
		ETIQUETAS.put("CARRO", "Carro");
		ETIQUETAS.put("MOTOCICLETA", "Motocicleta");
		ETIQUETAS.put("CICLOMOTOR", "Ciclomotor");
		ETIQUETAS.put("REMOLQUE", "Remolque");
		ETIQUETAS.put("TRAILER", "Tráiler");
		ETIQUETAS.put("CAMIONETA", "Camioneta");
		ETIQUETAS.put("OTRO", "Otro");
		ETIQUETAS.put("GASOLINA", "Gasolina");
		ETIQUETAS.put("BIOETANOL", "Bioetanol");
		ETIQUETAS.put("DISEL", "Diésel");
		ETIQUETAS.put("BIODISEL", "Biodiésel");
		ETIQUETAS.put("GASNATURAL", "Gas natural");
		ETIQUETAS.put("HIBRIDO", "Híbrido");
		ETIQUETAS.put("ELECTRICO", "Eléctrico");
		ETIQUETAS.put("AUTOMATICO", "Automática");
		ETIQUETAS.put("MECANICO", "Mecánica");
		ETIQUETAS.put("NUEVO", "Nuevo");
		ETIQUETAS.put("USADO", "Usado");
	}

	/**
	 * Constructor privado: la clase solo ofrece métodos estáticos.
	 */
	private FabricaGraficos() {
	}

	/**
	 * Consulta al servidor los datos del gráfico indicado.
	 *
	 * @param nombre nombre del gráfico, uno de {@link #NOMBRES}
	 * @param ejb referencia al EJB remoto de administración
	 * @return la lista de {@code EstadisticaDTO} o de {@code PuntoDTO} correspondiente
	 * @throws IllegalArgumentException si el nombre no corresponde a ningún gráfico
	 */
	public static List<?> consultar(String nombre, AdministradorEJBRemote ejb) {
		switch (nombre) {
		case "Vehículos por tipo":
			return ejb.vehiculosPorTipo();
		case "Nuevos vs. usados":
			return ejb.vehiculosPorCondicion();
		case "Transmisión":
			return ejb.vehiculosPorTransmision();
		case "Combustible":
			return ejb.vehiculosPorCombustible();
		case "Vehículos por marca":
			return ejb.vehiculosPorMarca();
		case "Precio promedio por marca":
			return ejb.precioPromedioPorMarca();
		case "Vehículos por ciudad":
			return ejb.vehiculosPorCiudad();
		case "Combustible por tipo":
			return ejb.combustiblePorTipo();
		case "Publicaciones por año del modelo":
			return ejb.vehiculosPorAnio();
		case "Ventas por mes":
			return ejb.ventasPorMes();
		case "Precio vs. kilometraje":
			return ejb.puntosPrecioKilometraje();
		case "Precio vs. año (tamaño = búsquedas)":
			return ejb.puntosPrecioAnioBusquedas();
		case "Top 10 más buscados":
			return ejb.topVehiculosBuscados();
		case "Clientes por ciudad":
			return ejb.clientesPorCiudad();
		case "Preguntas por vehículo":
			return ejb.preguntasPorVehiculo();
		case "Características más frecuentes":
			return ejb.caracteristicasFrecuentes();
		case "Calificación promedio por vendedor":
			return ejb.promedioCalificacionPorVendedor();
		default:
			throw new IllegalArgumentException("Gráfico desconocido: " + nombre);
		}
	}

	/**
	 * Construye el gráfico indicado. Si no hay datos devuelve una etiqueta con un aviso.
	 *
	 * @param nombre nombre del gráfico, uno de {@link #NOMBRES}
	 * @param datos datos obtenidos con {@link #consultar(String, AdministradorEJBRemote)}; puede ser nulo o vacío
	 * @return el gráfico, o una etiqueta "No hay datos para este gráfico."
	 * @throws IllegalArgumentException si el nombre no corresponde a ningún gráfico
	 */
	public static Node crear(String nombre, List<?> datos) {
		if (!NOMBRES.contains(nombre)) {
			throw new IllegalArgumentException("Gráfico desconocido: " + nombre);
		}
		if (datos == null || datos.isEmpty()) {
			Label vacio = new Label("No hay datos para este gráfico.");
			vacio.setStyle("-fx-font-size: 16px; -fx-text-fill: #6b7280;");
			return vacio;
		}
		Chart grafico;
		switch (nombre) {
		case "Vehículos por tipo":
			grafico = pastel(estadisticas(datos), true);
			break;
		case "Nuevos vs. usados":
		case "Transmisión":
		case "Combustible":
			grafico = pastel(estadisticas(datos), true);
			break;
		case "Vehículos por marca":
			grafico = barras(estadisticas(datos), false, "Marca", "Cantidad de vehículos", false);
			break;
		case "Precio promedio por marca":
			grafico = barras(estadisticas(datos), false, "Marca", "Precio promedio (millones)", true);
			break;
		case "Vehículos por ciudad":
			grafico = barras(estadisticas(datos), false, "Ciudad", "Cantidad de vehículos", false);
			break;
		case "Combustible por tipo":
			grafico = apiladas(estadisticas(datos));
			break;
		case "Publicaciones por año del modelo":
			grafico = lineas(estadisticas(datos));
			break;
		case "Ventas por mes":
			grafico = area(estadisticas(datos));
			break;
		case "Precio vs. kilometraje":
			grafico = dispersion(puntos(datos));
			break;
		case "Precio vs. año (tamaño = búsquedas)":
			grafico = burbujas(puntos(datos));
			break;
		case "Top 10 más buscados":
			grafico = horizontales(estadisticas(datos));
			break;
		case "Clientes por ciudad":
			grafico = barras(estadisticas(datos), false, "Ciudad", "Cantidad de clientes", false);
			break;
		case "Preguntas por vehículo":
			grafico = barras(estadisticas(datos), false, "Vehículo", "Cantidad de preguntas", false);
			break;
		case "Características más frecuentes":
			grafico = barras(estadisticas(datos), false, "Característica", "Cantidad de vehículos", false);
			break;
		default:
			grafico = calificaciones(estadisticas(datos));
			break;
		}
		grafico.setAnimated(false);
		return grafico;
	}

	// ------------------------------------------------------------------ gráficos

	/**
	 * Gráfico de pastel con etiquetas "Nombre (n)".
	 *
	 * @param lista datos con etiquetas de enumerado
	 * @param enumerado indica si las etiquetas se traducen a nombres legibles
	 * @return el gráfico
	 */
	private static PieChart pastel(List<EstadisticaDTO> lista, boolean enumerado) {
		ObservableList<PieChart.Data> porciones = FXCollections.observableArrayList();
		for (EstadisticaDTO e : lista) {
			String nombre = enumerado ? legible(e.getEtiqueta()) : texto(e.getEtiqueta());
			porciones.add(new PieChart.Data(nombre + " (" + formato(e.getValor()) + ")", numero(e.getValor())));
		}
		PieChart grafico = new PieChart(porciones);
		grafico.setLabelsVisible(true);
		grafico.setLegendVisible(true);
		return grafico;
	}

	/**
	 * Gráfico de barras verticales con una sola serie.
	 *
	 * @param lista datos
	 * @param enumerado indica si las etiquetas se traducen a nombres legibles
	 * @param ejeX título del eje de categorías
	 * @param ejeY título del eje de valores
	 * @param millones indica si el eje de valores se muestra en millones
	 * @return el gráfico
	 */
	private static BarChart<String, Number> barras(List<EstadisticaDTO> lista, boolean enumerado, String ejeX,
			String ejeY, boolean millones) {
		CategoryAxis x = new CategoryAxis();
		x.setLabel(ejeX);
		NumberAxis y = new NumberAxis();
		y.setLabel(ejeY);
		if (millones) {
			y.setTickLabelFormatter(convertidorMillones());
		} else {
			y.setTickLabelFormatter(convertidorEntero());
		}
		XYChart.Series<String, Number> serie = serieCategorias(lista, enumerado);
		x.setCategories(categorias(serie));
		if (!millones) {
			ejeConteo(y, maximo(serie));
		}
		BarChart<String, Number> grafico = new BarChart<String, Number>(x, y);
		grafico.setLegendVisible(false);
		grafico.getData().add(serie);
		limitarAnchoBarras(grafico, x.getCategories().size(), false);
		return grafico;
	}

	/**
	 * Gráfico de barras horizontales (NumberAxis en X y CategoryAxis en Y).
	 *
	 * @param lista datos
	 * @return el gráfico
	 */
	private static BarChart<Number, String> horizontales(List<EstadisticaDTO> lista) {
		NumberAxis x = new NumberAxis();
		x.setLabel("Número de búsquedas");
		x.setTickLabelFormatter(convertidorEntero());
		CategoryAxis y = new CategoryAxis();
		y.setLabel("Vehículo");
		XYChart.Series<Number, String> serie = new XYChart.Series<Number, String>();
		List<String> nombres = new ArrayList<String>();
		for (int i = lista.size() - 1; i >= 0; i--) {
			EstadisticaDTO e = lista.get(i);
			String nombre = texto(e.getEtiqueta());
			serie.getData().add(new XYChart.Data<Number, String>(numero(e.getValor()), nombre));
			if (!nombres.contains(nombre)) {
				nombres.add(nombre);
			}
		}
		y.setCategories(FXCollections.observableArrayList(nombres));
		double maximo = 0;
		for (XYChart.Data<Number, String> d : serie.getData()) {
			maximo = Math.max(maximo, numero(d.getXValue()));
		}
		ejeConteo(x, maximo);
		BarChart<Number, String> grafico = new BarChart<Number, String>(x, y);
		grafico.setLegendVisible(false);
		grafico.getData().add(serie);
		limitarAnchoBarras(grafico, nombres.size(), true);
		return grafico;
	}

	/**
	 * Gráfico de barras apiladas: una serie por combustible y una categoría por tipo de vehículo.
	 *
	 * @param lista datos con etiqueta (tipo de vehículo), serie (combustible) y valor
	 * @return el gráfico
	 */
	private static StackedBarChart<String, Number> apiladas(List<EstadisticaDTO> lista) {
		List<String> tipos = new ArrayList<String>();
		Map<String, XYChart.Series<String, Number>> series = new LinkedHashMap<String, XYChart.Series<String, Number>>();
		for (EstadisticaDTO e : lista) {
			String tipo = legible(e.getEtiqueta());
			String combustible = legible(e.getSerie());
			if (!tipos.contains(tipo)) {
				tipos.add(tipo);
			}
			XYChart.Series<String, Number> serie = series.get(combustible);
			if (serie == null) {
				serie = new XYChart.Series<String, Number>();
				serie.setName(combustible);
				series.put(combustible, serie);
			}
			serie.getData().add(new XYChart.Data<String, Number>(tipo, numero(e.getValor())));
		}
		CategoryAxis x = new CategoryAxis();
		x.setLabel("Tipo de vehículo");
		x.setCategories(FXCollections.observableArrayList(tipos));
		NumberAxis y = new NumberAxis();
		y.setLabel("Cantidad de vehículos");
		y.setTickLabelFormatter(convertidorEntero());
		Map<String, Double> totales = new HashMap<String, Double>();
		for (EstadisticaDTO e : lista) {
			String tipo = legible(e.getEtiqueta());
			Double total = totales.get(tipo);
			totales.put(tipo, (total == null ? 0 : total) + numero(e.getValor()));
		}
		ejeConteo(y, totales.isEmpty() ? 0 : Collections.max(totales.values()));
		StackedBarChart<String, Number> grafico = new StackedBarChart<String, Number>(x, y);
		grafico.setLegendVisible(true);
		grafico.getData().addAll(series.values());
		grafico.categoryGapProperty().bind(Bindings.createDoubleBinding(
				() -> espacioCategorias(grafico.getWidth(), tipos.size()), grafico.widthProperty()));
		return grafico;
	}

	/**
	 * Gráfico de líneas de las publicaciones por año del modelo.
	 *
	 * @param lista datos con el año como etiqueta
	 * @return el gráfico
	 */
	private static LineChart<String, Number> lineas(List<EstadisticaDTO> lista) {
		CategoryAxis x = new CategoryAxis();
		x.setLabel("Año del modelo");
		NumberAxis y = new NumberAxis();
		y.setLabel("Cantidad de vehículos");
		y.setTickLabelFormatter(convertidorEntero());
		XYChart.Series<String, Number> serie = serieCategorias(lista, false);
		serie.setName("Publicaciones");
		x.setCategories(categorias(serie));
		ejeConteo(y, maximo(serie));
		LineChart<String, Number> grafico = new LineChart<String, Number>(x, y);
		grafico.setLegendVisible(false);
		grafico.getData().add(serie);
		return grafico;
	}

	/**
	 * Gráfico de áreas de las ventas por mes.
	 *
	 * @param lista datos con el mes como etiqueta
	 * @return el gráfico
	 */
	private static AreaChart<String, Number> area(List<EstadisticaDTO> lista) {
		CategoryAxis x = new CategoryAxis();
		x.setLabel("Mes");
		NumberAxis y = new NumberAxis();
		y.setLabel("Cantidad de ventas");
		y.setTickLabelFormatter(convertidorEntero());
		XYChart.Series<String, Number> serie = serieCategorias(lista, false);
		serie.setName("Ventas");
		x.setCategories(categorias(serie));
		ejeConteo(y, maximo(serie));
		AreaChart<String, Number> grafico = new AreaChart<String, Number>(x, y);
		grafico.setLegendVisible(false);
		grafico.getData().add(serie);
		return grafico;
	}

	/**
	 * Gráfico de dispersión de precio contra kilometraje.
	 *
	 * @param lista puntos con x: kilómetros; y: precio
	 * @return el gráfico
	 */
	private static ScatterChart<Number, Number> dispersion(List<PuntoDTO> lista) {
		NumberAxis x = new NumberAxis();
		x.setLabel("Kilometraje");
		x.setForceZeroInRange(false);
		x.setTickLabelFormatter(convertidorEntero());
		NumberAxis y = new NumberAxis();
		y.setLabel("Precio (millones)");
		y.setForceZeroInRange(false);
		y.setTickLabelFormatter(convertidorMillones());
		XYChart.Series<Number, Number> serie = new XYChart.Series<Number, Number>();
		serie.setName("Vehículos");
		for (PuntoDTO p : lista) {
			serie.getData().add(new XYChart.Data<Number, Number>(numero(p.getX()), numero(p.getY())));
		}
		ScatterChart<Number, Number> grafico = new ScatterChart<Number, Number>(x, y);
		grafico.setLegendVisible(false);
		grafico.getData().add(serie);
		return grafico;
	}

	/**
	 * Gráfico de burbujas de precio contra año; el tamaño de la burbuja es el número de búsquedas.
	 * En JavaFX el tercer valor es el radio en unidades del eje X, por lo que se escala como
	 * rangoX * 0,04 * raíz(tamaño / tamaño máximo), con un mínimo visible. Se usa {@link BurbujasCirculares}
	 * para que la burbuja sea un círculo (JavaFX aplicaría el mismo valor en unidades del eje Y, que va en
	 * millones de pesos, y la burbuja quedaría aplastada e invisible).
	 *
	 * @param lista puntos con x: año; y: precio; tamaño: búsquedas
	 * @return el gráfico
	 */
	private static BubbleChart<Number, Number> burbujas(List<PuntoDTO> lista) {
		double minX = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		double maxTamano = 0;
		for (PuntoDTO p : lista) {
			minX = Math.min(minX, numero(p.getX()));
			maxX = Math.max(maxX, numero(p.getX()));
			minY = Math.min(minY, numero(p.getY()));
			maxY = Math.max(maxY, numero(p.getY()));
			maxTamano = Math.max(maxTamano, numero(p.getTamano()));
		}
		NumberAxis x = new NumberAxis();
		x.setLabel("Año");
		x.setForceZeroInRange(false);
		x.setTickLabelFormatter(convertidorEntero());
		NumberAxis y = new NumberAxis();
		y.setLabel("Precio (millones)");
		y.setForceZeroInRange(false);
		y.setTickLabelFormatter(convertidorMillones());
		ajustarEje(x, minX, maxX, 0.12);
		ajustarEje(y, minY, maxY, 0.10);
		double rangoX = x.getUpperBound() - x.getLowerBound();
		double minimo = rangoX * 0.008;
		XYChart.Series<Number, Number> serie = new XYChart.Series<Number, Number>();
		serie.setName("Vehículos");
		for (PuntoDTO p : lista) {
			double tamano = numero(p.getTamano());
			double radio = maxTamano > 0 ? rangoX * 0.04 * Math.sqrt(tamano / maxTamano) : minimo;
			serie.getData().add(new XYChart.Data<Number, Number>(numero(p.getX()), numero(p.getY()),
					Double.valueOf(Math.max(radio, minimo))));
		}
		BubbleChart<Number, Number> grafico = new BurbujasCirculares(x, y);
		grafico.setLegendVisible(false);
		grafico.getData().add(serie);
		return grafico;
	}

	/**
	 * Gráfico de burbujas cuyas burbujas son círculos: el radio se toma en unidades del eje X y se
	 * aplica igual en vertical, sin depender de la escala del eje Y.
	 */
	private static class BurbujasCirculares extends BubbleChart<Number, Number> {

		/**
		 * Crea el gráfico con sus ejes.
		 *
		 * @param x eje horizontal
		 * @param y eje vertical
		 */
		BurbujasCirculares(NumberAxis x, NumberAxis y) {
			super(x, y);
		}

		/**
		 * Ubica las burbujas como lo hace JavaFX y luego iguala el radio vertical al horizontal.
		 */
		@Override
		protected void layoutPlotChildren() {
			super.layoutPlotChildren();
			for (XYChart.Series<Number, Number> serie : getData()) {
				for (XYChart.Data<Number, Number> dato : serie.getData()) {
					if (dato.getNode() instanceof Region && ((Region) dato.getNode()).getShape() instanceof Ellipse) {
						Region region = (Region) dato.getNode();
						Ellipse elipse = (Ellipse) region.getShape();
						Ellipse circulo = new Ellipse(elipse.getRadiusX(), elipse.getRadiusX());
						region.setShape(circulo);
						region.resize(circulo.getRadiusX() * 2, circulo.getRadiusY() * 2);
					}
				}
			}
		}
	}

	/**
	 * Gráfico de barras de la calificación promedio, con el eje de valores entre 0 y 5.
	 *
	 * @param lista datos con el vendedor como etiqueta
	 * @return el gráfico
	 */
	private static BarChart<String, Number> calificaciones(List<EstadisticaDTO> lista) {
		CategoryAxis x = new CategoryAxis();
		x.setLabel("Vendedor");
		NumberAxis y = new NumberAxis(0, 5, 1);
		y.setAutoRanging(false);
		y.setLabel("Calificación promedio (estrellas)");
		y.setTickLabelFormatter(convertidorEntero());
		XYChart.Series<String, Number> serie = serieCategorias(lista, false);
		x.setCategories(categorias(serie));
		BarChart<String, Number> grafico = new BarChart<String, Number>(x, y);
		grafico.setLegendVisible(false);
		grafico.getData().add(serie);
		limitarAnchoBarras(grafico, x.getCategories().size(), false);
		return grafico;
	}

	// ------------------------------------------------------------------ auxiliares

	/**
	 * Crea una serie con una categoría por elemento.
	 *
	 * @param lista datos
	 * @param enumerado indica si las etiquetas se traducen a nombres legibles
	 * @return la serie
	 */
	private static XYChart.Series<String, Number> serieCategorias(List<EstadisticaDTO> lista, boolean enumerado) {
		XYChart.Series<String, Number> serie = new XYChart.Series<String, Number>();
		for (EstadisticaDTO e : lista) {
			String nombre = enumerado ? legible(e.getEtiqueta()) : texto(e.getEtiqueta());
			serie.getData().add(new XYChart.Data<String, Number>(nombre, numero(e.getValor())));
		}
		return serie;
	}

	/**
	 * Obtiene las categorías distintas de una serie, en su orden.
	 *
	 * @param serie serie de la que se leen las categorías
	 * @return lista observable de categorías sin repetir
	 */
	private static ObservableList<String> categorias(XYChart.Series<String, Number> serie) {
		List<String> nombres = new ArrayList<String>();
		for (XYChart.Data<String, Number> d : serie.getData()) {
			if (!nombres.contains(d.getXValue())) {
				nombres.add(d.getXValue());
			}
		}
		return FXCollections.observableArrayList(nombres);
	}

	/**
	 * Fija manualmente los límites y la marca de un eje, con un margen alrededor de los datos.
	 *
	 * @param eje eje a configurar
	 * @param min valor mínimo de los datos
	 * @param max valor máximo de los datos
	 * @param margen margen relativo al rango de los datos
	 */
	private static void ajustarEje(NumberAxis eje, double min, double max, double margen) {
		double rango = max - min;
		if (rango <= 0) {
			rango = Math.max(Math.abs(max) * 0.1, 1);
		}
		double paso = pasoLimpio(rango * (1 + 2 * margen) / 8);
		eje.setAutoRanging(false);
		eje.setLowerBound(Math.floor((min - rango * margen) / paso) * paso);
		eje.setUpperBound(Math.ceil((max + rango * margen) / paso) * paso);
		eje.setTickUnit(paso);
	}

	/**
	 * Evita que las barras ocupen todo el ancho cuando hay pocas categorías: el espacio entre categorías
	 * crece con el tamaño del gráfico para que cada barra mida a lo sumo 140 píxeles.
	 *
	 * @param grafico gráfico de barras
	 * @param categorias número de categorías
	 * @param horizontal true si las barras son horizontales (se usa la altura del gráfico)
	 */
	private static void limitarAnchoBarras(BarChart<?, ?> grafico, int categorias, boolean horizontal) {
		grafico.categoryGapProperty().bind(Bindings.createDoubleBinding(
				() -> espacioCategorias(horizontal ? grafico.getHeight() : grafico.getWidth(), categorias),
				horizontal ? grafico.heightProperty() : grafico.widthProperty()));
	}

	/**
	 * Calcula el espacio entre categorías para que la barra no supere el ancho máximo.
	 *
	 * @param tamano ancho (o alto) del gráfico en píxeles
	 * @param categorias número de categorías
	 * @return el espacio en píxeles, como mínimo 10
	 */
	private static double espacioCategorias(double tamano, int categorias) {
		double porCategoria = (tamano - 120) / Math.max(categorias, 1);
		return Math.max(10, porCategoria - ANCHO_MAXIMO_BARRA);
	}

	/**
	 * Configura un eje de cantidades: empieza en 0, usa un paso entero (1, 2, 5, 10...) y deja un margen
	 * sobre el valor máximo, para que no aparezcan marcas decimales como 0,1 o 1,1.
	 *
	 * @param eje eje a configurar
	 * @param max valor máximo de los datos
	 */
	private static void ejeConteo(NumberAxis eje, double max) {
		double paso = Math.max(1, pasoLimpio(Math.max(max, 1) / 8));
		eje.setAutoRanging(false);
		eje.setLowerBound(0);
		eje.setUpperBound(Math.max(paso, Math.ceil(max * 1.1 / paso) * paso));
		eje.setTickUnit(paso);
		eje.setMinorTickVisible(false);
	}

	/**
	 * Devuelve el mayor valor de una serie de categorías.
	 *
	 * @param serie serie con valores numéricos en Y
	 * @return el máximo, o 0 si la serie está vacía
	 */
	private static double maximo(XYChart.Series<String, Number> serie) {
		double max = 0;
		for (XYChart.Data<String, Number> d : serie.getData()) {
			max = Math.max(max, numero(d.getYValue()));
		}
		return max;
	}

	/**
	 * Redondea un paso al valor 1, 2, 5 o 10 por una potencia de diez.
	 *
	 * @param bruto paso aproximado
	 * @return paso redondeado
	 */
	private static double pasoLimpio(double bruto) {
		double potencia = Math.pow(10, Math.floor(Math.log10(bruto)));
		double fraccion = bruto / potencia;
		double base;
		if (fraccion <= 1) {
			base = 1;
		} else if (fraccion <= 2) {
			base = 2;
		} else if (fraccion <= 5) {
			base = 5;
		} else {
			base = 10;
		}
		return base * potencia;
	}

	/**
	 * Convierte los elementos de la lista a {@code EstadisticaDTO}.
	 *
	 * @param datos lista devuelta por el servidor
	 * @return lista tipada
	 */
	private static List<EstadisticaDTO> estadisticas(List<?> datos) {
		List<EstadisticaDTO> lista = new ArrayList<EstadisticaDTO>();
		for (Object o : datos) {
			lista.add((EstadisticaDTO) o);
		}
		return lista;
	}

	/**
	 * Convierte los elementos de la lista a {@code PuntoDTO}.
	 *
	 * @param datos lista devuelta por el servidor
	 * @return lista tipada
	 */
	private static List<PuntoDTO> puntos(List<?> datos) {
		List<PuntoDTO> lista = new ArrayList<PuntoDTO>();
		for (Object o : datos) {
			lista.add((PuntoDTO) o);
		}
		return lista;
	}

	/**
	 * Traduce el nombre de un enumerado a un texto legible; si no está en el mapa, deja la primera
	 * letra en mayúscula y el resto en minúscula.
	 *
	 * @param valor nombre del enumerado, por ejemplo {@code DISEL}
	 * @return texto legible, por ejemplo "Diésel"
	 */
	static String legible(String valor) {
		if (valor == null || valor.isEmpty()) {
			return "Sin dato";
		}
		String conocido = ETIQUETAS.get(valor);
		if (conocido != null) {
			return conocido;
		}
		String minuscula = valor.toLowerCase(Locale.ROOT);
		return Character.toUpperCase(minuscula.charAt(0)) + minuscula.substring(1);
	}

	/**
	 * Devuelve un texto no nulo para usar como categoría.
	 *
	 * @param valor texto original
	 * @return el texto, o "Sin dato" si es nulo o vacío
	 */
	private static String texto(String valor) {
		return valor == null || valor.isEmpty() ? "Sin dato" : valor;
	}

	/**
	 * Convierte un número posiblemente nulo a {@code double}.
	 *
	 * @param n número o nulo
	 * @return el valor, o cero si es nulo
	 */
	private static double numero(Number n) {
		return n == null ? 0 : n.doubleValue();
	}

	/**
	 * Da formato a un número: sin decimales si es entero y con hasta dos decimales en otro caso.
	 *
	 * @param n número o nulo
	 * @return texto del número
	 */
	private static String formato(Number n) {
		double v = numero(n);
		DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("es", "CO"));
		if (v == Math.rint(v)) {
			return new DecimalFormat("0", simbolos).format(v);
		}
		return new DecimalFormat("0.##", simbolos).format(v);
	}

	/**
	 * Convertidor para ejes numéricos: números enteros sin separador de miles.
	 *
	 * @return convertidor de etiquetas de eje
	 */
	private static StringConverter<Number> convertidorEntero() {
		return new StringConverter<Number>() {
			@Override
			public String toString(Number objeto) {
				return objeto == null ? "" : formato(objeto);
			}

			@Override
			public Number fromString(String cadena) {
				try {
					return Double.valueOf(cadena);
				} catch (NumberFormatException e) {
					return Double.valueOf(0);
				}
			}
		};
	}

	/**
	 * Convertidor para ejes de precios: muestra el valor en millones, por ejemplo "26 M".
	 *
	 * @return convertidor de etiquetas de eje
	 */
	private static StringConverter<Number> convertidorMillones() {
		return new StringConverter<Number>() {
			@Override
			public String toString(Number objeto) {
				return objeto == null ? "" : formato(Double.valueOf(objeto.doubleValue() / 1000000d)) + " M";
			}

			@Override
			public Number fromString(String cadena) {
				try {
					return Double.valueOf(cadena.replace("M", "").trim().replace(',', '.')) * 1000000d;
				} catch (NumberFormatException e) {
					return Double.valueOf(0);
				}
			}
		};
	}
}
