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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private Button buttonRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignup;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();

        buttonRegister = (Button) findViewById(R.id.register_submit);
        editTextEmail = (EditText) findViewById(R.id.register_email_editText);
        editTextPassword = (EditText) findViewById(R.id.register_password_editText);
        textViewSignup = (TextView) findViewById(R.id.login_link);

        buttonRegister.setOnClickListener(this);
        textViewSignup.setOnClickListener(this);
    }

    /**
     * This is called upon clicking one of the RegisterActivity links
     * @param v which view has been clicked
     */
    @Override
    public void onClick(View v) {
        if(v == buttonRegister){
            registerUser();
        }
        if(v == textViewSignup){
            finish();
            Intent startLogin = new Intent(RegisterActivity.this, LoginActivity.class);
            RegisterActivity.this.startActivity(startLogin);
        }
    }

    /**
     * Called upon clicking the register button
     * Verifies correct format for email and password, then registers user
     */
    private void registerUser(){
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

        progressDialog.setMessage("Registering User...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            finish();
                            Intent startProfile = new Intent(RegisterActivity.this, ProfileActivity.class);
                            RegisterActivity.this.startActivity(startProfile);
                        }
                        else {
                            Toast.makeText(RegisterActivity.this, "Could not register, please try again", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

}

// CITATION: "Firebase Android Tutorial - Part 1 - User Registration" https://www.youtube.com/watch?v=0NFwF7L-YA8