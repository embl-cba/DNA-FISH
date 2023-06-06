package de.embl.cba.fish;

import ij.ImagePlus;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;

import static de.embl.cba.fish.ChannelConfigDialog.ChannelType;

@Plugin(type = Command.class, menuPath = "Plugins>Analyze>FISH Spots" )
public class AnalyzeFISHSpotsCommand implements Command {

    @Parameter
    ImagePlus imp;

    @Override
    public void run()
    {
        final ArrayList< ChannelType > channelTypes = new ChannelConfigDialog( imp ).showDialogToGetChannelTypes();
        final ArrayList< Double > channelBackgrounds = new BackgroundMeasurementDialog( imp ).showDialogToGetBackgroundMeasurements();
        new AnalyzeFISHSpotsUI( imp, channelTypes, channelBackgrounds ).showDialog();
    }
}
