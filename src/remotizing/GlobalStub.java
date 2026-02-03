package remotizing;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JTextArea;

import arcmanagement.Classific;
import arcmanagement.ElabException;
import arcmanagement.FileInfoRecord;
import arcmanagement.ValidationException;

import customobj.containers.ObjToInt;
import arcmanagement.FileContent;

public interface GlobalStub extends Remote {

	/**
	 * Verify user and password for a client.
	 * 
	 * @param name this will be always turned to upper-case mode.
	 * @param password
	 * @return user ID.
	 * @throws ValidationException
	 * @throws IOException
	 */
	public int verifyUserPassword(String name, String password) throws ValidationException, IOException, RemoteException;

	/**
	 * Inserisce un nuovo utente. Solo l'lutente ADMIN puo effettuare questa operazione. Se l'utente e la password non coincidono con ADMIN e relativa password il metodo ritorna un messaggio di errore
	 * e non effettua alcuna transazione sul DB. Il campo USER e' sempre case insensitive mentre la password e' case sensitive. Se neanche l'utente ADMIN e' presente allora deve essere inserito
	 * tramite questo stesso metodo passand ocome parametro passreq la striga admin.
	 * 
	 * @param userreq L'utente che vuole effettuare l'operazione.
	 * @param passwordreq La password dell'utente che vuole effettuare l'operazione.
	 * @param user L'utente che si vuole inserire. User is always turne to upper-case mode.
	 * @param password password dell'utente che si vuole inserire.
	 * @param identif Identificativo dell'utente che si sta inserendo (es. Nome Cognome).
	 * @throws ValidationException Scaturita ogni volta che la validazione (in senso generico) non va a buon fine.
	 * @throws SQLException
	 * @throws ServerException
	 */
	public void insUser(String userreq, String passwordreq, String user, String password, String identif, String telephone, String address) throws ValidationException, SQLException, ServerException, RemoteException;

	/**
	 * Funzione per cambio password degli utenti.
	 * 
	 * @param userreq
	 * @param oldpassword
	 * @param newpassword
	 * @throws ValidationException Scaturita ogni volta che la validazione (in senso generico) non va a buon fine.
	 * @throws SQLException
	 * @throws ServerException
	 */
	public void chPasswd(String userreq, String oldpassword, String newpassword) throws ValidationException, SQLException, ServerException, RemoteException;

	/**
	 * Eliminazione di un utente, ovviamente la politica di eliminazione per tutte le informazioni contenute nel DB, e inserite proprio da quell'utente, dipende dalle regole di inferenza dichiarate
	 * sul DB.
	 * 
	 * @param userreq utente che richiede la eliminazione (puo' essere solo admin).
	 * @param passwordreq utente che richiede la eliminazione.
	 * @param user utente da eliminare. this is always turned to upper-case mode.
	 * @throws ValidationException Scaturita ogni volta che la validazione (in senso generico) non va a buon fine.
	 * @throws SQLException
	 * @throws ServerException
	 */
	public void delUser(String userreq, String passwordreq, String user) throws ValidationException, SQLException, ServerException, RemoteException;

	/**
	 * Ritorna un Vector contenente i "group-concept" cioe' i concetti che accomunano i files contenuti del gruppo. Ogni group-concept definisce un gruppo di files accomunati dal concetto (descritto
	 * nel DB). Un file puo' appartenere a piu' "group-concept" (a piu' gruppi). <br>
	 * I campi contenuti nel ResultSet richiesto al DB sono: <br>
	 * ID=identificatore numerico. DESCR=descrizione del "group concept" USER=utente che ha inserito il group-concept <br>
	 * e devono essere contenuti nella tabella GRP_CNCPT che deve essere presente nel DB.
	 * 
	 * @return il Result Set con i "group concept" (vedi sopra). Ricordare di chiudere il rs a fine uso.
	 * @throws ServerException
	 */
	public Vector<ObjToInt<String>> getGrpCncptsList() throws ServerException, RemoteException;

	/**
	 * Ritorna un result set contenente le lingue registrate. I campi contenuti nel ResultSet richiesto al DB sono: <br>
	 * ID=identificatore numerico. DESCR=descrizione del "group concept" USER=utente che ha inserito il group-concept <br>
	 * e devono essere contenuti nella tabella GRP_CNCPT che deve essere presente nel DB.
	 * 
	 * @param linguistic Se devono solo essere selezionati i filtri linguistici.
	 * @return il Result Set con i "group concept" (vedi sopra). Ricordare di chiudere il rs a fine uso.
	 * @throws ServerException
	 */
	public Vector<ObjToInt<String>> getFiltersList(boolean linguistic) throws ServerException, RemoteException;

	/**
	 * Da chiamare ad ogni richiesta per ottenere la accessKey
	 * 
	 * @param user
	 * @param password
	 * @return la accessKey
	 */
	public long getNewAccessKey(int user, String password) throws RemoteException;

	public Vector<FileInfoRecord> conversion(FileContent fContent, long accessKey, int user, String password) throws ValidationException, ElabException, ServerException, RemoteException;

	public Vector<FileInfoRecord> conversion(FileContent fContent, JTextArea text, long accessKey, int user, String password) throws ValidationException, ElabException, ServerException, RemoteException;

	public String example(long accessKey, int user, String password) throws ValidationException, RemoteException;

	public int insertFile(Vector<String> paroleSignificative, String titolo, int[] groupConceptList, long accessKey, int user, String password) throws ValidationException, ElabException, ServerException, RemoteException;

	public int insertFile(String titolo, Vector<String> paroleSignificative, int[] groupConceptList, int filtroLingua, long accessKey, int user, String password) throws ValidationException, ElabException, ServerException, RemoteException;

	public int findLanguage(long accessKey, int user, String password) throws Exception, RemoteException;

	public Classific getResponses(long accessKey, int user, String password) throws ValidationException, ElabException, RemoteException;

	public void removeResponse(long accessKey, int user, String password) throws ValidationException, RemoteException;

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
	 * @param cuttingIndex Questo deve essere di dafault l'indice del filtro lingua!!!!!!!!!!!!!!!!!!!!!!!!!!!!!.
	 * @param accessKey
	 * @param user
	 * @param password
	 * @throws ValidationException
	 */
	public void search(final String queryString, final String phrase, final int nresults, final int nphrases, int[] cuttingIndex, final long accessKey, int user, String password) throws ValidationException, RemoteException;

	/**
	 * Individua in automatico a quale gruppo concettuale, tra quelli presenti, e` piu` probabile che appartenga il documento che si sta inserendo.
	 * 
	 * @param accessKey
	 * @param user
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public int findGroupCncpt(long accessKey, int user, String password) throws Exception, RemoteException;

	/**
	 * Viene utilizzato dai client per ottenere tutti i TableDataManager di cui necessitano. In questo modo si passa attraverso i 2 stati (tramite le accessKey) che rendono l'istanziazione di ogni
	 * oggetto (remoto o locale) avente accesso al server (tutti e soli quelli utilizzati e forniti da GlobalCollector) piu` sicura. Used by the client to obtain all the TableDataManager that it needs
	 * (is the only one way to do it).
	 */
	//public TableDataManager getAClientTableDataManager(String[] headers, String[] dbFields, final long accessKey, int user, String password) throws ValidationException, RemoteException;

	/**
	 * Initializes the TableDataManager previously obtained throw the call of getAClientTableDataManager(....). By this strategy the client can't have any access to che DB Server and security is
	 * increased by the need of the accessKey in the in parameters (see the "Limited Life Time Access Key" strategy).
	 * 
	 * @param tdm TableDataManager to be initialized
	 * @param sqlStr the SQL string used by the tdm above to initialythe the data in it (seing the TableDataManager behaviour is reccomended).
	 * @param DBUser The user throw it the DBMS determines permission grants to the DB Objects.
	 * @param DBPassword The password for the user above specified.
	 * @param accessKey
	 * @param user The user name registeredd in the Documat validation table.
	 * @param password The password registeredd in the Documat validation table for the user specified.
	 * @throws SQLException
	 * @throws DangerousOperationException
	 * @throws IOException
	 * @throws ValidationException
	 */
	//public void initialyzeATableDataManager(TableDataManager tdm, String sqlStr, String DBUser, String DBPassword, long accessKey, int user, String password) throws SQLException, DangerousOperationException, IOException, ValidationException, RemoteException;

	/**
	 * Makes a join of 2 or more groups in the group having as ID the first in the passed list (must be a previuosly created group). Afted the join the description of the joining group is replaced
	 * with that specified in "description" parameter. The joined groups (that will be embpy) will be deleted.
	 * 
	 * @param grpJoined a list of ID corresponding with the ID of the groups which has to be joined.
	 * @param description final description of the joining group.
	 * @return the number of words moved from the joined groups into the joining group. -1 if the grpJoined size was < 2 (without sense operation).
	 * @throws SQLException
	 * @throws IOException
	 */
	public int joinCncptGroups(int[] grpJoined, String description, boolean deleteOldGroups) throws SQLException, IOException, RemoteException;

	/**
	 * Ritorna un FileInfoRecord nel caso in cui esista nel DB un file appartenente ad almeno uno dei gruppi concettuali passati, nome e titolo (concatenati) diversi ma con similarita` > ad un certo
	 * valore [0,1) rispetto a quello passato (quindi anche se =). Se tra i titoli scansionati non ve ne e` nessuno con il requisito di similarita` minima allora il valoredi ritorno e` null. Nel
	 * FileInfo Record restituito il valore alla variabile genericInformation viene assegnato un oggetto Float contenente l'indice di dimilarita` [0,1] del titolo.
	 * 
	 * @param name Il nome dell'elemento (in pratica il nome del file senza estensione).
	 * @param title Il titolo dell'elemento.
	 * @param grpCncptList La lista dei gruppi che, in totale o in parte, devono essere associati agli elementi perche` questi ultimi vengano considerati nella ricerca.
	 * @param minOfMembership Minimo numero di gruppi tra quelli specificati a cui il file deve appartenere.
	 * @return un FileInfoRecord che con buona probabilita`, sia semplicemente lo stesso documento di quello in oggetto ma a cui il proprietario ha dato un titolo leggermente differente; null nel caso
	 *         in cui nessun documento gia` archiviato abbia tali caratteristiche (significa che il documento di cui e` richiesto l'inserimento puo` effettivamente essere inserito).
	 * @throws SQLException
	 * @throws IOException
	 */
	public FileInfoRecord ambiguityCheck(String name, String title, int[] grpCncptList, int minOfMembership) throws SQLException, IOException, RemoteException;

	/**
	 * Inizializza le tabelle dei filtri inserendo tutte le stop word conosciute per ogni lingua che vengono lette dai relativi file nella directory stopWordsFile
	 * 
	 * @param name Nome Utente
	 * @param password Password
	 * @return Numero di righeinserite;
	 */
	public int initialyzeStopWordsLists(String name, String password) throws ValidationException, IOException, ElabException, RemoteException;

	/**
	 * Viene chiamata dal servizio registry (il server) per controllare se la classe (praticamente il server Documat) si può disistanziare.
	 * 
	 * @return
	 */
	public boolean CanDeistantiate() throws RemoteException;

	/**
	 * Ritorna un vettore delle possibili parole significative ricavate dal file che si sta "tentando" di inserire. Non richiede particolari misure di sicurezza.
	 * 
	 * @param accessKey
	 * @param languages
	 * @return
	 * @throws ElabException
	 * @throws ValidationException
	 */
	public Vector<String> extractSigWordsList(long accessKey, int[] languages) throws ElabException, ValidationException, RemoteException;

	public Vector<FileInfoRecord> findEementsByFileName(String fileNameNoExtension) throws ElabException, RemoteException;

	public FileInfoRecord getAllElementInformation(String fileName, String title, int ownerID) throws ElabException, RemoteException;

	/**
	 * Importantissimo chiamare questa alla chiusura del server altrimenti rimangono processi attivi. Ferma tutti gli eventuali thread di background (compresi quelli del searcher). Si puo' chiamare
	 * alla fine della sessione ma non appena un'altro client richiede la classe vengono reinizializzate tutte le classi serer con relativi processi di back ground. Normalmente un client non dovrebbe
	 * mai chiamarla a meno che l'accesso non sia stato effettuato da ADMIN. E' quindi il client che deve gestire il permesso.
	 * 
	 * @throws IOException
	 */
	public void close(int userreq, String passwordreq) throws ValidationException, IOException, RemoteException;

	/**
	 * Ritorna il file descritto dalla riga della tabella "fileinfo" con ID passato.
	 * 
	 * @param ID
	 * @return
	 * @throws ServerException
	 * @throws ValidationException
	 */
	public FileContent getFile(String userreq, String passwordreq, int ID) throws ServerException, RemoteException, ValidationException;
}