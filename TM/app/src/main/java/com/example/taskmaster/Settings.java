package com.example.taskmaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {


    private static final String TAG = "test";
    public static final String TEAM_NAME = "teamName";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        final String[] mTeam = new String[]{"Design", "Render", "Poster"};

        Spinner TeamNameSelector = findViewById(R.id.team_spinner);
        ArrayAdapter<String> teamSpinnerAdapter = new ArrayAdapter<String>(
                getApplicationContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                mTeam
        );

        TeamNameSelector.setAdapter(teamSpinnerAdapter);
        TeamNameSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

//        Button button = findViewById(R.id.save_btn);
//
//        button.setOnClickListener(view -> {
//            mUsernameEditText = findViewById(R.id.usernameInput);
//            String username = mUsernameEditText.getText().toString();
////            saveUsername(username);
//            Toast.makeText(this, "username added "+username, Toast.LENGTH_SHORT).show();
//        });

        Button save_team_btn = findViewById(R.id.save_team_btn);
        save_team_btn.setOnClickListener(view->{
            Spinner mTeamNameSpinner = findViewById(R.id.team_spinner);
            String teamName = mTeamNameSpinner.getSelectedItem().toString();
            saveTeamName(teamName);
//            Intent intent = new Intent(Settings.this, MainActivity.class);
//            intent.putExtra(TEAM_NAME , teamName);

            Toast.makeText(this, "teamName changed to "+teamName, Toast.LENGTH_SHORT).show();


        });
    }

    private void saveTeamName (String teamName){

        //create sharedPreference and set up an editor
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferenceEditor = sharedPreferences.edit();
        //save
        preferenceEditor.putString(TEAM_NAME,teamName);
        preferenceEditor.apply();
    }

    //    private void saveUsername (String username){
//
//        //create sharedPreference and set up an editor
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences.Editor preferenceEditor = sharedPreferences.edit();
//        //save
//        preferenceEditor.putString(USERNAME,username);
//        preferenceEditor.apply();
//    }


}