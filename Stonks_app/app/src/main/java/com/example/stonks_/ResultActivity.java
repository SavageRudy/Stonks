package com.example.stonks_;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        TextView firmname= findViewById(R.id.firmname);


        ProgressBar spinner;
        spinner = (ProgressBar)findViewById(R.id.progressBar1);

        Objects.requireNonNull(getSupportActionBar()).hide();


        Button back1 = findViewById(R.id.back1);
        back1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this ,HomeActivity.class);
                startActivity(intent);
            }
        });

        RequestQueue queue = Volley.newRequestQueue(ResultActivity.this);
        String url = "http://192.168.0.8:5000";

        Intent intent= getIntent();
        String query = intent.getExtras().getString("query");

        firmname.setText(query.toUpperCase());

        String json = "{\"query\":\""+ query +"\"}";
        spinner.setVisibility(View.VISIBLE);
        try {

            JSONObject obj = new JSONObject(json);

            Log.i("stonks", obj.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("stonks",response.toString());
                    spinner.setVisibility(View.GONE);
                    TextView val, curr;
                    val = findViewById(R.id.closeval);
                    curr = findViewById(R.id.currentval);
                    try {
                        JSONArray jval1 = response.getJSONArray("pred");
                        Log.i("stonks",jval1.toString());
                        JSONArray jval2 = jval1.getJSONArray(0);
                        Integer i = jval2.getInt(0);
                        val.setText(i.toString());

                        JSONArray jval3 = response.getJSONArray("lastval");
                        Log.i("stonks",jval3.toString());
                        Integer in = jval3.getInt(0);
                        curr.setText(in.toString());

                        JSONArray jval4 = response.getJSONArray("values");
                        Log.i("stonks",jval4.toString());

                        GraphView graph =findViewById(R.id.graph);
                        graph.setVisibility(View.VISIBLE);

                        LineGraphSeries<DataPoint> series;
                        series = new LineGraphSeries<>();
                        int day=1;
                        for (int k = 0 ; k < jval4.length(); k++)
                        {
                            JSONArray jval5 = jval4.getJSONArray(k);
                            int stock = jval5.getInt(0);
                            series.appendData(new DataPoint(day,stock),true,65);
                            day+=1;
                        }
                        graph.setBackgroundColor(Color.BLACK);
                        graph.addSeries(series);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("stonks",error.toString());
                    Intent intent2 = new Intent(ResultActivity.this,ErrorActivity.class);
                    intent2.putExtra("error", "No Result found"); /* evenif the server is not runnin or if no such results were found in the database..it will show the same "no results: error...to give sepearate error text for both change the sever code in such a way that it will give a response of somekind if there is no such table in db..so u can give tht text and goto to the error intent in the above response section of code. and for server disconection error we can use this volley error*/
                    startActivity(intent2);
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);

        } catch (Throwable tx) {
            Intent intent1 = new Intent(ResultActivity.this,ErrorActivity.class);
            intent1.putExtra("error", "Something went wrong");
            startActivity(intent1);
        }
    }

}
