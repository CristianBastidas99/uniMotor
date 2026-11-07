package co.edu.uniquindio.unimotor.ejb.demo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import co.edu.uniquindio.unimotor.ejb.CiudadEJB;
import co.edu.uniquindio.unimotor.ejb.demo.CatalogoDemo.CaracteristicaDemo;
import co.edu.uniquindio.unimotor.ejb.demo.CatalogoDemo.CiudadDemo;
import co.edu.uniquindio.unimotor.ejb.demo.CatalogoDemo.ColorDemo;
import co.edu.uniquindio.unimotor.ejb.demo.CatalogoDemo.ComentarioDemo;
import co.edu.uniquindio.unimotor.ejb.demo.CatalogoDemo.ModeloDemo;
import co.edu.uniquindio.unimotor.ejb.demo.CatalogoDemo.PreguntaDemo;
import co.edu.uniquindio.unimotor.entidades.Calificacion;
import co.edu.uniquindio.unimotor.entidades.Caracteristica;
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Condicion;
import co.edu.uniquindio.unimotor.entidades.Modelo;
import co.edu.uniquindio.unimotor.entidades.Pregunta;
import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.entidades.Venta;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;

/**
 * Generador de datos de demostración masivos de uniMotor. A partir de los
 * catálogos de {@link CatalogoDemo} y de un {@link Random} con semilla fija
 * crea clientes, modelos, características, vehículos, preguntas, ventas,
 * calificaciones y favoritos. Solo elige elementos de las listas y números en
 * rangos válidos, de modo que los datos son coherentes y reproducibles. Todo
 * corre dentro de la transacción de quien lo invoca (el arranque de
 * {@code SetupEJB}) y respeta a mano las reglas de negocio (no preguntar por un
 * vehículo propio, calificar solo a quien vendió, un par de calificación una
 * sola vez, etc.).
 */
public class GeneradorDatosDemo {

	private static final Logger LOGGER = Logger.getLogger(GeneradorDatosDemo.class.getName());

	/** Semilla fija del generador aleatorio, para obtener siempre los mismos datos. */
	public static final long SEMILLA = 2026L;

	/** Número de clientes que se generan. */
	public static final int CLIENTES = 60;

	/** Número de vehículos que se generan. */
	public static final int VEHICULOS = 400;

	/** Número de preguntas que se generan. */
	public static final int PREGUNTAS = 800;

	/** Número de favoritos que se generan. */
	public static final int FAVORITOS = 150;

	/** Número de ventas que se generan. */
	public static final int VENTAS = 100;

	/** Número máximo de calificaciones que se generan. */
	public static final int CALIFICACIONES = 60;

	/** Número de fotos distintas de cada vehículo. */
	public static final int FOTOS_POR_VEHICULO = 5;

	/** Número de meses hacia atrás en los que se reparten las fechas. */
	public static final int MESES = 24;

	private static final long DIA_MS = 24L * 60L * 60L * 1000L;

	private static final int LOTE = 100;

	private static final String[] CLAVES_TELEFONO = { "Celular", "Casa", "Trabajo" };

	private static final TipoVehiculo[] TIPOS_USADOS = { TipoVehiculo.CARRO, TipoVehiculo.MOTOCICLETA,
			TipoVehiculo.CAMIONETA };

	private final EntityManager em;

	private final CiudadEJB ciudadEJB;

	private final RepartidorFotos fotos;

	private final Random random;

	private final Date hoy;

	private final int anioActual;

	private final Map<String, Integer> conteo = new LinkedHashMap<String, Integer>();

	private int pendientes = 0;

	private List<Ciudad> ciudades = new ArrayList<Ciudad>();

	private int[] pesosCiudades = new int[0];

	/**
	 * Crea el generador.
	 *
	 * @param em        administrador de entidades de la transacción en curso
	 * @param ciudadEJB EJB para buscar las ciudades ya cargadas
	 * @param fotos     repartidor de fotos
	 * @param random    generador aleatorio con semilla fija
	 */
	public GeneradorDatosDemo(EntityManager em, CiudadEJB ciudadEJB, RepartidorFotos fotos, Random random) {
		this.em = em;
		this.ciudadEJB = ciudadEJB;
		this.fotos = fotos;
		this.random = random;
		this.hoy = new Date();
		Calendar calendario = Calendar.getInstance();
		calendario.setTime(hoy);
		this.anioActual = calendario.get(Calendar.YEAR);
	}

	/**
	 * Genera todos los datos de demostración y registra en el log un resumen.
	 *
	 * @param clientesExistentes        clientes ya creados (se usan como dueños,
	 *                                  compradores y preguntadores)
	 * @param vehiculosExistentes       vehículos ya creados (solo para no repetir
	 *                                  placas)
	 * @param modelosExistentes         modelos ya creados (no se duplican)
	 * @param caracteristicasExistentes características ya creadas (no se
	 *                                  duplican)
	 * @param calificacionesExistentes  calificaciones ya creadas (no se repite su
	 *                                  par)
	 * @param ciudadesRespaldo          ciudades a usar solo si ninguna del catálogo
	 *                                  se encuentra en la base
	 */
	public void generar(List<Cliente> clientesExistentes, List<Vehiculo> vehiculosExistentes,
			List<Modelo> modelosExistentes, List<Caracteristica> caracteristicasExistentes,
			List<Calificacion> calificacionesExistentes, List<Ciudad> ciudadesRespaldo) {
		long inicio = System.currentTimeMillis();

		resolverCiudades(ciudadesRespaldo);
		List<Cliente> clientes = new ArrayList<Cliente>(clientesExistentes);
		crearClientes(clientes);
		Modelo[] modelos = crearModelos(modelosExistentes);
		Caracteristica[] caracteristicas = crearCaracteristicas(caracteristicasExistentes);
		List<Vehiculo> vehiculos = crearVehiculos(clientes, vehiculosExistentes, modelos, caracteristicas);
		Map<Vehiculo, Date> fechasVenta = new IdentityHashMap<Vehiculo, Date>();
		List<Venta> ventas = crearVentas(vehiculos, clientes, fechasVenta);
		crearPreguntas(vehiculos, clientes, fechasVenta);
		crearCalificaciones(ventas, clientes, calificacionesExistentes);
		crearFavoritos(vehiculos, clientes);
		em.flush();

		int min = Integer.MAX_VALUE;
		int max = 0;
		for (Integer uso : fotos.getUsos().values()) {
			min = Math.min(min, uso);
			max = Math.max(max, uso);
		}
		if (min == Integer.MAX_VALUE) {
			min = 0;
		}
		LOGGER.log(Level.INFO, "Datos de demostración creados en {0} ms: {1}",
				new Object[] { System.currentTimeMillis() - inicio, conteo });
		LOGGER.log(Level.INFO, "Uso de fotos de demostración: mínimo {0}, máximo {1}", new Object[] { min, max });
	}

	// ------------------------------------------------------------------ ciudades

	/**
	 * Busca en la base las ciudades del catálogo y guarda las encontradas con su
	 * peso. Las que no existen se omiten con una advertencia.
	 *
	 * @param respaldo ciudades a usar si no se encuentra ninguna
	 */
	private void resolverCiudades(List<Ciudad> respaldo) {
		List<Integer> pesos = new ArrayList<Integer>();
		for (CiudadDemo demo : CatalogoDemo.CIUDADES) {
			try {
				ciudades.add(ciudadEJB.buscarCiudad(demo.nombre, demo.departamento));
				pesos.add(demo.peso);
			} catch (ElementoNoEncontradoException e) {
				LOGGER.log(Level.WARNING, "Ciudad de demostración no encontrada: {0} ({1}); se omite.",
						new Object[] { demo.nombre, demo.departamento });
			}
		}
		if (ciudades.isEmpty()) {
			for (Ciudad ciudad : respaldo) {
				ciudades.add(ciudad);
				pesos.add(1);
			}
		}
		pesosCiudades = new int[pesos.size()];
		for (int i = 0; i < pesosCiudades.length; i++) {
			pesosCiudades[i] = pesos.get(i);
		}
	}

	/**
	 * Elige una ciudad según su peso.
	 *
	 * @return ciudad elegida
	 */
	private Ciudad ciudadAleatoria() {
		return ciudades.get(elegirPonderado(pesosCiudades));
	}

	// ------------------------------------------------------------------ clientes

	/**
	 * Crea y persiste los clientes de demostración y los agrega a la lista.
	 *
	 * @param clientes lista de clientes, que se amplía con los nuevos
	 */
	private void crearClientes(List<Cliente> clientes) {
		Set<String> emails = new HashSet<String>();
		for (Object email : em.createQuery("SELECT u.email FROM Usuario u").getResultList()) {
			emails.add(String.valueOf(email).toLowerCase(Locale.ROOT));
		}
		for (Cliente existente : clientes) {
			emails.add(existente.getEmail().toLowerCase(Locale.ROOT));
		}
		for (int i = 0; i < CLIENTES; i++) {
			boolean hombre = random.nextBoolean();
			String nombre = elegir(hombre ? CatalogoDemo.NOMBRES_HOMBRE : CatalogoDemo.NOMBRES_MUJER);
			String apellido1 = elegir(CatalogoDemo.APELLIDOS);
			String apellido2 = elegir(CatalogoDemo.APELLIDOS);
			while (apellido2.equals(apellido1)) {
				apellido2 = elegir(CatalogoDemo.APELLIDOS);
			}
			String completo = recortar(nombre + " " + apellido1 + " " + apellido2, 100);
			String email = generarEmail(nombre, apellido1, apellido2, emails);
			Cliente cliente = new Cliente(email, "12345", completo, generarDireccion(), generarTelefonos(),
					ciudadAleatoria());
			cliente.setVehiculos_favoritos(new ArrayList<Vehiculo>());
			persistir(cliente, "Cliente");
			clientes.add(cliente);
		}
	}

	/**
	 * Arma un email único a partir del nombre, sin tildes ni eñes y en
	 * minúsculas, con un formato elegido al azar.
	 *
	 * @param nombre    nombre de pila
	 * @param apellido1 primer apellido
	 * @param apellido2 segundo apellido
	 * @param usados    emails ya usados (se actualiza con el nuevo)
	 * @return email único de máximo 100 caracteres
	 */
	private String generarEmail(String nombre, String apellido1, String apellido2, Set<String> usados) {
		String n = soloLetras(primeraPalabra(nombre));
		String a1 = soloLetras(primeraPalabra(apellido1));
		String a2 = soloLetras(primeraPalabra(apellido2));
		String dominio = elegir(CatalogoDemo.DOMINIOS_EMAIL);
		String local;
		switch (random.nextInt(5)) {
		case 0:
			local = n + "." + a1;
			break;
		case 1:
			local = n + a1;
			break;
		case 2:
			local = n.substring(0, 1) + "." + a1 + (10 + random.nextInt(90));
			break;
		case 3:
			local = n + "." + a1 + "." + a2.substring(0, 1);
			break;
		default:
			local = a1 + "." + n + (random.nextInt(90) + 10);
			break;
		}
		String email = local + "@" + dominio;
		int sufijo = 2;
		while (usados.contains(email) || email.length() > 100) {
			email = local + sufijo + "@" + dominio;
			sufijo++;
		}
		usados.add(email);
		return email;
	}

	/**
	 * Genera entre uno y tres teléfonos: un celular y, opcionalmente, fijos de
	 * casa y trabajo.
	 *
	 * @return mapa tipo de teléfono &rarr; número de diez dígitos
	 */
	private Map<String, String> generarTelefonos() {
		int cantidad = 1 + random.nextInt(3);
		Map<String, String> telefonos = new LinkedHashMap<String, String>();
		for (int i = 0; i < cantidad; i++) {
			String numero;
			if (i == 0) {
				numero = elegir(CatalogoDemo.PREFIJOS_CELULAR) + digitos(7);
			} else {
				numero = "60" + (1 + random.nextInt(8)) + digitos(7);
			}
			telefonos.put(CLAVES_TELEFONO[i], numero);
		}
		return telefonos;
	}

	/**
	 * Genera una dirección con formato colombiano, por ejemplo
	 * {@code Calle 21 # 14-35}.
	 *
	 * @return dirección de máximo 200 caracteres
	 */
	private String generarDireccion() {
		String direccion = elegir(CatalogoDemo.TIPOS_VIA) + " " + (1 + random.nextInt(170)) + " # "
				+ (1 + random.nextInt(99)) + "-" + (1 + random.nextInt(99));
		return recortar(direccion, 200);
	}

	// ------------------------------------------------------------------ catálogos

	/**
	 * Persiste los modelos del catálogo que todavía no existen.
	 *
	 * @param existentes modelos ya creados
	 * @return arreglo de modelos alineado con {@link CatalogoDemo#MODELOS}
	 */
	private Modelo[] crearModelos(List<Modelo> existentes) {
		Map<String, Modelo> porClave = new HashMap<String, Modelo>();
		for (Modelo modelo : existentes) {
			porClave.put(clave(modelo.getMarca()) + "|" + clave(modelo.getNombre()), modelo);
		}
		Modelo[] resultado = new Modelo[CatalogoDemo.MODELOS.length];
		for (int i = 0; i < resultado.length; i++) {
			ModeloDemo demo = CatalogoDemo.MODELOS[i];
			String clave = clave(demo.marca) + "|" + clave(demo.nombre);
			Modelo modelo = porClave.get(clave);
			if (modelo == null) {
				modelo = new Modelo(demo.nombre, demo.marca);
				modelo.setVehiculos(new ArrayList<Vehiculo>());
				persistir(modelo, "Modelo");
				porClave.put(clave, modelo);
			}
			resultado[i] = modelo;
		}
		return resultado;
	}

	/**
	 * Persiste las características del catálogo que todavía no existen (se
	 * comparan sin tildes ni mayúsculas).
	 *
	 * @param existentes características ya creadas
	 * @return arreglo de características alineado con
	 *         {@link CatalogoDemo#CARACTERISTICAS}
	 */
	private Caracteristica[] crearCaracteristicas(List<Caracteristica> existentes) {
		Map<String, Caracteristica> porClave = new HashMap<String, Caracteristica>();
		for (Caracteristica caracteristica : existentes) {
			porClave.put(clave(caracteristica.getNombre()), caracteristica);
		}
		Caracteristica[] resultado = new Caracteristica[CatalogoDemo.CARACTERISTICAS.length];
		for (int i = 0; i < resultado.length; i++) {
			CaracteristicaDemo demo = CatalogoDemo.CARACTERISTICAS[i];
			String clave = clave(demo.nombre);
			Caracteristica caracteristica = porClave.get(clave);
			if (caracteristica == null) {
				caracteristica = new Caracteristica(demo.nombre);
				caracteristica.setVehiculos(new ArrayList<Vehiculo>());
				persistir(caracteristica, "Caracteristica");
				porClave.put(clave, caracteristica);
			}
			if (caracteristica.getVehiculos() == null) {
				caracteristica.setVehiculos(new ArrayList<Vehiculo>());
			}
			resultado[i] = caracteristica;
		}
		return resultado;
	}

	// ------------------------------------------------------------------ vehículos

	/**
	 * Crea los vehículos de demostración (todavía sin persistir: se persisten al
	 * crear las ventas, cuando ya se conocen sus fechas de publicación).
	 *
	 * @param clientes        todos los clientes
	 * @param existentes      vehículos ya creados (para no repetir placas)
	 * @param modelos         modelos alineados con el catálogo
	 * @param caracteristicas características alineadas con el catálogo
	 * @return los vehículos generados
	 */
	private List<Vehiculo> crearVehiculos(List<Cliente> clientes, List<Vehiculo> existentes, Modelo[] modelos,
			Caracteristica[] caracteristicas) {
		Set<String> placas = new HashSet<String>();
		for (Vehiculo existente : existentes) {
			placas.add(existente.getPlaca());
		}
		for (Object placa : em.createQuery("SELECT v.placa FROM Vehiculo v").getResultList()) {
			placas.add(String.valueOf(placa));
		}

		int[] pesosDueno = new int[clientes.size()];
		for (int i = 0; i < pesosDueno.length; i++) {
			double r = random.nextDouble();
			pesosDueno[i] = r < 0.2 ? 0 : (int) (1 + 30 * r * r * r);
		}
		int[] pesosModelo = new int[CatalogoDemo.MODELOS.length];
		for (int i = 0; i < pesosModelo.length; i++) {
			pesosModelo[i] = CatalogoDemo.MODELOS[i].peso;
		}
		int[] pesosColor = new int[CatalogoDemo.COLORES.length];
		for (int i = 0; i < pesosColor.length; i++) {
			pesosColor[i] = CatalogoDemo.COLORES[i].peso;
		}

		DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
		simbolos.setGroupingSeparator('.');
		DecimalFormat formato = new DecimalFormat("#,##0", simbolos);

		List<Vehiculo> vehiculos = new ArrayList<Vehiculo>();
		for (int i = 0; i < VEHICULOS; i++) {
			Cliente dueno = clientes.get(elegirPonderado(pesosDueno));
			int posicion = elegirPonderado(pesosModelo);
			ModeloDemo demo = CatalogoDemo.MODELOS[posicion];
			Modelo modelo = modelos[posicion];
			TipoVehiculo tipo = demo.tipo;
			boolean moto = tipo == TipoVehiculo.MOTOCICLETA;

			Condicion condicion = (random.nextDouble() < 0.25 || demo.anioDesde >= anioActual) ? Condicion.NUEVO
					: Condicion.USADO;
			int anio;
			int km;
			double factor;
			if (condicion == Condicion.NUEVO) {
				anio = random.nextDouble() < 0.1 ? anioActual + 1 : anioActual;
				km = 0;
				factor = anio > anioActual ? 1.03 : 1.0;
			} else {
				int desde = Math.max(demo.anioDesde, 2005);
				int hasta = anioActual - 1;
				if (desde > hasta) {
					desde = hasta;
				}
				anio = desde + random.nextInt(hasta - desde + 1);
				int edad = anioActual - anio;
				double porAnio = 8000 + random.nextDouble() * 10000;
				if (moto) {
					porAnio *= 0.7;
				}
				double ruido = 0.85 + random.nextDouble() * 0.3;
				km = (int) Math.max(3000, edad * porAnio * ruido);
				km = (km / 100) * 100;
				factor = Math.pow(0.87, edad);
				double esperado = edad * 13000.0 * (moto ? 0.7 : 1.0);
				double exceso = Math.max(0, km - esperado) / 100000.0;
				factor *= 1 - Math.min(0.3, exceso * 0.3);
				factor = Math.max(0.15, factor);
			}
			double ruidoPrecio = 0.95 + random.nextDouble() * 0.1;
			double paso = moto ? 50000 : 100000;
			double precio = Math.max(paso, Math.round(demo.precioNuevo * factor * ruidoPrecio / paso) * paso);

			int cilindraje = 0;
			if (demo.cilindrajeMax > 0) {
				if (demo.cilindrajeMin >= demo.cilindrajeMax) {
					cilindraje = demo.cilindrajeMin;
				} else {
					int bruto = demo.cilindrajeMin
							+ (int) (random.nextDouble() * (demo.cilindrajeMax - demo.cilindrajeMin));
					int redondeo = moto ? 5 : 100;
					cilindraje = Math.round((float) bruto / redondeo) * redondeo;
					cilindraje = Math.max(demo.cilindrajeMin, Math.min(demo.cilindrajeMax, cilindraje));
				}
			}

			ColorDemo color = CatalogoDemo.COLORES[elegirPonderado(pesosColor)];
			Ciudad ciudad = (random.nextDouble() < 0.8 && dueno.getCodigo_ciudad() != null)
					? dueno.getCodigo_ciudad()
					: ciudadAleatoria();

			String[] plantillas = condicion == Condicion.NUEVO ? CatalogoDemo.DESCRIPCIONES_NUEVO
					: CatalogoDemo.DESCRIPCIONES;
			String descripcion = elegir(plantillas).replace("{marca}", demo.marca).replace("{modelo}", demo.nombre)
					.replace("{anio}", String.valueOf(anio)).replace("{km}", formato.format(km))
					.replace("{color}", color.nombre.toLowerCase(Locale.ROOT)).replace("{ciudad}", ciudad.getNombre());
			descripcion = recortar(descripcion, 1000);

			String placa = generarPlaca(moto, placas);
			Vehiculo vehiculo = new Vehiculo(descripcion, color.nombre, placa, precio, anio, cilindraje, demo.puertas,
					km, ciudad, dueno, modelo, elegir(demo.combustibles), tipo, elegir(demo.transmisiones),
					condicion);

			List<Caracteristica> propias = new ArrayList<Caracteristica>();
			for (int c = 0; c < caracteristicas.length; c++) {
				CaracteristicaDemo cd = CatalogoDemo.CARACTERISTICAS[c];
				if (aplica(cd.aplicaA, tipo) && random.nextDouble() < cd.probabilidad) {
					propias.add(caracteristicas[c]);
				}
			}
			vehiculo.setCaracteristicas(propias);
			for (Caracteristica propia : propias) {
				propia.getVehiculos().add(vehiculo);
			}
			vehiculo.setClientes(new ArrayList<Cliente>());
			vehiculo.setUrl_foto(new ArrayList<String>(fotos.siguientes(tipo, FOTOS_POR_VEHICULO)));
			vehiculo.setFecha_publicacion(hace((int) (random.nextDouble() * MESES * 30.4)));
			double r = random.nextDouble();
			vehiculo.setNumero_busquedas((int) (3 + 1500 * r * r * r * r));
			vehiculo.setActivo(true);
			vehiculos.add(vehiculo);
		}
		return vehiculos;
	}

	/**
	 * Genera una placa única: tres letras y tres dígitos para carros y
	 * camionetas, o tres letras, dos dígitos y una letra para motos.
	 *
	 * @param moto   {@code true} si es motocicleta
	 * @param usadas placas ya usadas (se actualiza con la nueva)
	 * @return placa nueva de seis caracteres
	 */
	private String generarPlaca(boolean moto, Set<String> usadas) {
		String placa;
		do {
			placa = letras(3) + digitos(moto ? 2 : 3) + (moto ? letras(1) : "");
		} while (usadas.contains(placa));
		usadas.add(placa);
		return placa;
	}

	// ------------------------------------------------------------------ ventas

	/**
	 * Crea las ventas. Elige primero el mes de la venta y luego un vehículo
	 * publicado antes; si no lo encuentra, adelanta la fecha de publicación del
	 * vehículo elegido para que siempre sea al menos tres días anterior a la
	 * venta. Persiste los vehículos y luego las ventas.
	 *
	 * @param vehiculos   vehículos generados (todavía sin persistir)
	 * @param clientes    todos los clientes
	 * @param fechasVenta mapa que se llena con la fecha de venta de cada vehículo
	 *                    vendido
	 * @return las ventas creadas
	 */
	private List<Venta> crearVentas(List<Vehiculo> vehiculos, List<Cliente> clientes,
			Map<Vehiculo, Date> fechasVenta) {
		List<Vehiculo> libres = new ArrayList<Vehiculo>(vehiculos);
		List<Vehiculo> vendidos = new ArrayList<Vehiculo>();
		List<Date> fechas = new ArrayList<Date>();
		long limiteAntiguo = hoy.getTime() - 730L * DIA_MS;
		int cantidad = Math.min(VENTAS, libres.size());
		for (int k = 0; k < cantidad; k++) {
			int mes = k % MESES;
			int dias = mes * 30 + random.nextInt(30);
			Date fecha = new Date(hoy.getTime() - dias * DIA_MS - (long) (random.nextDouble() * DIA_MS));
			if (fecha.after(hoy)) {
				fecha = hoy;
			}
			long tope = fecha.getTime() - 3L * DIA_MS;
			Vehiculo elegido = null;
			for (int intento = 0; intento < 30 && elegido == null; intento++) {
				Vehiculo candidato = libres.get(random.nextInt(libres.size()));
				if (candidato.getFecha_publicacion().getTime() <= tope) {
					elegido = candidato;
				}
			}
			if (elegido == null) {
				elegido = libres.get(random.nextInt(libres.size()));
				long nueva = Math.max(limiteAntiguo, tope - (long) (random.nextDouble() * 30 * DIA_MS));
				elegido.setFecha_publicacion(new Date(nueva));
			}
			// Se quita por identidad: Vehiculo.equals compara por id y aquí aún es
			// null en todos, así que libres.remove(elegido) quitaría otro vehículo.
			for (int i = 0; i < libres.size(); i++) {
				if (libres.get(i) == elegido) {
					libres.remove(i);
					break;
				}
			}
			vendidos.add(elegido);
			fechas.add(fecha);
		}
		// Todos los vehículos se persisten antes de cualquier flush: las listas
		// inversas de las características ya persistidas los referencian, y un
		// flush a mitad de camino encontraría vehículos aún sin persistir.
		for (Vehiculo vehiculo : vehiculos) {
			em.persist(vehiculo);
		}
		conteo.put("Vehiculo", vehiculos.size());
		em.flush();
		pendientes = 0;
		List<Venta> ventas = new ArrayList<Venta>();
		for (int k = 0; k < vendidos.size(); k++) {
			Vehiculo vehiculo = vendidos.get(k);
			Cliente comprador = clienteDistinto(clientes, vehiculo.getCodigo_dueno());
			Venta venta = new Venta(fechas.get(k), vehiculo, comprador);
			persistir(venta, "Venta");
			vehiculo.setCodigo_venta(venta);
			vehiculo.setActivo(false);
			fechasVenta.put(vehiculo, fechas.get(k));
			ventas.add(venta);
		}
		return ventas;
	}

	// ------------------------------------------------------------------ preguntas

	/**
	 * Crea las preguntas. Los vehículos más buscados reciben más preguntas.
	 *
	 * @param vehiculos   vehículos generados
	 * @param clientes    todos los clientes
	 * @param fechasVenta fecha de venta de los vehículos vendidos
	 */
	private void crearPreguntas(List<Vehiculo> vehiculos, List<Cliente> clientes, Map<Vehiculo, Date> fechasVenta) {
		Map<TipoVehiculo, List<PreguntaDemo>> porTipo = new EnumMap<TipoVehiculo, List<PreguntaDemo>>(
				TipoVehiculo.class);
		for (TipoVehiculo tipo : TIPOS_USADOS) {
			List<PreguntaDemo> lista = new ArrayList<PreguntaDemo>();
			for (PreguntaDemo pregunta : CatalogoDemo.PREGUNTAS) {
				if (aplica(pregunta.aplicaA, tipo)) {
					lista.add(pregunta);
				}
			}
			porTipo.put(tipo, lista);
		}
		int[] pesos = new int[vehiculos.size()];
		for (int i = 0; i < pesos.length; i++) {
			pesos[i] = 10 + vehiculos.get(i).getNumero_busquedas();
		}
		for (int i = 0; i < PREGUNTAS; i++) {
			Vehiculo vehiculo = vehiculos.get(elegirPonderado(pesos));
			List<PreguntaDemo> lista = porTipo.get(vehiculo.getTipo_vehiculo());
			PreguntaDemo plantilla = lista.get(random.nextInt(lista.size()));
			Cliente cliente = clienteDistinto(clientes, vehiculo.getCodigo_dueno());
			Date fin = fechasVenta.containsKey(vehiculo) ? fechasVenta.get(vehiculo) : hoy;
			Date fecha = entre(vehiculo.getFecha_publicacion(), fin);
			Pregunta pregunta = new Pregunta(vehiculo, cliente, fecha, recortar(plantilla.pregunta, 500));
			if (random.nextDouble() < 0.6 && plantilla.respuestas.length > 0) {
				pregunta.setRespuesta(recortar(elegir(plantilla.respuestas), 1000));
				pregunta.setFecha_respuesta(entre(fecha, hoy));
			}
			persistir(pregunta, "Pregunta");
		}
	}

	// ------------------------------------------------------------------ calificaciones

	/**
	 * Crea calificaciones solo para pares comprador y vendedor que existan en las
	 * ventas, sin repetir ningún par.
	 *
	 * @param ventas     ventas generadas
	 * @param clientes   todos los clientes
	 * @param existentes calificaciones ya creadas
	 */
	private void crearCalificaciones(List<Venta> ventas, List<Cliente> clientes, List<Calificacion> existentes) {
		Map<Cliente, Integer> indice = new IdentityHashMap<Cliente, Integer>();
		for (int i = 0; i < clientes.size(); i++) {
			indice.put(clientes.get(i), i);
		}
		Set<Long> pares = new HashSet<Long>();
		for (Calificacion calificacion : existentes) {
			Integer a = indice.get(calificacion.getCalificador());
			Integer b = indice.get(calificacion.getVendedor());
			if (a != null && b != null) {
				pares.add(par(a, b));
			}
		}
		List<ComentarioDemo> buenos = new ArrayList<ComentarioDemo>();
		for (ComentarioDemo comentario : CatalogoDemo.COMENTARIOS) {
			if (comentario.estrellas >= 4) {
				buenos.add(comentario);
			}
		}
		List<Venta> orden = new ArrayList<Venta>(ventas);
		Collections.shuffle(orden, random);
		int creadas = 0;
		for (Venta venta : orden) {
			if (creadas >= CALIFICACIONES) {
				break;
			}
			Cliente comprador = venta.getCodigo_usuario();
			Cliente vendedor = venta.getCodigo_vehiculo().getCodigo_dueno();
			if (!pares.add(par(indice.get(comprador), indice.get(vendedor)))) {
				continue;
			}
			ComentarioDemo comentario;
			if (!buenos.isEmpty() && random.nextDouble() < 0.75) {
				comentario = buenos.get(random.nextInt(buenos.size()));
			} else {
				comentario = CatalogoDemo.COMENTARIOS[random.nextInt(CatalogoDemo.COMENTARIOS.length)];
			}
			long maxima = Math.min(hoy.getTime(), venta.getFecha().getTime() + 30L * DIA_MS);
			long minima = Math.min(maxima, venta.getFecha().getTime() + DIA_MS);
			Date fecha = entre(new Date(minima), new Date(maxima));
			persistir(new Calificacion(comprador, vendedor, comentario.estrellas, recortar(comentario.texto, 500),
					fecha), "Calificacion");
			creadas++;
		}
	}

	// ------------------------------------------------------------------ favoritos

	/**
	 * Crea favoritos únicos (cliente y vehículo), nunca sobre un vehículo propio y
	 * preferiblemente sobre vehículos activos. El lado dueño de la relación es
	 * {@code Vehiculo.clientes}; se mantiene también el lado inverso.
	 *
	 * @param vehiculos vehículos generados
	 * @param clientes  todos los clientes
	 */
	private void crearFavoritos(List<Vehiculo> vehiculos, List<Cliente> clientes) {
		List<Vehiculo> activos = new ArrayList<Vehiculo>();
		for (Vehiculo vehiculo : vehiculos) {
			if (vehiculo.isActivo()) {
				activos.add(vehiculo);
			}
		}
		Set<String> usados = new HashSet<String>();
		int creados = 0;
		int intentos = 0;
		while (creados < FAVORITOS && intentos < FAVORITOS * 50) {
			intentos++;
			List<Vehiculo> fuente = (random.nextDouble() < 0.9 && !activos.isEmpty()) ? activos : vehiculos;
			Vehiculo vehiculo = fuente.get(random.nextInt(fuente.size()));
			Cliente cliente = clientes.get(random.nextInt(clientes.size()));
			if (cliente == vehiculo.getCodigo_dueno()) {
				continue;
			}
			String clave = System.identityHashCode(vehiculo) + ":" + System.identityHashCode(cliente);
			if (!usados.add(clave)) {
				continue;
			}
			vehiculo.getClientes().add(cliente);
			if (cliente.getVehiculos_favoritos() == null) {
				cliente.setVehiculos_favoritos(new ArrayList<Vehiculo>());
			}
			cliente.getVehiculos_favoritos().add(vehiculo);
			creados++;
		}
		conteo.put("Favorito", creados);
	}

	// ------------------------------------------------------------------ utilidades

	/**
	 * Persiste una entidad, la cuenta en el resumen y hace {@code flush} cada
	 * cien entidades (sin {@code clear}).
	 *
	 * @param entidad entidad a persistir
	 * @param tipo    nombre de la tabla para el resumen
	 */
	private void persistir(Object entidad, String tipo) {
		em.persist(entidad);
		Integer actual = conteo.get(tipo);
		conteo.put(tipo, actual == null ? 1 : actual + 1);
		pendientes++;
		if (pendientes >= LOTE) {
			em.flush();
			pendientes = 0;
		}
	}

	/**
	 * Indica si un arreglo de tipos incluye el tipo dado ({@code null} significa
	 * todos).
	 *
	 * @param aplicaA tipos permitidos o {@code null}
	 * @param tipo    tipo a comprobar
	 * @return {@code true} si aplica
	 */
	private boolean aplica(TipoVehiculo[] aplicaA, TipoVehiculo tipo) {
		if (aplicaA == null) {
			return true;
		}
		for (TipoVehiculo t : aplicaA) {
			if (t == tipo) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Elige un cliente distinto del indicado.
	 *
	 * @param clientes clientes disponibles
	 * @param excluido cliente que no puede salir
	 * @return cliente elegido
	 */
	private Cliente clienteDistinto(List<Cliente> clientes, Cliente excluido) {
		Cliente cliente;
		do {
			cliente = clientes.get(random.nextInt(clientes.size()));
		} while (cliente == excluido);
		return cliente;
	}

	/**
	 * Elige un índice según pesos enteros no negativos.
	 *
	 * @param pesos pesos de cada posición
	 * @return índice elegido
	 */
	private int elegirPonderado(int[] pesos) {
		long total = 0;
		for (int peso : pesos) {
			total += peso;
		}
		if (total <= 0) {
			return random.nextInt(pesos.length);
		}
		long punto = (long) (random.nextDouble() * total);
		long acumulado = 0;
		for (int i = 0; i < pesos.length; i++) {
			acumulado += pesos[i];
			if (punto < acumulado) {
				return i;
			}
		}
		return pesos.length - 1;
	}

	/**
	 * Elige un elemento al azar de un arreglo.
	 *
	 * @param <T>      tipo de los elementos
	 * @param opciones arreglo no vacío
	 * @return elemento elegido
	 */
	private <T> T elegir(T[] opciones) {
		return opciones[random.nextInt(opciones.length)];
	}

	/**
	 * Fecha anterior a hoy.
	 *
	 * @param dias días hacia atrás
	 * @return fecha resultante con hora aleatoria
	 */
	private Date hace(int dias) {
		return new Date(hoy.getTime() - dias * DIA_MS - (long) (random.nextDouble() * DIA_MS));
	}

	/**
	 * Fecha aleatoria entre dos fechas, sin pasar de hoy. Si el rango es vacío
	 * devuelve la fecha inicial.
	 *
	 * @param desde fecha inicial
	 * @param hasta fecha final
	 * @return fecha entre ambas
	 */
	private Date entre(Date desde, Date hasta) {
		long a = desde.getTime();
		long b = Math.min(hasta.getTime(), hoy.getTime());
		if (b <= a) {
			return new Date(Math.min(a, hoy.getTime()));
		}
		return new Date(a + (long) (random.nextDouble() * (b - a)));
	}

	/**
	 * Combina dos índices en una sola clave.
	 *
	 * @param a primer índice
	 * @param b segundo índice
	 * @return clave del par ordenado
	 */
	private long par(int a, int b) {
		return (long) a * 100000L + b;
	}

	/**
	 * Genera una cadena de dígitos.
	 *
	 * @param cantidad número de dígitos
	 * @return dígitos aleatorios
	 */
	private String digitos(int cantidad) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cantidad; i++) {
			sb.append((char) ('0' + random.nextInt(10)));
		}
		return sb.toString();
	}

	/**
	 * Genera una cadena de letras mayúsculas de la A a la Z.
	 *
	 * @param cantidad número de letras
	 * @return letras aleatorias
	 */
	private String letras(int cantidad) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cantidad; i++) {
			sb.append((char) ('A' + random.nextInt(26)));
		}
		return sb.toString();
	}

	/**
	 * Recorta un texto a una longitud máxima.
	 *
	 * @param texto  texto original
	 * @param maximo longitud máxima
	 * @return texto recortado si hacía falta
	 */
	private static String recortar(String texto, int maximo) {
		return texto.length() <= maximo ? texto : texto.substring(0, maximo);
	}

	/**
	 * Devuelve la primera palabra de un texto.
	 *
	 * @param texto texto original
	 * @return primera palabra
	 */
	private static String primeraPalabra(String texto) {
		String limpio = texto.trim();
		int espacio = limpio.indexOf(' ');
		return espacio < 0 ? limpio : limpio.substring(0, espacio);
	}

	/**
	 * Quita tildes y eñes y deja solo letras y dígitos en minúsculas.
	 *
	 * @param texto texto original
	 * @return texto normalizado (nunca vacío)
	 */
	private static String soloLetras(String texto) {
		String limpio = clave(texto);
		return limpio.isEmpty() ? "x" : limpio;
	}

	/**
	 * Clave de comparación sin tildes, mayúsculas, espacios ni signos.
	 *
	 * @param texto texto original
	 * @return clave normalizada
	 */
	private static String clave(String texto) {
		String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		return sinTildes.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}

}
