package de.embl.cba.fish;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

import javax.swing.*;

/**
 * Created by tischi on 24/03/17.
 */
public class AnalyzeFISHSpotsPlugIn implements PlugIn {

    ImagePlus imp;
    AnalyzeFISHSpotsGUI analyzeFISHSpotsGUI;

    public AnalyzeFISHSpotsPlugIn( ) {
    }

    public AnalyzeFISHSpotsPlugIn(String path) {
        this();
        IJ.open(path);
    }

    public void run(String arg) {
        this.imp = IJ.getImage();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showDialog();
            }
        });
    }

    public void showDialog(){
        analyzeFISHSpotsGUI = new AnalyzeFISHSpotsGUI();
        analyzeFISHSpotsGUI.showDialog();
    }

    public static String getChannelFlag(int iChannel)
    {
        String key = "Analyze Channel " + (iChannel + 1);
        return key;
    }

    public static String getSpotThresholdKey(int iChannel)
    {
        String key = "Spot Threshold Channel " + (iChannel + 1);
        return key;
    }

    public static String getSpotRadiiKey(int iChannel)
    {
        String key = "Spot Radii Channel " + (iChannel + 1);
        return key;
    }



}
