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
 * Created on Dec 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package customobj.wrappers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

/**
 * Gestore delle opzioni salvate nel file properties.prop (nella cartella radice o comunque nel path della VM). Il metodo get(nome Opzione) ritorna l'opzione che compare nel .prop senza bisogno di nessun'altra operazione.
 * 
 * @author lsola
 */
public class LocalProp {
	private static Vector<Properties> properties;
	private static Vector<File> fileOpzioni;
	private static Vector<Boolean> toSave;
	private int localReference;

	/**
	 * Constructs an LocalProp class in which the corrisponding properties file has the name passed as parameter.
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public LocalProp(String fileName) throws IOException {
		if(fileOpzioni == null) {
			fileOpzioni = new Vector<File>();
			properties = new Vector<Properties>();
			toSave = new Vector<Boolean>();
		}
		synchronized (fileOpzioni) {
			int i = 0;
			for(i = 0; i < fileOpzioni.size(); i++) {
				if(fileOpzioni.get(i).getName().equals(fileName)) break;
			}
			if(i == fileOpzioni.size()) {
				File f = new File(fileName);
				if(!f.exists()) {
					throw new IOException("File " + f.getName() + " doesn't exists.");
				}
				if(!f.canRead()) {
					throw new IOException("File " + f.getName() + " doesn't exists.");
				}
				fileOpzioni.add(f);
				Properties p = new Properties();
				FileInputStream fis = new FileInputStream(f);
				p.load(fis);
				fis.close();
				properties.add(p);
				toSave.add(new Boolean(false));
			}
			localReference = i;
		}
	}

	/**
	 * Ottiene una stringa con il valore del parametro contenuto nel file. Se il file non contiene tale parametro la funzione ritorna null.
	 * 
	 * @param nomeParametro
	 * @return String
	 * @throws IOException
	 */
	public String get(String nomeParametro) throws IOException {
		return properties.get(localReference).getProperty(nomeParametro);
	}

	/**
	 * Imposta imposta/aggiunge un parametro nel file di prefenze.
	 * 
	 * @param nomeParametro
	 * @param valore
	 * @return Se la modifica e` riuscita ritorna il valore del parametro altrimenti null.
	 * @throws IOException
	 */
	public String set(String nomeParametro, String valore) throws IOException {
		// if(refresh) {
		// reload();
		// }
		synchronized (properties.get(localReference)) {
			properties.get(localReference).setProperty(nomeParametro, valore);
			toSave.set(localReference, new Boolean(true));
		}
		return valore;
	}

	/**
	 * Se il parametro non esiste, il file non esiste piu`, o il parsing non e` corretto si ha un IOException.
	 * 
	 * @param nomeParametro
	 * @return il valore numerico corrispondente al parametro intero cercato.
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public int getIntPar(String nomeParametro) throws NumberFormatException, IOException {
		return Integer.parseInt(get(nomeParametro));
	}

	public float getFloatPar(String name) {
		return Float.valueOf(properties.get(localReference).getProperty(name));
	}
	
	/**
	 * Reload the parameter file.
	 * 
	 * @throws IOException
	 */
	public void reload() throws IOException {
		FileInputStream fis = new FileInputStream(fileOpzioni.get(localReference));
		properties.get(localReference).load(fis);
		fis.close();
	}

	/**
	 * Save the parameter as theiy are (modified or not).
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		synchronized (properties.get(localReference)) {
			if(toSave.get(localReference) == true) {
				FileOutputStream fos = new FileOutputStream(fileOpzioni.get(localReference));
				properties.get(localReference).store(fos, null);
				fos.close();
				toSave.set(localReference, new Boolean(false));
			}
		}
	}

}