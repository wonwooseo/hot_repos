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
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Visualization extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_visualization);
        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar_visualization);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        String repoName = getIntent().getStringExtra("reponame");
        String owner = getIntent().getStringExtra("owner");
        getSupportActionBar().setTitle(repoName);
        String url = "https://api.github.com/repos/" + owner + "/" + repoName + "/stats/commit_activity";
        new StatFetcher().execute(url);
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
                Intent search_intent = new Intent(getBaseContext(), Search.class);
                startActivity(search_intent);
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle HTTP request and updating ListView in different thread.
     */
    private class StatFetcher extends AsyncTask<String, Integer, String> {
        boolean noCache = false;
        /**
         * Send request to url and make response data into string in background.
         * @param params Parameter containing url to send request.
         * @return Response json data in string.
         */
        @Override
        protected String doInBackground(String... params) {
            Log.d("StatFetcher", "Sending HTTP Request");
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
                if(code == 202) {
                    noCache = true;
                }
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
            if(noCache) {
                BarChart chart = (BarChart) findViewById(R.id.visualization_barchart);
                chart.setVisibility(View.INVISIBLE);
                TextView noData = (TextView) findViewById(R.id.visualization_nodata);
                noData.setVisibility(View.VISIBLE);
                return;
            }
            // Parse created string into JSON array
            JSONParser jParser = new JSONParser();
            JSONArray response;
            try {
                response = (JSONArray) jParser.parse(result);
            } catch(Exception e) {
                e.printStackTrace();
                return;
            }
            int[] commitCount = new int[response.size()];
            Date[] weeks = new Date[response.size()];
            for(int index = 0; index < response.size(); index++) {
                JSONObject responseItem = (JSONObject) response.get(index);
                commitCount[index] = Integer.parseInt(responseItem.get("total").toString());
                long unixTime = Long.parseLong(responseItem.get("week").toString());
                Date date = new Date();
                date.setTime(unixTime * 1000L);
                weeks[index] = date;
            }
            // Build data set for bar graph entries
            int[] monthlyCommits = new int[13];
            Date[] months = new Date[13];
            months[0] = weeks[0];
            int monthIndex = 0;
            for(int index = 0; index < weeks.length; index++) {
                if(months[monthIndex].getMonth() != weeks[index].getMonth()) {
                    monthIndex += 1;
                    months[monthIndex] = weeks[index];
                }
                monthlyCommits[monthIndex] += commitCount[index];
            }
            // Data to use in chart
            List<BarEntry> entries = new ArrayList<>();
            final List<String> xLabel = new ArrayList<>();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy - MM", Locale.US);
            for(int i = 0; i < 13; i++) {
                entries.add(new BarEntry(i, monthlyCommits[i]));
                xLabel.add(formatter.format(months[i]));
            }
            BarDataSet set = new BarDataSet(entries, "Number of Commits");
            set.setColors(ColorTemplate.MATERIAL_COLORS);
            set.setValueTextSize(16f);
            BarData data = new BarData(set);
            BarChart chart = (BarChart) findViewById(R.id.visualization_barchart);
            chart.setData(data);
            chart.setFitBars(true);
            // Set X-axis styling
            XAxis xAxis = chart.getXAxis();
            xAxis.setTextSize(16f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return xLabel.get((int) value);
                }
            });
            // Set Y-axis styling
            YAxis yAxis = chart.getAxisLeft();
            yAxis.setTextSize(16f);
            yAxis = chart.getAxisRight();
            yAxis.setTextSize(16f);
            // Overall chart styling
            chart.setVisibleXRangeMaximum(6);
            Description description = new Description();
            description.setText("Monthly commits on repository last year");
            description.setTextSize(14f);
            chart.setDescription(description);
            chart.invalidate();
        }
    }
}
