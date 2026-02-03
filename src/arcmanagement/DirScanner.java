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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/*
 * Created on Nov 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Questo ogetto 
 */

/**
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @author lsola
 */

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!ALL TO BE DEVELOPED !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

public class DirScanner {
	public static void esplora(String l_sorg, String l_dest) throws Exception //Propaga anche le exceptions derivate da queste (come classi), come le IOException che possono essere generate dai metodi della classe File e (derivate).
	{
	String[] lista_sorg,lista_dest;
	File f_sorg = new File(l_sorg);
	File f_dest = new File(l_dest);
	File hand_sorg,hand_dest;
	
	if( !(f_sorg.isDirectory() && f_dest.isDirectory()) ) 
			//throw new Exception("Nome sorgente e/o nome destinazione non corrisponde ad una dir valida.");
			throw new Exception("Source name and/or destination name does not correspond to one saying validates.");
		
	lista_sorg = f_sorg.list();
	lista_dest = f_dest.list();
	
	for(int i=0;i<lista_sorg.length;i++)//Scansione dir sorgente.
		{
		hand_sorg = new File(l_sorg+"\\"+lista_sorg[i]);
		if(hand_sorg.isDirectory()) {
			boolean trovata=false;
			for(int k=0;k<lista_dest.length;k++) {
				hand_dest = new File(l_dest+"\\"+lista_dest[k]);
				if( hand_dest.isDirectory() && (hand_sorg.getName()).equals(hand_dest.getName()) ) { //Il confronto tra stringhe, oltre che con il metodo equals() funziona anche con == (ma non con <=, <, >, ecc);
					esplora( (l_sorg + "\\" + hand_sorg.getName()) , (l_dest + "\\" + hand_dest.getName()) ); //Ricorsione.
					trovata=true;
					}
				}
			if(!trovata) throw new Exception("Sottodir <"+hand_sorg.getName()+"> non presente nella struttura di destinazione.");
			}
		else { //Gestione copia condizionata dei files.
			if(hand_sorg.isFile()) {
				hand_dest = new File(l_dest+"\\"+hand_sorg.getName());
				if(hand_dest.isFile()) {
					if( hand_dest.lastModified() < hand_sorg.lastModified() ) {
						hand_dest.delete();
						FileOutputStream f_hand_dest = new FileOutputStream(hand_dest);
						FileInputStream f_hand_sorg = new FileInputStream(hand_sorg);
						System.out.println("Sostituzione di "+hand_dest.getPath());
						int c;
						while( (c = f_hand_sorg.read()) != -1)
							f_hand_dest.write(c);
						f_hand_dest.close();
						f_hand_sorg.close();
						}
					}
				else {
					FileOutputStream f_hand_dest = new FileOutputStream(hand_dest);
					FileInputStream f_hand_sorg = new FileInputStream(hand_sorg);
					System.out.println("Creazione di " + hand_dest.getPath());
					int c;
					while( (c = f_hand_sorg.read()) != -1)
						f_hand_dest.write(c);
					f_hand_dest.close();
					f_hand_sorg.close();
					}
				}//Il file nella sorgente e' valido.

			} //Gestione copia condizionata dei files.

		} //Ciclo scansione dir. sorgente.

	}//Metodo esplora.

}
