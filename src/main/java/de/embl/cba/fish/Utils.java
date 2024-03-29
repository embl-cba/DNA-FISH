/*
 * #%L
 * Data streaming, tracking and cropping tools
 * %%
 * Copyright (C) 2017 Christian Tischer
 *
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package de.embl.cba.fish;

import fiji.plugin.trackmate.Spot;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.frame.RoiManager;

import javax.swing.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import static ij.IJ.log;

/**
 * Created by tischi on 06/11/16.
 */

public class Utils {

    public static boolean verbose = false;
    public static String version = "2016-Nov-21a";

    public static void threadlog(final String log) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                log(log);
            }
        });
    }

    public static double[] delimitedStringToDoubleArray(String s, String delimiter)
    {
        String[] sA = s.split(delimiter);
        double[] nums = new double[sA.length];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = Double.parseDouble(sA[i]);
        }

        return nums;
    }

    public static int[] delimitedStringToIntegerArray(String s, String delimiter)
    {
        String[] sA = s.split(delimiter);
        int[] nums = new int[sA.length];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = Integer.parseInt(sA[i]);
        }

        return nums;
    }

    public static void logSpotCoordinates(String string, Spot spot)
    {
        log(string+": "+
                +spot.getDoublePosition(0)+","
                +spot.getDoublePosition(1)+","
                +spot.getDoublePosition(2));
    }

    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            log(""+pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public static void openUrl(String url) throws IOException, URISyntaxException
    {
        if(java.awt.Desktop.isDesktopSupported() ) {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

            if(desktop.isSupported(java.awt.Desktop.Action.BROWSE) ) {
                java.net.URI uri = new java.net.URI(url);
                desktop.browse(uri);
            }
        }
    }

	public static void addHelpButton( NonBlockingGenericDialog gd, final String section )
	{
		gd.addHelp( "https://github.com/tischi/fiji-plugin-FISH/blob/master/README.md#" + section );
		gd.setHelpLabel( "Help" );
	}

    public static void closeRoiManagerAndRemoveRoisFromImage( RoiManager roiManager, ImagePlus imp )
    {
        roiManager.runCommand(imp,"Show None");
        roiManager.close();
        IJ.run(imp, "Select None", "");
    }
}
