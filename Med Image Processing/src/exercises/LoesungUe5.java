package exercises;


import ij.*;
import ij.gui.*;
import java.awt.*;
import java.util.Vector;  
import ij.plugin.filter.PlugInFilter;
import ij.process.*;

/** Graubildtest
  *
  * Klasse zum Testen von BV-Algorithmen in Graubildern
  *
  * @author Kai Saeger
  * @version 1.0
  */
public class LoesungUe5 implements PlugInFilter {
	Vector<Polygon> polyvec;		
	/**Initialisierung in ImageJ */
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about"))
			{showAbout(); return DONE;}
				//Zugelassen nur für 8-Bit Graubilder
                return DOES_8G+NO_CHANGES;
	}	
	/**About Message zu diesem Plug-In. */
	void showAbout() {
		IJ.showMessage("Graubildtest",
			"Testprogramm"
		);
	}

	/**Ausführende Funktion
	 * @param ip Image Processor. Klasse in ImageJ, beinhaltet das Bild und 
	 * zugehörige Metadaten.
	 * 	 */
	public void run(ImageProcessor ip) {
		// get width, height and the region of interest
		int w = ip.getWidth();     
		int h = ip.getHeight();    
		Rectangle roi = ip.getRoi();

		// create a new image with the same size and copy the pixels of the original image
		ImagePlus corrected = NewImage.createByteImage ("corrected image", w, h, 1, NewImage.FILL_BLACK);
		ImageProcessor cor_ip = corrected.getProcessor();
		//cor_ip.copyBits(ip,0,0,Blitter.COPY);
		//Pixel-Array des Eingabebildes
		byte[] pixelsin = (byte[])ip.getPixels();
		//Pixelarray des neuen Bildes
		byte[] pixels = (byte[])cor_ip.getPixels();
		/***********An dieser Stelle kann an den einzelnen Pixeln gearbeitet werden.*********/
			
		contour(pixelsin, pixels, roi, w, h);
		
		/*****************Ende**********************************************************/
		
		corrected.show();
		corrected.updateAndDraw();
		
		
	}
	private void contour(byte[] pixelsin, byte[]pixels, Rectangle roi, int w, int h){
		polyvec = new Vector<Polygon>();
		for (int i=roi.y+1; i<roi.y+roi.height-1; i++) {
			for (int j=roi.x+1; j<roi.x+roi.width-1; j++) {
				if(this.checkcontains(j,i)) {
					//pixels[i*w+j] = (byte) 100; //Innere Pixel grau setzen
				}
				else{
				int pix1 = pixelsin[i*w+j];
				pix1 = (pix1&0x0000ff);
				if(pix1 == 0) {
						createObject(pixelsin, pixels, j, i, w);
					}
				}
			}
		}
	}
	
	/**createObject renders a new contour line with the transferred threshold.
	 * The calculation is based on the current mouse coordinates.
	 * @param thres The threshold for the rendering of the new contour line.	 */
	 private void createObject(byte[]pixelsin, byte[]pixels, int x, int y, int w)	
	{    
		int xcoo = x;		
		int ycoo = y;       
		int holdx = x;	
		int holdy = y;
		int pixvalue;
		int direction = 1;  
		int rightcount = 0;  
		Polygon polygon = new Polygon();
		
		do{
			switch(direction){
			case 1: ycoo -= 1; break; //up
			case 2: xcoo += 1; break; //right
			case 3:	ycoo += 1; break; //down
			case 4: xcoo -= 1; break; //left
			}
			pixvalue = (int) pixelsin[ycoo*w+xcoo];
			pixvalue = (pixvalue&0xff); //because of byte to int conversion.
			
			if(pixvalue == 0) {  //if ObjectPixel, not Background

				if(xcoo == holdx && ycoo == holdy); //Do not register same point twice
				else {
					pixels[ycoo*w+xcoo] = (byte) 255; //add object-point to polygon.
					holdx = xcoo;
					holdy = ycoo;
					polygon.addPoint(xcoo, ycoo); 
				}
				direction -= 1;   //go left
				rightcount = 0;
			}
			else{
				direction += 1; //go right
				rightcount += 1;
			}
			if(rightcount == 4) { //if right for the fourth time, change to left
				direction -= 2;
				rightcount = 0;
			}
			if(direction == 5) direction  = 1;
			if(direction == 0) direction = 4;
		}while (ycoo != y || xcoo != x);  //end, if registered point == startpoint
		this.polyvec.addElement(polygon);
	}
	 
	 /**Checks, if a point is inside one of the ChromosomeObjects.
	 * @param x The x image coordinate of the point to be examined.
	 * @param y the y image coordinate of the point to be examined.
	 * @return inside True, if the point lies inside an object, false otherwise.	 */
	public boolean checkcontains(int x, int y) //checks all Polygons
	{
		for (int i = 0; i < this.polyvec.size(); i++)
		{
			Polygon poly = (Polygon) this.polyvec.elementAt(i);
				if(poly.contains(x,y)) return true;
				
			for (int j=0; j<poly.npoints; j++) {
				if(x == poly.xpoints[j] && y == poly.ypoints[j]) return true;
			}
		}
		return false;
	}
}

