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
public class LoesungUe3 implements PlugInFilter {

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
		ImagePlus corrected2 = NewImage.createByteImage ("delated image 2", w, h, 1, NewImage.FILL_BLACK);
		ImageProcessor cor_ip2 = corrected2.getProcessor();
		cor_ip2.copyBits(ip,0,0,Blitter.COPY);
		//Pixel-Array des Eingabebildes
		byte[] pixelsin = (byte[])ip.getPixels();
		//Pixelarray des neuen Bildes
		byte[] pixels = (byte[])cor_ip.getPixels();
		byte[] pixels2 = (byte[])cor_ip2.getPixels();
		/***********An dieser Stelle kann an den einzelnen Pixeln gearbeitet werden.*********/
	
		//Beispiel Filter
		for (int i=roi.y+1; i<roi.y+roi.height-1; i++) {
			for (int j=roi.x+1; j<roi.x+roi.width-1; j++) {
				int pix1 = pixelsin[(i-1)*w+(j-1)];
				pix1 = (pix1&0x0000ff);
				int pix2 = pixelsin[(i-1)*w+j];
				pix2 = (pix2&0x0000ff);
				int pix3 = pixelsin[(i-1)*w+(j+1)];
				pix3 = (pix3&0x0000ff);
				int pix4 = pixelsin[i*w+(j-1)];
				pix4 = (pix4&0x0000ff);
				int pix5 = pixelsin[i*w+j];
				pix5 = (pix5&0x0000ff);
				int pix6 = pixelsin[i*w+(j+1)];
				pix6 = (pix6&0x0000ff);
				int pix7 = pixelsin[(i+1)*w+(j-1)];
				pix7 = (pix7&0x0000ff);
				int pix8 = pixelsin[(i+1)*w+j];
				pix8 = (pix8&0x0000ff);
				int pix9 = pixelsin[(i+1)*w+(j+1)];
				pix9 = (pix9&0x0000ff);
				//Laplace-Operator
				//int newpix = (-pix2 - pix4 + 5*pix5 -pix6 - pix8);
				
				//Sobel-x Operator
				int newpix = 127 + pix1+2*pix2+pix3-pix7-2*pix8-pix9;
				//Sobel-y Operator
				int newpix2 = 127 + pix1+2*pix4+pix7-pix3-2*pix6-pix9;
				
				if(newpix < 0) newpix = 0;
				else if (newpix > 255) newpix = 255;

				pixels[i*w+j] = (byte) newpix;

				if(newpix2 < 0) newpix2 = 0;
				else if (newpix2 > 255) newpix2 = 255;
				pixels2[i*w+j] = (byte) newpix2;

			}
		}

		/*****************Ende**********************************************************/
		
		corrected.show();
		corrected.updateAndDraw();
		corrected2.show();
		corrected2.updateAndDraw();
	}
}