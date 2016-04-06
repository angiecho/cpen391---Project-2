package team22.messagingapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class Login extends AppCompatActivity {
    EditText mUser;
    EditText mPin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(((MessagingApplication) getApplication()).checkBluetoothAllowed()){
            if (!((MessagingApplication)getApplication()).checkBluetoothEnabled()){
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, 0);
            }
            if (!((MessagingApplication) getApplication()).checkBluetoothDevice()){
                System.out.println("HELLO!");
                selectBluetoothDevice();
            }
        }
        else{
            finish();
        }

        setContentView(R.layout.activity_login);
    }

    public void login(View view){
        mUser = (EditText)findViewById(R.id.username);
        mPin = (EditText)findViewById(R.id.pin);
        String userID = mUser.getText().toString();
        //String userPIN = mPin.getText().toString();

        int resID = getResources().getIdentifier(userID, "id", getPackageName());
        if (resID < 1){
            Toast.makeText(this, "Invalid login", Toast.LENGTH_SHORT).show();
        }
        else {

            Intent chatWindow = new Intent(this, Contacts.class);
            chatWindow.putExtra("resUser", resID);
            chatWindow.putExtra("username",userID);
            mUser.setText("");
            mPin.setText("");

            startActivity(chatWindow);
        }
    }

    public void selectBluetoothDevice(){
        Set<BluetoothDevice> pairedDevices;
        pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        final ArrayList<BluetoothDevice> list = new ArrayList<>();
        ArrayList<String> namesList = new ArrayList<>();

        for(BluetoothDevice bt : pairedDevices) {
            list.add(bt);
            namesList.add(bt.getName());
        }
        if (pairedDevices.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
            builder.setTitle("Choose which device to connect:").setItems(namesList.toArray(new CharSequence[list.size()]), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // The 'which' argument contains the index position
                    // of the selected item
                    BluetoothDevice device = list.get(which);
                    ParcelUuid[] uuids = device.getUuids();
                    BluetoothSocket socket;
                    try{
                        socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                        try {
                            socket.connect();
                        }catch (IOException e){
                            e.printStackTrace();
                            try{
                                socket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                                socket.connect();
                            }catch(Exception e2){
                                e2.printStackTrace();
                            }
                        }
                        if (socket.isConnected()) {
                            System.out.println("Connected to socket!");
                            ((MessagingApplication) getApplication()).setBluetoothInformation(device, socket);

                        }
                        else {
                            System.out.println("Could not connect to socket!");
                        }

                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}
