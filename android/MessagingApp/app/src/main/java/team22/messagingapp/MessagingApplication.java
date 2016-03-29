package team22.messagingapp;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by kwando1313 on 3/28/16.
 */
public class MessagingApplication extends Application {
    BluetoothAdapter BA;
    BluetoothDevice device;
    BluetoothSocket socket;
    InputStream inputStream;
    OutputStream outputStream;

    public boolean checkBluetoothAllowed(){
        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA == null){
            return false;
        }
        return true;
    }

    public boolean checkBluetoothEnabled(){
        if (!BA.isEnabled()) {
            return false;
        }
        return true;
    }

    public boolean checkBluetoothDevice(){
        if (device == null){
            return false;
        }
        return true;
    }

    public void setBluetoothInformation(BluetoothDevice d, BluetoothSocket s){
        device = d;
        socket = s;
        try{
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("NONONONONOONO");
            //This shouldn't ever happen, but... Just in case...
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
