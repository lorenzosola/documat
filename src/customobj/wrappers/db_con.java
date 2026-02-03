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

package customobj.wrappers;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

/**
 * Questa classe e` una interfaccia ad alto livello alle connessioni a DB che fornisce la possibilita` di ottenere una sola connessione per ognuno dei DB
 * con cui si vuole comunicare senza occuparsi della registrazione dei driver e del fatto che ne esista gia` una.
 * L'utilita` consiste nel fatto che utilizzando solo questa classe il programmatore non deve piu` preoccuparsi di controllare
 * se nel codice esistono piu` creazioni di una connessione allo stesso DB (che eventualmente non viene piu` utilizzata).
 * E` molto utile per creare statement al volo fornendo solo una stringa che identifica approssimativamente il nome del driver, l'URL del DB
 * il nome utente e la password.
 * 
 * SI SOTTOLINEA CHE QUESTO NON E` UN POOL DI CONNESSIONI infatti per ogni istanza dello stesso viene creata una e una sola connessione per ognuno dei DB
 * con cui ci si vuole interfacciare.
 * 
 * I riferimenit ai driver registrabili per ora sono memorizzati un una serie di String statiche e quindi per ogni nuovo driver che si vuole utilizzare deve essere aggiunta
 * la relativa stringa, che deve fare riferimenot ad un driver jdbc correttamente installato e reso disponibile alla libreria del progetto. 
 *  
 * Se la connessione non esiste viene creata, altrimenti viene riutilizzata. Per avere lo statement bisogna passare come parametri la stringa rappresentante la
 * query a parametri ed il nome della sorgente-dati. Per il caricamento del giusto driver viene
 * analizzata la stringa di connessione attraverso un' interessante funzione
 * :"calcoloCoerenza(...)". Tale funzione, ormai antiquata ma rappresentante la pietra miliare della
 * produzione di Lorenzo Sola all'Autentiweb (e' praticamente un ricordo), ha una complessita'
 * di calcolo O(n^2 * m) dove n=numero di lettere della frase a ed m=numero di lettere della frase
 * b (utilizzata con criterio, come in questo caso,  non introduce ritardi significativi).
 * 
 * @author lsola
 */
public class db_con {
	private boolean drvRegistrati = false;
	private Vector<Connection> conns = new Vector<Connection>(); //Vettore contenente gli oggetti Connection creati o
	// utilizzati di volta in vota.
	private Vector<String> connsName = new Vector<String>(); //Vettore della stessa dimensione di "conns" contenente
	// il nome degli oggetti Connection in esso contenuti.
	// L'indice determina l'associazione tra un elemento del
	// vettore conns ed il corrispondente elemento del vettore
	// connsName.
	
	public static Vector<String> DriversName;
	int indice; //Messa qui per velocizzare la getConnection(...).


	public db_con() {
		DriversName = new Vector<String>();
		DriversName.add("sun.jdbc.odbc.JdbcOdbcDriver");
		DriversName.add("com.microsoft.jdbc.sqlserver.SQLServerDriver");
		DriversName.add("org.firebirdsql.jdbc.FBDriver");
		DriversName.add("com.mysql.jdbc.Driver");
	}
	
	/**
	 * Ritorna un PreparedStatement data la stringa SQL a parametri ed il nome della sorgente dati.
	 * 
	 * @param SQL
	 * @param connString
	 * @param user
	 * @param password
	 * @return PreparedStatement
	 * @throws SQLException
	 */
	public PreparedStatement getPstat(String SQL, String connString, String user, String password) throws SQLException {
		PreparedStatement psttm = getConnection(connString, user, password).prepareStatement(SQL);
		return psttm;
	}

	/**
	 * Ritorna un CallableStatement data la stringa SQL a parametri ed il nome della sorgente dati.
	 * 
	 * @param SQL
	 * @param connString
	 * @param user
	 * @param password
	 * @return
	 * @throws SQLException
	 */
	public CallableStatement getCStat(String SQL, String connString, String user, String password) throws SQLException {
		CallableStatement csttm = getConnection(connString, user, password).prepareCall(SQL);
		return csttm;
	}
	
	/**
	 * Ritorna uno Statement.
	 * 
	 * @param connString
	 * @param user
	 * @param password
	 * @return Statement
	 * @throws SQLException
	 */
	public Statement getStat(String connString, String user, String password) throws SQLException {
		Statement sttm = getConnection(connString, user, password).createStatement();
		return sttm;
	}
	
	/**
	 * Ritorna una connessione.
	 * Se la connessione e` gia` stata precedente mente stabilita per il DB specificato allora restituisce qiella, altrimenti ne crea una nuova.
	 * @param connString
	 * @param user
	 * @param password
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection(String connString, String user, String password) throws SQLException {
		//String db_url = connString;
		registraDrivers(connString);

		//Se la sorgente dati specificata da connString non e' ancora stata connessa tento di creare una
		// connessione verso di essa, altrimenti utilizzo quella gia' esistente.
		if((indice = connsName.indexOf(connString)) == -1) {
			Connection con = DriverManager.getConnection(connString, user, password);
			conns.add(con);
			connsName.add(connString.toString());
			return con;
		}
		else {
			if(conns.get(indice).isClosed()) {
				Connection con = DriverManager.getConnection(connString, user, password);
				conns.set(indice, con);
			}
			return (Connection) conns.get(indice);
		}
	}

	private void registraDrivers(String db_url) {
		try {
			if(drvRegistrati == false) {
				//Ora cerco quale driver, tra i presenti, ha la maggior probabilita' di interfacciarsi al DB
				// con la stringa di connessione data..
				int indiceDriver = 0;
				int maxCoerenza = Integer.MIN_VALUE, coerenza;
				for(int i = 0; i < DriversName.size(); i++) {
					coerenza = calcoloCoerenza(db_url, DriversName.get(i));
					if(maxCoerenza < coerenza) {
						maxCoerenza = coerenza;
						indiceDriver = i;
					}
				}

				Class.forName(DriversName.get(indiceDriver)); //Questo fa si che nella cache della VM ia caricata
				// la classe cossispondente al driver che interessa.
				drvRegistrati = true;
			}
			//Enumeration driver_caricati = DriverManager.getDrivers();
			//System.out.println("Sono stati caricati i driver:");
			//while (driver_caricati.hasMoreElements())
			//	System.out.println(driver_caricati.nextElement());
		}
		catch (ClassNotFoundException e) {
			System.err.println("db_con : " + e.getMessage());
			e.fillInStackTrace();
		}
		
		return;
	}

	/**
	 * Chiude tutte le connessioni al DB. Se successivamente viene chiamato un metodo tipo getStat(...)
	 * o getPStat(....) viene automaticamente ricreata una connessione verso il DB di destinazione.
	 * 
	 * @throws SQLException
	 */
	public void discon() throws SQLException {
		for(int i = 0; i < conns.size(); i++) {
			((Connection) conns.get(i)).close();
			if(!((Connection) conns.get(i)).isClosed()) ((Connection) conns.get(i)).commit();
		}
		conns.clear();
		connsName.clear();
	}

	/**
	 * N.B. La funzione differisce da quella "originale" nel calcolo del punteggio e funziona
	 * decisamente meglio. (almeno per le stringhe lunghe).
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private int calcoloCoerenza(String a, String b) {
		byte A[] = a.getBytes();
		byte B[] = b.getBytes();
		int punteggio = 1;
		for(int lsub = 1; lsub < A.length; lsub++) { //lsub e' la lunghezza della sottostringa
			for(int Apos = 0; Apos + lsub <= A.length; Apos++) {
				for(int Bpos = 0; Bpos + lsub <= B.length; Bpos++) {
					int n;
					for(n = 0; n < lsub; n++) { //Trovo la corrispondenza della sottostringa.
						if(A[Apos + n] != B[Bpos + n]) break;
					}
					if(n == lsub) { //Significa che ho trovato una ricorrenza della sottostringa di lunghezza
						// "lsub".
						punteggio += lsub * lsub * 7; //Piu' grande e' la costante moltiplicativa piu' si sposta il
						// peso del punteggio verso le stringhe piccole (aumenta la
						// finezza). Nel contempo aumenta la probabilita' che stringhe
						// non coerenti sopravvalgano su stringhe coerenti al
						// significato che si vuole cercare.
						break; //Il punteggio deve essere incrementato una sola volta all'esistenza della
						// sottostringa nell'array B indipendentemente dal numero di ricorrenze.
					}
				}
				punteggio -= 1;
			}
		}
		return punteggio;
	}
	
	public void close() throws SQLException {
		discon();
	}
}