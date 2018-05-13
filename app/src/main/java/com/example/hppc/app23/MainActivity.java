package com.example.hppc.app23;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;

    Button button0;
    Button button1;
    Button button2;
    Button button3;

    String pageSource = "";

    ArrayList<String> urls = new ArrayList<String>();
    ArrayList<String> names = new ArrayList<String>();

    int currentIndex;
    int correctAnswerIndex;
    String[] labels = new String[4];

    Random random;

    public void checkAnswer(View view){
        //Log.i("info",view.getTag().toString());

        if((view.getTag().toString()).equals(Integer.toString(correctAnswerIndex))){
            Toast.makeText(MainActivity.this,"Correct!",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this,"Incorrect Answer!",Toast.LENGTH_SHORT).show();
        }
        showNextQuestion();
    }

    void showNextQuestion(){
        currentIndex = random.nextInt(names.size());

        try {
            Bitmap bitmap = new DownloadImage().execute(urls.get(currentIndex)).get();
            imageView.setImageBitmap(bitmap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        correctAnswerIndex = random.nextInt(4);

        for(int i=0;i<4;i++){
            if(i==correctAnswerIndex){
                labels[i] = names.get(currentIndex);
            }else{
                int incorrectIndex = random.nextInt(names.size());
                while(incorrectIndex==currentIndex){
                    incorrectIndex = random.nextInt(names.size());
                }
                labels[i] = names.get(incorrectIndex);
            }
        }

        button0.setText(labels[0]);
        button1.setText(labels[1]);
        button2.setText(labels[2]);
        button3.setText(labels[3]);
    }

    class DownloadImage extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);


        }
    }

    class DownloadPage extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader rd = new BufferedReader(reader);

                int ch = 0;
                while((ch=reader.read())!=-1){
                    pageSource += (char)ch;
                }

                return pageSource;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            pageSource = pageSource.split("<ul class=\"fv-list\">")[1];
            pageSource = pageSource.split("</ul>")[0];

            Pattern	p = Pattern.compile(">(.*?)</a>");
            Matcher m = p.matcher(pageSource);

            int count = 0;
            while(m.find()){
                String x = m.group();

                if(count%2!=0){
                    //Log.i("Fruit Names",x.substring(1,x.length()-4)+"\n\n");
                    names.add(x.substring(1,x.length()-4));
                }
                count++;
            }

            Pattern	p1 = Pattern.compile("src=\".*?\"");
            Matcher m1 = p1.matcher(pageSource);

            while(m1.find()){
                String y = m1.group();
                //Log.i("Info",y.substring(5,y.length()-1));
                urls.add(y.substring(5,y.length()-1));
            }

            MainActivity.this.showNextQuestion();
        }                                                                                              
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.ImageView);

        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        random = new Random();

        DownloadPage downloadPage = new DownloadPage();
        downloadPage.execute("http://www.halfyourplate.ca/fruits-and-veggies/fruits-a-z/");
    }
}

