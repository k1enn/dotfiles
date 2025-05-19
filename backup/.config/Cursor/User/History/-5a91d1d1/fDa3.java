package com.example.elsa_speak_clone.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.elsa_speak_clone.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class DictionaryActivity extends AppCompatActivity {
    private static final String TAG = "DictionaryActivity";
    private static final String API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    private TextView resultTextView;
    private EditText wordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        resultTextView = findViewById(R.id.resultTextView);
        wordEditText = findViewById(R.id.wordEditText);
        ImageView searchButton = (ImageView)findViewById(R.id.searchButton);

        // Set click listener for search button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = wordEditText.getText().toString().trim();
                if (!word.isEmpty()) {
                    new FetchDefinitionTask().execute(word);
                } else {
                    Toast.makeText(DictionaryActivity.this, "Please enter a word", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class FetchDefinitionTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            resultTextView.setText("Searching...");
        }

        @Override
        protected String doInBackground(String... params) {
            String word = params[0];
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(API_URL + word);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return "Error: " + responseCode;
                }

                InputStream inputStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                return result.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "Network error: " + e.getMessage();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null && !result.startsWith("Error") && !result.startsWith("Network error")) {
                try {
                    displayDefinition(result);
                } catch (JSONException e) {
                    resultTextView.setText("Error parsing data: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                resultTextView.setText(result);
            }
        }
    }

    private void displayDefinition(String jsonData) throws JSONException {
        StringBuilder displayText = new StringBuilder();

        JSONArray jsonArray = new JSONArray(jsonData);
        JSONObject wordObject = jsonArray.getJSONObject(0);

        // Get the word
        String word = wordObject.getString("word");
        displayText.append("Word: ").append(word).append("\n\n");

        // Get phonetics if available
        if (wordObject.has("phonetic") && !wordObject.isNull("phonetic")) {
            String phonetic = wordObject.getString("phonetic");
            displayText.append("Pronunciation: ").append(phonetic).append("\n\n");
        }

        // Get meanings
        JSONArray meaningsArray = wordObject.getJSONArray("meanings");
        for (int i = 0; i < meaningsArray.length(); i++) {
            JSONObject meaningObject = meaningsArray.getJSONObject(i);
            String partOfSpeech = meaningObject.getString("partOfSpeech");

            displayText.append(partOfSpeech.toUpperCase()).append("\n");

            JSONArray definitionsArray = meaningObject.getJSONArray("definitions");
            for (int j = 0; j < definitionsArray.length(); j++) {
                JSONObject definitionObject = definitionsArray.getJSONObject(j);
                String definition = definitionObject.getString("definition");

                displayText.append(j + 1).append(". ").append(definition).append("\n");

                // Get example if available
                if (definitionObject.has("example") && !definitionObject.isNull("example")) {
                    String example = definitionObject.getString("example");
                    displayText.append("   Example: ").append(example).append("\n");
                }

                displayText.append("\n");
            }
        }

        resultTextView.setText(displayText.toString());
    }
}
