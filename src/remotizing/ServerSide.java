package remotizing;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import arcmanagement.BackgroundTask;
import arcmanagement.GlobalCollector;
import java.rmi.RemoteException;

public class ServerSide {
	GlobalCollector gc;

	public static void main(String[] args) throws InterruptedException {
		BackgroundTask bgTask;
		Registry registry;
		String name = "GlobalStub";
		GlobalStub gs;

		try {
			bgTask = new BackgroundTask();
		}
		catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		/*
		 * //Sarebbe da scommentare ma prima bisogna capire come fare in modo che il gestore della sicurezza permetta la comunicazione del server con il DBMS (se remoto).
		 * if(System.getSecurityManager() == null) { System.setSecurityManager(new SecurityManager()); }
		 */
		try {
			gs = new GlobalCollector();
			GlobalStub gStub = (GlobalStub) UnicastRemoteObject.exportObject(gs, 0);
			registry = LocateRegistry.getRegistry();
			registry.rebind(name, gStub);
			System.out.println("GlobalStub bounded with name \"" + name + "\"");
		}
		catch (Exception e) {
			System.err.println("GlobalStub exception:");
			e.printStackTrace();
			return;
		}

		bgTask.start();

		Thread.sleep(3000);//Perchè gc.CanDeistantiate() ritorna "false" solo ad inizializzazione avvenuta (piccola cavolata ma non ho voglia di cambiare il codice).
		try {
			while(!gs.CanDeistantiate()) {//Esce dal ciclo quando il metodo gc.CanDeistantiate() ritorna true (quindi quando il cilent con credenziali di amministratore riceve l'evento dal bottone "Quit").
				Thread.sleep(3000);
			}
		}
		catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("BackgroundTask in arresto: attendere almeno <BackgroundTaskInterval> minuti (vedere properties.prop)......");
		bgTask.halt();

		try {
			registry.unbind(name);
		}
		catch (Exception e) {
			System.err.println("GlobalCollector exception:");
			e.printStackTrace();
			return;
		}

		System.out.println("GlobalCollector unbounded");
		return;
	}

}
