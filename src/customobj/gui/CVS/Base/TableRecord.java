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

/**
 * Ogni record ritornato dalla classe TableDataManager implementa questa interfaccia.
 * @author lsola
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface TableRecord {

	public static final int STATO_INIZIALE = 0; //un oggetto appena creato
	public static final int STATO_CANCELLATO = 1; //a segnalare che l'oggetto ha da essere cancellato
	public static final int STATO_MODIFICATO = 2; //a segnalare che l'oggetto esisteva gi\uFFFD nel db e va solo modificato
	public static final int STATO_NUOVO = 3; //a segnalare che l'oggetto non \uFFFD presente sul db
	
	
	/**
	 * All'interno del record esistono i campi hanno nomi uguali a quelli presenti dul DB e questo metodo serve ad impostarene il valore.
	 * Ritorna false nel caso in cui il campo non sia presente.
	 * @param nome
	 * @param valore
	 * @return true se tutto OK false se il campo non esiste.
	 */
	public boolean setField(String nome, Object valore);
	/**
	 * Per ottenere il valore di un campo all'interno del un record.
	 * @param nome
	 * @return
	 */
	public Object getField(String nome);
	/**
	 * Ottiene l'accessKey del record utile quando si voglia identificare in modo univoco tale record nell'utilizzo di particolari funzione della classe TableDataManager (quelle che accettano l'accessKey come parametro).
	 * @return accessKey del record.
	 */
	public int getAccessKey();
	/**
	 * Per aggiungere record dopo l'inizializzazione.
	 * Meglio usarlo con parsimonia.
	 * Se il record e' gia' presente imposta il suo valore ma in tal caso e' consigliato utilizzare la setField(..).
	 * @param name
	 * @param value
	 */
	public void addField(String name, Object value);	
	/**
	 * Lo stato del record e' una costante intera che denota lo stato del record. Il valore e' uno di quelli presenti nell'istanza che implementa questa interfaccia e che hanno preficco STATO_ .
	 * @param status
	 */
	public void setStatus(int status);
	/**
	 * Lo stato del record e' una costante intera che denota lo stato del record. Il valore e' uno di quelli presenti nell'istanza che implementa questa interfaccia e che hanno preficco STATO_ .
	 * @return stato di consistenza del record (STATO_INIZIALE, STATO_CANCELLATO, STATO_MODIFICATO, STATO_NUOVO).
	 */
	public int getStatus();
	
/**
	//Aggiungo alcuni metodi della classe Vector che posson oessere utili.
	public boolean add(Object obj);
	
	Object get(int index);
	 
	Object 	lastElement();
*/
}
