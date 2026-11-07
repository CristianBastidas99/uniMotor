package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import co.edu.uniquindio.unimotor.ejb.VehiculoEJB;
import co.edu.uniquindio.unimotor.ejb.VentaEJB;
import co.edu.uniquindio.unimotor.entidades.Pregunta;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.entidades.Venta;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;
import co.edu.uniquindio.unimotor.web.util.Mensajes;

/**
 * Bean de la página "Mis vehículos": lista, filtra y gestiona los vehículos publicados por el cliente en sesión.
 */
@Named
@ViewScoped
public class MisVehiculosBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Estado de un vehículo activo y sin venta. */
	public static final String ACTIVO = "Activo";
	/** Estado de un vehículo con venta registrada. */
	public static final String VENDIDO = "Vendido";
	/** Estado de un vehículo desactivado sin venta. */
	public static final String INACTIVO = "Inactivo";

	/**
	 * Fila de la tabla: el vehículo con sus datos derivados, calculados una sola vez al cargar la lista.
	 */
	public static class Fila implements Serializable {

		private static final long serialVersionUID = 1L;

		private final Vehiculo vehiculo;
		private final int preguntas;
		private final int sinResponder;
		private final String estado;
		private final Date publicado;
		private final int visitas;
		private final double precio;

		Fila(Vehiculo v) {
			this.vehiculo = v;
			int total = 0;
			int pendientes = 0;
			if (v.getPreguntas() != null) {
				for (Pregunta p : v.getPreguntas()) {
					total++;
					if (p.getRespuesta() == null || p.getRespuesta().trim().isEmpty()) {
						pendientes++;
					}
				}
			}
			this.preguntas = total;
			this.sinResponder = pendientes;
			this.estado = v.getCodigo_venta() != null ? VENDIDO : (v.isActivo() ? ACTIVO : INACTIVO);
			this.publicado = v.getFecha_publicacion();
			this.visitas = v.getNumero_busquedas();
			this.precio = v.getPrecio() == null ? 0 : v.getPrecio();
		}

		/**
		 * Vehículo de la fila.
		 *
		 * @return el vehículo
		 */
		public Vehiculo getVehiculo() {
			return vehiculo;
		}

		/**
		 * Número total de preguntas.
		 *
		 * @return total de preguntas
		 */
		public int getPreguntas() {
			return preguntas;
		}

		/**
		 * Número de preguntas pendientes de respuesta.
		 *
		 * @return preguntas sin responder
		 */
		public int getSinResponder() {
			return sinResponder;
		}

		/**
		 * Estado visible: Activo, Vendido o Inactivo.
		 *
		 * @return el estado
		 */
		public String getEstado() {
			return estado;
		}

		/**
		 * Fecha de publicación.
		 *
		 * @return fecha de publicación
		 */
		public Date getPublicado() {
			return publicado;
		}

		/**
		 * Número de visitas (búsquedas).
		 *
		 * @return visitas
		 */
		public int getVisitas() {
			return visitas;
		}

		/**
		 * Precio del vehículo.
		 *
		 * @return precio
		 */
		public double getPrecio() {
			return precio;
		}

		/**
		 * Indica si el estado es Activo.
		 *
		 * @return {@code true} si está activo y sin venta
		 */
		public boolean isActivo() {
			return ACTIVO.equals(estado);
		}

		/**
		 * Indica si el vehículo fue vendido.
		 *
		 * @return {@code true} si tiene venta
		 */
		public boolean isVendido() {
			return VENDIDO.equals(estado);
		}

		/**
		 * Clase CSS de la insignia de estado.
		 *
		 * @return nombre de clase en minúsculas
		 */
		public String getClaseEstado() {
			return estado.toLowerCase();
		}
	}

	@EJB
	private VehiculoEJB vehiculoEJB;

	@EJB
	private VentaEJB ventaEJB;

	@Inject
	private SeguridadBean seguridadBean;

	private List<Fila> filas = new ArrayList<Fila>();

	private String filtro = "TODOS";

	private double ingresos;

	private Fila seleccionada;

	private String emailComprador;

	/** Carga inicial de la lista. */
	@PostConstruct
	public void init() {
		recargar();
	}

	private String email() {
		return seguridadBean.getCliente().getEmail();
	}

	private void recargar() {
		List<Fila> nuevas = new ArrayList<Fila>();
		for (Vehiculo v : vehiculoEJB.listarVehiculosDeCliente(email())) {
			nuevas.add(new Fila(v));
		}
		filas = nuevas;
		double suma = 0;
		for (Venta venta : ventaEJB.listarVentas(email())) {
			if (venta.getCodigo_vehiculo() != null && venta.getCodigo_vehiculo().getPrecio() != null) {
				suma += venta.getCodigo_vehiculo().getPrecio();
			}
		}
		ingresos = suma;
	}

	/**
	 * Filas que cumplen el filtro actual.
	 *
	 * @return lista filtrada
	 */
	public List<Fila> getVisibles() {
		if ("TODOS".equals(filtro)) {
			return filas;
		}
		String estado = "ACTIVOS".equals(filtro) ? ACTIVO : ("VENDIDOS".equals(filtro) ? VENDIDO : INACTIVO);
		List<Fila> res = new ArrayList<Fila>();
		for (Fila f : filas) {
			if (f.getEstado().equals(estado)) {
				res.add(f);
			}
		}
		return res;
	}

	/**
	 * Total de vehículos publicados.
	 *
	 * @return total
	 */
	public int getTotal() {
		return filas.size();
	}

	/**
	 * Cantidad de vehículos activos.
	 *
	 * @return activos
	 */
	public int getActivos() {
		return contar(ACTIVO);
	}

	/**
	 * Cantidad de vehículos vendidos.
	 *
	 * @return vendidos
	 */
	public int getVendidos() {
		return contar(VENDIDO);
	}

	/**
	 * Cantidad de vehículos inactivos.
	 *
	 * @return inactivos
	 */
	public int getInactivos() {
		return contar(INACTIVO);
	}

	/**
	 * Suma de los precios de los vehículos vendidos.
	 *
	 * @return ingresos por ventas
	 */
	public double getIngresos() {
		return ingresos;
	}

	private int contar(String estado) {
		int n = 0;
		for (Fila f : filas) {
			if (f.getEstado().equals(estado)) {
				n++;
			}
		}
		return n;
	}

	/**
	 * Prepara el diálogo de venta para una fila.
	 *
	 * @param fila fila elegida
	 */
	public void prepararVenta(Fila fila) {
		this.seleccionada = fila;
		this.emailComprador = null;
	}

	/** Registra la venta del vehículo seleccionado; devuelve el parámetro {@code exito} para cerrar el diálogo. */
	public void registrarVenta() {
		boolean exito = false;
		try {
			if (seleccionada == null) {
				Mensajes.error("No se pudo registrar la venta", "Selecciona un vehículo.");
			} else {
				ventaEJB.registrarVenta(seleccionada.getVehiculo().getCodigo_vehiculo(), email(),
						emailComprador == null ? null : emailComprador.trim());
				Mensajes.info("Venta registrada", "Venta registrada. El comprador recibirá un correo.");
				seleccionada = null;
				emailComprador = null;
				recargar();
				exito = true;
			}
		} catch (ElementoNoEncontradoException | InformacionInvalidaException e) {
			Mensajes.error("No se pudo registrar la venta", e.getMessage());
		}
		PrimeFaces.current().ajax().addCallbackParam("exito", exito);
	}

	/**
	 * Desactiva un vehículo.
	 *
	 * @param fila fila elegida
	 */
	public void desactivar(Fila fila) {
		try {
			vehiculoEJB.invalidarVehiculo(fila.getVehiculo().getCodigo_vehiculo(), email());
			Mensajes.info("Vehículo desactivado", "Ya no aparecerá en las búsquedas.");
		} catch (ElementoNoEncontradoException | InformacionInvalidaException e) {
			Mensajes.error("No se pudo desactivar", e.getMessage());
		}
		recargar();
	}

	/**
	 * Elimina un vehículo con sus preguntas y favoritos.
	 *
	 * @param fila fila elegida
	 */
	public void eliminar(Fila fila) {
		try {
			vehiculoEJB.eliminarVehiculo(fila.getVehiculo().getCodigo_vehiculo(), email());
			Mensajes.info("Vehículo eliminado", "La publicación fue eliminada.");
		} catch (ElementoNoEncontradoException | InformacionInvalidaException e) {
			Mensajes.error("No se pudo eliminar", e.getMessage());
		}
		recargar();
	}

	/**
	 * Filtro de estado actual.
	 *
	 * @return TODOS, ACTIVOS, VENDIDOS o INACTIVOS
	 */
	public String getFiltro() {
		return filtro;
	}

	/**
	 * Establece el filtro de estado.
	 *
	 * @param filtro TODOS, ACTIVOS, VENDIDOS o INACTIVOS
	 */
	public void setFiltro(String filtro) {
		this.filtro = filtro;
	}

	/**
	 * Fila elegida para vender.
	 *
	 * @return la fila o {@code null}
	 */
	public Fila getSeleccionada() {
		return seleccionada;
	}

	/**
	 * Email del comprador digitado en el diálogo.
	 *
	 * @return el email
	 */
	public String getEmailComprador() {
		return emailComprador;
	}

	/**
	 * Establece el email del comprador.
	 *
	 * @param emailComprador email del comprador
	 */
	public void setEmailComprador(String emailComprador) {
		this.emailComprador = emailComprador;
	}
}
