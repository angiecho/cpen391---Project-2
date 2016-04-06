package team22.messagingapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;

public class Login extends AppCompatActivity {
    private OutputStream outputStream;
    private InputStream inputStream;
    private static final Integer SOH = 1;
    private static final Integer STX = 2;
    private static final Integer ETX = 3;

    private static SQLiteDatabase db;
    private byte[] readBuffer;
    private int readBufferPosition;
    private static final int KEY_IV_SIZE = 16;
    private static final String FORMAT = "US-ASCII";

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
        db = openOrCreateDatabase("Messages", Context.MODE_PRIVATE, null);
       // db.execSQL("DROP TABLE users;"); //Drop table is here in case I want to clear the database
        db.execSQL("CREATE TABLE IF NOT EXISTS users(username VARCHAR PRIMARY KEY, password VARCHAR, _id INTEGER);");
        Database.AddUser("caleb", "0003", 1, db);
        Database.AddUser("charles", "0001", 2, db);
        Database.AddUser("cho", "0002", 3, db);
        setContentView(R.layout.activity_login);
    }

    public void login(View view){
        mUser = (EditText)findViewById(R.id.username);
        mPin = (EditText)findViewById(R.id.pin);
        String userID = mUser.getText().toString();
        String userPIN = mPin.getText().toString();
        try{
            if(userID.length() > 0 && userPIN.length() >0) {
                if(Database.checkLogin(userID, userPIN, db))
                {
                    if (notLoggedIn(userID)) {
                        Toast.makeText(Login.this,"Successfully Logged In", Toast.LENGTH_LONG).show();
                        Intent contacts = new Intent(this, Contacts.class);
                        contacts.putExtra("username", userID);
                        startActivity(contacts);
                    }
                    else
                        Toast.makeText(Login.this, "Unable to login", Toast.LENGTH_LONG).show();

                    mUser.setText("");
                    mPin.setText("");
                }else{
                    Toast.makeText(Login.this,"Invalid Username/Password", Toast.LENGTH_LONG).show();
                }
            }
            else if (userID.length() > 0){
                Toast.makeText(Login.this, "You need to type in a password", Toast.LENGTH_LONG).show();
            }
            else if (userPIN.length() > 0){
                Toast.makeText(Login.this, "You need to type in a username", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(Login.this, "You need to type in a username/password", Toast.LENGTH_LONG).show();
            }

        }catch(Exception e){
            Toast.makeText(Login.this, "Invalid login", Toast.LENGTH_SHORT).show();
        }
    }
    private Boolean notLoggedIn(String user){
        sendID(user);
        SystemClock.sleep(250);
        try {
            Integer streamSize = inputStream.available();
            System.out.println(streamSize);
            if (streamSize > 0){
                byte[]byteArray = new byte[streamSize];
                inputStream.read(byteArray);
                System.out.println("Got an input stream back");
                for(int i = 0; i < streamSize; i++){
                    if (byteArray[i] == SOH) {
                        checkInbox(byteArray);
                        return true;
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private void checkInbox(byte[] byteArray) {
        for (int i = 2; i < byteArray.length; i++) {
            if (byteArray[i] == STX) {
                System.out.println("I have mail");
                SystemClock.sleep(250);
                getInbox(byteArray, i+1);
            }
        }
    }

    private void getInbox(byte[] byteArray, int index){
        try {
            readBufferPosition = 0;
            readBuffer = new byte[4096];
            for (int i = 0; i < byteArray.length; i++){
                System.out.print(byteArray[i] + " ");
            }
            byte[] newByteArray = new byte[byteArray.length - index];
            System.arraycopy(byteArray, index, newByteArray, 0, newByteArray.length);
            System.out.println("");

            for (int i = 0; i < newByteArray.length; i ++){
                System.out.print(newByteArray[i] + " ");
            }

            System.out.println("Here's my mail!");
            for (byte bite : newByteArray) {
                handleBite(bite);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void handleBite(byte bite) throws UnsupportedEncodingException {
        final byte delimiter = 0; //This is the ASCII code for a \0

        if (bite == delimiter) {
            handleEndOfMessage();
        }else {
            readBuffer[readBufferPosition++] = bite;
        }

    }
    private void handleEndOfMessage() throws  UnsupportedEncodingException{
        // need key, iv, header(1 byte), and message(1 >= bytes)
        System.out.println("Got a new message " + readBufferPosition);
        byte[] keyBytes = new byte[KEY_IV_SIZE];
        byte[] ivBytes = new byte[KEY_IV_SIZE];
        byte sender_receiver;
        byte[] encodedBytes = new byte[readBufferPosition-(KEY_IV_SIZE*2+1)];

        System.arraycopy(readBuffer, 0, keyBytes, 0, KEY_IV_SIZE);
        System.arraycopy(readBuffer, KEY_IV_SIZE, ivBytes, 0, KEY_IV_SIZE);
        sender_receiver = readBuffer[KEY_IV_SIZE*2];
        System.arraycopy(readBuffer, KEY_IV_SIZE*2+1, encodedBytes, 0, encodedBytes.length);

        String keyReceived = byteArrToString(keyBytes, KEY_IV_SIZE);
        String ivReceived = byteArrToString(ivBytes, KEY_IV_SIZE);
        System.out.println("Key received: " + keyReceived);
        System.out.println("IV received: " + ivReceived);

        AESEncryption.print_cipher(encodedBytes, encodedBytes.length);

        final int chunkSize = 16;

        // we expect this to be padded
        assert (encodedBytes.length % chunkSize == 0);
        int numChunks = encodedBytes.length/chunkSize;
        System.out.println("Received " + encodedBytes.length + " bytes");

        byte[][] byteChunks = new byte[numChunks][16];
        int curr = 0;

        for(int currChunk = 0; currChunk < numChunks; currChunk++) {
            int length = Math.min(chunkSize, encodedBytes.length - curr);
            System.arraycopy(encodedBytes, curr, byteChunks[currChunk], 0, length);
            curr += chunkSize;
        }

        String[] decodedByteChunks = new String[numChunks];

        for (int j = 0; j < byteChunks.length; j++){
            try {
                decodedByteChunks[j] = AESEncryption.decrypt(byteChunks[j], keyReceived, ivReceived);
                System.out.print("Decoded byte: " + decodedByteChunks[j]);
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        StringBuilder builder = new StringBuilder();
        for(String s : decodedByteChunks) {
            builder.append(s);
        }
        final String data = builder.toString().trim();

        final int receiver_id = 0xf & sender_receiver;
        final int sender_id = sender_receiver >>> 4;
        String user = mUser.getText().toString();
        boolean sent = (sender_id == Database.getUserID(user, db));

        Database.insertMessageToDatabase(sender_id, receiver_id, sent, data, db);

        System.out.println("The message inserted is: " + data);

        readBufferPosition = 0;
    }


    private String byteArrToString(byte[] bytes, int bytesLength)
            throws UnsupportedEncodingException{
        byte[] encodedBytes = new byte[bytesLength];
        System.arraycopy(bytes, 0, encodedBytes, 0, encodedBytes.length);
        return new String(encodedBytes, FORMAT);
    }
    private void sendID(String user) {
        Integer ID = Database.getUserID(user, db);
        System.out.println("Logging in:" + ID + "\n");
        connectBT();
        if (outputStream != null) {
            try {
                System.out.println("Sending ID!\n");
                outputStream.write(ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void connectBT(){
        BluetoothSocket socket = ((MessagingApplication) getApplication()).getSocket();
        if (!socket.isConnected()){
            try {
                socket.connect();
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        else {
            outputStream = ((MessagingApplication) getApplication()).getOutputStream();
            inputStream = ((MessagingApplication) getApplication()).getInputStream();
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
