package co.edu.uniquindio.proyecto.controlador;

import co.edu.uniquindio.empresa.Usuario;
import co.edu.uniquindio.proyecto.modelo.PruebaDelegado;
import co.edu.uniquinio.unimotor.excepciones.AutentificacionEcxeption;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class InicioController {

	@FXML
	private TextField cajonEmail;
	@FXML
	private TextField cajonPassword;

	@FXML
	public void autenticarUsuario(ActionEvent e) {

		PruebaDelegado delegado = PruebaDelegado.pruebaDelegado;

		if (!cajonPassword.getText().isEmpty() && !cajonEmail.getText().isEmpty()) {
			try {
				Usuario u = delegado.autentificarUsuario(cajonEmail.getText(), cajonPassword.getText());
			} catch (AutentificacionEcxeption e1) {
				// TODO Auto-generated catch block
				Alert a = new Alert(AlertType.ERROR);
				a.setHeaderText("Titulo Alerta");
				a.setContentText(e1.getMessage());
				a.show();
			}
		}

	}

}
