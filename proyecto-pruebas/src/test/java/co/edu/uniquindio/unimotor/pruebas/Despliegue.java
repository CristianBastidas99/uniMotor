package co.edu.uniquindio.unimotor.pruebas;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import co.edu.uniquindio.unimotor.dto.EstadisticaDTO;
import co.edu.uniquindio.unimotor.entidades.Cliente;

/**
 * Despliegue comun para las pruebas Arquillian. Cada clase de prueba lo usa asi:
 * <pre>
 * &#64;Deployment
 * public static WebArchive crear() { return Despliegue.crear(); }
 * </pre>
 * El archivo {@code prueba.war} incluye las entidades y los DTO, el persistenceForTest.xml
 * (como META-INF/persistence.xml, unidad que usa el recurso {@code jdbc/prueba}) y un beans.xml vacio.
 */
public final class Despliegue {

	private Despliegue() {
	}

	/**
	 * Arma el WAR de prueba con ShrinkWrap para desplegarlo en el GlassFish embebido.
	 * 
	 * @return archivo {@code prueba.war} listo para {@code @Deployment}
	 */
	public static WebArchive crear() {
		return ShrinkWrap.create(WebArchive.class, "prueba.war")
				.addPackage(Cliente.class.getPackage())
				.addPackage(EstadisticaDTO.class.getPackage())
				.addAsResource("persistenceForTest.xml", "META-INF/persistence.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}
}
