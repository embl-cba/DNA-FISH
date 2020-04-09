package de.embl.cba.fish;

import ij.ImagePlus;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.OptionalDouble;

public class BackgroundMeasurementDialog
{
    private final ImagePlus imp;
    private RoiManager rm;

    public BackgroundMeasurementDialog( ImagePlus imp )
    {
        this.imp = imp;
    }

    public ArrayList< Double > showDialogAndGetMeasurements()
    {
        rm = showRoiManager();

        final NonBlockingGenericDialog gd = new NonBlockingGenericDialog( "Background measurement" );
        gd.addMessage( "Please put multiple ROIs onto background regions,\n" +
                        "adding them to the ROI Manager. Press OK when done." +
                        "\nThe regions\n" +
                        "- must be inside nuclei,\n" +
                        "- can be in different z-planes,\n" +
                        "- should be close to FISH Spots,\n" +
                        "- must not contain any FISH spots.\n"
        );
        Utils.addHelpButton( gd, "background-measurement" );

        gd.showDialog();
        if ( gd.wasCanceled() ) return null;

        final ArrayList< Double > backgrounds = quantifySelectedRois();
        return backgrounds;
    }

    private ArrayList< Double > quantifySelectedRois( )
    {
        Roi[] rois = rm.getRoisAsArray();

        final ArrayList< ArrayList< Double > > backgroundMeasurements = new ArrayList<>();
        for ( int c = 0; c < imp.getNChannels(); c++ )
        {
            backgroundMeasurements.add( new ArrayList<>(  ) );
        }

        for (Roi roi : rois)
        {
            for ( int c = 1; c <= imp.getNChannels(); c++ )
            {
                imp.setC( c );
                imp.setZ( roi.getPosition() );
                ImageProcessor currentProcessor = imp.getChannelProcessor();
                currentProcessor.setRoi( roi );
                double meanValue = currentProcessor.getStats().mean;
                backgroundMeasurements.get( c - 1 ).add( meanValue );
                System.out.println( String.format( "Roi: %s; channel: %d; intensity: %f", roi.getName(), c, meanValue ) );
            }
        }

        final ArrayList< Double > averageBackgroundMeasurements = new ArrayList<>();
        for ( int c = 0; c < imp.getNChannels(); c++ )
        {
            final OptionalDouble average = backgroundMeasurements.get( c ).stream().mapToDouble( x -> x ).average();
            averageBackgroundMeasurements.add( average.getAsDouble() );
        }

        return averageBackgroundMeasurements;
    }

    public static RoiManager showRoiManager()
    {
        RoiManager rm=RoiManager.getInstance();
        if (rm==null)
            rm=new RoiManager();
        else
            rm.runCommand("Reset");
        return rm;
    }

}
