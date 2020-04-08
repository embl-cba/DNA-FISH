import de.embl.cba.fish.AnalyzeFISHSpotsPlugIn;
import ij.IJ;
import net.imagej.ImageJ;

import javax.swing.*;

public class RunAnalyzeFISHSpotsPlugin
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		// Open example image
		IJ.open("/Users/tischer/Documents/fiji-plugin-FISH/src/test/resources/test-data-00.zip");

		new AnalyzeFISHSpotsPlugIn().run( "" );


		// TODO:
		// - channels color should not change when finding spots. keep colors of input data.
	}
}
