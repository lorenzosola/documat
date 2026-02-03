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

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
/**
 * 
 * @author Lorenzo Sola
 * JTextField che permettel'inserimento di soli interi.
 * Permette di limitare la lunghezza della stringa, ed i limiti superiore ed inferiore del numero che si sta immettendo.
 * Puo` essere preso come punto di riferimento per l'implementazione di campi testo personalizzati nel modo piu` corretto e razionale
 * (vedere esempio nella documentazione di J2SE).
 */
@SuppressWarnings("serial")
public class SimpleIntegerField extends JTextField {
	static int maxDigitNumber = 10;
	static int maxValue = Integer.MAX_VALUE;
	static int minValue = Integer.MIN_VALUE;
	static int actualValue = 0;

	public SimpleIntegerField() {
		super();
	}
	
	/**
	 * @param maxDigitNumber il massimo numero di colonne ammesse(vedi JTextField).
	 */
	public SimpleIntegerField(int maxDigitNumber) {
		super(maxDigitNumber);
		SimpleIntegerField.maxDigitNumber = maxDigitNumber;
	}

	/**
	 * Con questo costruttore si specifica il valore iniziale del campo, in tal caso il valore di ritorno di getText non sar' mai null.
	 * @param maxDigitNumber
	 * @param initialValue
	 */
	public SimpleIntegerField(int maxDigitNumber, int initialValue) {
		this(maxDigitNumber);
		actualValue = initialValue;
		setText(Integer.toString(initialValue));
	}
	
	public void setMaxValue(int i) {
		maxValue = i;
		if(actualValue > maxValue) {
			actualValue = maxValue;
			setText(Integer.toString(maxValue));
		}
	}

	public void setMinValue(int i) {
		minValue = i;
		if(actualValue < minValue) {
			actualValue = minValue;
			setText(Integer.toString(minValue));
		}
	}

	protected javax.swing.text.Document createDefaultModel() {
		return new SimpleIntegerDocument();
	}
	
	public void setColumns(int nCols) {
		maxDigitNumber = nCols;
		super.setColumns(nCols);
	}

	@SuppressWarnings("serial")
	static class SimpleIntegerDocument extends PlainDocument {
		int i;
		static int tmpInt = 0, tmpInt1 = 0;
		StringBuffer strBuff = new StringBuffer();
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			if(str == null) {
				return;
			}
			tmpInt = getLength();
			tmpInt1 = str.length();
			if(tmpInt + tmpInt1 > maxDigitNumber) return;
			
			for(i = 0; i < tmpInt1; i++)
				if(!java.lang.Character.isDigit(str.charAt(i))) return;

			strBuff.setLength(0);
			strBuff.append(getText(0, tmpInt));
			strBuff.insert(offs, str);
			
			//Ora devo controllare se il numero e` convertibile in un int altrimenti non si trova la corrispondenza tra il campo stesso ed il tipo di dato che deve rappresentare (appunto un int).
			try {
				tmpInt = Integer.parseInt(strBuff.toString());
			}
			catch (Exception e) {
				return;
			}

			if(tmpInt > maxValue || tmpInt < minValue) return;
			
			actualValue = tmpInt;
			
			super.insertString(offs, new String(str), (javax.swing.text.AttributeSet) a);
		}
	}
	
	/**
	 * Se nessuna stringa e` stata immessa e nessuna impostazione dei limiti ha provocato la reimpostazione del campo il valore ritormato e` 0.
	 * Se il campo e` vuoto allora il valore ritormato e` quello rappresentato dall'ultimo valore non nullo presente nel campo.  
	 * @return
	 */
	public int getInt() {
		return actualValue;
	}
	
	public void setValue(int value) {
		setText(Integer.toString(actualValue));
	}
	
}
