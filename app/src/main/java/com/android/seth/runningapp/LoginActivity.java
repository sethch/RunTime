package com.android.seth.runningapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Seth on 4/15/2017.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private Button buttonLogin;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewRegister;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null){
            finish();
            Intent startProfile = new Intent(LoginActivity.this, HistoryActivity.class);
            LoginActivity.this.startActivity(startProfile);
        }

        buttonLogin = (Button) findViewById(R.id.login_submit);
        editTextEmail = (EditText) findViewById(R.id.login_email_editText);
        editTextPassword = (EditText) findViewById(R.id.login_password_editText);
        textViewRegister = (TextView) findViewById(R.id.register_link);

        buttonLogin.setOnClickListener(this);
        textViewRegister.setOnClickListener(this);
    }

    /**
     * This is called upon clicking one of the LoginActivity links
     * @param v which view has been clicked
     */
    @Override
    public void onClick(View v) {
        if(v == buttonLogin){
            verifyUser();
        }
        if(v == textViewRegister){
            finish();
            Intent startRegister = new Intent(LoginActivity.this, RegisterActivity.class);
            LoginActivity.this.startActivity(startRegister);
        }
    }

    /**
     * Called by clicking login button
     * Checks Firebase database to authenticate user, then logs in or displays error message.
     */
    private void verifyUser(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Verifying User...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            finish();
                            Intent startProfile = new Intent(LoginActivity.this, HistoryActivity.class);
                            LoginActivity.this.startActivity(startProfile);
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Could not register, please try again", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }
}

// TODO: Try google authentication