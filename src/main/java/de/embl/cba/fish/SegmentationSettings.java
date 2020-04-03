package de.embl.cba.fish;

/**
 * Created by tischi on 25/03/17.
 */
public class SegmentationSettings
{
    public static String TRACKMATEDOGSUBPIXEL = "TrackMate_DoG_SubPixel";
    public static String TRACKMATEDOG = "TrackMate_DoG";
    public static String IMAGESUITE3D = "3D ImageSuite";

    public double[][] spotRadii;
    public double[] thresholds;
    public double[] backgrounds;
    public int[] channels;
    public String channelIDs;

    public int[] frames;
    public String method;

    public String experimentalBatch;
    public String experimentID;
    public String treatment;
    public String pathName;
    public String fileName;
}
