package com.example.taskmaster;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.predictions.models.LanguageType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TaskDetailActivity extends AppCompatActivity {
    private static final String TAG = "test";

    private final MediaPlayer mp = new MediaPlayer();

    String imageKey ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        TaskNameChange();
        changeTaskBody();
        changeTaskState();
        translate();
        soundOn();

        imageKey = getIntent().getStringExtra("imageKey");


        if(imageKey!=null)
        pictureDownloadAndView(imageKey);
        else
            Log.i(TAG, "onCreate: imageKey is null->"+imageKey);

    }



    private void soundOn() {
        ImageButton sound = findViewById(R.id.sound_btn);
        sound.setOnClickListener(view -> {
            TextView body = findViewById(R.id.body);
            String textToRead = body.getText().toString();
            Amplify.Predictions.convertTextToSpeech(
                    textToRead,
                    result -> {
                        playAudio(result.getAudioData());
                        Log.i(TAG, "soundOn: "+result);
                    },
                    error -> Log.e("MyAmplifyApp", "Conversion failed", error)
            );
        });
    }

    private void translate() {
        Button translate_btn =  findViewById(R.id.translate_btn);
        translate_btn.setOnClickListener(view-> {

                    TextView mTranslatedBody = findViewById(R.id.translated_body);
                    TextView mBody = findViewById(R.id.body);
            Amplify.Predictions.translateText(
                    mBody.getText().toString(),
                    LanguageType.ENGLISH,
                    LanguageType.ARABIC,
                    result -> {
                        Log.i("MyAmplifyApp", result.getTranslatedText());
                        mTranslatedBody.setText(result.getTranslatedText());
                        mTranslatedBody.setEnabled(true);

                    },
                    error -> Log.e("MyAmplifyApp", "Translation failed", error)
            );
        }
        );
    }

    private void TaskNameChange() {
        TextView mTitle = findViewById(R.id.title);
        String title = getIntent().getStringExtra("title");
        mTitle.setText(title);
    }
    private void changeTaskBody() {
        TextView mBody = findViewById(R.id.body);
        String body = getIntent().getStringExtra("body");
        mBody.setText(body);

    }
    private void changeTaskState() {
        TextView mState = findViewById(R.id.state);
        String state = getIntent().getStringExtra("state");
        mState.setText(state);
    }
    private void pictureDownloadAndView(String imageKey) {

        Amplify.Storage.downloadFile(
                imageKey,
                new File( getApplicationContext().getFilesDir() + "/" + imageKey),
                result -> {
                    Log.i(TAG, "The root path is: " + getApplicationContext().getFilesDir());
                    Log.i(TAG, "Successfully downloaded: " + result.getFile().getName());


                    ImageView image = findViewById(R.id.task_image);
                    Bitmap bitmap = BitmapFactory.decodeFile(getApplicationContext().getFilesDir()+"/"+result.getFile().getName());
                    image.setImageBitmap(bitmap);
                },
                error -> Log.e(TAG,  "Download Failure", error)
        );
    }

    private void playAudio(InputStream data) {
        File mp3File = new File(getCacheDir(), "audio.mp3");

        try (OutputStream out = new FileOutputStream(mp3File)) {
            byte[] buffer = new byte[8 * 1_024];
            int bytesRead;
            while ((bytesRead = data.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            mp.reset();
            mp.setOnPreparedListener(MediaPlayer::start);
            mp.setDataSource(new FileInputStream(mp3File).getFD());
            mp.prepareAsync();
        } catch (IOException error) {
            Log.e("MyAmplifyApp", "Error writing audio file", error);
        }
    }
}
