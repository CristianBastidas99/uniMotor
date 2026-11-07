package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import co.edu.uniquindio.unimotor.ejb.CalificacionEJB;
import co.edu.uniquindio.unimotor.ejb.VentaEJB;
import co.edu.uniquindio.unimotor.entidades.Calificacion;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Venta;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.ElementoRepetidoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;
import co.edu.uniquindio.unimotor.web.util.Mensajes;

/**
 * Bean de la página "Mis compras": lista las compras del cliente y permite
 * calificar al vendedor.
 */
@Named
@ViewScoped
public class ComprasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Fila de la lista de compras.
	 */
	public static class CompraFila implements Serializable {

		private static final long serialVersionUID = 1L;

		private final Venta venta;
		private final boolean puedeCalificar;
		private final Calificacion calificacion;

		/**
		 * Crea una fila.
		 *
		 * @param venta la compra
		 * @param puedeCalificar si el cliente aún puede calificar al vendedor
		 * @param calificacion la calificación ya hecha al vendedor, o {@code null}
		 */
		public CompraFila(Venta venta, boolean puedeCalificar, Calificacion calificacion) {
			this.venta = venta;
			this.puedeCalificar = puedeCalificar;
			this.calificacion = calificacion;
		}

		/**
		 * Obtiene la compra.
		 *
		 * @return la venta
		 */
		public Venta getVenta() {
			return venta;
		}

		/**
		 * Indica si aún se puede calificar al vendedor.
		 *
		 * @return {@code true} si puede calificar
		 */
		public boolean isPuedeCalificar() {
			return puedeCalificar;
		}

		/**
		 * Calificación ya registrada para este vendedor.
		 *
		 * @return la calificación o {@code null}
		 */
		public Calificacion getCalificacion() {
			return calificacion;
		}

		/**
		 * Vendedor de la compra.
		 *
		 * @return el dueño del vehículo vendido
		 */
		public Cliente getVendedor() {
			return venta.getCodigo_vehiculo() == null ? null : venta.getCodigo_vehiculo().getCodigo_dueno();
		}
	}

	@EJB
	private transient VentaEJB ventaEJB;

	@EJB
	private transient CalificacionEJB calificacionEJB;

	@Inject
	private SeguridadBean seguridadBean;

	private List<CompraFila> compras = new ArrayList<>();

	private CompraFila seleccionada;
	private int estrellas = 5;
	private String comentario;

	/**
	 * Carga las compras del cliente.
	 */
	@PostConstruct
	public void iniciar() {
		cargar();
	}

	/**
	 * Recarga la lista de compras con su estado de calificación.
	 */
	private void cargar() {
		compras = new ArrayList<>();
		Cliente yo = seguridadBean.getCliente();
		if (yo == null) {
			return;
		}
		Map<String, Calificacion> hechas = new HashMap<>();
		for (Venta v : ventaEJB.listarCompras(yo.getEmail())) {
			Cliente vendedor = v.getCodigo_vehiculo() == null ? null : v.getCodigo_vehiculo().getCodigo_dueno();
			boolean puede = false;
			Calificacion cal = null;
			if (vendedor != null) {
				String correo = vendedor.getEmail();
				puede = calificacionEJB.puedeCalificar(yo.getEmail(), correo);
				if (!puede) {
					if (!hechas.containsKey(correo)) {
						Calificacion encontrada = null;
						for (Calificacion c : calificacionEJB.listarCalificacionesVendedor(correo)) {
							if (c.getCalificador() != null && yo.getEmail().equals(c.getCalificador().getEmail())) {
								encontrada = c;
								break;
							}
						}
						hechas.put(correo, encontrada);
					}
					cal = hechas.get(correo);
				}
			}
			compras.add(new CompraFila(v, puede, cal));
		}
	}

	/**
	 * Prepara el diálogo para calificar al vendedor de una compra.
	 *
	 * @param fila la compra elegida
	 */
	public void seleccionar(CompraFila fila) {
		this.seleccionada = fila;
		this.estrellas = 5;
		this.comentario = null;
	}

	/**
	 * Envía la calificación del vendedor de la compra seleccionada.
	 */
	public void calificar() {
		boolean ok = false;
		try {
			if (seleccionada == null || seleccionada.getVendedor() == null) {
				Mensajes.error("No se pudo calificar", "Seleccione una compra.");
				return;
			}
			if (estrellas < 1 || estrellas > 5) {
				Mensajes.error("Calificación", "Elija entre 1 y 5 estrellas.");
				return;
			}
			calificacionEJB.calificarVendedor(seguridadBean.getCliente().getEmail(),
					seleccionada.getVendedor().getEmail(), estrellas, comentario);
			Mensajes.info("Calificación enviada", "Gracias por calificar al vendedor.");
			ok = true;
			cargar();
		} catch (ElementoRepetidoException e) {
			Mensajes.error("Ya calificaste", e.getMessage());
			ok = true;
			cargar();
		} catch (ElementoNoEncontradoException | InformacionInvalidaException e) {
			Mensajes.error("No se pudo calificar", e.getMessage());
		} finally {
			PrimeFaces.current().ajax().addCallbackParam("ok", ok);
		}
	}

	/**
	 * Compras del cliente.
	 *
	 * @return las filas de compras
	 */
	public List<CompraFila> getCompras() {
		return compras;
	}

	/**
	 * Compra seleccionada para calificar.
	 *
	 * @return la fila o {@code null}
	 */
	public CompraFila getSeleccionada() {
		return seleccionada;
	}

	/**
	 * Obtiene las estrellas elegidas.
	 *
	 * @return las estrellas
	 */
	public int getEstrellas() {
		return estrellas;
	}

	/**
	 * Establece las estrellas elegidas.
	 *
	 * @param estrellas las estrellas
	 */
	public void setEstrellas(int estrellas) {
		this.estrellas = estrellas;
	}

	/**
	 * Obtiene el comentario.
	 *
	 * @return el comentario
	 */
	public String getComentario() {
		return comentario;
	}

	/**
	 * Establece el comentario.
	 *
	 * @param comentario el comentario
	 */
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
}
