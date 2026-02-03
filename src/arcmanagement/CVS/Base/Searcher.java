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

import customobj.functions.GenericFunctions;
import customobj.wrappers.AutoFileBuff;
import analysis.Psynthesis;


/*
 * Created on Nov 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * Questo oggetto ha il compito di effettuare una ricerca, utilizzando Psinthesis, su di un buffer autoriempiente (che utilizza un suo thread) rappresentato da un oggetto AutoFileBuff associato ad un file e contenente oportune variabili di stato per il controllo del flusso. Tale "buffer" viene dunque letto e, riconoscendo i punti ('.'), divide le frasi da inserire in Psinthesis. I punteggi di
 * coerenza assieme ai nomi e percorsi dei file analizzati vengono scritti in un oggetto che mantiene in memoria solo i piu' importanti in ordine di punteggio. (Tale oggetto e' passato al costruttore di questa classe da parte del dispatcher che, dopo averlo creato lo mappa in una Hashmap in cui viene associato alla richiesta).
 * La classe viene istanziata dalla sottoclasse SearhcManager dentro a SearchDispatcher per ogni file da analizzare.
 * 
 * @author lsola
 */
class Searcher extends Thread {
	private Classific cl;
	private AutoFileBuff afb;
	private String srcstr;//Stringa specificante l'argomento di ricerca.
	private int nphrases;//Dimensione degli accrpamenti, in numero di frasi, sul file da analizzare.
	private char inchar[] = new char[600];//La dinensione di questo array determina automaticamente anche la dimensione massima di una frase immessa in Psynthesis. Se , per esemio, manca il punto la frase viene comunque troncata.
	private FileInfoRecord record;//Tutte le informazioni non tecniche del file.
	private String[] cuttingWords;//Array di stringhe utilizzato dalla "filtro(.)".
	private String splitDelimitersPattern;
	
	/**
	 * Dopo l'istanziazione l'oggetto viene automaticamente eseguito da un suo thread.
	 * 
	 * @param afb
	 *         AutoFileBuff con il contenuto del file da analizzare
	 * @param cl
	 *         stesso oggetto Classific passato anche agli altri Searcher istanziati per la medesima ricerca.
	 * @param srcstr
	 *         stringa con la frase della ricerca.
	 * @param nphrases
	 *         dimensione dell'accorpamento in numero di frasi durante l'analisi.
	 * @param record
	 *         oggetto FileInfoRecord che contiene le informazioni del file il cui contenuto si trova nell'oggetto AutoFileBuff.
	 */
	Searcher(AutoFileBuff afb, Classific cl, String srcstr, int nphrases, FileInfoRecord record, String[] cuttingWords, String splitDelilmitersPattern) {
		this.afb = afb;
		this.cl = cl;
		this.srcstr = srcstr;
		this.nphrases = nphrases;
		this.record = record;

		this.cuttingWords = cuttingWords;
		
		this.splitDelimitersPattern = splitDelilmitersPattern;
		this.start();
	}


	/**
	 * Ciclo di lettura da AutoFileBuffer, filtraggio ed inserimento in Psynthesyis.
	 * Lo stream viene prelevato dall' AutoBileBuffer relativo al file in esame che viene diviso in frasi delimitate dal punto.
	 * Ogni fase ricavata viene poi inserita nell' analizzatore e al raggiungimento del numero di frasi stabilito viene calcolato il punteggio di coerenza con la frase di ricerca.
	 * Dalla sequenza di numeri ottenuta dai vari confronti di coerenza si ricava poi il punteggio finale (puo` essere una media pesata o altro, attualmente e` il massimo tra tutti i punteggi).
	 */
	public void run() {
		Psynthesis psyn = new Psynthesis(nphrases, splitDelimitersPattern);
		nphrases++;//Lo aumento di 1 per migliorare un po la coerenza tra risultati attesi dall'utente (inesperto) e risultati effettivi (ci si puo' ragionare meglio).
		String str;
		int nreaded = 0, i = 0, n = 0;
		float maxpunteggio = Float.MIN_VALUE, punteggio;
		boolean afbLoadingEnded = false;
		String[] strsrcFilteredArray = GenericFunctions.removeStopWords(srcstr, cuttingWords, splitDelimitersPattern);//srcstr e` la stringa immessa dall'utente per la ricerca.

		while(true) {
			afbLoadingEnded = afb.isLoaded();
			while((nreaded + i) < afb.getLastIndex()) {
			    if(afb.buffer[nreaded + i] != '.' && i < inchar.length) {//Le frasi sono delimitate dai punti.
					inchar[i] = (char) afb.buffer[nreaded + i];//inchar[] viene riempita con una frase intera.
				}
				else {
					str = String.copyValueOf(inchar, 0, i);

//Utilizza tutte le funzionalita` di Psynthesis ma la sintesi di frasi e ad un primo approccio sembrerebbe inadeguata invece risulta la piu` azzeccata. Un vero mitero.
					if(str.length() > 12) { //Escludo frasi di piccole dimensioni perche' possono essere singole parole che sbilanciano la sintesi.
						//System.out.println(str);
						psyn.insert(GenericFunctions.removeStopWords(str, cuttingWords, splitDelimitersPattern));
						if(n++ > nphrases) {//Dopo aver inserito nphrases frasi calcola il punteggio di affinita' con la stringa di ricerca.
							n = 0;
							if(maxpunteggio < (punteggio = psyn.calcolaPunteggio(strsrcFilteredArray)) && punteggio != 0) {
								maxpunteggio = punteggio;
							}
							//System.err.print(" "+punteggio+" ");
							//psyn.azzera();//Meglio di NO!!!
						}


/*/Nuova strategia di ricerca ma con prove empiriche si dimiostra piu` precisa quella sopra e la cosa e` molto interessante.
					if(str.length() > 12) { //Escludo frasi di piccole dimensioni perche' possono essere singole parole che sbilanciano la sintesi.
						//System.out.println(str);
					    punteggioMedio += psyn.calcolaCoerenza(strsrcFilteredArray, filtro(str));
					    if(n++ > nphrases) {//Dopo aver inserito nphrases frasi calcola il punteggio di affinita' con la stringa di ricerca.
							n = 0;
							punteggioMedio /= nphrases;
							if(maxpunteggio <  punteggioMedio && punteggioMedio != 0) {
								maxpunteggio = (int)punteggioMedio;
							}
							punteggioMedio=0;
							//System.err.print(" "+punteggioMedio+" ");
						}
*/

					}
					nreaded += i;
					i = 0;
				}
				i++;
			}

			try {
				Thread.sleep(250);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(afbLoadingEnded) break;
		}

		cl.add(record, maxpunteggio);//Aggiungo il "FileInfoRecord" in Classifica.
		//System.out.println(afb.getFileName() + ": punteggio=" + maxpunteggio);
		afb.close();
	}

}