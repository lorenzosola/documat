package arcmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;

public class MailManagement {
	String mailServerIP;
	String mailServerPort;
	String mailServerUname;
	String mailServerPwd;
	String ccAddress;
	boolean smtpAuth;
	ResultSet rs;
	String fromAddress;
	String subject;
	String tmpStr;
	Transport tr = null;
	PreparedStatement ps;
	PreparedStatement ps1;
	DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALY);
	private Session s;

	public MailManagement(String mailServerIP, String mailServerPort, String mailServerUname, String mailServerPwd, boolean smtpAuth, String fromAddress, String ccAddress, String subject, Connection conn) {
		this.mailServerIP = mailServerIP;
		this.mailServerPort = mailServerPort;
		this.mailServerUname = mailServerUname;
		this.mailServerPwd = mailServerPwd;
		this.smtpAuth = smtpAuth;
		this.fromAddress = fromAddress;
		this.ccAddress = ccAddress;
		this.subject = subject;
		try {
			ps = conn.prepareStatement("select * from (attivita att join attivita_utenti au on (au.id_attivita = att.id)) join utenti u on (au.id_utente = u.id)");
			ps1 = conn.prepareStatement("update attivita set avviso_inviato = ? where id = ?");
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Ora imposto le proprietà che stabiliscono i timeout di connessione e di comunicazione per la sessione.
		Properties prop = new Properties();
		if(smtpAuth) {
			//solo se si deve autenticare
			prop.put("mail.smtp.auth", "true");
		}
		prop.put("mail.smtp.connectiontimeout", "300000");
		prop.put("mail.smtp.timeout", "300000");
		s = Session.getDefaultInstance(prop);
	}

	public void invia() {
		try {
			rs = ps.executeQuery();
		}
		catch (SQLException e1) {
			e1.printStackTrace();
		}

		//Ora apro il "trasporto" (strato di..).
		try {
			tr = s.getTransport("smtp");
			tr.connect(mailServerIP, Integer.parseInt(mailServerPort), mailServerUname, mailServerPwd); //IP, port, username, password.
		}
		catch (MessagingException ex) {
			ex.printStackTrace();
			if(tr.isConnected()) try {
				tr.close();
			}
			catch (MessagingException e) {
				e.printStackTrace();
			}
		}	
		//Ora creo i messaggi e li invio tramite il trasporto
		Address addrto = null, addrfrom = null, ccAddr;
		try {
			while(rs.next()) {
				//Controllo se data "ultima" + "periodo" è stata superata e se la mail non è ancora stata inviata (campo "avviso_inviato" = false).
				long prossima = rs.getTimestamp("ultima").getTime() + rs.getInt("periodo")*(24*60*60*1000);
				if(((tmpStr = rs.getString("avviso_inviato")) != null && tmpStr.equals("1")) ||
						System.currentTimeMillis() < prossima) continue;
				
				if((tmpStr = rs.getString("email")) == null || tmpStr.length() < 7) continue;
				
				MimeMessage msg = new MimeMessage(s);
				if(tmpStr.indexOf("@") > 3 && tmpStr.indexOf(".") > 0) {//Controllo l'attendibilità dell'indirizzo in modo euristico (approssimativo).
					addrto = new InternetAddress(tmpStr);
					msg.addRecipient(Message.RecipientType.TO, addrto);
				}
				else {
					System.err.println("Indirizzo e-mail destinazione utente " + rs.getString("username") + "(da tupla tabella \"utenti\"): " + tmpStr + " non valido");
					continue;
				}
				if(fromAddress != null && fromAddress.length() > 7 && fromAddress.indexOf("@") > 3 && fromAddress.indexOf(".") > 0) {//Controllo l'attendibilità dell'indirizzo in modo euristico (approssimativo).
					addrfrom = new InternetAddress(fromAddress);
					msg.setFrom(addrfrom);
				}
				else {
					System.err.println("Indirizzo e-mail origine (da file \"properties.prop\"): " + fromAddress + " non valido");
				}
				if(ccAddress != null && ccAddress.length() > 7 && ccAddress.indexOf("@") > 3 && ccAddress.indexOf(".") > 0) {//Controllo l'attendibilità dell'indirizzo in modo euristico (approssimativo).
					ccAddr = new InternetAddress(ccAddress);
					msg.addRecipient(Message.RecipientType.CC, ccAddr);
				}
				else {
					System.err.println("Indirizzo e-mail copia in CC (da file \"properties.prop\"): " + ccAddress + " non valido");
				}
				msg.setSubject(subject);
				//msg.setDataHandler(new DataHandler(....));
				//msg.setHeader("Content-Type", "application/x-pkcs7-mime;\n\tsmime-type=signed-data;\n\tname=\"" + "smime.p7m" + "\"");
				//msg.setHeader("Content-Disposition", "attachment;\n\tfilename=\"" + "smime.p7m" + "\"");
				//msg.setHeader("Content-Transfer-Encoding", "base64");
				String content;
				content = rs.getString("descrizione");
				DataHandler dh = new DataHandler(content, "text/plain; charset=ISO-8859-1");//Attenzione al charset: se ci sono problemi nel visualizzare i caratteri nelle e-mail da parte del client è proprio per questo parametro (problemi avuti...e risolti).
				msg.setDataHandler(dh);
				msg.saveChanges();
				tr.sendMessage(msg, msg.getAllRecipients());
				ps1.setString(1, "1");
				ps1.setInt(2, rs.getInt("att.id"));
				ps1.executeUpdate();
			}
			tr.close();
		}
		catch (SQLException e) {
			System.err.println("Problema invio e-mail ad indirizzo: " + addrto);
			System.err.println("Da indirizzo: " + addrfrom);
			System.err.println("Tramite server: " + mailServerIP);
			System.err.println("Su porta: " + mailServerPort);
			System.err.println("Con User Name: " + mailServerUname);
			System.err.println("Password: " + mailServerPwd);
			System.err.println("Soggetto: " + subject);
			System.err.println("\nPer errore operazione su ResultSet SQL");
			e.printStackTrace();
		}
		catch (MessagingException e) {
			System.err.println("Problema invio e-mail ad indirizzo: " + addrto);
			System.err.println("Da indirizzo: " + addrfrom);
			System.err.println("Tramite server: " + mailServerIP);
			System.err.println("Su porta: " + mailServerPort);
			System.err.println("Con User Name: " + mailServerUname);
			System.err.println("Password: " + mailServerPwd);
			System.err.println("Soggetto: " + subject);
			System.err.println("\nPer errore di connessione");
			e.printStackTrace();
		}

		try {
			rs.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
