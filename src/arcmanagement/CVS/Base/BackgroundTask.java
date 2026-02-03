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

	public BackgroundTask() throws IOException {
		localProp = new LocalProp("properties.prop");
		period = localProp.getIntPar("BackgroundTaskInterval") * 1000 * 60;
		dbcon = new db_con();
	}

	public void run() {
		running = true;
		try {
			conn = dbcon.getConnection(localProp.get("connString"), localProp.get("user"), localProp.get("password"));
			PreparedStatement ps1 = conn.prepareStatement("select ID, NOMEFILE, ARCH_PATH from FILEINFO where ID not in ( select FI1.ID from FILEINFO FI1 join GRP_CNCPT_FILES GF on FI1.ID = GF.\"FILE\")");
			PreparedStatement ps2 = conn.prepareStatement("Select GRP_CNCPT, WORD, RELEVANCE from GRP_CNCPT_WORDS");
			PreparedStatement ps3 = conn.prepareStatement("insert into GRP_CNCPT_FILES (\"FILE\",GRP_CNCPT) values (?, ?)");
			PreparedStatement ps4 = conn.prepareStatement("select count(*) from GRP_CNCPT GC where GC.ID not in (select GCW.GRP_CNCPT from GRP_CNCPT_WORDS GCW)");//Questa per rilevare se esistono gruppi senza alcuna parola associata il che implica nessun documento associato.
			PreparedStatement ps5 = conn.prepareStatement("select GRP_CNCPT, avg(RELEVANCE) from GRP_CNCPT_WORDS group by GRP_CNCPT");
			PreparedStatement ps6 = conn.prepareStatement("update GRP_CNCPT_WORDS set RELEVANCE = RELEVANCE * ? where GRP_CNCPT = ?");
			PreparedStatement ps7 = conn.prepareStatement("delete from GRP_CNCPT_WORDS where GRP_CNCPT = ? and RELEVANCE < ?");
			while(running) {
				sleep(period);

				
				//Routine per l'associazione automatica dei file orfani con il gruppo più appropriato.
				//L'associazione avviene comunque solo se non esistono gruppi non associati ad alcun elemento in quanto se così non fosse l'elemento orfano potrebbe essere associato a gruppi palesemente sbagliati
				//per la mancanza di un rifrimento esatto.
				ResultSet rs1 = ps4.executeQuery();
				if(rs1.next() && rs1.getInt(1)>0) continue;
				rs1 = ps1.executeQuery();
				ResultSet rs2 = ps2.executeQuery();
				
				while(rs1.next()) {
					byte[] array = GenericFunctions.getRegularizedFileContent(new File(rs1.getString(3) + "/" + rs1.getString(2) + ".txt"), 0, 0);
					int deducedGroup = Compare.dbVectorsMaximumAffinity(rs2, array);
					ps3.setInt(1, rs1.getInt(1));
					ps3.setInt(2, deducedGroup);
					ps3.execute();
				}
				rs1.close();
				rs2.close();
				
				//Routine per la normalizzazione dei pesi delle parole indipendentemente per ogni gruppo concettuale.
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
				
				
				//Routine che  elimina dai gruppi le parole con relevace < 1 (da fare!!!!!!!)
			}
			ps1.close();
			ps2.close();
			ps3.close();
			ps4.close();
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ComputationException e) {
			// TODO Auto-generated catch block
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
