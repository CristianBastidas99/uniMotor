package co.edu.uniquindio.unimotor.web.beans;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.application.Resource;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import co.edu.uniquindio.unimotor.ejb.CalificacionEJB;
import co.edu.uniquindio.unimotor.ejb.VehiculoEJB;
import co.edu.uniquindio.unimotor.entidades.Caracteristica;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;

/**
 * Bean de la página de detalle de un vehículo. Carga el vehículo una sola vez (suma una visita)
 * y expone los datos ya preparados para la vista: fotos, características, teléfonos del vendedor,
 * calificación y enlaces para compartir.
 */
@Named
@ViewScoped
public class DetalleVehiculoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private VehiculoEJB vehiculoEJB;

	@EJB
	private CalificacionEJB calificacionEJB;

	@Inject
	private SeguridadBean seguridadBean;

	@Inject
	private FormatoBean formatoBean;

	/** Código del vehículo recibido por el parámetro {@code id}. */
	private Long id;

	private Vehiculo vehiculo;
	private List<String> fotos = new ArrayList<>();
	private List<String> caracteristicas = new ArrayList<>();
	private List<Map.Entry<String, String>> telefonos = new ArrayList<>();
	private Double promedioVendedor;
	private int numeroCalificaciones;

	/** Origen (esquema, host y puerto), URL base (con contexto) y ruta del recurso sin foto. */
	private String origen = "";
	private String urlBase = "";
	private String rutaSinFoto = "";

	/**
	 * Carga el vehículo y suma una visita. Se invoca solo en la carga inicial (no en postbacks).
	 * Si el código falta, es inválido o no existe, responde con un error 404.
	 */
	public void inicializar() {
		FacesContext fc = FacesContext.getCurrentInstance();
		try {
			if (id == null) {
				throw new ElementoNoEncontradoException("Código de vehículo no indicado.");
			}
			vehiculo = vehiculoEJB.registrarVisita(id);
		} catch (ElementoNoEncontradoException e) {
			vehiculo = null;
			enviarNoEncontrado(fc);
			return;
		}
		calcularUrlBase(fc);
		prepararDatos();
	}

	private void enviarNoEncontrado(FacesContext fc) {
		try {
			fc.getExternalContext().responseSendError(404, "Vehículo no encontrado");
		} catch (IOException e) {
			// Sin más opciones: la respuesta ya no se puede modificar.
		}
		fc.responseComplete();
	}

	private void calcularUrlBase(FacesContext fc) {
		ExternalContext ec = fc.getExternalContext();
		Object req = ec.getRequest();
		if (req instanceof HttpServletRequest) {
			HttpServletRequest r = (HttpServletRequest) req;
			StringBuilder sb = new StringBuilder(r.getScheme()).append("://").append(r.getServerName());
			int puerto = r.getServerPort();
			boolean estandar = ("http".equalsIgnoreCase(r.getScheme()) && puerto == 80)
					|| ("https".equalsIgnoreCase(r.getScheme()) && puerto == 443);
			if (!estandar && puerto > 0) {
				sb.append(':').append(puerto);
			}
			origen = sb.toString();
			urlBase = origen + r.getContextPath();
		}
		Resource sinFoto = fc.getApplication().getResourceHandler().createResource("sin-foto.svg", "img");
		rutaSinFoto = sinFoto == null ? "" : sinFoto.getRequestPath();
	}

	/** Toca las relaciones perezosas y copia los datos a estructuras simples. */
	private void prepararDatos() {
		fotos = new ArrayList<>();
		if (vehiculo.getUrl_foto() != null) {
			for (String f : vehiculo.getUrl_foto()) {
				if (f != null && !f.trim().isEmpty()) {
					fotos.add(f.trim());
				}
			}
		}
		caracteristicas = new ArrayList<>();
		if (vehiculo.getCaracteristicas() != null) {
			for (Caracteristica c : vehiculo.getCaracteristicas()) {
				if (c != null && c.getNombre() != null) {
					caracteristicas.add(c.getNombre());
				}
			}
			Collections.sort(caracteristicas, String.CASE_INSENSITIVE_ORDER);
		}
		telefonos = new ArrayList<>();
		promedioVendedor = null;
		numeroCalificaciones = 0;
		Cliente dueno = vehiculo.getCodigo_dueno();
		if (dueno != null) {
			Map<String, String> tel = dueno.getTelefono();
			if (tel != null) {
				for (Map.Entry<String, String> e : tel.entrySet()) {
					if (e.getValue() != null && !e.getValue().trim().isEmpty()) {
						telefonos.add(new SimpleEntry<String, String>(e.getKey() == null ? "Teléfono" : e.getKey(),
								e.getValue().trim()));
					}
				}
			}
			if (dueno.getEmail() != null) {
				promedioVendedor = calificacionEJB.promedioVendedor(dueno.getEmail());
				numeroCalificaciones = calificacionEJB.listarCalificacionesVendedor(dueno.getEmail()).size();
			}
		}
	}

	/**
	 * Código del vehículo.
	 *
	 * @return el código o {@code null}
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Establece el código del vehículo (lo llena {@code f:viewParam}).
	 *
	 * @param id código del vehículo
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Código del vehículo como texto, tal como llega en la dirección.
	 *
	 * @return el código en texto, o {@code null}
	 */
	public String getIdTexto() {
		return id == null ? null : id.toString();
	}

	/**
	 * Recibe el parámetro {@code id} de la dirección sin conversión de JSF: si no es un número
	 * válido queda {@code null} y {@link #inicializar()} responde 404. (Con un {@code Long} en el
	 * {@code f:viewParam}, un valor como {@code abc} fallaba la conversión, JSF omitía el
	 * {@code f:viewAction} y la página respondía 200 sin vehículo.)
	 *
	 * @param texto valor del parámetro
	 */
	public void setIdTexto(String texto) {
		try {
			id = texto == null || texto.trim().isEmpty() ? null : Long.valueOf(texto.trim());
		} catch (NumberFormatException e) {
			id = null;
		}
	}

	/**
	 * Vehículo cargado.
	 *
	 * @return el vehículo, o {@code null} si no se encontró
	 */
	public Vehiculo getVehiculo() {
		return vehiculo;
	}

	/**
	 * Indica si el cliente en sesión es el dueño del vehículo (compara correos sin distinguir mayúsculas).
	 *
	 * @return {@code true} si hay sesión y el cliente es el dueño
	 */
	public boolean isPropio() {
		if (vehiculo == null || vehiculo.getCodigo_dueno() == null || !seguridadBean.isAutenticado()
				|| seguridadBean.getCliente() == null) {
			return false;
		}
		String a = vehiculo.getCodigo_dueno().getEmail();
		String b = seguridadBean.getCliente().getEmail();
		return a != null && a.equalsIgnoreCase(b);
	}

	/**
	 * Indica si el vehículo está disponible (activo).
	 *
	 * @return {@code true} si el vehículo existe y está activo
	 */
	public boolean isDisponible() {
		return vehiculo != null && vehiculo.isActivo();
	}

	/**
	 * Nombres de archivo de las fotos.
	 *
	 * @return lista de fotos, vacía si no hay
	 */
	public List<String> getFotos() {
		return fotos;
	}

	/**
	 * Nombres de las características, ordenados alfabéticamente.
	 *
	 * @return lista de características, vacía si no hay
	 */
	public List<String> getCaracteristicas() {
		return caracteristicas;
	}

	/**
	 * Teléfonos del vendedor como pares tipo-número.
	 *
	 * @return lista de pares, vacía si no hay
	 */
	public List<Map.Entry<String, String>> getTelefonos() {
		return telefonos;
	}

	/**
	 * Calificación promedio del vendedor.
	 *
	 * @return promedio de 1 a 5, o {@code null} si no tiene calificaciones
	 */
	public Double getPromedioVendedor() {
		return promedioVendedor;
	}

	/**
	 * Número de calificaciones recibidas por el vendedor.
	 *
	 * @return cantidad de calificaciones
	 */
	public int getNumeroCalificaciones() {
		return numeroCalificaciones;
	}

	/**
	 * Promedio del vendedor con coma decimal, por ejemplo "4,3".
	 *
	 * @return texto del promedio, o cadena vacía si no hay
	 */
	public String getPromedioTexto() {
		return promedioVendedor == null ? ""
				: String.format(Locale.ROOT, "%.1f", promedioVendedor).replace('.', ',');
	}

	/**
	 * Estrellas para mostrar el promedio: 2 llena, 1 media, 0 vacía (siempre cinco elementos).
	 *
	 * @return lista de cinco valores
	 */
	public List<Integer> getEstrellas() {
		List<Integer> r = new ArrayList<>();
		double p = promedioVendedor == null ? 0 : promedioVendedor;
		for (int i = 1; i <= 5; i++) {
			if (p >= i - 0.25) {
				r.add(2);
			} else if (p >= i - 0.75) {
				r.add(1);
			} else {
				r.add(0);
			}
		}
		return r;
	}

	/**
	 * Solo los dígitos de un número de teléfono, para los enlaces {@code tel:} y de WhatsApp.
	 *
	 * @param numero número tal como se guardó
	 * @return el número con solo dígitos
	 */
	public String soloDigitos(String numero) {
		return numero == null ? "" : numero.replaceAll("\\D", "");
	}

	/**
	 * URL pública absoluta de esta página.
	 *
	 * @return esquema, host, puerto, contexto y la ruta de detalle con el código del vehículo
	 */
	public String getUrlAbsoluta() {
		return urlBase + "/detalleVehiculo.xhtml?id=" + (vehiculo == null ? "" : vehiculo.getCodigo_vehiculo());
	}

	/**
	 * URL absoluta de la primera foto, o de la imagen "sin foto".
	 *
	 * @return URL absoluta de la imagen
	 */
	public String getUrlFotoAbsoluta() {
		if (!fotos.isEmpty()) {
			return urlBase + "/fotos/" + fotos.get(0);
		}
		return origen + rutaSinFoto;
	}

	/**
	 * Enlace para compartir en Facebook.
	 *
	 * @return URL del compartidor de Facebook
	 */
	public String getUrlFacebook() {
		return "https://www.facebook.com/sharer/sharer.php?u=" + codificar(getUrlAbsoluta());
	}

	/**
	 * Enlace para compartir en Twitter.
	 *
	 * @return URL de publicación de Twitter con el texto del anuncio
	 */
	public String getUrlTwitter() {
		String texto = formatoBean.titulo(vehiculo) + " por "
				+ formatoBean.precio(vehiculo == null ? null : vehiculo.getPrecio()) + " en uniMotor";
		return "https://twitter.com/intent/tweet?url=" + codificar(getUrlAbsoluta()) + "&text=" + codificar(texto);
	}

	private static String codificar(String texto) {
		try {
			return URLEncoder.encode(texto, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			return texto;
		}
	}
}
