package de.embl.cba.fish;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;

import net.imglib2.Cursor;
import net.imglib2.algorithm.region.localneighborhood.RectangleNeighborhoodGPL;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;

import ij.ImagePlus;
import net.imglib2.outofbounds.OutOfBoundsMirrorExpWindowingFactory;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Class for preforming secondary analysis on the segmentation results.
 * Such as
 * - measuring distances between the segmented objects
 * - ...
 * 
 * @param <T>
 */

public class SegmentationAnalyzer< T extends RealType< T >>
{
    ImagePlus imp;
    SegmentationSettings segmentationSettings;
    SegmentationResults segmentationResults;

    public SegmentationAnalyzer( ImagePlus imp,
                                 SegmentationSettings segmentationSettings,
                                 SegmentationResults segmentationResults)
    {
        this.imp = imp;
        this.segmentationResults = segmentationResults;
        this.segmentationSettings = segmentationSettings;
    }

    public void analyzeSpotsClosestToSelectedPoints( SpotCollection selectedPoints )
    {
        // Initialise Results Table
        //
        segmentationResults.SpotsTable = new SpotsTable();
        segmentationResults.SpotsTable.segmentationSettings = segmentationSettings;
        segmentationResults.SpotsTable.initializeTable();

        // Get spot locations and compute pair-wise distances for each selection region
        //
        for (Spot selectedPoint : selectedPoints.iterable(false)) {

            // Init a new row in the table
            //
            List<String> tableRow = new ArrayList<>();

            // Add metadata to table
            //
            tableRow.add(segmentationSettings.experimentalBatch);
            tableRow.add(segmentationSettings.experimentID);
            tableRow.add(segmentationSettings.treatment);
            tableRow.add(segmentationSettings.pathName);
            tableRow.add(segmentationSettings.fileName);
            tableRow.add( Arrays.stream( segmentationSettings.spotChannelIndicesOneBased ).mapToObj( x -> "" + x )
                    .collect( Collectors.joining(",")));


            // Add selected region center to the table
            //
            for (int d = 0; d < 3; d++)
            {
                tableRow.add( String.valueOf( selectedPoint.getDoublePosition(d) ) );
            }

            // Find the closest spot to the selected point in each channel
            //
            final int numFISHChannels = segmentationSettings.spotChannelIndicesOneBased.length;
            Spot[] closestSpotsTrackMateDoGMax = new Spot[ numFISHChannels ];
            Spot[] closestSpotsCenterOfMass = new Spot[ numFISHChannels ];

            for ( int channelIndex = 0; channelIndex < numFISHChannels; channelIndex++) {

                SpotCollection spotCollection = segmentationResults.models[channelIndex].getSpots();

                /*
                // print all spots
                for (Spot spot : spotCollection.iterable(false)) {
                    Point3D pSpot = new Point3D(spot.getDoublePosition(0),
                            spot.getDoublePosition(1),
                            spot.getDoublePosition(2));
                    log("SPOT: "+pSpot.toString());
                }*/

                int frame = 0; // 0-based time point
                Spot spot = spotCollection.getClosestSpot(selectedPoint, frame, false);

                if ( spot != null ) {

                    Spot spotCenterOfMass = computeCenterOfMass(spot, channelIndex, segmentationSettings.backgrounds[channelIndex]);

                    // Add LoG based position to table
                    //
                    for (int d = 0; d < 3; d++)
                    {
                        tableRow.add(String.valueOf(spot.getDoublePosition(d)));
                    }

                    // Add center of mass to table
                    //
                    for (int d = 0; d < 3; d++)
                    {
                        tableRow.add(String.valueOf(spotCenterOfMass.getDoublePosition(d)));
                    }

                    // Remember positions for distance computations
                    //
                    closestSpotsTrackMateDoGMax[channelIndex] = spot;
                    closestSpotsCenterOfMass[channelIndex] = spotCenterOfMass;

                }
                else
                {
                    tableRow.add("No spot found");
                    tableRow.add("No spot found");
                    tableRow.add("No spot found");
                }

            } // channel loop


            // Compute pair-wise distances and add to table
            //
            computePairWiseDistances(tableRow, closestSpotsTrackMateDoGMax);
            computePairWiseDistances(tableRow, closestSpotsCenterOfMass);

            // Add the whole row to actual table
            //
            segmentationResults.SpotsTable.addRow(tableRow.toArray(new Object[tableRow.size()]));

        } // selected region loop
    }

    public void computePairWiseDistances(List<String> tableRow, Spot[] spots)
    {
        for ( int i = 0; i < spots.length - 1; i++ )
        {
            for ( int j = i + 1; j < spots.length; j++ )
            {
                Double distance = Math.sqrt(spots[i].squareDistanceTo(spots[j]));
                tableRow.add(String.valueOf(distance));
            }
        }
    }


    public Spot computeCenterOfMass(Spot spot, int iChannel, double backgroundValue)
    {
        // https://javadoc.imagej.net/ImgLib2/net/imglib2/view/Views.html
        // https://github.com/imglib/imglib2-introductory-workshop/blob/master/completed/ImgLib2_CenterOfMass2.java
        // http://javadoc.imagej.net/ImgLib2/index.html?net/imglib2/algorithm/neighborhood/Neighborhood.html

        // wrap to img
        //
        Img<T> img = ImageJFunctions.wrapReal(imp);

        /*for ( int d = 0; d < img.numDimensions(); d++ )
        {
            long nd = img.dimension(d);
            nd += 2;
        }*/


        // Compute spot center in pixel coordinates
        //
        // ! Dimensions in img are: x,y,c,z,t
        long[] center = new long[img.numDimensions()];
        center[0] = Math.round(spot.getFeature(Spot.POSITION_FEATURES[0]).doubleValue() / imp.getCalibration().pixelWidth);
        center[1] = Math.round(spot.getFeature(Spot.POSITION_FEATURES[1]).doubleValue() / imp.getCalibration().pixelHeight);
        center[2] = segmentationSettings.spotChannelIndicesOneBased[iChannel] - 1;  // zero-based channels for img
        center[3] = Math.round(spot.getFeature(Spot.POSITION_FEATURES[2]).doubleValue() / imp.getCalibration().pixelDepth);

        // Set radii of the region in which the center of mass should be computed
        //
        long[] size = new long[]{
                Math.round(1.5 * segmentationSettings.spotRadii[iChannel][0]),
                Math.round(1.5 * segmentationSettings.spotRadii[iChannel][1]),
                0,  // 0 size in  channel dimension, because we only want to evaluate one channel
                Math.round(1.5 * segmentationSettings.spotRadii[iChannel][2])};

        // Create a local neighborhood around this spot
        //
        RectangleNeighborhoodGPL rectangleNeighborhood = new RectangleNeighborhoodGPL(img, new OutOfBoundsMirrorExpWindowingFactory() );
        rectangleNeighborhood.setPosition(center);
        rectangleNeighborhood.setSpan(size);
        Cursor<T> cursor = rectangleNeighborhood.localizingCursor();

        // Loop through the region and compute the center of mass
        //
        double[] sumDim = new double[]{0,0,0};
        double sumVal = 0;
        while ( cursor.hasNext() )
        {
            // move the cursor to the next pixel
            cursor.fwd();

            double x = cursor.getDoublePosition(0) * imp.getCalibration().pixelWidth;
            double y = cursor.getDoublePosition(1) * imp.getCalibration().pixelHeight;
            double c = cursor.getDoublePosition(2);  // this is the channel; should be the same always
            double z = cursor.getDoublePosition(3) * imp.getCalibration().pixelDepth;
            double v = cursor.get().getRealDouble() -  backgroundValue;

            sumDim[0] += x * v;
            sumDim[1] += y * v;
            sumDim[2] += z * v;

            sumVal += v;
        }

        int radius = 1;
        int quality = 1;
        Spot spotCenterOfMass = new Spot(
                sumDim[0]/sumVal,
                sumDim[1]/sumVal,
                sumDim[2]/sumVal,
                radius,
                quality);

        return spotCenterOfMass;
    }

}
