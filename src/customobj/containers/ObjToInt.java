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

package customobj.containers;

import java.io.Serializable;

//Classe utilizzata per inserire coppie int,obj nei combo box. Dal momento che implementa il metodo toString() e` anche possibile compilare i BomboBox saggiungendo tali oggetti con il metodo addItem(Object anObject).
public class ObjToInt<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	T obj;
	int i;

	public ObjToInt(T obj, int i) {
		super();
		this.obj = obj;
		this.i = i;
	}

	public int toInt() {
		return i;
	}

	public T toObj() {
		return obj;
	}

	public String toString() {
		return obj.toString();
	}
}
