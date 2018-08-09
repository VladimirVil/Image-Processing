package exercises;


import ij.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.awt.image.IndexColorModel;  

/** Graubildtest
  *
  * Klasse zum Testen von BV-Algorithmen in Graubildern
  *
  * @author Kai Saeger
  * @version 1.0
  */
public class LoesungUe6 implements PlugInFilter {

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
		ImagePlus corrected = NewImage.createByteImage ("eroded image", w, h, 1, NewImage.FILL_BLACK);
		ImageProcessor cor_ip = corrected.getProcessor();
		cor_ip.copyBits(ip,0,0,Blitter.COPY);
		//Pixel-Array des Eingabebildes
		byte[] pixelsin = (byte[])ip.getPixels();
		//Pixelarray des neuen Bildes
		byte[] pixels = (byte[])cor_ip.getPixels();
		/***********An dieser Stelle kann an den einzelnen Pixeln gearbeitet werden.*********/

		//Beispiel Lookup Table LUT
		byte[] colorMapRed = new byte[256];
		byte[] colorMapGreen = new byte[256];
		byte[] colorMapBlue = new byte[256];

		for (int i = 0; i < 128; i++) colorMapRed[i] = (byte) (255-2*i);
		for (int i = 128; i <= 255; i++) colorMapGreen[i] = (byte) (2*i-255);
		for (int i = 64; i < 128; i++) colorMapBlue[i] = (byte) (4*i-255); 
		for (int i = 128; i < 192; i++) colorMapBlue[i] = (byte) (767-4*i); 
		
		IndexColorModel cm = new IndexColorModel(8, 256, colorMapRed, colorMapGreen, colorMapBlue);
		cor_ip.setColorModel(cm);
		
		
		/*****************Ende**********************************************************/
		
		corrected.show();
		corrected.updateAndDraw();

	}
}
