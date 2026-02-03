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

import customobj.gui.ObjSelectorPanel;
import customobj.gui.SimpleIntegerField;
import customobj.wrappers.LocalProp;

import javax.swing.JPanel;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import arcmanagement.Classific;
import arcmanagement.ElabException;
import arcmanagement.FileContent;
import arcmanagement.FileInfoRecord;
import arcmanagement.ValidationException;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.table.DefaultTableModel;

import customobj.containers.FloatToObjectSimpleNode;
import customobj.containers.ObjToInt;
import javax.swing.JComboBox;
import java.awt.Insets;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;

import remotizing.GlobalStub;

import java.awt.event.ItemEvent;

public class SearchesPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GlobalStub gc;
	private JScrollPane jScrollPane = null;
	private JTable searchResultTable = null;
	private DefaultTableModel cncptGroups;
	private JTable cncptGroupsForSearch = null;
	private JScrollPane jScrollPane1 = null;
	private SearchesPanel questo;
	private DefaultTableModel searchResult;
	private JButton jButton2 = null;
	private JButton search = null;
	private JLabel jLabel = null;
	private SimpleIntegerField minGroupsNumber = null;
	private JTextField searchingPhrase = null;
	private JLabel jLabel1 = null;
	private long accessKey;
	private JScrollPane jScrollPane2 = null;
	private JTable languagesFilters = null;
	private JComboBox languageSelection = null;
	private JLabel jLabel2 = null;
	private JPanel jPanel = null;
	private ObjSelectorPanel<ObjToInt<String>> objSelectorPanel = null;
	private CustomTableModel languagesFiltersModel;
	private JButton jButton = null;
	private JPanel jPanel1 = null;
	private JButton jButton1 = null;
	private LocalProp localProp;
	private Vector<FileInfoRecord> foundFInfoRecords;  //  @jve:decl-index=0:

	/**
	 * This method initializes
	 * 
	 */
	public SearchesPanel(GlobalStub gc) {
		super();
		this.gc = gc;
		this.questo = this;
		try {
			localProp = new LocalProp("guiprop.prop");
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(questo, e.getMessage());
		}
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
		gridBagConstraints33.gridx = 0;
		gridBagConstraints33.fill = GridBagConstraints.BOTH;
		gridBagConstraints33.gridwidth = 2;
		gridBagConstraints33.gridheight = 1;
		gridBagConstraints33.ipadx = 0;
		gridBagConstraints33.ipady = 110;
		gridBagConstraints33.gridy = 10;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.gridy = 7;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridheight = 3;
		gridBagConstraints.weightx = 0.7;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.ipadx = 101;
		gridBagConstraints.insets = new Insets(0, 0, 8, 0);
		gridBagConstraints.gridy = 1;
		GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
		gridBagConstraints22.gridx = 1;
		gridBagConstraints22.ipadx = 0;
		gridBagConstraints22.gridwidth = 0;
		gridBagConstraints22.fill = GridBagConstraints.BOTH;
		gridBagConstraints22.anchor = GridBagConstraints.NORTH;
		gridBagConstraints22.insets = new Insets(0, 0, 8, 0);
		gridBagConstraints22.gridy = 3;
		GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
		gridBagConstraints41.gridx = 0;
		gridBagConstraints41.anchor = GridBagConstraints.SOUTHEAST;
		gridBagConstraints41.weightx = 0.0;
		gridBagConstraints41.weighty = 0.5;
		gridBagConstraints41.gridy = 5;
		jLabel2 = new JLabel();
		jLabel2.setText("Adding Languages or Filters");
		GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
		gridBagConstraints32.fill = GridBagConstraints.NONE;
		gridBagConstraints32.gridy = 6;
		gridBagConstraints32.weightx = 0.0;
		gridBagConstraints32.anchor = GridBagConstraints.NORTHEAST;
		gridBagConstraints32.weighty = 0.5;
		gridBagConstraints32.gridx = 0;
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.fill = GridBagConstraints.BOTH;
		gridBagConstraints11.gridy = 5;
		gridBagConstraints11.weightx = 10.0;
		gridBagConstraints11.weighty = 1.0;
		gridBagConstraints11.gridwidth = 1;
		gridBagConstraints11.gridheight = 2;
		gridBagConstraints11.ipadx = 0;
		gridBagConstraints11.ipady = 0;
		gridBagConstraints11.gridx = 1;
		GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
		gridBagConstraints31.gridx = 0;
		gridBagConstraints31.gridy = 8;
		gridBagConstraints31.anchor = GridBagConstraints.EAST;
		jLabel1 = new JLabel();
		jLabel1.setText("Searching Phrase");
		GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
		gridBagConstraints21.fill = GridBagConstraints.BOTH;
		gridBagConstraints21.gridy = 8;
		gridBagConstraints21.weightx = 1.0;
		gridBagConstraints21.gridwidth = 2;
		gridBagConstraints21.insets = new Insets(8, 0, 8, 0);
		gridBagConstraints21.gridx = 1;
		jLabel = new JLabel();
		jLabel.setText("Minimum number of groups (of wich selected) the elements must belong to");
		jLabel.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		jLabel.setName("jLabel");
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 0;
		gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints4.gridwidth = 2;
		gridBagConstraints4.gridy = 9;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.fill = GridBagConstraints.BOTH;
		gridBagConstraints2.gridy = 1;
		gridBagConstraints2.weightx = 0.0;
		gridBagConstraints2.weighty = 1.0;
		gridBagConstraints2.gridheight = 1;
		gridBagConstraints2.gridwidth = 1;
		gridBagConstraints2.ipadx = 0;
		gridBagConstraints2.anchor = GridBagConstraints.CENTER;
		gridBagConstraints2.gridx = 1;
		this.setLayout(new GridBagLayout());
		this.setSize(new Dimension(733, 632));
		this.add(getJScrollPane(), gridBagConstraints2);
		this.add(getSearch(), gridBagConstraints4);
		this.add(getSearchingPhrase(), gridBagConstraints21);
		this.add(jLabel1, gridBagConstraints31);
		this.add(getJScrollPane2(), gridBagConstraints11);
		this.add(getLanguageSelection(), gridBagConstraints32);
		this.add(jLabel2, gridBagConstraints41);
		this.add(getJPanel(), gridBagConstraints22);
		this.add(getObjSelectorPanel(), gridBagConstraints);
		this.add(getJButton(), gridBagConstraints1);
		this.add(getJPanel1(), gridBagConstraints33);
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentShown(java.awt.event.ComponentEvent e) {
				if(CncptGroupInsertingPanel.groupsListChanges) {
					CncptGroupInsertingPanel.groupsListChanges = false;
					Class<? extends JPanel> classe = questo.getClass();
					try {
						MainPanel.reqTask(classe.getMethod("updateGroupsList"), questo);
					}
					catch (SecurityException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					catch (NoSuchMethodException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if(jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setPreferredSize(new Dimension(453, 100));
			jScrollPane.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
			jScrollPane.setViewportView(getCncptGroupsForSearch());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes searchResultTable
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getSearchResultTable() {
		if(searchResultTable == null) {
			searchResultTable = new JTable();
			searchResult = new CustomTableModel();
			searchResult.setColumnIdentifiers(new String[]{"Name", "Extension", "Title", "Date", "Owner", "ID"});
			searchResultTable.setModel(searchResult);
		}
		return searchResultTable;
	}

	/**
	 * This method initializes cncptGroupsForSearch
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getCncptGroupsForSearch() {
		if(cncptGroupsForSearch == null) {
			cncptGroups = new CustomTableModel();
			cncptGroups.setColumnIdentifiers(new String[] { "Conceptual Groups in which to Search" });
			cncptGroupsForSearch = new JTable();
			cncptGroupsForSearch.setModel(cncptGroups);
		}
		return cncptGroupsForSearch;
	}

	/**
	 * This method initializes jScrollPane1
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane1() {
		if(jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setPreferredSize(new Dimension(0, 0));
			jScrollPane1.setViewportView(getSearchResultTable());
		}
		return jScrollPane1;
	}

	@SuppressWarnings("unchecked")
	public void selectionAction() {
		int i;
		ObjToInt<String> selectedObject = objSelectorPanel.getSelectedObj();
		if(selectedObject == null) return;
		for(i = 0; i < cncptGroups.getRowCount(); i++) {
			if(((ObjToInt<String>) cncptGroups.getValueAt(i, 0)).toInt() == selectedObject.toInt()) break;
		}
		if(i >= 4) {
			JOptionPane.showMessageDialog(questo, "Not more than 4 groups can be selected");
			return;
		}
		if(i == cncptGroups.getRowCount()) cncptGroups.addRow(new ObjToInt[] { selectedObject });
		minGroupsNumber.setMaxValue(cncptGroups.getRowCount());
	}

	/**
	 * This method initializes jButton2
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton2() {
		if(jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setText("Remove Selected Groups");
			jButton2.setActionCommand("");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int[] selRows = cncptGroupsForSearch.getSelectedRows();
					for(int i = 0; i < selRows.length; i++) {
						cncptGroups.removeRow(selRows[i] - i);
					}
					minGroupsNumber.setMaxValue(cncptGroups.getRowCount());
				}
			});
		}
		return jButton2;
	}

	/**
	 * This method initializes search
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getSearch() {
		if(search == null) {
			search = new JButton();
			search.setText("Start Elements Analysis");
			foundFInfoRecords = new Vector<FileInfoRecord>();
			search.addActionListener(new java.awt.event.ActionListener() {
				@SuppressWarnings("unchecked")
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(cncptGroups.getRowCount() == 0 || languagesFiltersModel.getRowCount() == 0) return;
					try {
						accessKey = gc.getNewAccessKey(MainPanel.userID, MainPanel.password);
					}
					catch (RemoteException e4) {
						// TODO Auto-generated catch block
						e4.printStackTrace();
					}
					String or = "";
					StringBuffer sql = new StringBuffer("select FI.ID, FI.NOMEFILE, FI.ESTENSIONE, FI.DESCRIZIONE, FI.DATA, FI.OWNER_ID, U.IDENTIFIER, FI.ARCH_PATH from (fileinfo FI inner join grp_cncpt_files GCF on FI.ID = GCF.FILE) inner join utenti U on U.ID = FI.OWNER_ID where (");
					for(int i = 0; i < cncptGroups.getRowCount(); i++) {
						sql.append(or + "GCF.GRP_CNCPT = " + ((ObjToInt<String>) cncptGroups.getValueAt(i, 0)).toInt());
						or = " or ";
					}

					sql.append(") and (");

					or = "";
					for(int i = 0; i < languagesFiltersModel.getRowCount(); i++) {
						sql.append(or + "FI.LANGUAGE_FILTER = " + ((ObjToInt<String>) languagesFiltersModel.getValueAt(i, 0)).toInt());
						or = " or ";
					}

					sql.append(") and ");

					sql.append("FI.VERSIONE = (select max(FI1.VERSIONE) from fileinfo FI1 where FI1.NOMEFILE = FI.NOMEFILE and FI1.DESCRIZIONE = FI.DESCRIZIONE and FI.OWNER_ID = U.ID)");
					sql.append("group by FI.ID, FI.NOMEFILE, FI.ESTENSIONE, FI.DESCRIZIONE, FI.DATA, FI.OWNER_ID, U.IDENTIFIER, FI.ARCH_PATH having count(*) >= " + minGroupsNumber.getInt());
					//N.B. sulla query (non è banale). Con la query così formulata vengono selezionati gli elementi che appartengono ad un determinato gruppo o insieme di gruppi (o sottoinsieme di almeno N gruppi tra quello dichiarati), solo per le ultime versioni degli elementi stessi.
					//Se, per esempio, un elemento in versione 4 appartiene al gruppo A ed in versione 5 appartiene al gruppo B (l'utente ha deciso di associargli un gruppo diverso), se, durante la ricerca si specifica che si vogliono considerare elementi del gruppo A, tale elemento non verrà incluso nell'insieme di ricerca.
					int[] filterIndexes = new int[languagesFiltersModel.getRowCount()];
					for(int i = 0; i < languagesFiltersModel.getRowCount(); i++) {
						filterIndexes[i] = ((ObjToInt<String>) languagesFiltersModel.getValueAt(i, 0)).toInt();
					}
					try {
						gc.search(sql.toString(), searchingPhrase.getText(), 20, 5, filterIndexes, accessKey, MainPanel.userID, MainPanel.password);
					}
					catch (ValidationException e1) {
						JOptionPane.showMessageDialog(questo, e1.getMessage());
					}
					catch (RemoteException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}

					// Ciclo per chiedere di aggiornare i risultati in tabella fino a fine ricerca.
					Classific classifica;
					boolean cicla = true;
					
					searchResult.setRowCount(0);
					while(cicla) {
						try {
							Thread.sleep(1000);
							classifica = gc.getResponses(accessKey, MainPanel.userID, MainPanel.password);
							if(classifica != null && classifica.size() > 0) {
								cicla = false;
								System.out.println("Stringa cercata: " + classifica.getSearchString());
								Vector<FloatToObjectSimpleNode> vettoreClassifica = classifica.getClassific();
								FileInfoRecord tmpFInfo;
								foundFInfoRecords.clear();
								for(int i = 0; i < classifica.size(); i++) {
									// Notare che nella istruzione seguente la trasformazione dell'oggetto ritornato
									// dalla getValue a stringa avviene implicitamente tramite il metodo toString() che
									// nel FileInfoRecord e' stato sovrascritto...
									//System.out.println(vettoreClasifica.get(i).getValue() + "\n" + "punteggio:" + vettoreClasifica.get(i).getIndex() + "    richiesta:" + accessKey + "\n");
									tmpFInfo = (FileInfoRecord)vettoreClassifica.get(i).getValue();
									foundFInfoRecords.add(tmpFInfo);
									searchResult.addRow(new Object[]{tmpFInfo.nameNoExtension, tmpFInfo.estensione, tmpFInfo.title, tmpFInfo.data, tmpFInfo.ownerIdentifier, tmpFInfo.dbID});
									
								}
								gc.removeResponse(accessKey, MainPanel.userID, MainPanel.password);
							}
							else if(classifica != null && classifica.size() == 0) { // Devo catturare lo stato di "ricerca terminata ma non andata a buon fine" (nessun file trovato).
								gc.removeResponse(accessKey, MainPanel.userID, MainPanel.password);
								cicla = false;
							}
						}
						catch (InterruptedException e1) {
							// e1.printStackTrace();
							JOptionPane.showMessageDialog(questo, e1.getMessage());
							return;
						}
						catch (ValidationException e2) {
							// e2.printStackTrace();
							JOptionPane.showMessageDialog(questo, e2.getMessage());
							return;
						}
						catch (ElabException e3) {
							// e3.printStackTrace();
							JOptionPane.showMessageDialog(questo, e3.getMessage());
							return;
						}
						catch (RemoteException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
					}
				}
			});
		}
		return search;
	}

	/**
	 * This method initializes minGroupsNumber
	 * 
	 * @return javax.swing.JTextField
	 */
	private SimpleIntegerField getMinGroupsNumber() {
		if(minGroupsNumber == null) {
			minGroupsNumber = new SimpleIntegerField();
			minGroupsNumber.setToolTipText("Bacause the fact that any document can belong to more than one group, so this parameter can be very useful (if you know wath is means).");
			minGroupsNumber.setColumns(1);
			minGroupsNumber.setMaxValue(4);
			minGroupsNumber.setName("ninGroupsNumber");
			minGroupsNumber.setPreferredSize(new Dimension(10, 19));
			minGroupsNumber.setMinValue(1);
		}
		return minGroupsNumber;
	}

	/**
	 * This method initializes searchingPhrase
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getSearchingPhrase() {
		if(searchingPhrase == null) {
			searchingPhrase = new JTextField();
		}
		return searchingPhrase;
	}

	class CustomTableModel extends DefaultTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	/**
	 * This method initializes jScrollPane2
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane2() {
		if(jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setPreferredSize(new Dimension(600, 418));
			jScrollPane2.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
			jScrollPane2.setViewportView(getLanguagesFilters());
		}
		return jScrollPane2;
	}

	/**
	 * This method initializes languagesFilters
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getLanguagesFilters() {
		if(languagesFilters == null) {
			languagesFilters = new JTable();
			languagesFilters.setBackground(Color.white);
			languagesFiltersModel = new CustomTableModel();
			languagesFiltersModel.setColumnIdentifiers(new String[] { "Languages" });
			languagesFilters.setModel(languagesFiltersModel);
		}
		return languagesFilters;
	}

	/**
	 * This method initializes languageSelection
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getLanguageSelection() {
		if(languageSelection == null) {
			languageSelection = new JComboBox();
			languageSelection.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED && !(languageSelection.getSelectedIndex() == 0) && !languagesFiltersModel.getDataVector().contains(languageSelection.getSelectedItem())) languagesFiltersModel.addRow(languageSelection.getSelectedObjects());
				}
			});
			try {
				languageSelection.addItem(new ObjToInt<String>("", 0));
				Vector<ObjToInt<String>> languageList = gc.getFiltersList(true);
				Iterator<ObjToInt<String>> languageListI = languageList.iterator();
				while(languageListI.hasNext()) {
					languageSelection.addItem(languageListI.next());
				}
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(questo, "Problem retriving the filters list for selection:\n" + e.getMessage());
			}
		}
		return languageSelection;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if(jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BorderLayout());
			jPanel.add(jLabel, BorderLayout.CENTER);
			jPanel.add(getMinGroupsNumber(), BorderLayout.EAST);
			jPanel.add(getJButton2(), BorderLayout.NORTH);
		}
		return jPanel;
	}

	/**
	 * This method initializes objSelectorPanel
	 * 
	 * @return customobj.gui.ObjSelectorPanel
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ObjSelectorPanel getObjSelectorPanel() {
		if(objSelectorPanel == null) {
			// objSelectorPanel = new ObjSelectorPanel();
			try {
				Vector<ObjToInt<String>> groupsList = (Vector<ObjToInt<String>>) gc.getGrpCncptsList();

				objSelectorPanel = new ObjSelectorPanel(groupsList, "Conceptual Groups");
				objSelectorPanel.setSelectioButtonIcon(new ImageIcon("icons/rightArrow.gif"));
				objSelectorPanel.setSelectioButtonText(null);

				try {
					Class<? extends JPanel> classe = this.getClass();
					objSelectorPanel.setInvokdMethod(questo, classe.getMethod("selectionAction"));
				}
				catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
				catch (SecurityException e) {
					e.printStackTrace();
				}
				catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
			catch (ServerException e) {
				JOptionPane.showMessageDialog(questo, e.getMessage());
				e.printStackTrace();
			}
			catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				JOptionPane.showMessageDialog(questo, e1.getMessage());
			}
		}
		return objSelectorPanel;
	}
	
	public void updateGroupsList() {
		try {
			Vector<ObjToInt<String>> groupsList = gc.getGrpCncptsList();

			objSelectorPanel.updateSelectionListVector(groupsList);
		}
		catch (ServerException e1) {
			JOptionPane.showMessageDialog(questo, e1.getMessage());
			e1.printStackTrace();
		}
		catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if(jButton == null) {
			jButton = new JButton();
			jButton.setText("Remove Selected Language");
			jButton.setActionCommand("");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(languagesFilters.getSelectedRow() != -1) {
						languagesFiltersModel.removeRow(languagesFilters.getSelectedRow());
					}
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if(jPanel1 == null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = GridBagConstraints.BOTH;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.weighty = 1.0;
			gridBagConstraints5.gridx = 0;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 1;
			gridBagConstraints6.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints6.gridy = 0;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.gridx = -1;
			gridBagConstraints3.gridy = -1;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.weighty = 1.0;
			gridBagConstraints3.gridwidth = 0;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.setPreferredSize(new Dimension(281, 200));
			jPanel1.add(getJButton1(), gridBagConstraints6);
			jPanel1.add(getJScrollPane1(), gridBagConstraints5);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if(jButton1 == null) {
			jButton1 = new JButton();
			//jButton1.setText("View");
			jButton1.setIcon(new ImageIcon("icons/occhio20x64.gif"));
			jButton1.setFocusable(false);
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//JOptionPane.showMessageDialog(questo, "Dat implementare (è abbastanza facile)");
					int tableID = getSearchResultTable().getSelectedRow();
					if(tableID < 0 || tableID >= searchResult.getRowCount()) return;
					try {
						FileContent fileContent = gc.getFile(MainPanel.userName, MainPanel.password, (Integer)searchResult.getValueAt(tableID, 5));
						try {
							String outputPathName = localProp.get("CheckOutDir");
							fileContent.recreateLocalFile(outputPathName + "/" + fileContent.getSourceFileName());
							//File outFile = new File(outputPathName + "/" + fileContent.getSourceFileName());
							//if(!GenericFunctions.copiaFile(file, outFile)) JOptionPane.showMessageDialog(questo, "Impossibile effettuare l'operazione di estrazione del file " + file.getName() + " nella cartella " + outputPathName);
							JOptionPane.showMessageDialog(questo, "File " + fileContent.getSourceFileName() + " checked out in folder: " + outputPathName, "File Extraction", JOptionPane.INFORMATION_MESSAGE);
						}
						catch (IOException e1) {
							JOptionPane.showMessageDialog(questo, e1.getMessage());
							e1.printStackTrace();
						}
					}
					catch (ServerException e1) {
						JOptionPane.showMessageDialog(questo, "Server Message: " + e1.getMessage());
						e1.printStackTrace();
					}
					catch (ValidationException e1) {
						JOptionPane.showMessageDialog(questo, "Server Message: " + e1.getMessage());
						e1.printStackTrace();
					}
					catch (RemoteException e1) {
						JOptionPane.showMessageDialog(questo, "Server Message: " + e1.getMessage());
						e1.printStackTrace();
					}
				}
			});
		}
		return jButton1;
	}

	/**
	 * Ritorna la lista dei FileInfoRecord delle tuple della tabella "fileinfo" corrispondenti alle righe selezionate sulla tabella dei risultati di ricerca. 
	 * @return
	 */
	public Vector<FileInfoRecord> getFInfoRecordSelection() {
		int[] selRowsID = searchResultTable.getSelectedRows();
		Vector<FileInfoRecord> ret = new Vector<FileInfoRecord>(selRowsID.length);
		for(int i = 0; i<selRowsID.length; i++) {
			ret.add(foundFInfoRecords.get(selRowsID[i]));
		}
		return ret;
	}
}
