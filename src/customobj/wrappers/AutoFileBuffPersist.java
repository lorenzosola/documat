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
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * Created on Nov 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * Questo oggetto utilizza un thread indipendente per caricare in un buffer il file di cui il nome e' passato nel costruttore. L'utilizzatore di questo ogetto vede un array di cui puo' conoscere il puntatore relativo all'ultimo dato caricato e se il file e' stato completamente caricato. Subito dopo l'istanziazione viene avviato automaticamente il thread.
 * Differisce da AutoFileBuff per il fatto che non e` necessario reistanziarlo per caricare un nuovo file. Questo ha, pero`, lo svantaggio
 * che, se utilizzato in ambito multithreaded, puo` dare adito ad errori nell'esecuzione in quanto il contenuto del buffer puo` essere cambiato inavvertitamente
 * dal caricamento di un nuovo file richiesto da un'altro thread.  
 * @author lsola
 */
public class AutoFileBuffPersist {
	private File file;
	/**
	 * Buffer con il contenuto del file. Normalmente, in un contensto multithreaded, e' in riempimento. Per un utilizzo corretto vedere i metodi per il controllo dello stato.
	 */
	public byte[] buffer;
	private int nbytes, boffset = 0, len;
	private boolean isloaded = false;
	private int numberOfUsers = 0;
	private int maxFileLength;
	Thread thread;
	Runnable runnable = new Runnable() {
		public void run() {
			try {
				FileInputStream is = new FileInputStream(file);
				while (is.available() > 0) {
					if (boffset + len >= buffer.length) len = buffer.length - boffset;
					nbytes = is.read(buffer, boffset, len);
					boffset += nbytes;
				}
				isloaded = true;
			}
			catch (FileNotFoundException ex) {
				ex.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 
	 */
	public AutoFileBuffPersist(int maxFileLength) {
		this.maxFileLength = maxFileLength;
		len = 1024;
		buffer = new byte[maxFileLength];
	}

	/**
	 * Alla chiamata di questo metodo viene iniziato immediatamente il caricamento di un file nel buffer utilizzando il thread dedicato rappresentato da questo stesso oggetto.
	 */
	public void loadFile(final File file) throws IOException {
		//if(boffset!=0 && isLoaded()!=true) throw new IOException("AutoFileBuffPersist: tentativo di caricare un file quando era ancora in caricamento un'altro file.");
		if(boffset!=0 && isLoaded()!=true) throw new IOException("AutoFileBuffPersist: attempt of loading a file when was in loading another file.");
		//if (!file.canRead()) throw new IOException("Il file " + file.getPath() + " non esiste o non e` leggibile.");
		if (!file.canRead()) throw new IOException("The file " + file.getPath() + " not exists is not readable.");
//		if (maxFileLength < file.length()) throw new IOException("Il file che si desidera caricare e` piu` grande della massima dimensione di chiarata nel costruttore.");
		if (maxFileLength < file.length()) throw new IOException("The file that was requested to load is bigger than the maxFileLength specified in the contructor of this instance.");
		boffset = 0;
		isloaded = false;
		this.file = file;
		thread = new Thread(runnable);
		thread.start();
	}

	/**
	 * Ritorna la posizione dell'ultimo byte prima di quello "appena scritto";
	 */
	public int getLastIndex() {
		return boffset;
	}

	/**
	 * Indica se il file e' completamente caricato nel buffer.
	 * 
	 * @return booleano che indica se il caricamento del file e' giunto a termine.
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
	}

	/**
	 * In un contesto multithreaded (quello per cui e' stato progettato) questo metodo indica tramite la chiamata nullable() che l'oggetto non e' piu' utilizzato da un thread che ha precedentemente chiamato la open().
	 */
	public void close() {
		numberOfUsers--;
	}

	/**
	 * Indica se l'oggetto sia dichiarato in uso o meno (vedere open() close()).
	 * 
	 * @return boolean
	 */
	public boolean nullable() {
		if (numberOfUsers == 0) return true;
		else return false;
	}
}