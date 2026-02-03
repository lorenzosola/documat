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

import customobj.functions.Compare;

/**
 * Questa classe costituisce il motore di ricerca per similitudine di pattern intrasintattica per frasi ed insiemi di frasi.
 *   
 * @author lsola
 */


/*Un futuro porting di questa classe in C++ si risolve semplicemente definendo una classe che abbia tutte le funzioni utili della String ed utilizzandola al suo posto. Quest'ultima sarebbe da progettare per il contenimento di sequenze di byte piuttosto che di "parole". */ 
public class Psynthesis {
	final int ord = 60; //Numero massimo di parole che possono comparire in una frase.
	private int dimbuff; //numero di parole che vengono memorizzate per una determinata posizione nella frase.
	private String[][] wordBuffer; //wordBuffer[posizione nella frase][posizione nel buffer]
	private double[][] presenza;
	private final double presence = 50;//Addendo che viene aggiunto alla variabile presenza relativa ad una parola ad ogni riscontro positivo. E` moltiplicato per il fattore (1-percent).   
	private double percent;//Percentuale di decadimento. Piu` grande e` la percentuale (piu` vicina ad 1) piu` lento sara` il decadimento.
	private String resultS; //La funzione costruisci computa le informazioni solo una volta se nel frattempo non viene chiamata la insert per risparmiare calcoli: il risultato viene posto qui dentro.
	private String splitDelilmitersPattern;
	
	/**
	 * @param ripetizioni	(ripetizioni/2) e' il numero di inserimenti che fa dimezzare il valore di presenza di una parola se non reinserita. Questo implicitamente determina il numero di frasi che si vogliono accorpare per la sintesi. Piu' grande e' il numero meno dettagliata e piu' veloce sara' la ricerca. Per non far degenerare il comportament il range dei valori e' 0-8; un buon valore e' 5.
	 */
	public Psynthesis(int ripetizioni, String splitDelimitersPattern)
	{
		//Calcolo del coeffi.
		//X(k)= percent * X(k-1) + (1-percent) * X(K).  VECCHIO

	    //X(k)= percent * X(k-1). La condizione assunta e': percent^ripetizioni = 0.5 --> percent = 0.5^(1/ripetizioni).
	    percent = Math.pow((double) 0.5, (double) 1 / (ripetizioni/2));

		dimbuff = ripetizioni;
		wordBuffer = new String[ord][dimbuff];
		presenza = new double[ord][dimbuff];
		this.splitDelilmitersPattern = splitDelimitersPattern;
	}

	/**
	 * Inserimento "intelligente" dei messaggi nella struttura dati per "frasi" ossia una unica stringa contenenti piu' parole separate da spazi virgole ':' o ritorni a capo.
	 * Se i pattern sono stringhe di caratteri costituenti parole in lingua naturale (in questo caso e' quasi scontato) si ricordi
	 * che i confronti nella elaborazione sono case-sensitive e si rende quindi necessario porre TUTTA la "frase"
	 * di ingresso al metodo in upper-case o lower-case per avere un funzionamento coerente con quanto
	 * normalmente ci si aspetta. 
	 * 
	 * @param str Stringa (frase) da inserire nella matrice.
	 */
	public void insert(String str) { 
		String[] strArr = str.split(splitDelilmitersPattern);
		insert(strArr);
	}	
	
	/**
	 * Inserimento "intelligente" dei messaggi nella struttura dati.
	 * Se i pattern sono stringhe di caratteri costituenti parole in lingua naturale si ricordi
	 * che i confronti nella elaborazione sono case-sensitive e si rende quindi necessario porre TUTTE le stringhe
	 * di ingresso al metodo in upper-case o lower-case per avere un funzionamento coerente con quanto
	 * normalmente ci si aspetta. 
	 * @param strArr array di String da inserire nella matrice.
	 */
	public void insert(String[] strArr) { 
		resultS=null;//In questo modo la "costruisci" ricomputa la stringa.
		boolean presente = false;
		//La seguente e' rer DEBUG.
		//System.out.println("\n^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		int posOut = 0;
		//posOut e' la posizione della parola all'interno della frase. pbuf e' la posizione della parola all'interno del buffer di parole che hanno la stesa posizione nella frase.
		for(int pos = 0; pos < strArr.length && pos < ord; pos++) {
			//Ora controllo se la parola strArr[pos] e' presente in wordBuffer[posOut+dither][pbuf] in cui dither e' uno sfasamento della posizione che varia da un valore negativo ad uno positivo.
			//Infatti se una frase differisce da un'altra solamente per la presenza o meno di qualche parola puo' cappitare che le poche parole che coincidono
			//abbiano valori di presenza molto piu' alti di quelli delle altre parole sfasate e questo fa visualizzare una frase in cui
			//con buona probabilita' non compaiono le parole penalizzate anche se piu' significative.
			presente = false;
			int posOutDither;
			for(int dither = -3; dither < 4; dither++) {
				posOutDither = posOut + dither;
				if(posOutDither >= 0 && posOutDither < ord) {
					for(int pbuf = 0; pbuf < dimbuff; pbuf++) {
						if(posOut == posOutDither) presenza[posOutDither][pbuf] = percent * presenza[posOutDither][pbuf];
						if(wordBuffer[posOutDither][pbuf] != null && wordBuffer[posOutDither][pbuf].equals(strArr[pos])) {
							presenza[posOutDither][pbuf] += (1 - percent) * presence;
							presente = true;
							//System.out.println("Aggiornata presenza per parola "+wordBuffer[posOutDither][pbuf]+" in posizione "+posOutDither+" "+pbuf+" presenza= "+presenza[posOutDither][pbuf]);
							break;
						}
					}
				}
				if(presente == true) break;
			}
			if(presente == false) { //Aggiunta o sostituzione della parola meno presente nel buffer di posizione posOut con quella trovata in strArr[pos].
				int minPresIndex = 0;
				double minPres = Double.MAX_VALUE;
				for(int pbuf = 0; pbuf < dimbuff; pbuf++) {
					if(wordBuffer[posOut][pbuf] == null) {
						wordBuffer[posOut][pbuf] = strArr[pos];
						presenza[posOut][pbuf] = (1 - percent) * presence;
						presente = true;//ora la parola e' presente nel buffer.
						//System.out.println("Aggiuta parola "+strArr[pos]+" in posizione: "+posOut+", "+pbuf);
						break;
					}
					else if(presenza[posOut][pbuf] < minPres) {
						minPresIndex = pbuf; //Cosi' trova l'indice della parola meno presente nel buffer della posizione pos rispetto alla frase.
						minPres = presenza[posOut][pbuf];
					}
				}
				if(presente == false) {
					//Se sono arrivato qui significa che nessun elemento del buffer e' vuoto ed ho anche gia' calcolato l'elemento con presenza meno rilevante.
					wordBuffer[posOut][minPresIndex] = strArr[pos];
					presenza[posOut][minPresIndex] = (1 - percent) * presence;
					//System.out.println("Inserita parola "+strArr[pos]+" in posizione: "+posOut+", "+minPresIndex);
				}
			}
			posOut++;
		}
	}

	/**
	 * Costruzione del messaggio riassuntivo in base a cio' che si trova nella struttura dati (buffer).
	 * @return String
	 */
	public String costruisci() { 
		if(resultS!=null) return resultS; 
		double presMedia = 0;
		long numParole = 0;
		for(int pos = 0; pos < ord; pos++) {
			for(int pbuf = 0; pbuf < dimbuff; pbuf++) {
				if(presenza[pos][pbuf] != 0) {
					presMedia += presenza[pos][pbuf];
					numParole++;
				}
			}
		}
		presMedia /= numParole;
		//Imposto pres media per ottenere unmiglior valore di soglia. La soglia, in teoria, dovrebbe essere presence/2 (quindi la stessa per ogni slot) ma si e` visto che e` troppo alta e non considera l`andamento generale.
		//Quello utilizzato qui sotto e` un calcolo piu` complesso sempre determinato con conoscenze un po` empiriche ma si potrebbe giustificare con una descrizione teorica (se ne avro` il tempo e la voglia).
		presMedia = (presMedia + (presence / 2)) / 3;
		resultS = new String();
		for(int pos = 0; pos < ord; pos++) {
			int maxPresIndex = 0;
			double maxPres = 0;
			for(int pbuf = 0; pbuf < dimbuff; pbuf++) {
				if(presenza[pos][pbuf] > maxPres) {
					maxPresIndex = pbuf; //Cosi' trova anche l'indice della parola meno presente nel buffer della posizione di frase pos.
					maxPres = presenza[pos][pbuf];
				}
			}
			if(wordBuffer[pos][maxPresIndex] != null && presenza[pos][maxPresIndex] >= presMedia) {
				resultS += wordBuffer[pos][maxPresIndex] + " ";
			}
		}
		return resultS;
	}

	public void azzera() {
		for(int r = 0; r < dimbuff; r++) {
			for(int c = 0; c < ord; c++) {
				wordBuffer[c][r] = null;
				presenza[c][r] = 0;
			}

		}
	}
	
	/**
	 * Confronta la stringa passata come ingresso con quella ritornata dalla funzione "costruisci".
	 * Se i pattern sono stringhe di caratteri costituenti parole in lingua naturale si ricordi
	 * che i confronti nella elaborazione sono case-sensitive e si rende quindi necessario porre TUTTE la stringhe
	 * di ingresso al metodo in upper-case o lower-case per avere un funzionamento coerente con quanto
	 * normalmente ci si aspetta. 
	 * @param strArr	stringa da confrontare con quella ritornata dalla funzione "costruisci".
	 * @return	punteggio che descrive la coerernza della striga passata con quella ritornata dalla funzione "costruisci".
	 */
	public float calcolaPunteggio(String[] strArr) {
		StringBuffer str = new StringBuffer();
		for(int i = 0; i < strArr.length; i++) {
			str.append(strArr[i] + " ");
		}
		return Compare.similarityFunction(str.toString().getBytes(), costruisci().getBytes(), 5);
	}

}
