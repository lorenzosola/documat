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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;

import arcmanagement.ElabException;
import arcmanagement.FileInfoRecord;
import arcmanagement.ValidationException;
import arcmanagement.FileContent;

import java.awt.GridBagConstraints;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JComboBox;

import remotizing.GlobalStub;

import customobj.containers.ObjToInt;
import customobj.gui.ObjMultipleSelectorDialog;
import customobj.gui.ObjSelectorDialog;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileInsertionPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JButton jButton = null;
	private JFileChooser fileChooser; // @jve:decl-index=0:visual-constraint="85,420"
	private FileContent selectedFileContent;
	private Component questo;
	private GlobalStub gc;
	private JTextField selectedFileName = null;
	private JButton jButton1 = null;
	private JTextArea extractionExample = null;
	private long accessKey;// Deve essere assegnata solo quado viene effettuata la conversione di un file: da quel momento la chiave di accesso identifica la sessione di tutte le operazioni che si effettueranno su di esso.
	private JScrollPane jScrollPane = null;
	private JPanel jPanel = null;
	private JButton fileConversionSucceeded = null;
	private JLabel jLabel1 = null;
	private JPanel jPanel1 = null;
	private JLabel jLabel2 = null;
	private JComboBox selectedLanguage = null;
	private JTable selectedGroup = null;
	private JButton goWithInsertion = null;
	private JTextArea significativeWords = null;
	private JLabel jLabel3 = null;
	private JTextField title = null;
	private JLabel jLabel4 = null;
	private JButton jButton2 = null;
	private JScrollPane jScrollPane1 = null;
	// private ObjToInt deducedGroup;
	int deducedLanguage = 0;
	private LocalTableModel concGroupsTM;
	private Vector<ObjToInt<String>> groupsVector;  //  @jve:decl-index=0:
	private JButton jButton3 = null;
	private JButton jButton4 = null;
	String[] splittedName;
	String fileNameNoExtension;
	static public FileInfoRecord lastInserted = null;

	/**
	 * This is the default constructor
	 */
	public FileInsertionPanel(GlobalStub gc) {
		super();
		this.gc = gc;
		questo = this;
		groupsVector = new Vector<ObjToInt<String>>();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(628, 330);
		GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
		gridBagConstraints31.gridx = 2;
		gridBagConstraints31.gridy = 1;
		GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
		gridBagConstraints17.gridx = 2;
		gridBagConstraints17.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints17.gridy = 0;
		GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
		gridBagConstraints21.fill = GridBagConstraints.BOTH;
		gridBagConstraints21.gridy = 2;
		gridBagConstraints21.weightx = 1.0;
		gridBagConstraints21.weighty = 1.0;
		gridBagConstraints21.gridwidth = 0;
		gridBagConstraints21.gridx = 0;
		GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
		gridBagConstraints8.gridx = 0;
		gridBagConstraints8.gridwidth = 0;
		gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints8.gridy = 4;
		jLabel1 = new JLabel();
		jLabel1.setText("Conceptual Groups");
		GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
		gridBagConstraints12.gridx = 0;
		gridBagConstraints12.gridwidth = 0;
		gridBagConstraints12.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints12.gridy = 3;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.gridwidth = 1;
		gridBagConstraints1.gridheight = 2;
		gridBagConstraints1.gridx = 1;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 2;
		gridBagConstraints.gridy = 0;
		GridBagLayout layoutManager = new GridBagLayout();
		this.setLayout(layoutManager);
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		this.add(getJButton(), gridBagConstraints);
		this.add(getSelectedFileName(), gridBagConstraints1);
		this.add(getJPanel(), gridBagConstraints12);
		this.add(getJPanel1(), gridBagConstraints8);
		jPanel.setEnabled(false);
		this.add(getJScrollPane(), gridBagConstraints21);
		this.add(getJButton1(), gridBagConstraints17);
		this.add(getJButton4(), gridBagConstraints31);
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton(){
		if(jButton == null) {
			jButton = new JButton();
			jButton.setText("File Selection");
			if(fileChooser == null) fileChooser = new JFileChooser();
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int returnVal = fileChooser.showOpenDialog(questo);
					if(returnVal == JFileChooser.APPROVE_OPTION) {
						jPanel1.setVisible(false);
						try {
							selectedFileContent = new FileContent(fileChooser.getSelectedFile());
						}
						catch (IOException e1) {
							JOptionPane.showMessageDialog(questo, e1.getMessage(), "Problem", JOptionPane.ERROR_MESSAGE);
							e1.printStackTrace();
						}
						selectedFileName.setText(selectedFileContent.getSourceAbsolutePath());
						fileConversionSucceeded.setSelected(false);
						jButton1.setEnabled(true);
						jButton4.setEnabled(true);
						extractionExample.setText(null);
						fileConversionSucceeded.setEnabled(false);
					}
					// fileConversionSucceeded.setEnabled(false);
					extractionExample.requestFocus();
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes selectedFileName
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getSelectedFileName() {
		if(selectedFileName == null) {
			selectedFileName = new JTextField();
			selectedFileName.setBackground(new Color(204, 204, 204));
			selectedFileName.setEditable(false);
		}
		return selectedFileName;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if(jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Recognize");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						accessKey = gc.getNewAccessKey(MainPanel.userID, MainPanel.password);
					}
					catch (RemoteException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					try {
						gc.conversion(selectedFileContent, accessKey, MainPanel.userID, MainPanel.password);
						extractionExample.setText("---------------EXAMPLE OF THE AUTOMATICALLY EXTRACTED TEXT-----------\n\n.......................");
						extractionExample.append(gc.example(accessKey, MainPanel.userID, MainPanel.password));
						extractionExample.append(".............");
					}
					catch (ServerException e1) {
						JOptionPane.showMessageDialog(questo, e1.getMessage());
						jButton1.setEnabled(false);
						return;
					}
					catch (ValidationException e1) {
						JOptionPane.showMessageDialog(questo, e1.getMessage());
						jButton1.setEnabled(false);
						return;
					}
					catch (ElabException e1) {
						jButton1.setEnabled(false);
						JOptionPane.showMessageDialog(questo, e1.getMessage());
						return;
					}
					catch (RemoteException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					jButton1.setEnabled(false);
					jPanel.setVisible(true);
					fileConversionSucceeded.setEnabled(true);
				}
			});
			jButton1.setEnabled(false);
		}
		return jButton1;
	}

	/**
	 * This method initializes extractionExample
	 * 
	 * @return javax.swing.JTextPane
	 */
	private JTextArea getExtractionExample() {
		if(extractionExample == null) {
			extractionExample = new JTextArea();
			extractionExample.setLineWrap(true);
			extractionExample.setWrapStyleWord(true);
		}
		return extractionExample;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if(jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBorder(BorderFactory.createLineBorder(Color.pink, 1));
			jScrollPane.setViewportView(getExtractionExample());
			jScrollPane.setFocusable(false);
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if(jPanel == null) {
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 2;
			jPanel = new JPanel();
			jPanel.setBorder(BorderFactory.createLineBorder(Color.pink, 1));
			jPanel.add(getFileConversionSucceeded(), gridBagConstraints4);
			jPanel.setEnabled(false);
		}
		return jPanel;
	}

	/**
	 * This method initializes fileConversionSucceeded
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JButton getFileConversionSucceeded() {
		if(fileConversionSucceeded == null) {
			fileConversionSucceeded = new JButton();
			fileConversionSucceeded.setText("OK");
			fileConversionSucceeded.addActionListener(new ActionListener() {
				@SuppressWarnings("unchecked")
				public void actionPerformed(ActionEvent e) {
					try {
						significativeWords.setText(null);
						title.setText(null);

						/*
						 * Comparsa di una tabella che elencha le possibili corrispondenze con elementi già presenti in DB ed in base alla scelta efettuata riempia eventualmente i campi.
						 */
						splittedName = selectedFileContent.getSourceFileName().split("\\.|/.");
						fileNameNoExtension = splittedName[splittedName.length - 2];
						Vector<FileInfoRecord> firecs = null;
						try {
							firecs = gc.findEementsByFileName(fileNameNoExtension);
						}
						catch (ElabException e1) {
							JOptionPane.showMessageDialog(questo, e1.getMessage());
							// e1.printStackTrace();
						}
						if(firecs.size() != 0) {
							Vector<JLabel> objDescriptions = new Vector<JLabel>();
							FileInfoRecord tmpFirec;
							int i;
							for(i = 0; i < firecs.size(); i++) {
								// Sistemare in modo che il campo venga visualizzato in modo elegante. Forse è necessario cambiare il renderer della cella.....
								tmpFirec = firecs.get(i);
								tmpFirec.enableDNiceStrPres("<HTML>", "<BR>", "</HTML>");
								// System.out.println(tmpFirec.toString());
								objDescriptions.add(new JLabel(tmpFirec.toString()));
							}
							ObjSelectorDialog<JLabel> objSel = new ObjSelectorDialog<JLabel>(Window.getOwnerlessWindows()[0], Dialog.ModalityType.APPLICATION_MODAL, objDescriptions, "Probable Elements. If you are going to create a new element press \"Cancell\" or \"OK\" prior to select any row", false);
							objSel.setSize(600,500);
							objSel.getJTable().setRowHeight(100);
							objSel.getJTable().setBackground(new Color(0xe0, 0xd6, 0x7e));
							objSel.getJTable().setSelectionBackground(new Color(0x00, 0xff, 0x00));
							objSel.setVisible(true);
							// concGroupsTM.addRow(new Object[]{deducedGroup});
							loadFilterComboBoxesItemsFromDB();
							if(objSel.getSelectedObj() != null) {
								FileInfoRecord firec = firecs.get(objSel.getJTable().getSelectedRow());
								try {
									firec = gc.getAllElementInformation(firec.nameNoExtension, firec.title, firec.ownerID);
								}
								catch (ElabException e1) {
									// TODO Auto-generated catch block
									JOptionPane.showMessageDialog(questo, e1.getMessage());
									return;
								}
								// Impostazione della lingua della vecchia versione.
								for(i = 0; i < selectedLanguage.getItemCount(); i++) {
									if(((ObjToInt<String>) selectedLanguage.getItemAt(i)).toInt() == deducedLanguage) selectedLanguage.setSelectedIndex(i);
								}
								setGroupsVector();
								// Impostazione della lista di gruppi della vecchia versione.
								concGroupsTM.setRowCount(0);
								for(int n = 0; n < firec.groupsList.size(); n++) {
									for(i = 0; i < groupsVector.size(); i++) {
										if(((ObjToInt<String>) groupsVector.get(i)).toInt() == firec.groupsList.get(n)) {
											concGroupsTM.addRow(new Object[] { groupsVector.get(i) });
											break;
										}
									}
								}
								// Impostazione del titolo della vecchia versione.
								getTitle().setText(firec.title);
								// Impostazione delle parole significative della vecchia versione.
								JTextArea sigw = getSignificativeWords();
								for(i = 0; i < firec.sigWords.size(); i++) {
									sigw.append(firec.sigWords.get(i) + " ");
								}

							}
							else {
								reloadGroupsAndLanguageFields();
							}
						}
						else {
							reloadGroupsAndLanguageFields();
						}
					}
					catch (ServerTaskException e1) {
						return;
					}
					catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						JOptionPane.showMessageDialog(questo, e1.getMessage());
					}
					jPanel1.setVisible(true);
				}
			});
			fileConversionSucceeded.setEnabled(false);
		}
		return fileConversionSucceeded;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if(jPanel1 == null) {
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 0;
			gridBagConstraints16.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints16.fill = GridBagConstraints.NONE;
			gridBagConstraints16.gridy = 1;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.fill = GridBagConstraints.BOTH;
			gridBagConstraints10.gridy = 3;
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.weighty = 1.0;
			gridBagConstraints10.gridwidth = 4;
			gridBagConstraints10.gridheight = 2;
			gridBagConstraints10.gridx = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints11.gridy = 4;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.anchor = GridBagConstraints.EAST;
			gridBagConstraints15.gridy = 2;
			jLabel4 = new JLabel();
			jLabel4.setText("Title");
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.fill = GridBagConstraints.BOTH;
			gridBagConstraints14.gridy = 2;
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.gridwidth = 5;
			gridBagConstraints14.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints14.gridx = 1;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.ipadx = 0;
			gridBagConstraints13.ipady = 0;
			gridBagConstraints13.anchor = GridBagConstraints.EAST;
			gridBagConstraints13.gridy = 3;
			jLabel3 = new JLabel();
			jLabel3.setText("Relevant Words (for A.C.E.)");
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.gridwidth = 5;
			gridBagConstraints7.ipadx = 0;
			gridBagConstraints7.ipady = 0;
			gridBagConstraints7.insets = new Insets(0, 2, 2, 2);
			gridBagConstraints7.gridy = 5;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.gridx = 4;
			gridBagConstraints3.weightx = 1.0;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.gridheight = 2;
			gridBagConstraints6.weightx = 1.0;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 2;
			gridBagConstraints9.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints9.gridy = 0;
			jLabel2 = new JLabel();
			jLabel2.setText("Language");
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.gridwidth = 1;
			gridBagConstraints5.gridy = 0;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.setBorder(BorderFactory.createLineBorder(Color.pink, 1));
			jPanel1.setBackground(new Color(204, 204, 204));
			jPanel1.add(jLabel1, gridBagConstraints5);
			jPanel1.add(getSelectedGroup(), gridBagConstraints6);
			jPanel1.add(jLabel2, gridBagConstraints9);
			jPanel1.add(getSelectedLanguage(), gridBagConstraints3);
			jPanel1.add(getGoWithInsertion(), gridBagConstraints7);
			jPanel1.add(jLabel3, gridBagConstraints13);
			jPanel1.add(jLabel4, gridBagConstraints15);
			jPanel1.add(getTitle(), gridBagConstraints14);
			jPanel1.add(getJButton2(), gridBagConstraints11);
			jPanel1.add(getJScrollPane1(), gridBagConstraints10);
			jPanel1.add(getJButton3(), gridBagConstraints16);
			jPanel1.setVisible(false);
		}
		return jPanel1;
	}

	/**
	 * This method initializes selectedLanguage
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getSelectedLanguage() {
		if(selectedLanguage == null) {
			selectedLanguage = new JComboBox();
			// selectedLanguage.setEnabled(false);
		}
		return selectedLanguage;
	}

	/**
	 * This method initializes selectedGroup
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JTable getSelectedGroup() {
		if(selectedGroup == null) {
			selectedGroup = new JTable();
			// selectedGroup.setEnabled(false);
			concGroupsTM = new LocalTableModel(new String[] { "Appartainance Groups" }, 0);
			selectedGroup.setModel(concGroupsTM);
			selectedGroup.setPreferredSize(new Dimension(0, 60));
			selectedGroup.setFocusable(false);
			selectedGroup.addMouseListener(new java.awt.event.MouseAdapter() {
				@SuppressWarnings("unchecked")
				public void mouseReleased(java.awt.event.MouseEvent e) {
					Vector<ObjToInt<String>> initialSelectionVector = new Vector<ObjToInt<String>>();
					for(int i = 0; i < concGroupsTM.getRowCount(); i++) {
						initialSelectionVector.add((ObjToInt<String>) concGroupsTM.getValueAt(i, 0));
					}
					ObjMultipleSelectorDialog<ObjToInt<String>> groupsConfiguration = new ObjMultipleSelectorDialog<ObjToInt<String>>(MainPanel.questo, Dialog.ModalityType.APPLICATION_MODAL, groupsVector, initialSelectionVector, "Available Groups", true);
					groupsConfiguration.setVisible(true);
					Vector<ObjToInt<String>> selection = groupsConfiguration.getObjSelection();
					concGroupsTM.setRowCount(0);
					// La condizione serve in quanto se nessun gruppo viene associato dall'utente allora l'associazione id default è quella calcolata dal server.
					if(selection != null) {
						for(int i = 0; i < selection.size(); i++) {
							concGroupsTM.addRow(new Object[] { selection.get(i) });
						}
					}
				}
			});
		}
		return selectedGroup;
	}

	private void reloadGroupsAndLanguageFields() throws ServerTaskException {
		concGroupsTM.setRowCount(0);
		try {
			addDefaultGroup();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(questo, "Problem in deducing the conceptual group:\n" + e.getMessage() + "\nSet it manually");
			// throw new ServerTaskException(e.getMessage());
		}
		selectedLanguage.removeAllItems();
		try {
			loadFilterComboBoxesItemsFromDB();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(questo, "Problem in deducing the language:\n" + e.getMessage() + "\nSet it manually");
			// throw new ServerTaskException(e.getMessage());
		}
	}

	private void setGroupsVector() {
		try {
			groupsVector.clear();
			groupsVector = gc.getGrpCncptsList();
		}
		catch (ServerException e) {
			JOptionPane.showMessageDialog(questo, "Problem retriving the group list for selection:\n" + e.getMessage());
		}
		catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addDefaultGroup() throws Exception {
		setGroupsVector();
		int deducedGroupID = gc.findGroupCncpt(accessKey, MainPanel.userID, MainPanel.password);
		for(int i = 0; i < groupsVector.size(); i++) {
			if(((ObjToInt<String>) groupsVector.get(i)).toInt() == deducedGroupID) {
				concGroupsTM.addRow(new Object[] { groupsVector.get(i) });
				// deducedGroup = groupsVector.get(i);
				break;
			}
		}
		// selectedGroup.setEnabled(true);
	}

	@SuppressWarnings("unchecked")
	private void loadFilterComboBoxesItemsFromDB() throws Exception {
		Vector<ObjToInt<String>> filtersList;
		try {
			filtersList = gc.getFiltersList(true);
			selectedLanguage.addItem(new ObjToInt<String>("", 0));
			Iterator<ObjToInt<String>> flI = filtersList.iterator();
			while(flI.hasNext()) {
				selectedLanguage.addItem(flI.next());
			}
			deducedLanguage = gc.findLanguage(accessKey, MainPanel.userID, MainPanel.password);
			for(int i = 0; i < selectedLanguage.getItemCount(); i++) {
				if(((ObjToInt<String>) selectedLanguage.getItemAt(i)).toInt() == deducedLanguage) selectedLanguage.setSelectedIndex(i);
			}
		}
		catch (SQLException e) {
			JOptionPane.showMessageDialog(questo, "Problem retriving the filters list for selection:\n" + e.getMessage());
		}
		catch (ServerException e) {
			JOptionPane.showMessageDialog(questo, "Problem retriving the filters list for selection:\n" + e.getMessage());
		}
		// selectedLanguage.setEnabled(true);
	}

	/**
	 * This method initializes goWithInsertion
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getGoWithInsertion() {
		if(goWithInsertion == null) {
			goWithInsertion = new JButton();
			goWithInsertion.setText("GO");
			goWithInsertion.addActionListener(new java.awt.event.ActionListener() {
				@SuppressWarnings("unchecked")
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						int calculatedVersion;
						// Ottengo il nome del file senza estensione.
						String elementName = selectedFileContent.getSourceFileName();// Qui si e` sicuri per la riuscita della conversione del file da parte del server, che il nome del file ha una estensione separata dal punto.
						elementName = elementName.substring(0, elementName.lastIndexOf('.'));

						int[] groupsList = new int[concGroupsTM.getRowCount()];
						if(concGroupsTM.getRowCount() == 0) {
							JOptionPane.showMessageDialog(questo, "No one group association: select at least one group.");
							return;
						}
						for(int i = 0; i < concGroupsTM.getRowCount(); i++) {
							groupsList[i] = ((ObjToInt<String>) concGroupsTM.getValueAt(i, 0)).toInt();
						}
						FileInfoRecord fb = gc.ambiguityCheck(elementName, title.getText(), groupsList, 1);
						// Esistenza di elemento con titolo simile o = ed intersezione gruppi con almeno 1 elemento.
						if(fb != null) {
							// Se il nome del file e` lo stesso
							int scelta;
							if(fb.nameNoExtension.concat(".").concat(fb.estensione).equals(selectedFileContent.getSourceFileName())) {
								scelta = JOptionPane.showOptionDialog(questo, "Server found that the file you attempt to insert has probabily\nthe same meaning of an element jet present in archive\n\""
										+ fb.getTitle()
										+ "\"\nFile Name: "
										+ fb.nameNoExtension
										+ "\nExtension (remember that this doesn't participate to the Element Identification): "
										+ fb.estensione + "\nOuner: " + fb.ownerUserName, null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[] { "GO back",
										"Confirm the insertion of a new element" }, 0);
								if(scelta == 0) return;
							}
						}

						if(significativeWords.getText().length() > 2000) {
							JOptionPane.showMessageDialog(questo, "The field containing the significative words must be not bigger than 2000 caracters:\ndelete some word");
							return;
						}

						String[] sigWStrings = significativeWords.getText().split("\t+|\n+|\r+|\f+|\\a+|\\e+|\\p{Punct}| +");
						Vector<String> sigWords = new Vector<String>();
						for(int i = 0; i < sigWStrings.length; i++) {
							sigWords.add(sigWStrings[i]);
						}
						//selectedFileContent = null;//Per dar modo a garbage collector di disallocare selectedFile permettendo il rilascio al file system per poter cancellare.
						calculatedVersion = gc.insertFile(title.getText(), sigWords, groupsList, ((ObjToInt<String>) selectedLanguage.getSelectedItem()).toInt(), accessKey, MainPanel.userID, MainPanel.password);
						JOptionPane.showMessageDialog(null, "File succesfully inserted with version: " + calculatedVersion);
					}
					catch (ServerException e1) {
						JOptionPane.showMessageDialog(questo, "Server Exception:\n" + e1.getMessage());
					}
					catch (ValidationException e1) {
						JOptionPane.showMessageDialog(questo, "Validation Exception:\n" + e1.getMessage());
					}
					catch (ElabException e1) {
						JOptionPane.showMessageDialog(questo, "Elab Exception:\n" + e1.getMessage());
					}
					catch (SQLException e1) {
						JOptionPane.showMessageDialog(questo, "Elab Exception:\n" + e1.getMessage());
					}
					catch (IOException e1) {
						JOptionPane.showMessageDialog(questo, "Elab Exception:\n" + e1.getMessage());
					}
					/*
					finally {
						selectedFile = null;//Per dar modo a garbage collector di disallocare selectedFile permettendo il rilascio al file system per poter cancellare.
					}
					*/
					try {
						lastInserted = gc.getAllElementInformation(fileNameNoExtension, title.getText(), MainPanel.userID);
					}
					catch (RemoteException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(questo, e1.getMessage());
					}
					catch (ElabException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(questo, e1.getMessage());
					}

					fileConversionSucceeded.setEnabled(false);
					selectedFileName.setText(null);
					extractionExample.setText(null);
					title.setText(null);
					significativeWords.setText(null);
					jPanel1.setVisible(false);
				}
			});
		}
		return goWithInsertion;
	}

	/**
	 * This method initializes significativeWords
	 * 
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getSignificativeWords() {
		if(significativeWords == null) {
			significativeWords = new JTextArea();
			significativeWords.setWrapStyleWord(true);
			significativeWords.setLineWrap(true);
		}
		return significativeWords;
	}

	/**
	 * This method initializes title
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getTitle() {
		if(title == null) {
			title = new JTextField();
		}
		return title;
	}

	/**
	 * This method initializes jButton2
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton2() {
		if(jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setText("Auto Extraction");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				@SuppressWarnings("unchecked")
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						Vector<String> sigWordsList;
						sigWordsList = gc.extractSigWordsList(accessKey, new int[] { ((ObjToInt<String>) selectedLanguage.getSelectedItem()).toInt() });
						if(sigWordsList != null) {
							Iterator<String> iter = sigWordsList.iterator();
							significativeWords.setText(null);
							while(iter.hasNext()) {
								significativeWords.append(iter.next());
								significativeWords.append(" ");
							}
						}
					}
					catch (ElabException e1) {
						JOptionPane.showMessageDialog(questo, e1.getMessage());
						// e1.printStackTrace();
					}
					catch (ValidationException e1) {
						JOptionPane.showMessageDialog(questo, e1.getMessage());
						// e1.printStackTrace();
					}
					catch (RemoteException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
			});
		}
		return jButton2;
	}

	/**
	 * This method initializes jScrollPane1
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane1() {
		if(jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setPreferredSize(new Dimension(300, 100));
			jScrollPane1.setViewportView(getSignificativeWords());
			// jScrollPane1.setSize(new Dimension(200,100));
		}
		return jScrollPane1;
	}

	private class LocalTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		public LocalTableModel(String[] colname, int i) {
			super(colname, i);
		}

		@SuppressWarnings("unused")
		public void addRowNoDup(Object[] row) {
			int i;
			for(i = 0; i < dataVector.size(); i++) {
				if(dataVector.get(i).equals(row)) break;
			}
			if(i == dataVector.size()) addRow(row);
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	/**
	 * This method initializes jButton3
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton3() {
		if(jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setText("Recalculate");
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					concGroupsTM.setRowCount(0);
					try {
						addDefaultGroup();
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(questo, "Problem in deducing the conceptual group:\n" + e1.getMessage() + "\nSet it manually");
						// throw new ServerTaskException(e.getMessage());
					}
				}
			});
		}
		return jButton3;
	}

	/**
	 * This method initializes jButton4
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton4() {
		if(jButton4 == null) {
			jButton4 = new JButton();
			jButton4.setText("Insert Manually");
			jButton4.setEnabled(false);
			jButton4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(extractionExample.getCaretPosition() < 10) {
						JOptionPane.showMessageDialog(questo, "Insert some text in the panel first (by pressing \"Ctrl + V\").");
						return;
					}
					try {
						accessKey = gc.getNewAccessKey(MainPanel.userID, MainPanel.password);
					}
					catch (RemoteException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					try {
						gc.conversion(selectedFileContent, extractionExample, accessKey, MainPanel.userID, MainPanel.password);
						extractionExample.setText("---------------EXAMPLE OF THE AUTOMATICALLY EXTRACTED TEXT-----------\n\n.......................");
						extractionExample.append(gc.example(accessKey, MainPanel.userID, MainPanel.password));
						extractionExample.append(".............");
					}
					catch (ServerException e1) {
						JOptionPane.showMessageDialog(questo, e1.getMessage());
						jButton1.setEnabled(false);
						return;
					}
					catch (ValidationException e1) {
						JOptionPane.showMessageDialog(questo, e1.getMessage());
						jButton1.setEnabled(false);
						return;
					}
					catch (ElabException e1) {
						jButton1.setEnabled(false);
						JOptionPane.showMessageDialog(questo, e1.getMessage());
						return;
					}
					catch (RemoteException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					jButton1.setEnabled(false);
					jButton4.setEnabled(false);
					jPanel.setVisible(true);
					fileConversionSucceeded.setEnabled(true);
				}
			});
		}
		return jButton4;
	}
} // @jve:decl-index=0:visual-constraint="10,10"
