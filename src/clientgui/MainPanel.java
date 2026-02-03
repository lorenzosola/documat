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
 * Created on Apr 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 * 
 */
package clientgui;


import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.ServerException;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import remotizing.GlobalStub;

import customobj.wrappers.LocalProp;

import arcmanagement.ValidationException;

/**
 * @author lsola
 * 
 */
public class MainPanel extends JFrame {
	private static final long serialVersionUID = 1L;

	private UserLogin login = null;
	private UserPanel userPanel = null;
	private AdminPanel adminPanel = null;
	static JFrame questo;
	// I due campi seguenti vengono utilizzati per identificare l'utente in tutte le richieste al server.
	static int userID;
	static String userName;
	static String password;
	private GlobalStub gc;
	boolean logoutReq = false;// Quando diventa a true nel compare il pannello di login.
	LocalProp localProp;
	boolean loggedIn = false;

	/**
	 * This is the default constructor
	 */
	public MainPanel(GlobalStub gc) {
		super();
		questo = this;
		// Catturo l'evento di chiusura della finestra per effettuare tutte le operazioni necessarie alla chiusura del programma.
		// Questa sezione e' meglio metterla qui perche l'initialize puo' essere ripetuto (non si sa mai).
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				logoutReq = true;
				if(loggedIn == false) login.exitRequest();
			}
		});
		
		this.gc = gc;
		
		try {
			localProp = new LocalProp("guiprop.prop");
		}
		catch (IOException e1) {
			//JOptionPane.showMessageDialog(this, "File named \"guiprop.prop\" in the base directory was not found", "Unsatisfacted requirement", JOptionPane.ERROR_MESSAGE);
			JOptionPane.showMessageDialog(this, e1.getMessage(), "Unsatisfacted requirement", JOptionPane.ERROR_MESSAGE);
		}
		initialize();
	}
	
	JFrame getMainPanelInstance() {
		return questo;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(1100, 700);
		this.setTitle("Documat");
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);// Di default c'e' il HIDE_ON_CLOSE ma questo impedisce una corretta gestione del relogin per chiusura server.

		guiTasking = true;

		try {
			this.setContentPane(getUserLogin());
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), null, JOptionPane.ERROR_MESSAGE);
		}

		this.setVisible(true);
		backgroundLoop();// Il background look viene eseguito dal thread di avvio del programma e lo stesso ne gestisce la chiusura.
	}

	private UserLogin getUserLogin() throws IOException {
		if(login == null) {
			login = new UserLogin(gc);
			try {
				login.tfUtente.setText(localProp.get("defaultUser"));
				login.tfPassword.setText(localProp.get("defaultPassword"));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		login.setVisible(true);
		return login;
	}

	/**
	 * Chiamata per effettuare tutte la chiusura del client (e la chiusura del server solo se le credenziali di ADMIN sono inserite).
	 * 
	 * @return true se tutto OK false se non e' possibile chiudere qualcosa.
	 */
	private boolean haltServer() {
		// Se GlobalCollector non `e stato istanziato da nessuno perche` nessuno ha effettuato almeno un login il seguente metodo non genera ValidationException per alcun utente.
		// In tal modo se un utente qualsiasi avvia il programma per errore senza utilizzarlo puo` comunque chiuderlo.
		try {
			gc.close(userID, password);
		}
		catch (ServerException e) {
			JOptionPane.showMessageDialog(this, e.getMessage() + " Contact the administrator");
			return false;
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Strange Problem", JOptionPane.ERROR_MESSAGE);
		}
		catch (ValidationException e) {
			// Se la versione e` client-server solo l'utente ADMIN puo' chiudere il programma....
			JOptionPane.showMessageDialog(questo, e.getMessage());
			JOptionPane.showMessageDialog(questo, "Immettere dati per utente ADMIN\nper chiudere il programma (inclusa la sezione SERVER).");
			login.tfUtente.setText("ADMIN");
			return false;
		}
		questo.dispose();
		return true;
	}

	private UserPanel getUserPanel() {
		if(userPanel == null) userPanel = new UserPanel(gc, userID, userName);
		return userPanel;
	}

	private AdminPanel getAdminPanel() {
		if(adminPanel == null) {
			adminPanel = new AdminPanel(gc);
		}
		return adminPanel;
	}

	/**
	 * Il thread che gestisce gli eventi e' lo stesso che si occupa dell'aggiornamento dello schermo. Per questo e' opportuno associare agli eventi sempre metodi non bloccanti e fare eseguire tutti gli altri compiti al processo principale che verra' trattenuto in un loop anziche terminare immediatamente dopo l'inizializzazione. Questo metodo contiene il loop principale che gestisce le richieste in
	 * base ad un ID. L'ID viene impostato tramite un metodo reso publico in questa classe: reqTask(int i).
	 */
	private boolean guiTasking;
	private int backgroundLoopTime = 500;
	private static Vector<MethodObj> methodQueue = new Vector<MethodObj>();  //  @jve:decl-index=0:

	private void backgroundLoop() {
		while(guiTasking) {
			// -----------------------------------------Gestione Richieste esterne--------------------------
			if(methodQueue.size() > 0) {
				try {
					MethodObj metobj = methodQueue.remove(0); 
					metobj.method.invoke(metobj.obj, (Object[]) null);
					
				}
				catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				}
				catch (IllegalAccessException e1) {
					e1.printStackTrace();
				}
				catch (InvocationTargetException e1) {
					e1.printStackTrace();
				}
			}
			// -----------------------------------------Fine Gestione Richieste esterne--------------------------

			try {
				Thread.sleep(backgroundLoopTime);

				// if(backgroundLoopTime < 1500) {
				// backgroundLoopTime += 5;
				// System.out.println("backgroundLoopTime=" + backgroundLoopTime);
				// }

			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}

			if(logoutReq == true) {//This state will be reaced every time a user press "window closing" button on the frame window.
				logoutReq = false;

				userID = login.finalUserID;
				userName = login.finalUserName;
				password = login.finalPassword;

				if(userPanel != null) {
					remove(userPanel);
					userPanel = null;
				}
				if(adminPanel != null) {
					remove(adminPanel);
					adminPanel = null;
				}

				try {
					setContentPane(getUserLogin());
					loggedIn = false;
					//getUserLogin().validate();
					//login.repaint();
					questo.validate();
					questo.repaint();
				}
				catch (IOException e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), null, JOptionPane.ERROR_MESSAGE);
				}
				
				//Le seguenti 2 istruzioni sono state aggiunta per comodità: togliere non appena il programma verr
				try {
					login.tfUtente.setText(localProp.get("defaultUser"));
					login.tfPassword.setText(localProp.get("defaultPassword"));
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}

			else if(login.isLoginOK() && loggedIn == false) {//This state will be reaced when a user press "Login" button on login panel after he has inserterted the correct login data. 
				userID = login.finalUserID;
				userName = login.finalUserName;
				password = login.finalPassword;
				remove(login);
				// login = null;
				if(!userName.equalsIgnoreCase("admin")) {
					if(adminPanel!=null) {
						remove(adminPanel);
					}
					setContentPane(getUserPanel());
				}
				else {
					if(userPanel!=null) remove(userPanel);
					setContentPane(getAdminPanel());
				}
				this.validateTree();
				//repaint();
				loggedIn = true;
			}

			else if(login.exitOK()) {//This state will be reaced when a user press "Exit" button on login panel after he has inserterted the correct login data.
				userID = login.finalUserID;
				userName = login.finalUserName;
				password = login.finalPassword;

				if(haltServer()) {
					guiTasking = false;
					this.dispose();
				}
			}
		}
		System.out.println("MainPanel Ended.");
	}

	
	/**
	 * Tramite questa funzione le altre finestre richiedono un task grafico. I "task grafici" sono chiamate a funzioni di elaborazione di media durata che agiscono sulla parte grafica in background. Un esempio puo' essere l'aggiornamento dei risultati in un tabella a mano a mano che essi sono resi disponibili.
	 * 
	 * @param met
	 *            il metodo (ottenuto tramite reflection) da eseguire (una volta). I metodi non devono avere argomenti.
	 */
	public static void reqTask(Method met, Object obj) {
		if(methodQueue.size() < 50) {
			methodQueue.add(new MethodObj(met, obj));
		}
		// if(backgroundLoopTime > 75) {
		// backgroundLoopTime -= 70;
		// }
	}

	public static int getUserID() {
		return userID;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"

/**
 * Utilizzata per associare un metodo e una istanza della classe che lo contiene.
 * @author lsola
 *
 */
class MethodObj {
	Method method;
	Object obj;
	/**
	 * @param method method to be scheduled.
	 * @param obj - the object the underlying method is invoked from.
	 */
	MethodObj(Method method, Object obj) {
		this.method = method;
		this.obj = obj;
	}
}
