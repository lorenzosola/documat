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
 * Created on May 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package clientgui;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import remotizing.GlobalStub;
import java.awt.Dimension;
import java.io.IOException;

/**
 * @author lsola
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class UserPanel extends JTabbedPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GlobalStub gc;
	public UserPanel(GlobalStub gc, int userID, String userName) {
		this.gc = gc;
		
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(new Dimension(800, 511));
		SearchesPanel searchesPanel = new SearchesPanel(gc);
		this.addTab("Searches", searchesPanel);
		this.addTab("Insertion", new FileInsertionPanel(gc));
		this.addTab("Conceptual Groups Management", new CncptGroupInsertingPanel(gc));
		try {
			this.addTab("Gestione Attività", new ActivityManagement(searchesPanel));
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
		}
	}

}  //  @jve:decl-index=0:visual-constraint="31,143"
