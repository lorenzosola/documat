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

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;


import customobj.containers.FloatToObjectSimpleNode;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class ObjSelectorPanel<T> extends JPanel{
	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane = null;
	private JTable jTable = null;
	private int selectedIndex;
	private Vector<T> objVector;
	private LocalTableModel tm;
	private static String sortingKey = "";
	private boolean atLeastOneSelection = false;
	private String header;
	private Method invokedMethod;  //  @jve:decl-index=0:
	private Object invokedMethodOwner;  //  @jve:decl-index=0:
	private JPanel jPanel = null;
	private JLabel jLabel = null;
	private JTextField jTextField = null;
	private JButton jButton1 = null;


	public ObjSelectorPanel(Vector<T> objVector, String header) {
		this.header = header;
		if(objVector == null) {
			JOptionPane.showMessageDialog(this, "Nothing to be selected.");
		}
		this.objVector = objVector;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.fill = GridBagConstraints.BOTH;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.weightx = 1.0;
		gridBagConstraints2.weighty = 1.0;
		gridBagConstraints2.ipadx = 0;
		gridBagConstraints2.gridx = 0;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.gridwidth = 2;
		gridBagConstraints1.gridy = 2;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints.gridy = 0;
		this.setLayout(new GridBagLayout());
		this.setSize(465, 459);
		this.add(getJButton1(), gridBagConstraints);
		this.add(getJPanel(), gridBagConstraints1);
		this.add(getJScrollPane(), gridBagConstraints2);
		tm = new LocalTableModel();
		tm.setColumnCount(1);
		jTable.setModel(tm);
		
		//Riempimento tabella con tutti i nomi presenti nel vettore objVector.
		for(int i = 0; i < objVector.size(); i++) {
			Vector<T> tmpVector = new Vector<T>();
			tmpVector.add(objVector.get(i));
			tm.addRow(tmpVector);
		}

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
		
		jTextField.setText(sortingKey);
		if(sortingKey.length()!=0) {
			resort();
		}
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if(jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setPreferredSize(new Dimension(0, 0));
			jScrollPane.setViewportView(getJTable());
			
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getJTable() {
		if(jTable == null) {
			jTable = new JTable();
			jTable.addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()==2) {
						try {
							invokedMethod.invoke(invokedMethodOwner);
						}
						catch (IllegalArgumentException e1) {
							e1.printStackTrace();
						}
						catch (IllegalAccessException e1) {
							e1.printStackTrace();
						}
						catch (InvocationTargetException e1) {
							e1.printStackTrace();
							System.err.println(e1.getCause());
						}
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
		//System.out.println("actionPerformed()");
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
	
	@SuppressWarnings("unchecked")
	public T getSelectedObj() {
		if(!atLeastOneSelection) return null;
		return (T)tm.getValueAt(selectedIndex, 0);
	}
	
	class LocalTableModel extends DefaultTableModel {
		/**
		 * 
		 */
		
		LocalTableModel() {
			super.setColumnIdentifiers(new String[]{header});
		}
		
		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int a, int b) {
			return false;
		}
	}
	
	/**
	 * Serve per specificare il metodo viene invocato quando viene fatta la selezine di un oggeto. 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 *
	 */
	public void setInvokdMethod(Object invokedMethodOwner, Method invokedMethod) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		this.invokedMethod = invokedMethod;
		this.invokedMethodOwner = invokedMethodOwner;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if(jPanel == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(3);
			gridLayout.setColumns(1);
			jLabel = new JLabel();
			jLabel.setText("Sorting Key (type and press ENTER)");
			jLabel.setName("jLabel");
			jPanel = new JPanel();
			jPanel.setLayout(gridLayout);
			jPanel.setPreferredSize(new Dimension(459, 60));
			jPanel.add(jLabel, null);
			jPanel.add(getJTextField(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if(jTextField == null) {
			jTextField = new JTextField();
			jTextField.setPreferredSize(new Dimension(150, 19));
			jTextField.setName("jTextField");
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
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if(jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Select");
			jButton1.setName("jButton1");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						invokedMethod.invoke(invokedMethodOwner, (Object[])null);
					}
					catch (IllegalArgumentException e1) {
						e1.printStackTrace();
					}
					catch (IllegalAccessException e1) {
						e1.printStackTrace();
					}
					catch (InvocationTargetException e1) {
						e1.printStackTrace();
					}
				}
			});
		}
		return jButton1;
	}
	
	public void setSelectioButtonText(String str) {
		jButton1.setText(str);
	}
	
	public void setSelectioButtonIcon(Icon icon) {
		jButton1.setIcon(icon);
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
	
}  //  @jve:decl-index=0:visual-constraint="133,68"
