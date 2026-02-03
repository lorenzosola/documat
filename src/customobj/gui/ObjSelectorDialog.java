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
import java.io.Serializable;
import java.util.Vector;
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

public class ObjSelectorDialog<T> extends Dialog implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
//	private JButton jButton = null;
	private JLabel jLabel = null;
	private JTextField jTextField = null;
	private JScrollPane jScrollPane = null;
	private JTable jTable = null;
	private JButton jButton1 = null;
	private JButton jButton2 = null;
	private int selectedIndex;
	private Vector<T> objVector;
	private LocalTableModel tm;
	private static String sortingKey = "";
	private boolean atLeastOneSelection = false;
	private Window owner;
	private Dialog questo;
	private String header;
	private boolean orderingFunctionAvailable;

	/**
	 * 
	 * @param owner
	 * @param modal
	 * @param objArray Array di oggetti in cui il metodo toString dovrebbe restituire una stringa descrittiva dell'oggetto.
	 * @param header Etichetta che viene posta come titolo per la colonna delle stringhe descrittive degli oggetti selezionabili.
	 * @param orderingFunctionAvailable Se a true vengono visualizzati il campo ed il pulsante per la funzionalità di riordino delle stringhe.
	 */
	public ObjSelectorDialog(Window owner, ModalityType modal, Vector<T> objVector, String header, boolean orderingFunctionAvailable) {
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
		this.setSize(569, 430);
		this.add(getJPanel(), java.awt.BorderLayout.SOUTH);
		this.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
		tm = new LocalTableModel();
		tm.setColumnCount(1);
		jTable.setModel(tm);
		jTable.setDefaultRenderer(JComponent.class, new JComponentCellRenderer());
		//Riempimento tabella con tutti i nomi dei file presenti nel vettore stringsVector.
		for(int i = 0; i < objVector.size(); i++) {
			Vector<T> tmpVector = new Vector<T>();
			tmpVector.add(objVector.get(i));
			tm.addRow(tmpVector);
		}
		if(tm.getRowCount() > 0) jTable.changeSelection(0, 0, false, false);
		// La sezione seguente serve per aggiungere alla tabella un ListSelectionListener personalizzato che imposta l'indice clickedIndex all'indice della riga clickata.
		ListSelectionModel rowSM = jTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) {
					return;
				}

				// La seguente pappardella serve ad ottenere il ListSelectionModel della tabella in quanto da qui essa non ??? visibile. Il LSM, poi, serve solo per sapere se l'indice di selezione ??? cambiato (altrimenti bastava un e.getFirstIndex).
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if(lsm.isSelectionEmpty()) {
					//jTextField.setText(null);
				}
				else {
					selectedIndex = lsm.getMinSelectionIndex();
					atLeastOneSelection = true;
				}
			}
		});
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				dispose();
			}
		});
		//jTextField.setText(sortingKey);
		if(sortingKey.length()!=0) {
			resort();
		}
		if(owner != null) {
			Point ownerHiLeftPosition = owner.getLocation();
			Dimension ownerDimension = owner.getSize();
			Dimension size = this.getSize();
			Point newRelativeLocation = new Point(ownerDimension.width/2 - size.width/2 + ownerHiLeftPosition.x,
				ownerDimension.height/2 - size.height/2 + ownerHiLeftPosition.y);
			this.setLocation(newRelativeLocation);
		}
		jTable.clearSelection();
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if(jPanel == null) {
			jPanel = new JPanel();
			if(orderingFunctionAvailable) {
				jLabel = new JLabel();
				jLabel.setText("Sorting Key (type and press ENTER)");
				jPanel.add(jLabel, null);
				jPanel.add(getJTextField(), null);
				//jPanel.add(getJButton(), null);
			}
			jPanel.add(getJButton1(), null);
			jPanel.add(getJButton2(), null);
		}
		return jPanel;
	}
/*
	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 *//*
	private JButton getJButton() {
		if(jButton == null) {
			jButton = new JButton();
			jButton.setText("Resort");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					resort();
				}
			});
		}
		return jButton;
	}
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
					if(e.getKeyChar()=='\n') {
						resort();
					}
				}

				public void keyPressed(KeyEvent e) {
				}

				public void keyReleased(KeyEvent e) {
				}
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
			jScrollPane.setViewportView(getJTable());
			
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable
	 * Questo metodo è public in quanto chi utilizza la classe può ottenere la tabella in modo da modificarne (in modo non invasivo) i parametri di visualizzazione.
	 * Chiaramente il buon comportamento dell'oggetto che chiama questo metodo è sottointesao. 
	 * @return javax.swing.JTable
	 */
	public JTable getJTable() {
		if(jTable == null) {
			jTable = new JTable();
			jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTable.setCellSelectionEnabled(true);
			jTable.addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()==2) {
						questo.dispose();
					}
				}

				public void mousePressed(MouseEvent e) {
				}

				public void mouseReleased(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}
				
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
			jButton1.setText("OK");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
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
			jButton2.setText("Cancel");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					jTable.clearSelection();
					atLeastOneSelection = false;
					dispose();
				}
			});
		}
		return jButton2;
	}

	
	/**
	* Calcola un punteggio di coerenza (o somiglianza) tra 2 stringhe date in ingresso con metodo "Morge Elkan" modificato.
	* (almeno per le stringhe lunghe).
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
		for(lsub = 1; lsub <= a.length(); lsub++) { //lsub e' la lunghezza della sottostringa
			weight = (double)lsub / (lsub + mediumWordLength);
			//System.out.println(weight);
			//weight = (double)lsub;
			for(Apos = 0; Apos + lsub <= A.length; Apos++) {
				normalizer += weight/(a.length() - lsub +1);
				for(Bpos = 0; Bpos + lsub <= B.length; Bpos++) {
					for(n = 0; n < lsub; n++) { //Trovo la corrispondenza della sottostringa.
						if(A[Apos + n] != B[Bpos + n]) break;
					}
					if(n == lsub) { //Significa che ho trovato una ricorrenza della sottostringa  lsub.
						punteggio += weight/(a.length() - lsub +1);
						break; //Questa se il punteggio deve essere incrementato una sola volta all'esistenza della sottostringa a nell'array B indipendentemente dal numero di ricorrenze.
					}
				}
			}
		}
		punteggio /= normalizer;
		punteggio *= ((float)a.length() / (float)b.length());
		//System.out.println(b.length());
		return punteggio;
	}

	
	@SuppressWarnings("unchecked")
	private void resort() {
		//System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
		FloatToObjectSimpleNode sortingTrans = new FloatToObjectSimpleNode(); 
		T str;
		float similarity;
		//long integerSimilarity;
		if(jTextField.getText() != null && jTextField.getText().length() != 0)
			sortingKey=jTextField.getText();
		int averageWordsLength;
		String[] tmpStrArray;
		String tmpStr;
		int n,i;
		for(i=0;i<objVector.size();i++) {
			str=objVector.get(i);
			tmpStr = str.toString().toLowerCase();
			tmpStrArray = tmpStr.split(" +");
			averageWordsLength = 0;
			for(n=0; n<tmpStrArray.length; n++) {
				averageWordsLength += tmpStrArray[n].length();
			}
			averageWordsLength /= tmpStrArray.length;
			similarity = calcoloCoerenza(tmpStr, sortingKey.toLowerCase(), averageWordsLength);
			//integerSimilarity = (int)(similarity*1000000000);//Moltiplico per 1000000000 in quanto so a priori che questi valori non saranno mai grandi ed e` necessario passare un int a SimpleNode.appen(..). 
 			sortingTrans.append(similarity, str, false);
		}
		Vector<FloatToObjectSimpleNode> strVectorOrdered = new Vector<FloatToObjectSimpleNode>();
		tm.setRowCount(0);
		sortingTrans.fillVectorDisc(strVectorOrdered);
		for(i=0; i<strVectorOrdered.size(); i++) {
			Vector<T> tmpRow = new Vector<T>();
			tmpRow.add((T)strVectorOrdered.get(i).getValue());
			tm.addRow(tmpRow);
		}
		jTable.changeSelection(0,0,true,true);
	}
	
	/**
	 * Se è stata fatta almeno una selezione viene ritornato l'oggetto selezionato altrimenti null.
	 * @return oggetto selezionato, null altrimenti.
	 */
	@SuppressWarnings("unchecked")
	public T getSelectedObj() {
		if(!atLeastOneSelection || jTable.getSelectedRow() == -1) return null;
		return (T)tm.getValueAt(selectedIndex, 0);
	}

	class LocalTableModel extends DefaultTableModel {
		LocalTableModel() {
			super.setColumnIdentifiers(new String[]{header});
		}
		
		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int a, int b) {
			return false;
		}

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
		public Class<? extends Object> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
	}
	
	public void updateSelectionListVector(Vector<T> objVector) {
		if(objVector == null) {
			JOptionPane.showMessageDialog(this, "Nothing to be selected.");
		}
		this.objVector = objVector;

		//Riempimento tabella con tutti i nomi presenti nel vettore objVector.
		tm.setRowCount(0);
		for(int i = 0; i < objVector.size(); i++) {
			Vector<T> tmpVector = new Vector<T>();
			tmpVector.add(objVector.get(i));
			tm.addRow(tmpVector);
		}
	}

} // @jve:decl-index=0:visual-constraint="10,10"



