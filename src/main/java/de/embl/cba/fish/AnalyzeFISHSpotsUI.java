package de.embl.cba.fish;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.frame.RoiManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.stream.IntStream;

import static de.embl.cba.fish.ChannelConfigDialog.*;


// Notes:
// - See: https://imagej.net/TrackMate_Algorithms#Spot_features_generated_by_the_spot_detectors
// -
// - The Quality feature of the DoG is the actual maximal DoG signal
// - We hope that TrackMate will be used in experiments requiring Sub-pixel localization, such as following motor proteins in biophysical experiments, so we added schemes to achieve this. The one currently implemented uses a quadratic fitting scheme (made by Stephan Saalfeld and Stephan Preibisch) based on David Lowe SIFT work[1]. It has the advantage of being very quick, compared to the segmentation time itself.
//     - See: http://www.cs.ubc.ca/~lowe/keypoints/

// Ideas:
// - maybe bin the data in z to have it isotropic in terms sigmas?

public class AnalyzeFISHSpotsUI implements ActionListener, FocusListener {

    private static final String SPOT_RADII_DEFAULT = "1.5,1.5,3";
    private static final String SPOT_THRESHOLD_DEFAULT = "200";
    private final ArrayList< ChannelType > channelTypes;

    // GUI
    JFrame frame;

    private final String buttonHelpText = "Help";
    private JButton buttonHelp = new JButton();

    private final String buttonSegmentSpotsText = "Find spots";
    private JButton buttonSegmentSpots =  new JButton();

    private final String buttonAnalyzeSpotsAroundSelectedRegionsText = "Analyze spots";
    private JButton buttonAnalyzeSpotsAroundSelectedRegions =  new JButton();

    private final String buttonSaveTableText = "Save table";
    private JButton buttonSaveTable =  new JButton();

    private final String buttonLoadTableText = "Load table";
    JButton buttonLoadTable =  new JButton();

    private final String buttonLogColumnAverageText = "Log column averages";
    JButton buttonLogColumnAverage =  new JButton();

    final String textFieldSpotRadiiLabel = "Spot radii [pixels]";
    JTextField textFieldSpotRadii = new JTextField(20);

    final String textFieldSpotThresholdsLabel = "Spot channel thresholds [a.u.]";
    JTextField textFieldSpotThresholds = new JTextField(12);

    final String textFieldSpotBackgroundValuesLabel = "Spot background values [gray value]";
    JTextField textFieldSpotBackgroundValues = new JTextField(12);

    final String textFieldExperimentalBatchLabel = "Experimental batch";
    JTextField textFieldExperimentalBatch = new JTextField(15);

    final String textFieldTreatmentLabel = "Treatment";
    JTextField textFieldTreatment = new JTextField(15);

    final String textFieldExperimentIDLabel = "Experiment ID";
    JTextField textFieldExperimentID = new JTextField(15);

    final String textFieldPathNameLabel = "Path to image";
    JTextField textFieldPathName = new JTextField(15);

    final String textFieldFileNameLabel = "Filename of image";
    JTextField textFieldFileName = new JTextField(15);

    final String comboBoxSegmentationMethodLabel = "Segmentation method";
    JComboBox comboBoxSegmentationMethod = new JComboBox(new String[] { SegmentationSettings.TRACKMATEDOGSUBPIXEL, SegmentationSettings.TRACKMATEDOG});

    private SegmentationResults segmentationResults = new SegmentationResults();
    private SegmentationSettings segmentationSettings = new SegmentationSettings();
    private SegmentationOverlay segmentationOverlay;

    private ImagePlus imp;
    private final int numFISHChannels;
    private final ArrayList< Double > channelBackgrounds;

    public AnalyzeFISHSpotsUI( ImagePlus imp, ArrayList< ChannelType > channelTypes, ArrayList< Double > channelBackgrounds )
    {
        this.imp = imp;
        this.channelTypes = channelTypes;
        this.numFISHChannels = ( int ) channelTypes.stream().filter( x -> x.equals( ChannelType.FISHSpots ) ).count();
        this.channelBackgrounds = channelBackgrounds;

        setSpotChannelIndices();
        setSpotChannelBackgrounds();
    }

    public void showDialog()
    {
        imp = IJ.getImage();

        frame = new JFrame("FISH Spots");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Container c = frame.getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

        // Panels
        //
        ArrayList<JPanel> panels = new ArrayList<JPanel>();
        int iPanel = 0;

        // Help
        //
        addButton(panels, iPanel++, c, buttonHelp, buttonHelpText);

        // Spot detection
        //
        addHeader(panels, iPanel++, c, "SPOT DETECTION");
        addComboBox(panels, iPanel++, c, comboBoxSegmentationMethod, comboBoxSegmentationMethodLabel);  // Do not show as there is only one choice
        addTextField(panels, iPanel++, c, textFieldSpotRadii, textFieldSpotRadiiLabel, getSpotRadiiDefaults() );
        addTextField(panels, iPanel++, c, textFieldSpotThresholds, textFieldSpotThresholdsLabel, getSpotTresholdDefaults() );
        addButton(panels, iPanel++, c, buttonSegmentSpots, buttonSegmentSpotsText);

        // Spot analysis
        //
        addHeader(panels, iPanel++, c, "SPOT ANALYSIS");
        addButton(panels, iPanel++, c, buttonAnalyzeSpotsAroundSelectedRegions, buttonAnalyzeSpotsAroundSelectedRegionsText );

        // Table
        //
        addHeader(panels, iPanel++, c, "TABLE");
        addTextField(panels, iPanel++, c, textFieldExperimentalBatch, textFieldExperimentalBatchLabel, "Today");
        addTextField(panels, iPanel++, c, textFieldExperimentID, textFieldExperimentIDLabel, "001");
        addTextField(panels, iPanel++, c, textFieldTreatment, textFieldTreatmentLabel, "Negative_Control");
        //addTextField(panels, iPanel++, c, textFieldPathName, textFieldPathNameLabel, imp.getOriginalFileInfo().directory);
        //addTextField(panels, iPanel++, c, textFieldFileName, textFieldFileNameLabel, imp.getOriginalFileInfo().fileName);
        addButton(panels, iPanel++, c, buttonLogColumnAverage, buttonLogColumnAverageText);
        addButton(panels, iPanel++, c, buttonSaveTable, buttonSaveTableText);
        //addButton(panels, iPanel++, c, buttonLoadTable, buttonLoadTableText);

        // Show GUI
        //
        frame.pack();
        frame.setLocation(imp.getWindow().getX() + imp.getWindow().getWidth(), imp.getWindow().getY());
        frame.setVisible(true);
    }

    // TODO: Read from Preferences
    private String getSpotRadiiDefaults()
    {
        String defaults = "";
        for ( int i = 0; i < numFISHChannels; i++ )
        {
            if ( i > 0 ) defaults += ";";
            defaults += SPOT_RADII_DEFAULT;
        }
        return defaults;
    }

    // TODO: Read from Preferences
    private String getSpotTresholdDefaults()
    {
        String defaults = "";
        for ( int i = 0; i < numFISHChannels; i++ )
        {
            if ( i > 0 ) defaults += ";";
            defaults += SPOT_THRESHOLD_DEFAULT;
        }
        return defaults;
    }

    public void actionPerformed(ActionEvent e)
    {
        imp = IJ.getImage();

        updateSegmentationSettings();

        if ( e.getActionCommand().equals( buttonHelpText ) )
        {
            showHelp();
        }
        else if ( e.getActionCommand().equals( buttonSegmentSpotsText ) )
        {
            segmentSpotsAction();
            initPointRoiSelection();
        }
        else if ( e.getActionCommand().equals( buttonAnalyzeSpotsAroundSelectedRegionsText ) )
        {
            analyzeSpotsAroundSelectedPointsAction();
        }
        else if ( e.getActionCommand().equals( buttonLogColumnAverageText ) )
        {
            segmentationResults.SpotsTable.logColumnAverages();
        }
        else if ( e.getActionCommand().equals( buttonSaveTableText ) )
        {
            saveTableAction();
        }
        else  if ( e.getActionCommand().equals( buttonLoadTableText ) )
        {
            loadTableAction();
        }
    }

    private void showHelp()
    {
        try
        {
            Utils.openUrl( "https://github.com/tischi/fiji-plugin-FISH/blob/master/README.md#usage" );
        } catch ( IOException e )
        {
            e.printStackTrace();
        } catch ( URISyntaxException e )
        {
            e.printStackTrace();
        }
    }

    private void loadTableAction()
    {
        // Load Table
        //
        JFileChooser jFileChooser = new JFileChooser(segmentationSettings.pathName);
        if (jFileChooser.showOpenDialog(this.frame) == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            if ( file != null )
            {
                // load and show table
                //
                segmentationResults.SpotsTable = new SpotsTable();
                segmentationResults.SpotsTable.loadTable(file);
                segmentationResults.SpotsTable.showTable();

                // construct overlay from table
                //
                //segmentationResults.table.segmentationOverlay = segmentationOverlay;
            }
            else
            {
                Utils.threadlog("No file selected.");
            }
        }
    }

    private void saveTableAction()
    {
        // Save Table
        //
        JFileChooser jFileChooser = new JFileChooser( segmentationSettings.pathName );
        if ( jFileChooser.showSaveDialog( this.frame ) == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            segmentationResults.SpotsTable.saveTable(file);
        }
    }

    private void analyzeSpotsAroundSelectedPointsAction()
    {
        // Check the ROI Manager for selected points
        //
        // ....

        // Measure spots around selected points
        //
        SegmentationAnalyzer segmentationAnalyzer =
                new SegmentationAnalyzer(imp, segmentationSettings, segmentationResults);

        SpotCollection selectedPoints = getSelectedPointsFromRoiManager();

        if ( selectedPoints.getNSpots( true ) == 0 )
        {
            IJ.showMessage( "Please select one or more regions using the multi-point selection tool,\nwhich should be selected already such that you just have to click into the image." );
            return;
        }


        segmentationAnalyzer.analyzeSpotsClosestToSelectedPoints( selectedPoints );


        // Show results table
        //
        segmentationResults.SpotsTable.showTable();

        // Notify table about overlay (such that it can change it, upon selection of a specific row)
        //
        segmentationResults.SpotsTable.segmentationOverlay = segmentationOverlay;
    }

    private void updateSegmentationSettings()
    {
        segmentationSettings.frames = null;

        updateSpotRadii();
        segmentationSettings.thresholds = Utils.delimitedStringToDoubleArray(textFieldSpotThresholds.getText(), ";");
        segmentationSettings.experimentalBatch = textFieldExperimentalBatch.getText();
        segmentationSettings.experimentID = textFieldExperimentID.getText();
        segmentationSettings.treatment = textFieldTreatment.getText();
        segmentationSettings.method = (String) comboBoxSegmentationMethod.getSelectedItem();
        segmentationSettings.pathName = imp.getOriginalFileInfo().directory; // textFieldPathName.getText();
        segmentationSettings.fileName = imp.getOriginalFileInfo().fileName; // textFieldFileName.getText();
    }

    private void setSpotChannelBackgrounds()
    {
        segmentationSettings.backgrounds = new double[ numFISHChannels ];

        for ( int channelIndex = 0; channelIndex < numFISHChannels; channelIndex++ )
        {
            final int spotChannelIndex = segmentationSettings.spotChannelIndicesOneBased[ channelIndex ];
            final Double background = channelBackgrounds.get( spotChannelIndex - 1 );
            IJ.log( "Background value of channel " + spotChannelIndex + " is " + background );
            segmentationSettings.backgrounds[ channelIndex ] = background;
        }
    }

    private void setSpotChannelIndices()
    {
        segmentationSettings.spotChannelIndicesOneBased = IntStream.range( 0, channelTypes.size() )
                .filter( channel ->
                        channelTypes.get( channel ).equals( ChannelType.FISHSpots ) )
                .map( channel -> channel + 1 )
                .toArray();
    }

    private void updateSpotRadii()
    {
        segmentationSettings.spotRadii = new double[ numFISHChannels ][];
        String[] spotRadii = textFieldSpotRadii.getText().split(";");
        for ( int iChannel = 0; iChannel < numFISHChannels; iChannel++)
        {
            segmentationSettings.spotRadii[iChannel] = Utils.delimitedStringToDoubleArray(spotRadii[iChannel], ",");
        }
    }

    private void segmentSpotsAction()
    {
        // Segment
        //
        segmentationResults = Segmenter.run(
                imp,
                segmentationResults,
                segmentationSettings);

        for ( int c = 0; c < segmentationResults.models.length; c++ )
        {
            final int spotChannelIndex = segmentationSettings.spotChannelIndicesOneBased[ c ];
            IJ.log( "Detected " + segmentationResults.models[ c ].getSpots().getNSpots( false ) + " spots in channel " + spotChannelIndex );
        }

        // Construct and show overlay
        //
        segmentationOverlay = new SegmentationOverlay(
                imp,
                segmentationResults,
                segmentationSettings);

        segmentationOverlay.setTrackMateModelForVisualisationOfSelectedChannels();
        segmentationOverlay.displayTrackMateModelAsOverlay();
    }

    private void initPointRoiSelection()
    {
        RoiManager roiManager = getRoiManager();
        roiManager.runCommand(imp,"Show All");
        IJ.run("Point Tool...", "type=Hybrid color=Blue size=[Extra Large] add label");
        IJ.run(imp, "Make Composite", "");
        IJ.run("Channels Tool...");
        IJ.setTool("point");
    }

    private RoiManager getRoiManager()
    {
        RoiManager roiManager = RoiManager.getInstance();
        if (roiManager == null)
            roiManager = new RoiManager();
        else
            roiManager.runCommand("Reset");
        return roiManager;
    }

    public SpotCollection getSelectedPointsFromRoiManager()
    {
        SpotCollection spotCollection = new SpotCollection();

        RoiManager roiManager = RoiManager.getInstance();
        if (roiManager != null)
        {
            Roi[] rois = roiManager.getRoisAsArray();
            for (Roi roi : rois)
            {
                if (roi.getTypeAsString().equals("Point"))
                {
                    Calibration calibration = imp.getCalibration();

                    int radius = 1;
                    int quality = 1;

                    Spot spot = new Spot(
                            calibration.getX(roi.getXBase()),
                            calibration.getY(roi.getYBase()),
                            calibration.getZ(roi.getZPosition() - 1), // roi z-position is the slice and thus one-based
                            radius,
                            quality);

                    int frame = 0;
                    spotCollection.add(spot, frame);
                }
            }
        }

        return spotCollection;

    }

    private void addHeader(ArrayList<JPanel> panels, int iPanel, Container c, String label)
    {
        panels.add(new JPanel(new FlowLayout(FlowLayout.LEFT)));
        panels.get(iPanel).add(new JLabel(label));
        c.add(panels.get(iPanel++));
    }

    private void addTextField(ArrayList<JPanel> panels, int iPanel, Container c, JTextField textField, String textFieldLabel, String textFieldDefault)
    {
        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
        textField.setActionCommand(textFieldLabel);
        textField.addActionListener(this);
        textField.addFocusListener(this);
        textField.setText(textFieldDefault);
        panels.get(iPanel).add(new JLabel(textFieldLabel));
        panels.get(iPanel).add(textField);
        c.add(panels.get(iPanel));
    }

    private void addButton(ArrayList<JPanel> panels, int iPanel, Container c, JButton button, String buttonLabel)
    {
        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
        button.setActionCommand(buttonLabel);
        button.addActionListener(this);
        button.setText(buttonLabel);
        panels.get(iPanel).add(button);
        c.add(panels.get(iPanel));
    }

    private void addComboBox(ArrayList<JPanel> panels, int iPanel, Container c, JComboBox comboBox, String comboBoxLabel)
    {
        panels.add(new JPanel(new FlowLayout(FlowLayout.RIGHT)));
        panels.get(iPanel).add(new JLabel(comboBoxLabel));
        panels.get(iPanel).add(comboBox);
        c.add(panels.get(iPanel));
    }

    public void focusGained(FocusEvent e) {
        //
    }

    public void focusLost(FocusEvent e) {
        JTextField tf = (JTextField) e.getSource();
        if (!(tf == null)) {
            tf.postActionEvent();
        }
    }

}
