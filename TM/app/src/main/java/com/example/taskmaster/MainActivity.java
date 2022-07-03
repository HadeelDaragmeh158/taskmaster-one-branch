package com.example.taskmaster;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.analytics.AnalyticsEvent;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.datastore.generated.model.Task;
import com.amplifyframework.datastore.generated.model.Team;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "test";

    private AdView mAdView;

    private Handler handler;

    private InterstitialAd mInterstitialAd;

    private RewardedAd mRewardedAd;


    List<com.amplifyframework.datastore.generated.model.Task> tasks= new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdView = findViewById(R.id.adView);
        AdRequest bannerAdRequest = new AdRequest.Builder().build();
        mAdView.loadAd(bannerAdRequest);

        Button interstitialAdBtn = findViewById(R.id.Interstitial_Ad);
        loadInterstitialAd();
        interstitialAdBtn.setOnClickListener(view->{
            if (mInterstitialAd != null) {
                mInterstitialAd.show(MainActivity.this);
            } else {
                Log.d(TAG, "The interstitial ad wasn't ready yet.");
            }
        });

        Button rewardedAdBtn = findViewById(R.id.Rewarded_Ad);
        loadRewardedAd();
        rewardedAdBtn.setOnClickListener(view->{


            if (mRewardedAd != null) {
                Activity activityContext = MainActivity.this;
                mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d(TAG, "The user earned the reward.");
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();
                    }
                });
            } else {
                Log.d(TAG, "The rewarded ad wasn't ready yet.");
            }


        });

        Button settingsButton = findViewById(R.id.settings);
        settingsButton.setOnClickListener(view -> {
            Intent settingsIntent = new Intent(this, Settings.class);
            startActivity(settingsIntent);
            analytics("settings_btn");
        });

        Button addTaskButton = findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(view -> {
            Intent addTaskIntent = new Intent(this, AddTaskAct.class);
            startActivity(addTaskIntent);
            analytics("addTask_btn");

        });

        Button allTasksButton = findViewById(R.id.allTasksButton);
        allTasksButton.setOnClickListener(view -> {
            Intent allTasksIntent = new Intent(this, AllTasksAct.class);
            startActivity(allTasksIntent);
            analytics("allTasks_btn");

        });

        changeTeamName();
        changeUsername();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        logout();
        return true;
    }
    private void authSession(String method) {
        Amplify.Auth.fetchAuthSession(
                result -> Log.i(TAG, "Auth Session => " + method + result.toString()),
                error -> Log.e(TAG, error.toString())
        );
    }

    private void logout (){
        Amplify.Auth.signOut(
                () -> {
                    Log.i(TAG, "Signed out successfully");
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    authSession("logout");
                    finish();
                },
                error -> Log.e(TAG, error.toString())
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //printing team name
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String teamName = sharedPreferences.getString(Settings.TEAM_NAME,"All");


        //printing tasks
        if(!Objects.equals(teamName,"All"))
        Amplify.API.query(ModelQuery
                        .list(Team.class, Team.NAME.eq(teamName)),
                success-> {
                    for (Team curTeam :
                            success.getData()) {

                        tasks = findTasksAPI(curTeam.getId());

                        handler = new Handler(Looper.getMainLooper() , msg -> {

                            ListView tasksList = findViewById(R.id.tasksList);
                            ArrayAdapter<com.amplifyframework.datastore.generated.model.Task> taskArrayAdapter = new ArrayAdapter<Task>(
                                    getApplicationContext(),
                                    android.R.layout.simple_list_item_2,
                                    android.R.id.text1,
                                    tasks
                            ) {
                                @NonNull
                                @Override
                                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    TextView title = (TextView) view.findViewById(android.R.id.text1);
                                    TextView body = (TextView) view.findViewById(android.R.id.text2);

                                    title.setText(tasks.get(position).getTitle());
                                    body.setText(tasks.get(position).getBody());

                                    return view;
                                }
                            };
                            tasksList.setAdapter(taskArrayAdapter);

                            tasksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent taskIntent = new Intent(getApplicationContext(),TaskDetailActivity.class);

                                    Log.i(TAG, "onItemClick: "+tasks.get(i).getImagePath());


                                    taskIntent.putExtra("title",tasks.get(i).getTitle());
                                    taskIntent.putExtra("body",tasks.get(i).getBody());
                                    taskIntent.putExtra("state",tasks.get(i).getStatus());
                                    taskIntent.putExtra("imageKey",tasks.get(i).getImagePath());


                                    startActivity(taskIntent);
                                }
                            });
                            return true ; //for the handler
                        });
                    }
                },
                error -> {
                });

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")


    private List<com.amplifyframework.datastore.generated.model.Task> findTasksAPI (String teamId){
        Amplify.API.query(
                ModelQuery.list(Task.class),
                success -> {
                    tasks.clear();
                    if (success.hasData()) {
                        for (Task task : success.getData()) {
                            if(task.getTeamTasksId()!=null)
                            if(task.getTeamTasksId().equals(teamId))
                                tasks.add(task);
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString("data","Done");
                        Message message = new Message();
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }
                    },
                notFound->{
                });
    return tasks;
    }

    private void  changeTeamName(){
        //        receive the team name from settings
        TextView mTeamName = findViewById(R.id.team_name);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mTeamName.setText(sharedPreferences.getString(Settings.TEAM_NAME,"All")+ " Tasks");
    }

    private void  changeUsername(){
        //        receive the team name from settings
        TextView mTeamName = findViewById(R.id.usernameHeader);
//        String teamNameText = getIntent().getStringExtra(TEAM_NAME);
//        mTeamName.setText(teamNameText);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mTeamName.setText(sharedPreferences.getString(LoginActivity.USERNAME,"My Tasks"));
    }

    private List<com.amplifyframework.datastore.generated.model.Task> findTasksDataStore (){
        List<com.amplifyframework.datastore.generated.model.Task> tasks = new ArrayList<>();

        Amplify.DataStore.query(
                com.amplifyframework.datastore.generated.model.Task.class,
                foundTasks->{
                    while (foundTasks.hasNext())
                    {
                        com.amplifyframework.datastore.generated.model.Task curTask = foundTasks.next();
                        tasks.add(curTask);
                    }

                },
                notFound->{
                }
        );
        return tasks ;
    }

    private void analytics (String eventName){
        AnalyticsEvent event = AnalyticsEvent.builder()
                .name(eventName)
                .addProperty("Successful", true)
                .addProperty("ProcessDuration", 792)
                .build();

        Amplify.Analytics.recordEvent(event);
    }

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d(TAG, "Ad was loaded.");

                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad was shown.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.d(TAG, "Ad failed to show.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad was dismissed.");
                                mRewardedAd = null;
                            }
                        });
                    }
                });
    }


    private void loadInterstitialAd() {

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "Interstitial => onAdLoaded");

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d(TAG, "The ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.d(TAG, "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null;
                                Log.d(TAG, "The ad was shown.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }
}
