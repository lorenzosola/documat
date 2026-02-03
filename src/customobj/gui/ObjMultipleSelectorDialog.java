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

package customobj.gui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import customobj.containers.FloatToObjectSimpleNode;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.SwingConstants;

public class ObjMultipleSelectorDialog<T> extends Dialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel jPanel = null;

	// private JButton jButton = null;
	private JLabel jLabel = null;

	private JTextField jTextField = null;

	private JScrollPane jScrollPane = null;

	private JTable jTable = null;

	private JButton jButton1 = null;

	private JButton jButton2 = null;

	private int selectedIndex;

	private Vector<T> objVector;

	private LocalTableModel tm;

	private LocalTableModel tmSelection;

	private static String sortingKey = "";

	// private boolean atLeastOneSelection = false;
	private Window owner;

	private Dialog questo;

	private String header;

	private boolean orderingFunctionAvailable;

	private JPanel jPanel1 = null;

	private JTable jTable1 = null;

	private JScrollPane jScrollPane1 = null;

	private JButton jButton = null;

	private JButton jButton3 = null;

	/**
	 * @param owner
	 * @param modal
	 * @param objVector Vetcor di oggetti in cui il metodo toString dovrebbe restituire una stringa descrittiva dell'oggetto.
	 * @param initialSelection Vector di oggetti che verranno posti come primo insieme di oggetti selezionati e che verranno, quindi, rappresentati nella tabella di DX con il relativo metodo toString.
	 * @param header Etichetta che viene posta come titolo del dialog e della colonna delle stringhe descrittive degli oggetti selezionabili (notare che questo è impostabile anche con il relativo
	 *        metodo.
	 * @param orderingFunctionAvailable Se a true vengono visualizzati il campo ed il pulsante per la funzionalità di riordino delle stringhe.
	 */
	public ObjMultipleSelectorDialog(Window owner, ModalityType modal, Vector<T> objVector, Vector<T> initialSelection, String header, boolean orderingFunctionAvailable) {
		super(owner, modal);
		this.header = header;
		this.orderingFunctionAvailable = orderingFunctionAvailable;
		this.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				getJTextField().requestFocusInWindow();
			}

			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub

			}

		});
		if(objVector == null) {
			JOptionPane.showMessageDialog(this, "Nothing to be selected.");
			this.dispose();
		}
		this.objVector = objVector;
		this.owner = owner;
		tmSelection = new LocalTableModel();
		tmSelection.setColumnIdentifiers(new String[] { header });
		tmSelection.setRowCount(0);
		if(initialSelection != null) {
			for(int i = 0; i < initialSelection.size(); i++) {
				tmSelection.addRow(new Object[] { initialSelection.get(i) });
			}
		}
		initialize();
		questo = this;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.setSize(894, 548);
		tm = new LocalTableModel();
		tm.setColumnIdentifiers(new String[] { header });
		tm.setColumnCount(1);
		getJTable().setModel(tm);
		jTable.setDefaultRenderer(JComponent.class, new JComponentCellRenderer());
		// Riempimento tabella con tutti i nomi dei file presenti nel vettore
		// stringsVector.
		for(int i = 0; i < objVector.size(); i++) {
			Vector<T> tmpVector = new Vector<T>();
			tmpVector.add(objVector.get(i));
			tm.addRow(tmpVector);
		}
		// La sezione seguente serve per aggiungere alla tabella un
		// ListSelectionListener personalizzato che imposta l'indice
		// clickedIndex all'indice della riga clickata.
		ListSelectionModel rowSM = jTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) {
					return;
				}

				// La seguente pappardella serve ad ottenere il
				// ListSelectionModel della tabella in quanto da qui essa non
				// ??? visibile. Il LSM, poi, serve solo per sapere se l'indice
				// di selezione ??? cambiato (altrimenti bastava un
				// e.getFirstIndex).
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if(lsm.isSelectionEmpty()) {
					// jTextField.setText(null);
				}
				else {
					selectedIndex = lsm.getMinSelectionIndex();
					// atLeastOneSelection = true;
				}
			}
		});

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				dispose();
			}
		});
		// jTextField.setText(sortingKey);
		if(sortingKey.length() != 0) {
			resort();
		}
		if(owner != null) {
			Point ownerHiLeftPosition = owner.getLocation();
			Dimension ownerDimension = owner.getSize();
			Dimension size = this.getSize();
			Point newRelativeLocation = new Point(ownerDimension.width / 2 - size.width / 2 + ownerHiLeftPosition.x, ownerDimension.height / 2 - size.height / 2 + ownerHiLeftPosition.y);
			this.setLocation(newRelativeLocation);
		}
		this.add(getJScrollPane(), BorderLayout.WEST);
		this.add(getJPanel1(), BorderLayout.CENTER);
		this.add(getJPanel(), BorderLayout.SOUTH);
		this.add(getJScrollPane1(), BorderLayout.EAST);
		getJTable1().setModel(tmSelection);
		jTable.clearSelection();
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if(jPanel == null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 2;
			gridBagConstraints2.gridy = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.insets = new Insets(5, 3, 5, 2);
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(7, 5, 7, 2);
			gridBagConstraints.gridy = 0;
			gridBagConstraints.gridx = 0;
			jPanel = new JPanel();
			if(orderingFunctionAvailable) {
				jLabel = new JLabel();
				jLabel.setText("Sorting Key (type and press ENTER)");
				jPanel.setLayout(new GridBagLayout());
				jPanel.add(jLabel, gridBagConstraints);
				jPanel.add(getJTextField(), gridBagConstraints1);
				jPanel.add(getJButton(), gridBagConstraints2);
			}
		}
		return jPanel;
	}

	/*
	 * /** This method initializes jButton
	 * @return javax.swing.JButton
	 *//*
		 * private JButton getJButton() { if(jButton == null) { jButton = new JButton(); jButton.setText("Resort"); jButton.addActionListener(new java.awt.event.ActionListener() { public void
		 * actionPerformed(java.awt.event.ActionEvent e) { resort(); } }); } return jButton; }
		 */
	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField() {
		if(jTextField == null) {
			jTextField = new JTextField();
			jTextField.setPreferredSize(new java.awt.Dimension(150, 19));
			jTextField.addKeyListener(new KeyListener() {

				public void keyTyped(KeyEvent e) {
					if(e.getKeyChar() == '\n') {
						resort();
					}
				}

				public void keyPressed(KeyEvent e) {}

				public void keyReleased(KeyEvent e) {}
			});
			jTextField.setToolTipText("Insert an approximative (or exact) key and press return");
		}
		return jTextField;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if(jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setPreferredSize(new Dimension(400, 418));
			jScrollPane.setViewportView(getJTable());

		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable Questo metodo è public in quanto chi utilizza la classe può ottenere la tabella in modo da modificarne (in modo non invasivo) i parametri di visualizzazione.
	 * Chiaramente il buon comportamento dell'oggetto che chiama questo metodo è sottointeso.
	 * 
	 * @return javax.swing.JTable
	 */
	public JTable getJTable() {
		if(jTable == null) {
			jTable = new JTable();
			jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTable.setCellSelectionEnabled(true);
			jTable.addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2) {
						questo.dispose();
					}
				}

				public void mousePressed(MouseEvent e) {}

				public void mouseReleased(MouseEvent e) {}

				public void mouseEntered(MouseEvent e) {}

				public void mouseExited(MouseEvent e) {}

			});
		}
		return jTable;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if(jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("");
			jButton1.setName("jButton1");
			jButton1.setPreferredSize(new Dimension(72, 52));
			jButton1.setIcon(new ImageIcon("icons/rightArrow.gif"));
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int i;
					if(jTable.getSelectedRow() != -1) {
						for(i = 0; i < tmSelection.getRowCount(); i++) {
							if(tmSelection.getValueAt(i, 0).equals(tm.getValueAt(selectedIndex, 0))) break;
						}
						if(i == tmSelection.getRowCount()) tmSelection.addRow(new Object[] { tm.getValueAt(selectedIndex, 0) });
					}
					// dispose();
				}
			});
		}
		return jButton1;
	}

	/**
	 * This method initializes jButton2
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton2() {
		if(jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setIcon(new ImageIcon("icons/leftArrow.gif"));
			jButton2.setText("");
			jButton2.setPreferredSize(new Dimension(72, 52));
			jButton2.setName("jButton2");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// dispose();
					if(jTable1.getSelectedRow() != -1) tmSelection.removeRow(jTable1.getSelectedRow());
				}
			});
		}
		return jButton2;
	}

	/**
	 * Calcola un punteggio di coerenza (o somiglianza) tra 2 stringhe date in ingresso con metodo "Morge Elkan" modificato. (almeno per le stringhe lunghe).
	 */
	private float calcoloCoerenza(String a, String b, int mediumWordLength) {
		float punteggio = 0;
		int lsub;
		int Apos;
		int Bpos;
		int n;
		if(a.length() > b.length()) {
			String temp = a;
			a = b;
			b = temp;
		}
		byte A[] = a.getBytes();
		byte B[] = b.getBytes();

		float normalizer = 0;
		double weight;
		for(lsub = 1; lsub <= a.length(); lsub++) { // lsub e' la lunghezza
													// della sottostringa
			weight = (double) lsub / (lsub + mediumWordLength);
			//System.out.println(weight);
			// weight = (double)lsub;
			for(Apos = 0; Apos + lsub <= A.length; Apos++) {
				normalizer += weight / (a.length() - lsub + 1);
				for(Bpos = 0; Bpos + lsub <= B.length; Bpos++) {
					for(n = 0; n < lsub; n++) { // Trovo la corrispondenza
												// della sottostringa.
						if(A[Apos + n] != B[Bpos + n]) break;
					}
					if(n == lsub) { // Significa che ho trovato una
									// ricorrenza della sottostringa lsub.
						punteggio += weight / (a.length() - lsub + 1);
						break; // Questa se il punteggio deve essere
								// incrementato una sola volta all'esistenza
								// della sottostringa a nell'array B
								// indipendentemente dal numero di ricorrenze.
					}
				}
			}
		}
		punteggio /= normalizer;
		punteggio *= ((float) a.length() / (float) b.length());
		//System.out.println(b.length());
		return punteggio;
	}

	@SuppressWarnings("unchecked")
	private void resort() {
		// System.out.println("actionPerformed()"); // TODO Auto-generated Event
		// stub actionPerformed()
		FloatToObjectSimpleNode sortingTrans = new FloatToObjectSimpleNode();
		T str;
		float similarity;
		// long integerSimilarity;
		if(jTextField == null) return;
		if(jTextField.getText() != null && jTextField.getText().length() != 0) sortingKey = jTextField.getText();
		int averageWordsLength;
		String[] tmpStrArray;
		String tmpStr;
		int n, i;
		for(i = 0; i < objVector.size(); i++) {
			str = objVector.get(i);
			tmpStr = str.toString().toLowerCase();
			tmpStrArray = tmpStr.split(" +");
			averageWordsLength = 0;
			for(n = 0; n < tmpStrArray.length; n++) {
				averageWordsLength += tmpStrArray[n].length();
			}
			averageWordsLength /= tmpStrArray.length;
			similarity = calcoloCoerenza(tmpStr, sortingKey.toLowerCase(), averageWordsLength);
			// integerSimilarity = (int)(similarity*1000000000);//Moltiplico per
			// 1000000000 in quanto so a priori che questi valori non saranno
			// mai grandi ed e` necessario passare un int a
			// SimpleNode.appen(..).
			sortingTrans.append(similarity, str, false);
		}
		Vector<FloatToObjectSimpleNode> strVectorOrdered = new Vector<FloatToObjectSimpleNode>();
		tm.setRowCount(0);
		sortingTrans.fillVectorDisc(strVectorOrdered);
		for(i = 0; i < strVectorOrdered.size(); i++) {
			Vector<T> tmpRow = new Vector<T>();
			tmpRow.add((T) strVectorOrdered.get(i).getValue());
			tm.addRow(tmpRow);
		}
		jTable.changeSelection(0, 0, true, true);
	}

	/**
	 * Se è stata fatta almeno una selezione viene ritornato l'oggetto selezionato altrimenti null.
	 * 
	 * @return oggetto selezionato, null altrimenti.
	 */
	@SuppressWarnings("unchecked")
	public Vector<T> getObjSelection() {
		// if(!atLeastOneSelection) return null;
		if(tmSelection.getRowCount() == 0) return null;
		Vector<T> ret = new Vector<T>(tmSelection.getRowCount());
		for(int i = 0; i < tmSelection.getRowCount(); i++) {
			ret.add((T) tmSelection.getValueAt(i, 0));
		}
		return ret;
	}

	class LocalTableModel extends DefaultTableModel {
		LocalTableModel() {
			// super.setColumnIdentifiers(new String[]{header});
		}

		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int a, int b) {
			return false;
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for each cell. If we didn't implement this method, then the last column would contain text ("true"/"false"), rather than a
		 * check box.
		 */
		public Class<? extends Object> getColumnClass(int c) {
			if(getValueAt(0, c) == null) return new String().getClass();
			else return getValueAt(0, c).getClass();
		}
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if(jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new FlowLayout());
			jPanel1.setPreferredSize(new Dimension(50, 100));
			jPanel1.add(getJButton1(), null);
			jPanel1.add(getJButton2(), null);
			jPanel1.add(getJButton3(), null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jTable1
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getJTable1() {
		if(jTable1 == null) {
			jTable1 = new JTable();
		}
		return jTable1;
	}

	public void setColumnIdentifiers(String left, String rigth) {
		tm.setColumnIdentifiers(new String[] { left });
		tmSelection.setColumnIdentifiers(new String[] { rigth });
	}

	/**
	 * This method initializes jScrollPane1
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane1() {
		if(jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setPreferredSize(new Dimension(400, 418));
			jScrollPane1.setViewportView(getJTable1());
		}
		return jScrollPane1;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if(jButton == null) {
			jButton = new JButton();
			jButton.setText("OK");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					questo.dispose();
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jButton3
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton3() {
		if(jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setText("Clear");
			jButton3.setHorizontalTextPosition(SwingConstants.CENTER);
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					tmSelection.setRowCount(0);
				}
			});
		}
		return jButton3;
	}

} // @jve:decl-index=0:visual-constraint="12,58"

