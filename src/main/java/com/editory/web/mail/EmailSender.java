package com.editory.web.mail;

import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
	
	private HashMap<String,String> contenido;
	
	
	
	
	public EmailSender(HashMap<String, String> contenido) {
		super();
		this.contenido = contenido;
	}
	
	
	public  void enviar() {
		/**
		   Outgoing Mail (SMTP) Server
		   requires TLS or SSL: smtp.gmail.com (use authentication)
		   Use Authentication: Yes
		   Port for TLS/STARTTLS: 587
		 */

			final String fromEmail = "editory.video@gmail.com"; //requires valid gmail id
			final String password = "editory2017"; // correct password for gmail id
			final String toEmail = "editory.video@gmail.com"; // can be any email id 
			
			System.out.println("TLSEmail Start");
			Properties props = new Properties();
			props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
			props.put("mail.smtp.port", "587"); //TLS Port
			props.put("mail.smtp.auth", "true"); //enable authentication
			props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
			
	                //create Authenticator object to pass in Session.getInstance argument
			Authenticator auth = new Authenticator() {
				//override the getPasswordAuthentication method
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(fromEmail, password);
				}
			};
			Session session = Session.getInstance(props, auth);
			
			StringBuilder mensaje = new StringBuilder();
			mensaje.append("Nuevo vídeo para editar con las siguientes características: \n\n");
			Set<String> claves = this.contenido.keySet();
			for(String clave: claves){
				mensaje.append(clave+":").append(this.contenido.get(clave)).append("\n"+"\n");
			}
			mensaje.append("Saludos");
			
			sendEmail(session, toEmail,"Nuevo vídeo a editar", mensaje.toString());
			
		}
	public static void sendEmail(Session session, String toEmail, String subject, String body){
		try
	    {
	      MimeMessage msg = new MimeMessage(session);
	      //set message headers
	      msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
	      msg.addHeader("format", "flowed");
	      msg.addHeader("Content-Transfer-Encoding", "8bit");

	      msg.setFrom(new InternetAddress("editory.video@gmail.com", "EditoryVideo"));

	      msg.setReplyTo(InternetAddress.parse("editory.video@gmail.com", true));

	      msg.setSubject(subject, "UTF-8");

	      msg.setText(body, "UTF-8");

	      msg.setSentDate(new Date());

	      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
	      System.out.println("Message is ready");
    	  Transport.send(msg);  

	      System.out.println("EMail Sent Successfully!!");
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	    }
	}


	public void enviarCliente() {
		final String fromEmail = "editory.video@gmail.com"; //requires valid gmail id
		final String password = "editory2017"; // correct password for gmail id
		final String toEmail = this.contenido.get("mail"); // can be any email id 
		
		System.out.println("TLSEmail Start");
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
		props.put("mail.smtp.port", "587"); //TLS Port
		props.put("mail.smtp.auth", "true"); //enable authentication
		props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
		
                //create Authenticator object to pass in Session.getInstance argument
		Authenticator auth = new Authenticator() {
			//override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, password);
			}
		};
		Session session = Session.getInstance(props, auth);
		
		StringBuilder mensaje = new StringBuilder();
		mensaje.append("Hola ");
		mensaje.append(this.contenido.get("nombre"));
		mensaje.append(",\n\nMuchas gracias por confiar en Editory. Pondremos todo nuestro trabajo, esfuerzo e ilusión en editar un vídeo inolvidable.\n\nEl precio final es de: ");
		mensaje.append(this.contenido.get("precio"));
		mensaje.append("€\n\nEl pago se deberá realizar antes de la entrega del vídeo. En el momento de la entrega, si no le gusta el resultado y no quiere descargar el vídeo, le devolveremos el 75% del importe.\n\nEl número de cuenta para realizar la transferencias es: ES79 2100 1732 4702 0026 7826 - La Caixa \n\nDe nuevo, muchas gracias.\n\nUn cordial saludo,\nEl equipo de Editory.");
		sendEmail(session, toEmail,"Nuevo pedido en Editory recibido correctamente", mensaje.toString());
	}

}
