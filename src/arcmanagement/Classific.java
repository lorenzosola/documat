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


import java.io.Serializable;
import java.util.Vector;

import customobj.containers.FloatToObjectSimpleNode;

/*
 * Created on Nov 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * Inserimento si coppie (FileInfoRecord,punteggio) in un array che viene mantenuto ordinato e riempito solamente con le coppie di maggior punteggio. Associazione a tutti i record inseriti in classifica
 * della stringa di ricerca (informazione che serve solo come criterio con cui sono stati inseriti i record).
 * 
 * @author lsola
 */

public class Classific implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6449365511449823362L;

	int i;

	private FloatToObjectSimpleNode sn;
	private String searchString;
	private Vector<FloatToObjectSimpleNode> lista = new Vector<FloatToObjectSimpleNode>();

	/**
	 * numero massimo dei risultati che dovra' contenere la classifica: se gli inserimenti sono in numero maggiore (quasi sempre) vengono tolti dalla classifica gli elementi con punteggio inferiore.
	 * 
	 * @param nresults
	 */
	Classific(int nresults, String searchString) {
		if(nresults > 1000) nresults = 1000;//1000 mi sembra gia' un numero esagerato per una classifica...
		this.sn = new FloatToObjectSimpleNode(nresults);//Gli passo la dimensione desiderata dell'array dei risultati (puo' essere esatta o maggiorata di 1 dipendentemente dalla sequenza di inserimenti).
		this.searchString = searchString;
	}

	/**
	 * Aggiunge un FileInfoRecord contenente tutte le informazioni del file ed il relativo punteggio.
	 * 
	 * @param record
	 * @param punteggio
	 */
	void add(FileInfoRecord record, float punteggio) {
		lista.clear(); //Si ripopola al richiamo di getClassific()
		sn.append(punteggio, record, false);
	}

	/**
	 * Ritorna un vettore di SimpleNode in ordine decrescente di punteggio. Ogni SimpleNode ha come value il FileInfoRecord con le informazioni del file e come index il rispettivo punteggio dell'analisi.
	 */
	public Vector<FloatToObjectSimpleNode> getClassific() {
		if(lista.size() == 0) {
			sn.fillVectorDisc(lista);
		}
		return lista;
	}

	/**
	 * Ritorna la striga di ricerca per cui e' stata stilata la classifica.
	 * 
	 * @return String
	 */
	public String getSearchString() {
		return searchString;
	}

	/**
	 * Ritorna il numero di record presenti in classifica.
	 * 
	 * @return int
	 */
	public int size() {
		return sn.belowSize();
	}
}

