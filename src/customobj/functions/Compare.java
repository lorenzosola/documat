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

package customobj.functions;



import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import customobj.containers.FloatToObjectSimpleNode;


public class Compare {
	/**
	 * Calcola un punteggio di coerenza (o somiglianza) tra 2 stringhe date in ingresso con metodo "Morge Elkan" modificato. (almeno per le stringhe lunghe). La stringa piu` corta, detta A, viene
	 * considerata in tutte le sue sottostringhe, la ricorrenza di ognuna delle quali viene ricercata nella stringa piu` lunga, detta B. Ad ogni ricorrenza si somma al risultato il valore
	 * weight/(Len(A) - lsub +1); dove weight e` un valore calcolato dalla funzione 'peso' che consiste nella significativita` della sottostringa in funzione della sola sua lunghezza ed lsub e` la
	 * lunghezza della sottostringa considerata. Il sisultato della somma viene diviso per per un valore di normalizzazione che consiste nella stessa somma ipotizzando che tutte le sottostringhe siano
	 * state trovate. Il risultato viene moltiplicato per il coefficiente Len(A) / Len(B) in quanto la prob. che una sottostringa A senza valori ripetuti sia inclusa in una stringa B corrisponde a P *
	 * (Len(B) / Len(A)) dove p e` la prob. che A sia riscontrata in B: P = (1/s)^n * (Len(B)-Len(A)+1). Se le stringhe sono di testo si consiglia sempre di effettuare una normalizzazione
	 * particolarmente spinta prima di passarle alla funzione come la eliminazione degli spazi anche tra le parole e la conversione in hier o lower case totale.
	 * 
	 * @param mediumWordLength
	 *            lunghezza media di una parola tra tutte quelle possibilmente presenti nelle stringhe esaminate. Normalamente puo` essere 5 o 6 se la lingua, per esempio, e` l'italiano.
	 * @return float contenente un valore (0,1) corrispondente alla probabilita` che A contenga le stesse informazioni di B.
	 */
	public static float similarityFunction(byte[] A, byte B[], int mediumWordLength) {
		if(A.length > B.length) {
			byte[] temp = A;
			A = B;
			B = temp;
		}

		float score = 0;
		int lsub;
		int Apos;
		int Bpos;
		int n;
		float normalizer = 0;
		double weight;
		double deltaScore;
		for(lsub = 1; lsub <= A.length; lsub++) { // lsub e' la lunghezza della sottostringa
			weight = (double) lsub / (lsub + mediumWordLength);// Funzione peso di sottostringa. Questa e W=len/(len+MWL) dove len e` la lunghezza della sottostringa e MWL e` la lunghezza media di
																// pasola;
			// System.out.println(weight);
			// weight = (double)lsub;
			for(Apos = 0; Apos + lsub <= A.length; Apos++) {
				deltaScore = weight / (A.length - lsub + 1);
				normalizer += deltaScore;
				for(Bpos = 0; Bpos + lsub <= B.length; Bpos++) {
					for(n = 0; n < lsub; n++) { // Trovo la corrispondenza della sottostringa.
						if(A[Apos + n] != B[Bpos + n]) break;
					}
					if(n == lsub) { // Significa che ho trovato una ricorrenza della sottostringa lsub.
						score += deltaScore;
						break; // Questa se il punteggio deve essere incrementato una sola volta all'esistenza della sottostringa a nell'array B indipendentemente dal numero di ricorrenze.
					}
				}
			}
		}
		score /= normalizer;
		score *= ((float) A.length / (float) B.length);
		// System.out.println(b.length());
		return score;
	}

	/**
	 * Calcola un punteggio di coerenza (o somiglianza) tra 2 stringhe date in ingresso con metodo "Morge Elkan" modificato. (almeno per le stringhe lunghe). La stringa piu` corta, detta A, viene
	 * considerata in tutte le sue sottostringhe, la ricorrenza di ognuna delle quali viene ricercata nella stringa piu` lunga, detta B. Ad ogni ricorrenza si somma al risultato il valore
	 * weight/(Len(A) - lsub +1); dove weight e` un valore calcolato dalla funzione 'peso' che consiste nella significativita` della sottostringa in funzione della sola sua lunghezza ed lsub e` la
	 * lunghezza della sottostringa considerata. Il sisultato della somma viene diviso per per un valore di normalizzazione che consiste nella stessa somma ipotizzando che tutte le sottostringhe siano
	 * state trovate. Il risultato viene moltiplicato per il coefficiente Len(A) / Len(B) in quanto la prob. che una sottostringa A senza valori ripetuti sia inclusa in una stringa B corrisponde a P *
	 * (Len(B) / Len(A)) dove p e` la prob. che A sia riscontrata in B: P = (1/s)^n * (Len(B)-Len(A)+1). Se le stringhe sono di testo si consiglia sempre di effettuare una normalizzazione
	 * particolarmente spinta prima di passarle alla funzione come la eliminazione degli spazi anche tra le parole e la conversione in hier o lower case totale.
	 * 
	 * @param mediumWordLength
	 *            lunghezza media di una parola tra tutte quelle possibilmente presenti nelle stringhe esaminate. Normalamente puo` essere 5 o 6 se la lingua, per esempio, e` l'italiano.
	 * @return float contenente un valore (0,1) corrispondente alla probabilita` che A contenga le stesse informazioni di B.
	 */
	public static float similarityFunction(char[] A, char B[], int mediumWordLength) {
		if(A.length > B.length) {
			char[] temp = A;
			A = B;
			B = temp;
		}

		float score = 0;
		int lsub;
		int Apos;
		int Bpos;
		int n;
		float normalizer = 0;
		double weight;
		double deltaScore;
		for(lsub = 1; lsub <= A.length; lsub++) { // lsub e' la lunghezza della sottostringa
			weight = (double) lsub / (lsub + mediumWordLength);// Funzione peso di sottostringa. Questa e W=len/(len+MWL) dove len e` la lunghezza della sottostringa e MWL e` la lunghezza media di
																// pasola;
			// System.out.println(weight);
			// weight = (double)lsub;
			for(Apos = 0; Apos + lsub <= A.length; Apos++) {
				deltaScore = weight / (A.length - lsub + 1);
				normalizer += deltaScore;
				for(Bpos = 0; Bpos + lsub <= B.length; Bpos++) {
					for(n = 0; n < lsub; n++) { // Trovo la corrispondenza della sottostringa.
						if(A[Apos + n] != B[Bpos + n]) break;
					}
					if(n == lsub) { // Significa che ho trovato una ricorrenza della sottostringa lsub.
						score += deltaScore;
						break; // Questa se il punteggio deve essere incrementato una sola volta all'esistenza della sottostringa a nell'array B indipendentemente dal numero di ricorrenze.
					}
				}
			}
		}
		score /= normalizer;
		score *= ((float) A.length / (float) B.length);
		// System.out.println(b.length());
		return score;
	}

	/**
	 * Calcola l'affinita` tra 2 vettori di parole, dei quali il primo (rs) e` specificato dal primo argomento della funzione sotto forma di ResultSet avente necessariamente i primi tre campi
	 * rispettivamente e nell'ordine seguente: 'indice di tipo int che contraddistingue il vettore','stringa che memorizza la parola','float che contiene il peso'; ed il secondo (array) e` in
	 * forma di array di byte contenenti il testo da analizzare. Dal testo vengono estratte le parole basandosi su una serie di separatori normalmente utilizzati per separare le parole da un testo
	 * generico e di queste valutato quindi il peso in base al numero di ricorrenze.
	 * 
	 * @param rs I campi contenuti devono essere, nello stesso ordine: 'indice di tipo int che contraddistingue il vettore di parole','stringa che memorizza la parola','float che contiene il peso'
	 * @param array di byte con il contenuto da confrontare.
	 * @return indice del vettore di parole (vedere struttura del Result Set) che maggiormante risulta affine all'array passato 
	 * @throws ComputationException
	 */
	public static int dbVectorsMaximumAffinity(ResultSet rs, byte[] array)
			throws ComputationException {
		class Moduluses implements Comparable<Moduluses> {// Ogni istanza di questa classe viene associata ad un gruppo ed inserita nell'albero scores con indice, appunto l'indice del gruppo a cui
															// e` associata.
			float scalar = 0;// Alla fine del calcolo conterra` il prodotto scalare dei 2 vettori: occorrenza delle parole del gruppo, occorrenza delle parole del documento presenti anche nel
								// gruppo rappresentato dal primo vettore messe nello stesso ordine.
			float groupsModulus = 0;// Alla fine del calcolo conterra` il modulo del vettore occorrenza delle parole del gruppo.
			float documentsModulus = 0;// Alla fine del calcolo conterra` il modulo del vettore occorrenza delle parole del documento presenti anche tra le parole del gruppo.
			public int compareTo(Moduluses o) {
				//System.out.println(scalar + "	" + groupsModulus + "	" + documentsModulus + "\n");// DEBUG
				//System.out.println(o.scalar + "	" + o.groupsModulus + "	" + o.documentsModulus + "\n");// DEBUG
				//System.out.println((scalar / (Math.sqrt(groupsModulus) + o.documentsModulus)) + "	" + (scalar / (Math.sqrt(o.groupsModulus + o.documentsModulus))));
				return (int) Math.signum((scalar / (Math.sqrt(groupsModulus + documentsModulus)))
						- (o.scalar / (Math.sqrt(o.groupsModulus + o.documentsModulus))));
			}
		}
		
		HashMap<String, Integer> documentWordsDictionary = new HashMap<String, Integer>();
		StringBuffer strBuff = new StringBuffer();
		int stato;
		// short endIgnoration = 1;// Number of character to be ignored at the end of the words during comparation.
		FloatToObjectSimpleNode scores = new FloatToObjectSimpleNode();
		double grpWordScore;
		Moduluses tmpModuluses = null;
		FloatToObjectSimpleNode tmpSimpleNode;
		int i = 0;
		stato = 0;
		Integer tmpInteger;
		String tmpString;
		//Costruzione del dizionario con valore di rilevanza per ogni parola.
		for(i = 0; i < array.length;) {
			switch (stato) {
				case 0:// Trovato spazio: comincio controllo.
					if(array[i] == ' ' || array[i] == '\t' || array[i] == '\r'
							|| array[i] == '\n' || array[i] == '\f'
							|| array[i] == ',' || array[i] == '.'
							|| array[i] == ':' || array[i] == ';'
							|| array[i] == '(' || array[i] == ')'
							|| array[i] == '{' || array[i] == '}'
							|| array[i] == '[' || array[i] == ']'
							|| array[i] == '+' || array[i] == '='
							|| array[i] == '!' || array[i] == '?'
							|| array[i] == '*' || array[i] == '/'
							|| array[i] == '-' || array[i] == '#'
							|| array[i] == '@' || array[i] == '^'
							|| array[i] == '<' || array[i] == '>'
							|| array[i] == '`' || array[i] == '|'
							|| array[i] == '\'' || array[i] == '\\'
							|| array[i] == '%' || array[i] == '&'
							|| array[i] == '"' || array[i] == '$'
							|| array[i] == '~' || array[i] == '_') {// Trovata una occorrenza.
					
						stato = 1;
//						if(strBuff.l)
						tmpString = strBuff.toString();
						strBuff.setLength(0);
						if((tmpInteger = documentWordsDictionary.get(tmpString)) == null)
							documentWordsDictionary.put(tmpString, new Integer(1));
						else {
							tmpInteger += 1;
							documentWordsDictionary.put(tmpString, tmpInteger);
						}
					}
					else {
						strBuff.append((char)array[i]);
					}
					i++;
					break;
				case 1:// Ricerca spazio iniziale.
					if(array[i] == ' ' || array[i] == '\t' || array[i] == '\r'
							|| array[i] == '\n' || array[i] == '\f' || array[i] == ','
							|| array[i] == '.' || array[i] == ':' || array[i] == ';'
							|| array[i] == '(' || array[i] == ')' || array[i] == '{'
							|| array[i] == '}' || array[i] == '[' || array[i] == ']'
							|| array[i] == '+' || array[i] == '=' || array[i] == '!'
							|| array[i] == '?' || array[i] == '*' || array[i] == '/'
							|| array[i] == '-' || array[i] == '#' || array[i] == '@'
							|| array[i] == '^' || array[i] == '<' || array[i] == '>'
							|| array[i] == '`' || array[i] == '|' || array[i] == '\''
							|| array[i] == '\\' || array[i] == '%' || array[i] == '&'
							|| array[i] == '"' || array[i] == '$' || array[i] == '~'
							|| array[i] == '_') {
						i++;
					}
					else stato = 0;
					
					/* Non e` necessario che ci siano tutti i simboli delimitatori in assoluto, infatti, per come e` fatta la funzione, quanto piu` l'insieme dei delimitatori e` piccolo tanto
					 * piu` la ricerca sara` selettiva nelle parole da considerare per l'eliminazione. Chiaramente la condizione ideale e` quella di fornire un insieme di stop word il cui
					 * siconoscimento all'interno degli stream da cui sono state ricavate viene fatto con l'uso degli stessi delimitatori: si puo` pensare in seguito di inserire tale insieme
					 * tra i parametri della funzione. L'insieme utilizzato in questa funzione attualmente corrisponde a quello utilizzato normalmente da tutte le altre funzioni di splitting
					 * per l'individuazione delle parole da un testo e si puo` considerare completo. Non e` possibile individuare pattern generici ma questo puo` rientrare in una futura
					 * revisione.
					 */
					break;
			}
		}
		
		try {
			// Il calcolo e`, fondamentalmente, il test del coseno tra vettori di parole in cui ogni parole e` associata ad un simbolo (grandezza) il cui peso e` quello della parola stessa per il
			// gruppo considerato per il primo vettore (vettore "gruppo"), ed il numero di sue ricorrenze all'interno del documento per il secondo vettore (vettore "documento").
			while(rs.next()) {
				if((tmpInteger = documentWordsDictionary.get(rs.getString(2))) == null) continue;//Mette in tmpInteger il numero di volte che la parola appartenente al gruppo copare nel documento, se essa non compare tmpInteger sara` null. 
				grpWordScore = rs.getDouble(3);
				tmpSimpleNode = scores.get(rs.getInt(1));
				if(tmpSimpleNode == null) {
					tmpModuluses = new Moduluses();
					scores.append(rs.getInt(1), tmpModuluses, true);
				}
				else tmpModuluses = (Moduluses) (tmpSimpleNode.getValue());

				tmpModuluses.scalar += tmpInteger * grpWordScore;
				tmpModuluses.groupsModulus += grpWordScore * grpWordScore;
				tmpModuluses.documentsModulus += tmpInteger * tmpInteger;
			}
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			// System.out.println(parola+" "+i+" "+pos);
			throw new ComputationException("Probabily there is a \"null\" word in a DB vector.");
		}

		// Controllo eventuali problemi.
		if(scores.belowSize() == 0) throw new ComputationException("No recurrences found.");
		
//		La seguente routine (commentata) e` di implementazione antecedente a quella sopra della quale ha lo stesso esito, richiede minor occupazione di memoria, ma e` notevolmente piu` lenta (quella nuova fa uso di una HashMap per rappresentare il vettore del documento).  		
/*		int stato, pos = 0;
		byte[] parola = null;
		// short endIgnoration = 1;// Number of character to be ignored at the end of the words during comparation.
		SimpleNode scores = new SimpleNode();
		double grpWordScore, documentWordScore;
		Moduluses tmpModuluses = null;
		SimpleNode tmpSimpleNode;
		int i = 0;
		try {
			while(rs.next()) {
				parola = rs.getString(2).trim().getBytes();
				stato = 0;
				documentWordScore = 0;
				pos = 0;

				// Cerco quante occorrenze esistono della parola cercata.
				for(i = 0; i < array.length; i++) {
					// System.out.print((char)array[i]);//DEBUG
					switch (stato) {
						case 0:// Trovato spazio: comincio controllo.
							if(array[i] == parola[pos]) {
								pos++;
								if(pos == parola.length) {// - endIgnoration) {
									// .............qualcosa nelle righe seguenti non va: vengono confrontate le parole dei vettori caratterizzanti con porzioni generiche dell'array di testo che
									// quindi non rispetta una suddivisione in parole. Questo è assolutamente sbagliato.
									i++;
									if(array[i] == ' ' || array[i] == '\t' || array[i] == '\r'
											|| array[i] == '\n' || array[i] == '\f'
											|| array[i] == ',' || array[i] == '.'
											|| array[i] == ':' || array[i] == ';'
											|| array[i] == '(' || array[i] == ')'
											|| array[i] == '{' || array[i] == '}'
											|| array[i] == '[' || array[i] == ']'
											|| array[i] == '+' || array[i] == '='
											|| array[i] == '!' || array[i] == '?'
											|| array[i] == '*' || array[i] == '/'
											|| array[i] == '-' || array[i] == '#'
											|| array[i] == '@' || array[i] == '^'
											|| array[i] == '<' || array[i] == '>'
											|| array[i] == '`' || array[i] == '|'
											|| array[i] == '\'' || array[i] == '\\'
											|| array[i] == '%' || array[i] == '&'
											|| array[i] == '"' || array[i] == '$'
											|| array[i] == '~' || array[i] == '_') documentWordScore++;// Trovata una occorrenza.
									pos = 0;
									stato = 1;
								}
							}
							else {
								pos = 0;
								stato = 1;
							}
							break;
						case 1:// Ricerca spazio iniziale.
							if(array[i] == ' ' || array[i] == '\t' || array[i] == '\r'
									|| array[i] == '\n' || array[i] == '\f' || array[i] == ','
									|| array[i] == '.' || array[i] == ':' || array[i] == ';'
									|| array[i] == '(' || array[i] == ')' || array[i] == '{'
									|| array[i] == '}' || array[i] == '[' || array[i] == ']'
									|| array[i] == '+' || array[i] == '=' || array[i] == '!'
									|| array[i] == '?' || array[i] == '*' || array[i] == '/'
									|| array[i] == '-' || array[i] == '#' || array[i] == '@'
									|| array[i] == '^' || array[i] == '<' || array[i] == '>'
									|| array[i] == '`' || array[i] == '|' || array[i] == '\''
									|| array[i] == '\\' || array[i] == '%' || array[i] == '&'
									|| array[i] == '"' || array[i] == '$' || array[i] == '~'
									|| array[i] == '_') stato = 0;
							
							  Non e` necessario che ci siano tutti i simboli delimitatori in assoluto, infatti, per come e` fatta la funzione, quanto piu` l'insieme dei delimitatori e` piccolo tanto
							 * piu` la ricerca sara` selettiva nelle parole da considerare per l'eliminazione. Chiaramente la condizione ideale e` quella di fornire un insieme di stop word il cui
							 * siconoscimento all'interno degli stream da cui sono state ricavate viene fatto con l'uso degli stessi delimitatori: si puo` pensare in seguito di inserire tale insieme
							 * tra i parametri della funzione. L'insieme utilizzato in questa funzione attualmente corrisponde a quello utilizzato normalmente da tutte le altre funzioni di splitting
							 * per l'individuazione delle parole da un testo e si puo` considerare completo. Non e` possibile individuare pattern generici ma questo puo` rientrare in una futura
							 * revisione.
							 
							 
							break;
					}
				}
				// Il calcolo e`, fondamentalmente, il test del coseno tra vettori di parole in cui ogni parole e` associata ad un simbolo (grandezza) il cui peso e` quello della parola stessa per il
				// gruppo considerato per il primo vettore (vettore "gruppo"), ed il numero di sue ricorrenze all'interno del documento per il secondo vettore (vettore "documento").
				if(documentWordScore != 0) {
					grpWordScore = rs.getDouble(3);
					tmpSimpleNode = scores.get(rs.getInt(1));
					if(tmpSimpleNode == null) {
						tmpModuluses = new Moduluses();
						scores.append(rs.getInt(1), tmpModuluses, true);
					}
					else tmpModuluses = (Moduluses) (tmpSimpleNode.getValue());

					tmpModuluses.scalar += documentWordScore * grpWordScore;
					tmpModuluses.groupsModulus += grpWordScore * grpWordScore;
					tmpModuluses.documentsModulus += documentWordScore * documentWordScore;
				}
			}
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			// System.out.println(parola+" "+i+" "+pos);
			throw new ComputationException("Probabily there is a \"null\" word in a DB vector.");
		}
		if(parola == null) throw new ComputationException(
				"No words in vectors to compute comparison.");

		// Controllo eventuali problemi.
		if(scores.belowSize() == 0) throw new ComputationException("No recurrences found.");
*/
		
		// Ora procedo con la ricerca del gruppo con punteggio massimo.
		// Ora devo trovare il gruppo che ha il massimo valore del calcolo punteggio/(moduloGruppo * moduloDocumentoGruppo) comunque aggiungendo il metodo "divide" che opera similmente a addValue in
		// un FLoat simple node si potrebbe utilizzare un FloatSimpleNode ed evitare tutta questa procedura...

		return Math.round(scores.getMaxValueSubNode(tmpModuluses).getIndex());// Il valore viene convertito in intero senza incorrere in errori perche` si ipotizza che gli oggetti
	}
}
