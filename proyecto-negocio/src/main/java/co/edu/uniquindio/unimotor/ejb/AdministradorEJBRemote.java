package co.edu.uniquindio.unimotor.ejb;

import java.util.List;

import javax.ejb.Remote;

import co.edu.uniquindio.unimotor.dto.EstadisticaDTO;
import co.edu.uniquindio.unimotor.dto.PuntoDTO;
import co.edu.uniquindio.unimotor.entidades.Administrador;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.EnvioCorreoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * Interfaz remota del EJB de administración, usada por la aplicación de escritorio.
 * Ofrece la autenticación del administrador, la recuperación de su contraseña y los datos de los gráficos estadísticos.
 */
@Remote
public interface AdministradorEJBRemote {

	/**
	 * Nombre JNDI global del EJB de administración.
	 */
	public static final String JNDI = "java:global/proyecto-ear/proyecto-negocio/AdministradorEJB!co.edu.uniquindio.unimotor.ejb.AdministradorEJBRemote";

	/**
	 * Autentica a un administrador por email y contraseña.
	 *
	 * @param email correo electrónico del administrador
	 * @param password contraseña del administrador
	 * @return el administrador autenticado
	 * @throws AutenticacionException si los datos son incorrectos o el usuario no es administrador
	 */
	Administrador autenticarAdministrador(String email, String password) throws AutenticacionException;

	/**
	 * Genera una contraseña temporal para el administrador y se la envía por correo.
	 *
	 * @param email correo electrónico del administrador
	 * @throws InformacionInvalidaException si el email es nulo o vacío
	 * @throws ElementoNoEncontradoException si no existe un administrador con ese email
	 * @throws EnvioCorreoException si no se puede enviar el correo
	 */
	void recuperarPasswordAdministrador(String email)
			throws InformacionInvalidaException, ElementoNoEncontradoException, EnvioCorreoException;

	/**
	 * Cantidad de vehículos por tipo de vehículo (gráfico de barras o pastel).
	 * Consulta nombrada Vehiculo.ESTADISTICA_POR_TIPO.
	 *
	 * @return lista de datos con etiqueta: tipo de vehículo; valor: cantidad de vehículos
	 */
	List<EstadisticaDTO> vehiculosPorTipo();

	/**
	 * Cantidad de vehículos por condición, nuevo o usado (gráfico de barras o pastel).
	 * Consulta nombrada Vehiculo.ESTADISTICA_POR_CONDICION.
	 *
	 * @return lista de datos con etiqueta: condición; valor: cantidad de vehículos
	 */
	List<EstadisticaDTO> vehiculosPorCondicion();

	/**
	 * Cantidad de vehículos por tipo de transmisión (gráfico de barras o pastel).
	 * Consulta nombrada Vehiculo.ESTADISTICA_POR_TRANSMISION.
	 *
	 * @return lista de datos con etiqueta: tipo de transmisión; valor: cantidad de vehículos
	 */
	List<EstadisticaDTO> vehiculosPorTransmision();

	/**
	 * Cantidad de vehículos por tipo de combustible (gráfico de barras o pastel).
	 * Consulta nombrada Vehiculo.ESTADISTICA_POR_COMBUSTIBLE.
	 *
	 * @return lista de datos con etiqueta: tipo de combustible; valor: cantidad de vehículos
	 */
	List<EstadisticaDTO> vehiculosPorCombustible();

	/**
	 * Cantidad de vehículos por marca (gráfico de barras o pastel).
	 * Consulta nombrada Vehiculo.ESTADISTICA_POR_MARCA.
	 *
	 * @return lista de datos con etiqueta: marca; valor: cantidad de vehículos
	 */
	List<EstadisticaDTO> vehiculosPorMarca();

	/**
	 * Precio promedio de los vehículos por marca (gráfico de barras).
	 * Consulta nombrada Vehiculo.ESTADISTICA_PRECIO_PROMEDIO_POR_MARCA.
	 *
	 * @return lista de datos con etiqueta: marca; valor: precio promedio
	 */
	List<EstadisticaDTO> precioPromedioPorMarca();

	/**
	 * Cantidad de vehículos publicados por ciudad (gráfico de barras o pastel).
	 * Consulta nombrada Vehiculo.ESTADISTICA_POR_CIUDAD.
	 *
	 * @return lista de datos con etiqueta: nombre de la ciudad; valor: cantidad de vehículos
	 */
	List<EstadisticaDTO> vehiculosPorCiudad();

	/**
	 * Cantidad de vehículos por año del modelo, ordenados por año (gráfico de barras o de líneas).
	 * Consulta nombrada Vehiculo.ESTADISTICA_POR_ANIO.
	 *
	 * @return lista de datos con etiqueta: año; valor: cantidad de vehículos
	 */
	List<EstadisticaDTO> vehiculosPorAnio();

	/**
	 * Los diez vehículos más buscados (gráfico de barras).
	 * Consulta nombrada Vehiculo.ESTADISTICA_TOP_BUSCADOS.
	 *
	 * @return lista de datos con etiqueta: marca, modelo y placa del vehículo; valor: número de búsquedas
	 */
	List<EstadisticaDTO> topVehiculosBuscados();

	/**
	 * Cantidad de clientes registrados por ciudad (gráfico de barras o pastel).
	 * Consulta nombrada Cliente.ESTADISTICA_CLIENTES_POR_CIUDAD.
	 *
	 * @return lista de datos con etiqueta: nombre de la ciudad; valor: cantidad de clientes
	 */
	List<EstadisticaDTO> clientesPorCiudad();

	/**
	 * Cantidad de preguntas recibidas por vehículo (gráfico de barras): los 15
	 * vehículos más preguntados. Consulta nombrada
	 * Pregunta.ESTADISTICA_PREGUNTAS_POR_VEHICULO.
	 *
	 * @return lista de máximo 15 datos con etiqueta: marca, modelo y placa del vehículo; valor: cantidad de
	 *         preguntas, de mayor a menor
	 */
	List<EstadisticaDTO> preguntasPorVehiculo();

	/**
	 * Características más frecuentes entre los vehículos (gráfico de barras o pastel).
	 * Consulta nombrada Caracteristica.ESTADISTICA_CARACTERISTICAS_FRECUENTES.
	 *
	 * @return lista de datos con etiqueta: nombre de la característica; valor: cantidad de vehículos que la tienen
	 */
	List<EstadisticaDTO> caracteristicasFrecuentes();

	/**
	 * Cantidad de ventas por mes, ordenadas cronológicamente (gráfico de barras o de líneas).
	 * Consulta nombrada Venta.ESTADISTICA_VENTAS_POR_MES.
	 *
	 * @return lista de datos con etiqueta: mes con formato año-mes; valor: cantidad de ventas
	 */
	List<EstadisticaDTO> ventasPorMes();

	/**
	 * Promedio de estrellas recibidas por cada vendedor (gráfico de barras): los
	 * 15 mejor calificados. Consulta nombrada
	 * Calificacion.ESTADISTICA_PROMEDIO_POR_VENDEDOR.
	 *
	 * @return lista de máximo 15 datos con etiqueta: nombre completo del vendedor; valor: promedio de
	 *         estrellas, de mayor a menor
	 */
	List<EstadisticaDTO> promedioCalificacionPorVendedor();

	/**
	 * Cantidad de vehículos por tipo de vehículo y combustible (gráfico de barras apiladas).
	 * Consulta nombrada Vehiculo.ESTADISTICA_COMBUSTIBLE_POR_TIPO.
	 *
	 * @return lista de datos con etiqueta: tipo de vehículo; serie: tipo de combustible; valor: cantidad de vehículos
	 */
	List<EstadisticaDTO> combustiblePorTipo();

	/**
	 * Relación entre kilometraje y precio de cada vehículo (gráfico de dispersión).
	 * Consulta nombrada Vehiculo.PUNTOS_PRECIO_KILOMETRAJE.
	 *
	 * @return lista de puntos con x: kilómetros; y: precio; tamaño: no se usa
	 */
	List<PuntoDTO> puntosPrecioKilometraje();

	/**
	 * Relación entre año y precio de cada vehículo, con el número de búsquedas como tamaño (gráfico de burbujas).
	 * Consulta nombrada Vehiculo.PUNTOS_PRECIO_ANIO_BUSQUEDAS.
	 *
	 * @return lista de puntos con x: año; y: precio; tamaño: número de búsquedas
	 */
	List<PuntoDTO> puntosPrecioAnioBusquedas();

}
