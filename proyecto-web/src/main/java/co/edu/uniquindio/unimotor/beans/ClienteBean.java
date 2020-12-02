package co.edu.uniquindio.unimotor.beans;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class ClienteBean {

	private String email;

	private String password;

	private String nombre_completo;

	private String direccion;

	private Map<String, String> telefono;

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the nombre_completo
	 */
	public String getNombre_completo() {
		return nombre_completo;
	}

	/**
	 * @param nombre_completo the nombre_completo to set
	 */
	public void setNombre_completo(String nombre_completo) {
		this.nombre_completo = nombre_completo;
	}

	/**
	 * @return the direccion
	 */
	public String getDireccion() {
		return direccion;
	}

	/**
	 * @param direccion the direccion to set
	 */
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	/**
	 * @return the telefono
	 */
	public Map<String, String> getTelefono() {
		return telefono;
	}

	/**
	 * @param telefono the telefono to set
	 */
	public void setTelefono(Map<String, String> telefono) {
		this.telefono = telefono;
	}

}
