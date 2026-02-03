package remotizing;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JOptionPane;

import customobj.wrappers.LocalProp;

import clientgui.MainPanel;

public class ClientSide {

	public static void main(String[] args) {
		String remoteBindedGSName = "GlobalStub";
		/*
		 * //Sarebbe da scommentare ma prima bisogna capire come fare in modo che il gestore della sicurezza permetta la comunicazione del server con il DBMS (se remoto). if
		 * (System.getSecurityManager() == null) { System.setSecurityManager(new SecurityManager()); }
		 */
		//Properties sysprop = System.getProperties();
		//sysprop.setProperty("java.class.path", ".\\documatBin.jar;.\\jaybird-full-2.1.0.jar;.\\mysql-connector-java-5.1.15-bin.jar");
		LocalProp localProp;
		try {
			localProp = new LocalProp("guiprop.prop");
		}
		catch (IOException e1) {
			//JOptionPane.showMessageDialog(this, "File named \"guiprop.prop\" in the base directory was not found", "Unsatisfacted requirement", JOptionPane.ERROR_MESSAGE);
			JOptionPane jop = new JOptionPane();
			JOptionPane.showMessageDialog(jop, e1.getMessage(), "Unsatisfacted requirement", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			String ipAddr;
			if((ipAddr = localProp.get("RegServerIP")) == null) {
				JOptionPane jop = new JOptionPane();
				JOptionPane.showMessageDialog(jop, "Parameter \"RegServerIP\" not present in file \"guiprop.prop\"", "Unsatisfacted requirement", JOptionPane.ERROR_MESSAGE);
			}
			Registry registry = LocateRegistry.getRegistry(ipAddr);
			GlobalStub remoteGc = (GlobalStub) registry.lookup(remoteBindedGSName);
			MainPanel mainPanel = new MainPanel(remoteGc);
			mainPanel.dispose();
		}
		catch (NotBoundException e) {
			JOptionPane.showMessageDialog(null, "Non possibile trovare oggetto remoto: probabilmente il server non è in funzione.");
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}

	}

}
