package co.edu.uniquindio.unimotor.web.seguridad;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import co.edu.uniquindio.unimotor.web.beans.SeguridadBean;

/**
 * Filtro que protege las páginas de {@code /usuario/*}: solo permite el acceso
 * a clientes autenticados y redirige al resto a la página de inicio de sesión.
 */
@WebFilter("/usuario/*")
public class FiltroSeguridad implements Filter {

	@Inject
	private SeguridadBean seguridadBean;

	/**
	 * Deja pasar la petición si hay un cliente autenticado; si no, redirige al
	 * login conservando la ruta de origen. Para peticiones AJAX de JSF responde
	 * con una redirección parcial.
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		if (seguridadBean != null && seguridadBean.isAutenticado()) {
			res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			res.setHeader("Pragma", "no-cache");
			res.setDateHeader("Expires", 0);
			chain.doFilter(request, response);
			return;
		}

		String ruta = req.getRequestURI().substring(req.getContextPath().length());
		if (req.getQueryString() != null) {
			ruta += "?" + req.getQueryString();
		}
		String destino = req.getContextPath() + "/login.xhtml?origen=" + codificar(ruta);

		if ("partial/ajax".equals(req.getHeader("Faces-Request"))) {
			res.setContentType("text/xml");
			res.setCharacterEncoding("UTF-8");
			res.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><partial-response><redirect url=\""
					+ destino.replace("&", "&amp;") + "\"/></partial-response>");
		} else {
			res.sendRedirect(destino);
		}
	}

	private static String codificar(String texto) {
		try {
			return URLEncoder.encode(texto, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
}
