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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.sun.corba.se.impl.orbutil.threadpool.TimeoutException;

public class GenericFunctions {

	/**
	 * Toglie dalla stringa di ingresso le parole troppo corte e quelle specificate nell'array di stringhe cuttingWords.
	 * Ritorna un array di stringhe corrispondenti alle parole.
	 * 
	 * @param str String[] da scansionare.
	 * @param stopWords String[] di stop words.
	 * @param delimiterPattern espressione regolare (RegExpr) che esprime il delimitatore per distinguere le parole all'interno della stringa str.
	 * @return Array di stringhe da cui sono state tolte alcune parole.
	 */
	public static String[] removeStopWords(String str,String[] stopWords,String splitDelilmitersPattern) {
		String[] strArr = str.split(splitDelilmitersPattern);
		return removeStopWords(strArr, stopWords);
	}

	/**
	 * Toglie dall'array di stringhe di ingresso le parole troppo corte e quelle specificate nell'array di stringhe cuttingWords.
	 * Ritorna un array di stringhe corrispondenti alle parole.
	 * 
	 * @param str array di stringhe da scansionare.
	 * @param stopWords String[] di stop words.
	 * @return Array di stringhe da cui sono state tolte alcune parole.
	 */
	public static String[] removeStopWords(String[] strArr,String[] stopWords) {
		//return strArr;
		int posOut[] = new int[strArr.length];//Qui dentro metto la delle posizioni in strArr in cui sono contenute le strighe da inserire nell'Array di Output.
		int pos = 0;

		//Ricostruzione dell'array senza parole insignificanti.
		int i, k;
		for(i = 0; i < strArr.length; i++) {
			if(strArr[i].length() <= 1) continue;//Salto le parole troppo corte (articoli, preposizioni, ecc) perche' possono creare casini.
			//strArr[i] = strArr[i].toUpperCase();//Non comporta una grossa perdita di tempo comunque per ora la scelta e` quella di conservare in maiuscolo tutti i files di testo in archivio.
			//Solo preposizioni e congiunzioni la cui lunghezza e' > 2 in quanto le altre sono tagliate di default.
			if(stopWords != null) {
				for(k = 0; k < stopWords.length; k++) {
					if(strArr[i].equals(stopWords[k])) break;
				}
				if(k >= stopWords.length) posOut[pos++] = i;
			}
			else posOut[pos++] = i;

		}

		String[] strArrOut = new String[pos];
		for(i = 0; i < pos; i++) {
			strArrOut[i] = strArr[posOut[i]];
		}
		return strArrOut;
	}


	static boolean chastrCmpTrimmed(char[] first, char[] second) {
		int firstId = 0, secondId = 0;
		while(true) {
			if(first[firstId] == ' ' || first[firstId] == '\t') {
				firstId++;
				continue;
			}
			if(second[secondId] == ' ' || second[secondId] == '\t') {
				secondId++;
				continue;
			}

			if(first[firstId] != second[secondId]) return false;

			if(firstId == first.length - 1 || secondId == second.length - 1) break;// In questo caso se una delle 2 strighe e` alla fine anche l'altra lo sara` per il passaggio sopra.

			firstId++;
			secondId++;
		}
		return true;
	}

	/**
	 * Exec an external native executable displaying its standard output on a JTextArea if not null.
	 * 
	 * @param null
	 *            value if also accepted.
	 * @param commandLine
	 * @param timeout
	 *            timeout in mSec oltre al quale il processo verra eliminato forzatamente dal sistema.
	 * @return StringBuffer with the entirely content of standard output.
	 * @author Lorenzo Sola
	 * @throws InterruptedException 
	 */
	static public StringBuffer execCommandDisplayingGraphically(JTextArea messages, String commandLine, int timeout) throws FileNotFoundException, TimeoutException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		StringBuffer str = new StringBuffer();
		int timeRunning = 0;
		if(timeout == 0) timeout = Integer.MAX_VALUE;
		char chr;
		try {
//			if(commandLine.length() != 0 && commandLine.charAt(0) != '/' && commandLine.charAt(0) != '.') commandLine = "./" + commandLine;
			final Process pr = rt.exec(commandLine);
			InputStream out = pr.getInputStream();
			// InputStream quando questo avviene senza effettiva lettura dei dati (il
			// processo esterno non e' riuscito ad elaborare alcun dato nel periodo di ciclo)
			// e costituisce quindi il tempo di attesa per il ciclo successivo. Il comportamento e
			// reciproco nel caso in cui il ciclo legga dati.
			int nReads, times=0;
			while(true) {
				Thread.sleep(250);
				// Il seguente si puo' ottimizzare con l'utilizzo di un buffer ma non e' un'operazione
				// critica.
//				nReads = out.available();
//				while(nReads > 0) {
//					chr = (char) out.read();
//					// System.out.print("" + chr);
//					str.append(chr);
//					if(messages != null) {
//						messages.append("" + chr);
//						messages.setCaretPosition(messages.getDocument().getLength());
//					}
//					nReads--;
//				}

				while(times < 20) {
					// Il seguente si puo' ottimizzare con l'utilizzo di un buffer ma non e' un'operazione
					// critica.
					if((nReads = out.available()) > 0) {
						if(times >= 10) times = 0;
						while(nReads > 0) {
							chr = (char) out.read();
							str.append(chr);
							messages.append("" + chr);
							messages.setCaretPosition(messages.getDocument().getLength());
							nReads--;
						}
					}
					else {
						times += 1;
						Thread.sleep(100);
					}
				}
				
				
				try {
					if(pr.exitValue() == 0) {
						// In questo caso il valore di uscita non ha significato perche` il programma non termina canonicamente ed il valore di ritorno non e` 0 (problema SUO ma fastidioso).
						// JOptionPane.showMessageDialog(this, "Executable " + acquisitionExecutableCommand + " is aborted."); failed = true; }
						// return -1;
						nReads = out.available();

						while(nReads > 0) {
							chr = (char) out.read();
							str.append(chr);
							if(messages != null) {
								messages.append("" + chr);
								messages.setCaretPosition(messages.getDocument().getLength());
							}
							nReads--;
						}
						
						break;
					}
					else {
						System.out.println(commandLine + " Exit Value = " + pr.exitValue());
						out = pr.getErrorStream();
						nReads = out.available();
						str.setLength(0);
						while(nReads > 0) {
							chr = (char) out.read();
							str.append(chr);
							if(messages != null) {
								messages.append("" + chr);
								messages.setCaretPosition(messages.getDocument().getLength());
							}
							nReads--;
						}
						throw new InterruptedException(str.toString());
					}
				}
				catch (IllegalThreadStateException ex) {
					// Thread.sleep(250);
				}
				if(timeRunning >= timeout) {
					pr.destroy();
					if(messages != null) {
						JOptionPane.showMessageDialog(messages, "Executable\"" + commandLine + "\" Killed for Timeout");
					}
					throw (new TimeoutException());
				}
				timeRunning += 250;
			}

		}
		catch (IOException e) {
			if(messages != null) JOptionPane.showMessageDialog(messages, "Cannot find executable \"" + commandLine + "\"");
			else throw (new FileNotFoundException("Cannot find executable \"" + commandLine + "\""));
			return null;
		}
		return str;
	}

	/**
	 * 
	 * @param tmpDir
	 * @return true se il direttorio e` stato cancellato correttamete o non esiste. false altrimenti.
	 */
	public static boolean deleteDir(File tmpDir) {
		if(!tmpDir.exists()) return true;
		if(tmpDir != null && tmpDir.isDirectory()) {
			// Procedura di cancellazione di un direttorio.
			File[] filelist = tmpDir.listFiles();
			if(filelist != null) {
				for(int i = 0; i < filelist.length; i++) {// altrimenti non cancella la dir.
					if(filelist[i].isDirectory()) {
						if(deleteDir(filelist[i]) == false) {
							break;
						}
					}
					else if(!filelist[i].delete()) break;
				}
				if(tmpDir.delete()) {
					return true;
				}
			}
		}
		return false;

	}

	public static String padIntegerString(String intNumber, int dim) {
		StringBuffer tmp = new StringBuffer();
		for(int i = intNumber.length(); i < dim; i++) {
			tmp.append("0");
		}
		tmp.append(intNumber);
		return tmp.toString();
	}

	private static void sortFileListByMTime(File a[], int lo0, int hi0) {
		int lo = lo0;
		int hi = hi0;
		if(lo >= hi) {
			return;
		}
		long mid = (a[(lo + hi) / 2]).lastModified();
		while(lo < hi) {
			while(lo < hi && (a[lo]).lastModified() < mid) {
				lo++;
			}
			while(lo < hi && (a[hi]).lastModified() >= mid) {
				hi--;
			}
			if(lo < hi) {
				File T = a[lo];
				a[lo] = a[hi];
				a[hi] = T;
			}
		}
		if(hi < lo) {
			int T = hi;
			hi = lo;
			lo = T;
		}
		sortFileListByMTime(a, lo0, lo);
		sortFileListByMTime(a, lo == lo0 ? lo + 1 : lo, hi0);
	}

	/**
	 * Ordina una array di file in ordine di data di modifica (istante in mSec) crescente.
	 * @param a array di File
	 */
	public static void sort(File a[]) {
		sortFileListByMTime(a, 0, a.length - 1);
	}

	/**
	 * Copia, e crea se non esiste, il file di origine nel file di destinazione.
	 * 
	 * @param fileOrig
	 * @param fileDest
	 */
	public static void copiaFile(File fileOrig, File fileDest) {
		byte[] buffer = new byte[1024];// Il buffer assume la stessa lunghezza del file.
		int len;
		FileInputStream is;
		try {
			is = new FileInputStream(fileOrig);
		}
		catch (FileNotFoundException e) {
			System.out.println("Impossibile trovare il file " + fileOrig.getPath());
			e.printStackTrace();
			return;
		}
		try {
			if(!fileDest.isFile()) fileDest.createNewFile();
			FileOutputStream os = new FileOutputStream(fileDest);
			while((len = is.available()) > 0) {
				if(len > 1024) len = 1024;
				len = is.read(buffer, 0, len);
				if(len < 0) {
					System.out.println("Problema di input/output nel copiare il contenuto di " + fileOrig.getPath() + " in " + fileDest.getPath());
					return;
				}
				os.write(buffer, 0, len);
			}
		}
		catch (IOException e1) {
			System.out.println("Problema di input/output nel copiare il contenuto di " + fileOrig.getPath() + " in " + fileDest.getPath());
			e1.printStackTrace();
		}
	}

	/**
	 * Ritorna una array di caratteri corrispondente al file passato come argomento rielaboratandolo leggermente per togliere eventuali irregolarita' generalmente riconoscibili.
	 * E' anche possibile stabilire quanti bytes far tornare e da che punto cominciare. Se il numero di bytes specificati e' maggiore rispetto a quelli presenti dal punto di inizio alla fine la
	 * stringa terminera' una volta raggiunta la file del file. Requisitio aggiunto il 6/4/2005: volgere tutto a lower-case.
	 * 
	 * @param fInput
	 *            file di ingresso.
	 * @param beginPoint
	 *            punto di inizio in percentuale di lunghezza del file (0-100).
	 * @param number
	 *            Numero di caratteri contenuti nella stringa di ritorno. Se passato 0 allora restituiti tutti i bytes fino alla fine del file.
	 * @return array di byte con il buffer contenente il file di testo elaborato e filtrato. Se il file temporaneo non viene trovato ritorna null.
	 * @throws ValidationException
	 */
	public static byte[] getRegularizedFileContent(File fInput, int beginPoint, int number) {
		int i = 0, pos = 0;
		short stato = 0;
		byte car = 0;
		byte[] vettoreCar = null;
		try {
			FileInputStream fInpStream = new FileInputStream(fInput);
			BufferedInputStream fis = new BufferedInputStream(fInpStream);
			fis.skip((fis.available() * beginPoint) / 100);// Scarto tanti bytes quanti ne servono per arrivare alla percentuale indicata.
			if(number == 0) number = fis.available();
			vettoreCar = new byte[number];
			// La seguente procedura si basa sul presupposto, generalmente vero, che le cifre da 1 a 9 ed i carateri A-Z, a-z, abbiano lo stesso codice asci indipendentemente dallo standard utilizzato. Tutti gli altri simboli vengono lasciati nella loro codifica originale.
			while(i < number && car != -1) {
				car = (byte) fis.read();
				if(stato == 0) {
					if(car == ' ' || car == 13 || car == 10) {//Nom modificare l'elenco dei caratteri: come spiegto sopra il presupposto e` che si tratti di file in cui le scritte sono in ASCII almeno per quanto concerne il normale set di caratteri (per i file di testo deve essere cosi`!). Il set non deve essere completo in quanto questa e` e vuole essere una normalizzazione blanda (altrimenti si rischia di perdere informazione). 
						if(car == 13 || car == 10) {
							if(i - pos < 4) {
								i = pos;// Tengo il segno per scartare eventualmente una serie di caratteri in colonna.
								vettoreCar[i++] = ' ';
							}
							else {
								pos = i;
								vettoreCar[i++] = '\n';
							}
						}
						else vettoreCar[i++] = ' ';
						stato = 1;
					}
					else {
						if(car >= 65 && car <= 90) car += 32;// Volge ad lower-case.
						/*
						 * Questo dipende dal char-set else if(car==130 || car==138) car='E';//Le e minuscole accentate le trasforno in E. else if(car==131 || car==133) car='A';//Le a minuscole accentate le trasforno in A. else if(car==140 || car==141) car='I';//Le i minuscole accentate le trasforno in I. else if(car==148 || car==149) car='O';//Le o minuscole accentate le trasforno in O. else
						 * if(car==150 || car==151) car='U';//Le u minuscole accentate le trasforno in U.
						 */
						vettoreCar[i++] = car;
					}
				}
				else if(stato == 1) {
					if(car == ' ' || car == 13 || car == 10) {// Converte LINE FEED o CARRIAGE RETURN in LINE FEED (per uniformita`).
						continue;
					}
					else {
						if(car >= 65 && car <= 90) car += 32;// Volge ad lower-case.
						/*
						 * Questo dipende dal char-set else if(car==130 || car==138) car='E';//Le e minuscole accentate le trasforno in E. else if(car==131 || car==133) car='A';//Le a minuscole accentate le trasforno in A. else if(car==140 || car==141) car='I';//Le i minuscole accentate le trasforno in I. else if(car==148 || car==149) car='O';//Le o minuscole accentate le trasforno in O. else
						 * if(car==150 || car==151) car='U';//Le u minuscole accentate le trasforno in U.
						 */
						vettoreCar[i++] = car;
						stato = 0;
					}
				}
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("File di transizione non trovato: possibile che sia gia' stato eliminato dalla \"tempdir\"");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return vettoreCar;
	}
	
	/**
	 * Ritorna un Vector di String ricevendo come ingresso un array di caratteri (passati come array di byte per il fatto che rirulta piu` comodo in molte situazioni, vedi l'uso dei buffer)
	 * di grandi dimensioni.
	 * @param array
	 * @return
	 */
	public static Vector<String> getStringArrayFromByteArray(byte[] array) {
		Vector<String> out = new Vector<String>();
		StringBuffer strBuff = new StringBuffer();
		short stato = 0;
		String tmpString;
		for(int i = 0; i < array.length;) {
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
						out.add(tmpString);
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
		return out;
	}

}
