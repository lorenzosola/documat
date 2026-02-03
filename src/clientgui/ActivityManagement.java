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
 * Created on May 10, 2005
 */
package clientgui;

import arcmanagement.FileInfoRecord;
import customobj.containers.ObjToInt;
import customobj.gui.ObjSelectorDialog;
import customobj.gui.TableDataManager;
import customobj.gui.TableRecord;
import customobj.wrappers.DangerousOperationException;
import customobj.wrappers.LocalProp;
import customobj.wrappers.db_con;
//import genericobj.db_con;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import clientgui.FileInsertionPanel;; 

/**
 * @author lsola
 */
public class ActivityManagement extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TableDataManager tdmAttivita;
	private JScrollPane tabellaAttivitaSP;

	private JPanel tabellaComandi = null;
	private JPanel comandi = null;
	private JButton aggiungi = null;
	private JButton elimina = null;
	private JButton modifica = null;
	private JPanel pannelloInserimentoDatiAttivita = null;
	private JPanel annullaSalva = null;
	private JButton applica = null;
	private JButton annulla = null;
	private TableRecord actualTableRecord;
	private JButton salva = null;
	private JPanel questo;
	private LocalProp localProp;
	private JButton annullaTutto = null;
	private db_con conn = null;
	private boolean dbOperationsEnabled = false;
	private String conString, dbUser, dbPassword;
	private boolean inModifica = false;
	private JTable docTable = null;
	private JScrollPane docTableSP = null;
	private TableDataManager docTableModel = null;  //  @jve:decl-index=0:
	private JTable usersTable = null;
	private JScrollPane usersTableSP = null;
	private TableDataManager usersTableModel = null;
	private JTable tabellaAttivita;
	private JPanel docManagementPanel;
	private SearchesPanel searchesPanel = null;
	private JPanel usersManagementPanel;

	/**
	 * This is the default constructor
	 * 
	 * @throws IOException
	 */
	public ActivityManagement(SearchesPanel searchesPanel) throws IOException {
		super();
		localProp = new LocalProp("guiprop.prop");

		try {
			if((this.dbUser = localProp.get("DBUser")) != null && (this.dbPassword = localProp.get("DBPassword")) != null && (this.conString = localProp.get("ConnString")) != null) {
				dbOperationsEnabled = true;
				conn = new db_con();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		initialize();
		questo = this;
		this.searchesPanel = searchesPanel;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.setSize(new Dimension(506, 183));
		if(!dbOperationsEnabled) {
			JLabel notEnabled = new JLabel("Non Enabled");
			notEnabled.setHorizontalAlignment(SwingConstants.CENTER);
			notEnabled.setFont(new Font("Dialog", Font.BOLD, 15));
			Color color = new Color(120,120,120);
			notEnabled.setForeground(color);
			this.add(notEnabled, BorderLayout.CENTER);
			return;
		}
		this.add(getTabellaComandi(), java.awt.BorderLayout.CENTER);
		this.add(getPannelloInserimentoDatiAttivita(), java.awt.BorderLayout.SOUTH);
		this.add(getDocManagementPanel(), BorderLayout.WEST);
		this.add(getUsersManagementPanel(), BorderLayout.EAST);
	}
	
	private JPanel getDocManagementPanel() {
		if (docManagementPanel == null) {
			docManagementPanel = new JPanel(new BorderLayout());
			
			docManagementPanel.add(getDocTableSP(), BorderLayout.CENTER);
			JButton addButton = new JButton("Aggiungi Da Ricerca");
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//Aggiunge i dati dei documenti selezionati nel pannelo di ricerca....vedere come si fa a fare selezione multipla senza bisogno del Ctrl nella tabella del pannello di ricerca
					Vector<FileInfoRecord> docSearchResult = searchesPanel.getFInfoRecordSelection();
					@SuppressWarnings("unchecked")
					Vector<TableRecord> dataVector = (Vector<TableRecord>) docTableModel.getDataVector();
					TableRecord tblRecord;
					int n;
					boolean salta;
					for(int i=0; i<docSearchResult.size(); i++) {
						if(tdmAttivita.getSelectedRow() == null) {
							JOptionPane.showMessageDialog(questo, "Selezionare prima Attività","Avviso",JOptionPane.INFORMATION_MESSAGE);
							break;
						}
						
						//Evito di inserire duplicati
						salta = false;
						for(n = 0; n < dataVector.size(); n++) {
							if(docSearchResult.get(i).nameNoExtension.equals(dataVector.get(n).getField("nomefile")) &&
									docSearchResult.get(i).title.equals(dataVector.get(n).getField("titolo")) &&
									docSearchResult.get(i).ownerID.equals(dataVector.get(n).getField("owner_id")))
								salta = true;
						}
						if(salta) continue;
						tblRecord = docTableModel.addRow();
						tblRecord.addField("id_attivita", tdmAttivita.getSelectedRow().getField("id"));
						tblRecord.addField("nomefile", docSearchResult.get(i).nameNoExtension);
						tblRecord.addField("titolo", docSearchResult.get(i).title);
						//tblRecord.addField("descrizione", docSearchResult.get(i).title);//Questo server solo perchè la query di popolamento della tabella ha come campo corrispondente al titolo il campo "descrizione", quindi per poter visualizzare il valore dopo la creazione di una nuova riga bisogna valorizzare anche quello.
						tblRecord.addField("owner_id", docSearchResult.get(i).ownerID);
						docTableModel.synchronizeTable(tblRecord);
						try {
							docTableModel.updateDB("attivita_elementi", new String[] {"id_attivita", "nomefile", "titolo", "owner_id"}, null);
						}
						catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
			
			JButton deleteButton = new JButton("Togli");
			deleteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(docTable.getSelectedRowCount() != 0) {
						docTableModel.deleteSelectedRow();
						try {
							docTableModel.updateDB("attivita_elementi", new String[] {"id_attivita", "nomefile", "titolo", "owner_id"}, new String[]{"id_attivita", "nomefile", "titolo", "owner_id"});
						}
						catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
			
			JButton addLastIns = new JButton("Aggiungi Ultimo Inserito");
			addLastIns.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(tdmAttivita.getSelectedRow() == null) {
						JOptionPane.showMessageDialog(questo, "Selezionare prima Attività","Avviso",JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					if(FileInsertionPanel.lastInserted == null) {
						JOptionPane.showMessageDialog(questo, "Non inserito ancora nessun file in questa sessione.","Avviso",JOptionPane.INFORMATION_MESSAGE);
						return;
						
					}

					//Evito di inserire duplicati
					Vector<? extends TableRecord> dataVector = docTableModel.getDataVector();
					boolean salta = false;
					for(int n = 0; n < dataVector.size(); n++) {
						if(FileInsertionPanel.lastInserted.nameNoExtension.equals(dataVector.get(n).getField("nomefile")) &&
								FileInsertionPanel.lastInserted.title.equals(dataVector.get(n).getField("titolo")) &&
								FileInsertionPanel.lastInserted.ownerID.equals(dataVector.get(n).getField("owner_id")))
							salta = true;
					}
					if(salta) return;
					TableRecord tblRecord;
					tblRecord = docTableModel.addRow();
					tblRecord.addField("id_attivita", tdmAttivita.getSelectedRow().getField("id"));
					tblRecord.addField("nomefile", FileInsertionPanel.lastInserted.nameNoExtension);
					tblRecord.addField("titolo", FileInsertionPanel.lastInserted.title);
					//tblRecord.addField("descrizione", docSearchResult.get(i).title);//Questo server solo perchè la query di popolamento della tabella ha come campo corrispondente al titolo il campo "descrizione", quindi per poter visualizzare il valore dopo la creazione di una nuova riga bisogna valorizzare anche quello.
					tblRecord.addField("owner_id", FileInsertionPanel.lastInserted.ownerID);
					docTableModel.synchronizeTable(tblRecord);
					try {
						docTableModel.updateDB("attivita_elementi", new String[] {"id_attivita", "nomefile", "titolo", "owner_id"}, null);
					}
					catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			});
			
			JPanel addDeleteButtons = new JPanel(new VerticalLayout());
			addDeleteButtons.add(addButton);
			addDeleteButtons.add(deleteButton);
			addDeleteButtons.add(addLastIns);
			docManagementPanel.add(addDeleteButtons, BorderLayout.SOUTH);
		}
		return docManagementPanel;
	}
	
	private JScrollPane getDocTableSP() {
		if(docTableSP == null) {
			docTableModel = new TableDataManager(conn, new String[]{"Documento"}, new String[]{"titolo"});
			caricaDatiDocTableModel();
			docTable = docTableModel.getJTable();
			docTableSP = new JScrollPane();
			docTableSP.setPreferredSize(new Dimension(300, 1));
			docTableSP.setViewportView(docTable);
		}
		return docTableSP;
	}

	/**
	 * Carica i tadi prendendo come id_attivita quello dell'attività selezionata nella tabella delle attività (ovvio).
	 */
	private void caricaDatiDocTableModel() {
		try {
			if(tdmAttivita.getSelectedRowIndex() >= 0) {
/*
				String sqlString = "select * from fileinfo, attivita_elementi where id_attivita = " +
				tdmAttivita.getSelectedRow().getField("id") + " and id = id_elemento";
*/
				String sqlString = "select ae.id_attivita, ae.nomefile, ae.titolo, ae.owner_id, ut.identifier, GROUP_CONCAT(gc.descr) as groups, max(fi.versione) as versione, max(fi.data) as datains" +
					" from fileinfo fi, attivita_elementi ae, grp_cncpt_files gcf, grp_cncpt gc, utenti ut" +
					" where ae.id_attivita = " + tdmAttivita.getSelectedRow().getField("id") +
					" and fi.nomefile = ae.nomefile" +
					" and fi.descrizione = ae.titolo" +
					" and fi.owner_id = ae.owner_id" +
					" and gcf.file = fi.id" +
					" and gc.id = gcf.grp_cncpt" +
					" and ut.id = fi.owner_id" +
					" group by ae.nomefile, ae.titolo, ae.owner_id, ut.identifier" +
					" order by groups";
				//System.out.println(sqlString);
				docTableModel.init(sqlString, localProp.get("ConnString"), localProp.get("DBUser"), localProp.get("DBPassword"));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		catch (DangerousOperationException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private JPanel getUsersManagementPanel() {
		if(usersManagementPanel == null) {
			usersManagementPanel = new JPanel(new BorderLayout());
			JButton addButton = new JButton("Aggiungi");
			JButton deleteButton = new JButton("Togli");
			JPanel addDeletePanel = new JPanel(new VerticalLayout());
			addDeletePanel.add(addButton);
			addDeletePanel.add(deleteButton);
			usersManagementPanel.add(getUsersTableSP(), BorderLayout.CENTER);
			usersManagementPanel.add(addDeletePanel, BorderLayout.SOUTH);
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(tdmAttivita.getSelectedRow() == null) {
						JOptionPane.showMessageDialog(questo, "Selezionare prima Attività","Avviso",JOptionPane.INFORMATION_MESSAGE);
						return;
					}

					Vector<ObjToInt<String>> lista = new Vector<ObjToInt<String>>();
					try {
						PreparedStatement ps = conn.getPstat("select * from utenti", localProp.get("ConnString"), localProp.get("DBUser"), localProp.get("DBPassword"));
						ResultSet rs = ps.executeQuery();
						while(rs.next()) {
							ObjToInt<String> tmpObjToInt = new ObjToInt<String>(rs.getString("username") + " (tel.: " + rs.getString("telefono") + ")", rs.getInt("id"));
							lista.add(tmpObjToInt);
						}
					}
					catch (SQLException e1) {
						e1.printStackTrace();
					}
					catch (IOException e1) {
						e1.printStackTrace();
					}
					ObjSelectorDialog<ObjToInt<String>> usersSelectionDialog = new ObjSelectorDialog<ObjToInt<String>>(Window.getOwnerlessWindows()[0], Dialog.ModalityType.APPLICATION_MODAL, lista, "Utenti presenti", true);
					usersSelectionDialog.getJTable().setSelectionBackground(new Color(0xf0, 0xff, 0x00));
					usersSelectionDialog.setVisible(true);
					if(usersSelectionDialog.getSelectedObj() != null) {
						//Controllo duplicati
						ObjToInt<String> tmpObjToInt = usersSelectionDialog.getSelectedObj();
						@SuppressWarnings("unchecked")
						Vector<TableRecord> dataVector = (Vector<TableRecord>) usersTableModel.getDataVector();
						for(int i=0; i<usersTableModel.getDataVector().size(); i++) {
							if(dataVector.get(i).getField("id_utente").equals(tmpObjToInt.toInt())) return;
						}
						
						TableRecord tr = usersTableModel.addRow();
						tr.setField("identifier", tmpObjToInt.toString());//Questo solo per un fatto di visualizzazione in tabella.
						tr.setField("id_utente", tmpObjToInt.toInt());
						tr.setField("id_attivita", tdmAttivita.getSelectedRow().getField("id"));
						usersTableModel.synchronizeTable(tr);
						try {
							usersTableModel.updateDB("attivita_utenti", new String[]{"id_utente","id_attivita"}, null);
						}
						catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
			deleteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					usersTableModel.deleteSelectedRow();
					try {
						usersTableModel.updateDB("attivita_utenti", null, new String[]{"id_utente","id_attivita"});
					}
					catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			});
		}
		return usersManagementPanel;
	}
	
	private JScrollPane getUsersTableSP() {
		if(usersTableSP == null) {
			usersTableSP = new JScrollPane();
			usersTableModel = new TableDataManager(conn, new String[]{"Utenti"}, new String[]{"identifier"});
			usersTable = usersTableModel.getJTable();
			usersTableSP.setViewportView(usersTable);
			caricaDatiUsersTableModel();
			usersTableSP.setPreferredSize(new Dimension(200,1));
		}
		return usersTableSP;
	}

	private void caricaDatiUsersTableModel() {
		try {
			if(tdmAttivita.getSelectedRowIndex() >= 0) {
				String sqlString = "select * from utenti, attivita_utenti where id_attivita = " +
				tdmAttivita.getSelectedRow().getField("id") +
				" and id = id_utente";
				System.out.println(sqlString);
				usersTableModel.init(sqlString, localProp.get("ConnString"), localProp.get("DBUser"), localProp.get("DBPassword"));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		catch (DangerousOperationException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method initializes tabellaUtenti
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getTabellaAttivitaSP() {
		if(tabellaAttivitaSP == null) {
			try {
				tdmAttivita = new TableDataManager(conn, new String[] { "Descrizione", "Periodo", "Ultima" }, new String[] { "descrizione", "periodo", "ultima"});
				try {
					tdmAttivita.init("select * from attivita", conString, dbUser, dbPassword);
				} catch (DangerousOperationException e) {
					JOptionPane.showMessageDialog(questo, e, "Warning!", JOptionPane.WARNING_MESSAGE);
				}
			}
			catch (SQLException ex) {
				JOptionPane.showMessageDialog(this, "Inizializzazione Fallita:\n" + ex.getMessage() + "\nContact the administrator.");
				return null;
			}
			tabellaAttivitaSP = new JScrollPane();
			tabellaAttivita = tdmAttivita.getJTable();
			tabellaAttivita.addMouseListener(new MouseListener() {
				public void mouseReleased(MouseEvent e) {
					caricaDatiDocTableModel();
					caricaDatiUsersTableModel();
				}
				public void mouseClicked(MouseEvent e) {
				}
				public void mousePressed(MouseEvent e) {
				}
				public void mouseEntered(MouseEvent e) {
				}
				public void mouseExited(MouseEvent e) {
				}
			});
			tabellaAttivitaSP.setViewportView(tabellaAttivita);
			if(tdmAttivita.getJTable().getRowCount() > 0)
				tdmAttivita.setSelectedIndex(0);
		}
		return tabellaAttivitaSP;
	}

	/**
	 * This method initializes tabellaComandi
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getTabellaComandi() {
		if(tabellaComandi == null) {
			tabellaComandi = new JPanel();
			tabellaComandi.setLayout(new BorderLayout());
			tabellaComandi.add(getComandi(), BorderLayout.SOUTH);
			tabellaComandi.add(getTabellaAttivitaSP(), BorderLayout.CENTER);
		}
		return tabellaComandi;
	}

	/**
	 * This method initializes comandi
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getComandi() {
		if(comandi == null) {
			comandi = new JPanel();
			comandi.add(getAggiungi(), null);
			comandi.add(getElimina(), null);
			comandi.add(getModifica(), null);
			comandi.setBorder(new BevelBorder(1));
			comandi.add(getSalva(), null);
			comandi.add(getAnnullaTutto(), null);
			//comandi.add(getAnnulla(), null);
		}
		return comandi;
	}

	/**
	 * This method initializes aggiungi
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getAggiungi() {
		if(aggiungi == null) {
			aggiungi = new JButton("Aggiungi");
			aggiungi.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					inModifica=false;
					actualTableRecord = tdmAttivita.addRow();
					comandi.setVisible(false);
					pannelloInserimentoDatiAttivita.setVisible(true);
					getSalva().setBackground(new Color(255,255,0));
				}
			});
		}
		return aggiungi;
	}

	/**
	 * This method initializes elimina
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getElimina() {
		if(elimina == null) {
			elimina = new JButton("Elimina");
			elimina.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					inModifica=false;
					tdmAttivita.deleteSelectedRow();
					getSalva().setBackground(new Color(255,255,0));
				}
			});
		}
		return elimina;
	}

	/**
	 * This method initializes modifica
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getModifica() {
		if(modifica == null) {
			modifica = new JButton("Modifica");
			modifica.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					inModifica = true;
					actualTableRecord = tdmAttivita.editSelectedRow();
					if(actualTableRecord!=null) {
						caricaDatiCampi(actualTableRecord);
						comandi.setVisible(false);
						pannelloInserimentoDatiAttivita.setVisible(true);
						getTabellaAttivitaSP().setVisible(false);
					}
				}
			});
		}
		return modifica;
	}

	/**
	 * This method initializes salva
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getSalva() {
		if(salva == null) {
			salva = new JButton("Salva modifiche");
			salva.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(JOptionPane.showConfirmDialog(questo, "Sicuro di voler applicare le modifiche di questa tabella al DB?", "Warning!", JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
						try {
							tdmAttivita.updateDB("attivita", new String[] { "descrizione", "periodo", "ultima", "avviso_inviato" }, new String[] { "id" });
						}
						catch (SQLException e1) {
							JOptionPane.showMessageDialog(questo, e1.getMessage());
							e1.printStackTrace();
							return;
						}
						getSalva().setBackground(getAnnullaTutto().getBackground());
					}
				}
			});
		}
		return salva;
	}

	/**
	 * This method initializes annullaTutto	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAnnullaTutto() {
		if(annullaTutto == null) {
			annullaTutto = new JButton();
			annullaTutto.setText("Annulla");
			annullaTutto.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(JOptionPane.showConfirmDialog(questo, "Sicuro di voler annullare tutte le modifiche a questa tabella?", "Warning!", JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
						try {
							try {
								tdmAttivita.reinit();
							}
							catch (DangerousOperationException e1) {
								JOptionPane.showMessageDialog(questo, e1, "Error!", JOptionPane.ERROR_MESSAGE);
							} //Per ora si rifa` la query. Con piu` tempo si puo` pensare di modificare il TableDataManager in modo che le righe modificate vengano restituite al modificatore in nuovi TableRecord copie di quelli originali ma classificati come MODIFIED mentre i primi vengono classificati come OLD (in modo da tenerli in memoria) per costruire una funzione di ripristino che non effettui nessuna query. Ho gia` abbozzato qualcosa ma ovviamente manca il tempo quindi e` commentata.  
						}
						catch (SQLException e1) {
							JOptionPane.showMessageDialog(questo, "BD Problems: contact the administrator");
							e1.printStackTrace();
						}
						getSalva().setBackground(getAnnullaTutto().getBackground());
					}
				}
			});
		}
		return annullaTutto;
	}

	/**
	 * This method initializes pannelloInserimentoDatiUtente
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getPannelloInserimentoDatiAttivita() {
		if(pannelloInserimentoDatiAttivita == null) {
			pannelloInserimentoDatiAttivita = new JPanel(new VerticalLayout());
			pannelloInserimentoDatiAttivita.setVisible(false);
			pannelloInserimentoDatiAttivita.add(getFPDescrizione(), null);
			pannelloInserimentoDatiAttivita.add(getFPPeriodo(), null);
			pannelloInserimentoDatiAttivita.add(getFPUltima(), null);
			pannelloInserimentoDatiAttivita.add(getAnnullaSalva(), null);
		}
		return pannelloInserimentoDatiAttivita;
	}


//Inizio "template" per aggiunta dei campi nel pannello di modifica
	JPanel fPDescrizione = null;  //  @jve:decl-index=0:visual-constraint="5,0"
	private JPanel getFPDescrizione() {
		if(fPDescrizione == null) {
			JLabel jLabel = new JLabel("Descrizione");
			jLabel.setHorizontalAlignment(JLabel.RIGHT);
			jLabel.setPreferredSize(new Dimension(100, 21));
			fPDescrizione = new JPanel();
			fPDescrizione.setSize(new Dimension(500, 213));
			fPDescrizione.add(jLabel, null);
			fPDescrizione.add(getDescrizione(), null);
		}
		return fPDescrizione;
	}
	
	JTextArea descrizione = null;
	JScrollPane descrizioneSP = null;
	private JScrollPane getDescrizione() {
		if(descrizioneSP == null) {
			descrizioneSP = new JScrollPane();
			descrizioneSP.setPreferredSize(new Dimension(500, 150));
			if(descrizione == null) {
				descrizione = new JTextArea();
				//descrizione.setPreferredSize(new Dimension(300,150));
			}
			descrizioneSP.setViewportView(descrizione);
		}
		return descrizioneSP;
	}

	JPanel fPPeriodo = null;
	private JPanel getFPPeriodo() {
		if(fPPeriodo == null) {
			JLabel jLabel = new JLabel("Periodo (numero giorni)");
			jLabel.setHorizontalAlignment(JLabel.RIGHT);
			jLabel.setPreferredSize(new Dimension(150, 21));
			fPPeriodo = new JPanel();
			fPPeriodo.add(jLabel, null);
			fPPeriodo.add(getPeriodo(), null);
		}
		return fPPeriodo;
	}
	
	JTextField periodo = null;
	private JTextField getPeriodo() {
		if(periodo == null) {
			periodo = new JTextField();
			periodo.setPreferredSize(new Dimension(150, 21));

		}
		return periodo;
	}

	JPanel fpUltima = null;
	private JPanel getFPUltima() {
		if(fpUltima == null) {
			JLabel jLabel = new JLabel("Ultima (gg/mm/aaaa)");
			jLabel.setHorizontalAlignment(JLabel.RIGHT);
			jLabel.setPreferredSize(new Dimension(150, 21));
			fpUltima = new JPanel();
			fpUltima.add(jLabel, null);
			fpUltima.add(getUltima(), null);
		}
		return fpUltima;
	}
	
	JTextField ultima = null;
	private JTextField getUltima() {
		if(ultima == null) {
			ultima = new JTextField();
			ultima.setPreferredSize(new Dimension(150, 21));

		}
		return ultima;
	}
//Fine "template" per aggiunta dei campi nel pannello di modifica


	/**
	 * This method initializes annullaSalva
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getAnnullaSalva() {
		if(annullaSalva == null) {
			annullaSalva = new JPanel();
			annullaSalva.add(getApplica(), null);
			annullaSalva.add(getAnnulla(), null);
			annullaSalva.setBorder(new BevelBorder(1));
		}
		return annullaSalva;
	}

	DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALY);  //  @jve:decl-index=0:
	private JButton getApplica() {
		if(applica == null) {
			applica = new JButton("Applica");
			applica.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						if(descrizione.getText().length() == 0 ||
								periodo.getText().length() == 0 ||
								Integer.parseInt(periodo.getText()) == 0 ||
								ultima.getText().length() == 0) {
							JOptionPane.showMessageDialog(questo, "Campi non valorizzati correttamente", "ATTENZIONE", JOptionPane.WARNING_MESSAGE);
							return;
						}

						if(actualTableRecord.getField("ultima") != null &&
								((Date)actualTableRecord.getField("ultima")).getTime() != df.parse(ultima.getText()).getTime()) {
							actualTableRecord.setField("avviso_inviato", 0);
							JOptionPane.showMessageDialog(questo, "La data di ultima esecuzione è stata reimpostata: le modifiche avranno effetto all'aggiornamento del Data-Base con il comando \"Salva Modifiche\"");
						}
						
						actualTableRecord.setField("descrizione", descrizione.getText());
						actualTableRecord.setField("periodo", periodo.getText());
						actualTableRecord.setField("ultima", df.parse(ultima.getText()));
						tdmAttivita.synchronizeTable(actualTableRecord.getAccessKey());
						
						pannelloInserimentoDatiAttivita.setVisible(false);
						comandi.setVisible(true);
						comandi.setVisible(true);
						getSalva().setBackground(new Color(255,255,0));
					}
					catch (ParseException e1) {
				    	JOptionPane.showMessageDialog(questo, "Campi non valorizzati correttamente: ricontrollare", "ATTENZIONE", JOptionPane.WARNING_MESSAGE);
						return;
					}
				    catch(NumberFormatException ex)  
				    {  
				    	JOptionPane.showMessageDialog(questo, "Campi non valorizzati correttamente: ricontrollare", "ATTENZIONE", JOptionPane.WARNING_MESSAGE);
				    	return;  
				    }  
					getTabellaAttivitaSP().setVisible(true);
				}
			});
		}
		return applica;
	}

	/**
	 * This method initializes annulla
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getAnnulla() {
		if(annulla == null) {
			annulla = new JButton("Annulla");
			annulla.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(actualTableRecord != null)
						if(actualTableRecord.getStatus() == TableRecord.STATO_NUOVO && inModifica == false) tdmAttivita.deleteRowByKey(actualTableRecord.getAccessKey());
						else actualTableRecord.setStatus(TableRecord.STATO_INIZIALE);
					pannelloInserimentoDatiAttivita.setVisible(false);
					comandi.setVisible(true);
					getTabellaAttivitaSP().setVisible(true);
				}
			});
		}
		return annulla;
	}

	/**
	 * Carica i dati nei campi del pannello di inserimento/modifica durante la fase di inserimento/modifica di un nuovo record.
	 * @param tb
	 */
	private void caricaDatiCampi(TableRecord tb) {
		if(tb != null) {
			descrizione.setText(tb.getField("descrizione").toString());
			periodo.setText(tb.getField("periodo").toString());
			ultima.setText(df.format(tb.getField("ultima")));
		}
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"