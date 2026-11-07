package co.edu.uniquindio.unimotor.escritorio.app;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

/**
 * Aplicación JavaFX del administrador de uniMotor. Gestiona la ventana principal
 * y la navegación entre la pantalla de inicio de sesión y la de estadísticas.
 */
public class Aplicacion extends Application {

	/** Título de la ventana. */
	public static final String TITULO = "uniMotor — Administrador";

	/** Ventana principal de la aplicación. */
	private static Stage ventana;

	/**
	 * Guarda la ventana principal y muestra la pantalla de inicio de sesión.
	 *
	 * @param primaryStage ventana principal entregada por JavaFX
	 */
	@Override
	public void start(Stage primaryStage) {
		ventana = primaryStage;
		ventana.setTitle(TITULO);
		mostrarInicio();
	}

	/**
	 * Muestra la pantalla de inicio de sesión (tamaño fijo).
	 */
	public static void mostrarInicio() {
		mostrar("/inicio.fxml", 420, 480, false);
	}

	/**
	 * Muestra la pantalla principal con las estadísticas (redimensionable).
	 */
	public static void mostrarPrincipal() {
		mostrar("/principal.fxml", 1200, 800, true);
	}

	/**
	 * Obtiene la ventana principal.
	 *
	 * @return el Stage principal de la aplicación
	 */
	public static Stage getVentana() {
		return ventana;
	}

	/**
	 * Carga un FXML en una escena nueva y la muestra en la ventana principal.
	 *
	 * @param recurso ruta del archivo FXML
	 * @param ancho ancho inicial de la escena
	 * @param alto alto inicial de la escena
	 * @param redimensionable si la ventana puede cambiar de tamaño
	 */
	private static void mostrar(String recurso, double ancho, double alto, boolean redimensionable) {
		try {
			Parent raiz = FXMLLoader.load(Aplicacion.class.getResource(recurso));
			Scene escena = new Scene(raiz, ancho, alto);
			escena.getStylesheets().add(Aplicacion.class.getResource("/estilos.css").toExternalForm());
			ventana.hide();
			ventana.setScene(escena);
			ventana.setResizable(redimensionable);
			if (redimensionable) {
				ventana.setMinWidth(1000);
				ventana.setMinHeight(700);
				ventana.setWidth(ancho);
				ventana.setHeight(alto);
			} else {
				ventana.setMinWidth(0);
				ventana.setMinHeight(0);
				ventana.sizeToScene();
			}
			ventana.centerOnScreen();
			ventana.show();
		} catch (IOException | RuntimeException e) {
			Alert alerta = new Alert(AlertType.ERROR);
			alerta.setTitle(TITULO);
			alerta.setHeaderText("No se pudo cargar la pantalla");
			alerta.setContentText(String.valueOf(e.getMessage()));
			alerta.showAndWait();
		}
	}

	/**
	 * Punto de entrada de la aplicación.
	 *
	 * @param args argumentos de la línea de comandos
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
