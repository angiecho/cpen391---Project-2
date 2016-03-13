package team22.messagingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView ViewTextMessage = (TextView) findViewById(R.id.view_message);
        ViewTextMessage.setMovementMethod(new ScrollingMovementMethod());
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        //Code for Bluetooth... Bluetooth won't work on emulator so it's commented out for now.
       /* BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }*/

    }
    public void sendMessage(View view) {
        // Do something in response to button
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();

        if (!message.equals("")){
            TextView viewText = (TextView) findViewById(R.id.view_message);

            String previousMessage = viewText.getText().toString();

            String fullMessage = previousMessage + "\n" + message;

            viewText.setText(fullMessage);
        }



        editText.setText("");

        //intent.putExtra(EXTRA_MESSAGE, message);
        //startActivity(intent);

    }
}
