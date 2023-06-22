package de.embl.cba.fish;

import ij.ImagePlus;
import ij.gui.NonBlockingGenericDialog;

import java.util.ArrayList;
import java.util.Arrays;

public class ChannelConfigDialog
{
    public enum ChannelType
    {
        FISHSpots,
        Other
    }

    private final ImagePlus imp;

    public ChannelConfigDialog( ImagePlus imp )
    {
        this.imp = imp;
    }

    public ArrayList< ChannelType > showDialogToGetChannelTypes()
    {
        final int nChannels = imp.getNChannels();

        final NonBlockingGenericDialog gd = new NonBlockingGenericDialog( "Channel Setup" );
        addChannelTypesDialogs( gd, nChannels );

        Utils.addHelpButton( gd, "channel-setup" );

        gd.showDialog();
        if ( gd.wasCanceled() ) return null;

        final ArrayList< ChannelType > channelTypes = getChannelTypes( gd, nChannels );
        return channelTypes;
    }

    private ArrayList< ChannelType > getChannelTypes( NonBlockingGenericDialog gd, int nChannels )
    {
        final ArrayList< ChannelType > channelTypes = new ArrayList< ChannelType >();
        for ( int i = 0; i < nChannels; i++ )
        {
            channelTypes.add( ChannelType.valueOf( gd.getNextChoice() ) );
        }
        return channelTypes;
    }

    private void addChannelTypesDialogs( NonBlockingGenericDialog gd, int nChannels )
    {
        for ( int i = 0; i < nChannels; i++ )
        {
            gd.addChoice( "Channel " + ( i + 1 ), getNames( ChannelType.class ), ChannelType.FISHSpots.toString() );
        }
    }

    public static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

}
