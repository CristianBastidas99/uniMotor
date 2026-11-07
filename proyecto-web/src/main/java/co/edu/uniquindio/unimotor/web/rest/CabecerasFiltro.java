package co.edu.uniquindio.unimotor.web.rest;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

/**
 * Añade cabeceras CORS de solo lectura a las respuestas de la API y fuerza
 * {@code charset=UTF-8} en las respuestas JSON que no lo indiquen.
 */
@Provider
public class CabecerasFiltro implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext peticion, ContainerResponseContext respuesta) throws IOException {
		respuesta.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
		respuesta.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, OPTIONS");
		MediaType tipo = respuesta.getMediaType();
		if (tipo != null && "json".equalsIgnoreCase(tipo.getSubtype())
				&& !tipo.getParameters().containsKey(MediaType.CHARSET_PARAMETER)) {
			respuesta.getHeaders().putSingle("Content-Type", tipo.withCharset("UTF-8").toString());
		}
	}
}
