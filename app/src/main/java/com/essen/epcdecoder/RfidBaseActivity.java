package com.essen.epcdecoder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.essen.epcdecoder.Util.rfid.InventoryModel;
import com.essen.epcdecoder.Util.rfid.ModelBase;
import com.essen.epcdecoder.Util.rfid.OnRfidReadListener;
import com.essen.epcdecoder.Util.rfid.RfidReaderListener;
import com.essen.epcdecoder.Util.rfid.WeakHandler;
import com.uk.tsl.rfid.DeviceListActivity;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.commands.BatteryStatusCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.device.ConnectionState;
import com.uk.tsl.rfid.asciiprotocol.device.IAsciiTransport;
import com.uk.tsl.rfid.asciiprotocol.device.ObservableReaderList;
import com.uk.tsl.rfid.asciiprotocol.device.Reader;
import com.uk.tsl.rfid.asciiprotocol.device.ReaderManager;
import com.uk.tsl.rfid.asciiprotocol.device.TransportType;
import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;
import com.uk.tsl.rfid.asciiprotocol.enumerations.EnumerationBase;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;
import com.uk.tsl.utils.Observable;

import static com.uk.tsl.rfid.DeviceListActivity.EXTRA_DEVICE_ACTION;
import static com.uk.tsl.rfid.DeviceListActivity.EXTRA_DEVICE_INDEX;

public class RfidBaseActivity extends AppCompatActivity implements RfidReaderListener, OnRfidReadListener {

    // Debug control
    private static final boolean D = BuildConfig.DEBUG;

    // The text view to display the RF Output Power used in RFID commands
    private TextView mPowerLevelTextView;
    // The seek bar used to adjust the RF Output Power for RFID commands
    private SeekBar mPowerSeekBar;
    // The current setting of the power level
    private int mPowerLevel = AntennaParameters.MaximumCarrierPower;
    private RfidReaderListener mRfidReaderListener;
    private OnRfidReadListener onRfidReadListener;

    InventoryModel mModel;

    @Override
    public void onReaderConnect() {
        if (mRfidReaderListener != null) {

            this.mRfidReaderListener.onReaderConnect();

        }
    }

    @Override
    public void onReaderDisconnect() {
        if (mRfidReaderListener != null) {
            this.mRfidReaderListener.onReaderDisconnect();

        }
    }

    @Override
    public void OnRead(String tag) {
        if (onRfidReadListener != null) {
            this.onRfidReadListener.OnRead(tag);
        }


    }

    public void setmRfidReaderListener(RfidReaderListener mRfidReaderListener) {
        this.mRfidReaderListener = mRfidReaderListener;
    }


    // Custom adapter for the Ascii command enumerated parameter values to display the description rather than the toString() value
    public class ParameterEnumerationArrayAdapter<T extends EnumerationBase> extends ArrayAdapter<T> {
        private final T[] mValues;

        public ParameterEnumerationArrayAdapter(Context context, int textViewResourceId, T[] objects) {
            super(context, textViewResourceId, objects);
            mValues = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            view.setText(mValues[position].getDescription());
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
            view.setText(mValues[position].getDescription());
            return view;
        }
    }

    private Databank[] mDatabanks = new Databank[]{
            Databank.ELECTRONIC_PRODUCT_CODE,
            Databank.TRANSPONDER_IDENTIFIER,
            Databank.RESERVED,
            Databank.USER
    };


    // The Reader currently in use
    private Reader mReader = null;

    private boolean mIsSelectingReader = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGenericModelHandler = new GenericHandler(this);


        final AsciiCommander commander = getCommander();

        // Ensure that all existing responders are removed
        commander.clearResponders();

        // Add the LoggerResponder - this simply echoes all lines received from the reader to the log
        // and passes the line onto the next responder
        // This is ADDED FIRST so that no other responder can consume received lines before they are logged.
        commander.addResponder(new LoggerResponder());

        // Add responder to enable the synchronous commands
        commander.addSynchronousResponder();

        // Configure the ReaderManager when necessary
        ReaderManager.create(getApplicationContext());

        mModel = new InventoryModel();
        mModel.setCommander(getCommander());
        mModel.setHandler(mGenericModelHandler);

        // Add observers for changes
        ReaderManager.sharedInstance().getReaderList().readerAddedEvent().addObserver(mAddedObserver);
        ReaderManager.sharedInstance().getReaderList().readerUpdatedEvent().addObserver(mUpdatedObserver);
        ReaderManager.sharedInstance().getReaderList().readerRemovedEvent().addObserver(mRemovedObserver);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove observers for changes
        ReaderManager.sharedInstance().getReaderList().readerAddedEvent().removeObserver(mAddedObserver);
        ReaderManager.sharedInstance().getReaderList().readerUpdatedEvent().removeObserver(mUpdatedObserver);
        ReaderManager.sharedInstance().getReaderList().readerRemovedEvent().removeObserver(mRemovedObserver);
    }


    //----------------------------------------------------------------------------------------------
    // Pause & Resume life cycle
    //----------------------------------------------------------------------------------------------

    @Override
    public synchronized void onPause() {
        super.onPause();






    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        // Register to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(this).registerReceiver(mCommanderMessageReceiver, new IntentFilter(AsciiCommander.STATE_CHANGED_NOTIFICATION));

        // Remember if the pause/resume was caused by ReaderManager - this will be cleared when ReaderManager.onResume() is called
        boolean readerManagerDidCauseOnPause = ReaderManager.sharedInstance().didCauseOnPause();

        // The ReaderManager needs to know about Activity lifecycle changes
        ReaderManager.sharedInstance().onResume();

        // The Activity may start with a reader already connected (perhaps by another App)
        // Update the ReaderList which will add any unknown reader, firing events appropriately
        ReaderManager.sharedInstance().updateList();

        // Locate a Reader to use when necessary
        AutoSelectReader(!readerManagerDidCauseOnPause);

        mIsSelectingReader = false;

        displayReaderState();

    }

    private void AutoSelectReader(boolean attemptReconnect) {
        ObservableReaderList readerList = ReaderManager.sharedInstance().getReaderList();
        Reader usbReader = null;
        if (readerList.list().size() >= 1) {
            // Currently only support a single USB connected device so we can safely take the
            // first CONNECTED reader if there is one
            for (Reader reader : readerList.list()) {
                if (reader.hasTransportOfType(TransportType.USB)) {
                    usbReader = reader;
                    break;
                }
            }
        }

        if (mReader == null) {
            if (usbReader != null) {
                // Use the Reader found, if any
                mReader = usbReader;
                getCommander().setReader(mReader);
            }
        } else {
            // If already connected to a Reader by anything other than USB then
            // switch to the USB Reader
            IAsciiTransport activeTransport = mReader.getActiveTransport();
            if (activeTransport != null && activeTransport.type() != TransportType.USB && usbReader != null) {
                mReader.disconnect();

                mReader = usbReader;

                // Use the Reader found, if any
                getCommander().setReader(mReader);
            }
        }

        // Reconnect to the chosen Reader
        if (mReader != null
                && !mReader.isConnecting()
                && (mReader.getActiveTransport() == null || mReader.getActiveTransport().connectionStatus().value() == ConnectionState.DISCONNECTED)) {
            // Attempt to reconnect on the last used transport unless the ReaderManager is cause of OnPause (USB device connecting)
            if (attemptReconnect) {
                if (mReader.allowMultipleTransports() || mReader.getLastTransportType() == null) {
                    // Reader allows multiple transports or has not yet been connected so connect to it over any available transport
                    mReader.connect();
                } else {
                    // Reader supports only a single active transport so connect to it over the transport that was last in use
                    mReader.connect(mReader.getLastTransportType());
                }
            }
        }
    }

    // ReaderList Observers
    Observable.Observer<Reader> mAddedObserver = new Observable.Observer<Reader>() {
        @Override
        public void update(Observable<? extends Reader> observable, Reader reader) {
            // See if this newly added Reader should be used
            AutoSelectReader(true);
        }
    };

    Observable.Observer<Reader> mUpdatedObserver = new Observable.Observer<Reader>() {
        @Override
        public void update(Observable<? extends Reader> observable, Reader reader) {
        }
    };

    Observable.Observer<Reader> mRemovedObserver = new Observable.Observer<Reader>() {
        @Override
        public void update(Observable<? extends Reader> observable, Reader reader) {
            // Was the current Reader removed
            if (reader == mReader) {
                mReader = null;

                // Stop using the old Reader
                getCommander().setReader(mReader);
            }
        }
    };


    //----------------------------------------------------------------------------------------------
    // UI state and display update
    //----------------------------------------------------------------------------------------------

    private void displayReaderState() {
        String connectionMsg = "Reader: ";
        switch (getCommander().getConnectionState()) {
            case CONNECTED:
                connectionMsg += getCommander().getConnectedDeviceName();
                break;
            case CONNECTING:
                connectionMsg += "Connecting...";
                break;
            default:
                connectionMsg += "Disconnected";
        }
        setTitle(connectionMsg);
    }

    //----------------------------------------------------------------------------------------------
    // AsciiCommander message handling
    //----------------------------------------------------------------------------------------------

    /**
     * @return the current AsciiCommander
     */
    public AsciiCommander getCommander() {
        return EpcDecoderApplication.getCommander();
    }

    //
    // Handle the messages broadcast from the AsciiCommander
    //
    private BroadcastReceiver mCommanderMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (D) {
                Log.d(getClass().getName(), "AsciiCommander state changed - isConnected: " + getCommander().isConnected());
            }

            String connectionStateMsg = intent.getStringExtra(AsciiCommander.REASON_KEY);
            Toast.makeText(context, connectionStateMsg, Toast.LENGTH_SHORT).show();

            displayReaderState();

            if (getCommander().isConnected()) {

                onReaderConnect();

            } else if (getCommander().getConnectionState() == ConnectionState.DISCONNECTED) {
                // A manual disconnect will have cleared mReader
                if (mReader != null) {
                    // See if this is from a failed connection attempt
                    if (!mReader.wasLastConnectSuccessful()) {
                        // Unable to connect so have to choose reader again
                        mReader = null;
                        onReaderDisconnect();
                    }
                }

            }


        }
    };


    //
    // Handle reset controls
    //
    private void resetReader() {
        try {
            // Reset the reader
            FactoryDefaultsCommand fdCommand = FactoryDefaultsCommand.synchronousCommand();
            getCommander().executeCommand(fdCommand);
            String msg = "Reset " + (fdCommand.isSuccessful() ? "succeeded" : "failed");
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updatePowerSetting(int level) {
        mPowerLevel = level;
        mPowerLevelTextView.setText(mPowerLevel + " dBm");
    }


    //
    // Handle Intent results
    //
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DeviceListActivity.SELECT_DEVICE_REQUEST:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    int readerIndex = data.getExtras().getInt(EXTRA_DEVICE_INDEX);
                    Reader chosenReader = ReaderManager.sharedInstance().getReaderList().list().get(readerIndex);

                    int action = data.getExtras().getInt(EXTRA_DEVICE_ACTION);

                    // If already connected to a different reader then disconnect it
                    if (mReader != null) {
                        if (action == DeviceListActivity.DEVICE_CHANGE || action == DeviceListActivity.DEVICE_DISCONNECT) {
                            mReader.disconnect();
                            if (action == DeviceListActivity.DEVICE_DISCONNECT) {
                                mReader = null;

                                onReaderDisconnect();
                            }
                        }
                    }

                    // Use the Reader found
                    if (action == DeviceListActivity.DEVICE_CHANGE || action == DeviceListActivity.DEVICE_CONNECT) {
                        mReader = chosenReader;
                        getCommander().setReader(mReader);
                    }
                }
                break;
        }
    }

    public void OpenRfidSelectDialog() {
// Launch the DeviceListActivity to see available Readers
        mIsSelectingReader = true;
        int index = -1;
        if (mReader != null) {
            index = ReaderManager.sharedInstance().getReaderList().list().indexOf(mReader);
        }
        Intent selectIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
        if (index >= 0) {
            selectIntent.putExtra(EXTRA_DEVICE_INDEX, index);
        }
        startActivityForResult(selectIntent, DeviceListActivity.SELECT_DEVICE_REQUEST);
    }

    public void setRfidReadListener(OnRfidReadListener mRfidReadListener) {
        this.onRfidReadListener = mRfidReadListener;
    }

    public boolean isReaderActive() {
        return getCommander().isConnected();
    }
    //----------------------------------------------------------------------------------------------
    // Model notifications
    //----------------------------------------------------------------------------------------------

    private static class GenericHandler extends WeakHandler<RfidBaseActivity> {
        public GenericHandler(RfidBaseActivity t) {
            super(t);
        }

        @Override
        public void handleMessage(Message msg, RfidBaseActivity t) {
            try {
                switch (msg.what) {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        //TODO: process change in model busy state
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        // Examine the message for prefix
                        String message = (String) msg.obj;

                        t.OnRead(message);


                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
            }

        }
    }

    ;

    // The handler for model messages
    private static GenericHandler mGenericModelHandler;

    public void setReaderEnable(boolean isEnable){
        mModel.setEnabled(isEnable);
    }

    public int getBatteryLevel(){
        // Report the battery level when Reader connects
        BatteryStatusCommand bCommand = BatteryStatusCommand.synchronousCommand();
        getCommander().executeCommand(bCommand);
        return  bCommand.getBatteryLevel();

    }
}
