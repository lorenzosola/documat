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

package analysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import customobj.containers.FloatToObjectSimpleNode;
/**
 * La funzione di uqesto oggetto e` quella individuare in un testo gli insiemi di parole che hanno una significanza maggiore o minore di una certa soglia. Attualmente si effettua una semplice frequenza media di comparizione di ogni singola parola (n-ripetizioni-totali/n-parole-totali per ogni parola). Questo non e` il metodo piu` preciso, in quanto non considera la localizzazione delle parole, ma
 * neanche concettualmente errato.
 * 
 * @author lsola
 * 
 */
public class TextSkimmer {
	private HashMap<String, KeyWord> wordsRate;
	private int totWordCount;// Numero totale di parole.
	private int totWordNumber;// Numero totale di parole considerate una sola volta.
	private double media;// Media della frequenza delle parole rispetto alla totalita` delle parole inserite dopo una inizializzazione.
	private double varianza;// Varianza della frequenza delle parole rispetto alla totalita` delle parole inserite dopo una inizializzazione.
	private double MQD;// middle quadratic discard della frequenza delle paorle rispetto alla totalita` delle parole inserite dopo una inizializzazione.
	private boolean momentsCalculated; // Impostata ad 1 ad ogni chiamata della recalculateMoments() e a 0 ad ogni chiamamta della insertWord(.) al fine di ottimizzare il numero dei calcoli per frequenti chiamamte alle altre funzioni.

	private class KeyWord {
		String word;
		float rate;// Numero di ricorrenze della inserita (incrementato ad ogni chiamata di insertWord(word) per la parola corrispondente all'argomento passato).

		KeyWord(String word) {
			this.word = new String(word);
		}
	}

	public TextSkimmer() {
		wordsRate = new HashMap<String, KeyWord>();
		media = 0;
	}

	/**
	 * Inserisce la parola passata. Per parole standard SICORDARE VOLGERLE A LOWER (O HIER) CASE prima di passarle come parametro.
	 * IMPORTANTE: statisticamente parlando tramite questa funzione devono essere inseriti elementi appartenenti ad una campione non condizionato.
	 * Questo significa che in riferimento ad un testo devono essere inserite TUTTE le parole incluse le ripetizioni per poter effetuare la selezione.
	 * Se, per esempio, si inseriscono parole senza ripetizioni la funzione getBetweenList restituisce in ogni caso o un insieme vuoto o tutte le parole inserite. 
	 * 
	 * @param word
	 */
	private String tmpStrForInsert;
	private KeyWord tmpKeyWordForInsert;

	public void insertWord(String word) {
		if(!momentsCalculated) momentsCalculated = false;
		tmpStrForInsert = new String(word);
		if((tmpKeyWordForInsert = wordsRate.get(tmpStrForInsert)) == null) {
			tmpKeyWordForInsert = new KeyWord(tmpStrForInsert);
			wordsRate.put(word, tmpKeyWordForInsert);
			totWordNumber++;
		}
		tmpKeyWordForInsert.rate++;
		totWordCount++;
	}

	/**
	 * Ritorna una vettore di parole con frequenza media maggiore alla soglia passata (valore in [0,1]). La funzione e` stata implementata principalmente per la ricerca delle stop-words.
	 * 
	 * @param treshold
	 *            E` il fattore moltiplicativo dello scarto quadratico medio per ottenere la soglia al di sopra della quale si deve trovare lo scarto della singola parola per essere inclusa nell'insieme ritornato.<BR>
	 *            In base a prove ho vallutato un valore ottimale di circa 3 ma se si tratta di analizzare testo con si rischia di includere anche qualche parola generalmente signifiactiva (si ricorda che la distribuzione non e` gaussiana, anzi e` molto differente e per questo motivo il metodo adottato con media e varianza non e` molto preciso...comunque per ora puo` andare). Proprio per questo e`
	 *            molto utile, nel caso di analisi di testi effettuare, un pre-filtraggio che puo`, per esempio, consistere nell'eliminare parole lunghe, numeri e lettere singole (solo se questi elementi vengono vewngono gia` esclusi da un identico prefiltraggio al momento dell'eliminazione delle stop words nelle altre analisi sigli stessi dati).
	 * @return
	 */
	private KeyWord tmpKeyWordForQuery;

	public Vector<String> getBlackList(float treshold) {
		recalculateMoments();
		Iterator<KeyWord> i = wordsRate.values().iterator();
		Vector<String> ret = new Vector<String>();
		treshold = (float) MQD * treshold;
		synchronized (tmpKeyWordForQuery) {
			while(i.hasNext()) {
				tmpKeyWordForQuery = i.next();
				if(tmpKeyWordForQuery.rate / totWordCount - media > treshold) {// Media della frequenza per la parola in tmpKeyWordForQuery - media = scarto della parola in tmpKeyWordForQuery.
					ret.add(tmpKeyWordForQuery.word);
				}
			}
		}
		return ret;
	}

	/**
	 * Ritorna una vettore di parole con frequenza media minore alla soglia passata (valore in [0,1]).
	 * 
	 * @param treshold
	 *            E` il fattore moltiplicativo dello scarto quadratico medio per ottenere la soglia (negativa rispetto alla media) al di sotto della quale si deve trovare lo scarto della singola parola (negativo rispetto alla media) per per essere inclusa nell'insieme ritornato.<BR>
	 *            In base a prove ho vallutato un valore ottimale di 0.34f (si ricorda che la distribuzione non e` gaussiana, anzi e` molto differente e per questo motivo il metodo adottato con media e varianza non e` molto preciso...comunque per ora puo` andare).<BR>
	 *            Proprio per l'andamento particolare della distribuzione delle parole di un testo (prob. fortemente condizionata) questo metodo funziona molto peggio di getBlackList.<BOR> Per file di testo di cui si vogliono ottenere le parole significative vedere getBetweenList.
	 * @return
	 */
	public Vector<String> getWhiteList(float treshold) {
		recalculateMoments();
		Iterator<KeyWord> i = wordsRate.values().iterator();
		Vector<String> ret = new Vector<String>();
		treshold = -((float) MQD * treshold);
		tmpKeyWordForQuery = new KeyWord("");
		synchronized (tmpKeyWordForQuery) {
			while(i.hasNext()) {
				tmpKeyWordForQuery = i.next();
				if(tmpKeyWordForQuery.rate / totWordCount - media < treshold) {// Media della frequenza per la parola in tmpKeyWordForQuery - media = scarto della parola in tmpKeyWordForQuery.
					ret.add(tmpKeyWordForQuery.word);
				}
			}
		}
		return ret;
	}

	/**
	 * Come getBlackList(.) ma qui viene specificato il numero massimo di parole da inserire nel risultato.
	 * 
	 * @param count
	 * @return
	 */
	public Vector<String> getBlackListFirstN(int count) {
		Vector<String> ret = new Vector<String>();
		Vector<FloatToObjectSimpleNode> orderedSn = new Vector<FloatToObjectSimpleNode>();
		FloatToObjectSimpleNode sn = new FloatToObjectSimpleNode(count);
		Iterator<KeyWord> it = wordsRate.values().iterator();
		if(it.hasNext()) {
			tmpKeyWordForQuery = it.next();
			synchronized (tmpKeyWordForQuery) {
				while(it.hasNext()) {
					tmpKeyWordForQuery = it.next();
					// sn.append((float)(tmpKeyWord.rate/totWordCount), tmpKeyWord.word, true);
					sn.append(tmpKeyWordForQuery.rate, tmpKeyWordForQuery.word, false);// Rispetto a quella sopra risparmio una divisione....
				}
				sn.fillVectorDisc(orderedSn);
				for(int i = 0; i < orderedSn.size(); i++) {
					ret.add((String) (orderedSn.get(i).getValue()));
				}
			}
		}
		return ret;
	}

	/**
	 * Come getWhiteList(.) ma qui viene specificato il numero massimo di parole da inserire nel risultato.
	 * 
	 * @param count
	 * @return
	 */
	public Vector<String> getWhiteListFirstN(int count) {
		Vector<String> ret = new Vector<String>();
		Vector<FloatToObjectSimpleNode> orderedSn = new Vector<FloatToObjectSimpleNode>();
		FloatToObjectSimpleNode sn = new FloatToObjectSimpleNode(count);
		Iterator<KeyWord> it = wordsRate.values().iterator();
		if(it.hasNext()) {
			tmpKeyWordForQuery = it.next();
			synchronized (tmpKeyWordForQuery) {
				do {
					tmpKeyWordForQuery = it.next();
					// sn.append((float)(-(tmpKeyWord.rate/totWordCount)), tmpKeyWord.word, true);//Rispetto a quella sopra risparmio una divisione....
					sn.append(-tmpKeyWordForQuery.rate, tmpKeyWordForQuery.word, true);// Rispetto a quella sopra risparmio una divisione....
				}
				while(it.hasNext());
				sn.fillVectorDisc(orderedSn);
				for(int i = 0; i < orderedSn.size(); i++) {
					ret.add((String) orderedSn.get(i).getValue());
				}
			}
		}
		return ret;
	}

	/**
	 * Ricalcola media varianza e MQD (scarto quadratico medio) dell'evento parola.
	 * 
	 */
	private void recalculateMoments() {
		if(momentsCalculated) return;
		else momentsCalculated = true;

		Iterator<KeyWord> it = wordsRate.values().iterator();

		// Ricalcolo media.

		// while(it.hasNext()) {
		// tmpKeyWordForQuery = it.next();
		// media += tmpKeyWordForQuery.rate;
		// }
		// media /= totWordCount;//Praticamente a questo punto media e` 1 quindi sarebbe inutile fare il calcolo
		// media /= totWordNumber;

		media = 1f / totWordNumber;
		it = wordsRate.values().iterator();
		while(it.hasNext()) {
			tmpKeyWordForQuery = it.next();
			varianza += Math.pow(media - tmpKeyWordForQuery.rate / totWordCount, 2);
		}
		varianza /= totWordNumber;
		MQD = Math.sqrt(varianza);

		//System.out.println("media= " + media);
		//System.out.println("varianza= " + varianza);
		//System.out.println("MQD= " + MQD);
	}

	/**
	 * Data la media M, lo scarto quadratico medio MQD, il tresholdStart ed il tresholdStop seleziona l'insieme delle parole che hanno scarto (rispetto alla media) compreso tra tresholdStart * MQD e tresholdStop*MQD. Chiaramente, potendo lo scarto, essere sia positivo sia negativo, cosi` possono essere anche tresholdStart e tresholdStop. In base a prove ho visto che con i 2 parametri di ingresso
	 * rispettivamente a -0.12f,-0.115f si ottinene un funzionamento compatibile con quello per cui questa funzione e` stata progettata (selezionare parole significative) ma ho provato con un solo file dal quale, pur essendo lungo piu` di 500KB, si otteneva un insieme che, pur essendo ben composto, era comunque molto corposo (2108 parole) a conferma del fatto che questo metodo e` abbastanza
	 * superficiale.
	 * 
	 * @param tresholdStart
	 * @param tresholdStop
	 * @return
	 */
	public Vector<String> getBetweenList(float tresholdStart, float tresholdStop) {
		if(tresholdStart > tresholdStop) throw new IllegalArgumentException("tresholdStart must be at least < tresholdStop to have sense");
		recalculateMoments();
		Iterator<KeyWord> i = wordsRate.values().iterator();
		Vector<String> ret = new Vector<String>();
		tresholdStart = (float) MQD * tresholdStart;
		tresholdStop = (float) MQD * tresholdStop;

		synchronized (tmpKeyWordForQuery) {
			while(i.hasNext()) {
				tmpKeyWordForQuery = i.next();
				if(tmpKeyWordForQuery.rate / totWordCount - media >= tresholdStart && tmpKeyWordForQuery.rate / totWordCount - media <= tresholdStop) {
					ret.add(tmpKeyWordForQuery.word);
				}
			}
		}

		return ret;
	}
	
}
