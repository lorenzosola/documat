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
import java.sql.Date;
import java.util.Vector;

/**
 * Classe che ha lo scopo di raggruppare tutte informazioni relative ad un file. Costituisce il
 * mezzo di scambio delle informazioni relative ad un file tra tutte le classi del progetto.
 * L'uso che ne e` previsto non comporta che necessariamente debbano essere inizializzati tutti i campi.
 * 
 * @author lsola
 */
public class FileInfoRecord {
	public String arch_path;
	public Integer ownerID = null;
	public String ownerUserName = null;
	public Date data = null;
	public Integer versione;
	public Integer lingua;
	public String nameNoExtension = null, estensione = null;
	public File origFile = null, textFile = null;
	public String title = null;
	public byte[] content = null;//Questo puo` contenere, se inizializzato, un array di bytes rappresentante il contenuto del file. Puo` essere l'intero file integramente riversato oppure pre-elaborato, o qualsiasi altro insieme di simboli che ne rappresenti in modo piu` o meno approssimativo il significato.  
	public Integer dbID = 0;
	public long instant;// Serve a memorizzare l'istante in cui e` stato creato il FileBean per poi poter eliminare dalla tabella quelli piu` vecchiu e permettere ala garbage collector di liberare memoria.
	public Vector<String> sigWords=null;
	public Object genericInformation = null;//Oggetto utilizzato per memorizzare eventuali informazioni che possono essere associate in fase di elaborazione ovunque sia richiesto al file. Sull'utilizzo di questo oggetto non esistono comunque regole.
	public Vector<Integer> groupsList = null;//Lista dei gruppi concettuali di appartenenza.
	public String ownerIdentifier = null;
	public String origPath;
	String headerString, fieldsSeparationString = "\n", footerString;
	private boolean useDNiceStrPres = false;

	FileInfoRecord(Integer ID, String nomeFile, String estensione, String titolo, Vector<String> sigWords, Date data, Integer owner, Integer versione, Integer lingua, String arch_path) {
		this.dbID = ID;
		this.nameNoExtension = nomeFile;
		this.estensione = estensione;
		this.title = titolo;
		this.sigWords = sigWords;
		this.data = data;
		this.ownerID = owner;
		this.versione = versione;
		this.lingua = lingua;
		this.arch_path = arch_path;
	
		groupsList = new Vector<Integer>();
	}

	FileInfoRecord(Integer ID, String nomeFile, String estensione, String titolo, String arch_path) {
		this.dbID = ID;
		this.nameNoExtension = nomeFile;
		this.estensione = estensione;
		this.title = titolo;
		this.arch_path = arch_path;
	}

	FileInfoRecord(File origFile, File textFile, String nomeNoEstensione, String estensione) {
		this.origFile = origFile;
		this.textFile = textFile;
		this.instant = System.currentTimeMillis();
		this.nameNoExtension = nomeNoEstensione;
		this.estensione = estensione;
	}
	
	public String getTitle() {
		return title;
	}
	
	FileInfoRecord() {
		
	}
	
	public String toString() {
		if(useDNiceStrPres) {
			return toStringNicely(headerString, fieldsSeparationString, footerString);
		}
		StringBuffer outStr = new StringBuffer();
		outStr.append(nameNoExtension);
		outStr.append("\n");
		outStr.append(estensione);
		outStr.append("\n");
		outStr.append(title);
		outStr.append("\n");
		outStr.append(sigWords);
		outStr.append("\n");
		outStr.append(data);
		outStr.append("\n");
		outStr.append(ownerID);
		outStr.append("\n");
		outStr.append(versione);
		outStr.append("\n");
		outStr.append(lingua);
		outStr.append("\n");
		outStr.append(arch_path);
		
		return outStr.toString();
	}

	/**
	 * Ottiene la stringa con le informazioni disponibili sull'elemento in formato ordinato ed interpretabile.
	 * 
	 * @param headerString Stringa che precede le informazioni.
	 * @param fieldsSeparationString stringa utilizzata per separare i vari campi.
	 * @param footerString String di chiusura delle informazioni.
	 * @return
	 */
	public String toStringNicely(String headerString, String fieldsSeparationString, String footerString) {
		StringBuffer outStr = new StringBuffer();
		if(headerString != null) outStr.append(headerString);
		if(nameNoExtension != null) {
			outStr.append("File Name: ");
			outStr.append(nameNoExtension);
			outStr.append(fieldsSeparationString);
		}
		if(estensione != null) {
			outStr.append("File format: ");
			outStr.append(estensione);
			outStr.append(fieldsSeparationString);
		}
		if(title != null) {
			outStr.append("Element title/short description: ");
			outStr.append(title);
			outStr.append(fieldsSeparationString);
		}
		if(ownerIdentifier != null) {
			outStr.append("Owner: ");
			outStr.append(ownerIdentifier);
			outStr.append(fieldsSeparationString);
		}
		if(sigWords != null) {
			outStr.append("KeyWords/Abstract: ");
			outStr.append(sigWords);
			outStr.append("fieldSeparationString");
		}
		if(data != null) {
			outStr.append("File last update date: ");
			outStr.append(data);
			outStr.append(fieldsSeparationString);
		}
		if(instant != 0) {
			outStr.append("Element insertion date: ");
			outStr.append(new java.util.Date(instant));
			outStr.append(fieldsSeparationString);
		}

		/*if(title != null) {
			outStr.append("Owner ID: ");
			outStr.append(ownerID);
			outStr.append(fieldsSeparationString);
		}*/

		if(versione != null) {
			outStr.append("Version number: ");
			outStr.append(versione);
			outStr.append(fieldsSeparationString);
		}

		if(lingua != null) {
			outStr.append("Language ID: ");
			outStr.append(lingua);
			outStr.append(fieldsSeparationString);
		}

		if(arch_path != null) {
			outStr.append("Physical archive path: ");
			outStr.append(arch_path);
		}
		if(footerString != null) outStr.append(footerString);
		
		return outStr.toString();
	}
	
	/**
	 * Se chiamata fa in modo che che alla chiamata di toString() venga utilizzata di default la toStringNicely in modo trasparente con i parametri impostati.
	 * @param headerString
	 * @param fieldsSeparationString
	 * @param footerString
	 */
	public void enableDNiceStrPres(String headerString, String fieldsSeparationString, String footerString) {
		this.headerString = headerString;
		this.fieldsSeparationString = fieldsSeparationString;
		this.footerString = footerString;
		useDNiceStrPres = true;
	}
	
	public void disableDNiceStrPres() {
		useDNiceStrPres  = false;
	}

	public void setOwnerIdentifier(String identifier) {
		this.ownerIdentifier = identifier;
	}
	
	public void setOwnerID(int ID){
		ownerID = ID;
	}
	
	/**
	 * Imposta tutti i cambi  i cui dati sono estraibili da una tupla corrispondente alla posizione corrente 
	 * del result set passato come argomento.  
	 * @throws SQLException 
	 *
	 */
	/*
	public void setDataFieldFromRS(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmt = rs.getMetaData();
		for(int i=0; i<rsmt.getColumnCount(); i++) {
			if(rsmt.getColumnName(i) == this.getClass().getDeclaredFields()[1].getName()) {
				
			}
		}
	}
	*/
}