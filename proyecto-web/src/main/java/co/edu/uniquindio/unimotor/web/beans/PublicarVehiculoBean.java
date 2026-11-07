package co.edu.uniquindio.unimotor.web.beans;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import co.edu.uniquindio.unimotor.ejb.CatalogoEJB;
import co.edu.uniquindio.unimotor.ejb.VehiculoEJB;
import co.edu.uniquindio.unimotor.entidades.Caracteristica;
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Condicion;
import co.edu.uniquindio.unimotor.entidades.Modelo;
import co.edu.uniquindio.unimotor.entidades.TipoCombustible;
import co.edu.uniquindio.unimotor.entidades.TipoTrasmision;
import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.ElementoRepetidoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;
import co.edu.uniquindio.unimotor.web.fotos.ConfiguracionFotos;
import co.edu.uniquindio.unimotor.web.util.Mensajes;

/**
 * Bean del formulario para publicar un vehículo nuevo o editar uno existente ({@code ?id=N}).
 * Gestiona también la subida de fotos al disco.
 */
@Named
@ViewScoped
public class PublicarVehiculoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Número máximo de fotos por vehículo. */
	public static final int MAXIMO_FOTOS = 10;

	/** Tamaño máximo por foto: 5 MB. */
	public static final long TAMANO_MAXIMO = 5L * 1024 * 1024;

	private static final List<String> TIPOS_MIME = Arrays.asList("image/jpeg", "image/png", "image/webp");

	private static final Pattern PLACA_CARRO = Pattern.compile("^[A-Z]{3}[0-9]{3}$");
	private static final Pattern PLACA_MOTO = Pattern.compile("^[A-Z]{3}[0-9]{2}[A-Z]$");

	@EJB
	private VehiculoEJB vehiculoEJB;

	@EJB
	private CatalogoEJB catalogoEJB;

	@Inject
	private SeguridadBean seguridadBean;

	// Parámetro de la dirección y modo
	private String idTexto;
	private Long id;

	// Catálogos
	private List<String> marcas = new ArrayList<>();
	private List<Modelo> modelos = new ArrayList<>();
	private List<Caracteristica> caracteristicas = new ArrayList<>();
	private List<TipoVehiculo> tipos = new ArrayList<>();
	private List<TipoCombustible> combustibles = new ArrayList<>();
	private List<TipoTrasmision> transmisiones = new ArrayList<>();
	private List<Condicion> condiciones = new ArrayList<>();

	// Campos del formulario
	private String marca;
	private Long codigoModelo;
	private TipoVehiculo tipo;
	private Condicion condicion;
	private Integer year;
	private Double precio;
	private Integer kilometros;
	private TipoCombustible combustible;
	private TipoTrasmision transmision;
	private Integer cilindraje;
	private Integer numeroPuertas;
	private String color;
	private String placa;
	private Ciudad ciudad;
	private String descripcion;
	private List<Long> caracteristicasSeleccionadas = new ArrayList<>();

	/** Fotos del vehículo en orden; la primera es la principal. Acceso sincronizado sobre este objeto. */
	private final List<String> fotos = new ArrayList<>();

	/** Fotos subidas en esta edición que aún no se han guardado en un vehículo. */
	private final Set<String> fotosNuevas = new HashSet<>();

	private boolean guardado;

	/**
	 * Carga los catálogos del formulario.
	 */
	@PostConstruct
	public void cargarCatalogos() {
		marcas = catalogoEJB.listarMarcas();
		caracteristicas = catalogoEJB.listarCaracteristicas();
		tipos = catalogoEJB.listarTiposVehiculo();
		combustibles = catalogoEJB.listarCombustibles();
		transmisiones = catalogoEJB.listarTransmisiones();
		condiciones = catalogoEJB.listarCondiciones();
		year = Calendar.getInstance().get(Calendar.YEAR);
	}

	/**
	 * Se ejecuta en la carga inicial: en modo edición carga el vehículo y verifica que sea del cliente en sesión.
	 */
	public void inicializar() {
		FacesContext fc = FacesContext.getCurrentInstance();
		if (idTexto == null || idTexto.trim().isEmpty()) {
			return;
		}
		Long codigo;
		try {
			codigo = Long.valueOf(idTexto.trim());
		} catch (NumberFormatException e) {
			rechazar(fc, "El código de vehículo indicado no es válido.");
			return;
		}
		Vehiculo v;
		try {
			v = vehiculoEJB.buscarVehiculo(codigo);
		} catch (ElementoNoEncontradoException e) {
			rechazar(fc, "El vehículo que intentas editar no existe.");
			return;
		}
		Cliente yo = seguridadBean.getCliente();
		String emailDueno = v.getCodigo_dueno() == null ? null : v.getCodigo_dueno().getEmail();
		if (yo == null || yo.getEmail() == null || emailDueno == null || !emailDueno.equalsIgnoreCase(yo.getEmail())) {
			rechazar(fc, "Solo el dueño puede editar este vehículo.");
			return;
		}
		id = codigo;
		copiarDesde(v);
	}

	private void rechazar(FacesContext fc, String detalle) {
		Mensajes.error("No se puede editar el vehículo", detalle);
		Mensajes.conservarTrasRedireccion();
		fc.getApplication().getNavigationHandler().handleNavigation(fc, null,
				"/usuario/misVehiculos?faces-redirect=true");
		fc.responseComplete();
	}

	private void copiarDesde(Vehiculo v) {
		if (v.getCodigo_modelo() != null) {
			marca = v.getCodigo_modelo().getMarca();
			codigoModelo = v.getCodigo_modelo().getCodigo_modelo();
			cargarModelos();
		}
		tipo = v.getTipo_vehiculo();
		condicion = v.getCondicion();
		year = v.getYear();
		precio = v.getPrecio();
		kilometros = v.getKilometros();
		combustible = v.getTipo_combustible();
		transmision = v.getTipo_trasmision();
		cilindraje = v.getCilindraje();
		numeroPuertas = v.getNumero_puertas();
		color = v.getColor();
		placa = v.getPlaca();
		ciudad = v.getCodigo_ciudad();
		descripcion = v.getDescripcion();
		caracteristicasSeleccionadas = new ArrayList<>();
		if (v.getCaracteristicas() != null) {
			for (Caracteristica c : v.getCaracteristicas()) {
				if (c != null && c.getCodigo_caracteristica() != null) {
					caracteristicasSeleccionadas.add(c.getCodigo_caracteristica());
				}
			}
		}
		synchronized (fotos) {
			fotos.clear();
			if (v.getUrl_foto() != null) {
				for (String f : v.getUrl_foto()) {
					if (f != null && !f.trim().isEmpty()) {
						fotos.add(f.trim());
					}
				}
			}
		}
	}

	/**
	 * Recarga la lista de modelos según la marca elegida (se invoca desde el cambio de marca).
	 */
	public void cambiarMarca() {
		codigoModelo = null;
		cargarModelos();
	}

	private void cargarModelos() {
		if (marca == null || marca.trim().isEmpty()) {
			modelos = new ArrayList<>();
		} else {
			modelos = catalogoEJB.listarModelosPorMarca(marca);
		}
	}

	/**
	 * Sugiere 0 kilómetros cuando la condición elegida es nuevo.
	 */
	public void cambiarCondicion() {
		if (condicion == Condicion.NUEVO) {
			kilometros = 0;
		}
	}

	/**
	 * Valida el formato de la placa colombiana (carro: tres letras y tres números; moto: tres letras,
	 * dos números y una letra), ignorando mayúsculas y espacios.
	 *
	 * @param contexto contexto de JSF
	 * @param componente componente validado
	 * @param valor texto escrito
	 */
	public void validarPlaca(FacesContext contexto, UIComponent componente, Object valor) {
		String texto = normalizarPlaca(valor == null ? null : valor.toString());
		if (texto.isEmpty()) {
			return;
		}
		if (!PLACA_CARRO.matcher(texto).matches() && !PLACA_MOTO.matcher(texto).matches()) {
			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"La placa no es válida. Usa el formato ABC123 (carros) o ABC12D (motos).", null));
		}
	}

	private static String normalizarPlaca(String texto) {
		return texto == null ? "" : texto.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
	}

	// ------------------------------------------------------------------ fotos

	/**
	 * Recibe una foto subida, la valida (extensión, tipo, tamaño y firma del archivo) y la guarda en disco.
	 * Es sincronizado porque pueden llegar varias subidas a la vez para la misma vista.
	 *
	 * @param evento evento de PrimeFaces con el archivo subido
	 */
	public synchronized void subirFoto(FileUploadEvent evento) {
		UploadedFile archivo = evento.getFile();
		if (archivo == null) {
			return;
		}
		String original = archivo.getFileName();
		String nombreMostrado = original == null ? "archivo" : original.replaceAll("[\\r\\n<>]", "");
		synchronized (fotos) {
			if (fotos.size() >= MAXIMO_FOTOS) {
				Mensajes.advertencia("Límite de fotos", "Solo se permiten " + MAXIMO_FOTOS
						+ " fotos por vehículo. No se agregó " + nombreMostrado + ".");
				return;
			}
		}
		String tipoMime = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase(Locale.ROOT);
		if (!TIPOS_MIME.contains(tipoMime)) {
			Mensajes.error("Archivo no permitido", nombreMostrado + ": solo se aceptan imágenes JPG, PNG o WEBP.");
			return;
		}
		if (archivo.getSize() <= 0 || archivo.getSize() > TAMANO_MAXIMO) {
			Mensajes.error("Archivo no permitido",
					nombreMostrado + ": el tamaño debe ser mayor que cero y de máximo 5 MB.");
			return;
		}
		String nombre;
		try {
			nombre = ConfiguracionFotos.generarNombreUnico(original);
		} catch (IllegalArgumentException e) {
			Mensajes.error("Archivo no permitido", nombreMostrado + ": la extensión debe ser JPG, JPEG, PNG o WEBP.");
			return;
		}
		Path destino = null;
		try {
			destino = ConfiguracionFotos.resolver(nombre);
			if (destino == null) {
				throw new IOException("Nombre de foto no válido: " + nombre);
			}
			try (InputStream entrada = archivo.getInputStream()) {
				Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
			}
			if (!tieneFirmaDeImagen(destino) || Files.size(destino) > TAMANO_MAXIMO) {
				Files.deleteIfExists(destino);
				Mensajes.error("Archivo no permitido", nombreMostrado + ": el contenido no es una imagen válida.");
				return;
			}
		} catch (IOException e) {
			borrarSilencioso(destino);
			Mensajes.error("No se pudo guardar la foto",
					nombreMostrado + ": ocurrió un error al guardarla. Inténtalo de nuevo.");
			return;
		}
		synchronized (fotos) {
			fotos.add(nombre);
			fotosNuevas.add(nombre);
		}
	}

	/** Comprueba los primeros bytes: JPEG, PNG o WEBP (RIFF, tamaño y WEBP). */
	private static boolean tieneFirmaDeImagen(Path ruta) throws IOException {
		byte[] c = new byte[12];
		int leidos;
		try (InputStream in = Files.newInputStream(ruta)) {
			leidos = in.read(c);
		}
		if (leidos < 12) {
			return false;
		}
		boolean jpeg = (c[0] & 0xFF) == 0xFF && (c[1] & 0xFF) == 0xD8 && (c[2] & 0xFF) == 0xFF;
		boolean png = (c[0] & 0xFF) == 0x89 && c[1] == 'P' && c[2] == 'N' && c[3] == 'G';
		boolean webp = c[0] == 'R' && c[1] == 'I' && c[2] == 'F' && c[3] == 'F' && c[8] == 'W' && c[9] == 'E'
				&& c[10] == 'B' && c[11] == 'P';
		return jpeg || png || webp;
	}

	private static void borrarSilencioso(Path ruta) {
		if (ruta != null) {
			try {
				Files.deleteIfExists(ruta);
			} catch (IOException e) {
				// Sin más opciones: queda un archivo huérfano.
			}
		}
	}

	/**
	 * Quita una foto de la lista. Si se subió en esta edición y nunca se guardó, también se borra del disco;
	 * las ya guardadas en el vehículo no se borran del disco.
	 *
	 * @param nombre nombre del archivo de la foto
	 */
	public void quitarFoto(String nombre) {
		synchronized (fotos) {
			if (fotos.remove(nombre) && fotosNuevas.remove(nombre)) {
				try {
					borrarSilencioso(ConfiguracionFotos.resolver(nombre));
				} catch (IOException e) {
					// Archivo huérfano inofensivo.
				}
			}
		}
	}

	/**
	 * Mueve una foto al primer lugar para que sea la principal.
	 *
	 * @param nombre nombre del archivo de la foto
	 */
	public void hacerPrincipal(String nombre) {
		synchronized (fotos) {
			if (fotos.remove(nombre)) {
				fotos.add(0, nombre);
			}
		}
	}

	/**
	 * Borra del disco las fotos subidas que nunca se guardaron cuando se abandona el formulario.
	 */
	@PreDestroy
	public void limpiar() {
		if (guardado) {
			return;
		}
		synchronized (fotos) {
			for (String nombre : fotosNuevas) {
				try {
					borrarSilencioso(ConfiguracionFotos.resolver(nombre));
				} catch (IOException e) {
					// Sin más opciones.
				}
			}
			fotosNuevas.clear();
		}
	}

	// ------------------------------------------------------------------ guardar

	/**
	 * Guarda el vehículo (publica o modifica).
	 *
	 * @return la navegación al detalle si todo salió bien, o {@code null} para permanecer en el formulario
	 */
	public String guardar() {
		List<String> copiaFotos;
		synchronized (fotos) {
			copiaFotos = new ArrayList<>(fotos);
		}
		if (copiaFotos.isEmpty()) {
			Mensajes.error("Faltan fotos", "Sube al menos una foto del vehículo.");
			return null;
		}
		Cliente yo = seguridadBean.getCliente();
		if (yo == null) {
			Mensajes.error("Sesión", "Debes iniciar sesión para publicar.");
			return null;
		}
		Cliente dueno = new Cliente();
		dueno.setCodigo_usuario(yo.getCodigo_usuario());

		Vehiculo v = new Vehiculo();
		v.setCodigo_vehiculo(id);
		Modelo modelo = new Modelo();
		modelo.setCodigo_modelo(codigoModelo);
		v.setCodigo_modelo(modelo);
		v.setTipo_vehiculo(tipo);
		v.setCondicion(condicion);
		v.setYear(year == null ? 0 : year);
		v.setPrecio(precio);
		v.setKilometros(kilometros == null ? 0 : kilometros);
		v.setTipo_combustible(combustible);
		v.setTipo_trasmision(transmision);
		v.setCilindraje(cilindraje == null ? 0 : cilindraje);
		v.setNumero_puertas(numeroPuertas == null ? 0 : numeroPuertas);
		v.setColor(color == null ? null : color.trim());
		v.setPlaca(normalizarPlaca(placa));
		v.setCodigo_ciudad(ciudad);
		v.setDescripcion(descripcion == null ? null : descripcion.trim());
		List<Caracteristica> cs = new ArrayList<>();
		for (Long codigo : caracteristicasSeleccionadas) {
			Caracteristica c = new Caracteristica();
			c.setCodigo_caracteristica(codigo);
			cs.add(c);
		}
		v.setCaracteristicas(cs);
		v.setUrl_foto(copiaFotos);
		v.setCodigo_dueno(dueno);

		try {
			Vehiculo resultado;
			if (id == null) {
				v.setFecha_publicacion(new Date());
				resultado = vehiculoEJB.publicarVehiculo(v);
				Mensajes.info("Vehículo publicado", "Tu vehículo ya está publicado.");
			} else {
				resultado = vehiculoEJB.modificarVehiculo(v, yo.getEmail());
				Mensajes.info("Vehículo actualizado", "Los cambios se guardaron correctamente.");
			}
			guardado = true;
			Mensajes.conservarTrasRedireccion();
			return "/detalleVehiculo.xhtml?faces-redirect=true&id=" + resultado.getCodigo_vehiculo();
		} catch (ElementoRepetidoException | InformacionInvalidaException | ElementoNoEncontradoException e) {
			Mensajes.error("No se pudo guardar el vehículo", e.getMessage());
			return null;
		}
	}

	// ------------------------------------------------------------------ accesores

	/**
	 * Indica si se está editando un vehículo existente.
	 *
	 * @return {@code true} en modo edición
	 */
	public boolean isEdicion() {
		return id != null;
	}

	/**
	 * Año máximo permitido (año actual más uno).
	 *
	 * @return el año máximo
	 */
	public long getAnioMaximo() {
		return Calendar.getInstance().get(Calendar.YEAR) + 1L;
	}

	/**
	 * Copia de las fotos actuales, en orden.
	 *
	 * @return lista de nombres de archivo
	 */
	public List<String> getFotos() {
		synchronized (fotos) {
			return new ArrayList<>(fotos);
		}
	}

	/**
	 * Número máximo de fotos.
	 *
	 * @return el máximo
	 */
	public int getMaximoFotos() {
		return MAXIMO_FOTOS;
	}

	/**
	 * Tamaño máximo por foto en bytes.
	 *
	 * @return el máximo
	 */
	public long getTamanoMaximo() {
		return TAMANO_MAXIMO;
	}

	/**
	 * Obtiene el texto del parámetro {@code id}.
	 *
	 * @return el texto del id
	 */
	public String getIdTexto() {
		return idTexto;
	}

	/**
	 * Establece el texto del parámetro {@code id}.
	 *
	 * @param idTexto el texto del id
	 */
	public void setIdTexto(String idTexto) {
		this.idTexto = idTexto;
	}

	/**
	 * Lista de marcas.
	 *
	 * @return las marcas
	 */
	public List<String> getMarcas() {
		return marcas;
	}

	/**
	 * Modelos de la marca elegida.
	 *
	 * @return los modelos
	 */
	public List<Modelo> getModelos() {
		return modelos;
	}

	/**
	 * Características disponibles.
	 *
	 * @return las características
	 */
	public List<Caracteristica> getCaracteristicas() {
		return caracteristicas;
	}

	/**
	 * Tipos de vehículo.
	 *
	 * @return los tipos
	 */
	public List<TipoVehiculo> getTipos() {
		return tipos;
	}

	/**
	 * Tipos de combustible.
	 *
	 * @return los combustibles
	 */
	public List<TipoCombustible> getCombustibles() {
		return combustibles;
	}

	/**
	 * Tipos de transmisión.
	 *
	 * @return las transmisiones
	 */
	public List<TipoTrasmision> getTransmisiones() {
		return transmisiones;
	}

	/**
	 * Condiciones.
	 *
	 * @return las condiciones
	 */
	public List<Condicion> getCondiciones() {
		return condiciones;
	}

	/**
	 * Obtiene la marca.
	 *
	 * @return la marca
	 */
	public String getMarca() {
		return marca;
	}

	/**
	 * Establece la marca.
	 *
	 * @param marca la marca
	 */
	public void setMarca(String marca) {
		this.marca = marca;
	}

	/**
	 * Obtiene el código del modelo.
	 *
	 * @return el código del modelo
	 */
	public Long getCodigoModelo() {
		return codigoModelo;
	}

	/**
	 * Establece el código del modelo.
	 *
	 * @param codigoModelo el código del modelo
	 */
	public void setCodigoModelo(Long codigoModelo) {
		this.codigoModelo = codigoModelo;
	}

	/**
	 * Obtiene el tipo de vehículo.
	 *
	 * @return el tipo
	 */
	public TipoVehiculo getTipo() {
		return tipo;
	}

	/**
	 * Establece el tipo de vehículo.
	 *
	 * @param tipo el tipo
	 */
	public void setTipo(TipoVehiculo tipo) {
		this.tipo = tipo;
	}

	/**
	 * Obtiene la condición.
	 *
	 * @return la condición
	 */
	public Condicion getCondicion() {
		return condicion;
	}

	/**
	 * Establece la condición.
	 *
	 * @param condicion la condición
	 */
	public void setCondicion(Condicion condicion) {
		this.condicion = condicion;
	}

	/**
	 * Obtiene el año.
	 *
	 * @return el año
	 */
	public Integer getYear() {
		return year;
	}

	/**
	 * Establece el año.
	 *
	 * @param year el año
	 */
	public void setYear(Integer year) {
		this.year = year;
	}

	/**
	 * Obtiene el precio.
	 *
	 * @return el precio
	 */
	public Double getPrecio() {
		return precio;
	}

	/**
	 * Establece el precio.
	 *
	 * @param precio el precio
	 */
	public void setPrecio(Double precio) {
		this.precio = precio;
	}

	/**
	 * Obtiene los kilómetros.
	 *
	 * @return los kilómetros
	 */
	public Integer getKilometros() {
		return kilometros;
	}

	/**
	 * Establece los kilómetros.
	 *
	 * @param kilometros los kilómetros
	 */
	public void setKilometros(Integer kilometros) {
		this.kilometros = kilometros;
	}

	/**
	 * Obtiene el combustible.
	 *
	 * @return el combustible
	 */
	public TipoCombustible getCombustible() {
		return combustible;
	}

	/**
	 * Establece el combustible.
	 *
	 * @param combustible el combustible
	 */
	public void setCombustible(TipoCombustible combustible) {
		this.combustible = combustible;
	}

	/**
	 * Obtiene la transmisión.
	 *
	 * @return la transmisión
	 */
	public TipoTrasmision getTransmision() {
		return transmision;
	}

	/**
	 * Establece la transmisión.
	 *
	 * @param transmision la transmisión
	 */
	public void setTransmision(TipoTrasmision transmision) {
		this.transmision = transmision;
	}

	/**
	 * Obtiene el cilindraje.
	 *
	 * @return el cilindraje
	 */
	public Integer getCilindraje() {
		return cilindraje;
	}

	/**
	 * Establece el cilindraje.
	 *
	 * @param cilindraje el cilindraje
	 */
	public void setCilindraje(Integer cilindraje) {
		this.cilindraje = cilindraje;
	}

	/**
	 * Obtiene el número de puertas.
	 *
	 * @return el número de puertas
	 */
	public Integer getNumeroPuertas() {
		return numeroPuertas;
	}

	/**
	 * Establece el número de puertas.
	 *
	 * @param numeroPuertas el número de puertas
	 */
	public void setNumeroPuertas(Integer numeroPuertas) {
		this.numeroPuertas = numeroPuertas;
	}

	/**
	 * Obtiene el color.
	 *
	 * @return el color
	 */
	public String getColor() {
		return color;
	}

	/**
	 * Establece el color.
	 *
	 * @param color el color
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * Obtiene la placa.
	 *
	 * @return la placa
	 */
	public String getPlaca() {
		return placa;
	}

	/**
	 * Establece la placa (en mayúsculas y sin espacios).
	 *
	 * @param placa la placa escrita
	 */
	public void setPlaca(String placa) {
		this.placa = placa == null ? null : normalizarPlaca(placa);
	}

	/**
	 * Obtiene la ciudad.
	 *
	 * @return la ciudad
	 */
	public Ciudad getCiudad() {
		return ciudad;
	}

	/**
	 * Establece la ciudad.
	 *
	 * @param ciudad la ciudad
	 */
	public void setCiudad(Ciudad ciudad) {
		this.ciudad = ciudad;
	}

	/**
	 * Obtiene la descripción.
	 *
	 * @return la descripción
	 */
	public String getDescripcion() {
		return descripcion;
	}

	/**
	 * Establece la descripción.
	 *
	 * @param descripcion la descripción
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/**
	 * Códigos de las características marcadas.
	 *
	 * @return los códigos
	 */
	public List<Long> getCaracteristicasSeleccionadas() {
		return caracteristicasSeleccionadas;
	}

	/**
	 * Establece los códigos de las características marcadas.
	 *
	 * @param caracteristicasSeleccionadas los códigos
	 */
	public void setCaracteristicasSeleccionadas(List<Long> caracteristicasSeleccionadas) {
		this.caracteristicasSeleccionadas = caracteristicasSeleccionadas;
	}
}
