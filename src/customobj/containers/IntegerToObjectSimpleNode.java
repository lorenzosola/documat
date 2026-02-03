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
 * Created on Nov 9, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package customobj.containers;

import java.util.Vector;

/**
 * Nodi che formano un albero bilanciato di coppie (idice , oggetto) limitato in numero di elementi tramite scarto di quelli a minor indice. Buona efficienza (il massimo che ho potuto ottenere); massimo numero di elementi contenuti = 2^31. La massima efficienza si ha se il numero di inserimenti totali non supera il numero massimo desiderato di elementi contenuti perche' in tal caso, oltre al fatto
 * non si devono fare cancellazioni,il primo nodo conserva una funzione di selettore binario la quale decade abbastanza rapidamente mano a mano che vengono cancellati elementi (sempre dal suo ramo sinistro). Il primo nodo, infatti, non deve contenere elementi ma fare solo la funzione di puntatore all'albero, indispensabile per le operazioni di cancellazione e bilanciamento. Il bilanciamento del
 * ramo destro e sinistro di tale nodo esula dalla struttura ricorsiva per cui ? stata implementata in modo molto limitato rispetto a quella che avviene per i nodi figli.
 * 
 * @author lsola
 */
public class IntegerToObjectSimpleNode {
	IntegerToObjectSimpleNode phather = null;
	IntegerToObjectSimpleNode leftchild = null;// Il SimpleNode con indice minore;
	IntegerToObjectSimpleNode rightchild = null;// Il SimpleNode con indice maggiore;
	private long index = 0;
	private Object value;
	// static int actualElNumberer;//Numero di elementi raggiunto dall'albero (pue' essere leggermnte maggiore dal momento le chiamate non sono sincronizzato ma questo non costituisce un problema).
	// int height;//Distanza in numero di livelli del nodo rispetto alla root (da utilizzare per il bilanciamento).
	final public IntegerToObjectSimpleNode rootElement;
	private boolean isRootNode;
	// Dal momento che il valore dell'index nel nodo root puo' essere variato entro i limiti in cui la condizione di ordinamento rimanga soddistfatta
	// si puo' utilizzare tale grado di liberta' per aumentare il piu' possibile il bilanciamento tra i due rami.
	// A tale scopo le segueti 2 variabili contengono gli indici delle 2 altezze.
	int leftElNumber;// Numero di elementi del sottoalbero a sinistra.
	int rightElNumber;// Numero di elementi del sottoalbero a destra.
	final public int maxElNumber;// Numero totale di elementi ammessi.

	/**
	 * Costruttore da utilizzare per il primo elemento dell'albero. Il numero massimo di elementi che verranno mantenuti nell'albero (con preferenza er gli ID piu` alti) sara` Integer.MAX_VALUE-1.
	 */
	public IntegerToObjectSimpleNode() {
		this(Integer.MAX_VALUE - 1);
	}

	/**
	 * Costruttore da utilizzare per il primo elemento dell'albero.
	 * 
	 * @param maxElNumber
	 *            numero massimo di elementi che verranno mantenuti nell'albero con precedenza agli ID piu` alti.
	 */
	public IntegerToObjectSimpleNode(int maxElements) {
		if(maxElements == 0) this.maxElNumber = Integer.MAX_VALUE;
		else this.maxElNumber = maxElements;
		this.isRootNode = true;// Segnalo a questo nodo che e' la radice.
		rootElement = this;
	}

	/**
	 * Questo costruttore esiste per permettere alla append(...) di funzionare comunque nulla vieta all, applicativo di utilizzarlo con coscienza.
	 * 
	 * @param phather
	 * @param index
	 * @param value
	 */
	public IntegerToObjectSimpleNode(IntegerToObjectSimpleNode phather, long index, Object value) {
		this.rootElement = phather.rootElement;
		this.maxElNumber = phather.maxElNumber;
		this.phather = phather;
		this.index = index;
		this.value = value;
	}

	public void clearSubTree() {
		if(leftchild != null) {
			leftchild.clearSubTree();
		}
		if(rightchild != null) {
			rightchild.clearSubTree();
		}
		if(leftchild == null && rightchild == null) {
			if(!isRootNode) {
				if(phather.leftchild == this) {
					phather.leftchild = null;
				}
				else {
					phather.rightchild = null;
				}
			}
			value = null;
			index = 0;
			phather = null;// (mette a null la variabile locale e non l'oggetto pather).
			leftElNumber = 0;
			rightElNumber = 0;
		}
	}

	/**
	 * @param index
	 *            punteggio che determina la posizione.
	 * @param value
	 *            oggetto che verra' associato al nodo creato.
	 * @param withReplacing
	 *            se true nel caso in cui esista un nodo con stesso indice di quello da inserire allora tale nodo verra` sostituito con quello nuovo; se false l'oggetto viene comunque inserito.
	 * @return 0 se l'elemento non e' stato inserito, 1 se l'elemento e' stato inserito normalmente nell'albero.
	 */
	// Tutto OK ma bisogna renderla non ricorsiva!!!!
	public synchronized short append(long index, Object value, boolean withReplacing) {
		// Questa procedura di inserimento non contempla il ribilanciamento dell'albero perche' il numero
		// di elementi non e' tale da renderlo indispensabile.
		// Il syncronizing posto su questo restringe l'accesso a tutto l'albero ad un solo thread per via del fatto
		// che il metodo sul nodo padre non esce fino a che non e' completamente concluso l'inserimento.
		short tmp = 0;
		// SimpleNode trans = this;

		if((leftElNumber + rightElNumber) < maxElNumber) {// Questa condizione non sara' verificata solo nel nodo root.
			if(index > this.index) {
				if(rightchild == null) {
					rightchild = new IntegerToObjectSimpleNode(this, index, value);
					rightElNumber++;
					tmp = 1;
				}
				else {
					tmp = rightchild.append(index, value, withReplacing);
					rightElNumber += tmp;
				}
			}
			else if(index < this.index || (index == this.index && !withReplacing)) {// Sostituzione oggetto (e` l'unica differenza rispetto a quello presente in documat).
				if(leftchild == null) {
					leftchild = new IntegerToObjectSimpleNode(this, index, value);
					leftElNumber++;
					tmp = 1;
				}
				else {
					tmp = leftchild.append(index, value, withReplacing);
					leftElNumber += tmp;
				}
			}
			else {// Sostituzione oggetto (e` l'unica differenza rispetto a quello presente in documat).
				if(!isRootNode) {
					this.value = value;
					return 0;// Perche` non vi e` stato alcun incremento del numero di nodi.
				}
				else if(leftchild != null) return leftchild.append(index, value, withReplacing);// Infatti la regola prestabilita e` che se l'elemento risulta = a quello da inserire esso va posto nel nodo di sinistra per cui e` proprio nel sottoalbero a sinistra che trovero` l'elemento =.
				// else if(rightchild != null) return rightchild.append(index, value, withReplacing);
				else {
					return append(index, value, false);// Se arriva qui significa che questo e` il root node ed il nodo va inserito (a sinistra) quindi richiamo il metodo forzando l'inserimento.
				}
			}
		}
		else {// Cerca l'elemento con minor punteggio e se il punteggio dell'elemento che si vuole inserire...
			// Trovo il minor nodo.
			IntegerToObjectSimpleNode tmpNode = getMin();

			if(tmpNode.index <= index) {
				tmpNode.remove(tmpNode.index);
				// Ora trans e' il nodo root.
				this.append(index, value, withReplacing);
			}
		}
		if(tmp != 0) bilanciamentoNodo();
		return tmp;
	}

	/**
	 * @param list
	 *            vettore che verra' riempito con la lista dei nodi del sottoalbero in ordine ascendente di punteggio.
	 */
	public void fillVectorAsc(Vector<IntegerToObjectSimpleNode> list) {
		list.clear();
		fillVectorAscRec(list);
	}

	private void fillVectorAscRec(Vector<IntegerToObjectSimpleNode> list) {
		if(leftchild != null) {
			leftchild.fillVectorAscRec(list);
		}
		if(value != null) list.add(this);
		if(rightchild != null) {
			rightchild.fillVectorAscRec(list);
		}
	}

	/**
	 * @param list
	 *            vettore che verra' riempito con la lista dei nodi del sottoalbero in ordine dicendente di punteggio.
	 */
	public void fillVectorDisc(Vector<IntegerToObjectSimpleNode> list) {
		list.clear();
		fillVectorDiscRec(list);
	}

	private void fillVectorDiscRec(Vector<IntegerToObjectSimpleNode> list) {
		if(rightchild != null) {
			rightchild.fillVectorDiscRec(list);
		}
		if(value != null) list.add(this);
		if(leftchild != null) {
			leftchild.fillVectorDiscRec(list);
		}
	}

	/**
	 * La funzione non ricorsiva ritorna il nodo il cui indice corrispondente a quello passato come argomento cercandolo tra quelli del proprio sottoalbero. Non e' ricorsiva per motivi di efficienza. Se l'indice non e' presente nel sotto albero ritorna null.
	 * 
	 * @param index
	 * @return oggetto avente come indice quello passato.
	 */
	public IntegerToObjectSimpleNode get(float index) {
		IntegerToObjectSimpleNode trans = this;
		if(trans.isRootNode && trans.index == index) {// Se si tratta del nodo root devo prosequire l'esplorazione con lo stesso criterio dell'inserimento: cominciando da sinistra.
			if(trans.leftchild != null) trans = trans.leftchild;
			else if(trans.rightchild != null) trans = trans.rightchild;
			else return null;
		}
		while(true) {
			if(index > trans.index) {
				if(trans.rightchild != null) {
					trans = trans.rightchild;
				}
				else {
					return null;
				}
			}
			else if(index < trans.index) {
				if(trans.leftchild != null) {
					trans = trans.leftchild;
				}
				else {
					return null;
				}
			}
			else break;
		}
		return trans;
	}

	/**
	 * La seguente funzione non ricorsiva ritorna il nodo con indice corrispondente a quello passato come argomento cercandolo tra quelli del proprio sottoalbero, se questo esiste. Altrimenti ritorna l'oggetto con indice piu' vicino tra quelli presenti nell'albero.
	 * 
	 * @param index
	 * @return SimpleNode
	 */
	public IntegerToObjectSimpleNode getNear(long index) {
		IntegerToObjectSimpleNode trans = this, near = null;
		double tmp, nimDiff = Double.MAX_VALUE;// Minimo della differenza in valore assoluto tra l'indice del nodi di volta in volta visitati e l'indice cercato.
		boolean cicla = true;
		if(this.leftchild == null) trans = rightchild;// Se il rootNode non ha figli a sinistra ......
		if(trans.isRootNode && trans.index == index) {// Se si tratta del nodo root devo prosequire l'esplorazione con lo stesso criterio dell'inserimento: cominciando da sinistra.
			if(trans.leftchild != null) trans = trans.leftchild;
			else if(trans.rightchild != null) trans = trans.rightchild;
			else return null;
		}
		while(cicla) {
			if(index > trans.index) {
				if(trans.rightchild != null) {
					trans = trans.rightchild;
				}
				else cicla = false;
			}
			else if(index < trans.index) {
				if(trans.leftchild != null) {
					trans = trans.leftchild;
				}
				else cicla = false;
			}
			else {
				near = trans;
				cicla = false;
			}
			if((tmp = Math.abs((trans.index - index))) < nimDiff) {
				nimDiff = tmp;
				near = trans;
			}
		}
		if(near != null) return near;
		else return null;
	}

	public long getIndex() {
		return index;
	}

	/**
	 * Ritorna l'oggetto associato a questo stesso nodo.
	 * 
	 * @return Object
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Funzione non ricorsiva che ritorna il nodo con indice massimo del sottoalbero avente per radice il nodo che effettua la chiamata oppure null se l'albero e' vuoto.
	 * 
	 * @return SimpleNode
	 */
	public IntegerToObjectSimpleNode getMax() {
		IntegerToObjectSimpleNode trans = null;
		if(isRootNode) {
			if(rightchild != null) trans = rightchild;
			else if(leftchild != null) trans = leftchild;
			else return null;
		}
		else if(rightchild != null) trans = rightchild;
		else trans = this;

		while(trans.rightchild != null)
			trans = trans.rightchild;

		return trans;
	}

	/**
	 * Funzione non ricorsiva che ritorna il nodo con indice minimo del sottoalbero avente per radice il nodo che effettua la chiamata oppure null se l'albero e' vuoto.
	 * 
	 * @return SimpleNode
	 */
	public IntegerToObjectSimpleNode getMin() {
		IntegerToObjectSimpleNode trans = null;
		if(isRootNode) {
			if(leftchild != null) trans = leftchild;
			else if(rightchild != null) trans = rightchild;
		}
		else if(leftchild != null) trans = leftchild;
		else trans = this;

		while(trans.leftchild != null)
			trans = trans.leftchild;

		return trans;
	}

	/**
	 * La funzione non ricorsiva opera la rimozione del nodo il cui indice e' specificato come argomento. Se il nodo viene effettivamente eliminato ritorna true altrimenti (il nodo non esiste) ritorna false;
	 * 
	 * @param index
	 *            indice del nodo da eliminare.
	 * @return true se il nodo esiste, false se il nodo non esiste.
	 */
	public boolean remove(float index) {
		IntegerToObjectSimpleNode trans = get(index), replacing;
		if(trans == null) return false;

		if(trans.leftchild == null && trans.rightchild == null) {
			if(trans == trans.phather.rightchild) {
				trans.phather.rightchild = null;
			}
			else trans.phather.leftchild = null;
			updateUpperWeight(trans, -1);
			return true; // Il nodo e` stato correttamente rimosso.
		}

		// Ora cerco la replacing adeguata del ramo piu` lungo sottostante a quello da sostituire in questo modo si contribuisce, anche se poco, a mantenere bilanciato l'albero.
		replacing = trans;
		if(replacing.rightElNumber >= replacing.leftElNumber && replacing.rightchild != null) {
			replacing = replacing.rightchild;
			while(replacing.leftchild != null) {
				replacing = replacing.leftchild;
			}
		}
		else if(replacing.leftchild != null) {
			replacing = replacing.leftchild;
			while(replacing.rightchild != null) {
				replacing = replacing.rightchild;
			}
		}

		/*
		 * Ora le operazioni da fare in sequenza dovranno essere le seguenti: -si aggiornano i pesi al di sopra del replacing (decremento unitario). -se il replacing non ha figli si svincola semplicemente dall'albero. -se il replacing ha un figlio, unico per definizione, questo va inserito al posto del replacing che rimane cosi` svincolato -si sostituisce il replaced con il replacing ereditando
		 * anche i pesi (gia` aggiornati esattamente nel primo passo). -TUTTO OK
		 */
		updateUpperWeight(replacing, -1);

		// Rimozione replacing dall'albero
		if(replacing.rightchild == null && replacing.leftchild == null) {// replacing senza figli
			// Caso in cui la replacing sia un nodo "destro".
			if(replacing == replacing.phather.rightchild) {
				replacing.phather.rightchild = null;
			}
			// Caso in cui la replacing sia un nodo "sinistro".
			else {
				replacing.phather.leftchild = null;
			}
		}
		else if(replacing.rightchild != null) {// Caso in cui il replacing abbia un figlio a destra.
			// A questo punto il replacing puo` avere solo un figlio ma lo stesso replacing puo` essere sia di destra sia di sinistra a seconda della situazione (!!!).
			if(replacing.phather.rightchild == replacing) replacing.phather.rightchild = replacing.rightchild;
			else replacing.phather.leftchild = replacing.rightchild;
			replacing.rightchild.phather = replacing.phather;
		}
		else {
			// A questo punto il replacing puo` avere solo un figlio che puo` essere di destra o di sinistra a seconda della situazione (!!!).
			if(replacing.phather.rightchild == replacing) replacing.phather.rightchild = replacing.leftchild;
			else replacing.phather.leftchild = replacing.leftchild;
			replacing.leftchild.phather = replacing.phather;
		}

		replaceNode(trans, replacing);
		trans = null;
		replacing = null;
		return true;
	}

	/**
	 * Bilancia l'albero sovrastante il nodo indicato incrementando (di un numero intero quindi anche negativo) i pesi. Come esempio si puo` avere che per la rimozione di del nodo specificato si chiami la updateUpperWeight(sn, -1);.
	 * 
	 * @param sn
	 *            nodo a partire dal quale verranno aggiornai i pesi nei nodi sovrastanti (quindi non nel nodo stesso).
	 * @param diff
	 */
	private void updateUpperWeight(IntegerToObjectSimpleNode sn, int diff) {
		// Correzione elnumber dei nodi sovrastanti il nodo replacing.
		IntegerToObjectSimpleNode trans;
		trans = sn;
		while(!trans.isRootNode) {
			if(trans == trans.phather.rightchild) {
				trans.phather.rightElNumber += diff;
				// if(trans.phather.rightElNumber < 0) System.err.println("SimpleNode: qualcosa non va nellaggiornamento degli elNumber nella funzione remove().");
			}
			else {
				trans.phather.leftElNumber += diff;
				// if(trans.phather.leftElNumber < 0) System.err.println("SimpleNode: qualcosa non va nellaggiornamento degli elNumber nella funzione remove().");
			}
			trans = trans.phather;
		}
	}

	/**
	 * Da usare con consapevolezza in quanto il sostituto eredita i pesi dal sostituito, inoltre gli eventuali figli del sostituto vengono persi per sempre.
	 * 
	 * @param replaced
	 * @param replacing
	 */
	private void replaceNode(IntegerToObjectSimpleNode replaced, IntegerToObjectSimpleNode replacing) {
		if(replaced == replaced.phather.rightchild) {
			replaced.phather.rightchild = replacing;
		}
		// Caso in cui il nodo da cancellare sia un nodo "sinistro".
		else {
			replaced.phather.leftchild = replacing;
		}
		replacing.leftchild = replaced.leftchild;
		replacing.rightchild = replaced.rightchild;
		replacing.rightElNumber = replaced.rightElNumber;
		replacing.leftElNumber = replaced.leftElNumber;
		replacing.phather = replaced.phather;
		if(replaced.leftchild != null) {
			if(replaced.leftchild != replacing) replaced.leftchild.phather = replacing;
		}
		if(replaced.rightchild != null) {
			if(replaced.rightchild != replacing) replaced.rightchild.phather = replacing;
		}
	}

	private static float statBilanciamento;

	/**
	 * La seguente funzione esegue una statisitca molto semplice sul bilanciamento totale dell'albero. Attenzione all'interpretazione del numero risultante: piu' variare da valori molto grandi a valori molto piccoli e questo dipende molto dalla sequenza di ingresso dei nodi. Quello che conta (per testare il corretto funzionemanto dell'albero) e' che il risultato includa, appunto, sia numeri > 1
	 * sia numeri < 1 da una statistica all'altra.
	 */
	public float balanceStat() {
		statBilanciamento = 1;
		multCoeffSxDx();
		return statBilanciamento;
	}

	/**
	 * Ritorna il numero totale di nodi del sottoalbero avente questo nodo come root.
	 * 
	 * @return int
	 */
	public int belowSize() {
		return leftElNumber + rightElNumber;
	}

	// Metodi non pubblici-------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Bilanciamento dei soli figli di questo nodo.
	 */
	private void bilanciamentoNodo() {
		int tmp;
		IntegerToObjectSimpleNode trans;
		// Il nodo padre non puo' essere bilanciato tramite rotazioni perche' non puo' accedere alla reference di se stesso per cui l'unica possibilta'
		// e' quella di modificare il suo index (l'unico che puo' essere cambiato).
		if(isRootNode) {// Se questo e' il nodo root non puo' bilanciare se stesso in quanto non ha riferimenti
			if(leftElNumber > rightElNumber) {
				trans = leftchild;
				if(trans != null) {
					while(trans.rightchild != null)
						trans = trans.rightchild;// Trovo il maggior nodo del ramo sinistro.
					this.index = trans.getIndex();
				}
			}
			else if(leftElNumber < rightElNumber) {
				trans = rightchild;
				if(trans != null) {
					while(trans.leftchild != null)
						trans = trans.leftchild;// Trovo il minor nodo del ramo destro.
					if(trans != null) this.index = trans.getIndex() - 1;// Questo per far si che l'albero soddisfi la condizione utilizzata come regola: se l'index e' <= viene scelto il nodo di sinistra.
				}
			}
		}

		// Bilanciamento dei figli.
		if(leftchild != null) {
			tmp = leftchild.leftElNumber - leftchild.rightElNumber;
			if(tmp > 2) {
				trans = leftchild.leftchild;
				leftchild.leftchild = trans.rightchild;
				if(trans.rightchild != null) {
					trans.rightchild.phather = leftchild;
				}
				trans.rightchild = leftchild;
				trans.phather = leftchild.phather;
				leftchild.phather = trans;
				leftchild = trans;

				// Aggiornamento punteggi (aggiorno solo quelli coivolti nel cambiamento).
				if(leftchild.leftchild != null) leftchild.leftElNumber = leftchild.leftchild.nElements(1);
				else leftchild.leftElNumber = 0;
				if(leftchild.rightchild != null) leftchild.rightElNumber = leftchild.rightchild.nElements(2);
				else leftchild.rightElNumber = 0;
			}
			else if(tmp < -2) {
				trans = leftchild.rightchild;
				leftchild.rightchild = trans.leftchild;
				if(trans.leftchild != null) {
					trans.leftchild.phather = leftchild;
				}
				trans.leftchild = leftchild;
				trans.phather = leftchild.phather;
				leftchild.phather = trans;
				leftchild = trans;

				// Aggiornamento punteggi (aggiorno solo quelli coivolti nel cambiamento).
				if(leftchild.leftchild != null) leftchild.leftElNumber = leftchild.leftchild.nElements(2);
				else leftchild.leftElNumber = 0;
				if(leftchild.rightchild != null) leftchild.rightElNumber = leftchild.rightchild.nElements(1);
				else leftchild.rightElNumber = 0;
			}
		}

		if(rightchild != null) {
			tmp = rightchild.leftElNumber - rightchild.rightElNumber;
			if(tmp > 2) {
				trans = rightchild.leftchild;
				rightchild.leftchild = trans.rightchild;
				if(trans.rightchild != null) {
					trans.rightchild.phather = rightchild;
				}
				trans.rightchild = rightchild;
				trans.phather = rightchild.phather;
				rightchild.phather = trans;
				rightchild = trans;

				// Aggiornamento punteggi (aggiorno solo quelli coivolti nel cambiamento).
				if(rightchild.leftchild != null) rightchild.leftElNumber = rightchild.leftchild.nElements(1);
				else rightchild.leftElNumber = 0;
				if(rightchild.rightchild != null) rightchild.rightElNumber = rightchild.rightchild.nElements(2);
				else rightchild.rightElNumber = 0;
			}
			else if(tmp < -2) {
				trans = rightchild.rightchild;
				rightchild.rightchild = trans.leftchild;
				if(trans.leftchild != null) {
					trans.leftchild.phather = rightchild;
				}
				trans.leftchild = rightchild;
				trans.phather = rightchild.phather;
				rightchild.phather = trans;
				rightchild = trans;

				// Aggiornamento punteggi (aggiorno solo quelli coivolti nel cambiamento).
				if(rightchild.leftchild != null) rightchild.leftElNumber = rightchild.leftchild.nElements(2);
				else rightchild.leftElNumber = 0;
				if(rightchild.rightchild != null) rightchild.rightElNumber = rightchild.rightchild.nElements(1);
				else rightchild.rightElNumber = 0;
			}
		}
	}

	// Aggiorna il numero di elementi del nodo andando al massimo nlevels livelli in profondita'.
	private int nElements(int nlevels) {
		nlevels--;
		if(nlevels > 0) {
			if(leftchild != null) {
				leftElNumber = leftchild.nElements(nlevels);
			}
			else leftElNumber = 0;
			if(rightchild != null) {
				rightElNumber = rightchild.nElements(nlevels);
			}
			else rightElNumber = 0;
		}
		return (leftElNumber + rightElNumber + 1);
	}

	private void multCoeffSxDx() {
		if(rightchild != null) {
			rightchild.multCoeffSxDx();
		}
		if(leftchild != null) {
			leftchild.multCoeffSxDx();
		}
		if(!isRootNode) statBilanciamento *= ((float) (leftElNumber + 1) / (float) (rightElNumber + 1));
	}

	/**
	 * The behaviour of this function is not simple but it to be understand is worth. This object has a value (getValue()) that has a type. The subtree objects has a valule that has a type. The tree is completely explored searching all the "value object" in a node that are instances of the exampleObject given as input parameter and every time a compatible object is found it will be compared by the
	 * function compareTo(.) the maximum founded exploring the tree.
	 * 
	 * @param exampleObject
	 *            The object used as type example to which the compared objects on the nodes must correspond.
	 * @return the node with the maximum (respect the method compareTo()) founded value object. If none object where compatible with the exampleObject then null is returned.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IntegerToObjectSimpleNode getMaxValueSubNode(Comparable exampleObject) {
		IntegerToObjectSimpleNode maxValueSubNodeR = null;
		IntegerToObjectSimpleNode maxValueSubNodeL = null;
		IntegerToObjectSimpleNode returnedNode = null;
		if(exampleObject == null) return null;
		if(leftchild != null) {
			maxValueSubNodeL = leftchild.getMaxValueSubNode(exampleObject);
		}
		if(rightchild != null) {
			maxValueSubNodeR = rightchild.getMaxValueSubNode(exampleObject);
		}
		if(this.getValue() != null && this.getValue().getClass().isInstance(exampleObject)) {
			returnedNode = this; 
			// Controllo che gli oggetti "valore" da confrontare siano confrontabili con l'oggetto "valore" di questo nodo ed in caso affermativo eseguo il confronto.
			if(maxValueSubNodeL != null && maxValueSubNodeL.getValue() != null && maxValueSubNodeL.getValue().getClass().isInstance(exampleObject)) {
				if(((Comparable) maxValueSubNodeL.getValue()).compareTo(returnedNode.getValue()) > 0) {
					returnedNode = maxValueSubNodeL;
				}
			}
			if(maxValueSubNodeR != null && maxValueSubNodeR.getValue() != null && maxValueSubNodeR.getValue().getClass().isInstance(exampleObject)) {
				if(((Comparable) maxValueSubNodeR.getValue()).compareTo(returnedNode.getValue()) > 0) {
					returnedNode = maxValueSubNodeR;
				}
			}
		}
		else {//Se questo nodo non e` compatibile con l'esempio cerco di caomparare il risultato della comparazione di quelli sottostanti e se nessuna comparazione non e` possibile e nessun risultato e` stato raggiunto dalle comparazioni sottostatni allora returnedNode rimane inevitabilmente a null (come dovrebbe essere).
			if(maxValueSubNodeL != null && maxValueSubNodeL.getValue() != null && maxValueSubNodeL.getValue().getClass().isInstance(exampleObject)) {
				if(maxValueSubNodeR != null && maxValueSubNodeR.getValue() != null && maxValueSubNodeR.getValue().getClass().isInstance(exampleObject)) {
					if(((Comparable)maxValueSubNodeL.getValue()).compareTo(maxValueSubNodeR.getValue()) < 0) {
						returnedNode = maxValueSubNodeR;
					}
					else returnedNode = maxValueSubNodeL;
				}
				else returnedNode = maxValueSubNodeL;
			}
			else if(maxValueSubNodeR != null && maxValueSubNodeR.getValue() != null && maxValueSubNodeR.getValue().getClass().isInstance(exampleObject)) {
				returnedNode = maxValueSubNodeR;
			}
		}

		return returnedNode;
	}

}
