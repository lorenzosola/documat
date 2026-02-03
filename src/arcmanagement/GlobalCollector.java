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

package arcmanagement;

import customobj.containers.ObjToInt;
import customobj.functions.Compare;
import customobj.gui.TableDataManager;
import customobj.wrappers.DangerousOperationException;
import customobj.wrappers.LocalProp;
import customobj.wrappers.db_con;

import java.sql.Statement;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.ServerException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JTextArea;

import remotizing.GlobalStub;

import java.io.Serializable;
/**
 * Classe multi-istanza (una per ogni client) o mono-istanza (la stessa per piu' CLIENT) che si occupa della gestione dei resultKey e dei permessi contenendo tutti i metodi necessari all'inserimento e
 * modifica dei dati. Entro questa classe verranno quindi istanziati tutti gli oggetti con metodi rientanti come "Fileer" e "SearchDispatcher" e vengono getitite le comunicazione tra tutte le
 * interfaccie utente e la parte di gestione dei dati. Tra i parametri di ingresso dei metodi devono sempre essere presenti il nome dell'utente, la sua password e, nei metodi non bloccanti, la chiave
 * di accesso costituendo un sistema di sicurezza dato appunto dall'associazione dei 3 parametri. Sono contenuti anche i metodi per l'inserimento coerente e controllato nelle tabelle UTENTI,
 * FILTERS_DESCR e FILTERS_WORDS, GRPCNCPT mentre l'estrazione delle informazioni dal DB avviene ad opera dell'intervaccia utente tramite PreparedStatement ottenuti da una classe db_con LOCALE e
 * quindi da connessione dedicata verso il DB. Questa classe costituisce quindi l'elemento base della comunicazione in un server ad oggetti: ogni client deve richiedere un'istanza di questa classe e
 * tramite essa effettuare tutte le operazioni che esulano dalla semplice interrogazione del DB.
 * 
 * Many methods needs a parameter called accessKey. This "key" can previously be obtained calling the getNewAccessKey(.) (see). By this strategy the client can't have any access to che DB Server and
 * security is increased by the need of the (limited life time) accessKey in the passed parameters. The only one way to get all the Objects that can do dangerous operations or access the DB-tables is
 * to pass throw 2 states: the request of an access key and the passing it at "sessitive" functions. accessKey has also another purpose: annotation (always by the existence of the implicit 2 states)
 * of a request number that must be used to obtains the relative response. This is the case (for example) of all the functions that begins a long search task: it returns the access key throw which the
 * client can obtains the relative response after some time (every limited for the presence of a timeout mechanism).
 * 
 * @author lsola
 */
public class GlobalCollector implements Serializable,  GlobalStub {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static db_con privateconn; // Per ora usata solo dalla close().
	private static db_con conn;
	private static FileInserter inserter = null;
	private static SearchDispatcher searcher = null;
	private static boolean activityClosed = true;
	private static LocalProp localProp;
	private static int maxReqQueue;
	private static UserKey richieste[];
	private int actualAK = 0;// Accesskey attuale.
	private int richiesteIndex = 0;

	public GlobalCollector() throws IOException, ServerException {
		super();
		if(localProp==null) {
			localProp = new LocalProp("properties.prop");
			System.out.println("Creata istanza di LocalProp");
		}

		if(conn==null) {
			conn = new db_con();
			System.out.println("Creata istanza di db_con");
		}

		if(inserter == null) {
			inserter = new FileInserter(GlobalCollector.conn);
			System.out.println("Creata istanza di FileInserter");
		}

		if(searcher == null) {
			searcher = new SearchDispatcher(Integer.parseInt(localProp.get("MaxThreads")), conn);
			System.out.println("Creata istanza di SearchDispatcher");
		}
		activityClosed = false;

		maxReqQueue = localProp.getIntPar("SimActiveRequests");
		richieste = new UserKey[maxReqQueue];// Contiene un certo numero di richieste gestite circolarmente (se il buffer e' pieno l'ultima cancella la prima).
		System.out.println("1 GlobalCollector inizializzato\n\n");
	}

	
	/**
	 * Verify user and password for a client.
	 * 
	 * @param name
	 *            this will be always turned to upper-case mode.
	 * @param password
	 * @return user ID.
	 * @throws ValidationException
	 * @throws IOException
	 */
	public int verifyUserPassword(String name, String password) throws ValidationException, IOException {
		PreparedStatement ps;
		
		int userID;
		try {
			try {
				ps = conn.getPstat("select * from utenti where USERNAME=?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			}
			catch (IOException e) {
				throw new IOException(e.getMessage());
			}
			ps.setString(1, name.toUpperCase());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				if(rs.getString("PASSWORD").equals(password)) {
					userID = rs.getInt("ID");
				}
				else {
					throw new ValidationException("Password not valid");
				}
			}
			else {
				if(name.equals("ADMIN") && password.equals("admin")) {
					insUser("ADMIN", "admin", "ADMIN", "admin", "Administrator", null, null);
					throw new ServerException("User ADMIN inserted (initialization OK)");
				}
				throw new ValidationException("UserName not valid");
			}

			rs.close();
			ps.close();
		}
		catch (org.firebirdsql.jdbc.FBSQLException e1) {
			throw new IOException(e1.getMessage());
		}
		catch (SQLException e2) {
			throw new IOException(e2.getMessage());
		}
		return userID;
	}

	/**
	 * Inserisce un nuovo utente. Solo l'lutente ADMIN puo effettuare questa operazione. Se l'utente e la password non coincidono con ADMIN e relativa password il metodo ritorna un messaggio di errore
	 * e non effettua alcuna transazione sul DB. Il campo USER e' sempre case insensitive mentre la password e' case sensitive. Se neanche l'utente ADMIN e' presente allora deve essere inserito
	 * tramite questo stesso metodo passando come parametro passreq la striga admin.
	 * 
	 * @param userreq
	 *            L'utente che vuole effettuare l'operazione.
	 * @param passwordreq
	 *            La password dell'utente che vuole effettuare l'operazione.
	 * @param user
	 *            L'utente che si vuole inserire. User is always turne to upper-case mode.
	 * @param password
	 *            password dell'utente che si vuole inserire.
	 * @param identif
	 *            Identificativo dell'utente che si sta inserendo (es. Nome Cognome).
	 * 
	 * @throws ValidationException
	 *             Scaturita ogni volta che la validazione (in senso generico) non va a buon fine.
	 * @throws SQLException
	 * @throws ServerException
	 * 
	 */
	public void insUser(String userreq, String passwordreq, String user, String password, String identif, String telephone, String address) throws ValidationException, SQLException, ServerException {
		userreq = userreq.toUpperCase();
		user = user.toUpperCase();
		if(userreq.equals("ADMIN")) {
			PreparedStatement ps;
			ResultSet rs;

			// Controllo utenti duplicati.
			try {
				ps = conn.getPstat("select * from utenti where USERNAME=?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			}
			catch (SQLException e) {
				throw new ServerException(e.getMessage() + "\nContact the administrator.");
			}
			catch (IOException e) {
				throw new ServerException(e.getMessage() + "\nContact the administrator.");
			}
			ps.setString(1, user);
			rs = ps.executeQuery();
			if(rs.next()) {
				rs.close();
				ps.close();
				throw new ValidationException("L'utente " + user + " esiste gia'.");
			}
			rs.close();
			ps.close();

			try {
				ps = conn.getPstat("select * from utenti where USERNAME=? and PASSWORD=?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			}
			catch (SQLException e) {
				throw new ServerException(e.getMessage() + "\nContact the administrator.");
			}
			catch (IOException e) {
				throw new ServerException(e.getMessage() + "\nContact the administrator.");
			}
			ps.setString(1, userreq);
			ps.setString(2, passwordreq);
			rs = ps.executeQuery();
			try {
				ps = conn.getPstat("insert into utenti (USERNAME, PASSWORD, IDENTIFIER, TELEFONO, INDIRIZZO) values(?,?,?,?,?) ", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			}
			catch (SQLException e) {
				throw new ServerException(e.getMessage() + "\nContact the administrator.");
			}
			catch (IOException e) {
				throw new ServerException(e.getMessage() + "\nContact the administrator.");
			}
			if(rs.next()) {
				ps.setString(1, user.toUpperCase());// L'utente va inserito sempre in UpperCase in quanto l'autenticazione prevede di volgere tutto ad upper case durente la richiesta.
				ps.setString(2, password);
				ps.setString(3, identif);
				ps.setString(4, telephone);
				ps.setString(5, address);
				if(ps.executeUpdate() == 0) throw new ValidationException("Inserimento in tabella utenti non avvenuto correttamente: il metodo executeUpdate ritorna 0.");
			}
			else {
				if(user.equals("ADMIN")) {// Significa che si sta inserendo l'utente admin.
					if(passwordreq.equals("admin")) {
						ps.setString(1, user);
						ps.setString(2, password);
						ps.setString(3, identif);
						ps.setString(4, telephone);
						ps.setString(5, address);
						ps.executeUpdate();
					}
					else throw new ValidationException("Per l'inserimento dell'utente ADMIN all'inizializzazione la passord deve essere admin (in minuscolo).");
				}
				else {
					rs.close();
					ps.close();
					throw new ValidationException("L'utente ADMIN non esiste ancora (necessaria inizializzazione) o la password e' errata.");
				}
			}
			rs.close();
			ps.close();
			return;
		}
		else {
			throw new ValidationException("Solo l'utente ADMIN e' autorizzato all'inserimento di un nuovo utente.");
		}
	}

	/**
	 * Funzione per cambio password degli utenti.
	 * 
	 * @param userreq
	 * @param oldpassword
	 * @param newpassword
	 * @throws ValidationException
	 *             Scaturita ogni volta che la validazione (in senso generico) non va a buon fine.
	 * @throws SQLException
	 * @throws ServerException
	 */
	public void chPasswd(String userreq, String oldpassword, String newpassword) throws ValidationException, SQLException, ServerException {
		userreq = userreq.toUpperCase();
		PreparedStatement ps = null;
		ResultSet rs;
		try {
			ps = conn.getPstat("select * from utenti where USERNAME=?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
		}
		catch (SQLException e) {
			throw new ServerException(e.getMessage() + "\nContact the administrator.");
		}
		catch (IOException e) {
			throw new ServerException(e.getMessage() + "\nContact the administrator.");
		}
		ps.setString(1, userreq);
		rs = ps.executeQuery();
		if(rs.next()) {
			if(rs.getString("PASSWORD").equals(oldpassword)) {
				rs.close();
				ps.close();
				try {
					ps = conn.getPstat("update utenti set PASSWORD=? where USERNAME=?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
				}
				catch (SQLException e) {
					throw new ServerException(e.getMessage() + "\nContact the administrator.");
				}
				catch (IOException e) {
					throw new ServerException(e.getMessage() + "\nContact the administrator.");
				}
				ps.setString(1, newpassword);
				ps.setString(2, userreq);
				if(ps.executeUpdate() == 0) throw new ValidationException("Modifica della tabella utenti non avvenuta correttamente: il metodo executeUpdate ritorna 0.");
			}
			else {
				rs.close();
				ps.close();
				throw new ValidationException("La vecchia password non e' corretta.");
			}
		}
		else {
			rs.close();
			ps.close();
			throw new ValidationException("Lo username non e' corretto.");
		}
		rs.close();
		ps.close();
		return;
	}

	/**
	 * Eliminazione di un utente, ovviamente la politica di eliminazione per tutte le informazioni contenute nel DB, e inserite proprio da quell'utente, dipende dalle regole di inferenza dichiarate
	 * sul DB.
	 * 
	 * @param userreq
	 *            utente che richiede la eliminazione (puo' essere solo admin).
	 * @param passwordreq
	 *            utente che richiede la eliminazione.
	 * @param user
	 *            utente da eliminare. this is always turned to upper-case mode.
	 * @throws ValidationException
	 *             Scaturita ogni volta che la validazione (in senso generico) non va a buon fine.
	 * @throws SQLException
	 * @throws ServerException
	 */
	public void delUser(String userreq, String passwordreq, String user) throws ValidationException, SQLException, ServerException {
		PreparedStatement ps;
		ResultSet rs;
		userreq = userreq.toUpperCase();
		user = user.toUpperCase();
		if(userreq.equals("ADMIN")) {
			// Controllo che la password di ADMIN sia corretta.
			try {
				ps = conn.getPstat("select * from utenti where USERNAME=?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			}
			catch (SQLException e) {
				throw new ServerException(e.getMessage() + "\nContact the administrator.");
			}
			catch (IOException e) {
				throw new ServerException(e.getMessage() + "\nContact the administrator.");
			}
			ps.setString(1, userreq);
			rs = ps.executeQuery();
			if(rs.next()) {
				if(rs.getString("PASSWORD").equals(passwordreq)) {
					try {
						ps = conn.getPstat("delete from utenti where USERNAME=?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
					}
					catch (SQLException e) {
						throw new ServerException(e.getMessage() + "\nContact the administrator.");
					}
					catch (IOException e) {
						throw new ServerException(e.getMessage() + "\nContact the administrator.");
					}
					ps.setString(1, user);
					if(ps.executeUpdate() == 0) {
						ps.close();
						throw new ValidationException("L'utente " + user + " non puo' essere eliminato, probabilmente non esiste.");
					}
					ps.close();
				}
				else {
					rs.close();
					ps.close();
					throw new ValidationException("La password per ADMIN non e' corretta.");
				}
			}
			else {
				rs.close();
				ps.close();
				throw new ValidationException("L'utente ADMIN non esiste ancora: necessaria inizializzazione.");
			}

		}
		else {
			throw new ValidationException("Solo ADMIN e' autorizzato alla eliminazione di un utente.");
		}
		return;
	}

	/**
	 * Ritorna un Vector contenente i "group concept" cioe' i concetti che accomunano i files contenuti del gruppo. Ogni group concept definisce un gruppo di files accomunati dal concetto
	 * (descritto nel DB). Un file puo' appartenere a piu' "group-concept" (a piu' gruppi). <br>
	 * I campi contenuti nel ResultSet richiesto al DB: <br>
	 * ID=identificatore numerico. DESCR=descrizione del "group concept" USER=utente che ha inserito il group-concept <br>
	 * e devono essere contenuti nella tabella GRP_CNCPT che deve essere presente nel DB.
	 * 
	 * @return il Result Set con i "group concept" (vedi sopra). Ricordare di chiudere il rs a fine uso.
	 * @throws ServerException
	 */
	public Vector<ObjToInt<String>> getGrpCncptsList() throws ServerException {
		String queryString = "select * from grp_cncpt";
		try {
			PreparedStatement ps;
			ResultSet rs;
			try {
				ps = conn.getPstat(queryString, localProp.get("connString"), localProp.get("user"), localProp.get("password"));
				rs = ps.executeQuery();
			}
			catch (IOException e) {
				throw new ServerException(e.getMessage() + "\nContact the administrator.");
			}
			ResultSetMetaData metadata = rs.getMetaData();
			int n = 0;// Se alla fine del controllo e' = al numero di colonne cercate si puo' dire OK
			// (controllo un po' approssimativo ma statisticamente valido).
			for(int i = 1; i <= metadata.getColumnCount(); i++) {
				String tmp = metadata.getColumnName(i);
				if(tmp.equalsIgnoreCase("id")) n++;
				else if(tmp.equalsIgnoreCase("descr")) n++;
				else if(tmp.equalsIgnoreCase("user")) n++;
			}
			if(n < 3) {
				System.out.println("SearchDispatcher: la query per ottenere i group-concepts non fornisce ");
				System.out.println("un result-set compatibile in quanto mancano delle colonne (vedere documentazione per questa classe).");
				return null;
			}
			
			Vector<ObjToInt<String>> groupsList = new Vector<ObjToInt<String>>();
			while(rs.next()) {
				groupsList.add(new ObjToInt<String>(rs.getString(2), rs.getInt(1)));
			}


			return groupsList;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Ritorna un Vector contenente le lingue registrate. I campi contenuti nel ResultSet richiesto al DB sono: <br>
	 * ID=identificatore numerico. DESCR=descrizione del "group concept" USER=utente che ha inserito il group-concept <br>
	 * e devono essere contenuti nella tabella GRP_CNCPT che deve essere presente nel DB.
	 * 
	 * @param linguistic
	 *            Se devono solo essere selezionati i filtri linguistici.
	 * @return il Result Set con i "group concept" (vedi sopra). Ricordare di chiudere il rs a fine uso.
	 * @throws ServerException
	 */
	public Vector<ObjToInt<String>> getFiltersList(boolean linguistic) throws ServerException {
		String queryString;
		if(linguistic) {
			queryString = "select * from filters_descr where LINGUISTIC=1";
		}
		else queryString = "select * from filters_descr";
		try {
			PreparedStatement ps;
			ResultSet rs;
			try {
				ps = conn.getPstat(queryString, localProp.get("connString"), localProp.get("user"), localProp.get("password"));
				rs = ps.executeQuery();
			}
			catch (IOException e) {
				throw new ServerException(e.getMessage() + "\nContact the administrator.");
			}
			ResultSetMetaData metadata = rs.getMetaData();
			int n = 0;// Se alla fine del controllo e' = al numero di colonne cercate si puo' dire OK
			// (controllo un po' approssimativo ma statisticamente valido).
			for(int i = 1; i <= metadata.getColumnCount(); i++) {
				String tmp = metadata.getColumnName(i);
				if(tmp.equalsIgnoreCase("id")) n++;
				else if(tmp.equalsIgnoreCase("descr")) n++;
				else if(tmp.equalsIgnoreCase("user")) n++;
			}
			if(n < 3) {
				System.out.println("SearchDispatcher: la query per ottenere i group-concepts non fornisce ");
				System.out.println("un result-set compatibile in quanto mancano delle colonne (vedere documentazione per questa classe).");
				return null;
			}

			Vector<ObjToInt<String>> filtersList = new Vector<ObjToInt<String>>();
			while(rs.next()) {
				filtersList.add(new ObjToInt<String>(rs.getString("DESCR"), rs.getInt("ID")));
			}
			return filtersList;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	// ---------------------------------Inizio metodi per gestione permesso e chiave di accesso------------------------------------------------

	/**
	 * Contiene la terna accessKey, user, password.
	 * 
	 * @author lsola
	 */
	private class UserKey {
		public long accessKey;
		public int user;
		public String password;

		public UserKey(long accessKey, int user, String password) {
			this.accessKey = accessKey;
			this.user = user;
			this.password = password;
		}
	}

	/**
	 * Da chiamare ad ogni richiesta per ottenere la accessKey
	 * 
	 * @param user
	 * @param password
	 * @return la accessKey
	 */
	public synchronized long getNewAccessKey(int user, String password) {
		actualAK++;// Ammettendo che il server riceva costantemente 10 rich/Sec allora il tempo massimo di funzionamento continuativo sarebbe di: (2^32)/(10*3600*24) = 4971.026963 giorni = 13.61925
		// anni senza ripetere chiavi gia` utilizzate. Quindi e` gia` soddisfacente. La chiave deve essere unica in tutto l'arco di funzionamento per come e` stato strutturato il server,
		// infatti molti oggetti
		// utilizzati per bufferizzare e tracciare le richieste, anche se con coda finita, necessitano di chiavi univocamente crescenti per tracciare l'ordine di arrivo ed eventualmente eliminare le
		// richieste scadute perche` troppo vecchie.
		richiesteIndex++;
		richiesteIndex %= maxReqQueue;
		richieste[richiesteIndex] = new UserKey(actualAK, user, password);
		return actualAK;
	}

	/**
	 * Da chiamare per sapere se la richiesta e' ancora valida o valida in generale la terna (accessKey, user, password).
	 * 
	 * @param accessKey
	 * @param user
	 * @param password
	 * @return
	 */
	private boolean validateRequest(long accessKey, int user, String password) {
		if(activityClosed) return false;
		int i = richiesteIndex;// Lo congelo.
		long tmplong;
		if(accessKey > (tmplong = richieste[i].accessKey)) return false;// Richiesta incoerente.
		tmplong = tmplong - accessKey;
		if(tmplong > maxReqQueue) return false;// La chiave e' gia' stata sovrascritta.
		i += maxReqQueue - tmplong;
		i %= maxReqQueue;// Ora i e' l'indice arretrato esattamente il necessario per posizionarsi sul record che interessa.
		if(richieste[i] != null && richieste[i].accessKey == accessKey && richieste[i].user == user && richieste[i].password.equals(password)) return true;
		else return false;// Puo' essere che la richiesta nel frattempo sia stata sovrascritta oppure che la terna non fosse corretta...
	}

	// ---------------------------------Fine metodi per gestione permesso e chiave di accesso------------------------------------------------

	/*
	public Vector<FileInfoRecord> conversion(File finput, long accessKey, int user, String password) throws ValidationException, ElabException, ServerException {
		if(finput == null) return null;
		if(!validateRequest(accessKey, user, password)) throw new ValidationException("Richiesta scaduta o parametri di accesso non validi o sessione server chiusa.");
		try {
			inserter.conversione(accessKey, finput);
		}
		catch (ElabException e) {
			throw new ServerException(e.getMessage() + "\nContact the administrator.");
		}
		catch (IOException e) {
			throw new ServerException(e.getMessage() + "\nContact the administrator.");
		}
		return null;
	}
	*/
	
	public Vector<FileInfoRecord> conversion(FileContent fContent, long accessKey, int user, String password) throws ValidationException, ElabException, ServerException {
		File finput;
		try {
			finput = fContent.recreateLocalFile(localProp.get("tempdir") + "/" + fContent.getSourceFileName());
		}
		catch (IOException e1) {
			e1.printStackTrace();
			throw new ServerException(e1.getMessage());
		}
		
		if(finput == null) return null;
		if(!validateRequest(accessKey, user, password)) throw new ValidationException("Richiesta scaduta o parametri di accesso non validi o sessione server chiusa.");
		
		try {
			inserter.conversione(accessKey, finput, fContent.getSourceFilePath());
		}
		catch (ElabException e) {
			throw new ServerException(e.getMessage() + "\nContact the administrator.");
		}
		catch (IOException e) {
			throw new ServerException(e.getMessage() + "\nContact the administrator.");
		}
		return null;
	}

	public Vector<FileInfoRecord> conversion(FileContent fContent, JTextArea text, long accessKey, int user, String password) throws ValidationException, ElabException, ServerException {
		File finput;
		try {
			finput = fContent.recreateLocalFile(localProp.get("tempdir") + "/" + fContent.getSourceFileName());
		}
		catch (IOException e1) {
			e1.printStackTrace();
			throw new ServerException(e1.getMessage());
		}

		if(finput == null) return null;
		if(!validateRequest(accessKey, user, password)) throw new ValidationException("Richiesta scaduta o parametri di accesso non validi o sessione server chiusa.");
		try {
			inserter.conversione(accessKey, finput, text, fContent.getSourceFilePath());
		}
		catch (IOException e) {
			throw new ServerException(e.getMessage() + "\nContact the administrator.");
		}
	
		return null;
	}

	public String example(long accessKey, int user, String password) throws ValidationException {
		if(!validateRequest(accessKey, user, password)) throw new ValidationException("Richiesta scaduta o parametri di accesso non validi o sessione server chiusa.");
		return inserter.esempio(accessKey);
	}

	public int insertFile(Vector<String> paroleSignificative, String titolo, int[] groupConceptList, long accessKey, int user, String password) throws ValidationException, ElabException, ServerException {
		if(!validateRequest(accessKey, user, password)) throw new ValidationException("Richiesta scaduta o parametri di accesso non validi o sessione server chiusa.");
		try {
			return inserter.insert(accessKey, titolo, paroleSignificative, groupConceptList, user);
		}
		catch (ElabException e) {
			throw new ServerException(e.getMessage() + "\nContact the administrator.");
		}
		catch (IOException e) {
			throw new ServerException(e.getMessage() + "\nContact the administrator.");
		}
	}

	public int insertFile(String titolo, Vector<String> paroleSignificative, int[] groupConceptList, int filtroLingua, long accessKey, int user, String password) throws ValidationException, ElabException, ServerException {
		if(!validateRequest(accessKey, user, password)) throw new ValidationException("Richiesta scaduta o parametri di accesso non validi o sessione server chiusa.");
		try {
			return inserter.insert(accessKey, titolo, paroleSignificative, groupConceptList, user, filtroLingua);
		}
		catch (ElabException e) {
			throw new ServerException(e.getMessage() + "\nContact the administrator.");
		}
		catch (IOException e) {
			throw new ServerException(e.getMessage() + "\nContact the administrator.");
		}
	}

	public int findLanguage(long accessKey, int user, String password) throws Exception {
		if(!validateRequest(accessKey, user, password)) throw new ValidationException("Richiesta scaduta o parametri di accesso non validi o sessione server chiusa.");
		return inserter.trovaLingua(accessKey);
	}

	public Classific getResponses(long accessKey, int user, String password) throws ValidationException, ElabException {
		if(!validateRequest(accessKey, user, password)) throw new ValidationException("Richiesta scaduta o parametri di accesso non validi o sessione server chiusa.");
		return searcher.getResponses(accessKey);
	}

	public void removeResponse(long accessKey, int user, String password) throws ValidationException {
		if(!validateRequest(accessKey, user, password)) throw new ValidationException("Richiesta scaduta o parametri di accesso non validi o sessione server chiusa.");
		searcher.removeResponse(accessKey);
	}

	/**
	 * Elabora la classifica di una serie di files ottenuti dal DB tramite una query compatibile basandosi sulla frase passata tra gli argomenti. La query compatibile consiste in una select che
	 * ritorna una riga per ogni file contenente almeno le colonne: <BR>
	 * -ID: descrizione del contenuto inserita dall'utente. Puo' essere una stringa vuota o null. <BR>
	 * -NOME_FILE: data di inserimento del file nel sistema. <br>
	 * -ESTENSIONE: percorso in cui si puo' raggiungere il file di testo (ed anche il documento). <br>
	 * -DESCRIZIONE: a cui appartiene il file. <br>
	 * -ARCH_PATH: del file. <br>
	 * 
	 * @param queryString
	 * @param phrase
	 * @param nresults
	 * @param nphrases
	 * @param cuttingIndex
	 *            Questo deve essere di dafault l'indice del filtro lingua!!!!!!!!!!!!!!!!!!!!!!!!!!!!!.
	 * @param accessKey
	 * @param user
	 * @param password
	 * @throws ValidationException
	 */
	public void search(final String queryString, final String phrase, final int nresults, final int nphrases, int[] cuttingIndex, final long accessKey, int user, String password) throws ValidationException {
		if(!validateRequest(accessKey, user, password)) throw new ValidationException("Richiesta scaduta o parametri di accesso non validi o sessione server chiusa.");

		try {
			searcher.search(queryString, phrase, nresults, nphrases, accessKey, cuttingIndex);
		}
		catch (DangerousOperationException e) {
			throw new ValidationException("Security violation:\n	" + e.getMessage());
			// e.printStackTrace();
		}
	}

	/**
	 * Individua in automatico a quale gruppo concettuale, tra quelli presenti, e` piu` probabile che appartenga il documento che si sta inserendo.
	 * 
	 * @param accessKey
	 * @param user
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public int findGroupCncpt(long accessKey, int user, String password) throws Exception {
		if(!validateRequest(accessKey, user, password)) throw new ValidationException("Richiesta scaduta o parametri di accesso non validi o sessione server chiusa.");
		return inserter.trovaGroupCncpt(accessKey);
	}

	/**
	 * Viene utilizzato dai client per ottenere tutti i TableDataManager di cui necessitano. In questo modo si passa attraverso i 2 stati (tramite le accessKey) che rendono l'istanziazione di ogni
	 * oggetto (remoto o locale) avente accesso al server (tutti e soli quelli utilizzati e forniti da GlobalCollector) piu` sicura.
	 * 
	 * Used by the client to obtain all the TableDataManager that it needs (is the only one way to do it).
	 */
	/*
	public TableDataManager getAClientTableDataManager(String[] headers, String[] dbFields, final long accessKey, int user, String password) throws ValidationException {
		if(!validateRequest(accessKey, user, password)) throw new ValidationException("Richiesta scaduta o parametri di accesso non validi o sessione server chiusa.");
		return new TableDataManager(conn, headers, dbFields);

	}
	*/
	
	/**
	 * Initializes the TableDataManager previously obtained throw the call of getAClientTableDataManager(....). By this strategy the client can't have any access to che DB Server and security is
	 * increased by the need of the accessKey in the in parameters (see the "Limited Life Time Access Key" strategy).
	 * 
	 * @param tdm
	 *            TableDataManager to be initialized
	 * @param sqlStr
	 *            the SQL string used by the tdm above to initialythe the data in it (seing the TableDataManager behaviour is reccomended).
	 * @param DBUser
	 *            The user throw it the DBMS determines permission grants to the DB Objects.
	 * @param DBPassword
	 *            The password for the user above specified.
	 * @param accessKey
	 * @param user
	 *            The user name registeredd in the Documat validation table.
	 * @param password
	 *            The password registeredd in the Documat validation table for the user specified.
	 * @throws SQLException
	 * @throws DangerousOperationException
	 * @throws IOException
	 * @throws ValidationException
	 */
	public void initialyzeATableDataManager(TableDataManager tdm, String sqlStr, String DBUser, String DBPassword, long accessKey, int user, String password) throws SQLException, DangerousOperationException, IOException, ValidationException {
		if(!validateRequest(accessKey, user, password)) throw new ValidationException("Richiesta scaduta o parametri di accesso non validi o sessione server chiusa.");
		tdm.init(sqlStr, localProp.get("connString"), DBUser, DBPassword);
	}

	/**
	 * Makes a join of 2 or more groups in the group having as ID the first in the passed list (must be a previuosly created group). Afted the join the description of the joining group is replaced
	 * with that specified in "description" parameter. The joined groups (that will be embpy) will be deleted.
	 * 
	 * @param grpJoined
	 *            a list of ID corresponding with the ID of the groups which has to be joined.
	 * @param description
	 *            final description of the joining group.
	 * @return the number of words moved from the joined groups into the joining group. -1 if the grpJoined size was < 2 (without sense operation).
	 * @throws SQLException
	 * @throws IOException
	 */
	public int joinCncptGroups(int[] grpJoined, String description, boolean deleteOldGroups) throws SQLException, IOException {
		int ret;
		if(grpJoined.length < 2) return -1;
		StringBuffer sql = new StringBuffer();
		boolean mettiOR = false;
		PreparedStatement ps;

		// Spostamento delle parole dei gruppi che verranno accorpati nel primo gruppo (e` sostanzialmente un cambio degli indici).
		sql.append("update grp_cncpt_words set GRP_CNCPT = ?");
		sql.append(" where (");
		// Aggiunta di condizioni che dovranno soddisfare i record da cambiare (GRP_CNCPT_ID = N1 OR GRP_CNCPT_ID = N2 OR.....).
		for(int i = 1; i < grpJoined.length; i++) {
			if(mettiOR == true) {
				sql.append(" OR ");
			}
			else mettiOR = true;
			sql.append("GRP_CNCPT = ?");
		}
		sql.append(")");
		// System.out.println(sql.toString());//For DEBUGGING.
		ps = conn.getPstat(sql.toString(), localProp.get("connString"), localProp.get("user"), localProp.get("password"));
		ps.setInt(1, grpJoined[0]);// Imposto l'indice del gruppo unente.
		for(int i = 1; i < grpJoined.length; i++) {// Imposto gli indici dei gruppi da unire.
			ps.setInt(i + 1, grpJoined[i]);
		}
		ret = ps.executeUpdate();
		ps.close();

		// Cambio del nome del primo gruppo in quanto ora e` diventato il gruppo unione.
		ps = conn.getPstat("update grp_cncpt set DESCR = ? where ID = ?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
		ps.setInt(2, grpJoined[0]);
		ps.setString(1, description);
		ps.executeUpdate();
		ps.close();

		// Cancellazione dei gruppi svuotati.
		if(deleteOldGroups) {
			sql.setLength(0);
			sql.append("delete from grp_cncpt where ");
			mettiOR = false;
			for(int i = 1; i < grpJoined.length; i++) {// Imposto gli indici dei gruppi da unire.
				if(mettiOR == true) {
					sql.append(" OR ");
				}
				else mettiOR = true;
				sql.append("ID = ?");
			}
			System.out.println(sql.toString());// For DEBUGGING.
			ps = conn.getPstat(sql.toString(), localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			for(int i = 1; i < grpJoined.length; i++) {// Imposto gli indici dei gruppi da unire.
				ps.setInt(i, grpJoined[i]);
			}
			ps.executeUpdate();
			ps.close();
		}

		return ret;
	}

	/**
	 * Ritorna un FileInfoRecord nel caso in cui esista nel DB un file appartenente ad almeno uno dei gruppi concettuali passati, nome e titolo (concatenati) diversi ma con similarita` > ad un certo
	 * valore [0,1) rispetto a quello passato (quindi anche se =). Se tra i titoli scansionati non ve ne e` nessuno con il requisito di similarita` minima allora il valoredi ritorno e` null. Nel
	 * FileInfo Record restituito il valore alla variabile genericInformation viene assegnato un oggetto Float contenente l'indice di dimilarita` [0,1] del titolo.
	 * 
	 * @param name
	 *            Il nome dell'elemento (in pratica il nome del file senza estensione).
	 * @param title
	 *            Il titolo dell'elemento.
	 * @param grpCncptList
	 *            La lista dei gruppi che, in totale o in parte, devono essere associati agli elementi perche` questi ultimi vengano considerati nella ricerca.
	 * @param minOfMembership
	 *            Minimo numero di gruppi tra quelli specificati a cui il file deve appartenere.
	 * @return un FileInfoRecord che con buona probabilita`, sia semplicemente lo stesso documento di quello in oggetto ma a cui il proprietario ha dato un titolo leggermente differente; null nel caso in cui
	 *         nessun documento gia` archiviato abbia tali caratteristiche (significa che il documento di cui e` richiesto l'inserimento puo` effettivamente essere inserito).
	 * @throws SQLException
	 * @throws IOException
	 */
	public FileInfoRecord ambiguityCheck(String name, String title, int[] grpCncptList, int minOfMembership) throws SQLException, IOException {
		PreparedStatement ps;
		ResultSet rs;
		FileInfoRecord fBean = null;

		/*
		 * Trova NOMEFILE, ESTENSIONE, DESCRIZIONE dei file appartenenti ad almeno "minOfMembership" dei gruppi specificati considerando solo quelli con ultima versione (max(VERSIONE)). Prototipo
		 * della query da effettuare. N.B. Query gia` provata e validata.
		 * 
		 * select FI.NOMEFILE, FI.ESTENSIONE, FI.DESCRIZIONE from FILEINFO FI inner join GRP_CNCPT_FILES GCF on FI.ID = GCF."FILE" where (GCF.GRP_CNCPT = IDGruppo1 or GCF.GRP_CNCPT = IDGruppo2 ecc.)
		 * and FI.VERSIONE = (select max(FI1.VERSIONE) from FILEINFO FI1 where FI1.NOMEFILE = FI.NOMEFILE) group by FI.NOMEFILE, FI.ESTENSIONE, FI.DESCRIZIONE having count(*) >= minOfMembership
		 */
		String or = "";
		StringBuffer sql = new StringBuffer("select FI.NOMEFILE, FI.ESTENSIONE, FI.DESCRIZIONE, FI.OWNER_ID, U.USERNAME from fileinfo FI inner join grp_cncpt_files GCF on FI.ID = GCF.FILE inner join utenti U on FI.OWNER_ID = U.ID "
				+ "where (");
		for(int i = 0; i < grpCncptList.length; i++) {
			sql.append(or + "GCF.GRP_CNCPT = " + grpCncptList[i]);
			or = " or ";
		}
		sql.append(") and FI.VERSIONE = " + "(select max(FI1.VERSIONE) from fileinfo FI1 where FI1.NOMEFILE = FI.NOMEFILE and FI1.DESCRIZIONE = FI.DESCRIZIONE) "
				+ "group by FI.NOMEFILE, FI.ESTENSIONE, FI.DESCRIZIONE, FI.OWNER_ID, U.USERNAME having count(*) >= " + minOfMembership);
		ps = conn.getPstat(sql.toString(), localProp.get("connString"), localProp.get("user"), localProp.get("password"));
		rs = ps.executeQuery();
		float simcheck = 0, simcheckMax = 0;

		// scoprire perche` per file con nome diverso ma titolo uguale o molto simile non viene trovata alcuna ambiguita`', inoltre fare in modo che se il titolo e` quasi = a quello presente venga
		// mantenuto il vecchio (questo nel pannello di inserimento

		// Analisi di similarita` tra il titolo in argomento e i titoli dei file trovati.
		byte[] bytesForCompare = ((name + title).toLowerCase()).getBytes();
		if(rs.next()) {
			fBean = new FileInfoRecord();
			fBean.nameNoExtension = rs.getString(1);
			fBean.estensione = rs.getString(2);
			fBean.title = rs.getString(3);
			fBean.ownerID = rs.getInt(4);
			fBean.ownerUserName = rs.getString(5);
			fBean.genericInformation = new Float(simcheckMax);
		}
		while(rs.next()) {
			simcheck = Compare.similarityFunction(((rs.getString(1) + rs.getString(3)).toLowerCase()).getBytes(), bytesForCompare, 6);
			if(simcheck > simcheckMax) {
				simcheckMax = simcheck;
				fBean.nameNoExtension = rs.getString(1);
				fBean.estensione = rs.getString(2);
				fBean.title = rs.getString(3);
				fBean.ownerID = rs.getInt(4);
				fBean.ownerUserName = rs.getString(5);
				fBean.genericInformation = new Float(simcheckMax);
			}
		}

		if(simcheckMax > localProp.getFloatPar("AmbiguityChkToll") && simcheckMax < 0.999) return fBean;
		else return null;
	}

	/**
	 * Inizializza le tabelle dei filtri inserendo tutte le stop word conosciute per ogni lingua che vengono lette dai relativi file nella directory stopWordsFile
	 * 
	 * @param name
	 *            Nome Utente
	 * @param password
	 *            Password
	 * @return Numero di righeinserite;
	 */
	public int initialyzeStopWordsLists(String name, String password) throws ValidationException, IOException, ElabException {
		int userID;
		if(!password.equals("ADMIN")) userID = verifyUserPassword(name, password);
		else throw new ValidationException("Only ADMIN user can do this");
		int insertedRows = 0;
		// int failedInsertion=0;

		String dir = localProp.get("StopWordsFilesDir");
		if(dir == null) throw new IOException("StopWordsFilesDir parameter not defined in properties.prop");
		File swdir = new File(dir);
		if(swdir.isDirectory()) {
			File[] filesList = swdir.listFiles();
			if(filesList != null) {
				BufferedInputStream bif;
				StringBuffer tmpStr = new StringBuffer();
				char ch;
				try {
					Statement stat = conn.getStat(localProp.get("connString"), localProp.get("user"), localProp.get("password"));
					stat.executeUpdate("delete from filters_descr");
					// stat.executeUpdate("delete from FILTERS_WORDS");//Se il DB e` ben impostato con la cancellazione in cascata della foreign key questa si puo` evitare.
					PreparedStatement ps1 = conn.getPstat("insert into filters_descr (DESCR,USER,LINGUISTIC) values(?,?,?)", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
					PreparedStatement ps2 = conn.getPstat("insert into filters_words (DESCR,WORD,RELEVANCE) values(?,?,?)", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
					PreparedStatement ps3 = conn.getPstat("select max(ID) from filters_descr", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
					ResultSet rs;
					int filtersDescr = 0;
					for(int i = 0; i < filesList.length; i++) {
						if(filesList[i].isFile()) {
							bif = new BufferedInputStream(new FileInputStream(filesList[i]));

							ps1.setString(1, filesList[i].getName());
							ps1.setInt(2, userID);
							ps1.setInt(3, 1);
							ps1.executeUpdate();

							rs = ps3.executeQuery();
							if(rs.next()) {
								filtersDescr = rs.getInt(1);
							}

							while(bif.available() > 0) {
								ch = (char) bif.read();
								if(ch != '\n') tmpStr.append(ch);
								else {
									try {
										if(tmpStr.length() > 0) {
											ps2.setInt(1, filtersDescr);
											ps2.setString(2, tmpStr.toString().toLowerCase());/*
																								 * Anche se alcune parole specifiche di una lingua richiedono di essere scritte in maiuscolo (vedi I in
																								 * inglese) qui le volgo tutte al minuscolo per aumentare, come al solito, la tolleranza al confronto.
																								 */
											ps2.setFloat(3, 1);
											ps2.executeUpdate();
											insertedRows++;
										}
									}
									catch (SQLException e) {
										//Commentata così continua l'inserimento anche se si dovessero incontrare duplicati (i file di stop-words non sono perfetti......). In tal modo si evita di fare query o stored procedure per verificare la presenza o meno di una parola per un determinato filtro che risuktano comqune più lente rispetto al semplice catching di una exception per alcune parole.
										//throw new ElabException(e.getMessage());
									}
									tmpStr.setLength(0);
								}
							}
						}
					}
					stat.close();
					ps1.close();
					ps2.close();
					ps3.close();
				}
				catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new ElabException("Problems in inserting words: " + e.getMessage());
				}
			}
		}
		else {
			throw new ElabException(swdir.getPath() + " is not a directory");
		}
		return insertedRows;
	}

	/**
	 * Importantissimo chiamare questa alla chiusura del server altrimenti rimangono processi attivi. Ferma tutti gli eventuali thread di background (compresi quelli del searcher). Si puo' chiamare
	 * alla fine della sessione ma non appena un'altro client richiede la classe vengono reinizializzate tutte le classi serer con relativi processi di back ground. Normalmente un client non dovrebbe
	 * mai chiamarla a meno che l'accesso non sia stato effettuato da ADMIN. E' quindi il client che deve gestire il permesso.
	 * 
	 * @throws IOException
	 */
	public void close(int userreq, String passwordreq) throws ValidationException, IOException {
		PreparedStatement ps;
		if(activityClosed) return;
		try {
			if(localProp.get("version").equalsIgnoreCase("client-server")) {// Nel caso in cui la versione sia client-server, bisogna controllare che l'utente che richiede la chiusura sia proprio "admin".
				if(privateconn == null) privateconn = new db_con();
				try {
					ps = privateconn.getPstat("select * from utenti where ID=? and PASSWORD=?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
				}
				catch (IOException e) {
					throw new ServerException(e.getMessage() + "\nContact the administrator.");
				}
				ps.setInt(1, userreq);
				ps.setString(2, passwordreq);
				ResultSet rs = ps.executeQuery();
				if(rs.next()) {
					if(!rs.getString("USERNAME").equalsIgnoreCase("admin")) {
						//throw new ValidationException("Sezione server istanziata: solo l'utente ADMIN puo` effettuarne la chiusura delle operazioni.");
						return;
					}
				}
				else throw new ValidationException("Credenziali utente non valide per la chiusura del client.");
				rs.close();
				ps.close();
				privateconn.discon();// Va messa qui in quanto puo` succedere che essa non sia mai stata istanziata prima della chiamata della close() ed in tal caso la close() ne istanzia una
										// nuova che viene utilizzata solo al suo interno.
				privateconn = null;
			}

			if(searcher != null) searcher.close();// Il metodo e' bloccante sino a che TUTTI thread coinvolti non sono conclusi.
			if(inserter != null) inserter = null;
			if(searcher != null) searcher = null;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		activityClosed = true;
		System.out.println("Server Section Closing request OK");
		// if(localProp.get("version").equals("multi")) System.exit(0);//Se il client gira in una JVM separata dovro' fermare forzatamente la JVM del server.
		// Meglio trovare un modo piu' "dolce" di arrestare il server in quanto thread ancora attivi in questo modo vengono bruscamente interrotti.
	}

	/**
	 * Viene chiamata dal servizio registry (il server) per controllare se la classe (praticamente il server Documat) si può disistanziare. 
	 * @return
	 */
	public boolean CanDeistantiate() {
		//System.out.println("GlobalCollector.activityClosed = " + activityClosed);
		return activityClosed;
	}
	
	/**
	 * Data una lista di indici corrispondenti agli indici di altrettante liste di "cutting words" ritorna un array di stringhe formato dall'unione di degli insiemi di "cutting words" (quindi ogni
	 * parola e` presa una sola volta).
	 * 
	 * @param cuttingIndex
	 * @return
	 */
	public static String[] getCuttingWordsList(int[] cuttingIndex) {
		// Riempimento dell'array cuttedWords.
		if(cuttingIndex == null || cuttingIndex.length == 0) return null;
		String cuttingWords[] = null;
		ResultSet rs;
		try {
			Statement stat = conn.getStat(localProp.get("connString"), localProp.get("user"), localProp.get("password"));

			StringBuffer sql = new StringBuffer();
			sql.append("select distinct count(*) as conteggio from filters_words where");
			String or = null;
			int i;
			for(i = 0; i < cuttingIndex.length; i++) {
				if(or != null) sql.append(or);
				sql.append(" DESCR = ");
				sql.append(cuttingIndex[i]);
				if(or == null) or = " or";
			}
			rs = stat.executeQuery(sql.toString());
			sql.setLength(0);
			if(rs.next()) {
				cuttingWords = new String[rs.getInt("conteggio")];
				sql.append("select distinct * from filters_words where");
				or = null;
				for(i = 0; i < cuttingIndex.length; i++) {
					if(or != null) sql.append(or);
					sql.append(" DESCR = ");
					sql.append(cuttingIndex[i]);
					if(or == null) or = " or";
				}
				rs = stat.executeQuery(sql.toString());
				i = 0;
				while(rs.next()) {
					cuttingWords[i++] = rs.getString("WORD");
				}
			}
			rs.close();
			stat.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return cuttingWords;
	}

	/**
	 * Ritorna un vettore delle possibili parole significative ricavate dal file che si sta "tentando" di inserire. Non richiede particolari misure di sicurezza.
	 * 
	 * @param accessKey
	 * @param languages
	 * @return
	 * @throws ElabException
	 * @throws ValidationException
	 */
	public Vector<String> extractSigWordsList(long accessKey, int[] languages) throws ElabException, ValidationException {
		return inserter.extractSigWordsList(accessKey, languages);
	}

	public Vector<FileInfoRecord> findEementsByFileName(String fileNameNoExtension) throws ElabException {
		String sql = "select FI.NOMEFILE, FI.DESCRIZIONE, FI.OWNER_ID, U.IDENTIFIER from fileinfo FI inner join utenti U on U.ID = FI.OWNER_ID where FI.NOMEFILE = ? and FI.VERSIONE = (select max(VERSIONE) from fileinfo FI1 where FI1.NOMEFILE = FI.NOMEFILE and FI1.ESTENSIONE = FI.ESTENSIONE and FI.DESCRIZIONE = FI1.DESCRIZIONE and FI1.OWNER_ID = FI.OWNER_ID)";
		PreparedStatement ps;
		Vector<FileInfoRecord> ret = new Vector<FileInfoRecord>();
		FileInfoRecord tempIR;
		try {
			ps = conn.getPstat(sql, localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			// ps.setInt(1, cncptGroup);
			ps.setString(1, fileNameNoExtension);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				tempIR = new FileInfoRecord(null, rs.getString("NOMEFILE"), null, rs.getString("DESCRIZIONE"), null, null, new Integer(rs.getInt("OWNER_ID")), null, null, null);
				tempIR.ownerIdentifier = rs.getString("IDENTIFIER");
				ret.add(tempIR);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new ElabException("I can't do the searching:" + e.getMessage());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public FileInfoRecord getAllElementInformation(String fileName, String title, int ownerID) throws ElabException {
		Vector<Integer> grpCncpts = null;
		Vector<String> sigWords = null;
		FileInfoRecord fir = null;
		String sql = "select * from fileinfo FI, utenti U " + "where U.ID = FI.OWNER_ID and FI.NOMEFILE = ? " + "and lower(FI.DESCRIZIONE) = lower(?) " + "and FI.OWNER_ID = ? " + "and FI.VERSIONE = ( "
				+ "    select max(VERSIONE) from fileinfo FI1 " + "    where FI1.NOMEFILE = FI.NOMEFILE " + "    and FI1.DESCRIZIONE = FI.DESCRIZIONE " + "    and FI1.OWNER_ID = FI.OWNER_ID) ";
		PreparedStatement ps;
		try {
			ps = conn.getPstat(sql, localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			ps.setString(1, fileName);
			ps.setString(2, title);
			ps.setInt(3, ownerID);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				fir = new FileInfoRecord();
				fir.dbID = rs.getInt("ID");
				fir.title = title;
				fir.ownerIdentifier = rs.getString("IDENTIFIER");
				fir.data = rs.getDate("DATA");
				fir.versione = rs.getInt("VERSIONE");
				fir.nameNoExtension = fileName;
				fir.estensione = rs.getString("ESTENSIONE");
				fir.arch_path = rs.getString("ARCH_PATH");
				fir.lingua = rs.getInt("LANGUAGE_FILTER");
				fir.origPath = rs.getString("ORIG_PATH");
				fir.ownerID = ownerID;
				fir.instant = rs.getDate("INSERTION_DATE").getTime();
			}
			else {
				ps.close();
				return null;
			}

			sql = "select * from grp_cncpt_files where FILE = ?";
			ps = conn.getPstat(sql, localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			ps.setInt(1, fir.dbID);
			rs = ps.executeQuery();
			grpCncpts = new Vector<Integer>();
			while(rs.next()) {
				grpCncpts.add(rs.getInt("GRP_CNCPT"));
			}
			ps.close();
			fir.groupsList = grpCncpts;

			sql = "select GW.WORD from file_sig_words FW, grp_cncpt_words GW where FILE_ID = ? and FW.WORD_ID = GW.ID";
			ps = conn.getPstat(sql, localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			ps.setInt(1, fir.dbID);
			rs = ps.executeQuery();
			sigWords = new Vector<String>();
			while(rs.next()) {
				sigWords.add(rs.getString(1));
			}
			ps.close();
			fir.sigWords = sigWords;

		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new ElabException("I can't do the searching:" + e.getMessage());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return fir;
	}


	public FileContent getFile(String userreq, String passwordreq, int ID) throws ServerException, ValidationException {
		FileContent fileContent = null;
		try {
			verifyUserPassword(userreq, passwordreq);
			String SQL = "select arch_path, nomefile, estensione from fileinfo where ID = ?";
			PreparedStatement ps = conn.getPstat(SQL, localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			ps.setInt(1, ID);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				fileContent = new FileContent(new File(rs.getString(1) + "/" + rs.getString(2) + "." + rs.getString(3)));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new ServerException(e.getMessage()); 
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new ServerException(e.getMessage()); 
		}
		
		return fileContent;
	}
}
