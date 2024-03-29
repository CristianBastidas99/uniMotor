package co.edu.uniquindio.unimotor.beans;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class EjemploBean {

	private String atributo1 = "Hola";
	private String atributo2 = "Mundo";

	public void cambiar() {
		String aux = atributo1;
		atributo1 = atributo2;
		atributo2 = aux;
	}

	/**
	 * @return the atributo1
	 */
	public String getAtributo1() {
		return atributo1;
	}

	/**
	 * @param atributo1 the atributo1 to set
	 */
	public void setAtributo1(String atributo1) {
		this.atributo1 = atributo1;
	}

	/**
	 * @return the atributo2
	 */
	public String getAtributo2() {
		return atributo2;
	}

	/**
	 * @param atributo2 the atributo2 to set
	 */
	public void setAtributo2(String atributo2) {
		this.atributo2 = atributo2;
	}

}
