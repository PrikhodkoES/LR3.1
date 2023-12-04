package com.example.lr31;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String url = "https://www.forbes.ru/rating/403469-40-samyh-uspeshnyh-zvezd-rossii-do-40-let-reyting-forbes";
    ArrayList<String> names = new ArrayList<>(), urls = new ArrayList<>();
    ImageView imgView;
    EditText editName;
    Button btnInput;
    Bitmap bmp;
    int index;

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnInput) {
            if (editName.getText().toString().equals("")) {
                Toast.makeText(this, getString(R.string.NoText), Toast.LENGTH_SHORT).show();
                return;
            }

            if (editName.getText().toString().equals(names.get(index)))
                Toast.makeText(this, getString(R.string.Correct), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, names.get(index), Toast.LENGTH_SHORT).show();
            editName.setText("");
            setNewBMP();
        }
    }

    private static class DownloadContentTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    result.append(line);
                    line = reader.readLine();
                }
                return result.toString();
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return null;
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String...strings) {
            URL url =  null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection!=null)
                    urlConnection.disconnect();
            }
            return null;
        }
    }

    private void getContent()
    {
        DownloadContentTask task = new DownloadContentTask();
        try {
            String content = task.execute(url).get();
            Pattern pattern = Pattern.compile("profile:(.*?)filemime");
            Pattern patternImg = Pattern.compile("uri:\"(.*?)\"");
            Pattern patternName = Pattern.compile("title:\"(.*?)\"");
            Matcher matcher = pattern.matcher(content);
            Matcher matcherImg;
            Matcher matcherName;
            String splitContent = "";
            while (matcher.find()) {
                splitContent = matcher.group(1);
                matcherImg = patternImg.matcher(splitContent);
                matcherName = patternName.matcher(splitContent);
                while (matcherImg.find())
                    urls.add(Objects.requireNonNull(matcherImg.group(1)).replace("u002F", ""));
                while (matcherName.find())
                    names.add(matcherName.group(1));
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setNewBMP() {
        DownloadImageTask task = new DownloadImageTask();
        Random rand = new Random();
        index = rand.nextInt(41);
        try {
            bmp = task.execute(urls.get(index)).get();
            imgView.setImageBitmap(bmp);
            Log.d("MyLog", names.get(index));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = findViewById(R.id.imgView);
        editName = findViewById(R.id.editName);
        btnInput = findViewById(R.id.btnInput);
        btnInput.setOnClickListener(this);

        getContent();
        setNewBMP();
    }
}

