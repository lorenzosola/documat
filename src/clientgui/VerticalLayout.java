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
 * Created on Dec 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package clientgui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 * @author lsola
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */

public class VerticalLayout implements LayoutManager {

	private int vgap = 0;

	public VerticalLayout() {

		this(1);

	}

	public VerticalLayout(int vgap) {

		this.vgap = vgap;

	}

	public void addLayoutComponent(String name, Component comp) {

	}

	public void removeLayoutComponent(Component comp) {

	}

	public Dimension preferredLayoutSize(Container parent) {

		int nComp = parent.getComponentCount();

		int precH = vgap;

		// parent.

		for(int i = 0; i < nComp; i++) {

			Component c = parent.getComponent(i);

			if(c.isVisible()) {

				// Dimension dim = new Dimension((int)parent.getSize().getWidth(),

				// (int)c.getPreferredSize().getHeight());

				// dim.setSize();

				// c.setSize(dim);

				c.setSize((int) parent.getSize().getWidth(),

				(int) c.getPreferredSize().getHeight());

				c.setLocation(c.getX(), precH);

				precH = precH + (int) c.getPreferredSize().getHeight() + vgap;

			}

		}

		Dimension newDim = new Dimension(parent.getWidth(), precH);

		// System.out.println(parent.getWidth() + "," + precH);

		parent.setSize(newDim);

		return newDim;

	}

	public Dimension minimumLayoutSize(Container parent) {

		return parent.getMinimumSize();

	}

	public void layoutContainer(Container parent) {

		int nComp = parent.getComponentCount();

		int precH = vgap;

		for(int i = 0; i < nComp; i++) {

			Component c = parent.getComponent(i);

			if(c.isVisible()) {

				c.setSize((int) parent.getSize().getWidth(),

				(int) c.getPreferredSize().getHeight());

				c.setLocation(c.getX(), precH);

				precH = precH + (int) c.getPreferredSize().getHeight() + vgap;

			}

		}

	}

}