package co.edu.uniquindio.proyecto.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Aplicacion extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub

		FXMLLoader loader = new FXMLLoader(Aplicacion.class.getResource("/inicio.fxml"));
		Parent parent = loader.load();

		Scene s = new Scene(parent);

		primaryStage.setScene(s);
		primaryStage.setTitle("Ventana Administrador");
		primaryStage.show();

	}

	public static void main(String[] args) {

		launch(args);

	}

}
