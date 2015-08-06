package com.example.sean.datatracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class SignUpActivity extends AppCompatActivity {

    private static final int UPPER_AGE_LIMIT = 70;
    private static final int LOWER_AGE_LIMIT = 18;

    private Button mSubmitButton;

    private RadioGroup mGenderRadioGroup;
    private RadioButton mMaleButton;
    private RadioButton mFemaleButton;
    private EditText mAgeEditText;
    private EditText mDataPlanEditText;
    private EditText mServiceProviderEditText;
    private EditText mPhoneModelEditText;
    private EditText mDataThisMonthEditText;

    //Fields to be submitted to Parse
    private String mGender;
    private String mAge;
    private String mDataPlan;
    private String mServiceProvider;
    private String mPhoneModel;
    private String mDataThisMonth;

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        settings = getSharedPreferences("MyPrefsFile", 0);

        //initialize buttons and edittexts
        widgetInit();

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitButtonCode();
            }
        });
    }

    private void submitButtonCode() {
        if (mMaleButton.isChecked()) {
            mGender = "male";
        } else if (mFemaleButton.isChecked()) {
            mGender = "Female";
        } else {
            mGender = "";
        }

        mAge = mAgeEditText.getText().toString();
        mDataPlan = mDataPlanEditText.getText().toString();
        mServiceProvider = mServiceProviderEditText.getText().toString();
        mPhoneModel = mPhoneModelEditText.getText().toString();
        mDataThisMonth = mDataThisMonthEditText.getText().toString();

        if (passedChecks()) {
            final AlertDialog.Builder submitDialog = new AlertDialog.Builder(SignUpActivity.this);
            submitDialog.setTitle("Are you sure?");
            submitDialog.setMessage("You can always edit options in the menu.");
            submitDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //submit data to Parse
                    beginDataSubmission();
                }
            });
            submitDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog dialog = submitDialog.create();
            dialog.show();
        } else {
            Toast.makeText(SignUpActivity.this, "Please properly complete all fields.", Toast.LENGTH_LONG).show();
        }
    }

    private void beginDataSubmission() {

        //finishSubmitting is called in both logical outcomes because if not done so, will be called before
        //background call is complete
        if (settings.getBoolean("my_first_time", true)) {
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if (e == null) {
                        finishSubmitting(parseUser);
                    }
                }
            });
        }
        else{
            finishSubmitting(ParseUser.getCurrentUser());
        }
    }

    private void finishSubmitting(ParseUser parseUser) {
        settings.edit().putBoolean("my_first_time", false).commit();

        ParseUser mCurrentUser = ParseUser.getCurrentUser();

        mCurrentUser.put("gender", mGender);
        mCurrentUser.put("age", mAge);
        mCurrentUser.put("dataPlan", mDataPlan);
        mCurrentUser.put("serviceProvider", mServiceProvider);
        mCurrentUser.put("phoneModel", mPhoneModel);
        mCurrentUser.put("dataThisMonth", mDataThisMonth);

        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setMessage(e.getMessage());
                    builder.setTitle("Oops!");
                    builder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    Intent returnToMainIntent = new Intent(SignUpActivity.this, MainActivity.class);
                    returnToMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    returnToMainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Toast.makeText(SignUpActivity.this, "User details updated.", Toast.LENGTH_LONG).show();
                    startActivity(returnToMainIntent);
                }
            }
        });
    }

    private boolean passedChecks() {
        if (mGender.isEmpty() || mAge.isEmpty() || mDataPlan.isEmpty() || mServiceProvider.isEmpty() || mPhoneModel.isEmpty() || mDataThisMonth.isEmpty()){
            return false;
        }

        int ageHolder = Integer.parseInt(mAge);
        if (ageHolder < LOWER_AGE_LIMIT || ageHolder > UPPER_AGE_LIMIT){
            return false;
        }
        return true;
    }

    private void widgetInit() {
        mSubmitButton = (Button) findViewById(R.id.submitButton);
        mGenderRadioGroup = (RadioGroup) findViewById(R.id.radioGenderGroup);
        mMaleButton = (RadioButton) findViewById(R.id.maleButton);
        mFemaleButton = (RadioButton) findViewById(R.id.femaleButton);
        mAgeEditText = (EditText) findViewById(R.id.ageEditText);
        mDataPlanEditText = (EditText) findViewById(R.id.dataPlanEditText);
        mServiceProviderEditText = (EditText) findViewById(R.id.serviceProviderEditText);
        mPhoneModelEditText = (EditText) findViewById(R.id.phoneModelEditText);
        mDataThisMonthEditText = (EditText) findViewById(R.id.dataThisMonthEditText);

    }

    //May add additional user info page
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}
