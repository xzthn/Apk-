package com.realsil.ota;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by rain1_wen on 2016/9/26.
 */

public class BluetoothInputDeviceManager {
    public static final int PROFILE_HID = 3;// This is hide, just use it.(BluetoothClass.PROFILE_HID)
    public static final int PERIPHERAL_KEYBOARD = 0x0540;// This is hide, just use it.(BluetoothClass.PERIPHERAL_KEYBOARD)
    public static final int PERIPHERAL_POINTING = 0x0580;// This is hide, just use it.(BluetoothClass.PERIPHERAL_POINTING)
    public static final int PERIPHERAL_KEYBOARD_POINTING = 0x05C0;// This is hide, just use it.(BluetoothClass.PERIPHERAL_KEYBOARD_POINTING)
    public static final String ACTION_CONNECTION_STATE_CHANGED =
            "android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED";
    private static final String TAG = "BIDM";
    private static final boolean D = true;
    private static final String mBluetoothInputDeviceClassName = "android.bluetooth.BluetoothInputDevice";
    private static int INPUT_DEVICE = 4; // This is hide, just use it.(BluetoothProfile.INPUT_DEVICE)
    // instance
    private static BluetoothInputDeviceManager mInstance;
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Class mRCUReconnectDevice;
    private BluetoothProfile mRCUReconnectProfile;
    // SeviceListener
    BluetoothProfile.ServiceListener listener = new BluetoothProfile.ServiceListener() {

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (INPUT_DEVICE == profile) {
                try {
                    mRCUReconnectDevice = proxy.getClass().asSubclass(Class.forName(mBluetoothInputDeviceClassName));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                mRCUReconnectProfile = proxy;
                if (D) {
                    Log.i(TAG, "get Bluetooth input device proxy");
                }
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (INPUT_DEVICE == profile) {
                mRCUReconnectDevice = null;
                mRCUReconnectProfile = null;
                if (D) {
                    Log.i(TAG, "close Bluetooth input device proxy");
                }
            }
        }
    };//SeviceListener
    private BluetoothDevice mBluetoothDevice;
    private boolean isRemoveBondFirst;
    private RCUReconnectReceiver mRCUReconnectReceiver;
    private HidConnectionCallback mCallback;

    public BluetoothInputDeviceManager(Context context) {
        mContext = context;

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "initialize(): Unable to initialize BluetoothManager.");
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "initialize(): Unable to obtain a BluetoothAdapter.");
            return;
        }
        // Hid is hide, we just set INPUT_DEVICE
        if (mBluetoothAdapter.getProfileProxy(context, listener, INPUT_DEVICE)) {
            if (D) {
                Log.i(TAG, "getProfileProxy INPUT DEVICE");
            }
        } else {
            if (D) {
                Log.e(TAG, "getProfileProxy(mContext, listener, PROFILE_ID) failed");
            }
        }

        // Broadcast to receive Hid connect message
        mRCUReconnectReceiver = new RCUReconnectReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mContext.registerReceiver(mRCUReconnectReceiver, filter);
    }

    public static void initial(Context context) {
        if (D) {
            Log.d(TAG, "initial");
        }
        mInstance = new BluetoothInputDeviceManager(context);
    }

    /**
     * Get the BluetoothInputDeviceManager object.
     *
     * <p>It will return a instance.
     *
     * @return The BluetoothInputDeviceManager instance.
     */
    public static BluetoothInputDeviceManager getInstance() {
        return mInstance;
    }

    public boolean isHidDevice(final String addr) {

        return isHidDevice(mBluetoothAdapter.getRemoteDevice(addr));
    }

    // This method is not right in some times
    // While first scan a le device, the BluetoothClass mostly return Device.Major.PERIPHERAL
    private boolean isHidDevice(final BluetoothDevice devcie) {
        BluetoothClass bluetoothClass = devcie.getBluetoothClass();
        if (D) {
            Log.i(TAG, "isHidDevice(): bluetoothClass.getDeviceClass(): " + bluetoothClass.getDeviceClass());
        }

        // doesClassMatch method is not right in some times
        /*
        try {
            final Method method = bluetoothClass.getClass().getMethod("doesClassMatch", int.class);
            if (method != null) {
                final boolean success = (Boolean) method.invoke(bluetoothClass, PROFILE_HID);
                if(D) Log.d(TAG, "isHidDevice(): result: " + success);
                return success;
            }
        } catch (Exception e) {
            if(D) Log.e(TAG,"isHidDevice(): An exception occured, e = " + e);
        }*/
        boolean isHid = false;
        switch (bluetoothClass.getDeviceClass()) {
            case PERIPHERAL_KEYBOARD:
            case PERIPHERAL_KEYBOARD_POINTING:
            case PERIPHERAL_POINTING:
                isHid = true;
                break;
        }

        if (D) {
            Log.i(TAG, "isHidDevice(): isHid: " + isHid);
        }
        return isHid;
    }

    public boolean checkProfileConnect() {
        if (mRCUReconnectDevice != null) {
            return true;
        }
        if (D) {
            Log.d(TAG, "checkProfileConnect(): profile not connect");
        }
        return false;
    }

    public boolean connect(final String addr, HidConnectionCallback callback) {
        return connect(mBluetoothAdapter.getRemoteDevice(addr), callback);
    }

    private boolean connect(final BluetoothDevice devcie, HidConnectionCallback callback) {
        if (D) {
            Log.d(TAG, "connect()");
        }

        mCallback = callback;

        mBluetoothDevice = devcie;

        isRemoveBondFirst = false;

        if (!checkProfileConnect()) {
            return false;
        }

        if (devcie.getBondState() != BluetoothDevice.BOND_BONDED) {
            if (D) {
                Log.i(TAG, "connect with not bond device, bond first, current state: " + devcie.getBondState());
            }
            devcie.createBond();
        } else {
            // remove bond first
            try {
                if (D) {
                    Log.i(TAG, "connect with bonded device, remove bond first.");
                }
                final Method method = devcie.getClass().getMethod("removeBond");
                if (method != null) {
                    final boolean success = (Boolean) method.invoke(devcie);
                    if (success) {
                        isRemoveBondFirst = success;
                    } else {
                        return connectWithoutCheckBondState(devcie);
                    }
                    if (D) {
                        Log.d(TAG, "removeBond(): result: " + success);
                    }
                    return success;
                }
            } catch (Exception e) {
                if (D) {
                    Log.e(TAG, "removeBond(): An exception occured, e = " + e);
                }
            }
            return connectWithoutCheckBondState(devcie);
        }
        return false;
    }

    private boolean connectWithoutCheckBondState(final BluetoothDevice devcie) {
        /*
         * There is a connect() method in BluetoothGatt class but for now it's hidden. We will call it using reflections.
         */
        try {
            final Method connect = mRCUReconnectDevice.getMethod("connect", BluetoothDevice.class);
            if (connect != null) {
                final boolean success = (Boolean) connect.invoke(mRCUReconnectProfile, devcie);
                if (D) {
                    Log.d(TAG, "connect(): connect result: " + success);
                }
                return success;
            }
        } catch (Exception e) {
            if (D) {
                Log.e(TAG, "connect(): An exception occured while connect device, e = " + e);
            }
        }
        return false;
    }

    public int getConnectionState(final String addr) {
        return getConnectionState(mBluetoothAdapter.getRemoteDevice(addr));
    }

    public int getConnectionState(final BluetoothDevice devcie) {
        if (D) {
            Log.d(TAG, "getConnectionState()");
        }

        if (!checkProfileConnect()) {
            return -1;
        }
        /*
         * There is a getConnectionState() method in BluetoothGatt class but for now it's hidden. We will call it using reflections.
         */
        try {
            final Method getConnectionState = mRCUReconnectDevice.getMethod("getConnectionState", BluetoothDevice.class);
            if (getConnectionState
                    != null) {
                final Integer state = (Integer) getConnectionState.invoke(mRCUReconnectProfile, devcie);
                if (D) {
                    Log.d(TAG, "getConnectionState(): result: " + state);
                }
                return state;
            }
        } catch (Exception e) {
            if (D) {
                Log.e(TAG, "getConnectionState(): An exception occured, e = " + e);
            }
        }
        return -1;
    }

    public boolean isHogpConnect(final String addr) {
        return getConnectionState(addr) == BluetoothProfile.STATE_CONNECTED;
    }

    public boolean isHogpConnect(final BluetoothDevice device) {
        return getConnectionState(device) == BluetoothProfile.STATE_CONNECTED;
    }

    public void close() {
        if (mRCUReconnectDevice != null) {
            mBluetoothAdapter.closeProfileProxy(INPUT_DEVICE, mRCUReconnectProfile);
        }
        //unregiste the broadcast Receiver
        if (mRCUReconnectReceiver != null) {
            mContext.unregisterReceiver(mRCUReconnectReceiver);
            if (D) {
                Log.i(TAG, "unregisterReceiver");
            }
        }

        mCallback = null;
    }

    public static abstract class HidConnectionCallback {
        public void onConnectionStateChange(boolean connect) {

        }
    }

    // Broadcast to receive RCU reconnect broadcast
    public class RCUReconnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (D) {
                Log.i(TAG, "RCUReconnectReceiver " + action);
            }
            if (ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                int inputDeviceState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
                switch (inputDeviceState) {
                    case BluetoothProfile.STATE_CONNECTING:
                        if (D) {
                            Log.i(TAG, " Braodcast: RCU Connecting!");
                        }
                        break;

                    case BluetoothProfile.STATE_CONNECTED:
                        if (D) {
                            Log.w(TAG, " Braodcast: RCU Connected! " + mCallback);
                        }
                        if (mCallback != null) {
                            // Here need wait for a while for HID profile prepare.
                            mCallback.onConnectionStateChange(true);
                        }
                        mCallback = null;
                        mBluetoothDevice = null;
                        break;

                    case BluetoothProfile.STATE_DISCONNECTING:
                        if (D) {
                            Log.i(TAG, " Braodcast: RCU Disconnecting!");
                        }
                        break;

                    case BluetoothProfile.STATE_DISCONNECTED:
                        if (D) {
                            Log.i(TAG, " Braodcast: RCU Disconnected!");
                        }
                        if (mCallback != null) {
                            mCallback.onConnectionStateChange(false);
                        }
                        mCallback = null;
                        mBluetoothDevice = null;
                        break;


                }//switch(inputDeviceState)

            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                switch (bondState) {
                    case BluetoothDevice.BOND_NONE:
                        if (D) {
                            Log.i(TAG, " Braodcast: RCU unpaired!");
                        }
                        if (isRemoveBondFirst) {
                            isRemoveBondFirst = false;
                            mBluetoothDevice.createBond();
                        } else {
                            if (mCallback != null) {
                                mCallback.onConnectionStateChange(false);
                            }
                            mCallback = null;
                            mBluetoothDevice = null;
                        }
                        break;

                    case BluetoothDevice.BOND_BONDING:
                        if (D) {
                            Log.i(TAG, " Braodcast: RCU BONDING!");
                        }
                        break;

                    case BluetoothDevice.BOND_BONDED:
                        if (D) {
                            Log.i(TAG, " Braodcast: RCU BONDED!");
                        }
                        // Use this to check current is wait for bonded
                        if (mBluetoothDevice != null) {
                            connectWithoutCheckBondState(mBluetoothDevice);
                        }
                        break;
                }//switch(bondState)

            }
        }//onReceive
    }//RCUReconnectReceiver
}
