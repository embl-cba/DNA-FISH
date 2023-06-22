package de.embl.cba.fish;

import ij.IJ;
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
    private RoiManager roiManager;

    public BackgroundMeasurementDialog( ImagePlus imp )
    {
        this.imp = imp;
    }

    public ArrayList< Double > showDialogToGetBackgroundMeasurements()
    {
        initOvalRoiSelection();

        final NonBlockingGenericDialog gd = new NonBlockingGenericDialog( "Background measurement" );
        gd.addMessage( "Please put multiple ROIs onto nuclear background regions,\n" +
                        "adding them to the ROI Manager. Press OK when done." +
                        "\nThe regions\n" +
                        "- must be inside nuclei,\n" +
                        "- can be in different z-planes,\n" +
                        "- should be close to FISH Spots,\n" +
                        "- should be well distributed across the image,\n" +
                        "- must not contain any FISH spots.\n"
        );
        Utils.addHelpButton( gd, "background-measurement" );

        gd.showDialog();
        if ( gd.wasCanceled() ) return null;

        final ArrayList< Double > backgrounds = measureAverageMeanIntensityInSelectedRois();

        Utils.closeRoiManagerAndRemoveRoisFromImage( roiManager, imp );

        return backgrounds;
    }

    private ArrayList< Double > measureAverageMeanIntensityInSelectedRois( )
    {
        if ( roiManager.getCount() == 0 )
            throw new UnsupportedOperationException("Please place at least one background ROI.");

        Roi[] rois = roiManager.getRoisAsArray();

        final ArrayList< ArrayList< Double > > backgroundMeasurements = new ArrayList<>();
        for ( int c = 0; c < imp.getNChannels(); c++ )
        {
            backgroundMeasurements.add( new ArrayList<>(  ) );
        }

        for ( Roi roi : rois )
        {
            // for simplicity, measure background for all channels, even though we only need the
            // background for the spot channels
            for ( int channelIndex = 0; channelIndex < imp.getNChannels(); channelIndex++ )
            {
                imp.setC( channelIndex + 1 );
                imp.setZ( roi.getZPosition() );
                ImageProcessor currentProcessor = imp.getChannelProcessor();
                currentProcessor.setRoi( roi );
                double meanValue = currentProcessor.getStats().mean;
                backgroundMeasurements.get( channelIndex ).add( meanValue );
                //System.out.println( String.format( "Roi: %s; channel: %d; intensity: %f", roi.getName(), channelIndex + 1, meanValue ) );
            }
        }

        final ArrayList< Double > averageBackgroundMeasurements = new ArrayList<>();
        for ( int channelIndex = 0; channelIndex < imp.getNChannels(); channelIndex++ )
        {
            final OptionalDouble average = backgroundMeasurements.get( channelIndex ).stream().mapToDouble( x -> x ).average();
            averageBackgroundMeasurements.add( average.getAsDouble() );
        }

        return averageBackgroundMeasurements;
    }

    private void initOvalRoiSelection()
    {
        IJ.setTool("oval");
        roiManager = RoiManager.getInstance();
        if ( roiManager == null)
            roiManager = new RoiManager();
        else
            roiManager.runCommand("Reset");
        roiManager.runCommand(imp,"Show All");
    }

}
