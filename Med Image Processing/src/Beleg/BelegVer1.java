package Beleg;

import ij.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Arrays.*;

/**
 * Belegarbeit - Med. Bildverarbeitung - Aufgabe 2: Berechnung Anteil der
 * Knochen an der Gesamtfläche der Hand in Röntgenbildern
 *
 * Plugin für ImageJ, ver. 1.51J8
 *
 * @author Vladimir Vilenchik s0556191
 * @version 1.0 17.07.2018
 * 
 */

/*
 * Arbeitsverlauf: 
 * 1. Heiligkeit anpassung - alles in Hintergrund besetzt die
 * gleiche Heiligkeit - würde durch Nutzung von Thresholdberechnung Alg. per
 * Zeile erreicht.
 * 
 * 2. Glätten des Bildes - Gauss Filter 
 * 
 * 3. Reduzierung der Anzahl
 * der Farben von 256 zu 32 für Berechnung der lokalen minimum Werte 
 * 
 * 4. Berechnung der lokale min. Werte 
 * 
 * 5. Auswertung der Ergebnisse (gemäß die lokale min. Werte), und Darstellung der Anzahl in %
 * 
 * 
 */
public class BelegVer1 implements PlugInFilter {

	// number of colors Histogram will be reduced to
	final int COLORSNUMBER = 16;

	/** Initialisierung in ImageJ */
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
		// Zugelassen nur für 8-Bit Graubilder
		return DOES_8G + NO_CHANGES;
	}

	/** About Message zu diesem Plug-In. */
	void showAbout() {
		IJ.showMessage("Med. Bildverarbeitung", "Belegarbeit");
	}

	/**
	 * 
	 * 
	 * @param ip
	 *            Image Processor. Klasse in ImageJ, beinhaltet das Bild und
	 *            zugehörige Metadaten.
	 */
	public void run(ImageProcessor ip) {
		IJ.log("run()");
		// get width, height and the region of interest
		int w = ip.getWidth();
		int h = ip.getHeight();
		Rectangle roi = ip.getRoi();

		// create a new image with the same size and copy the pixels of the original
		// image
		ImagePlus corrected = NewImage.createByteImage("Background correction", w, h, 1, NewImage.FILL_BLACK);
		ImageProcessor cor_ip = corrected.getProcessor();
		cor_ip.copyBits(ip, 0, 0, Blitter.COPY);
		ImagePlus corrected2 = NewImage.createByteImage("Filter(Gauss)", w, h, 1, NewImage.FILL_BLACK);
		ImageProcessor cor_ip2 = corrected2.getProcessor();
		cor_ip2.copyBits(ip, 0, 0, Blitter.COPY);
		ImagePlus corrected3 = NewImage.createByteImage("Hand with bones", w, h, 1, NewImage.FILL_BLACK);
		ImageProcessor cor_ip3 = corrected3.getProcessor();
		cor_ip3.copyBits(ip, 0, 0, Blitter.COPY);
		ImagePlus corrected4 = NewImage.createByteImage("Bones ", w, h, 1, NewImage.FILL_BLACK);
		ImageProcessor cor_ip4 = corrected4.getProcessor();
		cor_ip4.copyBits(ip, 0, 0, Blitter.COPY);
		// Pixel-Array des Eingabebildes
		byte[] pixelsin = (byte[]) ip.getPixels();
		// Pixelarray des neuen Bildes
		byte[] pixels = (byte[]) cor_ip.getPixels();
		byte[] pixels2 = (byte[]) cor_ip2.getPixels();
		byte[] handBones = (byte[]) cor_ip3.getPixels();
		byte[] bones = (byte[]) cor_ip4.getPixels();
		/***********
		 * An dieser Stelle kann an den einzelnen Pixeln gearbeitet werden.
		 *********/

		// ******* Thresholding per line *********
		// an Array of h(heigh) size is created. Each element store the value for that
		// line

		int[] thresholdPerLine = new int[h];
		int t0Experemental = 0; // The T value to start the thresholding ALgorithm. Will be updated later
		// Array lists that represent the areas in the Thresholding algorithm
		ArrayList<Integer> g0, g1;
		g0 = new ArrayList<Integer>();
		g1 = new ArrayList<Integer>();
		for (int i = 0; i < h; i++) {
			// create temp ROI per line , starts everytime from new line (h), and has always
			// the same width (w)
			Rectangle roiPerLine = new Rectangle(0, i, w, 1);
			t0Experemental = 127;
			thresholdPerLine[i] = threesholdCalc(pixelsin, t0Experemental, roiPerLine);
			// IJ.log("Threshold Value per line for line " + i + " is " +
			// thresholdPerLine[i]);

		}
		int[] thresholdPerLineNormalised = new int[thresholdPerLine.length];
		thresholdPerLineNormalised = normaliseUpdated(thresholdPerLine);
		// making the hinterground having similar value
		pixels = hintergrundSmoothering(pixelsin, pixels, roi, thresholdPerLine);
		// pixels = hintergrundSmoothering(pixelsin, pixels, roi, thresholdPerLine);
		tiefPassFilter(pixels, pixels2, roi);

		int nHistogramColors = COLORSNUMBER;
		// Reducing to just 32 colors (B&W)
		byte[] reducedHistogramColors = new byte[pixelsin.length];
		// Methode to actually implement the reduction of colors
		// reduceColors(pixelsin, reducedHistogramColors, nHistogramColors, roi, w);
		reduceColors(pixels2, reducedHistogramColors, nHistogramColors, roi, w);

		// histogram
		int[] histogram = new int[256];
		// histogram initialisation
		for (int k = 0; k < 256; k++)
			histogram[k] = 0;

		// Building a histogram
		for (int i = roi.y + 1; i < roi.y + roi.height - 1; i++) {
			for (int j = roi.x + 1; j < roi.x + roi.width - 1; j++) {
				int currentPixel = /* pixelsin */reducedHistogramColors[i * w + j];
				currentPixel = (currentPixel & 0x0000ff);
				histogram[currentPixel] += 1;
			}
		}

		// Build a more compact histogram with just nHistogramColors
		// To reconstruct the original color, multiply with compactFactor
		// compactHistogram - histogram without the zero values, build from n colors
		int[] compactHistogram = new int[nHistogramColors];
		int compactFactor = 256 / nHistogramColors; //
		for (int i = 0; i < histogram.length; i += compactFactor) {
			compactHistogram[i / compactFactor] = histogram[i];
		}

		IJ.log("COMPACT HISTOGRAM:\n");
		for (int x : compactHistogram) {
			IJ.log("x=" + x);
		}

		// here we search for minimum in the gestaucht(compressed) histogram, and these
		// are
		// approximately the minimums that we need
		IJ.log("MINIMA:\n");
		int firstMinimum = 0;
		boolean firstMinimumFound = false;
		int lastMinimum = 0;
		// needed to find the first and last minimum for analyse calculation
		int veryFirstMinimum = 0;
		int veryLastMinimum = 0;
		int beforeLastMinimu = 0;

		boolean[] minimaBoolean = localMinimumSearch(compactHistogram);
		for (int i = 0; i < minimaBoolean.length; i++) {
			IJ.log("b=" + minimaBoolean[i]);
			if (minimaBoolean[i] == true) {
				int color = i * compactFactor;
				IJ.log("minimum found @ " + color);
				if (veryFirstMinimum == 0)
					veryFirstMinimum = color;
				veryLastMinimum = color;
				if (!firstMinimumFound) {
					firstMinimumFound = true;
					firstMinimum = color;
				}
				beforeLastMinimu = lastMinimum;
				lastMinimum = color;
			}
		}

		/*for (boolean x : minimaBoolean) {
			IJ.log("Representaion of minima Array. Line : " + x + " value " + minimaBoolean);
		}  */

		calculationOfPixels(pixels2, handBones, bones, minimaBoolean, veryFirstMinimum, veryLastMinimum,
				beforeLastMinimu);
		/*****************
		 * Ende
		 **********************************************************/

		corrected.show();
		corrected.updateAndDraw();
		corrected2.show();
		corrected2.updateAndDraw();
		corrected3.show();
		corrected3.updateAndDraw();
		corrected4.show();
		corrected4.updateAndDraw();
	}

	// methode to calculate threeshold value. Receives the picture, and the start
	// point (t0)
	// Iterative Schwellenwertberechnung, Segmentierung Folien
	// public int threesholdCalc(byte[] currentPic, int t0, Rectangle roi,
	// ArrayList<Integer> g0, ArrayList<Integer> g1) {
	public int threesholdCalc(byte[] currentPic, int t0, Rectangle roi) {
		// height - roi.height; width - roi.width;
		// future values of zones to compare, at the beginning have the value of t0
		int ti, tiNew;
		ti = 0;
		tiNew = 0;
		ti = t0;
		tiNew = t0;
		int tempT = t0;
		do {
			ti = tempT;
			int[] avGreay = segmentisePictureSimple(currentPic, ti, roi);
			tempT = tiNew;
			tiNew = newT(avGreay);
		} while ((isDone(5, ti, tiNew) != true)); // 5 is the criterium value
		return tiNew;
	}

	// segments in two zones based on the value of T
	// would be eliminated after segmentisePictureSimple is fully functional
	public int[] segmentisePictureSimple(byte[] currentPic, int ti, Rectangle roi) {
		int sumg0 = 0;
		int amountg0 = 0;
		int sumg1 = 0;
		int amountg1 = 0;
		int width = (int) roi.getWidth();

		for (int i = roi.y; i < roi.y + roi.height; i++) {
			int offset = i * width;
			for (int j = roi.x; j < roi.x + roi.width; j++) {
				int pos = offset + j;
				int pix = currentPic[pos];
				pix = (pix & 0x0000ff);

				if (pix <= ti) {
					sumg0 += pix;
					amountg0++;
				} else if (pix > ti) {
					sumg0 += pix;
					amountg0++;
				}

			}
		}

		// important - prevent division by 0
		int averageg0 = 0;
		if (amountg0 != 0) {
			averageg0 = sumg0 / amountg0;
		} else if (amountg0 == 0) {
			averageg0 = 0;
		}
		int averageg1 = 0;
		if (amountg1 != 0) {
			averageg1 = sumg1 / amountg1;
		} else if (amountg1 == 0) {
			averageg1 = 0;
		}
		int[] greyAverages = new int[2];
		greyAverages[0] = averageg0;
		greyAverages[1] = averageg1;
		return greyAverages;

	}

	// calculates the T value for the next step
	public int newT(int[] mValues) {
		return ((mValues[0] + mValues[1]) / 2);
	}

	// return true, if the criterium is met
	// tiNew is t(i+1), ti is t(i)
	public boolean isDone(int criterium, int ti, int tiNew) {
		if (((tiNew - ti) < criterium) && ((tiNew - ti) * (-1) < criterium)) // added 10.07 - absolut value!
			return true;
		else
			return false;
	}

	public boolean[] localMinimumSearch(int[] histogram) {
		// array to contain if a point is local minimum, maximum or neutral
		// 2 for maximum, 1 for minimum, 0 for neutral
		int[] histogramMixMax = new int[histogram.length]; // keeps the minimum and maximum of histogram
		// Array to keep the differences between the current object and next object
		int[] hisDifferences = new int[histogram.length];
		int nMinima = 0;
		// Array that keeps minimum
		boolean[] minimum = new boolean[histogram.length];
		// Array that keeps the maximum
		// l
		boolean[] maximum = new boolean[histogram.length]; // added 10.07.2018 13:40
		for (int k = 0; k < histogram.length - 2; k++) {
			hisDifferences[k] = histogram[k + 1] - histogram[k];
		}
		for (int j = 1; j < histogram.length - 1; j++) {
			/*
			 * Consider an array of first differences d[i] = a[i] - a[i-1].
			 * 
			 * If d[i] is positive, then a increased over the last step and if d[i] is
			 * negative then a decreased. So, a change in sign of d from positive to
			 * negative indicates a was increasing, now decreasing, a local max. Similarly,
			 * negative to positive indicates a local min.
			 */
			if ((hisDifferences[j - 1] < 0) && (hisDifferences[j] >= 0)) {
				minimum[j] = true;
				nMinima++;
			}
			if ((hisDifferences[j - 1] <= 0) && (hisDifferences[j] > 0)) {
				minimum[j] = true;
				nMinima++;
			} else if ((hisDifferences[j - 1] > 0) && (hisDifferences[j] <= 0))
				maximum[j] = true;
			else if ((hisDifferences[j - 1] >= 0) && (hisDifferences[j] < 0))
				maximum[j] = true;

		}
		return minimum;
	}

	// n: # colors
	void reduceColors(byte[] pixelsIn, byte[] pixels, int n, Rectangle roi, int w) {
		byte delta = (byte) (256 / (byte) n);
		IJ.log("delta=" + delta);
		for (int i = roi.y; i < roi.y + roi.height; i++) {
			int offset = i * w;
			for (int j = roi.x; j < roi.x + roi.width; j++) {
				int pos = offset + j;
				int pix = pixelsIn[pos];
				pix = (pix & 0x0000ff);

				pixels[pos] = (byte) ((byte) (pix / delta) * delta);
			}
		}
		IJ.log("pixels=" + pixels);

	}

	// normalisation of the threshold per line values
	// not used, instead normaliseUpdated() is used
	int[] normalise(int[] thresholdArrayToNormalise) {
		int[] normalised = new int[thresholdArrayToNormalise.length];
		// value to be normalised to
		int normValue = 1;
		int average = 0;
		int sumOfLineValues = 0;

		for (int i = 0; i < thresholdArrayToNormalise.length; i++)
			sumOfLineValues += thresholdArrayToNormalise[i];
		// calculating average value of lines
		average = (int) (sumOfLineValues / thresholdArrayToNormalise.length);

		for (int i = 0; i < thresholdArrayToNormalise.length; i++) {
			// normalised[i]=thresholdArrayToNormalise[i] - (average - normValue) ;
			if ((thresholdArrayToNormalise[i] - (average - normValue)) >= 0)
				normalised[i] = thresholdArrayToNormalise[i] - (average - normValue);

			else
				normalised[i] = (-1) * (thresholdArrayToNormalise[i] - (average - normValue));
		}
		return normalised;
	}

	// this methode will give all the pixels in hintergrund the same color

	byte[] hintergrundSmoothering(byte[] pixelsin, byte[] pixels, Rectangle roi, int[] thresholdPerLine) {
		for (int i = roi.y; i < roi.y + roi.height; i++) {
			int offset = i * (int) roi.getWidth();
			int lineOffset = i;
			for (int j = roi.x; j < roi.x + roi.width; j++) {
				int pos = offset + j;
				int pix = pixelsin[pos];
				pix = (pix & 0x0000ff);

				// if(pix < thresholdPerLine[lineOffset])
				if (thresholdPerLine[lineOffset] < 25) {
					if (pix < (thresholdPerLine[lineOffset] + 30)) {
						pix = 5;

					}

				} else if ((thresholdPerLine[lineOffset] >= 25) && (thresholdPerLine[lineOffset] < 35)) {
					if (pix < (thresholdPerLine[lineOffset] + 20)) {
						pix = 5;

					}
				} else if ((thresholdPerLine[lineOffset] >= 35) && (thresholdPerLine[lineOffset] < 65)) {
					if (pix < (thresholdPerLine[lineOffset] + 13)) {
						pix = 5;

					}
				}
				if (thresholdPerLine[lineOffset] >= 65) {
					if (pix < (thresholdPerLine[lineOffset])) {
						pix = 5;

					}
				}
				pixels[pos] = (byte) pix;

			}
		}
		return pixels;
	}

	// Glätterung
	void tiefPassFilter(byte[] pixelsin, byte[] pixels, Rectangle roi) {
		// int[] medianOperator = new int[] {0, -1, 0, -1, 4, -1, 0, -1, 0};
		// int[] s_operator = new int[] {1, 2, 1, 0, 0, 0, -1, -2, -1};
		int[] medianOperator = new int[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 }; // Gauss-filter
		// int[] medianOperator = new int[] {-1, -1, -1, -1, 9, -1, -1, -1, -1};
		// //highpass-filter
		// int[] medianOperator = new int[] {-1, 0, 1, -1, 0, 1, -1, 0, 1}; //
		// Kantenfilter
		// int[] medianOperator = new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1}; //Mittlewert
		// Filter
		// int[] medianOperator = new int[] {5, 4, 5, 3, 2, 3, 2, 1, 1}; //Exp Filter,
		// (variation von Gauss-Filter, umgekehrt?)
		// int[] medianOperator = new int[] {4, 3, 4, 2, 1, 2, 1, 0, 0};
		int[] result = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // middle results per byte of &0xff
		int final_result;
		int temp_pix;
		int w = (int) roi.getWidth();
		int h = (int) roi.getHeight();
		for (int i = 1; i < roi.x + roi.width - 1; i++) {
			for (int j = 1; j < roi.y + roi.height - 1; j++) {
				final_result = 0;
				// temp_pix = pixelsin[j*w+i]; //?
				// temp_pix = temp_pix&0xff; //nicht gebraucht
				temp_pix = pixelsin[(j - 1) * w + (i - 1)];
				temp_pix = temp_pix & 0xff;
				result[0] = temp_pix;

				// pixels[j*w + i] += s_operator[0] * temp_pix;
				temp_pix = pixelsin[(j - 1) * w + i];
				temp_pix = temp_pix & 0xff;
				result[1] = temp_pix;

				temp_pix = pixelsin[(j - 1) * w + (i + 1)];
				temp_pix = temp_pix & 0xff;
				result[2] = temp_pix;

				temp_pix = pixelsin[j * w + (i - 1)];
				temp_pix = temp_pix & 0xff;
				result[3] = temp_pix;

				temp_pix = pixelsin[j * w + i];
				temp_pix = temp_pix & 0xff;
				result[4] = temp_pix;

				temp_pix = pixelsin[j * w + (i + 1)];
				temp_pix = temp_pix & 0xff;
				result[5] = temp_pix;

				temp_pix = pixelsin[(j + 1) * w + (i - 1)];
				temp_pix = temp_pix & 0xff;
				result[6] = temp_pix;

				temp_pix = pixelsin[(j + 1) * w + i];
				temp_pix = temp_pix & 0xff;
				result[7] = temp_pix;

				temp_pix = pixelsin[(j + 1) * w + (i + 1)];
				temp_pix = temp_pix & 0xff;
				result[8] = temp_pix;

				for (int k = 0; k < 9; k++) {
					result[k] = (medianOperator[k] * result[k]);
				}

				// final_result = median(result);
				for (int l = 0; l < 9; l++)
					final_result += result[l];
				final_result = (int) (final_result / 9);  //fehler!! soll durch 16 
				// final_result = (int)(final_result/16);
				final_result = final_result & 0xff;

				if (final_result < 0) // 0 is black
					// pixels[j*w+i] =(byte) 0;
					pixels[j * w + i] = (byte) (final_result * (-1));
				else if (final_result > 255) // 255 is white
					pixels[j * w + i] = (byte) 255;
				else
					pixels[j * w + i] = (byte) final_result;

			}
		}
		// return pixels;
	}

	// returns a median value of a given arrays
	int median(int[] arrayToCalculate) {
		Arrays.sort(arrayToCalculate);

		int middle = arrayToCalculate.length / 2;
		int medianValue = 0; // declare variable
		if (arrayToCalculate.length % 2 == 1)
			medianValue = arrayToCalculate[middle];
		else
			medianValue = (arrayToCalculate[middle - 1] + arrayToCalculate[middle]) / 2;
		return medianValue;
	}

	// after the procedures, the image is divided into zone (by local minimums)
	// first zone - background, last zone - bones. All in between - die Gewebe
	void calculationOfPixels(byte[] image, byte[] handBones, byte[] bones, boolean[] minimaBoolean, int firstMinimum,
			int lastMinimum, int beforeLastMinimum) {
		// num of pixels per object
		IJ.log("First minimum" + firstMinimum);
		IJ.log("Last minimum" + lastMinimum);
		IJ.log("Before last minimum" + beforeLastMinimum);
		int background = 0;
		int hand = 0;
		int bone = 0;
		double result = 0;

		// blacking the images before adding the results on them
		for (int k = 0; k < handBones.length; k++) {
			handBones[k] = (byte) 1;
			bones[k] = (byte) 1;
		}
		for (int i = 0; i < image.length; i++) {
			int pix1 = image[i];
			pix1 = (pix1 & 0x0000ff);
			if (pix1 <= firstMinimum) {
				background += 1;
			}
			// else if ((pix1 > firstMinimum) && (pix1 <= lastMinimum))
			else if ((pix1 > firstMinimum) && (pix1 <= beforeLastMinimum)) {
				hand += 1;
				handBones[i] = (byte) 254;
			}
			// else if ((pix1 > lastMinimum) && (pix1 <= 255))
			else if ((pix1 > beforeLastMinimum) && (pix1 <= 255)) {
				// IJ.log("Test, inside bones calculation!!"); //Do not uncomment - very long
				// loop
				bone += 1;
				bones[i] = (byte) 254;
			}
		}

		IJ.log("Total background " + background);
		IJ.log("Total hand " + (hand + bone));
		IJ.log("Bones " + bone);
		result = (double) (bone) / (double) (hand + bone);
		IJ.log("Percentage of bones inside the hand " + String.format("%.4f", result * 100) + "%");
	}

	// normalisation using the formule : g(i) = a + b*f(i)
	int[] normaliseUpdated(int[] thresholdArrayToNormalise) {

		// calculation of min. and max. values of the thresholds
		int[] sortedThreshold = thresholdArrayToNormalise;
		Arrays.sort(sortedThreshold);
		int minThresholdValue = sortedThreshold[0]; // ~9
		int maxThresholdValue = sortedThreshold[sortedThreshold.length - 1]; // ~56
		// Normalisation func: g(i) = a + b*f(i)
		// g(i) - new value, range from 0 to 255
		// f(i) - old value , range from minThresholdValue and maxThresholdValue
		// a = g(i) - b*f(i)
		// b = (g(i) - a) / f(i)

		int a = -48;
		int b = 5;
		int[] normalised = new int[thresholdArrayToNormalise.length];
		// value to be normalised to

		for (int i = 0; i < thresholdArrayToNormalise.length; i++) {
			normalised[i] = a + b * thresholdArrayToNormalise[i];
			normalised[i] = (normalised[i] & 0x0000ff);
			if (normalised[i] < 0) {
				normalised[i] = normalised[i] * (-1);
				normalised[i] = (normalised[i] & 0x0000ff);
			}
		}

		return normalised;

	}
}

