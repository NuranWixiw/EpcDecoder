package com.essen.epcdecoder;

import android.app.Activity;
import android.app.Application;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.device.Reader;

public class EpcDecoderApplication extends Application {

    static Activity mActivity;

    // The Reader currently in use
    private Reader mReader = null;

    private boolean mIsSelectingReader = false;
    //----------------------------------------------------------------------------------------------
    // AsciiCommander message handling
    //----------------------------------------------------------------------------------------------

    /**
     * @return the current AsciiCommander
     */
    static AsciiCommander commander ;
    public static AsciiCommander getCommander() {
        return  AsciiCommander.sharedInstance();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // Ensure the shared instance of AsciiCommander exists
        AsciiCommander.createSharedInstance(getApplicationContext());
    }

    public static void setCommander(AsciiCommander commander) {
        EpcDecoderApplication.commander = commander;
    }
}
