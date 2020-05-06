package com.example.guessthelyric;

import androidx.annotation.RequiresApi;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends YouTubeBaseActivity {
    private YouTubePlayerView myoutube;
    YouTubePlayer.OnInitializedListener moninit;

    private TextView startt;//start button
    private TextView baslik;//count down
    private TextView linkyazısı;//link yazısı
    private TextView sarkiyazisi;//sarki yazısı


    private EditText linkname;//link to song
    private EditText lyricyazı;// lyric edit text
    private EditText songn; //song name for api

    private CountDownTimer ct;

    private String lyric; // lyric itself
    private int score=0;

    private long stime; //countdown
    List<String> unchangedlines = new ArrayList<String>();
    List<String> removedwords = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startt = findViewById(R.id.startt);
        myoutube = findViewById(R.id.oynatıcı);
        linkname = findViewById(R.id.lyazı);
        baslik=findViewById(R.id.baslik);
        songn = findViewById(R.id.songn);
        lyricyazı = findViewById(R.id.lyricscreen);
        linkyazısı = findViewById(R.id.linkk);
        sarkiyazisi=findViewById(R.id.textView);

        lyricyazı.setScroller(new Scroller(this));
        lyricyazı.setMaxLines(15);
        lyricyazı.setVerticalScrollBarEnabled(true);
        lyricyazı.setMovementMethod(new ScrollingMovementMethod());

        moninit = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(linkname.getText().toString());
                youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                    @Override
                    public void onLoading() {

                    }

                    @Override
                    public void onLoaded(String s) {
                    }

                    @Override
                    public void onAdStarted() {

                    }

                    @Override
                    public void onVideoStarted() {
                        stime=youTubePlayer.getDurationMillis()+20000;
                    }

                    @Override
                    public void onVideoEnded() {

                    }

                    @Override
                    public void onError(YouTubePlayer.ErrorReason errorReason) {

                    }
                });

                String tmp = songn.getText().toString(); //get song name

                //convert song name into api parameter format
                String[] sns = tmp.split("\\s+");
                tmp = "";
                for (int i = 0; i < sns.length - 1; i++) {
                    tmp += sns[i];
                    tmp += "%2520";
                }
                tmp += sns[sns.length - 1];
                //convert song name into api parameter format


                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://canarado-lyrics.p.rapidapi.com/lyrics/" + tmp)
                        .get()
                        .addHeader("x-rapidapi-host", "canarado-lyrics.p.rapidapi.com")
                        .addHeader("x-rapidapi-key", "9de60e07fdmsh3a897439aeccf37p176da5jsn7a547ef577fc")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String myResponse = response.body().string();
                            //get the lyrics
                            int sindex = myResponse.indexOf("lyrics");
                            int eindex = myResponse.indexOf("artist");
                            lyric = myResponse.substring(sindex + 9, eindex - 2);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void run() {
                                    //Start showing on screen
                                    settxt();
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };
        startt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myoutube.initialize(YoutubeConfig.getApi(), moninit);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void settxt() {
        linkyazısı.setVisibility(View.GONE);
        linkname.setVisibility(View.GONE);
        sarkiyazisi.setVisibility(View.GONE);
        songn.setVisibility(View.GONE);

        String onscreen = lyric;//back up lyrics
        String onscreen2 = "";

        //remove unnecesseary characters and beautify the lyrics
        onscreen = onscreen.toLowerCase();
        onscreen = onscreen.replace(",", "");
        onscreen = onscreen.replace("?", "");
        onscreen = onscreen.replace("!", "");
        onscreen = onscreen.replace("\\n", System.getProperty("line.separator"));
        //remove unnecesseary characters and beautify the lyrics

        //get each line to arraylist
        String[] arrOfStr = onscreen.split(System.getProperty("line.separator"));
        List<String> strlist = new ArrayList<String>(Arrays.asList(arrOfStr));
        List<String> unchangedstrlist = new ArrayList<String>();
        //get each line to arraylist


        for (int i = 0; i < strlist.size(); i++) {

            String t = strlist.get(i);

            //remove the line if contains [] if contains () just remove the ()
            if (t.contains("[") || t.contains("]")) {
                strlist.set(i, "*");
            }
            if (t.contains("(") || t.contains(")")) {
                String temp = t.replace("(", "");
                temp = temp.replace(")", "");
                strlist.set(i, temp);
            }
            //remove the line if contains [] if contains () just remove the ()

            //start on working on the lyrics
            if (!strlist.get(i).equalsIgnoreCase("*")) {

                unchangedstrlist.add(strlist.get(i));//add to unchanged lyrics
                unchangedlines.add(strlist.get(i));
                //get random word and swap with equal length of "?"
                String[] tarrOfStr = strlist.get(i).split(" ");
                List<String> tarrOfStrlist = new ArrayList<String>(Arrays.asList(tarrOfStr));

                Random rand = new Random();
                String randomElement = tarrOfStr[(rand.nextInt(tarrOfStrlist.size()))];
                String q="";
                for (int gg=0;gg<randomElement.length();gg++){
                    q+="?";
                }
                String listString = String.join(" ", tarrOfStrlist);
                removedwords.add(randomElement);
                listString=listString.replaceFirst(randomElement,q);
                strlist.set(i, listString);
                //get random word and swap with equal length of "?"


                //add line to screen string
                onscreen2 += strlist.get(i);
                if (i<strlist.size()-1)
                onscreen2 += System.getProperty("line.separator");
                //add line to screen string
            }
        }

//        for (int i = 0; i < unchangedstrlist.size(); i++) {
//            System.out.println(unchangedstrlist.get(i));
//        }

        lyricyazı.setText(onscreen2);
        startTimer();

    }
    protected void compare(){
        String resultText=lyricyazı.getText().toString();
//        int count=0;
//        for(int i = 0; i < resultText.length(); i++) {
//            if(string.charAt(i) == ' ')
//                count++;
//        }
        //get each line to arraylist
        String[] arrOfStr = resultText.split(System.getProperty("line.separator"));
        List<String> strlist = new ArrayList<String>(Arrays.asList(arrOfStr));
        for (int i=0;i<strlist.size();i++){
            String line=strlist.get(i);
            String cline= unchangedlines.get(i);
            if (line.compareTo(cline)==0 && line.length()+1!=System.getProperty("line.separator").length()){
                System.out.println(line+" a "+ line.length());
                System.out.println(System.getProperty("line.separator")+" b "+ System.getProperty("line.separator").length());
                score+=1;
            }
        }
        baslik.setText("Skor: "+(score));
        //get each line to arraylist

    }
    protected void startTimer() {
        ct = new CountDownTimer(stime, 1000) {

            public void onTick(long millisUntilFinished) {
                stime = millisUntilFinished;
            }

            public void onFinish() {
                compare();

            }
        }.start();
    }
}
