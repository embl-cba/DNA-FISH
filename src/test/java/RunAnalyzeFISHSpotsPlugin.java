import de.embl.cba.fish.AnalyzeFISHSpotsGUI;
import ij.IJ;
import net.imagej.ImageJ;

public class RunAnalyzeFISHSpotsPlugin
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		// Open example image
		IJ.open("/Users/tischer/Documents/fiji-plugin-FISH/src/test/resources/test-data-00.zip");

		AnalyzeFISHSpotsGUI analyzeFISHSpotsGUI = new AnalyzeFISHSpotsGUI();
		analyzeFISHSpotsGUI.showDialog();

		// TODO:
		// - channels color should not change when finding spots. keep colors of input data.
	}
}
