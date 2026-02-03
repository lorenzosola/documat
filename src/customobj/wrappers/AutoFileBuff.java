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

package customobj.wrappers;

import java.io.File;
import java.io.FileInputStream;

/*
 * Created on Nov 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * Questo oggetto utilizza un thread indipendente per caricare in un buffer il file di cui il nome e' passato nel costruttore.
 * L'utilizzatore di questo ogetto vede un array di cui puo' conoscere il puntatore relativo all'ultimo dato caricato e se il file e' stato completamente caricato.
 * Subito dopo l'istanziazione viene avviato automaticamente il thread.
 * @author lsola
 */
public class AutoFileBuff extends Thread {
	private File file;
	/**
	 * Buffer con il contenuto del file. Normalmente, in un contensto multithreaded, e' in riempimento. Per un utilizzo corretto vedere i metodi per il controllo dello stato.
	 */
	public byte[] buffer;
	private int nbytes, boffset = 0, len;
	private boolean isloaded = false;
	private int numberOfUsers = 0;
	private long lifeTime;//Viene impostrato all'istante in cui il file risulta completamente caricato.

	/**
	 * File da leggere per lo riempimento del buffer.
	 */
	public AutoFileBuff(File file) {
		this.file = file;
		len = 1024;
		this.start();
		lifeTime = 0;
	}

	public void run() {
		try {
			buffer = new byte[(int) file.length()];//Il buffer assume la stessa lunghezza del file.
			FileInputStream is = new FileInputStream(file);
			while(is.available() > 0) {
				if(boffset + len >= buffer.length) len = buffer.length - boffset;
				nbytes = is.read(buffer, boffset, len);
				boffset += nbytes;
			}
			isloaded = true;
			lifeTime = System.currentTimeMillis();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Ritorna la posizione dell'ultimo byte prima di quello "appena scritto";
	 */
	public int getLastIndex() {
		return boffset;
	}

	/**
	 *Indica se il file e' completamente caricato nel buffer.
	 * @return	booleano che indica se il caricamento del file e' giunto a termine.
	 */
	public boolean isLoaded() {
		return isloaded;
	}

	public String getFileName() {
		return file.getPath();
	}

	/**
	 * In un contesto multithreaded (quello per cui e' stato progettato) questo metodo indica tramite la chiamata nullable() che l'oggetto e' utilizzato da un thread. 
	 */
	public void open() {
			numberOfUsers++;
			if(isloaded) lifeTime = System.currentTimeMillis();//Quando un oggetto inizia ad utilizzare nuovamente il buffer deve essere eventualmente aggiornato il lifeTime. 
	}

	/**
	 * In un contesto multithreaded (quello per cui e' stato progettato) questo metodo indica tramite la chiamata nullable() che l'oggetto non e' piu' utilizzato da un thread che ha precedentemente chiamato la open(). 
	 */
	public void close() {
			numberOfUsers--;
	}
	
	/**
	 * Indica se l'oggetto sia dichiarato in uso o meno (vedere open() close()).
	 * @return boolean
	 */
	public boolean nullable() {
			if(numberOfUsers == 0) return true;
			else return false;
	}
	
	/**
	 * Restituisce il numero di mSec intercorsi dall'istatne di completo caricamento.
	 * @return Se il file e` stato completamente caricato corrisponde numero di mSec intercorsi dall'istatne di completo caricamento
	 * altrimenti e` -1.
	 */
	public long getOldTime() {
		if(lifeTime == 0) return -1;
		return System.currentTimeMillis() - lifeTime;
	}
	
}