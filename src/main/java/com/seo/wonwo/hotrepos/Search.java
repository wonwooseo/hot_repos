package com.seo.wonwo.hotrepos;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.*;
import org.json.simple.parser.*;

public class Search extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar_search);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        // Populate search by spinner
        final Spinner searchSpinner = (Spinner) findViewById(R.id.search_options);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.search_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchSpinner.setAdapter(adapter);
        // set listener for go button
        Button goButton = (Button) findViewById(R.id.search_gobutton);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = (EditText) findViewById(R.id.search_editText);
                int searchOption = searchSpinner.getSelectedItemPosition();
                String queryText = input.getText().toString();
                String url = "https://api.github.com/search/repositories?q=";
                if(searchOption == 0) {
                    url = url + queryText;
                } else if(searchOption == 1) {
                    url = url + "language:" + queryText;
                } else {
                    url = url + "topic:" + queryText;
                }
                new SearchExecutor().execute(url);
            }
        });
    }

    /**
     * Set custom effect for transition initiated by pressing back button
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Inflates search, notification into toolbar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        return true;
    }

    /**
     * Listener for toolbar action buttons.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.appbar_search:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle HTTP request and updating ListView in different thread.
     */
    private class SearchExecutor extends AsyncTask<String, Integer, String> {
        /**
         * Send request to url and make response data into string in background.
         * @param params Parameter containing url to send request.
         * @return Response json data in string.
         */
        @Override
        protected String doInBackground(String... params) {
            Log.d("SearchExecutor", "Sending HTTP Request");
            String url = params[0];
            URL page;
            try {
                page = new URL(url);
            } catch(Exception e) {
                e.printStackTrace();
                return "HTTP Request failed";
            }
            // Open connection to given url
            HttpURLConnection connection;
            try {
                connection = (HttpURLConnection) page.openConnection();
            } catch(Exception e) {
                e.printStackTrace();
                return "HTTP Request failed";
            }
            // Set Authorization
            try {
                connection.setRequestProperty("Authorization", "TOKEN " + getString(R.string.token));
            } catch(Exception e) {
                e.printStackTrace();
                return "HTTP Request failed";
            }
            // Set request method to GET
            try {
                connection.setRequestMethod("GET");
                int code = connection.getResponseCode();
                Log.d("Search", "received " + code);
            } catch(Exception e) {
                e.printStackTrace();
                return "HTTP Request failed";
            }
            // Stream response data to string
            BufferedReader input;
            StringBuilder responseBuffer = new StringBuilder();
            String line;
            try {
                input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                line = input.readLine();
                while(line != null) {
                    responseBuffer.append(line);
                    line = input.readLine();
                }
                input.close();
            } catch(Exception e) {
                e.printStackTrace();
                return "HTTP Request failed";
            }
            // Send created string to onPostExecute()
            return responseBuffer.toString();
        }

        /**
         * Parse given string into JSON object and update TextView fields accordingly.
         * @param result String to parse into JSON object.
         */
        @Override
        protected void onPostExecute(String result) {
            Log.d("SearchExecutor", "Parsing JSON and updating fields");
            // Parse created string into JSON array
            JSONParser jParser = new JSONParser();
            JSONObject response;
            try {
                response = (JSONObject) jParser.parse(result);
            } catch(Exception e) {
                e.printStackTrace();
                return;
            }
            JSONArray responseArray = (JSONArray) response.get("items");
            final String[] nameList = new String[responseArray.size()];
            final String[] ownerList = new String[responseArray.size()];
            String[] descriptionList = new String[responseArray.size()];
            for(int index = 0; index < responseArray.size(); index++) {
                JSONObject repoItem = (JSONObject) responseArray.get(index);
                nameList[index] = repoItem.get("name").toString();
                try {
                    descriptionList[index] = repoItem.get("description").toString();
                } catch(Exception e) {
                    descriptionList[index] = "Repository description not provided";
                }
                JSONObject owner = (JSONObject) repoItem.get("owner");
                ownerList[index] = owner.get("login").toString();
            }
            // Use adapter to populate items in ListView
            RepoAdapter adapter = new RepoAdapter(getBaseContext(), nameList, ownerList, descriptionList);
            ListView listView = (ListView) findViewById(R.id.search_result_list);
            listView.setAdapter(adapter);
            // Add item click listener to open repo web page
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int index, long id) {
                    Intent visual_intent = new Intent(getBaseContext(), Visualization.class);
                    visual_intent.putExtra("reponame", nameList[index]);
                    visual_intent.putExtra("owner", ownerList[index]);
                    startActivity(visual_intent);
                }
            });
            // If there is no match result, show message
            TextView nomatch = (TextView) findViewById(R.id.search_no_match);
            nomatch.setVisibility(View.GONE);
            if(responseArray.size() == 0) {
                nomatch.setVisibility(View.VISIBLE);
            }
        }
    }
}
