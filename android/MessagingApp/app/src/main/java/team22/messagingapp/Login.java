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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;

public class Login extends AppCompatActivity {
    private OutputStream outputStream;
    private InputStream inputStream;

    public static final String KEY_ID = "_id";
    public static final String KEY_USERNAME= "username";
    public static final String KEY_PASSWORD = "password";
    private static final String DATABASE_NAME = "usersdb";
    private static final String DATABASE_TABLE = "users";

    private static SQLiteDatabase db;

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
        db = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
       // db.execSQL("DROP TABLE users;"); //Drop table is here in case I want to clear the database
        db.execSQL("CREATE TABLE IF NOT EXISTS users(username VARCHAR PRIMARY KEY, password VARCHAR, _id INTEGER);");
//        AddUser("caleb", "0003", 1);
//        AddUser("charles", "0001", 2);
//        AddUser("cho", "0002", 3);
        setContentView(R.layout.activity_login);
    }

    public long AddUser(String username, String password, Integer ID) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_USERNAME, username);
        initialValues.put(KEY_PASSWORD, password);
        initialValues.put(KEY_ID, ID);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean checkLogin(String username, String password) throws SQLException {
        String columns[] = {KEY_USERNAME, KEY_PASSWORD};
        String args[] = {username, password};
        Cursor mCursor = db.query(DATABASE_TABLE, columns, "username=? AND password=?", args, null, null, null, String.valueOf(3));
        if (mCursor != null) {
            if(mCursor.getCount() > 0)
            {
                return true;
            }
        }
        return false;
    }

    public static Integer getUserID(String user){
        String columns[] = {KEY_USERNAME, KEY_ID};
        String args[] = {user};
        Integer ID = -1;
        Cursor mCursor = db.query(DATABASE_TABLE, columns, "username=?", args, null, null, null, String.valueOf(3));
        if (mCursor.moveToFirst()) {
             ID = mCursor.getInt(mCursor.getColumnIndexOrThrow(KEY_ID));
        }
        return ID;
    }

    public void login(View view){
        mUser = (EditText)findViewById(R.id.username);
        mPin = (EditText)findViewById(R.id.pin);
        String userID = mUser.getText().toString();
        String userPIN = mPin.getText().toString();
        try{
            if(userID.length() > 0 && userPIN.length() >0) {
                if(checkLogin(userID, userPIN))
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
                    connectBT();

                }else{
                    Toast.makeText(Login.this,"Invalid Username/Password", Toast.LENGTH_LONG).show();
                }
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
            if (streamSize > 0){
                byte[]validID = new byte[streamSize];
                inputStream.read(validID);
                for(int i = 0; i < streamSize; i++){
                    System.out.println(validID[i]);
                    if (validID[i] == 1)
                        return true;
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private void sendID(String user) {
        Integer ID = Login.getUserID(user);
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
