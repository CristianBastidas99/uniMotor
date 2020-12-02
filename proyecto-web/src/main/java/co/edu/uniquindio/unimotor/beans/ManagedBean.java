package co.edu.uniquindio.unimotor.beans;

import javax.ejb.EJB;

import co.edu.uniquindio.empresa.Cliente;
import co.edu.uniquinio.unimotor.ejb.UnimotorEJB;

public class ManagedBean {

	@EJB
	private UnimotorEJB unimotorEJB;
	
	public void registarCliente(Cliente cliente) {
		
		
	}

}
