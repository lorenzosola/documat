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

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import customobj.containers.IntegerToObjectSimpleNode;
import customobj.wrappers.AutoFileBuff;
import customobj.wrappers.DangerousOperationException;
import customobj.wrappers.LocalProp;
import customobj.wrappers.db_con;

/*
 * Created on Nov 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * Questo oggetto fa da tramite a tutte le ricerche contemporanee di piu' utenti e ne deve essere istanziato SOLO UNO. All'istanziazione il thread di controllo parte automaticamente ma puo' essere fermato dall'esterno tramite la haltTread(). Nel caso in cui esso non sia in esecuzione e' necessario che l'oggetto detentore chiami la funzione start() per poter eseguire una ricerca. La funzione di
 * ricerca non e' bloccante ed i risultati delle ricerche vengono ottenuti trmite le funzione getResults() (vedere). Il timeout di default per i processi di ricerca e' di 2 ore ma si puo' reimpostare con la setSrcTimeout(). Alla chiamata della getResponses(.) (vedere) puo' essere ritornato un Classific o un ElabException contenente il messaggio di segnalazione/errore.
 * 
 * @author lsola
 */
class SearchDispatcher extends Thread {
	private Hashtable<String, AutoFileBuff> afBuffers = new Hashtable<String, AutoFileBuff>();
	static int actualthreads = 0;
	private int maxthreads;
	boolean bgThreadActive = true;
	private db_con conn;// Gestisce il tutto con una sola connessione evitando al server di DBMS di creare piu' thread. Soluzione rischiosa ma si puo' pensare ad un meccanismo che ricrea la connessione appena si verificano problemi.
	private Hashtable<Long, Object> responses = new Hashtable<Long, Object>();
	final Thread mainThread = this;
	private IntegerToObjectSimpleNode searchManagerTree; // Vettore di thread creati dalla funzione search(....). Ogni thread inserito si occupa della gestione di una ricerca )quindi crea, a sua volta, un tread per ogni file da analizzare).
	private long timeout;// Per protezione se un processo id ricerca dura piu' di 1 ora il corrispondente searchManager viene eliminato (chiamata la destroy() del Thread.
	private LocalProp localProp;
	private long afbPersistenceTime;

	/**
	 * @param maxthreads
	 *            indica quanti threads contemporanei possono essere istanziati dal Dispatcher durante le ricerche.
	 * @throws IOException
	 *             mainly for errors in finding/reading the properties.prop file.
	 */
	SearchDispatcher(int maxthreads, db_con conn) throws IOException {
		localProp = new LocalProp("properties.prop");
		this.maxthreads = maxthreads;
		searchManagerTree = new IntegerToObjectSimpleNode(localProp.getIntPar("SimActiveRequests"));// Albero contenente tutte le richieste.
		timeout = localProp.getIntPar("SearchManagerTimeout");
		this.start();
		this.conn = conn;
		afbPersistenceTime = localProp.getIntPar("AutoFileBuffersPersistence");
	}

	/**
	 * Ferma il thread di monitoraggio dei thread di ricerca, la creazione di nuovi thread di ricerca ed attente che tutti i thread siano conclusi. Una volta chiamata questa funzione non e' piu' possibile effettuare alcuna ricerca e quelle gia' in corso abortiscono. Infatti il thread principale di questa classe serve ad alcune funzioni indispensabili, anche se indirettamente, al corretto
	 * funzionamento del dispathcer (per esempio la rimozione degli AutoFileBuff non piu' utilizzati). Per ripristinare l'attivita' della classe e' necessario chiamare la start(). Il metodo e' bloccante sino a che TUTTI thread coinvolti non sono conclusi.
	 */
	void close() {
		bgThreadActive = false;
		while(searchManagerTree.belowSize() != 0) { // Da quando dichiaro bgThreadActive = false; il task di background richiede l'arresto "soft" dei thread dei SearchManager e non conclude sinoa a che non sono tutti conclusi. Lo stato e' quindi raggiunto quando si ha searchManagerTree vuoto.
			try {
				Thread.sleep(250);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void run() {
		// Effettua ad intervalli relativamente lunghi un ciclo che cerca gli AutoFileBuff allocati e non piu' utilizzati.
		// i quali vengono deallocati ed tolti dalla HashMap.
		Set set;
		Iterator iter;
		Object key;
		bgThreadActive = true;
		Vector<IntegerToObjectSimpleNode> listaSearchManager = new Vector<IntegerToObjectSimpleNode>();
		AutoFileBuff tmpAfb;
		long lifeTime;//Utilizzata come variabile temporanea per misurare il tempo di vita dei SearchManager.
		while(bgThreadActive || searchManagerTree.belowSize() != 0) {
			try {
				Thread.sleep(3000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}

			synchronized (afBuffers) {
				set = afBuffers.keySet();
				iter = set.iterator();
				while(iter.hasNext()) {
					key = iter.next();
					tmpAfb = (AutoFileBuff) afBuffers.get(key);
					if(tmpAfb.nullable() && tmpAfb.getOldTime() > afbPersistenceTime) {
						afBuffers.remove(key);
						iter = set.iterator();// Perche' quando rimuovo dalla Hashtable cambia il Set e dunque l'Iterator emette l'eccezione CurrentIteratorException.
					}
				}
			}

			// Controllo dello stato dei srcManager (attivita' e timeout);
			searchManagerTree.fillVectorDisc(listaSearchManager);
			IntegerToObjectSimpleNode snTemp;// Pur appartenendo ad un albero questo rappresentera` il SimpleNode contenente UN particolare SearchManager.
			SearchManager smTemp;
			for(int i = 0; i < listaSearchManager.size(); i++) {
				snTemp = (IntegerToObjectSimpleNode) listaSearchManager.get(i);
				smTemp = (SearchManager) snTemp.getValue();
				lifeTime = System.currentTimeMillis()-snTemp.getIndex();//DEBUG
				//System.out.println(lifeTime);//DEBUG
				//System.out.println(timeout);//DEBUG
				if(!smTemp.isAlive() || smTemp.isInterrupted() || smTemp.isStopped()) {
					System.out.println("Eliminazione del SearchManager concluso con chiave " + ((SearchManager) snTemp.getValue()).resultKey + " dalla lista degli attivi.");
					snTemp.remove(snTemp.getIndex());//Eliminazione dal vettore 
				}
				else if(lifeTime > timeout || !bgThreadActive) {//Arresto del SearchManager per timeout oppure per perche` e` arrivata una richiesta diarresto del SearchDispachter da parte di un oggetto del pacchetto "archmanagement" per ora si prevede sia solo il GlobalCollector. 
					System.out.println("Eliminazione del thread del SearchManager con chiave " + ((SearchManager) snTemp.getValue()).resultKey + " per timeout ed eliminzaione dello stesso dalla lista degli attivi.");
					responses.put(new Long(((SearchManager) snTemp.getValue()).resultKey), new ElabException("Ricerca abortita per timeout."));// In ta modo il chiamante della getResponses(.) capisce che l'elaborazione e' conclusa senza risultati.
					smTemp.ferma();// Questa e' bloccante sino a che tutti i thread di ricerca del Search manager in oggetto non siano conclusi.
					smTemp.interrupt();
					snTemp.remove(snTemp.getIndex());// Viene rimosso dal tree contenente i thread monitorati.
					snTemp = null; // Dopo questa assegnazione solo listaSearchManager dovrebbe contenere la reference a tale thread ma esso verra` ripulito sotto.
				}
			}
			// Ora devo forzare lo svuotamento del vettore perche' l'operazione searchManagerTree.fillVectorDisc(listaSearchManager) non fa altro che AGGIUNGERE elementi....
			listaSearchManager.clear();
			// A questo punto non dovrebbe esserci nessuna reference per i thread fermati e dal momento che non vi sono monitor allocati su di esso la JVM dovrebbe eliminarlo....
		}

	}

	/**
	 * Elabora la classifica di una serie di files ottenuti dal DB tramite una query compatibile basandosi sulla frase passata tra gli argomenti. La query compatibile consiste in una select che ritorna una riga per ogni file contenente almeno le colonne: <BR>
	 * -ID: descrizione del contenuto inserita dall'utente. Puo' essere una stringa vuota o null. <BR>
	 * -NOME_FILE: data di inserimento del file nel sistema. <br>
	 * -ESTENSIONE: percorso in cui si puo' raggiungere il file di testo (ed anche il documento). <br>
	 * -DESCRIZIONE: a cui appartiene il file. <br>
	 * -ARCH_PATH: del file. <br>
	 * La chiamata implica la creazione di un Thread denominato SearchManager che si occupa, a sua volta di creare un Thread per ogni file su cui effettuare la ricerca. Vengono istanziati nuovi thread di ricerca solo se il numero di quelli gia' presenti non supera il numero massimo dichiarato. Diversamente viene controllata in continuazione la lista locale dei processi alla ricerca di quelli
	 * conclusi interponendo una attesa di un certo numero di millisecondi da un controllo all'altro. Come intuibile la funzione NON E' BLOCCANTE ed i risultati possono essere prelevati in modo asincrono tramite la funzione getResponses();
	 * Nel caso venga fatta una richiesta con la stessa chiave di accesso di una ricerca ancora in atto, quest'ultima viene fermata e sostituita da quella piu` recente.
	 * @param queryString
	 * @param phrase
	 *            frase che definisce l'argomento cercato.
	 * @param nresults
	 *            numero massimo di elementi che potra' contenere la classifica elaborata.
	 * @param nphrases
	 *            dimensione dell'accorpamento in numero di frasi durante l'analisi.
	 * @param resultKey
	 *            un intero che viene utilizzato per ottenere il risultato dell'elaborazione tramite la funzione getResponses(long resultKey) (vedere).
	 * @param cuttingIndex
	 *            Indice utilizzato per ottenere le cuttedWords dalla tabella FILTERS_WORDS.
	 */
	void search(final String queryString, final String phrase, final int nresults, final int nphrases, final long resultKey, int[] cuttingIndex) throws DangerousOperationException {
		//Nel caso venga fatta una richiesta con la stessa chiave di accesso di una ricerca ancora in atto, quest'ultima deve venire fermata e sostituita da quella piu` recente. 
		removeResponse(resultKey);
		
		//Ora si puo` procedere.
		String queryLowerCase = queryString.toLowerCase();
		if(queryLowerCase.contains("insert") || queryLowerCase.contains("into") || queryLowerCase.contains("delete") || queryLowerCase.contains("update")) {
			throw new DangerousOperationException("Security problem in table initialization: the specified query is not allowed.");
		}
		SearchManager srcManager = new SearchManager(queryString, phrase, nresults, nphrases, resultKey, cuttingIndex);
		srcManager.start();
		try {
			Thread.sleep(100);// Per diminuire la prob. che il thread di controllo (Thread di SearchDispatcher) analizzi lo stato del processo quando non e' ancora in run (in tal caso lo eliminerebbe).
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		searchManagerTree.append(System.currentTimeMillis(), srcManager, false);
	}

	/**
	 * Restituisce un Classific contenente una lista di SimpleNode che costituiscono la risposta alla chiamata della funzione search(.....). Ogni SimpleNode della risposta ha come valore (getValue()) un oggetto FileInfoRecord con tutte le info di un file e come chiave (getIndex()) il punteggio di affinita' ottenuto dal file con la stringa passata alla chiamata della funzione search(.....) di
	 * questo oggetto. La lista dei SimpleNode e' ordinata dal file con punteggio piu' alto a quello con punteggio piu' basso ed il numero massimo dei nodi e' chiaramente limitato dal valore del parametro "nresult" nella chiamata a search(.....). La funzione non cancella i risultati e quindi essi possono essere riottenuti tramite le stesse chiamate fino a che non vengono eliminati tramite la
	 * removeResponse(long accessKey).
	 * 
	 * @param resultKey
	 *            codice che contraddistingue il risultato della ricerca avente lo stesso codice (vedere chiamata alla search(.....)).
	 * @return Classific con i SimpleNode della classifica. Se non sono stati trovati files dalla query della search(....) il Classific e' vuoto. Se la risposta e' null significa che la ricerca e' ancora in atto (e bisogna ritentare successivamente) oppure che non e' mai stata iniziata una ricerca con quel codice oppure che il risultato e' stato rimosso con la removeResponse(long accessKey).
	 * @throws ElabException
	 *             contenente il messaggio di segnalazione/errore per cui la ricerca e' abortita.
	 */
	Classific getResponses(long resultKey) throws ElabException {
		Object obj = responses.get(new Long(resultKey));
		if(obj instanceof ElabException) throw (ElabException) obj;// E' sottointeso che se l'oggetto trovato non e' un Classific sia un ElabException.
		else return (Classific) obj;
	}

	/**
	 * Rimuove il risultato con chiave specificata. Se il risultato non esiste perche' l'elaborazione di ricerca deve ancora concludere verra' fermata l'elaborazione.
	 * 
	 * @param resultKey
	 */
	void removeResponse(long resultKey) {
		Vector<IntegerToObjectSimpleNode> listaSearchManager = new Vector<IntegerToObjectSimpleNode>();
		searchManagerTree.fillVectorDisc(listaSearchManager);
		SearchManager searchManagerTemp;
		for(int i = 0; i < listaSearchManager.size(); i++) {
			searchManagerTemp = (SearchManager) ((IntegerToObjectSimpleNode) listaSearchManager.get(i)).getValue();
			if(searchManagerTemp.resultKey == resultKey) {
				System.out.println("Eliminazione risposta con chiave " + resultKey);
				searchManagerTemp.ferma();
				searchManagerTemp.interrupt();
				// A fare pulizia nel searchManagerTree ci pensa il task periodico del SearchDispatcher.
			}
		}
		responses.remove(new Long(resultKey));
	}

	/**
	 * Di default il timeout per tutte le ricerche eseguite da SearchDispatcher e' di 1 ora. Tramite questa funzione e' possibile reimpostarlo arbitrariamente.
	 * 
	 * @param timeout
	 *            timeout in mSec.
	 */
	void setSrcTimeout(long timeout) {
		this.timeout = timeout;
	}

	class SearchManager extends Thread {
		String queryString, phrase;
		long resultKey;
		int nphrases, nresults;
		boolean stopping = false;// Se a false non vengono istanziati nuovi thread di ricerca e al termine dell'elaborazione non viene inserito il risultato nella "responses".
		boolean stopped = false;// Qaundo a true significa che tutti i thread di ricerca sono conclusi.
		int[] cuttingIndex;
		private Searcher searcher;
		private String[] cuttingWords;
		private File txtfile;
		private Vector<Searcher> searcherList = new Vector<Searcher>();
		private Classific cl;
		private PreparedStatement ps;
		private ResultSet rs;
		private ResultSetMetaData metadata;
		private AutoFileBuff afb;

		/**
		 * Istanzia un search manager la che usa la query passata come argomeno per ottenere l'insieme di file si cui deve essere effettuata la ricerca. Da notare che la query deve contenere necessariamente i campi NOMEFILE ESTENSIONE DESCRIZIONE ARCH_PATH ID
		 * 
		 * @param queryString
		 * @param phrase
		 * @param nresults
		 * @param nphrases
		 * @param resultKey
		 * @param cuttingIndex
		 */
		public SearchManager(String queryString, String phrase, int nresults, int nphrases, long resultKey, int[] cuttingIndex) {
			this.queryString = queryString;
			this.phrase = phrase.toLowerCase();// !!!!!!!!!!!!!!!!!IMPORTANTE: il toLowerCase viene inserito per il fatto che i files di testo in archivio sono conservati in lower case e le procedure dui analisi nel substrato non effettuano conversioni in tal senso!!!!!!!!!!!!!!!
			this.nphrases = nphrases;
			this.resultKey = resultKey;
			this.nresults = nresults;
			this.cuttingIndex = cuttingIndex;
			this.cuttingWords = GlobalCollector.getCuttingWordsList(cuttingIndex);
			this.cl = new Classific(nresults, phrase);// Istanzio il Classific che a fine elaborazione, conterra' la classifica dei files piu' affini all'argomento cercato.
		}

		public void run() {
			// Ottengo la lista dei files di testo. N.B. nel DB i nomi dei files NON DEVONO AVERE ESTENSIONE (OPPURE SE HANNO UNA ESTENSIONE ESSA DEVE FAR PARTE DEL NOME PRE-ESTENSIONE).
			try {
				// Esecuzione della query passata come argomento e controllo della sua "compatibilia".
				try {
					ps = conn.getPstat(queryString, localProp.get("connString"), localProp.get("user"), localProp.get("password"));
				}
				catch (IOException e) {
					e.printStackTrace();
					return;
				}
				rs = ps.executeQuery();
				metadata = rs.getMetaData();
				int n = 0;// Se alla fine del controllo e' = al numero di colonne cercate si puo' dire OK (controllo un po approssimativo ma statisticamente valido).
				String tmp;
				for(int i = 1; i <= metadata.getColumnCount(); i++) {
					tmp = metadata.getColumnName(i);
					if(tmp.equalsIgnoreCase("ID")) n++;// Campo contenente il percorso completo ed il nome del file.
					if(tmp.equalsIgnoreCase("NOMEFILE")) n++;// Campo contenente il nome del file ma SENZA ESTENSIONE.
					if(tmp.equalsIgnoreCase("ESTENSIONE")) n++;// Campo contenente l'estensione del file originale.
					if(tmp.equalsIgnoreCase("DESCRIZIONE")) n++;//Campo contenente uns breve descrizione come per esempio il titolo dell'elemento.
					if(tmp.equalsIgnoreCase("ARCH_PATH")) n++;// Campo contenente il percorso completo ed il nome del file.
				}
				if(n < 5) {
					System.err.println("SearchDispatcher: la query passata come argomento non fornisce un result-set compatibile in quanto mancano delle colonne.");
					responses.put(new Long(resultKey), new ElabException("Errore interno del server Documat: ricerca abortita."));// Devo comunque inserire un Classific, anche se vuoto, perche' in ta modo il chiamante della getResponses(.) capisce che l'elaborazione e' conclusa.
					rs.close();
					ps.close();
					//System.out.println("ResultSet closed 1");// DEBUG
					return;
				}

				// Scansione della lista dei files ottenuta dalla query. Se si trova un file non di testo (il cui nome non termina con.txt) significa che vi e' stata una manomisione esterna per cui lo si elimina.
				while(rs.next() && !stopping) {
					// Se il thread di questo stesso oggetto non e' stato avviato allora e' indispensabile farlo perche' sono contenute operazioni periodiche di gestione necessarie.
					if(!mainThread.isAlive()) {
						System.err.println("Il thread di SearchDispatcher e' stato fermato troppo presto per cui la ricerca si e' interrotta: restituito oggetto null come risposta.");
						responses.put(new Long(resultKey), new ElabException("Errore interno del server Documat: ricerca abortita."));
						rs.close();
						ps.close();
						//System.out.println("ResultSet closed 2");// DEBUG
						return;
					}

					while(actualthreads > maxthreads) {// Se vi sono ancora troppi thread l'azione ? quella di controllare e rimuove quelli morti.
						for(int k = 0; k < searcherList.size(); k++) {
							if(!((Searcher) searcherList.get(k)).isAlive()) {
								searcherList.remove(k);
								actualthreads--;
								k--;
							}
						}
						try {
							Thread.sleep(250);
						}
						catch (InterruptedException e) {
							rs.close();
							ps.close();
							e.printStackTrace();
							//System.out.println("ResultSet closed 3");// DEBUG
						}
					}

					txtfile = new File(rs.getString("ARCH_PATH") + "/" + rs.getString("NOMEFILE") + ".txt");
					if(!txtfile.canRead()) {
						System.err.println("SearchDispatcher: problema di lettura del file " + txtfile.getPath());
						continue;
					}

					// Ora controlla se non vi sia gia' un AutoFillBuffer per quel file: in caso affermatico utilizza quello altrimenti ne crea un'altro.
					// Da notare che l'ogetto istanziato viene deallocato automaticamnte dalla JVM solo qando piu' nessuno lo utilizza.
					synchronized (afBuffers) {
						if((afb = (AutoFileBuff) afBuffers.get(txtfile.getPath())) != null) {
							//System.out.println("Riutilizzato AutoFileBuffer per: " + txtfile.getPath()); //DEBUG
						}
						else {
							afb = new AutoFileBuff(txtfile);
							afBuffers.put(txtfile.getPath(), afb);// Inserisco l' afb per la condivisione con gli altri thread che chiamano search(....).
							//System.out.println("Istanziato nuovo AutoFileBuffer per: " + txtfile.getPath()); //DEBUG
						}
						afb.open();// Il close() viene chiamato dal searcher.
					}

					// String[] cuttedWords = {};//Per debug.

					try {
						metadata = rs.getMetaData();
						FileInfoRecord firec = new FileInfoRecord(
								rs.getInt("ID"),
								rs.getString("NOMEFILE"),
								rs.getString("ESTENSIONE"),
								rs.getString("DESCRIZIONE"),
								rs.getString("ARCH_PATH"));
						
						searcher = new Searcher(afb, cl, phrase, nphrases, firec, cuttingWords, localProp.get("DelimiterPattern"));		

						/*Per ora la lista di parole significatice non serve (quindi risparmio la query). 
						//Ottengo la lista di parole significative dell'ultimo file inserito. Devo farlo qui perche` posso usufruire dell'ID dell'ultimo file.
						firec.sigWords=new Vector<String>();
						PreparedStatement psSigWords = conn.getPstat("select GCW.WORD from FILE_SIG_WORDS FSW, GRP_CNCPT_WORDS GCW  where FSW.FILE_ID = ? and GCW.ID = FSW.WORD_ID", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
						psSigWords.setInt(1, rs.getInt("ID"));//Devo sempre ottenere l'ID dell'ultima versione pe ricavare dall'ultimo file inserito il set di parole significative
						ResultSet rsSigWords = psSigWords.executeQuery();
						while(rsSigWords.next()) {
							firec.sigWords.add(rsSigWords.getString(1));
						}*/
						firec.data = rs.getDate("DATA");
						for(int i=1; i<metadata.getColumnCount()+1; i++) {
							if(metadata.getColumnName(i).equals("OWNER_ID")) firec.ownerID = rs.getInt("OWNER_ID");
							if(metadata.getColumnName(i).equals("VERSIONE")) firec.versione = rs.getInt("VERSIONE");
							if(metadata.getColumnName(i).equals("NOMEFILE")) firec.nameNoExtension = rs.getString("NOMEFILE");
							if(metadata.getColumnName(i).equals("ESTENSIONE")) firec.estensione = rs.getString("ESTENSIONE");
							if(metadata.getColumnName(i).equals("ARCH_PATH")) firec.arch_path = rs.getString("ARCH_PATH");
							if(metadata.getColumnName(i).equals("LANGUAGE_FILTER")) firec.lingua = rs.getInt("LANGUAGE_FILTER");
							if(metadata.getColumnName(i).equals("ORIG_PATH")) firec.origPath = rs.getString("ORIG_PATH");
							if(metadata.getColumnName(i).equals("OWNER_ID")) firec.ownerID = rs.getInt("OWNER_ID");
							if(metadata.getColumnName(i).equals("INSERTION_DATE")) firec.instant = rs.getDate("INSERTION_DATE").getTime();
							if(metadata.getColumnName(i).equals("IDENTIFIER")) firec.ownerIdentifier = rs.getString("IDENTIFIER");
							
						}
						
						searcherList.add(searcher); // Deve tenere in lista tutti i searcher avviati per sapere quando la ricerca e' conclusa.
						System.out.println("Added a new Searcher to the searcherList with search Stirng = " + phrase);//DEBUG
					}
					catch (IOException e) {
						e.printStackTrace();
						rs.close();
						ps.close();
						//System.out.println("ResultSet closed 4");// DEBUG
						return;
					}

					actualthreads++;
				}
				rs.close();
				ps.close();
				//System.out.println("ResultSet closed 5");// DEBUG
			}
			catch (SQLException e1) {
				e1.printStackTrace();
				responses.put(new Long(resultKey), new ElabException("Errore interno del server Documat: ricerca abortita."));// Devo comunque inserire un Classific, anche se vuoto, perche' in tal modo il chiamante della getResponses(.) capisce che l'elaborazione e' conclusa.
				return;
			}

			// Una volta istanziati tutti i Searcher necessari (anche se nel vettore ne sono presenti al massimo maxthreads) si attende che siano tutti conclusi per ritornare il risultato.
			boolean searchEnded = false;
			while(!searchEnded) {
				//System.out.println("Waiting for Searchers to end");//DEBUG
				searchEnded = true;
				try {
					Thread.sleep(250);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				// Ora ricerca almeno un Searcher attivo nel qual caso dichiara non conclusa la ricerca.
				for(int i = 0; i < searcherList.size(); i++) {
					if(((Searcher) searcherList.get(i)).isAlive()) {
						searchEnded = false;
					}
					else {
						searcherList.remove(i);
						actualthreads--;
						i--;
						//System.out.println("Remove of a died Searcher from the list");//DEBUG
					}
				}
			}

			// A questo punto la ricerca e' conclusa quindi vengono stampati i risultati (solo per DEBUG).
			// for(int i = 0; i < classifica.size(); i++) {
			// System.out.println(((FileInfoRecord) classifica.get(i)).value + " " + ((FileInfoRecord) classifica.get(i)).index);
			// }
			// return classifica;
			if(!stopping) responses.put(new Long(resultKey), cl);// Inserisco il risultato della ricerca (un Classific).
			stopped = true;
			//System.out.println("Search manager ended");//DEBUG
		}

		/**
		 * Ferma "dolcemente" l'elaborazione. Ritorna solo quando tutti i thread di questa ricerca sono conclusi.
		 */
		public void ferma() {
			stopping = true;
			while(stopped != true) {
				try {
					Thread.sleep(250);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * @return true se Il SearchManager e' stato fermato tramite la ferma().
		 */
		public boolean isStopped() {
			return stopped;
		}

		/**
		 * @return true se Il SearchManager si sta fermando per richiesta esplicita.
		 */
		public boolean isStopping() {
			return stopping;
		}

	}

}