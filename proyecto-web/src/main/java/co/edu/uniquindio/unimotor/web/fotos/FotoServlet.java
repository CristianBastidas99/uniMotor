package co.edu.uniquindio.unimotor.web.fotos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sirve las fotos de los vehículos desde la carpeta configurada en
 * {@link ConfiguracionFotos}. Atiende {@code GET} y {@code HEAD} en
 * {@code /fotos/<nombre>}.
 */
@WebServlet(name = "FotoServlet", urlPatterns = "/fotos/*")
public class FotoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(FotoServlet.class.getName());

	/**
	 * Responde a una petición GET enviando el archivo con su contenido.
	 *
	 * @param req petición
	 * @param resp respuesta
	 * @throws ServletException si falla el servlet
	 * @throws IOException si falla la lectura o escritura
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		atender(req, resp, true);
	}

	/**
	 * Responde a una petición HEAD enviando solo las cabeceras.
	 *
	 * @param req petición
	 * @param resp respuesta
	 * @throws ServletException si falla el servlet
	 * @throws IOException si falla la lectura
	 */
	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		atender(req, resp, false);
	}

	/**
	 * Lógica común de GET y HEAD.
	 *
	 * @param req petición
	 * @param resp respuesta
	 * @param conCuerpo si se debe escribir el contenido del archivo
	 * @throws IOException si falla la lectura o escritura
	 */
	private void atender(HttpServletRequest req, HttpServletResponse resp, boolean conCuerpo) throws IOException {
		String info = req.getPathInfo();
		if (info == null || info.length() < 2) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		Path archivo = ConfiguracionFotos.resolver(info.substring(1));
		if (archivo == null || !Files.isRegularFile(archivo)) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		// Las fechas HTTP tienen precisión de segundos
		long modificado = (Files.getLastModifiedTime(archivo).toMillis() / 1000) * 1000;
		long desde = -1;
		try {
			desde = req.getDateHeader("If-Modified-Since");
		} catch (IllegalArgumentException e) {
			LOG.log(Level.FINE, "If-Modified-Since inválido", e);
		}
		if (desde != -1 && modificado <= desde) {
			resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		String tipo = getServletContext().getMimeType(archivo.getFileName().toString());
		resp.setContentType(tipo != null ? tipo : "image/jpeg");
		resp.setHeader("Cache-Control", "public, max-age=86400");
		resp.setDateHeader("Last-Modified", modificado);
		long longitud = Files.size(archivo);
		resp.setContentLengthLong(longitud);
		if (!conCuerpo) {
			return;
		}
		byte[] buffer = new byte[16384];
		try (InputStream entrada = Files.newInputStream(archivo); OutputStream salida = resp.getOutputStream()) {
			int leidos;
			while ((leidos = entrada.read(buffer)) != -1) {
				salida.write(buffer, 0, leidos);
			}
		}
	}
}
