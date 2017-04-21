package com.example.sreer.sdsuchat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {

    private TextView signUpLink;
    private EditText editTextEmail,editTextPassword;
    private Button buttonLogin;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signUpLink = (TextView) findViewById(R.id.TextView_SignUpLink);
        editTextEmail = (EditText) findViewById(R.id.autoComplete_loginEmail);
        editTextPassword = (EditText) findViewById(R.id.EditText_LoginPassword);
        buttonLogin = (Button) findViewById(R.id.Button_SignIn);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });
        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignUpActivity();
            }
        });
    }


    public void startSignUpActivity(){
        Intent signUp = new Intent(this,SignUpActivity.class);
        startActivity(signUp);
    }

    public void userLogin(){
        String email = String.valueOf(editTextEmail.getText());
        String password = String.valueOf(editTextPassword.getText());
        if(validate(email,password))
            return;
        progressDialog.setMessage("Signing in...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    setUserOnline();
                    Log.i("test","SignIn Successful");
                    //Toast.makeText(getApplicationContext(), "SignIn Successful", Toast.LENGTH_SHORT).show();
                    Intent app = new Intent(LoginActivity.this,ListAndFilterActivity.class);
                    startActivity(app);
                    editTextPassword.setText(null);

                } else {
                    Log.i("test","SignIn Failed");
                    //Toast.makeText(getApplicationContext(), "SignIn Failed", Toast.LENGTH_SHORT).show();
                    showAlertDialog(task.getException().getMessage(),true);
                }

            }
        });
    }

    private void setUserOnline() {
        if(firebaseAuth.getCurrentUser()!=null ) {
            String userId = firebaseAuth.getCurrentUser().getUid();
            FirebaseDatabase.getInstance()
                    .getReference().
                    child("users").
                    child(userId).
                    child("connection").
                    setValue(UsersChatAdapter.ONLINE);
        }
    }

    private void showAlertDialog(String message, boolean isCancelable){
        dialog = ChatHelper.buildAlertDialog(getString(R.string.login_error_title), message,isCancelable,LoginActivity.this);
        dialog.show();
    }

    public boolean validate(String email,String password){
        if(email.isEmpty()) {
            editTextEmail.setError("Please enter a valid Email Address");
            return true;
        }
        if(!(email.contains("@"))) {
            editTextEmail.setError(("Email address is Invalid"));
            return true;
        }
        if(password.isEmpty()) {
            editTextPassword.setError("Please enter Password");
            return true;
        }
        return false;
    }
}

