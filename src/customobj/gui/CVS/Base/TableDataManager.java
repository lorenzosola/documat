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
 * Created on May 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package customobj.gui;


import javax.swing.table.*;
import javax.swing.*;
import java.util.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JLabel;

import customobj.wrappers.DangerousOperationException;
import customobj.wrappers.db_con;

/**
 * Classe che fa da framework in tutti i casi in cui una tabella del DB debba essere acceduta in modo grafico sotto forma di JTable.
 * Contiene una istanza di JTable e di un DefaultTableModel, e viene inizializzata con tutti i record della tabella del DB sotto forma di TableRecord (vedere).
 * Fornisce quindi tutti i metodi di gestione di alto livello per maneggiare i TableRecord ed infine riportarne i cambiamenti sulla tabella del DB.
 * La differenza tra i metodi che, per manipolare i TableRecord (identificati internamente da istanze di LocalRecord), utilizzano gli accessKey da quelli che utilizzano direttamente la reference all'istanza del record
 * è che nei primi vengono trattati tutti i record del mainVector (tutte le righe e con tutti campi della query di inizializzazione) mentre nei secondi vengono trattati tutti i record dela TableModel che contiene i dati della JTable,
 * quindi solo quelle righe e quei campi effettivametne visualizzati in tabella (p.e. non sono presenti quelli in STATO_CANCELLATO perchè TableDataManager li toglie dalla visualizzazione (quindi dal TableModel)). 
 * 
 * @see TableRecord
 * @author lsola
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */

public class TableDataManager {
	private db_con conn;
	private String colNames[];
	private String dbCamps[];// Nomi sul DB dei campi che comaiono in tabella.
	private String totalCamps[];// Nomi di TUTTI i campi risultanti dalla query.
	private InternModel tblmd;
	private JTable jTable;
	private int selectedKey, selectedIndex;
	private Vector<LocalRecord> mainvector;// Contiene anche i campi non visualizzati sulla tabella perche' cancellati da essa.  //  @jve:decl-index=0:

	/*
	 * MListener mListener = new MListener(); FListener fListener = new FListener();
	 */
	private String query;  //  @jve:decl-index=0:

	String conString, user, password;

	/**
	 * 
	 * @param conn
	 * @param headers
	 * @param dbCamps
	 * @throws SQLException
	 */
	public TableDataManager(db_con conn, String[] headers, String[] dbCamps) {
		this.jTable = new JTable(this.getTableModel());
		constrFunction(conn, headers, dbCamps, jTable);
	}

	/**
	 * 
	 * @param conn
	 * @param headers
	 * @param dbCamps
	 * @param jTable
	 * @throws SQLException
	 */
	public TableDataManager(db_con conn, String[] headers, String[] dbCamps, JTable jTable) {
		this.jTable = jTable;
		constrFunction(conn, headers, dbCamps, jTable);
	}

	private void constrFunction(db_con conn, String[] headers, String[] dbCamps, JTable jTable) {
		this.conn = conn;
		this.colNames = (String[]) headers.clone();
		this.tblmd = new InternModel(colNames, 0);
		this.dbCamps = (String[]) dbCamps.clone();
		mainvector = new Vector<LocalRecord>();

		// La sezione seguente serve per aggiungere alla tabella un ListSelectionListener personalizzato che imposta l'indice clickedIndex all'indice della riga clickata.
		ListSelectionModel rowSM = jTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) {
					return;
				}

				// La seguente pappardella serve ad ottenere il ListSelectionModel della tabella in quanto da qui essa non ? visibile. Il LSM, poi, serve solo per sapere se l'indice di selezione ? cambiato (altrimenti bastava un e.getFirstIndex).
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if(lsm.isSelectionEmpty()) {
					// no rows are selected
				}
				else {
					selectedIndex = lsm.getMinSelectionIndex();
					LocalRecord locrec = tblmd.getDataRecord(selectedIndex);
					selectedKey = locrec.accessKey;
					// System.out.println("clickedIndex = "+selectedIndex);
				}
			}
		});

		jTable.removeEditor();
		jTable.setModel(tblmd);
		totalCamps = new String[0];
	}

	/**
	 * Questo metodo deve essere chiamato per lo riempimento delle righe della tabella, del vettore interno di record (utile a tutte le operazioni effettuabili) e di alcune variabili interne comunque deve essere chiamato ogni volta che la query di ingresso subisce una modifica.
	 * 
	 * @param query
	 *            query che dovra' contenere almeno i campi il cui nome e' passato nel parametro camps del costruttore della classe.
	 * @param conString
	 *            stringa di connessione al DB.
	 * @param user
	 *            nome utente per la connessione al DB.
	 * @param password
	 *            password per la connessione al DB.
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public void init(String query, String conString, String user, String password) throws SQLException, DangerousOperationException{
		//Per garantire un livello minimo di sicurezza contro la cancellazione insesiderata dei dati esegue un semplice controllo (non sto` ad implementare un parser SQL..) della query per assicurare che sia sono si selezione.
		String queryLowerCase = query.toLowerCase();
		if(queryLowerCase.contains("insert") || queryLowerCase.contains("into") || queryLowerCase.contains("delete") || queryLowerCase.contains("update")) {
			throw new DangerousOperationException("Security problem in table initialization: the specified query is not allowed.");
		}
		// Ora inizializza il tblmd aggiungendo le righe derivanti dalla query in ordine di nomi dei campi.
		mainvector.clear();
		tblmd.setRowCount(0);
		tblmd.getDataVector().clear();
		this.query = query;
		this.conString = conString;
		this.user = user;
		this.password = password;
		PreparedStatement ps = conn.getPstat(query, conString, user, password);
		ResultSet rs = ps.executeQuery();

		this.totalCamps = new String[rs.getMetaData().getColumnCount()];

		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
			totalCamps[i] = (rs.getMetaData().getColumnName(i + 1));
		}

		LocalRecord locrec = null;
		while(rs.next()) {
			// Record da inserire in JTable.
			int i;
			locrec = new LocalRecord(dbCamps);
			for(i = 0; i < dbCamps.length; i++) {
				locrec.add(rs.getString(dbCamps[i]));
			}
			tblmd.addRow(locrec);

			// Record da inserire in mainvector (con tutti i campi).
			locrec = new LocalRecord(totalCamps, locrec.getAccessKey());
			for(i = 0; i < totalCamps.length; i++) {
				locrec.add(rs.getObject(totalCamps[i]));
			}
			mainvector.add(locrec);
		}
	}

	public void reinit() throws SQLException, DangerousOperationException {
		init(query, conString, user, password);
	}

	private int findVectorIndexByKey(Vector<LocalRecord> inputVector, int key) { // Ricerca ottimizzata degll'indice del TableModel data la chiave dell'ArchivioAutogestito.
		Vector<LocalRecord> searchVector = inputVector;
		// Controllo se esiste la discontinuita` degli indici.
		if(searchVector.size() == 0) {
			return -1;
		}
		int firstKey, lastKey, index;
		firstKey = (searchVector.firstElement()).accessKey;
		lastKey = (searchVector.lastElement()).accessKey;
		if((lastKey - firstKey) > 0 && searchVector.size() > 20) { // C'e' continuit\uFFFD && vale la pena di utilizzare la ricerca binaria quindi procedo in tal modo.
			int firstIndex = 0, lastIndex = searchVector.size() - 1, tmpKey = 0;
			index = lastIndex / 2;
			while(firstIndex <= lastIndex) {
				tmpKey = (searchVector.get(index)).accessKey;
				if(key > tmpKey) {
					firstIndex = ++index;
				}
				else if(key < tmpKey) {
					lastIndex = --index;
				}
				else {
					break;
				}
				index = firstIndex + (lastIndex - firstIndex) / 2;
			}
			if(tmpKey != key) {
				index = -1;
			}
		}
		else { // Nelle chiavi c'e' il salto o non vale la pena di utilizzare la ricerca binaria quindi procedo con la ricerca sequenziale partendo dall'ultimo
			for(index = searchVector.size() - 1; index >= 0; index--) {
				if((searchVector.get(index)).accessKey == key) {
					return index;
				}
			}
			index = -1;
		}
		return index; // Se non \uFFFD stato trovato l'elemento con chiave key il valore index a questo punto \uFFFD -1;
	}

	/**
	 * Aggiunge un record alla tabella e ritorna il TableRecord corrispondente pronto per essere impostato. In questo caso esso sara' contrassegnato con STATO_NUOVO. Notare bene che il TableRecord ritornato non e' quello utilizzato dal TableModel per far visualizzare la riga ma quello completo.
	 * 
	 * @return TsbleRecord inserito e pronto per essere popolato.
	 */
	@SuppressWarnings("unchecked")
	public TableRecord addRow() {
		LocalRecord locrec;
		int i;
		locrec = new LocalRecord(dbCamps);
		// Ovviamente devo inizializzare il vettore in modo tale che abbia una dimensione pari (almeno inizialmente) al numero di campi contenuti.
		// Questo per far funzionare correttamente la getField(.) che almeno deve ritornare un null senza indicizzare fuori range l'array.
		for(i = 0; i < dbCamps.length; i++) {
			locrec.add(i, null);
		}
		locrec.setStatus(TableRecord.STATO_NUOVO);
		tblmd.addRow(locrec);

		locrec = new LocalRecord(totalCamps, locrec.getAccessKey());
		// Ovviamente devo inizializzare il vettore in modo tale che abbia una dimensione pari (almeno inizialmente) al numero di campi contenuti.
		// Questo per far funzionare correttamente la getField(.) che almeno deve ritornare un null senza indicizzare fuori range l'array.
		for(i = 0; i < totalCamps.length; i++) {
			locrec.add(i, null);
		}
		locrec.setStatus(TableRecord.STATO_NUOVO);
		mainvector.add(locrec);

		return locrec;
	}

	/**
	 * Cerca il TableRecord corrispondente alla riga indicata dalla chiave e la contrasssegna come da eliminare dal DB se essa e` gia stata salvata, oppure la rimuove senza implicare nessuna operazione sul DB se essa non e` ancora stata salvata.
	 * 
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	public void deleteRowByKey(int key) {
		LocalRecord locrec;
		int i = findVectorIndexByKey(mainvector, key);
		if(i != -1) {
			locrec = (mainvector.get(i));
			if(locrec.stato == LocalRecord.STATO_NUOVO) {
				mainvector.remove(i);
			}
			else {
				locrec.stato = LocalRecord.STATO_CANCELLATO;
			}
		}
		i = findVectorIndexByKey(tblmd.getDataVector(), key);
		if(i != -1) {
			if(tblmd.getRowCount() != 0) {
				tblmd.removeRow(i);
			}
		}
	}

	/**
	 * Cerca il TableRecord corrispondente alla riga indicata dalla chiave e la contrasssegna come da eliminare dal DB se essa e` gia stata salvata, oppure la rimuove senza implicare nessuna operazione sul DB se essa non e` ancora stata salvata.
	 * 
	 * @param key
	 */
	public void deleteRow(TableRecord record) {
		if(record == null) return;
		deleteRowByKey(record.getAccessKey());
	}

	/*
	 * public void cancellModifications() { Vector jtableVector = blmd.getDataVector(); tblmd.setRowCount(0); jtableVector.clear(); for(int i=0; i<mainvector.size(); i++) { if(mainvector.get(i).stato==TableRecord.STATO_NUOVO) { mainvector.remove(i); i--; continue; } else { LocalRecord locRec = new LocalRecord(dbCamps); locRec. } } }
	 */

	public void deleteSelectedRow() {
		deleteRowByKey(selectedKey);
	}

	/**
	 * Ritorna il TableRecord corrispondente alla riga con chiave indicata.
	 * 
	 * @param key
	 * @return TableRecord corrispondente alla riga clickata
	 */
	public TableRecord getRowByKey(int key) {
		int i = findVectorIndexByKey(mainvector, key);
		if(i != -1) {
			return mainvector.get(i);
		}
		return null;
	}

	/**
	 * Ritorna il TableRecord corrispondente alla riga completa (con tutti i campi del DB quindi presa da mainvector) con indice indicato in cui l'indice si riferisce alle rige visualizzate in tabella (non alle righe preenti nel vettore principale).
	 * 
	 * @param key
	 * @return TableRecord corrispondente alla riga clickata
	 */
	public TableRecord getRowByIndex(int i) throws IndexOutOfBoundsException {
		int key = ((LocalRecord) tblmd.getDataVector().get(i)).getAccessKey();
		return getRowByKey(key);
	}

	/**
	 * Ritorna il TableRecord corrispondente alla riga selezionata (per esempio tramite un click) e contrassegna tale racord come modificato.
	 * 
	 * @return TableRecord corrispondente alla riga selezionata
	 */
	public TableRecord editSelectedRow() {
		LocalRecord locrec = (LocalRecord) getSelectedRow();
		if(locrec != null) {
			if(locrec.stato != LocalRecord.STATO_NUOVO) {
				locrec.stato = LocalRecord.STATO_MODIFICATO;
			}
			return locrec;
		}
		return null;
	}

	/**
	 * Fornisce il TableRecord corrispondente al record interno identificato da un particolare accesscKey.
	 * 
	 * @see getRigaSelKey
	 * @param key
	 * @return TableRecord corrispondente al record interno identificato da un particolare accesscKey
	 */
	public TableRecord editRowByKey(int key) {
		LocalRecord locrec = (LocalRecord) getRowByKey(key);
		if(locrec != null) {
			if(locrec.getStatus() != LocalRecord.STATO_NUOVO) {
				locrec.stato = LocalRecord.STATO_MODIFICATO;
			}
			return locrec;
		}
		return null;
	}

	/**
	 * Ritorna un oggetto TableRecord corrispondente all'ULTIMA riga selezionata sulla tabella.
	 * 
	 * @return TableRecord corrispondente all'ULTIMA riga selezionata sulla tabella
	 */
	public TableRecord getSelectedRow() {
		return getRowByKey(selectedKey);
	}

	/**
	 * Ritorna la chiave di accesso del TableRecord corrispondente all'ULTIMA riga selezionata.
	 * 
	 * @return chiave di accesso del TableRecord corrispondente all'ULTIMA riga selezionata
	 */
	public int getSelectedRowKey() {
		return selectedKey;
	}

	public JTable getJTable() {
		return jTable;
	}

	public TableModel getTableModel() {
		return tblmd;
	}

	/**
	 * Ritorna l'indice del TableModel corrispondente all'ULTIMA riga selezionata. In pratica riporta l'omonimo metodo della JTable al livello di questa classe.
	 * 
	 * @return ndice del TableModel corrispondente all'ULTIMA riga selezionata
	 */
	public int getSelectedRowIndex() {
		return selectedIndex;
	}

	/**
	 * Forza l'indice di selezione ad un valore fino a che qualche evento reale non lo cambia.
	 * 
	 * @param i
	 */
	public void setTemporarySelectionIndex(int i) {
		if(tblmd.getDataVector().size() > i) selectedKey = ((LocalRecord) tblmd.getDataVector().get(i)).accessKey;
	}

	/**
	 * Update the record identified by an accessKey (if it exists) on the table with the data just setted in it. 
	 * @param accessKey
	 */
	@SuppressWarnings("unchecked")
	public void synchronizeTable(int accessKey) {
		TableRecord source = getRowByKey(accessKey);
		try {
			TableRecord dest = (TableRecord) tblmd.getDataRecord(findVectorIndexByKey(tblmd.getDataVector(), accessKey));
			for(int i = 0; i < dbCamps.length; i++) {
				dest.setField(dbCamps[i], source.getField(dbCamps[i]));
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			//System.err.println("La riga sulla tabella che si e` chiesto di aggiornare non e` piu` presente.");
		}
	}

	/**
	 * Update the record "source" on the table (if it be part on the viewed table, then not in the "deleted state") 
	 * with the data just setted in it. 
	 * @param accessKey
	 */
	@SuppressWarnings("unchecked")
	public void synchronizeTable(TableRecord source) {
		int accessKey = source.getAccessKey();
		try {
			TableRecord dest = (TableRecord) tblmd.getDataRecord(findVectorIndexByKey(tblmd.getDataVector(), accessKey));
			for(int i = 0; i < dbCamps.length; i++) {
				dest.setField(dbCamps[i], source.getField(dbCamps[i]));
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			//System.err.println("La riga sulla tabella che si e` chiesto di aggiornare non e` piu` presente.");
		}
	}

	/**
	 * Esegue l'aggiornamento della tabella valorizzando i campi indicari da campsList e dipendentemente dallo stato dei record. Se il record e' nuovo eseguira' un inserimento, se cancellato una cancellazione, se e' modificato una aggiornamento, se in nessuno dei tre stati o se il recod non contiene campi con il nome specificato non viene eseguita alcuna azione per il record. Fatta questa
	 * operazione lo stato di ogni record modificato viene impostato a STATO_INIZIALE.
	 * 
	 * @param dbTable
	 *            tabella del db da agguirnare.
	 * @param campsList
	 *            nomi dei campi il cui valore viene prelevato dai record TableRecord per essere inserito nei campi sul DB che verranno aggiornati.
	 * @param indexes
	 *            campi indice da utilizzare per l'aggiornamento (update) o l'eliminazione dei record. Tali nomi, se passati, dovranno corrispondere a nomi dei campi presenti nei TableRecord il quale valore verra` inserito nella condizione di aggiornamento-cancellazione.
	 * @throws SQLException
	 */
	public void updateDB(String dbTable, String[] campsList, String indexes[]) throws SQLException {
		StringBuffer sql = new StringBuffer();
		String virgola = "";
		Object obj;
		TableRecord tablerecord;
		PreparedStatement ps = null;
		boolean tmpBool = false;
		int c = 0, qp = 0;
		boolean reinitRequest=false;//Serve per l'espediente posto al fine di'ottenere la chiave di un record appena inserito e che consiste nel rieseguire la rilettura di tutta la tabella. 
		for(int i = 0; i < mainvector.size(); i++) {
			sql.delete(0, sql.length());// Si arresta automaticamente se la stringa non contiene caratteri olter l'indice specificato...
			virgola = "";

			tablerecord = mainvector.get(i);

			// Inserimento.
			if(tablerecord.getStatus() == LocalRecord.STATO_NUOVO) {
				sql.append("insert into \"" + dbTable + "\" (");
				c = 0;
				for(; c < campsList.length; c++) {
					sql.append(virgola);
					sql.append("\"" + campsList[c] + "\"");
					if(!virgola.equals(",")) virgola = ",";
				}
				sql.append(") values(");
				virgola = "";
				c = 0;
				for(; c < campsList.length; c++) {
					sql.append(virgola);
					sql.append("?");
					if(!virgola.equals(",")) virgola = ",";
				}
				sql.append(")");

				System.out.println(sql);
				ps = conn.getPstat(sql.toString(), conString, user, password);

				if(indexes != null && indexes.length != 0) reinitRequest = true;
			}

			// Aggiornamento.
			if(tablerecord.getStatus() == LocalRecord.STATO_MODIFICATO) {
				sql.append("update \"" + dbTable + "\" set ");
				c = 0;
				for(; c < campsList.length; c++) {
					sql.append(virgola);
					sql.append("\"" + campsList[c] + "\"=?");
					if(!virgola.equals(",")) virgola = ",";
				}
				sql.append(" where ");
				virgola = "";
				c = 0;
				if(indexes != null) {
					for(; c < indexes.length; c++) {
						sql.append(virgola);
						sql.append("\"" + indexes[c] + "\"" + "=?");
						if(!virgola.equals(",")) virgola = ",";
					}
				}

				System.out.println(sql);
				ps = conn.getPstat(sql.toString(), conString, user, password);
			}

			// Cancellazione.
			if(tablerecord.getStatus() == LocalRecord.STATO_CANCELLATO) {
				sql.append("delete from \"" + dbTable + "\" where ");
				c = 0;
				if(indexes != null) {
					for(; c < indexes.length; c++) {
						sql.append(virgola);
						sql.append("\"" + indexes[c] + "\"" + "=?");
						if(!virgola.equals(",")) virgola = ",";
					}
				}

				System.out.println(sql);
				ps = conn.getPstat(sql.toString(), conString, user, password);
			}

			// Parte comune.
			try {
				if(ps != null) {
					qp = 1;
					tmpBool = false;
					if(tablerecord.getStatus() != LocalRecord.STATO_CANCELLATO) {
						for(c = 0; c < campsList.length; c++, qp++) {
							obj = tablerecord.getField(campsList[c]);
							ps.setObject(qp, obj);
							tmpBool = true;
						}
					}
	
					if(tablerecord.getStatus() == LocalRecord.STATO_MODIFICATO || tablerecord.getStatus() == LocalRecord.STATO_CANCELLATO) {
						if(indexes != null) {
							for(c = 0; c < indexes.length; c++, qp++) {
								obj = tablerecord.getField(indexes[c]);
								if(obj != null) {
									ps.setObject(qp, obj);
									tmpBool = true;
								}
							}
						}
					}
	
					if(tmpBool) {
						if(ps.executeUpdate() != 1) {
							throw new SQLException("TableDataManager: aggiornato o inserito o cancellato un numero di righe diverso da 1 quindi problema nel formulare la query.");
						}
						tablerecord.setStatus(LocalRecord.STATO_INIZIALE);
	
						if(reinitRequest) {//Espediente per ottenere la chiave di un record nuovo.
							try {
								reinit();
							} catch (DangerousOperationException e) {
								// TODO Auto-generated catch block
	//							e.printStackTrace();
							}
							reinitRequest = false;
						}
					}
	
					ps.close();
					ps = null;
				}
			}
			catch(SQLException ex) {
				//Se la riga non riesce ad essere inserita o aggiornata rimossa dai vettori locali per evitare che alla successiva richiesta di aggiornameto del DB si incorra nello stesso errore.
//				if(tablerecord.getStatus()==TableRecord.STATO_NUOVO)	deleteRow(tablerecord);
				deleteRow(tablerecord);
				throw ex;
			}
		}

	}

	/**
	 * Imposta la larghezza della colonna.
	 * 
	 * @param colnum
	 *            intero che identifica la colonna tra le colonne definite dagli header nel medesimo ordine.
	 * @param width
	 *            larcghezza in pxel.
	 */
	public void setColumnWidth(int colnum, int width) {
		if(jTable == null) {
			return;
		}
		jTable.getColumnModel().getColumn(colnum).setPreferredWidth(width);
	}

	/**
	 * 
	 * @param colnum
	 *            intero che identifica la colonna tra le colonne definite dagli header nel medesimo ordine.
	 * @param oralign
	 *            orientazione dell'allineamento: se corrisponde a D,d,E,e,R,r l'allineamento e' a destra (o east o right) altrimenti e' a sinistra.
	 */
	public void setColumnAlignment(int colnum, char oralign) {
		if(jTable == null) {
			return;
		}
		int alignment;
		TableColumn column = jTable.getColumnModel().getColumn(colnum);
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		if(oralign == 'D' || oralign == 'd' || oralign == 'E' || oralign == 'e' || oralign == 'R' || oralign == 'r') {
			alignment = JLabel.RIGHT;
		}
		else {
			alignment = JLabel.LEFT;
		}
		renderer.setHorizontalAlignment(alignment);
		column.setCellRenderer(renderer);
		// ((JLabel)jTable.getDefaultRenderer(String.class)).setHorizontalAlignment(JLabel.RIGHT);
	}

	/**
	 * Nel caso di selezione multipla sulla taella ritorna un vettore contenente tutti i TableRecord corrispondenti a quelli selezionati.
	 * 
	 * @return vettore contenente tutti i TableRecord corrispondenti a quelli selezionati.
	 */
	public TableRecord[] getSelectedRows() {
		int[] selezione = jTable.getSelectedRows();
		TableRecord[] array = new TableRecord[selezione.length];
		Vector dataVector = tblmd.getDataVector();
		for(int i = 0; i < selezione.length; i++) {
			array[i] = getRowByKey(((LocalRecord) dataVector.get(i)).accessKey);
		}
		return array;
	}

	/**
	 * Ritorna il vettore con tutti i record COMPLETI (con tutti i campi della tabella del DB) da eliminare, da modificare e da aggiungere.
	 * 
	 * @return Vettore con tutti i record in formato completo ed in tutto gli stati presenti.
	 */
	public Vector getDataVector() {
		return mainvector;
	}

	/**
	 * Vuota la tabella come prima della inizializzazione.
	 */
	public void clear() {
		mainvector.clear();
		tblmd.setRowCount(0);
		tblmd.getDataVector().clear();
	}

	/*
	 * class FListener implements FocusListener { public void focusGained(FocusEvent e) { Object clicked = e.getSource(); if(clicked instanceof exTextField) { if(clicked!= null && clicked instanceof exTextField) ((exTextField)clicked).setBackground(normalBackground); clickedKey = ((exTextField)clicked).accessKey; ((exTextField)clicked).setBackground(clickedBackground); lastClicked =
	 * (Component)clicked; } //A questo punto focusIndex e' l'indice della riga su cui \uFFFD puntato il fuoco. } public void focusLost(FocusEvent e) { } } class MListener implements MouseListener { public void mouseClicked(MouseEvent e) { Object clicked = e.getSource(); if(clicked instanceof exTextField) { if(clicked!= null && clicked instanceof exTextField)
	 * ((exTextField)clicked).setBackground(normalBackground); clickedKey = ((exTextField)clicked).accessKey; ((exTextField)clicked).setBackground(clickedBackground); lastClicked = (Component)clicked; } //A questo punto focusIndex e' l'indice della riga su cui \uFFFD puntato il fuoco. } public void mouseEntered(MouseEvent e) { } public void mouseExited(MouseEvent e) { } public void
	 * mousePressed(MouseEvent e) { } public void mouseReleased(MouseEvent e) { } }
	 */

	class InternModel extends DefaultTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public InternModel(String[] colname, int i) {
			super(colname, i);
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}

		public Object getValueAt(int row, int column) {
			if(this.dataVector != null) {
				if(dataVector.size() > row) {
					if(dbCamps.length > 0) return super.getValueAt(row, column);
				}
			}
			return null;
		}

		public LocalRecord getDataRecord(int i) {
			return (LocalRecord) dataVector.get(i);
		}

	}

	private static int accessKeyGenerator;

	private class LocalRecord extends Vector implements TableRecord {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		// public static final int STATO_INIZIALE = 0; //un oggetto appena creato
		// public static final int STATO_CANCELLATO = 1; //a segnalare che l'oggetto ha da essere cancellato
		// public static final int STATO_MODIFICATO = 2; //a segnalare che l'oggetto esisteva gi\uFFFD nel db e va solo modificato
		// public static final int STATO_NUOVO = 3; //a segnalare che l'oggetto non \uFFFD presente sul db
		private int stato, accessKey;

		private String fieldList[];

		private Vector<String> postAddedNames = null;// Vettore che accoglie i nomi di eventuali campi aggiunti dopo l'inizializzazione tramite il metodo addField(). Se non viene aggiunto nessun campo il Vector deve essere null (risparmio memoria).

		private Vector<Object> postAddedValues = null;// Vettore che accoglie i nomi di eventuali campi aggiunti dopo l'inizializzazione tramite il metodo addField(). Se non viene aggiunto nessun campo il Vector deve essere null (risparmio memoria).

		LocalRecord(String[] fieldList) {
			if(accessKeyGenerator == Integer.MAX_VALUE) {
				accessKeyGenerator = 0;
			}
			else {
				accessKeyGenerator++;
			}
			stato = STATO_INIZIALE;
			this.fieldList = fieldList;
			accessKey = accessKeyGenerator;
		}

		LocalRecord(String[] fieldList, int accessKey) {
			stato = STATO_INIZIALE;
			this.accessKey = accessKey;
			this.fieldList = fieldList;
		}

		/**
		 * Ritorna false nel caso in cui il campo non sia presente.
		 * 
		 * @param nome
		 * @param valore
		 * @return true se tutto OK false se il campo non esiste.
		 */
		@SuppressWarnings("unchecked")
		public boolean setField(String nome, Object valore) {
			int i;
			for(i = 0; i < fieldList.length; i++) {
				if(((String) fieldList[i]).equalsIgnoreCase(nome)) {
					set(i, valore);
					return true;
				}
			}
			if(postAddedNames != null && (i = postAddedNames.indexOf(nome)) != -1) {
				postAddedValues.set(i, valore);
				return true;
			}
			return false;
		}

		/**
		 * Per ottenere il valore di un campo all'interno del un record.
		 * 
		 * @param nome
		 * @return null se il campo non e' tra quelli presenti nella query iniziale.
		 */
		public Object getField(String nome) {
			int i;
			for(i = 0; i < fieldList.length; i++) {
				if(((String) fieldList[i]).equals(nome)) return (get(i));
			}
			if(postAddedNames != null && (i = postAddedNames.indexOf(nome)) != -1) {
				return postAddedValues.get(i);
			}
			return null;
		}

		/**
		 * Per aggiungere record dopo l'inizializzazione. Se il record e' gia' presente imposta il suo valore. Meglio usarlo con parsimonia.
		 * 
		 * @param name
		 * @param value
		 */
		public void addField(String name, Object value) {
			if(setField(name, value))
			;
			else {
				if(postAddedNames == null && postAddedValues == null) {
					postAddedNames = new Vector<String>();
					postAddedValues = new Vector<Object>();
				}
				postAddedNames.add(name);
				postAddedValues.add(value);
			}
		}

		public int getAccessKey() {
			return accessKey;
		}

		/**
		 * @param status
		 *            puo' essere : TableRecord/LocalRecord.STATO_INIZIALE, TableRecord/LocalRecord.STATO_CANCELLATO , TableRecord/LocalRecord.STATO_MODIFICATO , TableRecord/LocalRecord.STATO_NUOVO.
		 */
		public void setStatus(int status) {
			stato = status;
		}

		public int getStatus() {
			return stato;
		}

	}
	
	public String[] getColumnNames() {
		return colNames;
	}
}