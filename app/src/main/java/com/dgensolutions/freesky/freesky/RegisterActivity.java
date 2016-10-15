package com.dgensolutions.freesky.freesky;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicommons.people.contact.Contact;
import com.dgensolutions.freesky.freesky.app.AppConfig;
import com.dgensolutions.freesky.freesky.app.AppController;
import com.dgensolutions.freesky.freesky.helper.SQLiteHandler;
import com.dgensolutions.freesky.freesky.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Ganesh Kaple on 13-10-2016.
 */

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "com.dgensolutions.freesky.freesky.RegisterActivity";
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputPhone;
    private EditText inputPassword;
    private EditText inputPasswordAgain;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFullName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPhone = (EditText) findViewById(R.id.phone);
        inputPassword = (EditText) findViewById(R.id.password);
        inputPasswordAgain = (EditText) findViewById(R.id.password_again);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String password_again = inputPasswordAgain.getText().toString().trim();
                String phone = inputPhone.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(RegisterActivity.this, "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(password_again)) {
                    Toast.makeText(RegisterActivity.this, "Password Doesn't Match!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(phone.length() != 10) {
                    Toast.makeText(RegisterActivity.this, "Phone Number is not valid", Toast.LENGTH_SHORT).show();
                    return;
                }

                registerUser(name, email, phone, password);

            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String name, final String email,
                              final String password, final String phone) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);

                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String phone = user
                                .getString("phone");

                        // Inserting row in users table
                        db.addUser(name, email, uid, phone);


                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        appLozicLogin(uid,name,email, phone);
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "" + e, Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @SuppressLint("LongLogTag")
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })
        {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("email", email);
                params.put("phone", phone);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue



        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void appLozicLogin(String uid, String name, String email, String phone) {
        UserLoginTask.TaskListener listener = new UserLoginTask.TaskListener() {

            @Override
            public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                //After successful registration with Applozic server the callback will come here
                // Launch login activity
                PushNotificationTask pushNotificationTask = null;
                PushNotificationTask.TaskListener listener=  new PushNotificationTask.TaskListener() {
                    @Override
                    public void onSuccess(RegistrationResponse registrationResponse) {

                    }
                    @Override
                    public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

                    }

                };
                pushNotificationTask = new PushNotificationTask(Applozic.getInstance(context).getDeviceRegistrationId(),listener,context);
                pushNotificationTask.execute((Void)null);

                buildContactData();
                Intent intent = new Intent(
                        RegisterActivity.this,
                        LoginActivity.class);
                startActivity(intent);
                finish();

            }
            private void buildContactData() {

                Context context = getApplicationContext();
                AppContactService appContactService = new AppContactService(context);
                // avoid each time update ....
                if (!appContactService.isContactExists("adarshk")) {

                    List<Contact> contactList = new ArrayList<Contact>();
                    //Adarsh....
                    Contact contact = new Contact();
                    contact.setUserId("adarshk");
                    contact.setFullName("John");
                    contact.setImageURL("R.drawable.couple");
                    contactList.add(contact);

                    Contact contactRaj = new Contact();
                    contactRaj.setUserId("raj");
                    contactRaj.setFullName("rajni");
                    contactRaj.setImageURL("https://fbcdn-profile-a.akamaihd.net/hprofile-ak-xap1/v/t1.0-1/p200x200/12049601_556630871166455_1647160929759032778_n.jpg?oh=7ab819fc614f202e144cecaad0eb696b&oe=56EBA555&__gda__=1457202000_85552414c5142830db00c1571cc50641");
                    contactList.add(contactRaj);


                    //Adarsh
                    Contact contact2 = new Contact();
                    contact2.setUserId("rathan");
                    contact2.setFullName("Liz");
                    contact2.setImageURL("R.drawable.liz");
                    contactList.add(contact2);

                    Contact contact3 = new Contact();
                    contact3.setUserId("clem");
                    contact3.setFullName("Clement");
                    contact3.setImageURL("R.drawable.shivam");
                    contactList.add(contact3);

                    Contact contact4 = new Contact();
                    contact4.setUserId("shanki.gupta");
                    contact4.setFullName("Bill");
                    contact4.setImageURL("R.drawable.contact_shanki");
                    contactList.add(contact4);

                    Contact contact6 = new Contact();
                    contact6.setUserId("krishna");
                    contact6.setFullName("Krishi");
                    contact6.setImageURL("R.drawable.girl");
                    contactList.add(contact6);

                    Contact contact7 = new Contact();
                    contact7.setUserId("heather");
                    contact7.setFullName("Heather");
                    contact7.setImageURL("R.drawable.heather");
                    contactList.add(contact7);

                    appContactService.addAll(contactList);
                }
            }


            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                //If any failure in registration the callback  will come here
                Log.d(TAG,"Couldn't register to Applozic");
            }};

        User user = new User();
        user.setUserId(uid); //userId it can be any unique user identifier
        user.setDisplayName(name); //displayName is the name of the user which will be shown in chat messages
        user.setEmail(email);//optional
        user.setContactNumber(phone);
        user.setImageLink("");//optional,pass your image link
        new UserLoginTask(user, listener, this).execute((Void) null);

    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
