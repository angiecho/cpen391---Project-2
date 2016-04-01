package team22.messagingapp;

import android.app.Application;
import android.bluetooth.*;

import java.io.*;

public class MessagingApplication extends Application {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public boolean checkBluetoothAllowed(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null){
            return false;
        }
        return true;
    }

    public boolean checkBluetoothEnabled(){
        return bluetoothAdapter.isEnabled();
    }

    public boolean checkBluetoothDeviceExists(){
        return (device != null);
    }

    public void setBluetoothInformation(BluetoothDevice device, BluetoothSocket socket){
        this.device = device;
        this.socket = socket;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public OutputStream getOutputStream(){
        return outputStream;
    }

    public InputStream getInputStream(){
        return inputStream;
    }

    public BluetoothSocket getSocket(){
        return socket;
    }
}
