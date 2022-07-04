package com.example.taskmaster;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Team;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AddTask extends AppCompatActivity {
    private static final String TAG = "test";
    public static final int REQUEST_CODE = 123;

    Spinner taskStateSelector ;
    Spinner taskTeamSelector;
    String image ="" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        final String[] mTeam = new String[]{"Team1", "Team2", "Team3"};
//-------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------
//        Team team1 = Team.builder().name("Team1").build();
//        Team team2 = Team.builder().name("Team2").build();
//        Team team3 = Team.builder().name("Team3").build();
//
//        Amplify.API.mutate(ModelMutation.create(team1),
//                response -> Log.i("MyAmplifyApp", "Todo with id: " + response.getData().getId()),
//                error -> Log.e("MyAmplifyApp", "Create failed", error)
//        );
//
//        Amplify.API.mutate(ModelMutation.create(team2),
//                response -> Log.i("MyAmplifyApp", "Todo with id: " + response.getData().getId()),
//                error -> Log.e("MyAmplifyApp", "Create failed", error)
//        );
//
//        Amplify.API.mutate(ModelMutation.create(team3),
//                response -> Log.i("MyAmplifyApp", "Todo with id: " + response.getData().getId()),
//                error -> Log.e("MyAmplifyApp", "Create failed", error)
//        );
//
//------------------------------------------------
        taskTeamSelector = findViewById(R.id.task_team_spinner);
        ArrayAdapter<String> teamSpinnerAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                mTeam
        );
        taskTeamSelector.setAdapter(teamSpinnerAdapter);

        final String[] mState = new String[]{"New", "Assigned", "In progress", "complete"};

        Spinner taskStateSelector = findViewById(R.id.task_state_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                getApplicationContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                mState
        );
        taskStateSelector.setAdapter(spinnerAdapter);
        taskStateSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        handleSharedImage();

        Button uploadImageBtn = findViewById(R.id.addImageBtn);
        uploadImageBtn.setOnClickListener(view -> {
            pictureUpload();
        });

        Button button = findViewById(R.id.button);

        button.setOnClickListener(view -> {
            String team = taskTeamSelector.getSelectedItem().toString();
            Amplify.API.query(ModelQuery
                            .list(Team.class, Team.NAME.eq(team)),
                    success->{
                        for (Team currentT :
                                success.getData()) {
                            EditText titleField = findViewById(R.id.title);
                            String title = titleField.getText().toString();

                            EditText bodyField = findViewById(R.id.body);
                            String body = bodyField.getText().toString();

                            String state = taskStateSelector.getSelectedItem().toString();


                            Log.i(TAG, "onCreate: imageKey is => "+image);

                            com.amplifyframework.datastore.generated.model.Task newTask = com.amplifyframework.datastore.generated.model.Task.builder()
                                    .title(title)
                                    .body(body)
                                    .status(state)
                                    .teamTasksId(currentT.getId())
                                    .image(image) // image key not path
                                    .build();

                            Amplify.DataStore.save(newTask,
                                    saved -> {},
                                    notSaved ->{}
                            );

                            Amplify.API.mutate(
                                    ModelMutation.create(newTask),
                                    taskSaved-> { },
                                    error -> { }
                            );
                        }
                    },
                    Fail->{
                    }

            );


        });


    }
    private void handleSharedImage (){
        Intent intent = getIntent();
        String type = intent.getType();
        if(intent != null && intent.getType()!=null && type.startsWith("image/")){
            Uri currentUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            Log.i(TAG, "handleSharedImage: currentUri last path segment =>"+currentUri.getLastPathSegment().substring(currentUri.getLastPathSegment().lastIndexOf("/")+1));
            if(!currentUri.equals(null)){

                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(currentUri);

                } catch (FileNotFoundException err) {
                    Log.e(TAG,  err.getMessage());
                }


                Log.i(TAG, "onActivityResult: the uri is => " + currentUri);

                try {
                    Bitmap bitmap = getBitmapFromUri(currentUri);
                    File file = new File(getApplicationContext().getFilesDir(), currentUri.getLastPathSegment().substring(currentUri.getLastPathSegment().lastIndexOf("/")+1));
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.close();


                    Amplify.Storage.uploadFile(
                            currentUri.getLastPathSegment(),
                            file,
                            result -> {
                                Log.i(TAG, "Successfully uploaded: " + result.getKey());
                                image = result.getKey();
                                Toast.makeText(getApplicationContext(), "image is uploaded", Toast.LENGTH_SHORT).show();
                            },
                            storageFailure -> Log.e(TAG, "Upload failed", storageFailure)
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;

            }
        }
    }

    private void pictureUpload() {
        // Launches photo picker in single-select mode.
        // This means that the user can select one photo or video.
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            // Handle error
            Log.e(TAG, "onActivityResult: Error getting image from device" + resultCode);
            return;
        }

        switch(requestCode) {
            case REQUEST_CODE:

                Uri currentUri = data.getData();


                Log.i(TAG, "onActivityResult: the uri is => " + currentUri);

                try {
                    Bitmap bitmap = getBitmapFromUri(currentUri);

                    File file = new File(getApplicationContext().getFilesDir(), currentUri.getLastPathSegment());
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.close();


                    Amplify.Storage.uploadFile(
                            currentUri.getLastPathSegment(),
                            file,
                            result -> {
                                Log.i(TAG, "Successfully uploaded: " + result.getKey());
                                image = result.getKey();
                                Toast.makeText(getApplicationContext(), "image is uploaded", Toast.LENGTH_SHORT).show();
                            },
                            storageFailure -> Log.e(TAG, "Upload failed", storageFailure)
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
        }
    }


    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();

        return image;
    }




}


