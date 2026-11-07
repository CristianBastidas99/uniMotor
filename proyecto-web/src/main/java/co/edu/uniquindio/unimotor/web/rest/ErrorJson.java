package co.edu.uniquindio.unimotor.web.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import co.edu.uniquindio.unimotor.web.rest.dto.ErrorDTO;

/** Utilidad interna para construir respuestas de error JSON. */
final class ErrorJson {

	private ErrorJson() {
	}

	static Response respuesta(int estado, String error, String mensaje) {
		return Response.status(estado).type(MediaType.APPLICATION_JSON + ";charset=UTF-8")
				.entity(new ErrorDTO(estado, error, mensaje)).build();
	}
}
