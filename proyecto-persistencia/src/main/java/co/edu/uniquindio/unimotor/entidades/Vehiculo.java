package co.edu.uniquindio.unimotor.entidades;

import java.io.Serializable;
import java.lang.String;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

/**
 * Vehículo publicado a la venta en uniMotor: es la entidad central del portal de compra/venta.
 * <p>
 * Tabla: {@code Vehiculo}. Relaciones: muchos a uno con {@link Ciudad}, {@link Modelo} y {@link Cliente}
 * (dueño/vendedor); uno a uno con {@link Venta} (lado inverso); uno a muchos con {@link Pregunta};
 * muchos a muchos con {@link Cliente} (favoritos, tabla {@code favorito}) y con {@link Caracteristica}
 * (tabla {@code caracteristica_vehiculo}); colección de fotos en la tabla {@code foto_Vehiculo}.
 */
@Entity
@NamedQueries({
	@NamedQuery(name = Vehiculo.FIND_BY_ID, query = "select v from Vehiculo v where v.codigo_vehiculo = :codigo"),
	@NamedQuery(name = Vehiculo.GETALL, query = "select v from Vehiculo v"),
	@NamedQuery(name = Vehiculo.FIND_BY_PLACA, query = "select v from Vehiculo v where v.placa = :placa"),
	@NamedQuery(name = Vehiculo.GET_ALL_COLOR, query = "select DISTINCT v.color from Vehiculo v"),
	@NamedQuery(name = Vehiculo.FIND_BY_COLOR, query = "select v from Vehiculo v where v.color = :color"),
	@NamedQuery(name = Vehiculo.FIND_BY_MIN_PRECIO, query = "select v from Vehiculo v where v.precio > :precio"),
	@NamedQuery(name = Vehiculo.FIND_BY_MAX_PRECIO, query = "select v from Vehiculo v where v.precio < :precio"),
	@NamedQuery(name = Vehiculo.FIND_BY_RANGO_PRECIO, query = "select v from Vehiculo v where v.precio between :precioMin and :precioMax"),
	@NamedQuery(name = Vehiculo.FIND_BY_ASCENDENTE_PRECIO, query = "select v from Vehiculo v order by v.precio ASC"),
	@NamedQuery(name = Vehiculo.FIND_BY_DESCENDENTE_PRECIO, query = "select v from Vehiculo v order by v.precio DESC"),
	@NamedQuery(name = Vehiculo.FIND_BY_YEAR, query = "select v from Vehiculo v where v.year = :year"),
	@NamedQuery(name = Vehiculo.VEHICULOS_POR_DUENO, query = "select v from Vehiculo v where v.codigo_dueno.codigo_usuario = :codigo order by v.fecha_publicacion desc"),
	@NamedQuery(name = Vehiculo.VEHICULOS_POR_EMAIL_DUENO, query = "select v from Vehiculo v where v.codigo_dueno.email = :email order by v.fecha_publicacion desc"),
	@NamedQuery(name = Vehiculo.BUSQUEDA_COMBINADA, query = "select v from Vehiculo v where v.activo = true and (:tipo IS NULL OR v.tipo_vehiculo = :tipo) and (:marca IS NULL OR v.codigo_modelo.marca = :marca) and (:precioMin IS NULL OR v.precio >= :precioMin) and (:precioMax IS NULL OR v.precio <= :precioMax) and (:anioMin IS NULL OR v.year >= :anioMin) and (:anioMax IS NULL OR v.year <= :anioMax) order by v.fecha_publicacion desc"),
	@NamedQuery(name = Vehiculo.VEHICULOS_POR_CARACTERISTICA, query = "select v from Vehiculo v join v.caracteristicas c where v.activo = true and c.codigo_caracteristica = :codigoCaracteristica"),
	@NamedQuery(name = Vehiculo.VEHICULOS_CON_CARACTERISTICAS, query = "select distinct v from Vehiculo v join v.caracteristicas c where v.activo = true and c.codigo_caracteristica in :caracteristicas"),
	@NamedQuery(name = Vehiculo.TOP_BUSCADOS, query = "select v from Vehiculo v where v.activo = true and v.condicion = :condicion order by v.numero_busquedas desc"),
	@NamedQuery(name = Vehiculo.VEHICULOS_ACTIVOS, query = "select v from Vehiculo v where v.activo = true order by v.fecha_publicacion desc"),
	@NamedQuery(name = Vehiculo.ESTADISTICA_POR_TIPO, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(v.tipo_vehiculo, count(v)) from Vehiculo v group by v.tipo_vehiculo order by count(v) desc"),
	@NamedQuery(name = Vehiculo.ESTADISTICA_POR_CONDICION, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(v.condicion, count(v)) from Vehiculo v group by v.condicion order by count(v) desc"),
	@NamedQuery(name = Vehiculo.ESTADISTICA_POR_TRANSMISION, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(v.tipo_trasmision, count(v)) from Vehiculo v group by v.tipo_trasmision order by count(v) desc"),
	@NamedQuery(name = Vehiculo.ESTADISTICA_POR_COMBUSTIBLE, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(v.tipo_combustible, count(v)) from Vehiculo v group by v.tipo_combustible order by count(v) desc"),
	@NamedQuery(name = Vehiculo.ESTADISTICA_COMBUSTIBLE_POR_TIPO, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(v.tipo_vehiculo, v.tipo_combustible, count(v)) from Vehiculo v group by v.tipo_vehiculo, v.tipo_combustible order by v.tipo_vehiculo, v.tipo_combustible"),
	@NamedQuery(name = Vehiculo.ESTADISTICA_POR_MARCA, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(m.marca, count(v)) from Vehiculo v join v.codigo_modelo m group by m.marca order by count(v) desc"),
	@NamedQuery(name = Vehiculo.ESTADISTICA_PRECIO_PROMEDIO_POR_MARCA, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(m.marca, avg(v.precio)) from Vehiculo v join v.codigo_modelo m group by m.marca order by avg(v.precio) desc"),
	@NamedQuery(name = Vehiculo.ESTADISTICA_POR_CIUDAD, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(ci.nombre, count(v)) from Vehiculo v join v.codigo_ciudad ci group by ci.nombre order by count(v) desc"),
	@NamedQuery(name = Vehiculo.ESTADISTICA_POR_ANIO, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(v.year, count(v)) from Vehiculo v group by v.year order by v.year"),
	@NamedQuery(name = Vehiculo.ESTADISTICA_TOP_BUSCADOS, query = "select new co.edu.uniquindio.unimotor.dto.EstadisticaDTO(concat(m.marca, ' ', m.nombre, ' ', v.placa), v.numero_busquedas) from Vehiculo v join v.codigo_modelo m order by v.numero_busquedas desc"),
	@NamedQuery(name = Vehiculo.PUNTOS_PRECIO_KILOMETRAJE, query = "select new co.edu.uniquindio.unimotor.dto.PuntoDTO(v.kilometros, v.precio) from Vehiculo v"),
	@NamedQuery(name = Vehiculo.PUNTOS_PRECIO_ANIO_BUSQUEDAS, query = "select new co.edu.uniquindio.unimotor.dto.PuntoDTO(v.year, v.precio, v.numero_busquedas) from Vehiculo v")
})
public class Vehiculo implements Serializable {

	/**
	 * Identificador único autogenerado (clave primaria).
	 */
	@Id
	@Column(name = "codigo_vehiculo")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long codigo_vehiculo;

	/**
	 * Descripción del vehículo; obligatoria, máximo 1000 caracteres.
	 */
	@Column(name = "descripcion", nullable = false, length = 1000)
	private String descripcion;

	/**
	 * Color del vehículo; obligatorio, máximo 30 caracteres.
	 */
	@Column(name = "color", nullable = false, length = 30)
	private String color;

	/**
	 * Placa del vehículo; obligatoria, única, máximo 10 caracteres.
	 */
	@Column(name = "placa", nullable = false, unique = true, length = 10)
	private String placa;

	/**
	 * Precio de venta; obligatorio.
	 */
	@Column(name = "precio", nullable = false)
	private Double precio;

	/**
	 * Año (modelo) del vehículo; obligatorio.
	 */
	@Column(name = "year", nullable = false)
	private int year;

	/**
	 * Cilindraje en centímetros cúbicos; obligatorio.
	 */
	@Column(name = "cilindraje", nullable = false)
	private int cilindraje;

	/**
	 * Número de puertas; opcional.
	 */
	@Column(name = "numero_puertas")
	private int numero_puertas;

	/**
	 * Kilometraje recorrido; obligatorio (0 para vehículos nuevos).
	 */
	@Column(name = "kilometros", nullable = false)
	private int kilometros;

	/**
	 * Ciudad donde está ubicado el vehículo; obligatoria.
	 */
	@JoinColumn(name = "codigo_ciudad", nullable = false)
	@ManyToOne
	private Ciudad codigo_ciudad;

	/**
	 * Cliente dueño y vendedor del vehículo; obligatorio.
	 */
	@JoinColumn(name = "codigo_dueno", nullable = false)
	@ManyToOne
	private Cliente codigo_dueno;

	/**
	 * Venta asociada si el vehículo ya fue vendido; nula en caso contrario (lado inverso de {@link Venta#getCodigo_vehiculo()}).
	 */
	@OneToOne(mappedBy = "codigo_vehiculo")
	private Venta codigo_venta;

	/**
	 * Modelo (y marca) del vehículo; obligatorio.
	 */
	@JoinColumn(name = "codigo_modelo", nullable = false)
	@ManyToOne
	private Modelo codigo_modelo;

	/**
	 * Tipo de combustible; obligatorio, se guarda como texto.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_combustible", nullable = false)
	private TipoCombustible tipo_combustible;

	/**
	 * Categoría del vehículo; obligatoria, se guarda como texto.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_vehiculo", nullable = false)
	private TipoVehiculo tipo_vehiculo;

	/**
	 * Tipo de transmisión; obligatorio, se guarda como texto.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_trasmision", nullable = false)
	private TipoTrasmision tipo_trasmision;

	/**
	 * Condición comercial (nuevo o usado); obligatoria, se guarda como texto.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "condicion", nullable = false)
	private Condicion condicion;

	/**
	 * Preguntas hechas sobre el vehículo (lado inverso de {@link Pregunta#getVehiculo()}).
	 */
	@OneToMany(mappedBy = "vehiculo")
	private List<Pregunta> preguntas;

	/**
	 * Contador de veces que el vehículo ha sido consultado; obligatorio, inicia en 0. Alimenta el top de más buscados.
	 */
	@Column(name = "numero_busquedas", nullable = false)
	private int numero_busquedas = 0;

	/**
	 * Fecha y hora de publicación; obligatoria (se asigna al persistir si viene vacía, ver {@link #prePersist()}).
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha_publicacion", nullable = false)
	private Date fecha_publicacion;

	/**
	 * Indica si la publicación está activa (visible en las búsquedas); obligatorio, por defecto {@code true}.
	 */
	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	/**
	 * URLs de las fotos del vehículo (tabla {@code foto_Vehiculo}).
	 */
	@ElementCollection
	@CollectionTable(name = "foto_Vehiculo", joinColumns = @JoinColumn(name = "codigo_vehiculo"))
	@Column(name = "url_foto")
	private List<String> url_foto;

	/**
	 * Clientes que marcaron el vehículo como favorito (lado propietario; tabla {@code favorito}).
	 */
	@JoinTable(name = "favorito", joinColumns = @JoinColumn(name = "codigo_vehiculo"), inverseJoinColumns = @JoinColumn(name = "codigo_persona"))
	@ManyToMany
	private List<Cliente> clientes;

	/**
	 * Características o equipamiento del vehículo (lado propietario; tabla {@code caracteristica_vehiculo}).
	 */
	@JoinTable(name = "caracteristica_vehiculo", joinColumns = @JoinColumn(name = "codigo_vehiculo"), inverseJoinColumns = @JoinColumn(name = "codigo_caracteristica"))
	@ManyToMany
	private List<Caracteristica> caracteristicas;

	/**
	 * Versión de serialización.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * NamedQuery que busca un vehículo por identificador.
	 * Parámetro: {@code :codigo} (Long). Devuelve la entidad {@link Vehiculo}.
	 */
	public static final String FIND_BY_ID = "Vehiculo_FIND_BY_ID";
	/**
	 * NamedQuery sin parámetros que devuelve todos los {@link Vehiculo}.
	 */
	public static final String GETALL = "Vehiculo_GETALL";
	/**
	 * NamedQuery que busca un vehículo por placa.
	 * Parámetro: {@code :placa} (String). Devuelve la entidad {@link Vehiculo}.
	 */
	public static final String FIND_BY_PLACA = "Vehiculo_FIND_BY_PLACA";
	/**
	 * NamedQuery sin parámetros que devuelve los colores distintos (String) registrados.
	 */
	public static final String GET_ALL_COLOR = "Vehiculo_GET_ALL_COLOR";
	/**
	 * NamedQuery que lista los vehículos de un color.
	 * Parámetro: {@code :color} (String). Devuelve entidades {@link Vehiculo}.
	 */
	public static final String FIND_BY_COLOR = "Vehiculo_FIND_BY_COLOR";
	/**
	 * NamedQuery que lista los vehículos con precio mayor al indicado.
	 * Parámetro: {@code :precio} (Double). Devuelve entidades {@link Vehiculo}.
	 */
	public static final String FIND_BY_MIN_PRECIO = "Vehiculo_FIND_BY_MIN_PRECIO";
	/**
	 * NamedQuery que lista los vehículos con precio menor al indicado.
	 * Parámetro: {@code :precio} (Double). Devuelve entidades {@link Vehiculo}.
	 */
	public static final String FIND_BY_MAX_PRECIO = "Vehiculo_FIND_BY_MAX_PRECIO";
	/**
	 * NamedQuery que lista los vehículos con precio dentro de un rango (inclusive).
	 * Parámetros: {@code :precioMin} y {@code :precioMax} (Double). Devuelve entidades {@link Vehiculo}.
	 */
	public static final String FIND_BY_RANGO_PRECIO = "Vehiculo_FIND_BY_RANGO_PRECIO";
	/**
	 * NamedQuery sin parámetros que devuelve todos los vehículos ordenados por precio ascendente.
	 */
	public static final String FIND_BY_ASCENDENTE_PRECIO = "Vehiculo_FIND_BY_ASCENDENTE_PRECIO";
	/**
	 * NamedQuery sin parámetros que devuelve todos los vehículos ordenados por precio descendente.
	 */
	public static final String FIND_BY_DESCENDENTE_PRECIO = "Vehiculo_FIND_BY_DESCENDENTE_PRECIO";
	/**
	 * NamedQuery que lista los vehículos de un año.
	 * Parámetro: {@code :year} (int). Devuelve entidades {@link Vehiculo}.
	 */
	public static final String FIND_BY_YEAR = "Vehiculo_FIND_BY_YEAR";
	/**
	 * NamedQuery que lista los vehículos de un dueño, del más reciente al más antiguo.
	 * Parámetro: {@code :codigo} (Long, {@code codigo_usuario} del dueño). Devuelve entidades {@link Vehiculo}.
	 */
	public static final String VEHICULOS_POR_DUENO = "Vehiculo_VEHICULOS_POR_DUENO";
	/**
	 * NamedQuery que lista los vehículos de un dueño por su correo, del más reciente al más antiguo.
	 * Parámetro: {@code :email} (String). Devuelve entidades {@link Vehiculo}.
	 */
	public static final String VEHICULOS_POR_EMAIL_DUENO = "Vehiculo_VEHICULOS_POR_EMAIL_DUENO";
	/**
	 * NamedQuery de búsqueda combinada sobre vehículos activos; cada filtro es opcional (si el parámetro es nulo se ignora).
	 * Parámetros: {@code :tipo} ({@link TipoVehiculo}), {@code :marca} (String), {@code :precioMin} y {@code :precioMax} (Double),
	 * {@code :anioMin} y {@code :anioMax} (int). Devuelve entidades {@link Vehiculo}, las más recientes primero.
	 */
	public static final String BUSQUEDA_COMBINADA = "Vehiculo_BUSQUEDA_COMBINADA";
	/**
	 * NamedQuery que lista los vehículos activos que tienen una característica.
	 * Parámetro: {@code :codigoCaracteristica} (Long). Devuelve entidades {@link Vehiculo}.
	 */
	public static final String VEHICULOS_POR_CARACTERISTICA = "Vehiculo_VEHICULOS_POR_CARACTERISTICA";
	/**
	 * NamedQuery que lista los vehículos activos que tienen al menos una de las características indicadas.
	 * Parámetro: {@code :caracteristicas} (colección de Long con los códigos de característica). Devuelve entidades {@link Vehiculo} sin repetir.
	 */
	public static final String VEHICULOS_CON_CARACTERISTICAS = "Vehiculo_VEHICULOS_CON_CARACTERISTICAS";
	/**
	 * Top de más buscados por condición, ordenado por {@code numero_busquedas} descendente.
	 * Parámetro: {@code :condicion} ({@link Condicion}). Devuelve entidades {@link Vehiculo} activos.
	 * El límite de 10 lo aplica el negocio con {@code setMaxResults(10)}.
	 */
	public static final String TOP_BUSCADOS = "Vehiculo_TOP_BUSCADOS";
	/**
	 * NamedQuery sin parámetros que lista los vehículos activos, del más reciente al más antiguo.
	 */
	public static final String VEHICULOS_ACTIVOS = "Vehiculo_VEHICULOS_ACTIVOS";
	/**
	 * NamedQuery de estadística sin parámetros: vehículos por tipo de vehículo, de mayor a menor.
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = tipo, valor = conteo).
	 */
	public static final String ESTADISTICA_POR_TIPO = "Vehiculo_ESTADISTICA_POR_TIPO";
	/**
	 * NamedQuery de estadística sin parámetros: vehículos por condición (nuevo/usado).
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = condición, valor = conteo).
	 */
	public static final String ESTADISTICA_POR_CONDICION = "Vehiculo_ESTADISTICA_POR_CONDICION";
	/**
	 * NamedQuery de estadística sin parámetros: vehículos por tipo de transmisión.
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = transmisión, valor = conteo).
	 */
	public static final String ESTADISTICA_POR_TRANSMISION = "Vehiculo_ESTADISTICA_POR_TRANSMISION";
	/**
	 * NamedQuery de estadística sin parámetros: vehículos por tipo de combustible.
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = combustible, valor = conteo).
	 */
	public static final String ESTADISTICA_POR_COMBUSTIBLE = "Vehiculo_ESTADISTICA_POR_COMBUSTIBLE";
	/**
	 * NamedQuery de estadística sin parámetros: vehículos por tipo de vehículo y combustible (gráfico de series).
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = tipo de vehículo, serie = combustible, valor = conteo).
	 */
	public static final String ESTADISTICA_COMBUSTIBLE_POR_TIPO = "Vehiculo_ESTADISTICA_COMBUSTIBLE_POR_TIPO";
	/**
	 * NamedQuery de estadística sin parámetros: vehículos por marca, de mayor a menor.
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = marca, valor = conteo).
	 */
	public static final String ESTADISTICA_POR_MARCA = "Vehiculo_ESTADISTICA_POR_MARCA";
	/**
	 * NamedQuery de estadística sin parámetros: precio promedio por marca, de mayor a menor.
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = marca, valor = promedio).
	 */
	public static final String ESTADISTICA_PRECIO_PROMEDIO_POR_MARCA = "Vehiculo_ESTADISTICA_PRECIO_PROMEDIO_POR_MARCA";
	/**
	 * NamedQuery de estadística sin parámetros: vehículos por ciudad, de mayor a menor.
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = ciudad, valor = conteo).
	 */
	public static final String ESTADISTICA_POR_CIUDAD = "Vehiculo_ESTADISTICA_POR_CIUDAD";
	/**
	 * NamedQuery de estadística sin parámetros: vehículos por año, en orden cronológico.
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = año, valor = conteo).
	 */
	public static final String ESTADISTICA_POR_ANIO = "Vehiculo_ESTADISTICA_POR_ANIO";
	/**
	 * NamedQuery de estadística sin parámetros: vehículos ordenados por número de búsquedas (el límite lo aplica el negocio).
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.EstadisticaDTO} (etiqueta = "marca modelo placa", valor = búsquedas).
	 */
	public static final String ESTADISTICA_TOP_BUSCADOS = "Vehiculo_ESTADISTICA_TOP_BUSCADOS";
	/**
	 * NamedQuery sin parámetros para el gráfico de dispersión.
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.PuntoDTO} (x = kilómetros, y = precio).
	 */
	public static final String PUNTOS_PRECIO_KILOMETRAJE = "Vehiculo_PUNTOS_PRECIO_KILOMETRAJE";
	/**
	 * NamedQuery sin parámetros para el gráfico de burbujas.
	 * Devuelve {@link co.edu.uniquindio.unimotor.dto.PuntoDTO} (x = año, y = precio, tamaño = número de búsquedas).
	 */
	public static final String PUNTOS_PRECIO_ANIO_BUSQUEDAS = "Vehiculo_PUNTOS_PRECIO_ANIO_BUSQUEDAS";

	/**
	 * Constructor vacío requerido por JPA.
	 */
	public Vehiculo() {
		super();
	}

	/**
	 * Crea un vehículo con sus datos obligatorios. {@code numero_busquedas} inicia en 0, {@code activo} en {@code true}
	 * y la fecha de publicación se asigna al persistir.
	 *
	 * @param descripcion descripción del vehículo
	 * @param color color
	 * @param placa placa (única)
	 * @param precio precio de venta
	 * @param year año del vehículo
	 * @param cilindraje cilindraje en cc
	 * @param numero_puertas número de puertas
	 * @param kilometros kilometraje recorrido
	 * @param codigo_ciudad ciudad donde está el vehículo
	 * @param codigo_dueno cliente dueño
	 * @param codigo_modelo modelo del vehículo
	 * @param tipo_combustible tipo de combustible
	 * @param tipo_vehiculo categoría del vehículo
	 * @param tipo_trasmision tipo de transmisión
	 * @param condicion condición (nuevo o usado)
	 */
	public Vehiculo(String descripcion, String color, String placa, Double precio, int year, int cilindraje,
			int numero_puertas, int kilometros, Ciudad codigo_ciudad, Cliente codigo_dueno,
			Modelo codigo_modelo, TipoCombustible tipo_combustible, TipoVehiculo tipo_vehiculo,
			TipoTrasmision tipo_trasmision, Condicion condicion) {
		super();
		this.descripcion = descripcion;
		this.color = color;
		this.placa = placa;
		this.precio = precio;
		this.year = year;
		this.cilindraje = cilindraje;
		this.numero_puertas = numero_puertas;
		this.kilometros = kilometros;
		this.codigo_ciudad = codigo_ciudad;
		this.codigo_dueno = codigo_dueno;
		this.codigo_modelo = codigo_modelo;
		this.tipo_combustible = tipo_combustible;
		this.tipo_vehiculo = tipo_vehiculo;
		this.tipo_trasmision = tipo_trasmision;
		this.condicion = condicion;
	}

	/**
	 * Calcula el hash a partir del identificador {@code codigo_vehiculo}.
	 *
	 * @return código hash del vehículo
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo_vehiculo == null) ? 0 : codigo_vehiculo.hashCode());
		return result;
	}

	/**
	 * Dos vehículos son iguales si son de la misma clase y tienen el mismo {@code codigo_vehiculo}.
	 *
	 * @param obj objeto a comparar
	 * @return {@code true} si representan el mismo vehículo
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vehiculo other = (Vehiculo) obj;
		if (codigo_vehiculo == null) {
			if (other.codigo_vehiculo != null)
				return false;
		} else if (!codigo_vehiculo.equals(other.codigo_vehiculo))
			return false;
		return true;
	}

	/**
	 * Obtiene el valor de {@code Codigo_vehiculo}.
	 *
	 * @return valor de {@code Codigo_vehiculo}
	 */
	public Long getCodigo_vehiculo() {
		return this.codigo_vehiculo;
	}

	/**
	 * Establece el valor de {@code Codigo_vehiculo}.
	 *
	 * @param codigo_vehiculo nuevo valor
	 */
	public void setCodigo_vehiculo(Long codigo_vehiculo) {
		this.codigo_vehiculo = codigo_vehiculo;
	}

	/**
	 * Obtiene el valor de {@code Precio}.
	 *
	 * @return valor de {@code Precio}
	 */
	public Double getPrecio() {
		return this.precio;
	}

	/**
	 * Establece el valor de {@code Precio}.
	 *
	 * @param precio nuevo valor
	 */
	public void setPrecio(Double precio) {
		this.precio = precio;
	}

	/**
	 * Obtiene el valor de {@code Descripcion}.
	 *
	 * @return valor de {@code Descripcion}
	 */
	public String getDescripcion() {
		return this.descripcion;
	}

	/**
	 * Establece el valor de {@code Descripcion}.
	 *
	 * @param descripcion nuevo valor
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/**
	 * Obtiene el valor de {@code Color}.
	 *
	 * @return valor de {@code Color}
	 */
	public String getColor() {
		return this.color;
	}

	/**
	 * Establece el valor de {@code Color}.
	 *
	 * @param color nuevo valor
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * Obtiene el valor de {@code Year}.
	 *
	 * @return valor de {@code Year}
	 */
	public int getYear() {
		return this.year;
	}

	/**
	 * Establece el valor de {@code Year}.
	 *
	 * @param year nuevo valor
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * @return the codigo_ciudad
	 */
	public Ciudad getCodigo_ciudad() {
		return codigo_ciudad;
	}

	/**
	 * @param codigo_ciudad the codigo_ciudad to set
	 */
	public void setCodigo_ciudad(Ciudad codigo_ciudad) {
		this.codigo_ciudad = codigo_ciudad;
	}

	/**
	 * @return the codigo_venta
	 */
	public Venta getCodigo_venta() {
		return codigo_venta;
	}

	/**
	 * @param codigo_venta the codigo_venta to set
	 */
	public void setCodigo_venta(Venta codigo_venta) {
		this.codigo_venta = codigo_venta;
	}

	/**
	 * @return the codigo_modelo
	 */
	public Modelo getCodigo_modelo() {
		return codigo_modelo;
	}

	/**
	 * @param codigo_modelo the codigo_modelo to set
	 */
	public void setCodigo_modelo(Modelo codigo_modelo) {
		this.codigo_modelo = codigo_modelo;
	}

	/**
	 * @return the tipo_combustible
	 */
	public TipoCombustible getTipo_combustible() {
		return tipo_combustible;
	}

	/**
	 * @param tipo_combustible the tipo_combustible to set
	 */
	public void setTipo_combustible(TipoCombustible tipo_combustible) {
		this.tipo_combustible = tipo_combustible;
	}

	/**
	 * @return the tipo_vehiculo
	 */
	public TipoVehiculo getTipo_vehiculo() {
		return tipo_vehiculo;
	}

	/**
	 * @param tipo_vehiculo the tipo_vehiculo to set
	 */
	public void setTipo_vehiculo(TipoVehiculo tipo_vehiculo) {
		this.tipo_vehiculo = tipo_vehiculo;
	}

	/**
	 * @return the preguntas
	 */
	public List<Pregunta> getPreguntas() {
		return preguntas;
	}

	/**
	 * @param preguntas the preguntas to set
	 */
	public void setPreguntas(List<Pregunta> preguntas) {
		this.preguntas = preguntas;
	}

	/**
	 * @return the url_foto
	 */
	public List<String> getUrl_foto() {
		return url_foto;
	}

	/**
	 * @param url_foto the url_foto to set
	 */
	public void setUrl_foto(List<String> url_foto) {
		this.url_foto = url_foto;
	}

	/**
	 * @return the clientes
	 */
	public List<Cliente> getClientes() {
		return clientes;
	}

	/**
	 * @param clientes the clientes to set
	 */
	public void setClientes(List<Cliente> clientes) {
		this.clientes = clientes;
	}

	/**
	 * @return the caracteristicas
	 */
	public List<Caracteristica> getCaracteristicas() {
		return caracteristicas;
	}

	/**
	 * @param caracteristicas the caracteristicas to set
	 */
	public void setCaracteristicas(List<Caracteristica> caracteristicas) {
		this.caracteristicas = caracteristicas;
	}

	/**
	 * @return the placa
	 */
	public String getPlaca() {
		return placa;
	}

	/**
	 * @param placa the placa to set
	 */
	public void setPlaca(String placa) {
		this.placa = placa;
	}

	/**
	 * @return the cilindraje
	 */
	public int getCilindraje() {
		return cilindraje;
	}

	/**
	 * @param cilindraje the cilindraje to set
	 */
	public void setCilindraje(int cilindraje) {
		this.cilindraje = cilindraje;
	}

	/**
	 * @return the numero_puertas
	 */
	public int getNumero_puertas() {
		return numero_puertas;
	}

	/**
	 * @param numero_puertas the numero_puertas to set
	 */
	public void setNumero_puertas(int numero_puertas) {
		this.numero_puertas = numero_puertas;
	}

	/**
	 * @return the kilometros
	 */
	public int getKilometros() {
		return kilometros;
	}

	/**
	 * @param kilometros the kilometros to set
	 */
	public void setKilometros(int kilometros) {
		this.kilometros = kilometros;
	}

	/**
	 * @return the codigo_dueno
	 */
	public Cliente getCodigo_dueno() {
		return codigo_dueno;
	}

	/**
	 * @param codigo_dueno the codigo_dueno to set
	 */
	public void setCodigo_dueno(Cliente codigo_dueno) {
		this.codigo_dueno = codigo_dueno;
	}

	/**
	 * @return the tipo_trasmision
	 */
	public TipoTrasmision getTipo_trasmision() {
		return tipo_trasmision;
	}

	/**
	 * @param tipo_trasmision the tipo_trasmision to set
	 */
	public void setTipo_trasmision(TipoTrasmision tipo_trasmision) {
		this.tipo_trasmision = tipo_trasmision;
	}

	/**
	 * @return the condicion
	 */
	public Condicion getCondicion() {
		return condicion;
	}

	/**
	 * @param condicion the condicion to set
	 */
	public void setCondicion(Condicion condicion) {
		this.condicion = condicion;
	}

	/**
	 * Obtiene el valor de {@code Numero_busquedas}.
	 *
	 * @return valor de {@code Numero_busquedas}
	 */
	public int getNumero_busquedas() {
		return numero_busquedas;
	}

	/**
	 * Establece el valor de {@code Numero_busquedas}.
	 *
	 * @param numero_busquedas nuevo valor
	 */
	public void setNumero_busquedas(int numero_busquedas) {
		this.numero_busquedas = numero_busquedas;
	}

	/**
	 * Obtiene el valor de {@code Fecha_publicacion}.
	 *
	 * @return valor de {@code Fecha_publicacion}
	 */
	public Date getFecha_publicacion() {
		return fecha_publicacion;
	}

	/**
	 * Establece el valor de {@code Fecha_publicacion}.
	 *
	 * @param fecha_publicacion nuevo valor
	 */
	public void setFecha_publicacion(Date fecha_publicacion) {
		this.fecha_publicacion = fecha_publicacion;
	}

	/**
	 * Obtiene el valor de {@code Activo}.
	 *
	 * @return valor de {@code Activo}
	 */
	public boolean isActivo() {
		return activo;
	}

	/**
	 * Establece el valor de {@code Activo}.
	 *
	 * @param activo nuevo valor
	 */
	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	/**
	 * Callback de ciclo de vida ({@code @PrePersist}): asigna la fecha actual a {@code fecha_publicacion} si no se indicó.
	 */
	@PrePersist
	void prePersist() {
		if (fecha_publicacion == null) {
			fecha_publicacion = new Date();
		}
	}

	/**
	 * Representación en texto del vehículo.
	 *
	 * @return texto descriptivo del objeto
	 */
	@Override
	public String toString() {
		return "Vehiculo [codigo_vehiculo=" + codigo_vehiculo + ", descripcion=" + descripcion + ", placa=" + placa
				+ "]";
	}

}
