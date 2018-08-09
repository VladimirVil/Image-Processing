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
public class LoesungUe4 implements PlugInFilter {

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
	
				
		erode(pixelsin, pixels, roi, w, h);
		delate(pixelsin, pixels2, roi, w, h);
		
		
		/*****************Ende**********************************************************/
		
		corrected.show();
		corrected.updateAndDraw();
		corrected2.show();
		corrected2.updateAndDraw();
	}

	private void erode(byte[] pixelsin, byte[]pixels, Rectangle roi, int w, int h){
		for (int i=roi.y+1; i<roi.y+roi.height-1; i++) {
			for (int j=roi.x+1; j<roi.x+roi.width-1; j++) {
				int pix1 = pixelsin[(i-1)*w+(j-1)];
				pix1 = (pix1&0xff);
				int pix2 = pixelsin[(i-1)*w+j];
				pix2 = (pix2&0xff);
				int pix3 = pixelsin[(i-1)*w+(j+1)];
				pix3 = (pix3&0xff);
				int pix4 = pixelsin[i*w+(j-1)];
				pix4 = (pix4&0xff);
				int pix5 = pixelsin[i*w+j];
				pix5 = (pix5&0xff);
				int pix6 = pixelsin[i*w+(j+1)];
				pix6 = (pix6&0xff);
				int pix7 = pixelsin[(i+1)*w+(j-1)];
				pix7 = (pix7&0xff);
				int pix8 = pixelsin[(i+1)*w+j];
				pix8 = (pix8&0xff);
				int pix9 = pixelsin[(i+1)*w+(j+1)];
				pix9 = (pix9&0xff);
				//erode
				if(pix5 == 0 && (pix1==255||pix2==255||pix3==255||pix4==255||pix6==255||pix7==255||pix8==255||pix9==255)) pixels[i*w+j] = (byte) 255;
			}
		}
		
	}
	private void delate(byte[] pixelsin, byte[]pixels2, Rectangle roi, int w, int h){
		for (int i=roi.y+1; i<roi.y+roi.height-1; i++) {
			for (int j=roi.x+1; j<roi.x+roi.width-1; j++) {
				int pix1 = pixelsin[(i-1)*w+(j-1)];
				pix1 = (pix1&0xff);
				int pix2 = pixelsin[(i-1)*w+j];
				pix2 = (pix2&0xff);
				int pix3 = pixelsin[(i-1)*w+(j+1)];
				pix3 = (pix3&0xff);
				int pix4 = pixelsin[i*w+(j-1)];
				pix4 = (pix4&0xff);
				int pix5 = pixelsin[i*w+j];
				pix5 = (pix5&0xff);
				int pix6 = pixelsin[i*w+(j+1)];
				pix6 = (pix6&0xff);
				int pix7 = pixelsin[(i+1)*w+(j-1)];
				pix7 = (pix7&0xff);
				int pix8 = pixelsin[(i+1)*w+j];
				pix8 = (pix8&0xff);
				int pix9 = pixelsin[(i+1)*w+(j+1)];
				pix9 = (pix9&0xff);
				//dilate
				if(pix5 == 255 && (pix1==0||pix2==0||pix3==0||pix4==0||pix6==0||pix7==0||pix8==0||pix9==0)) pixels2[i*w+j] = (byte) 0;
			}
		}
		
	}
}