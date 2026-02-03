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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.Vector;
import java.awt.geom.Ellipse2D;

import customobj.containers.IntegerToObjectSimpleNode;
import customobj.containers.ObjArray;

public class XYPlot extends Component {
	private static final long serialVersionUID = 3023352774452295844L;

	// private int maxCharHeight = 15;
	// private int minFontSize = 6;
	private Color bg = new Color(50, 60, 120);
	private Color fg = Color.white;
	// private Color red = Color.red;
	// private Color white = Color.white;
	// Vari tipi di linea utilizzati.
	private BasicStroke stroke = new BasicStroke(1.0f);// Linea generica.
	private BasicStroke firstPlaneStroke = new BasicStroke(2.0f);// Linea utilizzata per la shape posta in risalto.
	// private float dash1[] = { 5.0f };// Lunghezza dei trattini (e degli spazi) con viene disegnata la linea di tipo "dashed".
	// private BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);// Linea sottile tratteggiata.
	// private BasicStroke wideStroke = new BasicStroke(8.0f);// Linea spessa.
	private Graphics2D g2;// Viene aggiornata solo alla chiamata della paint....
	private int maxX, oldMaxX;// Valore della coordinata X dell'angolo in basso a destra del riquadro di output del componente. Viene aggiornata solo alla chiamata della paint....
	private int maxY, oldMaxY;// Valore della coordinata Y dell'angolo in basso a destra del riquadro di output del componente. Viene aggiornata solo alla chiamata della paint.....
	private int rectX0 = 0;// X0 per il sistema di coordinate (viene aggiornato successivamente).
	private int rectY0 = 0;// Y0 per il sistema di coordinate.
	private int rectWidth;// Corrisponde alla larghezza del sistema di coordinate. Viene aggiornata alla chiamata della paint in base alla dimensione del frame ed utilizzata anche nella clear(.) ma senza aggiornarla...
	private int rectHeight;// Corrisponde all'altezza del sistema di coordinate. Viene aggiornata alla chiamata della paint in base alla dimensione del frame ed utilizzata anche nella clear(.) ma senza aggiornarla...
	private int plotX0 = rectX0;// vienne impostato dalla setMaxMinXYValues in modo da far apparire il grafico entro un riquadro piu` piccolo rispetto ai limiti del sistema di coordinate per i valori di minX e maxX impostati.
	private int plotY0 = rectY0;// vienne impostato dalla setMaxMinXYValues in modo da far apparire il grafico entro un riquadro piu` piccolo rispetto ai limiti del sistema di coordinate per i valori di minY e maxY impostati.
	private String xLabel;
	private String yLabel;
	private String title;  //  @jve:decl-index=0:
	private float maxXValue = 0, maxYValue = 0, minXValue = 0, minYValue = 0;// Valori limite per i dati da rappresentare nel sistema di coordinate.
	private float xscale, yscale;// Aggiornate in base ai valori limite dei dati da rappresentare nel sistema di coordinate ed alle dinensioni del frame ad ogni chiamata della paint(g). Servono per trasformare opportunamente i valori dei dati in coordinate sullo schermo.
	// private Shape sh;// Variabile transitoria per referenziare di volta in volta le Shape disegnate.
	private IntegerToObjectSimpleNode shapes = new IntegerToObjectSimpleNode();// Qui pongo tutte le shape da far comparire sul grafico.  //  @jve:decl-index=0:
	private Vector<IntegerToObjectSimpleNode> shapesVector = new Vector<IntegerToObjectSimpleNode>();  //  @jve:decl-index=0:

	private int stringXLabelY = 0;
	private int stringYLabelY;
	private Font graphicsFont;
	private float rateOfSpaceUsed = 0.9f;// Quantita` di spazio utilizzata dal grafico relativamente al riquadro di output.
	private boolean maxMinLinesAuto;
	private FontMetrics fontMetrics;
	private long firstPlaneShape = Integer.MAX_VALUE;
	private Composite originalComposite;
	private ExShape tmpShape;
	private IntegerToObjectSimpleNode tmpNode;

	// private Point mousePositionOnClick;

	public XYPlot() {
		this(null, null, null);
	}

	public XYPlot(String xLabel, String yLabel, String title) {
		if(xLabel != null) this.xLabel = xLabel;
		else this.xLabel = "";
		if(yLabel != null) this.yLabel = yLabel;
		else this.yLabel = "";
		if(title != null) this.title = title;
		else this.title = "";
		graphicsFont = new Font("Labels", Font.ITALIC, 20);
		/*
		 * this.addMouseListener(new MouseListener(){
		 * 
		 * public void mouseClicked(MouseEvent e) { mousePositionOnClick=getMousePosition(); //System.out.println("viewport X position = "+mousePositionOnClick.x+"\nviewport Y position = "+mousePositionOnClick.y); }
		 * 
		 * public void mousePressed(MouseEvent e) { // TODO Auto-generated method stub }
		 * 
		 * public void mouseReleased(MouseEvent e) { // TODO Auto-generated method stub }
		 * 
		 * public void mouseEntered(MouseEvent e) { // TODO Auto-generated method stub }
		 * 
		 * public void mouseExited(MouseEvent e) { // TODO Auto-generated method stub }
		 * 
		 * });
		 */
	}

	private FontMetrics pickFont(String longString, int xSpace) {
		// boolean fontFits = false;

		// Font font = g2.getFont();
		FontMetrics fontMetrics = g2.getFontMetrics();
		/*
		 * int size = graphicsFont.getSize(); String name = graphicsFont.getName(); int style = graphicsFont.getStyle();
		 * 
		 * while (!fontFits) { if ((fontMetrics.getHeight() <= maxCharHeight) && (fontMetrics.stringWidth(longString) <= xSpace)) { fontFits = true; } else { if (size <= minFontSize) { fontFits = true; } else { g2.setFont(graphicsFont = new Font(name, style, --size)); fontMetrics = g2.getFontMetrics(); } } }
		 */
		return fontMetrics;
	}

	public void paint(Graphics g) {
		g2 = (Graphics2D) g;
		g2.setFont(graphicsFont);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Dimension d = getSize();
		maxX = d.width;
		maxY = d.height;
		if(oldMaxX != maxX || oldMaxY != maxY) {
			if(fontMetrics == null) {
				fontMetrics = pickFont(xLabel, maxX);
			}
			rectX0 = fontMetrics.getMaxAscent() + fontMetrics.getDescent() + 5;
			rectY0 = fontMetrics.getMaxAscent() + fontMetrics.getDescent() + 5;

			// Imposto i parametri di partenza per i fonts, le dimensioni del riquadro di graficazione e le etichette degli assi.
			// Il font utilizzato per le etichette degli assi e` quello impostato nei componenti superiori tramite l'oggetto Grafics
			rectWidth = maxX - 2 * rectX0;// Larghezza del riquadro utilizzabile per l'output.
			stringXLabelY = maxY - 3 - fontMetrics.getDescent();// Altezza della stringa.
			rectHeight = stringXLabelY - rectY0 - fontMetrics.getMaxAscent();// Altezza del riquadro utilizzabile per l'output.
			stringYLabelY = (rectY0 + rectHeight) / 2;

			// clearPlot();
			oldMaxX = maxX;
			oldMaxY = maxY;
		}

		// Disegno delle etichette degli assi.
		g2.setColor(Color.RED);
		g2.drawString(xLabel, rectWidth / 2, stringXLabelY);
		g2.rotate(-Math.PI / 2, rectX0, stringYLabelY);
		g2.drawString(yLabel, rectX0, stringYLabelY - 5);
		g2.rotate(Math.PI / 2, rectX0, stringYLabelY);
		// Disegno del titolo sul lato alto del riquadro di output.
		g2.setColor(Color.BLACK);
		g2.drawString(title, rectWidth / 2 - fontMetrics.stringWidth(title) / 2, rectY0 - 3);

		// Disegno del riquadro di contorno.
		Color fg3D = Color.lightGray;
		g2.setPaint(fg3D);
		g2.draw3DRect(0, 0, d.width - 1, d.height - 1, true);
		g2.draw3DRect(3, 3, d.width - 7, d.height - 7, false);
		g2.setPaint(fg);

		setXYScale();

		/*
		 * draw Line2D.Double g2.setColor(Color.WHITE); g2.setStroke(dashed); sh = new Line2D.Double(x0, y0 + rectHeight - 1, x0 + rectWidth, y0); g2.draw(sh);
		 */

		// Disegno delle figure.
		g2.setBackground(bg);
		originalComposite = g2.getComposite();
		g2.clearRect(rectX0, rectY0, rectWidth, rectHeight);
		if(maxMinLinesAuto) {
			plotMaxMinLines();
		}
		shapesVector.clear();
		shapes.fillVectorAsc(shapesVector);
		// Disegno shapes non in primo piano.
		for(int i = 0; i < shapesVector.size(); i++) {
			tmpShape = (ExShape) shapesVector.get(i).getValue();
			if(tmpShape.ID != firstPlaneShape) {
				g2.setColor(tmpShape.color);
				g2.setStroke(tmpShape.stroke);
				if(tmpShape.composite != null) g2.setComposite(tmpShape.composite);
				else g2.setComposite(originalComposite);
				g2.draw(tmpShape.sh);
				if(tmpShape.filled == true) g2.fill(tmpShape.sh);
			}
		}
		// Disegno shapes in primo piano.
		tmpNode = shapes.get((int) firstPlaneShape);
		if(tmpNode != null) {
			tmpShape = (ExShape) tmpNode.getValue();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			g2.setColor(tmpShape.color);
			g2.setStroke(firstPlaneStroke);
			g2.draw(tmpShape.sh);
			if(tmpShape.filled == true) g2.fill(tmpShape.sh);
		}
	}

	/**
	 * Imposta la lo Stroke della figura in primo piano. Di default lo stroke e` il BasicStroke(2.0f).
	 * 
	 * @param firstPlaneStroke
	 */
	public void setFirstPlaneShapeStroke(BasicStroke firstPlaneStroke) {
		this.firstPlaneStroke = firstPlaneStroke;
	}

	/**
	 * Calcola ed imposta le costanti di scalatura da applicare ai dati graficati affinche vengano visualizzati nell finestra di graficazione dove i valori massimi e minimi per tutti gli assi sono stati impostati dalla funzione setMaxMinXYValues(float, float, float, float).
	 * 
	 */
	private void setXYScale() {
		if((maxXValue - minXValue) == 0) xscale = 1;
		else xscale = rectWidth / (maxXValue - minXValue);
		// else xscale = rectWidth / (maxXValue - minXValue) * rateOfSpaceUsed;//Qui` si riduce la scala in modo da non fare apparire il grafico ai limiti superiore ed inferiore del rettangolo visualizzato ma leggermente al suo interno.
		if((maxYValue - minYValue) == 0) yscale = 1;
		else yscale = (rectHeight / (maxYValue - minYValue)) * rateOfSpaceUsed;// Qui` si riduce la scala in modo da non fare apparire il grafico ai limiti superiore ed inferiore del rettangolo visualizzato ma leggermente al suo interno.

		// plotX0 = rectX0 + Math.round((rectWidth * (1-rateOfSpaceUsed)))/2;
		plotX0 = rectX0;
		plotY0 = rectY0 + Math.round((rectHeight * (1 - rateOfSpaceUsed))) / 2;
	}

	/**
	 * Rimuuove tutte le rappresentazioni di dati inseriti in precedenza.
	 * 
	 */
	public void clearPlot() {
		shapes.clearSubTree();
	}

	/**
	 * Rimuove la rappresentazione del solo grafico identificato con la stringa shapename al momento dell'inserimento dei dati.
	 * 
	 * @param shapename
	 * @return true se la shape era preesnte ed e` quindi stata rimossa false altrimenti.
	 */
	public boolean removeShape(long shapeID) {
		return shapes.remove(shapeID);
	}

	/**
	 * Rimuove ricorsivamente la shape e tutta la catena di shapes collegate ad essa.
	 * 
	 * @param shapeID
	 * @return
	 */
	public boolean removeShapeRecursively(long shapeID) {
		ExShape tmpShape = (ExShape) shapes.get(shapeID).getValue();
		if(tmpShape != null && tmpShape.hasLinkedShape()) {
			IntegerToObjectSimpleNode tmpNode = shapes.get(tmpShape.linkedShape.ID);
			if(tmpNode != null) removeShapeRecursively(((ExShape) tmpNode.getValue()).ID);
		}
		return shapes.remove(shapeID);
	}

	/**
	 * Rimuove tutte le shapes con indice >= a shapeID
	 * 
	 * @param shapeID
	 *            Indice di partenza
	 * @return
	 * 
	 */
	public void removeShapeAndUpper(long shapeID) {
		shapes.fillVectorAsc(shapesVector);
		shapes.clearSubTree();
		for(int i = 0; i < shapesVector.size(); i++) {
			if(shapesVector.get(i).getIndex() < shapeID) {
				shapes.append(i, shapesVector.get(i), true);
			}
		}
		return;
	}

	/**
	 * Ritorna l'ID per una figura appena successivo a quello della figura con ID piu` grande tra quelle inserite nel grafico. Se questo non viene utilizzato per l'inserimento di una figura con quell'ID o un ID maggiore la successiva chiamata restituisce lo stesso valore.
	 * 
	 * @return
	 */
	public float assignID() {
		if(shapes.getMax()==null) return 0;
		return shapes.getMax().getIndex() + 1;
	}

	/**
	 * Serve a definire il range di graficazione e di conseguenza la scalatura al momento della costruzione del grafico.
	 * 
	 * @param minX
	 *            minimo valore di x grafico entro la finestra di visualizzazione del grafico.
	 * @param maxX
	 *            massimo valore di x grafico entro la finestra di visualizzazione del grafico.
	 * @param minY
	 *            minimo valore di y grafico entro la finestra di visualizzazione del grafico.
	 * @param maxY
	 *            massimo valore di x grafico entro la finestra di visualizzazione del grafico..
	 */
	public void setMaxMinXYValues(float minX, float maxX, float minY, float maxY) {
		maxXValue = maxX;
		maxYValue = maxY;
		minXValue = minX;
		minYValue = minY;
		setXYScale();
	}

	public float getXRangeLow() {
		return minXValue;
	}

	public float getXRangeHi() {
		return maxXValue;
	}

	public float getYRangeLow() {
		return minYValue;
	}

	public float getYRangeHi() {
		return maxYValue;
	}

	public float getXRange() {
		return maxXValue - minXValue;
	}

	public float getYRange() {
		return maxYValue - minYValue;
	}

	public void fillShape(long ID) {
		ExShape es = (ExShape) shapes.get(ID).getValue();
		es.filled = true;
	}

	/**
	 * Imporsta il titolo che verra` visualizzato al di sopra della finestra di output del grafico. Se il parametro e` null (come di default) non viene visualizzaot nessun titolo.
	 * 
	 * @param title
	 */
	public void setPlotTitle(String title) {
		this.title = title;
	}

	/**
	 * Ritorna il valore di ascissa sul viewport dato il valore della ascissa relativa alla scala impostata.
	 * 
	 * @param x
	 *            Relative
	 * @return x Absolute
	 */
	public float getXAbs(float xRelative) {
		float ret;
		ret = plotX0 + xscale * xRelative;
		return ret;
	}

	/**
	 * Ritorna il valore della ascissa relativa alla scala impostata dato il valore di ascissa sul viewport.
	 * 
	 * @param viewportX
	 * @return
	 */
	public float getXRel(int viewportX) {
		return (viewportX - plotX0) / xscale;
	}

	/**
	 * Ritorna il valore di ordinata sul viewport dato il valore della ordinata relativa alla scala impostata.
	 * 
	 * @param yRelative
	 * @return
	 */
	public float getYAbs(float yRelative) {
		float ret;
		ret = (plotY0 + rectHeight * rateOfSpaceUsed) - yscale * (yRelative - minYValue);
		return ret;
	}

	/**
	 * Limita l'ascissa passata come argomento al riquadro del grafico nel caso in cui questa sia al di fuori di esso.
	 * 
	 * @param xAbs
	 * @return
	 */
	private float limitsXAbs(float xAbs) {
		if(xAbs < plotX0) xAbs = rectX0;
		else if(xAbs > plotX0 + rectWidth) xAbs = plotX0 + rectWidth;
		return xAbs;
	}

	/**
	 * Limita l'ordinata passata come argomento al riquadro del grafico nel caso in cui questa sia al di fuori di esso.
	 * 
	 * @param yAbs
	 * @return
	 */
	private float limitsYAbs(float yAbs) {
		if(yAbs < rectY0) yAbs = rectY0;
		else if(yAbs > rectY0 + rectHeight) yAbs = rectY0 + rectHeight;
		return yAbs;
	}

	/**
	 * Se il valore di y relativo al viewport non rientra nella finestra gel grafico ritorna true.
	 * 
	 * @param yAbs
	 * @return
	 */
	private boolean yOutOfPlot(float yAbs) {
		if(yAbs != limitsYAbs(yAbs)) return true;
		else return false;
	}

	/**
	 * Se il valore di x relativo al viewport non rientra nella finestra gel grafico ritorna true.
	 * 
	 * @param xAbs
	 * @return
	 */
	private boolean xOutOfPlot(float xAbs) {
		if(xAbs != limitsXAbs(xAbs)) return true;
		else return false;
	}

	/**
	 * Ritorna il valore della ordinata relativa alla scala impostata dato il valore di ordinata sul viewport.
	 * 
	 * @param viewportY
	 * @return
	 */
	public float getYRel(int viewportY) {
		return (((plotY0 + rectHeight * rateOfSpaceUsed) - viewportY) / yscale) + minYValue;
	}

	/**
	 * Imposta la shape che verra` disegnata in primo piano e leggermente risaltata.
	 * 
	 * @param ID
	 */
	void setFirstPlaneShape(long ID) {
		firstPlaneShape = ID;
	}

	public Color getPBGColor() {
		return bg;
	}

	public void plotMaxMinLines() {
		plotLine(minXValue, minYValue, maxXValue, minYValue, Long.MIN_VALUE, Color.GRAY);// Viene chiamato tutte le volte ma nella hasttable se il key e` lo stesso l
		plotLine(minXValue, maxYValue, maxXValue, maxYValue, Long.MIN_VALUE + 1, Color.GRAY);
	}

	public void plotLine(float x1, float y1, float x2, float y2, long shapeID, Color color) {
		float X1 = 0, Y1 = 0, X2 = 0, Y2 = 0;
		Line2D linea;
		X1 = getXAbs(x1);
		Y1 = getYAbs(y1);
		X2 = getXAbs(x2);
		Y2 = getYAbs(y2);
		linea = new Line2D.Float(X1, Y1, X2, Y2);
		shapes.append(shapeID, new ExShape(linea, color, stroke, shapeID), true);
	}

	/**
	 * Imposta il colore di background del grafico.
	 * 
	 * @param bgcol
	 */
	public void setPBGColor(Color bgcol) {
		this.bg = bgcol;
	}

	public void enableAutoPlottingMaxMinLines(boolean enabled) {
		this.maxMinLinesAuto = enabled;
	}

	/**
	 * Aggiunge un insieme di dati da graficare con il colore specificato. L'ID serve come identificativio di tale graficatione e non ha nessuna influenza su cio` che viene visualizzato. Tale parametro serve internamente e nel caso in cui, per esempio, si voglia utilizzare la funzione removeShape(Stirng shapeName) in cui, esso dovra` essere inserito come shapeName.
	 * 
	 * @param dataTable
	 * @param color
	 * @param ID
	 * @throws GraphicException
	 */
	/*
	 * public void plot2DData(ObjArray dataTable, Color color, long ID) throws GraphicException { if(dataTable.size() == 0) return; try { if(((float[]) dataTable.get(0)).length != 2) throw new GraphicException("XYPlot: la tabella contenente i dati da graficare non e` di dimensione nx2."); // clearPlot(); if(dataTable == null) return; int decimationFactor = 1; g2.setColor(color); float x, y; int
	 * tmpint = Math.round(plotY0 + rectHeight * rateOfSpaceUsed); float[] row = (float[]) dataTable.get(0);
	 * 
	 * x = plotX0 + xscale * row[0]; y = tmpint - yscale * (row[1] - minYValue); // Con linee GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, dataTable.size()); polyline.moveTo(x, y); if(dataTable.size() > 6000) decimationFactor = dataTable.size() / 6000;// Limito i punti da rappesentare perche` questo influisce sul tempo elaborazione ed e` inutile porre piu` punti di quelli
	 * rappresentabili sul monitor. Ovviamente questo puo` nascondere particolari come i picchi costituiti da pochi punti ma d'altro canto sarebbero imprecisi dato lo spessore della linea. for(int i = 0; i < dataTable.size(); i += decimationFactor) { // draw GeneralPath (polyline) row = (float[]) dataTable.get(i); x = plotX0 + xscale * row[0]; y = tmpint - yscale * (row[1] - minYValue);
	 * polyline.lineTo(x, y); } // polyline.lineTo(100,100); shapes.append(ID, new ExShape(polyline, color, stroke), true); } catch (Exception ex) { ex.printStackTrace(); } }
	 */
	public void plot2DData(ObjArray<float[]> dataTable, Color color, long ID) throws GraphicsException {
		if(dataTable.size() == 0) return;
		try {
			if(((float[]) dataTable.get(0)).length != 2) throw new GraphicsException("XYPlot: la tabella contenente i dati da graficare non e` di dimensione nx2.");
			// clearPlot();
			if(dataTable == null) return;
			int xInterval = 1, relativePos = 0;
			g2.setColor(color);
			float x, y;
			float max = -Float.MAX_VALUE, min = Float.MAX_VALUE;
			int tmpint = Math.round(plotY0 + rectHeight * rateOfSpaceUsed);// E` il valore della coordinata assoluta del riquadro di output del frame in cui si trovera` il valore minY.
			float[] row = (float[]) dataTable.get(0);
			float[] rowMin = null, rowMax = null;

			x = plotX0 + xscale * row[0];
			y = tmpint - yscale * (row[1] - minYValue);

			// Con linee
			GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, dataTable.size());
			polyline.moveTo(x, y);

			// Se il numero di dati supera rectWidth*2 (il numero di colonen dello schermo * 2) la cosa
			// migliore e` dividere il numero di dati per recWidth in modo da avere recrWigth intervalli
			// di dati rispetto alle ascisse e di ognuno di essi considerare solo i 2 punti
			// aventi il massimo ed il minimo valore delle ordinate nell ordine in cui vengono trovati.
			if(dataTable.size() > rectWidth * 2) {
				xInterval = (dataTable.size() / (rectWidth * 2)) - 1;
				boolean minPreceed = true;// Se il punto minimo precede il punto massimo questa variabile vale true nel momento dell'inseriemtno dei 2 punti trovati.
				for(int i = 0; i < dataTable.size(); i++) {
					row = (float[]) dataTable.get(i);
					if(min > row[1]) {
						min = row[1];
						rowMin = row;
						minPreceed = false;
					}
					if(max < row[1]) {
						max = row[1];
						rowMax = row;
						minPreceed = true;
					}
					if(relativePos++ >= xInterval) {
						relativePos = 0;
						max = -Float.MAX_VALUE;
						min = Float.MAX_VALUE;
						if(minPreceed) {
							x = plotX0 + xscale * rowMin[0];
							y = tmpint - yscale * (rowMin[1] - minYValue);
							polyline.lineTo(x, y);
							x = plotX0 + xscale * rowMax[0];
							y = tmpint - yscale * (rowMax[1] - minYValue);
							polyline.lineTo(x, y);
						}
						else {
							x = plotX0 + xscale * rowMax[0];
							y = tmpint - yscale * (rowMax[1] - minYValue);
							polyline.lineTo(x, y);
							x = plotX0 + xscale * rowMin[0];
							y = tmpint - yscale * (rowMin[1] - minYValue);
							polyline.lineTo(x, y);
						}
					}
				}
				if(relativePos < xInterval) {
					if(minPreceed) {
						x = plotX0 + xscale * rowMin[0];
						y = tmpint - yscale * (rowMin[1] - minYValue);
						polyline.lineTo(x, y);
						x = plotX0 + xscale * rowMax[0];
						y = tmpint - yscale * (rowMax[1] - minYValue);
						polyline.lineTo(x, y);
					}
					else {
						x = plotX0 + xscale * rowMax[0];
						y = tmpint - yscale * (rowMax[1] - minYValue);
						polyline.lineTo(x, y);
						x = plotX0 + xscale * rowMin[0];
						y = tmpint - yscale * (rowMin[1] - minYValue);
						polyline.lineTo(x, y);
					}
				}
			}
			else {// Il ciclo sopra funziona anche con tabelle di dimensione < a rectWidth ma mette sempre due punti (max e min) anche se in realta` si tra tta di 1 solo punto. Per non aggiungere controlli if(...) rallentando il ciclo se ne esegue 1 piu' semplice e + adatto.
				for(int i = 0; i < dataTable.size(); i++) {
					row = (float[]) dataTable.get(i);
					x = plotX0 + xscale * row[0];
					y = tmpint - yscale * (row[1] - minYValue);
					polyline.lineTo(x, y);
				}
			}

			shapes.append(ID, new ExShape(polyline, color, stroke, ID), true);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Tutti i valori dei parametri di coordinata e dimensione hanno come riferimento i range impostati con la funzione setMaxMinXYValues(....) e se questa non e` mai stata chiamata sono riferiti alle dimensioni in pixel del riquadro di output.
	 * 
	 * @param x
	 *            posizione X del punto in alto a destra (rispetto all'origine degli assi X,Y standard).
	 * @param y
	 *            posizione Y del punto in alto a destra (rispetto all'origine degli assi X,Y standard).
	 * @param width
	 * @param heigth
	 * @param color
	 * @param transparency
	 *            0 to 1
	 * @param ID
	 *            identifier of created sghape for successive modifications or removal.
	 */
	public void plotRectancge(float x, float y, float width, float heigth, Color color, float transparency, long ID) {
		Rectangle2D.Float rectangle;
		rectangle = new Rectangle2D.Float(getXAbs(x), getYAbs(y), width * xscale, heigth * yscale);
		ExShape es = new ExShape(rectangle, color, stroke, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency), ID);
		shapes.append(ID, es, true);
	}

	public void plotCenteredRectangle(float x, float y, float width, float heigth, Color color, float transparency, long ID) {
		Rectangle2D.Float rectangle;
		rectangle = new Rectangle2D.Float(getXAbs(x) - (width * xscale) / 2, getYAbs(y) - (heigth * yscale) / 2, width * xscale, heigth * yscale);
		ExShape es = new ExShape(rectangle, color, stroke, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency), ID);
		shapes.append(ID, es, true);
	}

	/**
	 * WML sta` per With Managed Limits
	 * Disegna un rettangolo sull'area ri plot date le misure x,y,x,y rispettivamente dei lati sinistro,superiore,destro,inferiore.
	 * La particolatita` e` che il rettangolo rientra sempre nel grafico senza mai debordare e se una misura risulta all'esterno della scala
	 * il lato corrispondente viene disegnato a zig-zag.
	 * @param left
	 * @param high
	 * @param rigth
	 * @param low
	 * @param color
	 * @param transparency
	 * @param ID
	 */
	public void plotBoundedRectangleWML(float left, float high, float rigth, float low, Color color, float transparency, long ID) {
		boolean leftNotIn = false;
		boolean rigthNotIn = false;
		boolean highNotIn = false;
		boolean lowNotIn = false;
		float le = 0;
		float hi = 0;
		float ri = 0;
		float lo = 0;
		le = getXAbs(left);
		if(xOutOfPlot(le)) {
			leftNotIn = true;
			le = limitsXAbs(le);
		}
		hi = getYAbs(high);
		if(yOutOfPlot(hi)) {
			highNotIn = true;
			hi = limitsYAbs(hi);
		}
		ri = getXAbs(rigth);
		if(xOutOfPlot(ri)) {
			rigthNotIn = true;
			ri = limitsXAbs(ri);
		}
		lo = getYAbs(low);
		if(yOutOfPlot(lo)) {
			lowNotIn = true;
			lo = limitsYAbs(lo);
		}

		//Si ricorda che le coordinate le,hi,ri,lo sono rispetto agli assi orientati come sul viewport (la Y va dall'alto al basso del display). 
		float yGap;
		float xGap;
		float tmpX=le, tmpY=lo;
		GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		polyline.moveTo(le, lo);
		yGap = Math.max(1, lo - hi) / 12;
		xGap = Math.max(1, (ri - le) / 12);
		if(leftNotIn) {
			xGap = Math.abs(xGap);
			yGap = Math.abs(yGap);
			tmpX = le;
			tmpY = lo;
			polyline.lineTo(tmpX, tmpY);
			while(tmpY > hi+1) {
				tmpX += xGap;
				tmpY -= yGap;
				xGap = -xGap;
				polyline.lineTo(tmpX, tmpY);
			}
		}
		else {
			polyline.lineTo(le, hi);
		}
		
		if(highNotIn) {
			xGap = Math.abs(xGap);
			yGap = Math.abs(yGap);
			tmpX = le;
			tmpY = hi;
			polyline.lineTo(tmpX, tmpY);
			while(tmpX < ri-1) {
				tmpX += xGap;
				tmpY += yGap;
				yGap = -yGap;
				polyline.lineTo(tmpX, tmpY);
			}
		}
		else {
			polyline.lineTo(ri, hi);
		}
		
		if(rigthNotIn) {
			xGap = Math.abs(xGap);
			yGap = Math.abs(yGap);
			tmpX = ri;
			tmpY = hi;
			polyline.lineTo(tmpX, tmpY);
			while(tmpY < lo-1) {
				tmpX -= xGap;
				tmpY += yGap;
				xGap = -xGap;
				polyline.lineTo(tmpX, tmpY);
			}
		}
		else {
			polyline.lineTo(ri, lo);
		}
		
		if(lowNotIn) {
			xGap = Math.abs(xGap);
			yGap = Math.abs(yGap);
			tmpX = ri;
			tmpY = lo;
			polyline.lineTo(tmpX, tmpY);
			while(tmpX > le+1) {
				polyline.lineTo(tmpX, tmpY);
				tmpX -= xGap;
				tmpY -= yGap;
				yGap = -yGap;
			}
		}
		else {
			polyline.lineTo(le, lo);
		}

		ExShape es = new ExShape(polyline, color, stroke, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency), ID);
		shapes.append(ID, es, true);
	}

	public void plotCenteredEllipse(float x, float y, float width, float heigth, Color color, float transparency, long ID) {
		Ellipse2D.Float ellipse;
		ellipse = new Ellipse2D.Float(getXAbs(x) - (width * xscale) / 2, getYAbs(y) - (heigth * yscale) / 2, width * xscale, heigth * yscale);
		ExShape es = new ExShape(ellipse, color, stroke, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency), ID);
		shapes.append(ID, es, true);
	}

	public void appShape(Shape shape, Color color, float transparency, long ID) {
		ExShape es = new ExShape(shape, color, stroke, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency), ID);
		shapes.append(ID, es, true);
	}

}

class ExShape {
	public Shape sh;
	public Color color;
	public BasicStroke stroke;
	public Composite composite = null;
	public boolean filled = false;
	public long ID;
	public ExShape linkedShape;// Nel caso in la questa shape sia collegata in senso generico ad un'altra shape (per esempio l'eliminazione di una comporti l'eliminazione della display area).

	public ExShape(Shape sh, Color color, BasicStroke stroke, Composite composite, long ID) {
		this(sh, color, stroke, ID);
		this.composite = composite;
		this.ID = ID;
	}

	public ExShape(Shape sh, Color color, BasicStroke stroke, long ID) {
		this.sh = sh;
		this.color = color;
		this.stroke = stroke;
		this.ID = ID;
		linkedShape = null;//Viene collegata a se stessa in quanto altrimenti il valore di linkedShape, se non esplicitamente impostato, potrebbe corrispondere erroneamente a quello di un'altra shape gia` presente.
	}

	/**
	 * Nel caso in la questa shape debba essere collegata in senso generico ad un'altra shape (per esempio l'eliminazione di una comporti l'eliminazione dell'altra).
	 * 
	 * @param es ExShape da collegare.
	 */
	public void linkTo(ExShape es) {
		linkedShape=es;
	}
	
	public boolean hasLinkedShape() {
		if(linkedShape != null) return true;
		else return false;
	}
}

// class OutOfScaleValue extends Exception {
// /**
// *
// */
// private static final long serialVersionUID = 1L;
//
// public OutOfScaleValue(String message) {
// super(message);
// }
// }
