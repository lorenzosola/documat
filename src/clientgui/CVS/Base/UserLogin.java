/**
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 *  @author Lorenzo Sola: lorenzo.sola@alice.it
 */


/*
 * Created on May 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package clientgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import arcmanagement.GlobalCollector;
import arcmanagement.ValidationException;

/**
 * @author lsola
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
class UserLogin extends JPanel {
	private static final long serialVersionUID = 1L;

	private javax.swing.JPanel inserimenti = null;

	private JPanel utente = null;
	private JPanel password = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	public JTextField tfUtente = null;
	public JTextField tfPassword = null;
	private JPanel funzioni = null;
	private JButton login = null;
	private boolean exitOK = false, loginOK = false;
	public int finalUserID = 0;
	public String finalUserName = null; // Viene impostato dall'azione di login eseguita con successo;
	public String finalPassword = null; // Viene impostato dall'azione di login eseguita con successo;
	private JPanel questo = this;
	private GlobalCollector gc;

	private JButton exitButton = null;

	/**
	 * This is the default constructor
	 * 
	 * @throws IOException
	 */
	public UserLogin(GlobalCollector gc) throws IOException {
		super();
		initialize();
		this.gc = gc;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 * @throws IOException
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getInserimenti(), BorderLayout.CENTER);
		this.add(getFunzioni(), java.awt.BorderLayout.SOUTH);
	}

	/**
	 * This method initializes inserimenti
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getInserimenti() {
		if(inserimenti == null) {
			inserimenti = new JPanel();
			inserimenti.setLayout(new VerticalLayout());
			inserimenti.add(getUtente(), getUtente().getName());
			inserimenti.add(getPassword(), getPassword().getName());
		}
		return inserimenti;
	}

	/**
	 * This method initializes utente
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getUtente() {
		if(utente == null) {
			jLabel = new JLabel();
			utente = new JPanel();
			jLabel.setText("User");
			jLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
			jLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			jLabel.setPreferredSize(new Dimension(60, 21));
			utente.add(jLabel, null);
			utente.add(getTfUtente(), null);
		}
		return utente;
	}

	/**
	 * This method initializes password
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getPassword() {
		if(password == null) {
			jLabel1 = new JLabel();
			password = new JPanel();
			jLabel1.setText("Password");
			jLabel1.setPreferredSize(new Dimension(60, 21));
			password.add(jLabel1, null);
			password.add(getTfPassword(), null);
		}
		return password;
	}

	/**
	 * This method initializes tfUtente
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getTfUtente() {
		if(tfUtente == null) {
			tfUtente = new JTextField();
			tfUtente.setPreferredSize(new Dimension(200, 20));
		}
		return tfUtente;
	}

	/**
	 * This method initializes tfPassword
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getTfPassword() {
		if(tfPassword == null) {
			tfPassword = new JPasswordField();
			tfPassword.setPreferredSize(new Dimension(200, 20));
		}
		return tfPassword;
	}

	/**
	 * This method initializes funzioni
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getFunzioni() {
		if(funzioni == null) {
			funzioni = new JPanel();
			funzioni.add(getLogin(), null);
			funzioni.add(getExitButton(), null);
		}
		return funzioni;
	}

	/**
	 * This method initializes inizia
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getLogin() {
		if(login == null) {
			login = new JButton("Login");
			login.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					loginOK = verifyUserPassword();
				}
			});
		}
		return login;
	}

	/**
	 * 
	 * @return true solo quando i dati di login immessi vengono confermati dal serevr.
	 */
	private boolean verifyUserPassword() {
		//boolean loginOK=true;Per DEBUG.
		boolean res = false;
		finalUserName = tfUtente.getText();
		finalPassword =  tfPassword.getText();
		try {
			try {
				finalUserID = gc.verifyUserPassword(finalUserName, finalPassword);
				res = true;
			}
			catch (IOException e) {
				JOptionPane.showMessageDialog(questo, "Server's message:" +e.getMessage());
			}
		}
		catch (ValidationException e) {
			JOptionPane.showMessageDialog(questo, e.getMessage());
		}
		if(!res) {
			tfUtente.setText(null);
			tfUtente.requestFocus();
			tfPassword.setText(null);
		}
		return res;
	}

	/**
	 * Se il valore di ritorno e` true, alla chiamata successiva false se non viene prima rieffettuata la richiesta di login. 
	 * @return boolean che indica se il login e' stato effettuato con successo (true) oppure se si e' premuto il pulsante "esci" (false).
	 */
	public boolean isLoginOK() {
		boolean tmpLoginOK = loginOK;
		loginOK = false;
		return tmpLoginOK;
	}

	/**
	 * Quando chiamata con dati immessi validi allora exitRequested ritorna true.
	 * 
	 */
	public void exitRequest() {
		if(verifyUserPassword()) exitOK = true;
		else exitOK = false;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getExitButton() {
		if(exitButton == null) {
			exitButton = new JButton();
			exitButton.setText("Quit");
			exitButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
					exitRequest();
				}
			});
		}
		return exitButton;
	}
	
	/**
	 * @return boolean true solo dopo che viene cliccato il pulsante "Esci Programma" con dati immessi validi.
	 */
	public boolean exitOK() {
		boolean tmpExitOK = exitOK;
		exitOK = false;
		return tmpExitOK;
	}

}