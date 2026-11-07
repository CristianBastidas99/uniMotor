package co.edu.uniquindio.unimotor.escritorio.controlador;

import java.util.Optional;

import co.edu.uniquindio.unimotor.entidades.Administrador;
import co.edu.uniquindio.unimotor.escritorio.app.Aplicacion;
import co.edu.uniquindio.unimotor.escritorio.modelo.AdministradorDelegado;
import co.edu.uniquindio.unimotor.escritorio.modelo.ErrorConexionException;
import co.edu.uniquindio.unimotor.escritorio.modelo.Sesion;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.EnvioCorreoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

/**
 * Controlador de la ventana de inicio de sesión del administrador. Valida los
 * datos ingresados, autentica contra el servidor en un hilo aparte y permite
 * recuperar la contraseña por correo.
 */
public class InicioController {

	/** Expresión regular básica para validar el formato de un email. */
	private static final String PATRON_EMAIL = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

	/** Caja de texto del email. */
	@FXML
	private TextField cajonEmail;

	/** Caja de texto de la contraseña. */
	@FXML
	private PasswordField cajonPassword;

	/** Botón que inicia la sesión. */
	@FXML
	private Button botonIngresar;

	/** Etiqueta donde se muestran los mensajes de error de validación. */
	@FXML
	private Label mensaje;

	/** Enlace para recuperar la contraseña. */
	@FXML
	private Hyperlink enlaceRecuperar;

	/**
	 * Inicializa la ventana: deja el mensaje vacío y pone el foco en el email.
	 */
	@FXML
	public void initialize() {
		mensaje.setText("");
		Platform.runLater(() -> cajonEmail.requestFocus());
	}

	/**
	 * Valida los campos y autentica al administrador. Si es correcto abre la
	 * ventana principal.
	 *
	 * @param evento evento de acción del botón
	 */
	@FXML
	void ingresar(ActionEvent evento) {
		final String email = cajonEmail.getText() == null ? "" : cajonEmail.getText().trim();
		final String password = cajonPassword.getText() == null ? "" : cajonPassword.getText();

		if (email.isEmpty() || password.isEmpty()) {
			mensaje.setText("Ingrese el email y la contraseña.");
			return;
		}
		if (!email.matches(PATRON_EMAIL)) {
			mensaje.setText("Ingrese un email válido.");
			return;
		}

		mensaje.setText("Conectando...");
		botonIngresar.setDisable(true);

		Task<Administrador> tarea = new Task<Administrador>() {
			@Override
			protected Administrador call() throws Exception {
				return AdministradorDelegado.getInstancia().autenticarAdministrador(email, password);
			}
		};
		tarea.setOnSucceeded(e -> {
			Sesion.iniciar(tarea.getValue());
			Aplicacion.mostrarPrincipal();
		});
		tarea.setOnFailed(e -> {
			botonIngresar.setDisable(false);
			Throwable error = tarea.getException();
			if (error instanceof AutenticacionException) {
				mensaje.setText("El email o la contraseña son incorrectos.");
				cajonPassword.clear();
				cajonPassword.requestFocus();
			} else if (error instanceof ErrorConexionException) {
				mensaje.setText("");
				mostrarAlerta(AlertType.ERROR, error.getMessage());
			} else {
				mensaje.setText("");
				mostrarAlerta(AlertType.ERROR, "Ocurrió un error inesperado al iniciar sesión.");
			}
		});
		lanzar(tarea);
	}

	/**
	 * Pide el email del administrador y solicita al servidor el envío de una
	 * contraseña temporal.
	 *
	 * @param evento evento de acción del enlace
	 */
	@FXML
	void recuperarPassword(ActionEvent evento) {
		TextInputDialog dialogo = new TextInputDialog(cajonEmail.getText() == null ? "" : cajonEmail.getText().trim());
		dialogo.setTitle("Recuperar contraseña");
		dialogo.setHeaderText(null);
		dialogo.setContentText("Ingrese el email del administrador:");
		Optional<String> resultado = dialogo.showAndWait();
		if (!resultado.isPresent()) {
			return;
		}
		final String email = resultado.get().trim();

		enlaceRecuperar.setDisable(true);
		Task<Void> tarea = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				AdministradorDelegado.getInstancia().recuperarPasswordAdministrador(email);
				return null;
			}
		};
		tarea.setOnSucceeded(e -> {
			enlaceRecuperar.setDisable(false);
			mostrarAlerta(AlertType.INFORMATION, "Se envió una contraseña temporal a " + email + ". Revise su correo.");
		});
		tarea.setOnFailed(e -> {
			enlaceRecuperar.setDisable(false);
			Throwable error = tarea.getException();
			if (error instanceof ElementoNoEncontradoException || error instanceof InformacionInvalidaException) {
				mostrarAlerta(AlertType.WARNING, error.getMessage());
			} else if (error instanceof EnvioCorreoException) {
				mostrarAlerta(AlertType.ERROR, "No se pudo enviar el correo. Inténtelo de nuevo más tarde.");
			} else if (error instanceof ErrorConexionException) {
				mostrarAlerta(AlertType.ERROR, error.getMessage());
			} else {
				mostrarAlerta(AlertType.ERROR, "Ocurrió un error inesperado al recuperar la contraseña.");
			}
		});
		lanzar(tarea);
	}

	/**
	 * Muestra una alerta con el mensaje indicado.
	 *
	 * @param tipo    tipo de alerta
	 * @param mensaje texto a mostrar
	 */
	private void mostrarAlerta(AlertType tipo, String mensaje) {
		Alert alerta = new Alert(tipo);
		alerta.setTitle("uniMotor");
		alerta.setHeaderText(null);
		alerta.setContentText(mensaje);
		alerta.showAndWait();
	}

	/**
	 * Ejecuta una tarea en un hilo daemon.
	 *
	 * @param tarea tarea a ejecutar
	 */
	private void lanzar(Task<?> tarea) {
		Thread hilo = new Thread(tarea);
		hilo.setDaemon(true);
		hilo.start();
	}
}
