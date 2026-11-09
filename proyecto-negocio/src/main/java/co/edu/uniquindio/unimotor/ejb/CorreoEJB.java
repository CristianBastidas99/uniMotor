package co.edu.uniquindio.unimotor.ejb;

import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import co.edu.uniquindio.unimotor.excepciones.EnvioCorreoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * EJB encargado de enviar correos electrónicos mediante el recurso JavaMail {@value #RECURSO_CORREO}.
 * Si el recurso no existe, funciona en modo simulado y solo registra el correo en el log.
 * <p>
 * El envío no es transaccional ({@code NOT_SUPPORTED}): así la {@link EnvioCorreoException} no marca para rollback
 * la transacción de quien lo llama; quien quiera deshacer sus cambios, como {@code recuperarPassword}, deja que la
 * excepción salga de su propio método.
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CorreoEJB {

	/** Nombre JNDI del recurso de correo configurado en el servidor. */
	public static final String RECURSO_CORREO = "mail/unimotor";

	private static final Logger LOGGER = Logger.getLogger(CorreoEJB.class.getName());

	/**
	 * Crea el EJB de correo.
	 */
	public CorreoEJB() {
		super();
	}

	/**
	 * Envía un correo de texto plano.
	 *
	 * @param asunto asunto del correo
	 * @param mensaje cuerpo del correo en texto plano
	 * @param destinatario dirección de correo del destinatario
	 * @return {@code true} si el correo se envió realmente, {@code false} si se simuló por no existir el recurso
	 * @throws InformacionInvalidaException si algún argumento es nulo o vacío o el destinatario no es una dirección válida
	 * @throws EnvioCorreoException si ocurre un error al enviar el correo
	 */
	public boolean enviarEmail(String asunto, String mensaje, String destinatario)
			throws InformacionInvalidaException, EnvioCorreoException {
		if (asunto == null || asunto.trim().isEmpty()) {
			throw new InformacionInvalidaException("El asunto del correo es obligatorio.");
		}
		if (mensaje == null || mensaje.trim().isEmpty()) {
			throw new InformacionInvalidaException("El mensaje del correo es obligatorio.");
		}
		if (destinatario == null || destinatario.trim().isEmpty()) {
			throw new InformacionInvalidaException("El destinatario del correo es obligatorio.");
		}
		InternetAddress direccion;
		try {
			direccion = new InternetAddress(destinatario.trim(), true);
		} catch (AddressException e) {
			throw new InformacionInvalidaException(
					"El destinatario no es una dirección de correo válida: " + destinatario);
		}

		Session sesion = buscarSesion();
		if (sesion == null) {
			LOGGER.info("[Correo simulado] para=" + destinatario + ", asunto=" + asunto + "\n" + mensaje);
			return false;
		}

		try {
			MimeMessage correo = new MimeMessage(sesion);
			correo.setFrom();
			correo.setRecipient(Message.RecipientType.TO, direccion);
			correo.setSubject(asunto, "UTF-8");
			correo.setText(mensaje, "UTF-8");
			correo.setSentDate(new Date());
			Transport.send(correo);
			return true;
		} catch (MessagingException e) {
			throw new EnvioCorreoException("No se pudo enviar el correo a " + destinatario, e);
		}
	}

	/**
	 * Busca la sesión de correo en JNDI probando los nombres de cada servidor: Payara/GlassFish la publica como
	 * {@value #RECURSO_CORREO} y TomEE como {@code openejb:Resource/mail/unimotor}.
	 *
	 * @return la sesión de correo, o {@code null} si el recurso no existe en ningún nombre
	 */
	private Session buscarSesion() {
		String[] nombres = { RECURSO_CORREO, "openejb:Resource/" + RECURSO_CORREO };
		for (String nombre : nombres) {
			try {
				Object recurso = new InitialContext().lookup(nombre);
				if (recurso instanceof Session) {
					return (Session) recurso;
				}
			} catch (NamingException e) {
				// se prueba el siguiente nombre
			}
		}
		return null;
	}

}
