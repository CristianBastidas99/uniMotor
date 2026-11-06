package co.edu.uniquindio.unimotor.beans;

import javax.ejb.EJB;

import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.ejb.UnimotorEJB;

public class ManagedBean {

	@EJB
	private UnimotorEJB unimotorEJB;
	
	public void registarCliente(Cliente cliente) {
		
		
	}

}
