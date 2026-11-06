package co.edu.uniquindio.unimotor.ejb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import co.edu.uniquindio.unimotor.entidades.Administrador;
import co.edu.uniquindio.unimotor.entidades.Calificacion;
import co.edu.uniquindio.unimotor.entidades.Caracteristica;
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Condicion;
import co.edu.uniquindio.unimotor.entidades.Modelo;
import co.edu.uniquindio.unimotor.entidades.Pregunta;
import co.edu.uniquindio.unimotor.entidades.TipoCombustible;
import co.edu.uniquindio.unimotor.entidades.TipoTrasmision;
import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;
import co.edu.uniquindio.unimotor.entidades.Usuario;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.entidades.Venta;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;

/**
 * EJB de arranque que carga los datos iniciales de uniMotor cuando la base de
 * datos está vacía: administradores, clientes, modelos, características,
 * vehículos (con fotos y características), preguntas, una venta y una
 * calificación. Las ciudades no están quemadas: las carga desde la API de
 * terceros mediante {@link CiudadEJB} y los datos de ejemplo las consultan.
 */
@Singleton
@LocalBean
@Startup
public class SetupEJB {

	private static final Logger LOGGER = Logger.getLogger(SetupEJB.class.getName());

	@PersistenceContext
	private EntityManager entityManager;

	@EJB
	private CiudadEJB ciudadEJB;

	/**
	 * Constructor por defecto.
	 */
	public SetupEJB() {
		super();
	}

	/**
	 * Carga siempre primero las ciudades desde la API mediante {@link CiudadEJB}
	 * (solo si la tabla está vacía) y luego llena la base de datos con los datos
	 * de ejemplo si todavía no existe ningún usuario. El orden respeta las
	 * dependencias entre entidades.
	 */
	@PostConstruct
	public void config() {
		int cargadas = ciudadEJB.cargarCiudades();
		LOGGER.log(Level.INFO, "Ciudades cargadas: {0}", cargadas);

		if (isUsuario()) {
			return;
		}

		crearAdministradores();
		Ciudad[] ciudades = obtenerCiudades();
		Cliente[] clientes = crearClientes(ciudades);
		Modelo[] modelos = crearModelos();
		Caracteristica[] caracteristicas = crearCaracteristicas();
		Vehiculo[] vehiculos = crearVehiculos(ciudades, clientes, modelos, caracteristicas);
		crearPreguntas(vehiculos, clientes);
		crearVenta(vehiculos[2], clientes[2]);
		crearCalificacion(clientes[2], clientes[1]);
	}

	/**
	 * Crea y persiste los dos administradores del sistema.
	 */
	private void crearAdministradores() {
		entityManager.persist(new Administrador("cristia@proyecto.com", "123", "Cristian Bastidas"));
		entityManager.persist(new Administrador("profesor@proyecto.com", "4444", "Profesor"));
	}

	/**
	 * Obtiene, mediante {@link CiudadEJB#buscarCiudad(String, String)}, las
	 * ciudades que usan los datos de ejemplo. Si alguna no se encuentra (por
	 * ejemplo, porque la API cambió los nombres), la crea y la persiste como
	 * respaldo y registra una advertencia.
	 *
	 * @return arreglo con Armenia, Calarcá y Pijao (Quindío), en ese orden
	 */
	private Ciudad[] obtenerCiudades() {
		String[][] datos = { { "Armenia", "Quindío" }, { "Calarcá", "Quindío" }, { "Pijao", "Quindío" } };
		Ciudad[] ciudades = new Ciudad[datos.length];
		for (int i = 0; i < datos.length; i++) {
			try {
				ciudades[i] = ciudadEJB.buscarCiudad(datos[i][0], datos[i][1]);
			} catch (ElementoNoEncontradoException e) {
				LOGGER.log(Level.WARNING, "No se encontró la ciudad {0} ({1}); se crea como respaldo.",
						new Object[] { datos[i][0], datos[i][1] });
				ciudades[i] = new Ciudad(datos[i][0], datos[i][1]);
				entityManager.persist(ciudades[i]);
			}
		}
		return ciudades;
	}

	/**
	 * Crea y persiste tres clientes, cada uno con su propio mapa de teléfonos.
	 *
	 * @param ciudades ciudades ya persistidas
	 * @return arreglo con los tres clientes
	 */
	private Cliente[] crearClientes(Ciudad[] ciudades) {
		Map<String, String> telefono = new HashMap<String, String>();
		telefono.put("Casa", "7444445");
		telefono.put("Trabajo", "7456789");

		Map<String, String> telefono2 = new HashMap<String, String>();
		telefono2.put("Casa", "5643567");
		telefono2.put("Trabajo", "3134526849");

		Map<String, String> telefono3 = new HashMap<String, String>();
		telefono3.put("Casa", "3127456789");

		Cliente[] clientes = {
				new Cliente("usuario1@servidor.com", "12345", "Hector Rodriguez", "B/ la clarita Mz F #45", telefono,
						ciudades[0]),
				new Cliente("usuario2@servidor.com", "12345", "Rodolfo el Grande", "Cra 5 #23-45", telefono2,
						ciudades[1]),
				new Cliente("usuario3@servidor.com", "12345", "Ruben zuluaga", "Calle 2 #10-12", telefono3,
						ciudades[2]) };
		for (Cliente cliente : clientes) {
			entityManager.persist(cliente);
		}
		return clientes;
	}

	/**
	 * Crea y persiste los modelos de vehículo.
	 *
	 * @return arreglo con Audi A1, Mazda 2 y Renault 4
	 */
	private Modelo[] crearModelos() {
		Modelo[] modelos = { new Modelo("A1", "Audi"), new Modelo("Mazda 2", "Mazda"), new Modelo("4", "Renault"),
				new Modelo("NKD 125", "AKT"), new Modelo("Hilux", "Toyota") };
		for (Modelo modelo : modelos) {
			entityManager.persist(modelo);
		}
		return modelos;
	}

	/**
	 * Crea y persiste las características opcionales de los vehículos.
	 *
	 * @return arreglo con aire acondicionado, sunroof, bluetooth y cámara de
	 *         reversa
	 */
	private Caracteristica[] crearCaracteristicas() {
		Caracteristica[] caracteristicas = { new Caracteristica("Aire acondicionado"), new Caracteristica("Sunroof"),
				new Caracteristica("Bluetooth"), new Caracteristica("Camara de reversa") };
		for (Caracteristica caracteristica : caracteristicas) {
			caracteristica.setVehiculos(new ArrayList<Vehiculo>());
			entityManager.persist(caracteristica);
		}
		return caracteristicas;
	}

	/**
	 * Crea y persiste cinco vehículos de distintos dueños, con fotos y
	 * características. Vehiculo es el lado dueño de la relación con
	 * Caracteristica, pero se mantiene también el lado inverso.
	 *
	 * @param ciudades        ciudades persistidas
	 * @param clientes        clientes persistidos
	 * @param modelos         modelos persistidos
	 * @param caracteristicas características persistidas
	 * @return arreglo con los cinco vehículos
	 */
	private Vehiculo[] crearVehiculos(Ciudad[] ciudades, Cliente[] clientes, Modelo[] modelos,
			Caracteristica[] caracteristicas) {
		Vehiculo v1 = new Vehiculo("Bolido perfectamente cuidado, con menos de mil kilometros, practicamente nuevo",
				"Negro", "VEV592", 26000000d, 2018, 2000, 2, 10000, ciudades[0], clientes[0], modelos[0],
				TipoCombustible.GASOLINA, TipoVehiculo.CARRO, TipoTrasmision.MECANICO, Condicion.NUEVO);
		v1.setNumero_busquedas(120);
		v1.setFecha_publicacion(hace(30));
		completar(v1, new String[] { "audi-a1-frente.jpg", "audi-a1-interior.jpg" }, caracteristicas[0],
				caracteristicas[1], caracteristicas[2]);

		Vehiculo v2 = new Vehiculo("Mazda 2 hibrido de un solo dueno, mantenimiento al dia", "Rojo", "MZD210",
				42000000d, 2020, 1500, 4, 35000, ciudades[0], clientes[0], modelos[1], TipoCombustible.HIBRIDO,
				TipoVehiculo.CARRO, TipoTrasmision.AUTOMATICO, Condicion.USADO);
		v2.setNumero_busquedas(85);
		v2.setFecha_publicacion(hace(20));
		completar(v2, new String[] { "mazda2-lateral.jpg" }, caracteristicas[0], caracteristicas[2],
				caracteristicas[3]);

		Vehiculo v3 = new Vehiculo("Renault 4 clasico restaurado, ideal para coleccionistas", "Blanco", "REN404",
				15000000d, 1985, 850, 4, 150000, ciudades[1], clientes[1], modelos[2], TipoCombustible.GASOLINA,
				TipoVehiculo.CARRO, TipoTrasmision.MECANICO, Condicion.USADO);
		v3.setNumero_busquedas(60);
		v3.setFecha_publicacion(hace(45));
		completar(v3, new String[] { "renault4-frente.jpg", "renault4-lateral.jpg" }, caracteristicas[2]);

		Vehiculo v4 = new Vehiculo("Motocicleta ideal para la ciudad, economica y agil", "Azul", "MOT123", 6500000d,
				2019, 200, 0, 22000, ciudades[1], clientes[1], modelos[3], TipoCombustible.GASOLINA,
				TipoVehiculo.MOTOCICLETA, TipoTrasmision.MECANICO, Condicion.USADO);
		v4.setNumero_busquedas(30);
		v4.setFecha_publicacion(hace(10));
		completar(v4, new String[] { "moto-lateral.jpg" }, caracteristicas[2]);

		Vehiculo v5 = new Vehiculo("Camioneta diesel 0 km con garantia de fabrica", "Gris", "CAM777", 98000000d, 2024,
				2800, 4, 0, ciudades[2], clientes[2], modelos[4], TipoCombustible.DISEL, TipoVehiculo.CAMIONETA,
				TipoTrasmision.AUTOMATICO, Condicion.NUEVO);
		v5.setNumero_busquedas(5);
		v5.setFecha_publicacion(hace(2));
		completar(v5, new String[] { "camioneta-frente.jpg", "camioneta-interior.jpg" }, caracteristicas[0],
				caracteristicas[1], caracteristicas[3]);

		Vehiculo[] vehiculos = { v1, v2, v3, v4, v5 };
		for (Vehiculo vehiculo : vehiculos) {
			entityManager.persist(vehiculo);
		}
		return vehiculos;
	}

	/**
	 * Asigna fotos y características a un vehículo y mantiene coherente el lado
	 * inverso de la relación en cada característica.
	 *
	 * @param vehiculo        vehículo a completar
	 * @param fotos           nombres o URL de las fotos
	 * @param caracteristicas características del vehículo
	 */
	private void completar(Vehiculo vehiculo, String[] fotos, Caracteristica... caracteristicas) {
		vehiculo.setUrl_foto(new ArrayList<String>(Arrays.asList(fotos)));
		List<Caracteristica> lista = new ArrayList<Caracteristica>(Arrays.asList(caracteristicas));
		vehiculo.setCaracteristicas(lista);
		for (Caracteristica caracteristica : lista) {
			caracteristica.getVehiculos().add(vehiculo);
		}
	}

	/**
	 * Crea y persiste tres preguntas hechas por clientes sobre vehículos de otro
	 * cliente; la segunda aparece respondida.
	 *
	 * @param vehiculos vehículos persistidos
	 * @param clientes  clientes persistidos
	 */
	private void crearPreguntas(Vehiculo[] vehiculos, Cliente[] clientes) {
		Pregunta p1 = new Pregunta(vehiculos[0], clientes[1], hace(5), "¿El precio es negociable?");
		entityManager.persist(p1);

		Pregunta p2 = new Pregunta(vehiculos[1], clientes[2], hace(4), "¿Tiene los papeles al dia?");
		p2.setRespuesta("Si, todos los papeles y el SOAT estan al dia.");
		p2.setFecha_respuesta(hace(3));
		entityManager.persist(p2);

		Pregunta p3 = new Pregunta(vehiculos[3], clientes[0], hace(1), "¿Acepta cambio por otra motocicleta?");
		entityManager.persist(p3);
	}

	/**
	 * Crea y persiste una venta y deja el vehículo vendido como inactivo. Se
	 * actualizan ambos lados de la relación Venta y Vehiculo.
	 *
	 * @param vehiculo  vehículo vendido
	 * @param comprador cliente que lo compra (distinto del dueño)
	 */
	private void crearVenta(Vehiculo vehiculo, Cliente comprador) {
		Venta venta = new Venta(hace(2), vehiculo, comprador);
		entityManager.persist(venta);
		vehiculo.setCodigo_venta(venta);
		vehiculo.setActivo(false);
	}

	/**
	 * Crea y persiste la calificación que un comprador hace al vendedor.
	 *
	 * @param calificador cliente que califica
	 * @param vendedor    cliente calificado, distinto del calificador
	 */
	private void crearCalificacion(Cliente calificador, Cliente vendedor) {
		entityManager.persist(new Calificacion(calificador, vendedor, 5,
				"Excelente vendedor, el vehiculo estaba tal cual lo describio.", hace(1)));
	}

	/**
	 * Calcula una fecha anterior a la actual.
	 *
	 * @param dias número de días hacia atrás
	 * @return fecha resultante
	 */
	private Date hace(int dias) {
		Calendar calendario = Calendar.getInstance();
		calendario.add(Calendar.DAY_OF_YEAR, -dias);
		return calendario.getTime();
	}

	/**
	 * Indica si ya existe algún usuario en la base de datos.
	 *
	 * @return {@code true} si hay al menos un usuario registrado
	 */
	public boolean isUsuario() {
		TypedQuery<Usuario> query = entityManager.createNamedQuery(Usuario.GETALL, Usuario.class);
		return !query.getResultList().isEmpty();
	}

}
