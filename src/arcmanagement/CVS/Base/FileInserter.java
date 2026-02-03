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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.TreeSet;
import java.util.Vector;

import java.util.Iterator;

import java.sql.Connection;

import analysis.TextSkimmer;

import customobj.containers.FloatToObjectSimpleNode;
import customobj.functions.Compare;
import customobj.functions.ComputationException;
import customobj.functions.GenericFunctions;
import customobj.wrappers.LocalProp;
import customobj.wrappers.db_con;

/**
 * Classe che fa da manager per gli inserimenti dei documenti/elementi. Il compito e' quello di provvedere alla creazione del file di testo descrittivo, all'inserimento dei 2 files (quello originale e l'estratto in formato testo) nella gerarchia e all'inserimento del record di descrizione nel DB. IMPORTANTISSIMO: di questa classe va istanziata una sola volta ogni sessione (quindi da quando si
 * avvia il programma server). A differenza delle classi per la ricerca questa dipende maggiormante dalla struttura del DB anche se ho cercato comunque di evitare per quanto possibile questo fattore. L'utilizzo di un database come archivio sia dei documenti sia delle informazioni aggiuntive porta a vantaggi in prestazioni. Con opportuni accorgimenti, pero', e' possibile riporre i contenuti in
 * cartelle del filesystem in modo da renderli piu' accessibili e facilitare l'archiviazione differenziale. La ricerca di un file all'interno di un direttorio in ext2/ext3 richiede la lettura ed il riconoscimento di tutti i nomi. Si consiglia pertanto o l'utilizzo di altri filesystem com per esempio XFS (migliori prestazioni ma meno maturo di ext2/ext3) oppure di riporre i files in un struttura di
 * direttori costituenti un albero. Opto per quest'utlima soluzione utilizzando come chiave di selezione dei nodi un numero da 0 a maxArchChild che andra' a costituire il nome del direttorio (in ogni direttorio ci possono essere al massimo maxArchChild sotto-direttori) in una gerarchia fissa di profondita' 4 (maxArchChild^4 direttori) in cui ogni direttorio foglia possa contenere maxArchChild
 * files (maxArchChild^5). Il db associera' le informazioni extra per ogni file ad un percorso della gerarchia. Lo riempimento avviene distribuendo uniformemente i files in tutti i direttori foglia in sequenza: due files inseriti nello stesso momento non si troveranno mai nello stesso direttorio. In questo modo sara' praticamente impossibile creare conflitti con i nomi e se questo avvenisse
 * bastera' cambiare direttorio. Prima dell'inserimento viene cercato nel DB il numero di versioni del medesimo file presenti simultaneamente nell'archivio ad albero. Se questo supera il parametro MaxHistory scritto nel file delle propriet?, il programma provvede ad eliminare il file con verione minore e a fare l'update al posto dell'insert sul DB. Da tutto questo si deduce che se il parametro
 * MaxHistory viene cambiato nel ciclo id vita dell'applicazione i dati rimangono coerenti: se il valore diminuisce ad inserimenti successivi del medesimo file le versioni presenti diminuiranno fino a raggiungere tale valore, se il parametro viene incrementato ad ogni inserimento aumenteranno le versioni simultane fino al suo raggiungimento, questo e' valido sia per il DB sia per l'archivio ad
 * albero.
 * 
 * @author lsola
 */
class FileInserter {
	private db_con conn;
	private FloatToObjectSimpleNode accessTableTxt;// Albero contenente tutte le richieste.
	private int DB_fileInfoID;// Contiene di volta in volta 'ID raggiunto in FILEINFO. Viene
	// inizializzata con il massimo trovato all'istanziazione di questo
	// oggetto dopo di che viene incrementata ad ogni inserimento da codice
	// controllato da un monitor.
	private int path1 = 0, path2 = 0, path3 = 0, path4 = 0;// Elementi del path nell'archivio ad
	// albero.
	private int maxHistory = 0;// Numero masimo di versioni di uno stesso file presenti nell'acrhivio (le piu'
	// vecchie vengono cancellate).
	private Vector<Long> inserSeqKey = new Vector<Long>();// Ad ogni inserimento in accessTable viene accodata la
	// accessKey in questo vettore. Periodicamente viene
	// controllato se la prima chiave esiste ancora nella
	// accessTable ed in caso positivo si controlla
	// l'anzianita' del FileInfoRecord ottenuto e se maggiore di un
	// certo valore si elimina il nodo contenente lo stesso
	// ed il valore all'interno di questo vettore.

	private LocalProp localProp = new LocalProp("properties.prop");

	FileInserter(db_con conn) throws IOException {
		this.conn = conn;
		// Ottengo L'ID da cui partire per l'inserimento.
		try {
			PreparedStatement ps = conn.getPstat("select max(ID) from FILEINFO", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			ResultSet rs = ps.executeQuery();
			if(rs.next()) DB_fileInfoID = rs.getInt(1);

			// Ottengo gli elementi del path da cui partire per l'inserimento nell'archvio ad albero.
			ps = conn.getPstat("select * from LASTPATH", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			rs = ps.executeQuery();
			if(rs.next()) {
				path1 = rs.getInt(1);
				path2 = rs.getInt(2);
				path3 = rs.getInt(3);
				path4 = rs.getInt(4);
			}
			else {
				ps = conn.getPstat("insert into LASTPATH (PATH1,PATH2, PATH3, PATH4) values(?,?,?,?)", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
				ps.setInt(1, 0);
				ps.setInt(2, 0);
				ps.setInt(3, 0);
				ps.setInt(4, 0);
				ps.execute();
				rs.close();
				ps.close();
				// Riscontro
				ps = conn.getPstat("select * from LASTPATH", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
				rs = ps.executeQuery();
				if(rs.next()) {
					path1 = rs.getInt(1);
					path2 = rs.getInt(2);
					path3 = rs.getInt(3);
					path4 = rs.getInt(4);
				}
				else {
					// System.err.println("Non riuscita inizializzazione della tabella \"LASTPATH\"");
					System.err.println("Initialization of table \"LASTPATH\" not success.");
				}
			}
			rs.close();
			ps.close();
		}
		catch (SQLException ex) {
			// System.err.println("Problema inizializzazione della classe FileInserter.");
			System.err.println("Initialization problem in class FileInserter.");
			// ex.printStackTrace();
		}
		try {
			maxHistory = Integer.parseInt(localProp.get("MaxHistory"));
			if(maxHistory == 0 || maxHistory > maxArchChild / 2) {
				maxHistory = maxArchChild / 2;
				System.err.println("Il valore del parametro MaxHistory nel file delle proprieta' non puo essere maggiore della meta' del valore della variabile interna maxArchChil (=" + maxArchChild + ") per cui il valore a run-time e' stato impostato a " + maxArchChild / 2);
			}
		}
		catch (java.lang.NumberFormatException ex) {
			System.err.println("Impossibile stabilire il massimo numero di versioni di uno stesso file presenti in archivio: parametro nel file di configurazione errato o mancante. Il valore di default e' 10.");
			maxHistory = maxArchChild / 2;
		}

		accessTableTxt = new FloatToObjectSimpleNode(localProp.getIntPar("SimActiveRequests"));// Albero contenente tutte le richieste.
	}

	/**
	 * E` la prima funzione da chiamare per la procedura di inserimento di un file nell'archivio. Effettua la conversione e l' inserimento di un file di ingresso nella catasta dei risultati di RunTime. Il risultato della conversione e` presente come file nella tempdir specificata nel file di proprieta` dell'applicazione (vedere ogetto GuiProp). Il tipo di convertitore utilizzato viene dedotto
	 * dall'associazione estensione=comando par1 par2... presente sempre nel file proprieta' dell'applicazione (vedere ogetto GuiProp). Tra i parametri del comando deve comparire un %i, che viene sostituito con il nome del file di ingresso, e puo' comparire un %o che viene sostituito con il nome del file di uscita se il comando prevede la creazione di un file anziche' l'immissione dei dati
	 * elaborati su "standard output". L'utilizzo di programmi di conversione che scrivano direttamente su file e' preferito perche' in tal modo lo svuotamento del buffer di uscita avviene direttamente ad opera del FileSystem di sistema senza passare attraverso la JVM risultando piu' veloce (e meno costoso).
	 * 
	 * @param accessKey
	 *            intero da utilizzare per ottenere i dati della conversione.
	 * @param finput
	 *            File da inserire in struttura. Deve essere un File valido e leggibile.
	 * @throws ElabException
	 *             con il messaggio di segnalazione/errore che descrive perche' l'operazione non e' possibile.
	 * @throws IOException
	 */
	void conversione(long accessKey, File finput) throws ElabException, IOException {
		pulisci();// Prima di inserire un nuovo file faccio un po` di pulizia degli oggetti istanziati e
		// dimenticati (non dovessero essercene ma se qualche user non ha portato a termine
		// l'inserimento....).
		if(finput == null) return;
		if(!finput.canRead()) throw new ElabException("Impossibile leggere il contenuto del file.");
		int i;
		Runtime rt = Runtime.getRuntime();
		String[] splittedName = (finput.getName()).split("\\.");
		String filename = new String();
		// Ottengo il nome del file senza estensione.
		for(i = 0; i < splittedName.length - 1; i++) {
			filename = filename.concat(splittedName[i]);
		}

		String metaCommand = null;
		if((metaCommand = localProp.get(splittedName[splittedName.length - 1].toLowerCase())) != null) {
			String[] command = metaCommand.split(" +");
			boolean comDirect = false;// Quando a true segnala che il comando emette i dati direttamente Su
			// file.
			// Ora filename contiene il nome del file senza estensione.
			// Nel comando specifico il nome del file su cui deve essere scritto il risultato della conversione.
			// Ho anche provato a convogliare lo stream di output in uno stream Java ma ci cono problemi di
			// riempiemento che rallentano notevolmente l'esecuzione....
			for(int m = 0; m < command.length; m++) {
				if(command[m].equalsIgnoreCase("%i")) command[m] = finput.getPath();
				else if(command[m].equalsIgnoreCase("%o")) {
					command[m] = localProp.get("tempdir") + "/" + filename + ".txt";
					comDirect = true;
				}
			}
			StringBuffer errorMessage = new StringBuffer();
			try {
				final Process pr = rt.exec(command);
				InputStream err = pr.getErrorStream();// DEBUG

				if(comDirect) {
					pr.waitFor();
				}
				else {// In questo caso devo prelevare i dati dallo standard output.
					InputStream out = pr.getInputStream();
					int times = 0;// Viene incrementata di 1 ad ogni passaggio del ciclo di lettura della
					// InputStream quando questo avviene senza effettiva lettura dei dati (il
					// processo esterno non e' riuscito ad elaborare alcun dato nel periodo di ciclo)
					// e costituisce quindi il tempo di attesa per il ciclo successivo. Il comportamento e
					// reciproco nel caso in cui il ciclo legga dati.
					int nReads;
					// char buffer[] = new char[256];
					FileOutputStream fos = new FileOutputStream(localProp.get("tempdir") + "/" + filename + ".txt");
					while(times < 30) {
						// Il seguente si puo' ottimizzare con l'utilizzo di un buffer ma non e' un'operazione
						// critica.
						if((nReads = out.available()) > 0) {
							if(times >= 10) times = 0;
							while(nReads > 0) {
								fos.write(out.read());
								// System.out.print("" + (char) out.read());
								nReads--;
							}
						}
						else {
							times += 1;
							Thread.sleep(150);
						}
					}
					// pr.waitFor();//Per DEBUG.
				}

				if(pr.exitValue() != 0) {
					while(err.available() > 0)
						errorMessage.append((char) err.read());
					throw new ElabException("Computation problem in: \"" + command[0] + "\": " + errorMessage);
				}

				accessTableTxt.append(accessKey, new FileInfoRecord(finput, new File(localProp.get("tempdir") + "/" + filename + ".txt"), filename, splittedName[splittedName.length - 1]), false);
				inserSeqKey.add(new Long(accessKey));
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new ElabException("Computation problem in: \"" + command[0] + "\": " + e.getMessage());
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				throw new ElabException("Problem in execution of \"" + command[0] + "\" task prematurely ended.");
			}
		}
		else throw new ElabException("Sorry: unrecognized file type (obtained by extension).");

		// return "DEBUG";//Per DEBUG
	}

	/**
	 * Elimina le reference molto vecchie (eventualmente dimenticate) nella accessTableTxt in modo da permettere al garbage-collector di liberare memoria.
	 * 
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	private void pulisci() throws NumberFormatException, IOException {
		long tmp = 0;
		FileInfoRecord tmpFB;
		while(tmp > 0) {
			tmpFB = (FileInfoRecord) accessTableTxt.get(inserSeqKey.get(0).intValue()).getValue();
			tmp = tmpFB.instant;
			if(System.currentTimeMillis() - tmp > localProp.getIntPar("InsertManagerTimeout")) {
				tmpFB.textFile.delete();// Rimozione del file temporaneo corrispondente.
				accessTableTxt.remove(tmp);
				inserSeqKey.remove(0);
			}
			else tmp = -1;
		}
	}

	/**
	 * Ritorna una array di caratteri corrispondente al file ottenuto dalla conversione nel formato testo originale rielaboratandolo leggermente per togliere eventuali irregolarita' generalmente riconoscibili. E' anche possibile stabilire quanti bytes far tornare e da che punto cominciare. Se il numero di bytes specificati e' maggiore rispetto a quelli presenti dal punto di inizio alla fine la
	 * stringa terminera' una volta raggiunta la file del file. Requisitio aggiunto il 6/4/2005: volgere tutto a lower-case.
	 * 
	 * @param accessKey
	 *            chiave di accesso con cui si e' chiamata coversione(..).
	 * @param beginPoint
	 *            punto di inizio in percentuale di lunghezza del file (0-100).
	 * @param number
	 *            Numero di caratteri contenuti nella stringa di ritorno. Se passato 0 allora restituiti tutti i bytes fino alla fine del file.
	 * @return array di byte con il buffer contenente il file di testo elaborato e filtrato. Se il file temporaneo non viene trovato ritorna null.
	 * @throws ValidationException
	 */
	private byte[] filtro(long accessKey, int beginPoint, int number) throws ValidationException {
		// String esempio;
		FileInfoRecord fBean;
		if(accessTableTxt.get(accessKey) == null) throw new ValidationException("Access Key not valid or timed out");
		else fBean = (FileInfoRecord) accessTableTxt.get(accessKey).getValue();

		if(beginPoint == 0 && number == 0) {// Ottimizzazione : solo se e` stato richiesto almeno una volta l'intero contenuto posso inserirlo nel fBean. In questo modo quando questa funzione viene
			// chiamata ripetutamente per ottenere il contenuto filtrato dell'intero file l'elaborazione non viene ripetuta piu` di 1 volta.
			if(fBean.content != null) {
				return fBean.content;
			}
			else {
				fBean.content = GenericFunctions.getRegularizedFileContent(fBean.textFile, beginPoint, number);
				return fBean.content;
			}
		}
		else {
			return GenericFunctions.getRegularizedFileContent(fBean.textFile, beginPoint, number);
		}
	}

	/**
	 * Ritorna una stringa contenente una frase di max 1000 caratteri estratta dal file di testo (risultato dalla conversione e del filtraggio) a partire da una posizione corrispondente ad un 10% della dimensione e da presentare all'operatore per il controllo della correttezza.
	 * 
	 * @param accessKey
	 * @return stringa contenente il testo estratto.
	 * @throws ValidationException
	 */
	String esempio(long accessKey) throws ValidationException {
		return new String(filtro(accessKey, 10, 1000));
	}

	/**
	 * Utilizzata dalle successive per ottenere un ID univoco da inserire come chiave nella tabella FILEINFO.
	 */
	private synchronized int getDBID() {
		DB_fileInfoID++;
		return DB_fileInfoID;
	}

	/**
	 * Inserisce nel DB e nell'archtree. Provvede a calcolare la versione (Ricerca di altri record sul DB con lo stesso nome di file ed estensione). Provvede a dividere il nome del file dall'estensione (ovvio). Se l'inserimento va a buon file viene anche eliminato il file temporaneo derivante dalla conversione (necessaria precedente chiamata chiamata al metodo conversione(..)). Il filtro
	 * linguistico diene determinato in automatico tramite la trovaLingua().
	 * 
	 * @param accessKey
	 *            chiave di avvesso per il risultato dell'elaborazione dato dalla funzione conversione(..)
	 * @param paroleSignificative
	 *            descrizione breve.
	 * @param groupConceptList
	 *            lista di interi corrispondenti ai GroupConcepts a cui appartiene il file. I valori sono quelli presenti sul DB e provvengono dall'interfaccia user che a sua volta li puo' ottenere tramite il ResultSet fornito dalla funzione getGrpCncpts().
	 * @param user
	 *            user che sta effettuando l'operazione.
	 * @throws ElabException
	 *             con il messaggio di segnalazione/errore che descrive perche' l'operazione non e' possibile.
	 * @throws IOException
	 * @throws ValidationException
	 * @throws ReqNonValidException
	 * 
	 */
	int insert(long accessKey, String titolo, Vector<String> paroleSignificative, int[] groupConceptList, int user) throws ElabException, IOException, ValidationException {
		return insert(accessKey, titolo, paroleSignificative, groupConceptList, user, trovaLingua(accessKey));
	}

	/**
	 * Inserisce nel DB e nell'archtree. Provvede a calcolare la versione (Ricerca di altri record sul DB con lo stesso nome di file (senza estesione), la stessa descrizione (titolo) con un confronto non case sinsitive, e lo stesso utente). Provvede a dividere il nome del file dall'estensione (ovvio). Se l'inserimento va a buon file viene anche eliminato il file temporaneo derivante dalla conversione (necessaria precedente chiamata chiamata al metodo conversione(..)). La data presente
	 * nel record inserito sul DB si riferisce all'istante di ultima modifica del file. Al raggiungimento del massimo numero di versioni per uno stesso file (prescindendo dall'estensione) viene rimpiazzato quello con data di modifica piu' bassa. Il meccanismo di inserimento delle parole significative e` complesso: nel caso di nuove versioni esso inserisce nel DB (o aggiorna il punteggio) delle
	 * sole parole passate come argomento che non risultavano gia` presenti tra quelle significative inserite della precedente versione, nel caso di file nuovo viene inserita l'unione delle parole presenti nel titolo e quelle passate come argomento. Nel caso di nuova versione la rispettiva lista di parole sig. memorizzata viene aggiornata come unione. Va dichiarata sincronizzata per impedire che
	 * successive richieste a breve distanza di tempo possano accedere alla manipolazione degli stessi dati creando conflitti disastrosi.
	 * 
	 * @param accessKey
	 *            chiave di avvesso per il risultato dell'elaborazione dato dalla funzione conversione(..)
	 * @param titolo
	 *            descrizione breve.
	 * @param significative
	 *            words (for auto classification engine).
	 * @param groupConceptList
	 *            lista di interi corrispondenti ai GroupConcepts a cui appartiene il file. I valori sono quelli presenti sul DB e provvengono dall'interfaccia user che a sua volta li puo' ottenere tramite il ResultSet fornito dalla funzione getGrpCncpts().
	 * @param user
	 *            user che sta effettuando l'operazione.
	 * @param language
	 *            l'ID del filtro linguistico di default per questo filto.
	 * @throws ElabException
	 *             con il messaggio di segnalazione/errore che descrive perche' l'operazione non e' possibile.
	 * @return calculated file version. Value -1 means that an error is occured.
	 * @throws IOException
	 * @throws ValidationException
	 */
	synchronized int insert(long accessKey, String titolo, Vector<String> paroleSignificative, int[] groupConceptList, int user, int filtroLingua) throws ElabException, IOException, ValidationException {
		// Ottengo il nome del file
		int tmpInt;
		boolean elementoNuovo = true; // true se l'elemento e` completamnte nuovo.
		if(titolo == null || titolo.length() == 0 || paroleSignificative == null || paroleSignificative.size() < 2) {
			throw new ValidationException("Title and Significative Words must necessarily be valid fields");
		}
		if(groupConceptList == null || groupConceptList.length == 0 || groupConceptList[0] == 0) {
			throw new ValidationException("The file must be associated at least to one Conceptual Group.");
		}
		FloatToObjectSimpleNode fbeanNode;
		FileInfoRecord fagiolo;
		if((fbeanNode = accessTableTxt.get(accessKey)) == null) throw new ValidationException("Element not valid.");

		if((fagiolo = (FileInfoRecord) fbeanNode.getValue()) == null) throw new ElabException("Element not valid.");

		if(!(fagiolo).origFile.canRead()) throw new ElabException("Original file not readable");
		if(!(fagiolo).textFile.canRead()) throw new ElabException("Textual content file not readable.");
		fagiolo.title = titolo.trim();
		if(fagiolo.groupsList == null) fagiolo.groupsList = new Vector<Integer>();
		else fagiolo.groupsList.clear();

		PreparedStatement ps;// Reference dei vari statement che verranno creati durante la procedura.
		ResultSet rs;

		short versione = -1;
		String tmpString = null;
		try {
			ps = conn.getPstat("select max(VERSIONE), min(DATA), count(*), min(VERSIONE) from FILEINFO where NOMEFILE = ? and OWNER_ID = ? and lower(DESCRIZIONE) = lower(?)", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			ps.setString(1, fagiolo.nameNoExtension);
			ps.setInt(2, user);
			ps.setString(3, fagiolo.title);
			rs = ps.executeQuery();

			// Calcolo versione ed eventuale eliminazione file fisico nel caso di sostituzione.
			short conteggio, maxVers;
			int lastElementID;
			boolean sostituzione = false;
			int oldestElementVers;
			if(rs.next()) {
				//Gestione versione ad eliminazione del file piu' vecchio quando il numero delle versioni e' > maxStorico.
				conteggio = rs.getShort(3);
				maxVers = rs.getShort(1);
				oldestElementVers = rs.getInt(4);
				if(conteggio > 0) {// Esiste gia`� una versione.
					elementoNuovo = false;

					//Ottengo l'ID dell'ultimo elemento inserito.
					ps = conn.getPstat("select ID from FILEINFO where  NOMEFILE = ? and OWNER_ID = ? and lower(DESCRIZIONE) = lower(?) and VERSIONE = ?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
					ps.setString(1, fagiolo.nameNoExtension);
					ps.setInt(2, user);
					ps.setString(3, fagiolo.title);
					ps.setInt(4, maxVers);
					rs = ps.executeQuery();
					rs.next();
					lastElementID = rs.getInt(1);
					
					
					// Ottengo la lista di parole significative dell'ultimo elemento inserito.
					fagiolo.sigWords = new Vector<String>();
					ps = conn.getPstat("select GCW.WORD from FILE_SIG_WORDS FSW, GRP_CNCPT_WORDS GCW  where FSW.FILE_ID = ? and GCW.ID = FSW.WORD_ID group by GCW.WORD", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
					ps.setInt(1, lastElementID);// Devo sempre ottenere l'ID dell'ultima versione per ricavare dall'ultimo file inserito il set di parole significative
					rs = ps.executeQuery();
					while(rs.next()) {
						fagiolo.sigWords.add(rs.getString(1));
					}
					rs.close();
					ps.close();

					// Ottengo la lista di gruppi dell'ultimo elemento inserito e la inserisco nel FileInfoRecord.
					ps = conn.getPstat("select GRP_CNCPT from GRP_CNCPT_FILES where \"FILE\" = ?", localProp.get("connString"), localProp.get("connString"), localProp.get("user"));
					ps.setInt(1, lastElementID);
					rs = ps.executeQuery();
					while(rs.next()) {
						fagiolo.groupsList.add(rs.getInt(1));
					}
					rs.close();
					ps.close();

					/*
					 * Timestamp ts = rs.getTimestamp(2);// Bisogna fare cosi` in quanto il result set precedentemente viene chiuso in automatico dalla VM appena si definisce un nuovo // PreparedStatement al posto del vecchio (la vecchia reference va a null e quindi il ps viene chiuso di conseguenza il rs fa riferimento ad un // ps chiuso). ps = conn.getPstat("select * from FILEINFO where NOMEFILE = ?
					 * and OWNER_ID = ? and lower(DESCRIZIONE) = lower(?) and DATA = ?", localProp.get("connString"), localProp.get("user"), localProp.get("password")); ps.setString(1, fagiolo.nameNoExtension); ps.setInt(2, user); ps.setString(3, fagiolo.title); ps.setTimestamp(4, ts); rs = ps.executeQuery();
					 */

					// Timestamp ts = rs.getTimestamp(2);// Bisogna fare cosi` in quanto il result set precedentemente viene chiuso in automatico dalla VM appena si definisce un nuovo
					// PreparedStatement al posto del vecchio (la vecchia reference va a null e quindi il ps viene chiuso di conseguenza il rs fa riferimento ad un
					// ps chiuso).
					if(conteggio > maxHistory) {// Il numero di versioni e` troppo alto: procedo con sostituzione.
						//Ottengo alcune informazioni del piu` vecchio elemento in ordine di versione. 
						ps = conn.getPstat("select * from FILEINFO where NOMEFILE = ? and OWNER_ID = ? and lower(DESCRIZIONE) = lower(?) and VERSIONE = ?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
						ps.setString(1, fagiolo.nameNoExtension);
						ps.setInt(2, user);
						ps.setString(3, fagiolo.title);
						ps.setInt(4, oldestElementVers);
						rs = ps.executeQuery();
						if(rs.next()) {
							// Si provvede ad eliminazione del file con data di modifica piu' piccola indipendentemente dalla versione.
							// Si potrebbe anche decidere di considerare la versione e non la data di modifica (vedere appunti).
							// Eliminazione del file dall'archtree.
							sostituzione = true;// Segnalo a quanto viene dopo....
							// fagiolo.dbID = rs.getInt("ID");
							fagiolo.dbID = rs.getInt("ID");//Nota bene: rs != rs1!!!
							File fileEliminare = new File(rs.getString("ARCH_PATH") + "/" + rs.getString("NOMEFILE") + "." + rs.getString("ESTENSIONE"));
							if(fileEliminare.isFile()) {
								if(!fileEliminare.delete()) throw new ElabException("Tentativo di eliminare il file " + fileEliminare.getPath() + " per sostituzione di versione non riuscito.");
								fileEliminare = new File(rs.getString("ARCH_PATH") + "/" + rs.getString("NOMEFILE") + ".txt");
								if(!fileEliminare.delete()) throw new ElabException("Tentativo di eliminare il file " + fileEliminare.getPath() + " per sostituzione di versione non riuscito.");
							}
						}
						else {
							ps.close();
							throw new ElabException("Sostituzione di versione non riuscita perche' impossibile stabilire quale file eliminare.");
						}
						rs.close();
						ps.close();
					}
					else {
						fagiolo.dbID = getDBID();
					}
				}
				else {// Non e` un elemento di cui esista gia` una versione ma un elemento completamente nuovo.
					fagiolo.sigWords = new Vector<String>();
					fagiolo.dbID = getDBID();
				}
				versione = (short) (maxVers + 1);
			}
			else {// Se entra qui la prima query non ha fornito neanche un record. Questo non si spiega ma comunque si procede ignorando il problema.
				fagiolo.dbID = getDBID();
				versione = 0;
				fagiolo.sigWords = new Vector<String>();
				ps.close();
			}

			// Inserimento o aggiornamento dei dati nella tabella FILEINFO.
			int first;// Posizione del primo campo della query da impostare.
			if(sostituzione) {
				first = 1;
				ps = conn.getPstat("update FILEINFO set DESCRIZIONE=?, DATA=?, OWNER_ID=?, VERSIONE=?, ARCH_PATH=?, NOMEFILE=?, ESTENSIONE=?, LANGUAGE_FILTER=?, ORIG_PATH=?, INSERTION_DATE=? where ID=?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
				ps.setInt(11, fagiolo.dbID);
			}
			else {
				first = 2;
				ps = conn.getPstat("insert into FILEINFO(ID, DESCRIZIONE, DATA, OWNER_ID, VERSIONE, ARCH_PATH, NOMEFILE, ESTENSIONE, LANGUAGE_FILTER, ORIG_PATH, INSERTION_DATE)  values(?,?,?,?,?,?,?,?,?,?,?)", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
				ps.setInt(1, fagiolo.dbID);
			}
			ps.setString(first++, fagiolo.title);
			ps.setTimestamp(first++, new Timestamp(fagiolo.origFile.lastModified()));
			ps.setInt(first++, user);
			ps.setShort(first++, versione);
			String path = getNewPath(fagiolo.origFile.getName());// Gestione archivio ad albero.
			/* Segnalo la non riuscita dell'operazione */
			if(path == null) throw new ElabException("Fallito il tentativo di creare il nuovo path di inserimento per: " + fagiolo.origFile.getName());
			ps.setString(first++, path);
			ps.setString(first++, fagiolo.nameNoExtension);
			ps.setString(first++, fagiolo.estensione);
			ps.setInt(first++, filtroLingua);
			try {
				ps.setString(first++, fagiolo.origFile.getCanonicalPath());
			}
			catch (IOException e1) {
				e1.printStackTrace();
				throw new ElabException("Impossibile stabilire il percorso assoluto del file per l'inserimento nel campo ORIG_PATH sul DB.");
			}
			ps.setDate(first++, new Date(System.currentTimeMillis()));
			if(ps.executeUpdate() != 1) {
				throw new ElabException("Nessun inserimento nell'archivio per problema su DBMS.");
			}

			/*
			 * Appunti generali per tutto quanto segue.
			 * 
			 * Qui bisogna ottenere 3 insiemi di gruppi conc.: A = {di appartenenza del vecchio elemento}/{di appartenenza del nuovo elemento}; B = {di appartenenza del nuovo elemento}/{di appartenenza del vecchio elemento}, C = {di appartenenza del nuovo elemento} ^ {di appartenenza del vecchio elemento}. Per tutti i gruppi dell'insieme A si provvede ad abbassare il punteggio per tutte le parole
			 * significative ad essi associate (per esepio si sottrae 1) Sara` poi il processo periodico in B.G. a togliere definitivamente tutte le parole (indipendentemente dai gruppi ai quali sono associate) con un punteggio < un valore piccolo < 1 (p.e. 0.5). Per i gruppi appartenenti all'insieme B occorre inserire tutte le parole significative associate all'elemento che si sta` inserendo
			 * (contenute in "paroleSignificative"). Per i gruppi appartenenti all'insieme C (intersezione) si provvede ad inserire solo le parole significative nuove}.
			 */

			// Creazione dei 2 insiemi sopra descritti.
			TreeSet<Integer> groupsJoinerA = new TreeSet<Integer>();// {vecchi} / {nuovi}
			TreeSet<Integer> groupsJoinerB = new TreeSet<Integer>();// {nuovi} / {vecchi}
			TreeSet<Integer> groupsJoinerC = new TreeSet<Integer>();// {vecchi} ^ {nuovi} (^ intersezione...)

			// Si ricorda che, comunque, se l'elemento e` completamente nuovo allora {groupConceptList} = {fagiolo.groupsList}
			// groupsJoinerA = A
			groupsJoinerA.addAll(fagiolo.groupsList);
			for(tmpInt = 0; tmpInt < groupConceptList.length; tmpInt++) {
				groupsJoinerA.remove(groupConceptList[tmpInt]);
			}
			// groupsJoinerB = B
			for(tmpInt = 0; tmpInt < groupConceptList.length; tmpInt++) {
				groupsJoinerB.add(groupConceptList[tmpInt]);
			}
			groupsJoinerB.removeAll(fagiolo.groupsList);
			// groupsJoinerC = C
			for(tmpInt = 0; tmpInt < groupConceptList.length; tmpInt++) {
				if(fagiolo.groupsList.contains(new Integer(groupConceptList[tmpInt]))) groupsJoinerC.add(groupConceptList[tmpInt]);
			}

			// Inserimento delle associazioni file->group-concepts per l'elemento in inserimento.
			if(sostituzione) {//Per tagliare corto (ed in questo caso e` assolutamente conveniente :-)) cancello tutte le vecchie associazioni.
				ps = conn.getPstat("delete from GRP_CNCPT_FILES where \"FILE\"=?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
				ps.setInt(1, fagiolo.dbID);
				ps.execute();
			}
			ps = conn.getPstat("insert into \"GRP_CNCPT_FILES\"(\"GRP_CNCPT\", \"FILE\")  values(?,?)", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			for(tmpInt = 0; tmpInt < groupConceptList.length; tmpInt++) {
				ps.setInt(1, groupConceptList[tmpInt]);
				ps.setInt(2, fagiolo.dbID);
				if(ps.executeUpdate() != 1) {// In questo caso, probabilmente, il gruppo da associare al file non esiste piu` (l'aggiornamento in cascata dei gruppi da parte del DBMS potrebbe
					// non funzionare oppure l'indice del gruppo non e` valido per altre ragioni oppure l'inserimento nella tabella FILEINFO non e` andato a buon fine
					// o...).
					rs.close();
					ps.close();
					// Roll-back dell'operazione precedente.
					ps = conn.getPstat("delete from \"FILEINFO\" where \"ID\"=?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
					ps.setInt(1, fagiolo.dbID);
					ps.executeUpdate();
					rs.close();
					ps.close();
					throw new ElabException("Nessun inserimento nell'archivio per problema su DBMS.");
				}
			}
			rs.close();
			ps.close();

			// Gestione delle parole significative.
			// N.B. la procedura seguente funziona bene sia con file nuovi che con nuove versioni in quanto l'insieme di parole da inserire nel DB viene ottenuto analizzando: titolo, eventuali parole
			// gia` presenti, che includevano anche quelle del titolo per una nuova versione di un file gia` presente, parole significative passate come argomento (indipendenti dal fatto che sia o
			// meno presente una versione precedente).
			String[] paroleSigSplitted;
			TreeSet<String> stringJoinerA = new TreeSet<String>();// {parole vecchie}/{parole nuove}
			TreeSet<String> stringJoinerB = new TreeSet<String>();// {parole nuove}/{parole vecchie} (di conseguenza contiene tutte le parole nel caso in cui l'elelento sia completamente nuovo).
			TreeSet<String> stringJoinerC = new TreeSet<String>();// {tutte le parole del nuovo elemento}

			// Inserimento nel set delle parole contenute in "paroleSignificative".
			stringJoinerB.addAll(paroleSignificative);
			stringJoinerC.addAll(paroleSignificative);

			// Inserimento delle parole significative della vecchia versione.
			stringJoinerA.addAll(fagiolo.sigWords);

			// Inserimento nel set delle parole contenute in "titolo".
			paroleSigSplitted = titolo.split(localProp.get("DelimiterPattern"));
			for(tmpInt = 0; tmpInt < paroleSigSplitted.length; tmpInt++) {
				stringJoinerB.add(paroleSigSplitted[tmpInt].toLowerCase());
				stringJoinerC.add(paroleSigSplitted[tmpInt].toLowerCase());
			}

			// Eliminazione delle Stop Words dal set.
			int[] filtroLinguaArr = new int[] { filtroLingua };// Server solo per passare un array di interi anziche` un intero alla funzione GlobalCollector.getCuttingWordsList(filtroLinguaArr).
			String[] cuttingWords = GlobalCollector.getCuttingWordsList(filtroLinguaArr);
			for(tmpInt = 0; tmpInt < cuttingWords.length; tmpInt++) {
				stringJoinerB.remove(cuttingWords[tmpInt]);
				stringJoinerC.remove(cuttingWords[tmpInt]);
			}

			// Caratterizzazione degli insiemi di parole.
			stringJoinerA.removeAll(stringJoinerB);
			stringJoinerB.removeAll(fagiolo.sigWords);
			// stringJoinerC contiene gia` tute le parole del nuovo elemento.

			/*
			 * Inserimento, aggiornamento punteggi e cancellazione, delle parole significative. Per l'insieme groupsJoinerA si provvede ad abbassare il punteggio delle parole nell'insieme fagiolo.sigWords (ossia quelle della vecchia versione) qual'ora esistano ancora (possono essere rimosse in b.g. dal processo di normalizzazione dei punteggi). Per l'insieme groupsJoinerB si inseriscono o si alza
			 * il punteggio per tutte le parole della nuova versione (ossia quelle dell'insieme stringJoinerC). Per l'insieme groupsJoinerC si abbassa il punteggio delle parole contenute in stringJoinerA (qual'ora esistano ancora) e si alza il punteggio (o si inseriscono qual'ora non esistano) per tutte le parole in stringJoinerB. Con il metodo sopra descritto e` soddisfatta la condizione che dopo
			 * una serie di aggiornamenti di versione con una serie di modifiche sull'associazione dei gruppi seguite da una serie di aggiornamenti con le modifiche inverse si ritorni alla situazione corrispondente al quella da cui si parte prima della modifica della prima serie serie. La stessa condizione vale anche per serie di modifiche sull'insieme delle parole significative a meno di parole
			 * aggiunte sulla tabella GRP_CNCPT_WORDS che comunque avranno punteggio 0 e quindi verranno eliminate (notare che con un punteggio 0 non influiscono sui risultati delle elaborazioni).
			 */
			Iterator<String> wordsJoin;
			Iterator<Integer> groupsJoin;
			Connection localConn = conn.getConnection(localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			CallableStatement cs;
			cs = localConn.prepareCall("EXECUTE PROCEDURE UPDATE_SIG_WORD(?,?,?,?)");
			groupsJoin = groupsJoinerA.iterator();
			cs.setString(4, "0");// Specifico che non beve essere fatto alcun inserimento se la parola non esiste gia` in tabella.
			while(groupsJoin.hasNext()) {
				tmpInt = groupsJoin.next();
				for(tmpInt = 0; tmpInt < fagiolo.sigWords.size(); tmpInt++) {
					if((tmpString = fagiolo.sigWords.get(tmpInt)).length() > 19) continue;//In teoria dovrebbe essere 40 ma il DB o JDBC fanno i capricci. Appena possibile rimettere la lunghezza massima come quella del relativo campo sul DB ossia (attualmente) 40.
					cs.setInt(1, tmpInt);
					cs.setString(2, tmpString);
					cs.setFloat(3, -1f);
					cs.execute();
				}
			}
			groupsJoin = groupsJoinerB.iterator();
			wordsJoin = stringJoinerC.iterator();
			cs.setString(4, "1");// Specifico che beve essere fatto l'inserimento se la parola non esiste gia` in tabella.
			while(groupsJoin.hasNext()) {
				tmpInt = groupsJoin.next();
				while(wordsJoin.hasNext()) {
					if((tmpString =  wordsJoin.next()).length() > 19) continue;//In teoria dovrebbe essere 40 ma il DB o JDBC fanno i capricci. Appena possibile rimettere la lunghezza massima come quella del relativo campo sul DB ossia (attualmente) 40.
					cs.setInt(1, tmpInt);
					cs.setString(2, tmpString);
					cs.setFloat(3, 1f);
					cs.execute();
				}
			}
			if(!elementoNuovo) {// Solo se si tratta di aggiornamento di versione.
				groupsJoin = groupsJoinerC.iterator();
				wordsJoin = stringJoinerA.iterator();
				cs.setString(4, "0");// Specifico che non beve essere fatto alcun inserimento se la parola non esiste gia` in tabella.
				while(groupsJoin.hasNext()) {
					tmpInt = groupsJoin.next();
					while(wordsJoin.hasNext()) {
						if((tmpString =  wordsJoin.next()).length() > 19) continue;//In teoria dovrebbe essere 40 ma il DB o JDBC fanno i capricci. Appena possibile rimettere la lunghezza massima come quella del relativo campo sul DB ossia (attualmente) 40.
						cs.setInt(1, tmpInt);
						cs.setString(2, tmpString);
						cs.setFloat(3, -1f);
						cs.execute();
					}
				}
			}
			groupsJoin = groupsJoinerC.iterator();
			wordsJoin = stringJoinerB.iterator();
			cs.setString(4, "1");// Specifico che beve essere fatto l'inserimento se la parola non esiste gia` in tabella.
			while(groupsJoin.hasNext()) {
				tmpInt = groupsJoin.next();
				while(wordsJoin.hasNext()) {
					if((tmpString =  wordsJoin.next()).length() > 19) continue;//In teoria dovrebbe essere 40 ma il DB o JDBC fanno i capricci. Appena possibile rimettere la lunghezza massima come quella del relativo campo sul DB ossia (attualmente) 40.
					cs.setInt(1, tmpInt);
					cs.setString(2, tmpString);
					cs.setFloat(3, 1f);
					cs.execute();
				}
			}
			// debuggare!!

			// Inserimento delle associazioni element sigWords nella tabella FILE_SIG_WORDS.
			wordsJoin = stringJoinerC.iterator();
			// Prima elimino le vecchie associazioni nel caso in cui esistano e quindi nel caso in cui si stia operando una sostituzione id un elemento precedentemente inserito (taglio corto).
			ps = localConn.prepareStatement("delete from FILE_SIG_WORDS where FILE_ID = ?");
			ps.setInt(1, fagiolo.dbID);
			ps.execute();
			// Poi inserisco quelle nuove.
			cs = localConn.prepareCall("EXECUTE PROCEDURE INSER_ASS_FILE_SIGWORDS(?,?,?)");
			if(groupConceptList.length != 0) {
				for(int i = 0; i < groupConceptList.length; i++) {
					while(wordsJoin.hasNext()) {
						cs.setInt(1, groupConceptList[i]);
						cs.setInt(2, fagiolo.dbID);
						if((tmpString =  wordsJoin.next()).length() > 19) continue;
						cs.setString(3, tmpString);
						cs.executeUpdate();
					}
				}
			}
			cs.close();

			// Inserisco i 2 file nell'archivio ad albero. Va fatto qui per principio.
			GenericFunctions.copiaFile(fagiolo.origFile, new File(path + "/" + fagiolo.origFile.getName()));
			copiaFile(accessKey, new File(path + "/" + fagiolo.textFile.getName()));

			// Eliminazione del file temporaneo.
			if(!fagiolo.textFile.delete()) throw new ElabException("Inserimento andato a buon fine ma impossibilie eliminare il file temporaneo: " + fagiolo.textFile.getPath());
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.err.println(tmpString);
			throw new ElabException(e.getMessage());
		}

		// Eliminazione dalla lista delle richieste.
		accessTableTxt.remove(accessKey);

		return versione;
	}

	private void copiaFile(long accessKey, File fileDest) throws ValidationException {
		try {
			if(!fileDest.isFile()) fileDest.createNewFile();
			FileOutputStream os = new FileOutputStream(fileDest);
			os.write(filtro(accessKey, 0, 0));
		}
		catch (IOException e1) {
			System.out.println("Problema di input/output nel copiare il buffer elaborato in " + fileDest.getPath());
			e1.printStackTrace();
		}
	}

	/**
	 * Fornisce un path valido per l'inserimento di un nuovo file nell'archivio ad albero. Effettua automaticamente l'aggiornamento della tabella LASTPATH (anche se viene interrogata solo all'inizializzazione di questo oggetto). L'accesso completo alla funzione e' gestito da monitor in quanto, anche se si potrebbe lockare una sola parte del codice, la frequenza delle chiamate non e' critica. La
	 * sovrascittura Il meccanismo di ricerca fa si che nel path ritormato non esista alcun file il cui nome corrisponde a quello passato come argomento. Se c'e' stato qualche problema nel creare il path, inclusa la casistica in cui sono presenti troppe versioni dello stesso file, ritorna null.
	 * 
	 * @param fileName
	 * @return nuovo path.
	 */
	final int maxArchChild = 99; // Numero massimo di figli per ogni nodo dell'archivio ad albero

	// (inclusi quelli piu' bassi contenenti i files ed escluso il nodo
	// radice che non ha limite).
	private synchronized String getNewPath(String fileName) throws IOException {
		File tempFile = null;
		boolean riprova = true;
		String[] listaFile;
		int ntentativi = 0;
		// Se il direttorio attuale non contiene il file da inserire ritorno il direttorio attuale.
		// Altrimenti cambio nome al direttorio partendo dal livello piu' basso possibile e se il
		// direttorio esiste faccio il controllo per confermarlo altrimenti lo creo.
		while(riprova) {
			tempFile = new File(localProp.get("arctree") + "/" + path1 + "/" + path2 + "/" + path3 + "/" + path4);

			// Imposto i path per la volta successiva.
			path1++;
			if(path1 >= maxArchChild) {
				path1 = 0;
				path2++;
				if(path2 >= maxArchChild) {
					path2 = 0;
					path3++;
					if(path3 >= maxArchChild) {
						path3 = 0;
						path4++;
					}
				}
			}

			if(tempFile.isDirectory()) {
				listaFile = tempFile.list();
				riprova = false;
				for(int i = 0; i < listaFile.length; i++) {
					if(listaFile[i].equalsIgnoreCase(fileName)) {
						riprova = true;
						break;
					}
				}
			}
			else {
				if(!tempFile.mkdirs()) return null;
				riprova = false;
			}

			ntentativi++;
			if(ntentativi > 1000000) return null;// Quando arriva a 1000000 vuol dire che non ci siamo...
		}

		// Aggiornamento DB
		// L'atomicita' della funzione di aggiornamento dei path locali e del DB non e' critica......
		try {
			PreparedStatement ps = conn.getPstat("update LASTPATH set PATH1=?,PATH2=?,PATH3=?,PATH4=?", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			ps.setInt(1, path1);
			ps.setInt(2, path2);
			ps.setInt(3, path3);
			ps.setInt(4, path4);
			ps.executeUpdate();
			ps.close();
		}
		catch (SQLException e1) {
			e1.printStackTrace();
		}

		return tempFile.getPath();
	}

	/**
	 * Basandosi sull'analisi delle ricorrenze per le parole dei filtri linguistici (quelli con flag LINGUISTICO a 1) tenta di dedurre la lingua del documento.
	 * 
	 * @param accessKey
	 *            riferimento ad un documento ottenuto dalla conversione (chiamamt aa conversione()).
	 * @throws messaggio
	 *             di errore o segnalazione nel caso in cui non sia possibile stabilire la lingua.
	 * @return ID del record su FILTERS_DESRC che indica un filtro linguistico per la lingua in cui, con maggior probabilita', il documento e' stato scritto.
	 * @throws IOException
	 * @throws ValidationException
	 */
	int trovaLingua(long accessKey) throws ElabException, IOException, ValidationException {
		/*
		 * byte[] array = filtro(accessKey, 0, 0)(signed long); int i; IntegerSimpleNode punteggi = new IntegerSimpleNode(100);// Associa l'ID della lingua ad un // punteggio. if(array == null) { throw new ElabException("Impossibile determinare la lingua perche' la funzione \"filtro\" non trova il file temporaneo della richiesta."); } byte[] parola = null; try { PreparedStatement ps =
		 * conn.getPstat("Select FW.DESCR, FW.WORD from FILTERS_DESCR FD, FILTERS_WORDS FW where FD.LINGUISTIC='1' and FD.ID=FW.DESCR", localProp.get("connString"), localProp.get("user"), localProp.get("password")); ResultSet rs = ps.executeQuery(); int stato, n, pos; while(rs.next()) { parola = rs.getString(2).trim().getBytes(); stato = 0; n = 0; pos = 0; // Cerco quante occorrenze esistono della
		 * parola cercata. for(i = 0; i < array.length; i++) { // System.out.print((char)array[i]);//DEBUG switch (stato){ case 0:// Trovato spazio: comincio controllo. if(array[i] == parola[pos]) { pos++; if(pos == parola.length) { if(array[i + 1] == ' '...) n++;// Trovata una occorrenza. pos = 0; stato = 1; } } else { pos = 0; stato = 1; } break; case 1:// Ricerca spazio iniziale. if(array[i] == '
		 * '...) stato = 0; break; } } punteggi.addValue(rs.getInt(1), parola.length * n);// Aggiorno i punteggi: il punteggio per la // lingua viene incrementato del numero di // occorrenze per la lunghezza della parola // stessa (vedere in dettaglio il metodo addValue del SimpleNode:-). } } catch (SQLException ex) { ex.printStackTrace(); } catch(ArrayIndexOutOfBoundsException ex) { throw new
		 * ElabException("Probabily there is a \"null\" word in a filter."); } if(parola == null) throw new ElabException("Nessun filtro linguistico presente sul DB."); // Controllo eventuali problemi. if(punteggi.belowSize() == 0) throw new ElabException("Non sono stare trovate ricorrenze per nessun filtro linguistico presente sul DB."); // Ora procedo con la ricerca della lingua con punteggio
		 * massimo. return (int) punteggi.getMaxValueIndex();
		 */

		byte[] array = filtro(accessKey, 0, 0);
		// punteggio.
		if(array == null) {
			throw new ElabException("Impossibile determinare la lingua perche' la funzione \"filtro\" non trova il file temporaneo della richiesta.");
		}
		try {
			PreparedStatement ps = conn.getPstat("Select FW.DESCR, FW.WORD, FW.RELEVANCE from FILTERS_DESCR FD, FILTERS_WORDS FW where FD.LINGUISTIC='1' and FD.ID=FW.DESCR", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			ResultSet rs = ps.executeQuery();
			return Compare.dbVectorsMaximumAffinity(rs, array);
		}
		catch (ComputationException e) {
			throw new ElabException(e.getMessage());
		}
		catch (SQLException e) {
			throw new ElabException(e.getMessage());
		}
		catch (IOException e) {
			throw new ElabException(e.getMessage());
		}
	}

	/**
	 * Come quella sopra (cambia solo la query e non considera parole intere) ma serve per trovare il gruppo concettuale a cui, con maggior probabilita', il file appartiene. Si basa sulle parole contenute nella tabella GRP_CNCPT_WORDS.
	 * 
	 * @param accessKey
	 * @return ID del record su GRP_CNCPT che indica un gruppo concettuale cui, con maggior probabilita', il file appartiene.
	 * @throws ElabException
	 *             con il messaggio di segnalazione/errore che descrive perche' l'operazione non e' possibile.
	 * @throws IOException
	 * @throws
	 * @throws ValidationException
	 */
	int trovaGroupCncpt(long accessKey) throws ElabException, ValidationException {
		byte[] array = filtro(accessKey, 0, 0);
		// punteggio.
		if(array == null) {
			throw new ElabException("Impossibile determinare il gruppo concettuale perche' la funzione \"filtro\" non trova il file temporaneo della richiesta.");
		}
		try {
			PreparedStatement ps = conn.getPstat("Select GRP_CNCPT, WORD, RELEVANCE from GRP_CNCPT_WORDS", localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			ResultSet rs = ps.executeQuery();
			return Compare.dbVectorsMaximumAffinity(rs, array);
		}
		catch (ComputationException e) {
			throw new ElabException(e.getMessage());
		}
		catch (SQLException e) {
			throw new ElabException(e.getMessage());
		}
		catch (IOException e) {
			throw new ElabException(e.getMessage());
		}
	}

	Vector<String> extractSigWordsList(long accessKey, int[] filtroLinguaArr) throws ElabException, ValidationException {
		byte[] array = filtro(accessKey, 0, 0);
		if(array == null) {
			throw new ElabException("Estrazione automatica delle parole significative: impossibile determinare il gruppo concettuale perche' la funzione \"filtro\" non trova il file temporaneo della richiesta.");
		}

		// TreeSet prova = new TreeSet<String>(GenericFunctions.getStringArrayFromByteArray(array));
		Vector<String> estracted = GenericFunctions.getStringArrayFromByteArray(array);
		TextSkimmer skimmer = new TextSkimmer();

		String[] cuttingWords = GlobalCollector.getCuttingWordsList(filtroLinguaArr);
		for(int i = 0; i < cuttingWords.length; i++) {
			estracted.remove(cuttingWords[i]);
		}

		Iterator<String> wordsIterator = estracted.iterator();
		while(wordsIterator.hasNext()) {
			skimmer.insertWord(wordsIterator.next());
		}

		// Il seguente e` un metodo abbastanza "spannometrico" ma funziona....
		float rate1 = 0.00f, rate2 = 0.01f;
		Vector<String> ret = null, tmpResult;
		int refLength = 100, tmpLength = Integer.MAX_VALUE, tmpLength1 = Integer.MAX_VALUE;
		short state = 0, i;
		for(i = 0; i < 400; i++) {
			switch (state){
				case 0:
					rate1 -= 0.001;
					break;
				case 1:
					rate2 -= 0.001;
					break;
			}

			if(rate1 >= rate2) {
				rate1 = rate2;
				state = 0;
			}

			tmpResult = skimmer.getBetweenList(rate1, rate2);
			tmpLength = tmpResult.size();
			if(tmpLength != 0) {
				// Se il numero di parole si sta avvicinando al targhet continuo con lo stesso gradiente di rate altrimenti lo cambio.
				// Non attuo considerazioni piu` complesse riguardo alla correzione del gradiente in quanto so a priori che bastano 2 direzioni (dRate1<0; dRate2=0 e dRate1=0; dRate2<0) per ottimizzare il numero di parole (ragionare per credere) e mi accontento di una scarsa approssimazione per cui vado a passo fisso.
				if(Math.abs(refLength - tmpLength) < tmpLength1) {
					ret = tmpResult;
					tmpLength1 = Math.abs(refLength - tmpLength);
				}
				if(tmpLength < refLength) state = 0;
				else state = 1;
			}
			else state = 0;
		}

		return ret;
	}
}