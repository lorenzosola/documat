package arcmanagement;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import customobj.functions.Compare;
import customobj.functions.ComputationException;
import customobj.functions.GenericFunctions;
import customobj.wrappers.LocalProp;
import customobj.wrappers.db_con;

public class BackgroundTask extends Thread {
	private LocalProp localProp;
	private db_con dbcon;
	private Connection conn;
	private int period;
	private boolean running;
	private ResultSet rs1;
	private ResultSet rs2;

	public BackgroundTask() throws IOException {
		localProp = new LocalProp("properties.prop");
		period = localProp.getIntPar("BackgroundTaskInterval") * 1000 * 60;
		dbcon = new db_con();
	}

	public void run() {
		running = true;
		try {
			System.out.println("conString=" + localProp.get("connString"));
			conn = dbcon.getConnection(localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			PreparedStatement ps1 = conn.prepareStatement("select ID, NOMEFILE, ARCH_PATH from fileinfo where ID not in ( select FI1.ID from fileinfo FI1 join grp_cncpt_files GF on FI1.ID = GF.FILE)");
			PreparedStatement ps2 = conn.prepareStatement("select grp_cncpt, WORD, RELEVANCE from grp_cncpt_words");
			PreparedStatement ps3 = conn.prepareStatement("insert into grp_cncpt_files (FILE,GRP_CNCPT) values (?, ?)");
			PreparedStatement ps4 = conn.prepareStatement("select count(*) from grp_cncpt GC where GC.ID not in (select distinct GCW.grp_cncpt from grp_cncpt_words GCW)");//Questa per rilevare se esistono gruppi senza alcuna parola associata il che implica nessun documento associato.
			PreparedStatement ps5 = conn.prepareStatement("select GRP_CNCPT, avg(RELEVANCE) from grp_cncpt_words group by GRP_CNCPT");
			PreparedStatement ps6 = conn.prepareStatement("update grp_cncpt_words set RELEVANCE = RELEVANCE * ? where grp_cncpt = ?");
			PreparedStatement ps7 = conn.prepareStatement("delete from grp_cncpt_words where GRP_CNCPT = ? and RELEVANCE < ?");
			while(running) {
				sleep(period);
				
				if(localProp.get("MailServerIP")!=null) {//Eseguo l'invio solamente se esiste il parametro "MailServerIP".
					MailManagement mm = new MailManagement(localProp.get("MailServerIP"), localProp.get("MailServerPort"), localProp.get("MailServerUName"), localProp.get("MailServerPassword"), Boolean.parseBoolean(localProp.get("SMTPAuth")), localProp.get("FromAddress"), localProp.get("CCAddress"), localProp.get("Subject"), conn);
					try {
						mm.invia();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					mm = null;
				}
				
				//Routine per l'associazione automatica dei file orfani con il gruppo piÃ¹ appropriato.
				//L'associazione avviene comunque solo se non esistono gruppi non associati ad alcun elemento in quanto se cosÃ¬ non fosse l'elemento orfano potrebbe essere associato a gruppi potenzialmente sbagliati
				//in quanto i gruppi non caraterizzati potrebbero essere proprio i più appropriati, nelle intenzioni degli utenti.
				rs1 = ps4.executeQuery();
				if(rs1.next() && rs1.getInt(1)>0) {
					rs1.close();
					continue;//Questo può essere la causa del heap memory overflow (i guru sconsigniano l'uso di continue per motivi teorici ma a mio parere a livello di esecuzione da origine a problemi di stack overflow se il compilatore non adotta gli accorgimenti opportuni)???
				}
				rs1 = ps1.executeQuery();
				rs2 = ps2.executeQuery();
				
				while(rs1.next()) {
					byte[] array = GenericFunctions.getRegularizedFileContent(new File(rs1.getString(3) + "/" + rs1.getString(2) + ".txt"), 0, 0);
					int deducedGroup = Compare.dbVectorsMaximumAffinity(rs2, array);
					ps3.setInt(1, rs1.getInt(1));
					ps3.setInt(2, deducedGroup);
					ps3.execute();
				}
				rs1.close();
				rs2.close();
				
				//Routine per la normalizzazione dei pesi delle parole indipendentemente per ogni gruppo concettuale ed eliminazione delle parole con relevance < del valore minimo (MinWRA).
				rs1 = ps5.executeQuery();
				float avg;
				int tmpInt;
				while(rs1.next()) {
					avg = rs1.getFloat(2);
					if(avg > localProp.getFloatPar("MaxWRA")) {
						ps6.setFloat(1, localProp.getFloatPar("NormalWRA")/avg);
						ps6.setInt(2, rs1.getInt(1));
						tmpInt = ps6.executeUpdate();
						System.out.println("aggiornate relevance del gruppo: "+rs1.getInt(1) + "	con fattore di scalatura: " + localProp.getFloatPar("MaxWRA")/avg + "	aggiornate "+tmpInt+" righe.");
						
						//Eliminazione delle word con relevance < del valore minimo.
						ps7.setInt(1, rs1.getInt(1));
						ps7.setFloat(2, localProp.getFloatPar("MinWRA"));
						tmpInt = ps7.executeUpdate();
						System.out.println("Eliminate "+tmpInt+" dal gruppo "+rs1.getInt(1)+" (con relevance < "+localProp.getFloatPar("MinWRA")+")");
					}
				}
				rs1.close();
			}
			ps1.close();
			ps2.close();
			ps3.close();
			ps4.close();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ComputationException e) {
			e.printStackTrace();
		}
	}

	public void halt() throws InterruptedException {
		running = false;
		while(isAlive()) {
			sleep(1000);
		}
	}
}
