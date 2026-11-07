package co.edu.uniquindio.unimotor.web.convertidores;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.enterprise.inject.spi.CDI;

import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.web.beans.CiudadesBean;

/**
 * Convertidor de {@link Ciudad} para los componentes de selección: usa el código como valor
 * de texto y recupera la ciudad desde la caché de {@link CiudadesBean}.
 * <p>
 * Mojarra no inyecta dependencias CDI en un convertidor registrado con {@code forClass}
 * (aunque lleve {@code managed = true}: el campo quedaba {@code null}), por eso el bean se
 * obtiene con {@link CDI#current()} en cada conversión.
 */
@FacesConverter(forClass = Ciudad.class)
public class CiudadConverter implements Converter<Ciudad> {

	/**
	 * Obtiene la caché de ciudades desde el contenedor CDI.
	 *
	 * @return el bean de ciudades
	 */
	private CiudadesBean ciudadesBean() {
		return CDI.current().select(CiudadesBean.class).get();
	}

	/**
	 * Convierte el código recibido en la ciudad correspondiente.
	 *
	 * @param context contexto de JSF
	 * @param component componente asociado
	 * @param value código de la ciudad en texto
	 * @return la ciudad, o {@code null} si el valor está vacío
	 * @throws ConverterException si el valor no es un código de ciudad válido
	 */
	@Override
	public Ciudad getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		Ciudad ciudad = null;
		try {
			ciudad = ciudadesBean().buscarPorCodigo(Long.valueOf(value.trim()));
		} catch (NumberFormatException e) {
			ciudad = null;
		}
		if (ciudad == null) {
			throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Selecciona una ciudad de la lista.", null));
		}
		return ciudad;
	}

	/**
	 * Convierte una ciudad en su código.
	 *
	 * @param context contexto de JSF
	 * @param component componente asociado
	 * @param value la ciudad
	 * @return el código en texto, o cadena vacía si es nula
	 */
	@Override
	public String getAsString(FacesContext context, UIComponent component, Ciudad value) {
		return value == null || value.getCodigo_ciudad() == null ? "" : value.getCodigo_ciudad().toString();
	}
}
