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

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import arcmanagement.GlobalCollector;

/**
 * @author lsola
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class AdminPanel extends JTabbedPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8539404718480679863L;
	GlobalCollector gc;

	public AdminPanel(GlobalCollector gc) {
		this.gc=gc;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		try {
			this.addTab("User Management", new UserManagement(gc));
			this.addTab("Utility", new AdminUtils(gc));
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Some problems occured during initialization:\n"+e.getLocalizedMessage());
		}
	}

  }
