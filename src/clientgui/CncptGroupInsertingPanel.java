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

package clientgui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Vector;

import customobj.gui.TableDataManager;
import customobj.gui.TableRecord;
import customobj.wrappers.DangerousOperationException;
import customobj.wrappers.LocalProp;
//import genericobj.db_con;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JTextField;
import javax.swing.JLabel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JCheckBox;

import remotizing.GlobalStub;
import customobj.wrappers.db_con;;

/**
 * @author lsola Pannello per l'inserimento e la gestione dei gruppi concettuali.
 */
public class CncptGroupInsertingPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//db_con conn;
	private TableDataManager groupsTableDM;
	private JTable groupsTable;
	private JPanel groupsManageingCommands = null;
	private JButton addGroup = null;
	private TableDataManager wordsTable;  //  @jve:decl-index=0:
	private JTable wordsJTable = null;
	private JPanel wordsInsertionAndEditPanel = null;
	private JScrollPane wordsTablePane;
	private JScrollPane groupsTablePane;
	private JPanel groupsManageingPanel = null;
	private CncptGroupInsertingPanel questo;
	private JPanel grpInsertionAndEditFields = null;
	private JTextField username = null;
	private JTextField conceptsDescription = null;
	private JButton confirmModifiedGroup = null;
	private JButton abortEditingGroup = null;
	private LocalProp localProp;
	private JPanel identifier = null;
	private JLabel jLabel = null;
	private JPanel description = null;
	private JLabel jLabel1 = null;
	private JPanel grpInsertionAndEditCommands = null;
	private TableRecord consideredGroupRecord;
	private JButton deleteGroups = null;
	private JPanel wordsEditingPanel = null;
	private JTextField wordTypeing = null;
	private JButton insertWord = null;
	private JButton deleteWord = null;
	private TableRecord consideredWordRecord;
	private boolean editingAWord; //Used to understand if the user has the intention of insert or edit a word.
	private JButton editGroups = null;
	private JButton joinGroups = null;
	private JPanel joiningGroupsSelection = null;
	private JScrollPane grpJoiningListScrollPane = null;
	private JTable joiningGroupsTable = null;
	private JPanel grpJoiningCommands = null;
	private JButton addJoiningGroup = null;
	private JButton removeJoiningGroup = null;
	private GlobalStub gc;
	private JButton goWithJoining = null;
	private JButton abortJoinig = null;
	private JTextField joinedGroupName = null;
	private JPanel jPanel = null;
	private JCheckBox deleteOldGroups = null;
	public static boolean groupsListChanges;
	private db_con conn = null;
	private String conString, dbUser, dbPassword;
	private boolean dbOperationsEnabled = false;
	
	/**
	 * Constructs a new CncptGroupInsertingPanel class.
	 * 
	 * @param conn
	 *            db_conn object to get the prepared statements.
	 * @throws IOException
	 */
	public CncptGroupInsertingPanel(GlobalStub gc) {
		super();
		this.gc = gc;
		try {
			localProp = new LocalProp("guiprop.prop");
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(questo, e.getMessage());
		}
		questo = this;
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
	}

	private void initialize() {
		this.setLayout(new BorderLayout());
		if(!dbOperationsEnabled) {
			JLabel notEnabled = new JLabel("Non Enabled (DB credential are not present in your guiprop.prop");
			notEnabled.setHorizontalAlignment(SwingConstants.CENTER);
			notEnabled.setFont(new Font("Dialog", Font.BOLD, 15));
			Color color = new Color(120,120,120);
			notEnabled.setForeground(color);
			this.add(notEnabled, BorderLayout.CENTER);
			return;
		}
		this.add(getGroupsManageingPanel(), BorderLayout.CENTER);
		this.add(getWordsInsertionAndEditPanel(), BorderLayout.EAST);
		/*
		 * GridBagLayout gridbag = new GridBagLayout(); GridBagConstraints c = new GridBagConstraints(); setFont(new Font("SansSerif", Font.PLAIN, 14)); setLayout(gridbag);
		 * 
		 * c.fill = GridBagConstraints.BOTH; c.weightx = 1.0; c.weighty = 1.0; c.gridheight = 2; gridbag.setConstraints(getGroupsTablePane(), c); add(getGroupsTablePane());
		 * 
		 * c.gridheight = 1; c.gridwidth = GridBagConstraints.REMAINDER; gridbag.setConstraints(getWordsTablePane(), c); add(getWordsTablePane());
		 * 
		 * c.weighty = 1.0; gridbag.setConstraints(getWordsInsertionCommands(), c); add(getWordsInsertionCommands());
		 * 
		 * 
		 * c.gridwidth = 2; gridbag.setConstraints(getGroupsCommand(), c); add(getGroupsCommand());
		 */
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getGroupsManageingCommands() {
		if(groupsManageingCommands == null) {
			groupsManageingCommands = new JPanel();
			groupsManageingCommands.add(getAddGroup(), null);
			groupsManageingCommands.add(getEditGroups(), null);
			groupsManageingCommands.add(getJoinGroups(), null);
			groupsManageingCommands.add(getDeleteGroups(), null);
			groupsManageingCommands.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseEntered(java.awt.event.MouseEvent e) {
					groupsListChanges = true;
				}
			});
		}
		return groupsManageingCommands;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getAddGroup() {
		if(addGroup == null) {
			addGroup = new JButton();
			addGroup.setText("Add");
			addGroup.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					consideredGroupRecord = getGroupsTableDM().addRow();
					consideredGroupRecord.setField("USER", MainPanel.userID);
					consideredGroupRecord.setField("USERNAME", MainPanel.userName);
					getWordsInsertionAndEditPanel().setEnabled(false);
					getGroupsTableDM().synchronizeTable(consideredGroupRecord);// Serve per aggiornare la vista della tabella con i dati impostati dalle righe precedenti nel record.
					editGroupsActionPerformed();
				}
			});
		}
		return addGroup;
	}

	/**
	 * This method initializes jTable
	 * 
	 * @return javax.swing.JTable
	 */
	private JScrollPane getWordsTablePane() {
		if(wordsTablePane == null) {
			wordsTablePane = new JScrollPane();
		}
		if(wordsJTable == null) {
			wordsJTable = getWordsTable().getJTable();
			wordsTablePane.getViewport().add(wordsJTable);
			wordsTablePane.setPreferredSize(new Dimension(100, 200));
		}
		return wordsTablePane;
	}

	private TableDataManager getWordsTable() {
		if(wordsTable == null) {
			//wordsTable = new TableDataManager(conn, new String[] { "Parole Chiave" }, new String[] { "WORD" });
			wordsTable = new TableDataManager(conn, new String[] { "Words" }, new String[] { "WORD" });
			ListSelectionModel rowSM = wordsTable.getJTable().getSelectionModel();
			rowSM.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if(e.getValueIsAdjusting()) {
						return;
					}
					ListSelectionModel lsm = (ListSelectionModel) e.getSource();// Questa pappardella serve ad ottenere il ListSelectionModel della tabella in quanto da qui essa non ? visibile. Il LSM, poi, serve solo per sapere se l'indice di selezione è cambiato (altrimenti bastava un e.getFirstIndex).
					if(lsm.isSelectionEmpty()) {
						// no rows are selected
					}
					else {// E' necessario ottenere l'indice da qui perche` questo ListSelectionListener viene chiamato prima rispetto a quello del TableDataManager (anche se e` aggiunto dopo).
						try {
							consideredWordRecord = wordsTable.getRowByIndex(lsm.getMinSelectionIndex());
							if(consideredWordRecord.getField("ID") != null) {
								wordTypeing.setText((String)consideredWordRecord.getField("WORD"));
								wordTypeing.requestFocus();
								editingAWord = true;
							}
						}
						catch(IndexOutOfBoundsException ex) {
						}
						
					}
				}
			});
			
		}
		return wordsTable;
	}

	private TableDataManager getGroupsTableDM() {
		if(groupsTableDM == null) {
			try {
				groupsTableDM = new TableDataManager(conn, new String[] { "Description" }, new String[] { "DESCR", "USER" });
				try {
					groupsTableDM.init("select GC.*, U.ID, U.USERNAME from grp_cncpt GC, utenti U where GC.USER=U.ID and U.ID=" + MainPanel.getUserID(), conString, dbUser, dbPassword);
					//gc.initialyzeATableDataManager(groupsTableDM, "select GC.*, U.ID, U.USERNAME from GRP_CNCPT GC, UTENTI U where GC.USER=U.ID and U.ID=" + MainPanel.getUserID(), localProp.get("user"), localProp.get("password"), gc.getNewAccessKey(MainPanel.userID,MainPanel.password), MainPanel.userID, MainPanel.password);
				} catch (DangerousOperationException e1) {
					JOptionPane.showMessageDialog(questo, e1, "DANGER!!", JOptionPane.WARNING_MESSAGE);
				}
				/*catch (ValidationException e) {
					JOptionPane.showMessageDialog(questo, e, "DANGER!!", JOptionPane.WARNING_MESSAGE);
				}*/
				groupsTableDM.setColumnAlignment(0, 'r');
				groupsTableDM.setTemporarySelectionIndex(0);
				groupsTable = groupsTableDM.getJTable();
				groupsTable.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, Color.red));
				ListSelectionModel rowSM = groupsTable.getSelectionModel();
				rowSM.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if(e.getValueIsAdjusting()) {
							return;
						}

						ListSelectionModel lsm = (ListSelectionModel) e.getSource();// Questa pappardella serve ad ottenere il ListSelectionModel della tabella in quanto da qui essa non ? visibile. Il LSM, poi, serve solo per sapere se l'indice di selezione ? cambiato (altrimenti bastava un e.getFirstIndex).
						if(lsm.isSelectionEmpty()) {
							// no rows are selected
						}
						else {// E' necessario ottenere l'indice da qui perche` questo ListSelectionListener viene chiamato prima rispetto a quello del TableDataManager (anche se e` aggiunto dopo).
							consideredGroupRecord = groupsTableDM.getRowByIndex(lsm.getMinSelectionIndex());
							if(consideredGroupRecord.getField("ID") != null) {
								getAddJoiningGroup().setEnabled(true);
								getRemoveJoiningGroup().setEnabled(false);
								try {
									getWordsTable().init("select * from grp_cncpt_words where GRP_CNCPT = " + consideredGroupRecord.getField("ID"), conString, dbUser, dbPassword);
								}
								catch (DangerousOperationException e1) {
									e1.printStackTrace();
								}	
								catch (SQLException e2) {
									e2.printStackTrace();
								}
							}
							else getWordsTable().clear();
						}
					}
				});
			}
			catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return groupsTableDM;

	}

	private JScrollPane getGroupsTablePane() {
		if(groupsTablePane == null) {
			groupsTablePane = new JScrollPane();
			groupsTablePane.getViewport().add(getGroupsTableDM().getJTable());
		}
		return groupsTablePane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getWordsInsertionAndEditPanel() {
		if(wordsInsertionAndEditPanel == null) {
			wordsInsertionAndEditPanel = new JPanel(new BorderLayout());
			wordsInsertionAndEditPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			wordsInsertionAndEditPanel.add(getWordsTablePane(), BorderLayout.CENTER);
			wordsInsertionAndEditPanel.add(getWordsEditingPanel(), BorderLayout.SOUTH);
		}
		return wordsInsertionAndEditPanel;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getGroupsManageingPanel() {
		if(groupsManageingPanel == null) {
			groupsManageingPanel = new JPanel(new BorderLayout());
			groupsManageingPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			groupsManageingPanel.add(getGroupsTablePane(), BorderLayout.CENTER);
			groupsManageingPanel.add(getGroupsManageingCommands(), BorderLayout.SOUTH);
		}
		return groupsManageingPanel;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getGrpInsertionAndEditFields() {
		if(grpInsertionAndEditFields == null) {
			grpInsertionAndEditFields = new JPanel();
			grpInsertionAndEditFields.setLayout(new GridLayout(3, 1));
			grpInsertionAndEditFields.add(getIdentifier(), null);
			grpInsertionAndEditFields.add(getDescription(), null);
			grpInsertionAndEditFields.add(getGrpInsertionAndEditCommands(), null);
		}
		return grpInsertionAndEditFields;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getUsername() {
		if(username == null) {
			username = new JTextField();
			username.setPreferredSize(new java.awt.Dimension(100, 19));
			username.setEditable(false);
		}
		return username;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getConceptsDescription() {
		if(conceptsDescription == null) {
			conceptsDescription = new JTextField();
			conceptsDescription.setPreferredSize(new java.awt.Dimension(150, 19));
			conceptsDescription.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyTyped(java.awt.event.KeyEvent e) {
					// System.out.println("keyTyped()"); // TODO Auto-generated Event stub keyTyped()
					if(e.getKeyChar() == '\n') {
						confirmModifiedGroup.dispatchEvent(new java.awt.event.ActionEvent(conceptsDescription, 0, "update"));
						//System.out.println("pippo");
					}
				}
			});
		}
		return conceptsDescription;
	}

	/**
	 * This method initializes jButton2
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getModifyGroupsTableOnDB() {
		if(confirmModifiedGroup == null) {
			confirmModifiedGroup = new JButton();
			confirmModifiedGroup.setText("OK");
			confirmModifiedGroup.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
					try {
						if(conceptsDescription.getText() == null || conceptsDescription.getText().length() == 0) {
							JOptionPane.showMessageDialog(questo, "Group description mustn't be empty", "Group description requirement", JOptionPane.WARNING_MESSAGE);
							return;
						}
						if(consideredGroupRecord != null) {
							consideredGroupRecord.setField("DESCR", conceptsDescription.getText());
							groupsTableDM.updateDB("grp_cncpt", new String[] { "DESCR", "USER" }, new String[] { "ID" });
							groupsTableDM.synchronizeTable(consideredGroupRecord.getAccessKey());
							groupsTableDM.getJTable().changeSelection(groupsTableDM.getJTable().getRowCount()-1, 0, true, false);
						}

						groupsManageingPanel.remove(grpInsertionAndEditFields);//Qui non bisogna chiamare la funzione getModifyInsertionFields(.).
						grpInsertionAndEditFields = null;
						groupsManageingPanel.add(getGroupsManageingCommands(), BorderLayout.SOUTH);
						groupsManageingPanel.validate();
						getGroupsManageingCommands().repaint();
						getGroupsTablePane().setEnabled(true);
					}
					catch (SQLException e1) {
						JOptionPane.showMessageDialog(questo, "Some SQL problems occured in updating DB: \n"+e1.getMessage());
						e1.printStackTrace();
					}
				}
			});
		}
		return confirmModifiedGroup;
	}

	/**
	 * This method initializes jButton2
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getAbortEditingGroup() {
		if(abortEditingGroup == null) {
			abortEditingGroup = new JButton();
			abortEditingGroup.setText("Abort");
			abortEditingGroup.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
					if(consideredGroupRecord != null) { 
						if(consideredGroupRecord.getStatus()==TableRecord.STATO_NUOVO) {//In questo modo cancella la riga dalla tabella visualizzata.
							groupsTableDM.deleteRow(consideredGroupRecord);
						}
					}
					groupsManageingPanel.remove(grpInsertionAndEditFields);
					grpInsertionAndEditFields = null;
					groupsManageingPanel.add(getGroupsManageingCommands(), BorderLayout.SOUTH);
					groupsManageingPanel.validate();
					getGroupsManageingCommands().repaint();
					getGroupsTablePane().setEnabled(true);
				}
			});
		}
		return abortEditingGroup;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getIdentifier() {
		if(identifier == null) {
			jLabel = new JLabel();
			jLabel.setText("User Identifier");
			identifier = new JPanel();
			identifier.add(jLabel, null);
			identifier.add(getUsername(), null);
		}
		return identifier;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getDescription() {
		if(description == null) {
			jLabel1 = new JLabel();
			jLabel1.setText("Description");
			description = new JPanel();
			description.add(jLabel1, null);
			description.add(getConceptsDescription(), null);
		}
		return description;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getGrpInsertionAndEditCommands() {
		if(grpInsertionAndEditCommands == null) {
			grpInsertionAndEditCommands = new JPanel();
			grpInsertionAndEditCommands.add(getModifyGroupsTableOnDB(), null);
			grpInsertionAndEditCommands.add(getAbortEditingGroup(), null);
		}
		return grpInsertionAndEditCommands;
	}

	public void editGroupsActionPerformed() {
			// System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
			// groupsInsertionCommands.setVisible(false);
			// modifyInsertionFields.setVisible(true);
			groupsManageingPanel.remove(getGroupsManageingCommands());
			groupsManageingPanel.add(getGrpInsertionAndEditFields(), BorderLayout.SOUTH);
			groupsManageingPanel.validate();
			grpInsertionAndEditFields.repaint();

			if(consideredGroupRecord != null) {
				username.setText((String) consideredGroupRecord.getField("USERNAME"));
				conceptsDescription.setText((String) consideredGroupRecord.getField("DESCR"));
			}
			conceptsDescription.requestFocus();
			getGroupsTablePane().setEnabled(false);
		}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getDeleteGroups() {
		if(deleteGroups == null) {
			deleteGroups = new JButton();
			deleteGroups.setText("Delete");
			deleteGroups.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					consideredGroupRecord = groupsTableDM.getSelectedRow();
					if(consideredGroupRecord != null && JOptionPane.showConfirmDialog(questo, "Are you sure you want to delete the group: "+consideredGroupRecord.getField("DESCR")+"?", "Group Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
						if(JOptionPane.showConfirmDialog(questo, "This means that the orphaned elements are going to be automatically assigned to a group.\nAre you sure?", "Group Deletion", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							groupsTableDM.deleteRow(consideredGroupRecord);
							try {
								groupsTableDM.updateDB("grp_cncpt", null, new String[] { "ID" });
							}
							catch (SQLException e1) {
								JOptionPane.showMessageDialog(questo, "Some SQL problems occured in updating DB: \n"+e1.getMessage());
								e1.printStackTrace();
							}
							groupsTableDM.getJTable().changeSelection(groupsTableDM.getJTable().getRowCount()-1, 0, true, false);
							//groupsTable.synchronizeTable(consideredGroupRecord.getAccessKey());
						}
					}					
				}
			});
		}
		return deleteGroups;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getWordsEditingPanel() {
		if(wordsEditingPanel == null) {
			wordsEditingPanel = new JPanel();
			wordsEditingPanel.setLayout(new FlowLayout());
			wordsEditingPanel.add(getWordTypeing(), null);
			wordsEditingPanel.add(getInsertWord(), null);
			wordsEditingPanel.add(getDeleteWord(), null);
		}
		return wordsEditingPanel;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getWordTypeing() {
		if(wordTypeing == null) {
			wordTypeing = new JTextField();
			wordTypeing.setPreferredSize(new java.awt.Dimension(150,19));
			wordTypeing.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyTyped(java.awt.event.KeyEvent e) {
					if(e.getKeyChar()=='\n') {
						if(wordTypeing.getText() != null && wordTypeing.getText().length() != 0) {
							if(!editingAWord) {
								insertTheWord();
							}
							else {
								if(consideredWordRecord != null) {
									//TableRecord tableRecord = wordsTable.getSelectedRow();
									//if(tableRecord==null) return;
									//tableRecord.
									consideredWordRecord.setField("WORD", wordTypeing.getText());
									try {
										wordsTable.updateDB("grp_cncpt_words", new String[]{"WORD"}, new String[]{"ID"});
									}
									catch (SQLException e1) {
										JOptionPane.showMessageDialog(questo, "Some SQL problems occured in updating DB: \n"+e1.getMessage());
										e1.printStackTrace();
									}
									//wordTypeing.setText(null); 
									wordsTable.synchronizeTable(consideredWordRecord);
									wordsJTable.repaint();
								}
							}
						}
					}
					else {
						if(consideredWordRecord != null) {
							consideredWordRecord.setStatus(TableRecord.STATO_MODIFICATO);
						}
					}
				}
			});
		}
		return wordTypeing;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getInsertWord() {
		if(insertWord == null) {
			insertWord = new JButton();
			insertWord.setText("Insert");
			insertWord.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					insertTheWord();
					wordTypeing.requestFocus();
					editingAWord = false;
				}
			});
		}
		return insertWord;
	}
	
	private void insertTheWord() {
		if(wordTypeing.getText() == null || wordTypeing.getText().length()==0) return;
		consideredWordRecord = wordsTable.addRow();
		consideredWordRecord.setField("WORD", wordTypeing.getText().toLowerCase());
		if(consideredGroupRecord != null) consideredWordRecord.setField("GRP_CNCPT", consideredGroupRecord.getField("ID"));
		else {
			wordsTable.deleteRow(consideredWordRecord);
			JOptionPane.showMessageDialog(questo, "SELECT BEFORE A GROUP");
			return;
		}
		consideredWordRecord.setField("RELEVANCE", 1);
		try {
			wordsTable.updateDB("grp_cncpt_words", new String[]{"WORD", "GRP_CNCPT", "RELEVANCE"}, new String[]{"ID"});
		}
		catch (SQLException e1) {
			String message = e1.getMessage();
			if(message.toLowerCase().indexOf("unique") != -1)//Qui` faccio l'ipotesi, abbastanza azzardata, che se nell'eccezione restituira dal server sql compare anche la parola 'unique' allora si tratta della violazione della condizione id unicita` della coppia (GRP_CNCPT, WORD).
				JOptionPane.showMessageDialog(questo, "The world you want to insert is jet present in this group.\n");
			else {
				JOptionPane.showMessageDialog(questo, "Some SQL problems occured in updating DB: \n"+e1.getMessage());
				e1.printStackTrace();
			}
		}
		wordsTable.synchronizeTable(consideredWordRecord);
		wordTypeing.setText(null);
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getDeleteWord() {
		if(deleteWord == null) {
			deleteWord = new JButton();
			deleteWord.setText("Delete");
			deleteWord.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int tempIndex = wordsJTable.getSelectedRow();
					if(consideredWordRecord == null) { 
						if(tempIndex != -1) {
							consideredWordRecord = wordsTable.getRowByIndex(tempIndex);
						}
						else return;
					}
					wordsTable.deleteSelectedRow();
					try {
						wordsTable.updateDB("grp_cncpt_words", null, new String[]{"ID"});
						if(wordsJTable.getRowCount()>tempIndex) {
							wordsJTable.changeSelection(tempIndex, 0, true, false);
						}
					}
					catch (SQLException e1) {
						JOptionPane.showMessageDialog(questo, "Some SQL problems occured in updating DB: \n"+e1.getMessage());
						e1.printStackTrace();
					}
					consideredWordRecord = null;
					wordTypeing.setText(null);
				}
			});
		}
		return deleteWord;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getEditGroups() {
		if(editGroups == null) {
			editGroups = new JButton();
			editGroups.setText("Edit");
			editGroups.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					consideredGroupRecord = groupsTableDM.editSelectedRow();
					editGroupsActionPerformed();
				}
			});
		}
		return editGroups;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJoinGroups() {
		if(joinGroups == null) {
			joinGroups = new JButton();
			joinGroups.setText("Join");
			joinGroups.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
					getGroupsManageingPanel().remove(getGroupsManageingCommands());
					getGroupsManageingPanel().add(getJoiningGroupsSelection(), BorderLayout.SOUTH);
					groupsManageingPanel.validate();
					groupsManageingPanel.repaint();
				}
			});
		}
		return joinGroups;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJoiningGroupsSelection() {
		if(joiningGroupsSelection == null) {
			joiningGroupsSelection = new JPanel();
			joiningGroupsSelection.setLayout(new BorderLayout());
			joiningGroupsSelection.add(getGrpJoiningCommands(), BorderLayout.NORTH);
			joiningGroupsSelection.add(getGrpJoiningListScrollPane(), BorderLayout.CENTER);
			joiningGroupsSelection.setPreferredSize(new Dimension(0, 200));
			joiningGroupsSelection.add(getJPanel(), BorderLayout.SOUTH);
		}
		return joiningGroupsSelection;
	}

	/**
	 * This method initializes grpJoiningListScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getGrpJoiningListScrollPane() {
		if(grpJoiningListScrollPane == null) {
			grpJoiningListScrollPane = new JScrollPane();
			grpJoiningListScrollPane.setViewportView(getJoiningGroupsTable());
		}
		return grpJoiningListScrollPane;
	}

	/**
	 * This method initializes joiningGroupsTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJoiningGroupsTable() {
		if(joiningGroupsTable == null) {
			joiningGroupsTable = new JTable(0,1);
			joiningGroupsTable.setCellSelectionEnabled(true);
			DefaultTableModel dftblmd = new DefaultTableModel(new String[]{"Joining Groups"}, 0);
			
			joiningGroupsTable.setModel(dftblmd);
			ListSelectionModel rowSM = joiningGroupsTable.getSelectionModel();
			rowSM.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if(e.getValueIsAdjusting()) {
						return;
					}
					ListSelectionModel lsm = (ListSelectionModel) e.getSource();// Questa pappardella serve ad ottenere il ListSelectionModel della tabella in quanto da qui essa non ? visibile. Il LSM, poi, serve solo per sapere se l'indice di selezione ? cambiato (altrimenti bastava un e.getFirstIndex).
					if(lsm.isSelectionEmpty()) {
						// no rows are selected
					}
					else {// E' necessario ottenere l'indice da qui perche` questo ListSelectionListener viene chiamato prima rispetto a quello del TableDataManager (anche se e` aggiunto dopo).
						getAddJoiningGroup().setEnabled(false);
						getRemoveJoiningGroup().setEnabled(true);
					}
				}
			});
		}
		return joiningGroupsTable;
	}

	/**
	 * This method initializes grpJoiningCommands	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getGrpJoiningCommands() {
		if (grpJoiningCommands == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = -1;
			gridBagConstraints.gridy = -1;
			grpJoiningCommands = new JPanel();
			grpJoiningCommands.setLayout(new GridBagLayout());
			grpJoiningCommands.add(getAddJoiningGroup(), gridBagConstraints);
			grpJoiningCommands.add(getRemoveJoiningGroup(), gridBagConstraints);
			grpJoiningCommands.add(getGoWithJoining(), gridBagConstraints);
			grpJoiningCommands.add(getAbortJoinig(), gridBagConstraints);
		}
		return grpJoiningCommands;
	}

	/**
	 * This method initializes addJoiningGroup	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAddJoiningGroup() {
		if (addJoiningGroup == null) {
			addJoiningGroup = new JButton();
			addJoiningGroup.setIcon(new ImageIcon("icons/downArrow.gif"));
			addJoiningGroup.setText("Add");
			addJoiningGroup.setEnabled(false);
			addJoiningGroup.addActionListener(new java.awt.event.ActionListener() {
				@SuppressWarnings("unchecked")
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int[] indexes = groupsTable.getSelectedRows();
					DefaultTableModel tblmd = (DefaultTableModel)joiningGroupsTable.getModel();
					Vector<Object> row;
					Vector<Object> dataVector = tblmd.getDataVector();
					for(int i=0; i<indexes.length; i++) {
						row = new Vector<Object>();
						row.add(groupsTableDM.getRowByIndex(indexes[i]).getField("DESCR"));
						row.add(groupsTableDM.getRowByIndex(indexes[i]).getField("ID"));
						dataVector.add(row);
						joiningGroupsTable.revalidate();
						//System.out.println(tblmd.getValueAt(0,1));
					}
					groupsTable.clearSelection();
				}
			});
		}
		return addJoiningGroup;
	}

	/**
	 * This method initializes removeJoiningGroup	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRemoveJoiningGroup() {
		if (removeJoiningGroup == null) {
			removeJoiningGroup = new JButton();
			removeJoiningGroup.setIcon(new ImageIcon("icons/upArrow.gif"));
			removeJoiningGroup.setText("Remove");
			removeJoiningGroup.setEnabled(false);
			removeJoiningGroup.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					DefaultTableModel tblmd = (DefaultTableModel)joiningGroupsTable.getModel(); 
					int[] indexes = joiningGroupsTable.getSelectedRows();
					for(int i=indexes.length-1; i>=0; i--) {
						tblmd.removeRow(indexes[i]);
					}
				}
			});
		}
		return removeJoiningGroup;
	}

	/**
	 * This method initializes goWithJoining	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getGoWithJoining() {
		if (goWithJoining == null) {
			goWithJoining = new JButton();
			goWithJoining.setText("GO");
			goWithJoining.addActionListener(new java.awt.event.ActionListener() {
				@SuppressWarnings("unchecked")
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int[] joiningGroupsID = new int[joiningGroupsTable.getRowCount()];
					DefaultTableModel tblmd = (DefaultTableModel)joiningGroupsTable.getModel();
					Vector<Object> dataVector = tblmd.getDataVector();
					for(int i=0; i<tblmd.getRowCount(); i++) {
						joiningGroupsID[i] = ((Integer)((Vector<Object>)dataVector.get(i)).get(1)).intValue();
					}
					try {
						boolean deleteOld = deleteOldGroups.isSelected();
						gc.joinCncptGroups(joiningGroupsID, joinedGroupName.getText(), deleteOld);
					}
					catch (SQLException e1) {
						System.err.println(e1.getMessage());
						e1.printStackTrace();
					}
					catch (IOException e1) {
						System.err.println(e1.getMessage());
						e1.printStackTrace();
					}
					getGroupsManageingPanel().remove(getJoiningGroupsSelection());
					getGroupsManageingPanel().add(getGroupsManageingCommands(), BorderLayout.SOUTH);
					try {
						groupsTableDM.reinit();
						wordsTable.clear();
						((DefaultTableModel)joiningGroupsTable.getModel()).setRowCount(0);
					}
					catch (SQLException e1) {
						JOptionPane.showMessageDialog(questo, e1.getMessage());
						//e1.printStackTrace();
					}
					catch (DangerousOperationException e1) {
						JOptionPane.showMessageDialog(questo, e1.getMessage());
						//e1.printStackTrace();
					}
					groupsManageingPanel.validate();
					groupsManageingPanel.repaint();
				}
			});
		}
		return goWithJoining;
	}

	/**
	 * This method initializes abortJoinig	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAbortJoinig() {
		if (abortJoinig == null) {
			abortJoinig = new JButton();
			abortJoinig.setText("Cancel");
			abortJoinig.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getGroupsManageingPanel().remove(getJoiningGroupsSelection());
					getGroupsManageingPanel().add(getGroupsManageingCommands(), BorderLayout.SOUTH);
					groupsManageingPanel.validate();
					groupsManageingPanel.repaint();
				}
			});
		}
		return abortJoinig;
	}

	/**
	 * This method initializes joinedGroupName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJoinedGroupName() {
		if (joinedGroupName == null) {
			joinedGroupName = new JTextField();
			joinedGroupName.setText("Joined Groups New Description (change!)");
		}
		return joinedGroupName;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if(jPanel == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints1.weightx = 1.0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getJoinedGroupName(), gridBagConstraints1);
			jPanel.add(getDeleteOldGroups(), new GridBagConstraints());
		}
		return jPanel;
	}

	/**
	 * This method initializes deleteOldGroups	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getDeleteOldGroups() {
		if(deleteOldGroups == null) {
			deleteOldGroups = new JCheckBox();
			deleteOldGroups.setText("Delete Old Groups");
		}
		return deleteOldGroups;
	}
}
