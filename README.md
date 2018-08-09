Repository to include the image processing exercises and the semester project (Beleg). All the files are written in Java, and 
are plugins for ImageJ ( https://imagej.nih.gov/ij/download.html )

Beleg:

Finding the bones and the hand in an X-Ray  image. Calculating the amount of bones in the whole hand surface.

Working process:
1. Brightness adjustment - everything in the background occupies the same brigthness - reached using iterative threshold 
algorithm per line 
2. Smoothing the image - Guss Filter
3. Reduction of the number of colors from 256 to 32 for calculation of local minimum values (needed to separate background from
the hand and from the bones)
4. Calculation of the local minimum values
5. Evaluation of the results, and representation of the number in %
