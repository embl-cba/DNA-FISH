package de.embl.cba.fish;

import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.util.HashMap;
import java.util.Map;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.features.ModelFeatureUpdater;
import fiji.plugin.trackmate.features.manual.ManualSpotColorAnalyzerFactory;
import fiji.plugin.trackmate.features.track.TrackIndexAnalyzer;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.visualization.ManualSpotColorGenerator;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import ij.CompositeImage;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.process.LUT;


// TODO:
// - only highlight the points of the active channels

public class SegmentationOverlay implements ImageListener {

    ImagePlus imp;
    SegmentationResults segmentationResults;
    SegmentationSettings segmentationSettings;
    boolean[] activeChannels;

    // TrackMate specific
    public SelectionModel selectionModel;
    public Model selectedChannelsModel;
    HyperStackDisplayer hyperStackDisplayer;

    public SegmentationOverlay(ImagePlus imp,
                               SegmentationResults segmentationResults,
                               SegmentationSettings segmentationSettings)

    {
        this.imp = imp;
        this.segmentationResults = segmentationResults;
        this.segmentationSettings = segmentationSettings;

        activeChannels = new boolean[imp.getNChannels()];
        for ( int i = 0; i < activeChannels.length; i++ )
        {
            activeChannels[ i ] = true;
        }
        ImagePlus.addImageListener(this);
    }

	public void highlightClosestSpotOfActiveChannels( Spot location, int frame )
    {
        selectionModel.clearSpotSelection();

        for ( int iChannel = 0; iChannel < segmentationSettings.spotChannelIndicesOneBased.length; iChannel++)
        {
            // add spots to selectionModel only if this channel is active
            //
            if (activeChannels[segmentationSettings.spotChannelIndicesOneBased[iChannel] - 1])
            {
                Model model = segmentationResults.models[iChannel];
				selectionModel.addSpotToSelection( model.getSpots().getClosestSpot( location, frame, false ) );
            }
        }

        location.putFeature("FRAME", (double) frame); // if this is not set the "center view on method" crashes
        hyperStackDisplayer.centerViewOn(location);
        hyperStackDisplayer.refresh();
    }

    public void trackMateClearSpotSelection()
    {
        selectionModel.clearSpotSelection();
    }

    public void setTrackMateOverlayFromTable()
    {
        selectedChannelsModel = new Model();
        selectedChannelsModel.setLogger(Logger.IJ_LOGGER);
        Settings settings = new Settings();
        settings.addTrackAnalyzer(new TrackIndexAnalyzer());
        ModelFeatureUpdater modelFeatureUpdater = new ModelFeatureUpdater( selectedChannelsModel, settings);

        int frame = 0; // zero-based !!
        int channelColumn = 5;

        //segmentationSettings.channelIDs = segmentationResults.SpotsTable.table.getModel().getValueAt(0, channelColumn).toString();
       // segmentationSettings.spotChannelIndices =  Utils.delimitedStringToIntegerArray(segmentationSettings.channelIDs, ";");

        selectedChannelsModel.beginUpdate();
        for ( int iChannel = 0; iChannel < segmentationSettings.spotChannelIndicesOneBased.length; iChannel++)
        {
            // add spots to overlay only if this channel is active
            //
            if (activeChannels[segmentationSettings.spotChannelIndicesOneBased[iChannel] - 1])
            {
                /*
                Spot spot = new Spot();
                spot.putFeature("COLOR", (double) segmentationSettings.channels[iChannel]); // one-based
                modelSelectedChannels.addSpotTo(spot, frame);
                */
            }
        }
        selectedChannelsModel.endUpdate();
    }

    public void setTrackMateModelForVisualisationOfSelectedChannels()
    {
        // get the multi-channel TrackMate results
        Model[] models = segmentationResults.models;

        selectedChannelsModel = new Model();
        selectedChannelsModel.setLogger(Logger.IJ_LOGGER);

        Settings settings = new Settings();
        settings.addTrackAnalyzer(new TrackIndexAnalyzer());

        int frame = 0; // zero-based !!

        Map< Integer, Color > channelIndexToColor = createChannelIndexToColorMap();

        selectedChannelsModel.beginUpdate();
        for ( int iChannel = 0; iChannel < segmentationSettings.spotChannelIndicesOneBased.length; iChannel++ )
        {
            // add spots to overlay only if this channel is active
            //
            if (activeChannels[segmentationSettings.spotChannelIndicesOneBased[iChannel] - 1])
            {
                Model model = models[iChannel];
                SpotCollection spotCollection = model.getSpots();
                for (Spot spot : spotCollection.iterable(false))
                {
                    final int channelIndex = segmentationSettings.spotChannelIndicesOneBased[ iChannel ];
                    final Color color = channelIndexToColor.get( channelIndex );
                    final Double colorIndex = Double.valueOf( color.getRGB() );
                    System.out.println("Channel: " + channelIndex + "; color: " + colorIndex);
                    spot.putFeature(ManualSpotColorAnalyzerFactory.FEATURE, colorIndex);
                    selectedChannelsModel.addSpotTo(spot, frame);
                }
            }
        }
        selectedChannelsModel.endUpdate();
    }

    private Map< Integer, Color > createChannelIndexToColorMap()
    {
        final CompositeImage compositeImage = ( CompositeImage ) (imp);
        final int nChannels = compositeImage.getNChannels();
        Map< Integer, Color > channelIndexToColor = new HashMap<>();
        final LUT[] luts = compositeImage.getLuts();
        for ( int channelIndex = 0; channelIndex < nChannels; channelIndex++ )
        {
            IndexColorModel cm = luts[ channelIndex ];
            int index = cm.getMapSize() - 1;
            int r = cm.getRed(index);
            int g = cm.getGreen(index);
            int b = cm.getBlue(index);
            final Color color = new Color( r, g, b );
            channelIndexToColor.put( channelIndex+1, color );
        }
        return channelIndexToColor;
    }

    public void displayTrackMateModelAsOverlay()
    {
        // Configure trackMate's visualization scheme
		DisplaySettings ds = DisplaySettings.defaultStyle();
		ds.setTrackVisible( false );
		ds.setSpotVisible( true );
		ds.setSpotDisplayRadius( 2. );
		ds.setHighlightColor( Color.BLUE );
        ds.setSpotColorBy( DisplaySettings.TrackMateObject.SPOTS, ManualSpotColorAnalyzerFactory.FEATURE );

		selectionModel = new SelectionModel( selectedChannelsModel );
		hyperStackDisplayer = new HyperStackDisplayer( selectedChannelsModel, selectionModel, imp, ds );
        hyperStackDisplayer.render();
        hyperStackDisplayer.refresh();
    }

    public LUT createLUTFromColor(Color color)
    {
        byte[] red = new byte[256];
        byte[] green = new byte[256];
        byte[] blue = new byte[256];

        for (int i=0; i<256; i++)
        {
            red[i] = (byte)(color.getRed()*i/255.0);
            green[i] = (byte)(color.getGreen()*i/255.0);
            blue[i] = (byte)(color.getBlue()*i/255.0);
        }

        return new LUT(red, green, blue);
    }

    public static void clearOverlay(ImagePlus imp)
    {
        Overlay overlay = imp.getOverlay();
        if(overlay != null) {
            overlay.clear();
        }
    }

    public void updateActiveChannels()
    {
        if( imp == IJ.getImage() )
        {
            boolean updateView = false;
            boolean[] activeChannelsImp = ((CompositeImage) imp).getActiveChannels();
            for (int i = 0; i < activeChannels.length; i++)
            {
                //Utils.threadlog("Channel " + (i+1) + ":" + activeChannels[i]);
                if (activeChannelsImp[i] != activeChannels[i])
                {
                    updateView = true;
                    activeChannels[i] = activeChannelsImp[i];
                }
            }

            // update the view
            if( updateView )
            {
                this.setTrackMateModelForVisualisationOfSelectedChannels();
                this.displayTrackMateModelAsOverlay();
            }
        }
    }


    @Override
    public void imageOpened(ImagePlus imagePlus)
    {

    }

    @Override
    public void imageClosed(ImagePlus imagePlus)
    {
        Utils.threadlog("closed");
        ImagePlus.removeImageListener(this);

    }

    @Override
    public void imageUpdated(ImagePlus imagePlus)
    {
        updateActiveChannels();
    }
}
