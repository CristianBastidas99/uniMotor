package co.edu.uniquindio.unimotor.escritorio.controlador;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import co.edu.uniquindio.unimotor.entidades.Administrador;
import co.edu.uniquindio.unimotor.escritorio.app.Aplicacion;
import co.edu.uniquindio.unimotor.escritorio.modelo.AdministradorDelegado;
import co.edu.uniquindio.unimotor.escritorio.modelo.ErrorConexionException;
import co.edu.uniquindio.unimotor.escritorio.modelo.Sesion;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controlador de la ventana principal del administrador: muestra un menú con los gráficos
 * estadísticos y el gráfico seleccionado. Las consultas al servidor se hacen en un hilo aparte
 * y la vista se reconstruye siempre en el hilo de JavaFX.
 */
public class PrincipalController implements Initializable {

	/**
	 * Formato de la hora que se muestra en la barra de estado.
	 */
	private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

	/**
	 * Etiqueta con el nombre del administrador.
	 */
	@FXML
	private Label etiquetaUsuario;
	/**
	 * Botón para volver a cargar los datos.
	 */
	@FXML
	private Button botonRefrescar;
	/**
	 * Botón para cerrar la sesión.
	 */
	@FXML
	private Button botonCerrarSesion;
	/**
	 * Menú lateral con un botón por gráfico.
	 */
	@FXML
	private VBox menu;
	/**
	 * Zona central donde se muestra el gráfico.
	 */
	@FXML
	private StackPane contenedorGrafico;
	/**
	 * Etiqueta de la barra de estado.
	 */
	@FXML
	private Label estado;

	/**
	 * Datos cargados, por nombre de gráfico.
	 */
	private final Map<String, List<?>> datos = new LinkedHashMap<String, List<?>>();
	/**
	 * Botones del menú, por nombre de gráfico.
	 */
	private final Map<String, Button> botones = new LinkedHashMap<String, Button>();
	/**
	 * Nombre del gráfico seleccionado.
	 */
	private String seleccionado;
	/**
	 * Indica si hay una carga de datos en curso.
	 */
	private boolean cargando;

	/**
	 * Prepara la ventana: muestra el nombre del administrador, construye el menú y carga los datos.
	 *
	 * @param location ubicación del FXML; no se usa
	 * @param resources recursos de internacionalización; no se usan
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Administrador admin = Sesion.getAdministrador();
		String nombre = admin == null ? null : admin.getNombre_administrador();
		etiquetaUsuario.setText("Administrador: " + (nombre == null ? "" : nombre));
		for (String nombreGrafico : FabricaGraficos.NOMBRES) {
			Button boton = new Button(nombreGrafico);
			boton.getStyleClass().add("boton-menu");
			boton.setMaxWidth(Double.MAX_VALUE);
			boton.setOnAction(e -> mostrarGrafico(nombreGrafico));
			botones.put(nombreGrafico, boton);
			menu.getChildren().add(boton);
		}
		seleccionado = FabricaGraficos.NOMBRES.get(0);
		cargarDatos();
	}

	/**
	 * Devuelve los nombres de los gráficos disponibles, en el orden del menú.
	 *
	 * @return lista de nombres de gráficos
	 */
	public List<String> getNombresGraficos() {
		return new ArrayList<String>(FabricaGraficos.NOMBRES);
	}

	/**
	 * Muestra el gráfico indicado en la zona central, con su título. Debe llamarse desde el hilo de JavaFX.
	 *
	 * @param nombre nombre del gráfico, uno de los devueltos por {@link #getNombresGraficos()}
	 * @throws IllegalArgumentException si el nombre no corresponde a ningún gráfico
	 */
	public void mostrarGrafico(String nombre) {
		if (!FabricaGraficos.NOMBRES.contains(nombre)) {
			throw new IllegalArgumentException("Gráfico desconocido: " + nombre);
		}
		seleccionado = nombre;
		for (Map.Entry<String, Button> e : botones.entrySet()) {
			e.getValue().getStyleClass().remove("boton-menu-activo");
			if (e.getKey().equals(nombre)) {
				e.getValue().getStyleClass().add("boton-menu-activo");
			}
		}
		Label titulo = new Label(nombre);
		titulo.getStyleClass().add("titulo-grafico");
		Node grafico = FabricaGraficos.crear(nombre, datos.get(nombre));
		VBox caja = new VBox(8, titulo, grafico);
		caja.setPadding(new Insets(4));
		VBox.setVgrow(grafico, Priority.ALWAYS);
		contenedorGrafico.getChildren().setAll(caja);
	}

	/**
	 * Establece directamente los datos de los gráficos, sin consultar al servidor, y vuelve a dibujar
	 * el gráfico seleccionado. Debe llamarse desde el hilo de JavaFX.
	 *
	 * @param nuevos datos por nombre de gráfico
	 */
	public void establecerDatos(Map<String, List<?>> nuevos) {
		datos.clear();
		datos.putAll(nuevos);
		mostrarGrafico(seleccionado);
	}

	/**
	 * Vuelve a cargar todas las estadísticas y dibuja de nuevo el gráfico seleccionado.
	 */
	@FXML
	public void refrescar() {
		cargarDatos();
	}

	/**
	 * Pide confirmación y, si se acepta, cierra la sesión y vuelve a la pantalla de inicio.
	 */
	@FXML
	public void cerrarSesion() {
		Alert alerta = new Alert(AlertType.CONFIRMATION, "¿Desea cerrar sesión?", ButtonType.OK, ButtonType.CANCEL);
		alerta.setTitle("Cerrar sesión");
		alerta.setHeaderText(null);
		Optional<ButtonType> respuesta = alerta.showAndWait();
		if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
			Sesion.cerrar();
			Aplicacion.mostrarInicio();
		}
	}

	/**
	 * Lanza en un hilo daemon la consulta de las diecisiete estadísticas y, al terminar, redibuja la vista.
	 */
	private void cargarDatos() {
		if (cargando) {
			return;
		}
		establecerCarga(true);
		Task<Map<String, List<?>>> tarea = new Task<Map<String, List<?>>>() {
			@Override
			protected Map<String, List<?>> call() throws Exception {
				AdministradorDelegado ejb = AdministradorDelegado.getInstancia();
				Map<String, List<?>> resultado = new LinkedHashMap<String, List<?>>();
				for (String nombre : FabricaGraficos.NOMBRES) {
					resultado.put(nombre, FabricaGraficos.consultar(nombre, ejb));
				}
				return resultado;
			}
		};
		tarea.setOnSucceeded(e -> {
			datos.clear();
			datos.putAll(tarea.getValue());
			establecerCarga(false);
			mostrarGrafico(seleccionado);
			estado.setText("Datos actualizados a las " + LocalTime.now().format(FORMATO_HORA));
		});
		tarea.setOnFailed(e -> {
			Throwable error = tarea.getException();
			establecerCarga(false);
			String mensaje = error instanceof ErrorConexionException ? error.getMessage()
					: "Ocurrió un error al cargar las estadísticas.";
			estado.setText(mensaje);
			Alert alerta = new Alert(AlertType.ERROR, mensaje, ButtonType.OK);
			alerta.setTitle("Error");
			alerta.setHeaderText(null);
			alerta.show();
			mostrarGrafico(seleccionado);
		});
		Thread hilo = new Thread(tarea, "carga-estadisticas");
		hilo.setDaemon(true);
		hilo.start();
	}

	/**
	 * Activa o desactiva el estado de carga: indicador de progreso y botones deshabilitados.
	 *
	 * @param activo verdadero mientras se cargan los datos
	 */
	private void establecerCarga(boolean activo) {
		cargando = activo;
		botonRefrescar.setDisable(activo);
		for (Button b : botones.values()) {
			b.setDisable(activo);
		}
		if (activo) {
			estado.setText("Cargando datos...");
			ProgressIndicator indicador = new ProgressIndicator();
			indicador.setMaxSize(60, 60);
			contenedorGrafico.getChildren().setAll(indicador);
		}
	}
}
