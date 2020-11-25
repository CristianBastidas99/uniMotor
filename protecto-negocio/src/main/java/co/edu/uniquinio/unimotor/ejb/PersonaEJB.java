package co.edu.uniquinio.unimotor.ejb;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Session Bean implementation class PersonaEJB
 */
@Stateless
@LocalBean
public class PersonaEJB implements PersonaEJBRemote {

	@PersistenceContext
	private EntityManager entityManager;
	
    /**
     * Default constructor. 
     */
    public PersonaEJB() {
        // TODO Auto-generated constructor stub
    }

}
