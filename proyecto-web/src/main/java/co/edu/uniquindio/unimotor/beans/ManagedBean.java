package co.edu.uniquindio.unimotor.beans;

import javax.ejb.EJB;

import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.ejb.ClienteEJB;

public class ManagedBean {

	@EJB
	private ClienteEJB clienteEJB;
	
	public void registarCliente(Cliente cliente) {
		
		
	}

}
