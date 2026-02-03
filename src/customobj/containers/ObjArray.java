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

import java.util.NoSuchElementException;

/**
 * An efficient (not dynamic) object's vector.
 * 
 * @author lsola
 *
 * @param <T> 
 */

public class ObjArray<T> {
	private Object[] buffer;
	private int maxIndex = -1;
	private int maxArraySize;

	/**
	 * Instantiate this object with an entire Object array of size maxArraySize.
	 * @param maxArraySize max size reachable by this Object's Vector (going over is not admitted).
	 */
	public ObjArray(int maxArraySize) {
		buffer = new Object[maxArraySize];
		this.maxArraySize = maxArraySize;
	}

	/**
	 * Returns the element at the specified position in this Vector.
	 * @param i
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 */
	@SuppressWarnings("unchecked") //Dico al compilatore di non segnalare un warning per il fatto che l'oggetto di ritorno della funzione potrebbe non essere del tipo T facendo scaturire un "castin exception".
	public T get(int i) throws ArrayIndexOutOfBoundsException {
		if(i > maxIndex || i < 0 || maxIndex == -1) throw new ArrayIndexOutOfBoundsException("ObjArray: indice in posizione non inizializzata o nessun elemento presente.");
		return (T)buffer[i];
	}

	public int size() {
		return maxIndex;
	}

	/**
	 * Appends the specified element to the end of this Vector.
	 * @param in
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public boolean add(T in) throws ArrayIndexOutOfBoundsException {
		if(maxIndex == -1) maxIndex = 0;
		if(maxIndex >= maxArraySize) throw new ArrayIndexOutOfBoundsException("ObjArray: array pieno, impossibile aggiungere altri elementi.");
		buffer[maxIndex++] = in;
		return true;
	}

	/**
	 * Returns the first component (the item at index 0) of this vector.
	 * @return the first element of Vector.
	 * @throws NoSuchElementException if this vector has no components.
	 */
	public T firstElement() throws NoSuchElementException{
		if(maxIndex == -1) throw new NoSuchElementException();
		return get(0);
	}
	
	public T lastElement() throws  NoSuchElementException {
		if(maxIndex == -1) throw new NoSuchElementException();
		return get(maxIndex-1);
	}
	
	/**
	 * Removes all of the elements from this Vector. 
	 * The Vector will be empty after this call returns.
	 *
	 */
	public void clear() {
		maxIndex = -1;
	}
}
