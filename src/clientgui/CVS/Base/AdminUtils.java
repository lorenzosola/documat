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
import java.awt.GridBagLayout;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;

import arcmanagement.ElabException;
import arcmanagement.GlobalCollector;
import arcmanagement.ValidationException;

import java.awt.GridBagConstraints;
import java.io.IOException;

public class AdminUtils extends JPanel {

	private static final long serialVersionUID = 1L;
	private JButton jButton = null;
	private GlobalCollector gc;
	private Component questo;

	/**
	 * This is the default constructor
	 */
	public AdminUtils(GlobalCollector gc) {
		super();
		initialize();
		this.gc = gc;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		this.setSize(300, 200);
		this.setLayout(new GridBagLayout());
		this.add(getJButton(), gridBagConstraints);
		questo = this;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if(jButton == null) {
			jButton = new JButton();
			jButton.setText("Reinitialyze Stop Words Tables");
			jButton.addActionListener(new java.awt.event.ActionListener() {

				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						JOptionPane.showMessageDialog(questo, "Inserted " + gc.initialyzeStopWordsLists(MainPanel.userName, MainPanel.password) + " words");
					}
					catch (ValidationException e1) {
						JOptionPane.showMessageDialog(questo, e1.getMessage());
					}
					catch (IOException e1) {
						JOptionPane.showMessageDialog(questo, e1.getMessage());
					}
					catch (ElabException e1) {
						JOptionPane.showMessageDialog(questo, e1.getMessage());
					}
				}
			});
		}
		return jButton;
	}

}
