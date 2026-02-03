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
 * Created on Nov 3, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package main;

import java.io.File;
import java.util.Random;
import java.util.Vector;

import java.util.Scanner;
import java.util.regex.MatchResult;

import javax.swing.ToolTipManager;

import customobj.containers.FloatToObjectSimpleNode;

import clientgui.MainPanel;
import analysis.Psynthesis;
import analysis.TextSkimmer;
import arcmanagement.BackgroundTask;
import arcmanagement.Classific;
import arcmanagement.ElabException;
import arcmanagement.GlobalCollector;
import arcmanagement.ValidationException;

/**
 * @author lsola
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class main {
	public static void main(String[] args) throws Exception {
		ToolTipManager.sharedInstance().setInitialDelay(100);
		ToolTipManager.sharedInstance().setDismissDelay(15000);
		BackgroundTask bgTask = new BackgroundTask();
		bgTask.start();
/*
		GlobalCollector gc = new GlobalCollector();
/*
		long accessKey = gc.getNewAccessKey(1, "lsola");
		try {
			int[] languages = new int[]{294,303};
			//gc.search("select * from FILEINFO", "formula per il calcolo della riluttanza magnetica di un solenoide", 25, 5, languages, accessKey, 1, "lsola");
			//gc.search("select * from FILEINFO", "sistema per analizzare le immagini e riconoscere un uomo", 25, 8, languages, accessKey, 1, "lsola");
			gc.search("select * from FILEINFO", "sviluppo dei moduli del kernel linux", 25, 8, languages, accessKey, 1, "lsola");
			Classific classifica;
			short cicla = 0;
			while(cicla < 1) {
			    Thread.sleep(1000);
				classifica = gc.getResponses(accessKey, 1, "lsola");
				if(classifica != null && classifica.size() > 0) {
					cicla++;
					System.out.println("Stringa cercata: " + classifica.getSearchString());
					Vector<FloatToObjectSimpleNode> vettoreClasifica = classifica.getClassific();
					for(int i = 0; i < classifica.size(); i++) {
						//Notare che nella istruzione seguente la trasformazione dell'oggetto ritornato 
						//dalla getValue a stringa avviene implicitamente tramite il metodo toString() che
						//nel FileInfoRecord e' stato sovrascritto...
						System.out.println(vettoreClasifica.get(i).getValue() + "\n" + "punteggio:" + vettoreClasifica.get(i).getIndex() + "    richiesta:" + accessKey + "\n");
					}
					gc.removeResponse(accessKey, 1, "lsola");
				}
				else if(classifica != null && classifica.size() == 0) { //Devo catturare lo stato di "ricerca terminata ma non andata a buon fine" (nessun file trovato).
					gc.removeResponse(accessKey, 1, "lsola");
					cicla++;
				}
			}

			//Thread.sleep(10000);//Inserito per vedere se il SearchDispactcher elimina gli AutoFillBuff non piu' utilizzati.
/*	
			 File file = new File("./testingdocs/capitolo8.pdf");
			 gc.conversion(file, accessKey, 1, "lsola");
			 
			 int lingua = gc.findLanguage(accessKey, 1, "lsola");
			 int gruppoconc = gc.findGroupCncpt(accessKey, 1, "lsola");
			 System.out.println(gc.example(accessKey, 1, "lsola"));
			 System.out.println("lingua= "+lingua);
			 System.out.println("Gruppo concettuale= "+gruppoconc);
			 
			 int grpCncptsList[] ={gruppoconc};
			 gc.insertFile("titolo","descrizione supplementare inserita dall'utente", grpCncptsList, lingua, accessKey, 1, "lsola");
			 

<<<<<<< main.java
//			 GlobalCollector gc = new GlobalCollector();
//			 gc.insUtente("admin", "pappo", "prova", "prova", "Pippo")
//						 }
//			
//			 gc.chPasswd("poiu", "pappo", "prova");
//			 }
//			
//			 gc.delUser("admin", "prova", "pippo");
//			 }
=======
			/*GlobalCollector gc = new GlobalCollector();
			 /*gc.insUtente("admin", "pappo", "prova", "prova", "Pippo")
			 }*/
			/*
			 gc.chPasswd("poiu", "pappo", "prova");
			 }*/
			/*
			 gc.delUser("admin", "prova", "pippo");
			 }*/
/*
			gc.close(5, "admin");

>>>>>>> 1.11
		}
		catch (ElabException ex) {
			System.out.println(ex.getMessage());
		}
		catch (ValidationException ex) {
			System.out.println(ex.getMessage());
		}
		catch (Exception ex) {
			ex.printStackTrace();//Per DEBUG
			System.err.println(ex.getMessage());
		}
*/

//		GlobalCollector.close(5, "admin");//Nel caso in cui la versione non sia client-server (solo locale) il nome utente e la password non vengono considerati ed il server comunque chiuso.

		//------------------------------------CLIENT-----------------------------------------
		//Il thread che istanzia il mainPanel viene intrappolato in un loop di servizio che
		//perdura sino alla chiusura dell'oggetto stesso. Quindi non inserire operazioni prioritarie dopo
		//la seguente.
		MainPanel mainPanel = new MainPanel();
		
		//System.out.println("Processo principale concluso.");

		/*Prova del calcolo coerenza (effettivamente Psynthesis.calcoloCoerenza(..) si comporta male con stringhe switchate ma in compenso ha una sensibilita` elevatissima per la presenza di parole significative e la dinamica
		data dalla successione di parole che in qualche modo esprime un significato simile. La motivazione formale di tutto cio` mi rimane misteriosa (o me la sono dimenticata?)
		anche se l'algoritmo lo ho pensato io impiegandoci parecchio tempo e cervello.*/
		//Psynthesis psynth=new Psynthesis(1);
		//System.out.println(psynth.calcoloCoerenza("animalli da appartamento", "i cani sono sporcaccioni e per questo non bisogna tenerli dentro gli appartamenti"));
		//System.out.println(psynth.calcoloCoerenza("animalli da appartamento", "Appartamenti in vendita. Villette a schiera a prezzi competitivi"));

/*Codice di test per TextSkimmer
		TextSkimmer ts = new TextSkimmer();
        File file = new File("/home/lsola/documatTempdir/(ebook - pdf) Industrial Control Handbook.txt");
        Scanner s = new Scanner(file).useDelimiter(" +|\t+|\n+|\r+|\f+|\\a+|\\e+|\\p{Punct}");
        String instr;
        while(s.hasNext()) {
        	instr = s.next();
        	if(instr.length()!=0) {
        		//System.out.println(instr);
        		if(instr.length()>1)
        			ts.insertWord(instr.toLowerCase());
        	}
        }
        s.close();
        System.out.println();
		Vector<String> skimmed  = ts.getBetweenList(-0.15f,-0.114f);
		for(int i=0;i<skimmed.size(); i++)
			System.out.println(skimmed.get(i));
		System.out.println(skimmed.size());
*/
		bgTask.halt();
	}
}

/* Testing code for SimpleNode
SimpleNode sn = new SimpleNode(1000000);
Random rand=new Random();
for(int i=0;i <500000;i++) {
int tmp=rand.nextInt();
sn.append(tmp,""+tmp);
}

System.out.println(" Statistica: "+sn.balanceStat());
System.out.println("\n 2146836108 "+sn.get(2146836108));
System.out.println(" 2146836108 "+sn.getNear(2146836108).getValue());
System.out.println(""+sn.getMin().getValue()+"  "+sn.getMax().getValue());
*/
/*	
	//--------------------------------------------Codice per testare la funzione getMaxValueSubNode(.) di SimpleNode-----------------------------------------------------------------------------------------------------
	class Moduluses implements Comparable<Moduluses> {  
		float scalar=0; 
		float groupsModulus=0;
		float documentsModulus=0;
		public int compareTo(Moduluses o) {
//			System.out.println(( scalar / (Math.sqrt(groupsModulus) * Math.sqrt(documentsModulus)) )+"\n"+ 
//					( o.scalar / (Math.sqrt(o.groupsModulus) * Math.sqrt(o.documentsModulus))));
			return (int) Math.signum(( scalar / (Math.sqrt(groupsModulus) * Math.sqrt(documentsModulus)) ) - 
				( o.scalar / (Math.sqrt(o.groupsModulus) * Math.sqrt(o.documentsModulus)) ));
		}
		public String toStrina() {
			return "scalar="+scalar+"	groupModulus="+groupsModulus+"	documentoModulus="+documentsModulus+"\nModulus="+
			(scalar / (Math.sqrt(groupsModulus) * Math.sqrt(documentsModulus)));
		}
	}
	SimpleNode sn = new SimpleNode();
	Moduluses module = null;
	Random rand=new Random();
	for(int i=1; i<10; i++) {
		module = new Moduluses();
		module.scalar = rand.nextFloat();
		module.groupsModulus = rand.nextFloat();
		module.documentsModulus = rand.nextFloat();
		sn.append(i, module, false);
		System.out.println("Oggetto "+i+":\n"+((Moduluses)sn.get(i).getValue()).toStrina());
	}
	
	System.out.println("\nIndice del maggiore="+sn.getMaxValueSubNode(new Moduluses()).getIndex());
*/

