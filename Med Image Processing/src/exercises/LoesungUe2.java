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
public class LoesungUe2 implements PlugInFilter {

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
		ImagePlus corrected = NewImage.createByteImage ("Corrected image", w, h, 1, NewImage.FILL_BLACK);
		ImageProcessor cor_ip = corrected.getProcessor();
		cor_ip.copyBits(ip,0,0,Blitter.COPY);
		//Pixel-Array des Eingabebildes
		byte[] pixelsin = (byte[])ip.getPixels();
		//Pixelarray des neuen Bildes
		byte[] pixels = (byte[])cor_ip.getPixels();
		/***********An dieser Stelle kann an den einzelnen Pixeln gearbeitet werden.*********/
			
		
		
		//Beispiel Graustufen-Reduzierung:
		for (int i=roi.y; i<roi.y+roi.height; i++) {
			int offset =i*w; 
			for (int j=roi.x; j<roi.x+roi.width; j++) {
				int pos = offset+j;		
				int pix = pixelsin[pos];
				//Das Folgende ist auch so lösbar: if(pix < 0) pix += 256;
				pix = (pix&0x0000ff);
						
				if(pix < 43) pix = 0;
				else if (pix > 42 && pix < 128) pix = 85;
				else if (pix > 127 && pix < 213) pix = 170;
				else if (pix > 213) pix = 255;
				pixels[pos] = (byte) pix;
			}
		}
			
		/*
		//Beispiel Auflösungs-Reduzierung
		for (int i=roi.y+1; i<roi.y+roi.height; i+=2) {
			for (int j=roi.x+1; j<roi.x+roi.width; j+=2) {
				int pix1 = pixelsin[(i-1)*w+j-1];
				pix1 = (pix1&0x0000ff);
				int pix2 = pixelsin[i*w+j-1];
				pix2 = (pix2&0x0000ff);
				int pix3 = pixelsin[(i-1)*w+j];
				pix3 = (pix3&0x0000ff);
				int pix4 = pixelsin[i*w+j];
				pix4 = (pix4&0x0000ff);
				
				int newpix = (pix1+pix2+pix3+pix4) / 4;
				
				pixels[(i-1)*w+j-1] = (byte) newpix;
				pixels[i*w+j-1] = (byte) newpix;
				pixels[(i-1)*w+j] = (byte) newpix;
				pixels[i*w+j] = (byte) newpix;
			}
		}
		*/
		
		/*****************Ende**********************************************************/
		
		corrected.show();
		corrected.updateAndDraw();
	}
}