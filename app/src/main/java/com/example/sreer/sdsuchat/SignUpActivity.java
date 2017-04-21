package com.example.sreer.sdsuchat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {

    private Button done;
    private String message;
    private EditText nickname, city, password, latitude, longitude, email;
    private Spinner stateSpinner, countrySpinner, yearSpinner;
    private TextView gpsSetLink;
    private List<String> countryList = new ArrayList<String>();
    private List<String> stateList = new ArrayList<String>();
    private ArrayList<String> yearList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    private AlertDialog dialog;

    private static final int MAP_SETTER = 456;
    VolleySingleton requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        countrySpinner = (Spinner) findViewById(R.id.Spinner_country);
        stateSpinner = (Spinner) findViewById(R.id.Spinner_state);
        nickname = (EditText) findViewById(R.id.EditText_nickname);
        password = (EditText) findViewById(R.id.EditText_password);
        gpsSetLink = (TextView) findViewById(R.id.TextView_gps_set);
        city = (EditText) findViewById(R.id.EditText_city);
        email = (EditText) findViewById(R.id.EditText_signUpemail);
        yearSpinner = (Spinner) findViewById(R.id.Spinner_year);
        done = (Button) findViewById(R.id.Button_submit);
        latitude = (EditText) findViewById(R.id.EditText_latitude);
        longitude = (EditText) findViewById(R.id.EditText_longitude);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        countryListLoader();
        yearLoader();
        done.setOnClickListener(new View.OnClickListener() {
            @Override
           public void onClick(View v) {
               validateUserDetails();
                boolean isError=submitUserDetails();
                if(!isError)
                new Registeration().execute();
            }
        });
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String data = String.valueOf(countrySpinner.getItemAtPosition(position));
                if (!(data.equals("Select Country"))){
                    countrySpinner.setBackgroundColor(Color.TRANSPARENT);
                }
                stateListLoader(String.valueOf(parentView.getItemAtPosition(position)));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                stateSpinner.setAdapter(null);

            }});
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String data = String.valueOf(stateSpinner.getItemAtPosition(position));
                if (!(data.equals("Select State"))){
                    stateSpinner.setBackgroundColor(Color.TRANSPARENT);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }});
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String data = String.valueOf(yearSpinner.getItemAtPosition(position));
                if (!(data.equals("Select Year"))){
                    yearSpinner.setBackgroundColor(Color.TRANSPARENT);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }});
        gpsSetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent map = new Intent(SignUpActivity.this,MapSetterActivity.class);
                startActivityForResult(map,MAP_SETTER);
            }
        });

        latitude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent map = new Intent(SignUpActivity.this,MapSetterActivity.class);
                startActivityForResult(map,MAP_SETTER);
            }
        });

        longitude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent map = new Intent(SignUpActivity.this,MapSetterActivity.class);
                startActivityForResult(map,MAP_SETTER);
            }
        });
//        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if(hasFocus){
//
//                }else {
//                    if(isEmailValid(String.valueOf(email.getText()))){
//                        email.setError(null);
//                    }
//                }
//            }
//        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MAP_SETTER)
            switch (resultCode) {
                case RESULT_OK:
                    latitude.setText(data.getStringExtra("latitude"));
                    longitude.setText(data.getStringExtra("longitude"));
                    break;
                case RESULT_CANCELED:
                    break;

            }
    }


    public void countryListLoader() {
        String url = "http://bismarck.sdsu.edu/hometown/countries";
        countryList.add("Select Country");
        stateList.add("Select State");
        VolleySingleton requestQueue = VolleySingleton.getInstance(getApplicationContext());
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                Log.i("test", String.valueOf(response.length()));
                for (int i = 0; i < response.length(); i++)
                    try {
                        countryList.add(response.getString(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                ArrayAdapter<String> adapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_spinner_item, countryList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                countrySpinner.setAdapter(adapter);
                ArrayAdapter<String> stateadapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_spinner_item, stateList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                stateSpinner.setAdapter(stateadapter);
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
            }
        };
        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        requestQueue.addToRequestQueue(getRequest);
    }


    public void stateListLoader(String country) {
        String url = "http://bismarck.sdsu.edu/hometown/states?country=" + country;
        VolleySingleton requestQueue = VolleySingleton.getInstance(getApplicationContext());
        stateList.clear();
        stateList.add("Select State");
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++)
                    try {
                        stateList.add(response.getString(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                ArrayAdapter<String> adapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_spinner_item, stateList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                stateSpinner.setAdapter(adapter);
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
            }
        };
        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        requestQueue.addToRequestQueue(getRequest);
    }

    public void yearLoader() {
        yearList.add("Select Year");
        for (int i = 1970; i <= 2017; i++)
            yearList.add(String.valueOf(i));
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, yearList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);
    }

    public boolean submitUserDetails() {
        JSONObject data = new JSONObject();
        try {

            if (isNameEmpty(String.valueOf(nickname.getText()))) {
                nickname.setError("Nickname is mandatory");
                return true;
            }

            isNameUnique(String.valueOf(nickname.getText()));

            if (isWhiteSpaceInName(String.valueOf(nickname.getText()))) {
                nickname.setError("White space not allowed in nickname");
                return true;
            }

            if (isPasswordEmpty(String.valueOf(password.getText()))) {
                password.setError("Password is mandatory");
                return true;
            }

            if (isPasswordLengthLessThan3(String.valueOf(password.getText()))) {
                password.setError("Password length should be more than 3");
                return true;
            }

            if (isPasswordWeek(String.valueOf(password.getText()))){
                password.setError("Password should be greater than 5 characters for firebase");
                return true;
            }

            if (isEmailEmpty(String.valueOf(email.getText()))) {
                email.setError("Email is mandatory");
                return true;
            }

            if (isEmailValid(String.valueOf(email.getText()))) {
                email.setError("Invalid Email Address");
                return true;
            }

            if (isCountryEmpty(String.valueOf(countrySpinner.getSelectedItem()))) {
                //country.setError("Country is mandatory");
                Toast.makeText(this, "Country is mandatory", Toast.LENGTH_SHORT).show();
                countrySpinner.setBackgroundColor(Color.RED);
                return true;
            }

            if (isStateEmpty(String.valueOf(stateSpinner.getSelectedItem()))) {
                //state.setError("State is mandatory");
                Toast.makeText(this, "State is mandatory", Toast.LENGTH_SHORT).show();
                stateSpinner.setBackgroundColor(Color.RED);
                return true;
            }

            if (isCityEmpty(String.valueOf(city.getText()))) {
                city.setError("City is mandatory");
                return true;
            }
            if (isYearEmpty(String.valueOf(yearSpinner.getSelectedItem()))) {
                //year.setError("Year is mandatory");
                Toast.makeText(this, "Year is mandatory", Toast.LENGTH_SHORT).show();
                yearSpinner.setBackgroundColor(Color.RED);
                return true;
            }

            if (isYearInRange(String.valueOf(yearSpinner.getSelectedItem()))) {
                //year.setError("Year should be between 1970 and 2017");
                Toast.makeText(this, "Year should be between 1970 and 2017", Toast.LENGTH_SHORT).show();
                yearSpinner.setBackgroundColor(Color.RED);
                return true;
            }

            progressDialog.setMessage("Registering...Please Wait...");
            progressDialog.show();
            data.put("password", password.getText());
            data.put("country", String.valueOf(countrySpinner.getSelectedItem()));
            data.put("nickname", nickname.getText());
            if (!(latitude.getText().toString() == null || latitude.getText().toString().isEmpty()))
                data.put("latitude", Double.valueOf(latitude.getText().toString()));
            if (!(longitude.getText().toString() == null || longitude.getText().toString().isEmpty()))
                data.put("longitude", Double.valueOf(longitude.getText().toString()));
            data.put("city", city.getText());
            data.put("year", Integer.valueOf(String.valueOf(yearSpinner.getSelectedItem())));
            data.put("state", String.valueOf(stateSpinner.getSelectedItem()));
        } catch (JSONException error) {
            Log.e("rew", "JSON error", error);
        }
        String url = " http://bismarck.sdsu.edu/hometown/adduser";
        requestQueue = VolleySingleton.getInstance(
                getApplicationContext()
        );

        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("response", response.toString());
                try {
                    if (!(response.length() <= 0))
                        message = response.getString("message");
                    else
                        message = response.toString();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Some Error Occured. Try Again", Toast.LENGTH_SHORT).show();
                    //e.printStackTrace();
                }
                if (message.equals("ok"))
                    Log.i("test","Data Saved Successfully");
                    //Toast.makeText(getApplicationContext(), "Data Saved Successfully", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("response", "post fail " + new String(error.networkResponse.data));
                Toast.makeText(getApplicationContext(), "Failed: " + new String(error.networkResponse.data), Toast.LENGTH_SHORT).show();
            }
        };

        JsonObjectRequest postRequest = new JsonObjectRequest(url, data, success, failure);
        requestQueue.addToRequestQueue(postRequest);
        return false;
    }

    public void validateUserDetails() {

        if (isNameEmpty(String.valueOf(nickname.getText()))) {
            nickname.setError("Nickname is mandatory");
        }


        if (isPasswordEmpty(String.valueOf(password.getText()))) {
            password.setError("Password is mandatory");

        }

        if (isPasswordLengthLessThan3(String.valueOf(password.getText()))) {
            password.setError("Password length should be more than 3");
        }

        if (isPasswordWeek(String.valueOf(password.getText()))){
            password.setError("Password should be greater than 5 characters for firebase");
        }

        if (isEmailEmpty(String.valueOf(email.getText()))) {
            email.setError("Email is mandatory");
        }

        if (isEmailValid(String.valueOf(email.getText()))) {
            email.setError("Invalid Email address");
        }

        if (isCountryEmpty(String.valueOf(countrySpinner.getSelectedItem()))) {
            //country.setError("Country is mandatory");
            countrySpinner.setBackgroundColor(Color.RED);
        }

        if (isStateEmpty(String.valueOf(stateSpinner.getSelectedItem()))) {
            //state.setError("State is mandatory");
            stateSpinner.setBackgroundColor(Color.RED);
        }

        if (isCityEmpty(String.valueOf(city.getText()))) {
            city.setError("City is mandatory");
        }

        if (isYearEmpty(String.valueOf(yearSpinner.getSelectedItem()))) {
            //yearSpinner.setError("Year is mandatory");
            yearSpinner.setBackgroundColor(Color.RED);
        }

    }


    public boolean isNameEmpty(String nickname) {
        return nickname.isEmpty();
    }

    public boolean isNameUnique(final String name) {
        String nameCheckURL = "http://bismarck.sdsu.edu/hometown/nicknameexists?name=" + name;
        requestQueue = VolleySingleton.getInstance(getApplicationContext());
        StringRequest req = new StringRequest(Request.Method.GET, nameCheckURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("rew", response);
                        if (response.equals("true"))
                            nickname.setError("Nickname already exist");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("error", error.toString());
                    }
                }) {
        };
        requestQueue.addToRequestQueue(req);
        return false;
    }

    public boolean isCityEmpty(String city) {
        return city.isEmpty();
    }

    public boolean isPasswordEmpty(String password) {
        return password.isEmpty();
    }

    public boolean isYearEmpty(String year) {
        return year.isEmpty() || year.equals("Select Year");
    }

    public boolean isStateEmpty(String state) {
        return state.isEmpty() || state.equals("Select State");
    }

    public boolean isCountryEmpty(String country) {
        return country.isEmpty() || country.equals("Select Country");
    }

    public boolean isYearInRange(String year) {
        return !(1970 <= Integer.valueOf(year) && Integer.valueOf(year) <= 2017);
    }

    public boolean isPasswordLengthLessThan3(String password) {
        return password.length() < 3;
    }

    public boolean isWhiteSpaceInName(String nickname) {
        return nickname.contains(" ");
    }

    public boolean isEmailEmpty(String email) {
        return email.isEmpty();
    }

    public boolean isEmailValid(String email){
        return !email.contains("@");
    }

    public boolean isPasswordWeek(String password){
        return password.length()<6;
    }

    public void firebaseUserRegisteration() {
        Log.i("email id",String.valueOf(email.getText()));
        Log.i("password",String.valueOf(password.getText()));
        firebaseAuth.createUserWithEmailAndPassword(String.valueOf(email.getText()), String.valueOf(password.getText()))
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                            Log.i("test","Registeration Successful");
                            //Toast.makeText(getApplicationContext(), "Registeration Successful", Toast.LENGTH_SHORT).show();
                            Intent app = new Intent(SignUpActivity.this,ListAndFilterActivity.class);
                            startActivity(app);
                            finish();
                        } else {
                            Log.i("test","Registeration Failed");
                            //Toast.makeText(getApplicationContext(), "Registeration Failed", Toast.LENGTH_SHORT).show();
                            showAlertDialog(task.getException().getMessage(), true);
                        }

                    }
                });

    }

    private void onAuthSuccess(FirebaseUser user) {
        createNewUser(user.getUid());
    }

    private void createNewUser(String userId){
        User user = buildNewUser();
        mDatabase.child("users").child(userId).setValue(user);
        saveMetaData(userId,user.getCreatedAt());
    }

    private void showAlertDialog(String message, boolean isCancelable){

        dialog = ChatHelper.buildAlertDialog(getString(R.string.login_error_title),message,isCancelable,SignUpActivity.this);
        dialog.show();
    }

    private User buildNewUser() {
       User user =  new User(
                getUserDisplayName(),
                getUserEmail(),
                UsersChatAdapter.ONLINE,
                ChatHelper.generateRandomAvatarForUser(),
                new Date().getTime()
        );
        if (!(latitude.getText().toString() == null || latitude.getText().toString().isEmpty()))
        user.setLatitude(Double.valueOf(latitude.getText().toString()));
        if (!(longitude.getText().toString() == null || longitude.getText().toString().isEmpty()))
        user.setLongitude(Double.valueOf(longitude.getText().toString()));
        user.setYear(Integer.valueOf(yearSpinner.getSelectedItem().toString()));
        return user;
    }

    private String getUserDisplayName() {
        return nickname.getText().toString().trim();
    }

    private void saveMetaData(String userId,Long dateTime){
        User metaData = new User();
        metaData.setNickname(nickname.getText().toString());
        metaData.setEmail(email.getText().toString());
        metaData.setFirebaseID(userId);
        metaData.setCreatedAt(dateTime);
        if (!(latitude.getText().toString() == null || latitude.getText().toString().isEmpty()))
        metaData.setLatitude(Double.valueOf(latitude.getText().toString()));
        if (!(longitude.getText().toString() == null || longitude.getText().toString().isEmpty()))
        metaData.setLongitude(Double.valueOf(longitude.getText().toString()));
        metaData.setYear(Integer.valueOf(yearSpinner.getSelectedItem().toString()));
        mDatabase.child("meta-data").child(nickname.getText().toString()).setValue(metaData);
        mDatabase.child("Identifier").child(metaData.cleanEmailAddress(email.getText().toString())).setValue(metaData);
    }

    private String getUserEmail() {
        return email.getText().toString().trim();
    }

    class Registeration extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            firebaseUserRegisteration();
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Runs on the UI thread before doInBackground
            // Good for toggling visibility of a progress indicator
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

}
