package co.edu.uniquindio.unimotor.ejb;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Modelo;
import co.edu.uniquindio.unimotor.entidades.Pregunta;
import co.edu.uniquindio.unimotor.entidades.Usuario;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;

/**
 * Plantillas de texto plano en español para los correos que envía la aplicación.
 */
public final class PlantillasCorreo {

	private static final String FORMATO_FECHA = "dd/MM/yyyy HH:mm";

	private PlantillasCorreo() {
	}

	/**
	 * Asunto del correo que se envía al vendedor cuando recibe una pregunta.
	 *
	 * @param pregunta pregunta recibida
	 * @return asunto del correo
	 */
	public static String asuntoPreguntaRecibida(Pregunta pregunta) {
		Vehiculo vehiculo = pregunta == null ? null : pregunta.getVehiculo();
		return "uniMotor: nueva pregunta sobre tu " + descripcionVehiculo(vehiculo);
	}

	/**
	 * Cuerpo del correo que se envía al vendedor cuando recibe una pregunta.
	 *
	 * @param pregunta pregunta recibida
	 * @return texto plano con los datos del cliente, del vehículo y de la pregunta
	 */
	public static String preguntaRecibida(Pregunta pregunta) {
		StringBuilder sb = new StringBuilder();
		sb.append("Hola,\n\nHas recibido una nueva pregunta en uniMotor.\n\n");
		Cliente cliente = pregunta == null ? null : pregunta.getCliente();
		sb.append("Quien pregunta:\n");
		if (cliente != null) {
			sb.append("  Nombre: ").append(valor(cliente.getNombre_completo())).append('\n');
			sb.append("  Email: ").append(valor(cliente.getEmail())).append('\n');
			Map<String, String> telefonos = cliente.getTelefono();
			if (telefonos != null) {
				for (Map.Entry<String, String> t : telefonos.entrySet()) {
					sb.append("  Teléfono (").append(valor(t.getKey())).append("): ").append(valor(t.getValue()))
							.append('\n');
				}
			}
		} else {
			sb.append("  (no disponible)\n");
		}
		Vehiculo vehiculo = pregunta == null ? null : pregunta.getVehiculo();
		sb.append("\nVehículo:\n");
		if (vehiculo != null) {
			Modelo modelo = vehiculo.getCodigo_modelo();
			sb.append("  Marca: ").append(modelo == null ? "-" : valor(modelo.getMarca())).append('\n');
			sb.append("  Modelo: ").append(modelo == null ? "-" : valor(modelo.getNombre())).append('\n');
			sb.append("  Año: ").append(vehiculo.getYear()).append('\n');
			sb.append("  Placa: ").append(valor(vehiculo.getPlaca())).append('\n');
			sb.append("  Precio: ").append(precio(vehiculo.getPrecio())).append('\n');
		} else {
			sb.append("  (no disponible)\n");
		}
		sb.append("\nPregunta:\n  ").append(pregunta == null ? "-" : valor(pregunta.getDescripcion())).append('\n');
		sb.append("\nFecha: ").append(fecha(pregunta == null ? null : pregunta.getFecha())).append('\n');
		sb.append("\nIngresa a uniMotor para responderla.\n\nEquipo uniMotor\n");
		return sb.toString();
	}

	/**
	 * Asunto del correo de recuperación de contraseña.
	 *
	 * @return asunto del correo
	 */
	public static String asuntoRecuperarPassword() {
		return "uniMotor: recuperación de contraseña";
	}

	/**
	 * Cuerpo del correo de recuperación de contraseña.
	 *
	 * @param usuario usuario que recupera su contraseña
	 * @param passwordTemporal contraseña temporal generada
	 * @return texto plano con el saludo, la contraseña temporal y la recomendación de cambiarla
	 */
	public static String recuperarPassword(Usuario usuario, String passwordTemporal) {
		String nombre = "";
		if (usuario instanceof Cliente && ((Cliente) usuario).getNombre_completo() != null) {
			nombre = ((Cliente) usuario).getNombre_completo();
		} else if (usuario != null && usuario.getEmail() != null) {
			nombre = usuario.getEmail();
		}
		return "Hola " + nombre + ",\n\n"
				+ "Recibimos una solicitud para recuperar tu contraseña de uniMotor.\n\n"
				+ "Tu contraseña temporal es: " + passwordTemporal + "\n\n"
				+ "Te recomendamos iniciar sesión y cambiarla cuanto antes. "
				+ "Si no solicitaste este cambio, comunícate con nosotros.\n\n"
				+ "Equipo uniMotor\n";
	}

	private static String descripcionVehiculo(Vehiculo vehiculo) {
		if (vehiculo == null || vehiculo.getCodigo_modelo() == null) {
			return "vehículo";
		}
		Modelo modelo = vehiculo.getCodigo_modelo();
		return valor(modelo.getMarca()) + " " + valor(modelo.getNombre()) + " " + vehiculo.getYear();
	}

	private static String valor(String texto) {
		return texto == null || texto.trim().isEmpty() ? "-" : texto;
	}

	private static String precio(Double precio) {
		if (precio == null) {
			return "-";
		}
		DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("es", "CO"));
		simbolos.setGroupingSeparator('.');
		simbolos.setDecimalSeparator(',');
		return "$ " + new DecimalFormat("#,##0", simbolos).format(precio);
	}

	private static String fecha(Date fecha) {
		return fecha == null ? "-" : new SimpleDateFormat(FORMATO_FECHA).format(fecha);
	}

}
