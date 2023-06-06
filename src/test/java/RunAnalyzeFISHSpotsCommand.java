import de.embl.cba.fish.AnalyzeFISHSpotsCommand;
import ij.IJ;
import net.imagej.ImageJ;

public class RunAnalyzeFISHSpotsCommand
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		// Open example image
		IJ.open("/Users/tischer/Documents/fiji-plugin-FISH/src/test/resources/test-data-00.zip");

		imageJ.command().run( AnalyzeFISHSpotsCommand.class, true );
	}
}
