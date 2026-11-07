package co.edu.uniquindio.unimotor.web.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/** Traduce {@link InformacionInvalidaException} a una respuesta JSON 400. */
@Provider
public class InformacionInvalidaMapper implements ExceptionMapper<InformacionInvalidaException> {

	@Override
	public Response toResponse(InformacionInvalidaException e) {
		return ErrorJson.respuesta(400, "Solicitud inválida", e.getMessage());
	}
}
