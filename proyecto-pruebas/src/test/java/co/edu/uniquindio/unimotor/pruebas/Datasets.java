package co.edu.uniquindio.unimotor.pruebas;

/**
 * Constantes con los nombres de los datasets JSON (src/test/resources).
 * Las anotaciones exigen constantes en tiempo de compilacion, por eso se
 * exponen como String individuales; el orden de TODOS respeta las
 * dependencias entre tablas (DBUnit inserta en ese orden).
 * Uso: poner {@code @UsingDataSet({ Datasets.MODELO, ... })} a nivel de clase (ver EntidadTest);
 * {@code @UsingDataSet} a nivel de clase aplica a todos los metodos de la clase.
 */
public final class Datasets {

	public static final String MODELO = "modelo.json";
	public static final String ADMINISTRADOR = "administrador.json";
	public static final String CARACTERISTICA = "caracteristica.json";
	public static final String CARACTERISTICA_VEHICULO = "caracteristica_vehiculo.json";
	public static final String CIUDAD = "ciudad.json";
	public static final String CLIENTE = "cliente.json";
	public static final String FAVORITO = "favorito.json";
	public static final String FOTO_VEHICULO = "foto_vehiculo.json";
	public static final String PREGUNTA = "pregunta.json";
	public static final String TELEFONO_USUARIO = "telefono_usuario.json";
	public static final String USUARIO = "usuario.json";
	public static final String VEHICULO = "vehiculo.json";
	public static final String VENTA = "venta.json";
	public static final String CALIFICACION = "calificacion.json";

	private Datasets() {
	}
}
