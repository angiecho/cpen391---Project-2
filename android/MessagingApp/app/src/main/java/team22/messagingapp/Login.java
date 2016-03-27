package team22.messagingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity {
    EditText mUser;
    EditText mPin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
