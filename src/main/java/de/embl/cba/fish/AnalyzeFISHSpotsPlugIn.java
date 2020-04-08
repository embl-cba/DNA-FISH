package de.embl.cba.fish;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

import javax.swing.*;
import java.util.ArrayList;

import static de.embl.cba.fish.ChannelConfigDialog.*;

/**
 * Created by tischi on 24/03/17.
 */
public class AnalyzeFISHSpotsPlugIn implements PlugIn {

    ImagePlus imp;
    AnalyzeFISHSpotsUI analyzeFISHSpotsUI;

    public AnalyzeFISHSpotsPlugIn()
    {
    }

    public AnalyzeFISHSpotsPlugIn( String path) {
        IJ.open(path);
        this.imp = IJ.getImage();
    }

    public void run(String arg) {
        if ( imp == null )
            this.imp = IJ.getImage();

        final ArrayList< ChannelType > channelTypes = showChannelConfigDialog();
        showAnalysisUI( channelTypes );
    }

    private ArrayList< ChannelType > showChannelConfigDialog()
    {
        return new ChannelConfigDialog( imp ).getChannelTypesDialog();
    }

    private void showAnalysisUI( ArrayList< ChannelType > channelTypes ){
        analyzeFISHSpotsUI = new AnalyzeFISHSpotsUI( channelTypes );
        analyzeFISHSpotsUI.showDialog();
    }
}
