package co.edu.uniquinio.unimotor.ejb;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import co.edu.uniquindio.empresa.Administrador;

/**
 * Session Bean implementation class SetupEJB
 */
@Singleton
@LocalBean
@Startup
public class SetupEJB {

	@PersistenceContext
	private EntityManager entityManager;
	
    /**
     * Default constructor. 
     */
    public SetupEJB() {
        // TODO Auto-generated constructor stub
    }
    
    @PostConstruct
    public void config() {
boolean bandera = isAdministrador();
		
		if(!bandera) {
			
			Administrador administrador = new Administrador();
			
			administrador.setCodigo_usuario((long) 1);
			administrador.setEmail("cristia@proyecto.com");
			administrador.setNombre_administrador("Cristian Bastidas");
			administrador.setPassword("123");
			
			entityManager.persist(administrador);
		}

	}

	public boolean isAdministrador() {

		TypedQuery<Administrador> query = entityManager.createNamedQuery(Administrador.GETALL, Administrador.class);

		if (query.getResultList().isEmpty()) {

			return false;
		}

		return true;
	}


}
