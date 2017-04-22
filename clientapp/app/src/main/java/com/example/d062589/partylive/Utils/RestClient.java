package com.example.d062589.partylive.Utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by D062589 on 22.03.2017.
 */

public class RestClient {



    private static final String TAG = "RestClient";
    private static RestClient instance = null;

    private static final String PREFIX_URL = "http://207.154.218.165";

    //for Volley API
    public RequestQueue requestQueue;

    private RestClient(Context context)
    {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        //other stuff if you need
    }

    public static synchronized RestClient getInstance(Context context)
    {
        if (null == instance)
            instance = new RestClient(context);
        return instance;
    }

    // this is so you don't need to pass context each time
    public static synchronized RestClient getInstance()
    {
        if (null == instance)
        {
            throw new IllegalStateException(RestClient.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first");
        }
        return instance;
    }



    public void post(JSONObject entity, String path, final MyListener<JSONObject> listener)
    {
        String absoluteUrl = PREFIX_URL + path;

        MetaRequest request = new MetaRequest(Request.Method.POST, absoluteUrl, entity, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG + ": ", "PostRequest Response : " + response.toString());
                    if (null != response.toString())
                        listener.getResult(response);
                }
            },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        if (null != error.networkResponse)
                        {
                            Log.d(TAG + ": ", "Error Response code: " + error.networkResponse.statusCode);
                            listener.getResult(null);
                        }
                    }
                });
        requestQueue.add(request);
    }



    // Post with Cookie & userId
    public void post(final String userId, final String sessionCookie, JSONObject entity, String path, final MyListener<JSONObject> listener)
    {
        String absoluteUrl = PREFIX_URL + path;

        MetaRequest request = new MetaRequest(Request.Method.POST, absoluteUrl, entity, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG + ": ", "PostRequest Response : " + response.toString());
                    if (null != response.toString())
                        listener.getResult(response);
                }
            },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        if (null != error.networkResponse)
                        {
                            Log.d(TAG + ": ", "Error Response: " + error.networkResponse);
                            listener.getResult(null);
                        }
                    }
                })  {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("Cookie", sessionCookie);
                        params.put("userId", userId);
                        return params;
                    }
                };
        requestQueue.add(request);
    }



    public void get(final String userId, final String sessionCookie, String path, final MyListener<String> listener)
    {
        String absoluteUrl = PREFIX_URL + path;

        final StringRequest request = new StringRequest(Request.Method.GET, absoluteUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG + ": ", "PostRequest Response : " + response);
                    if (null != response)
                        listener.getResult(response);
                }
            },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        if (null != error.networkResponse)
                        {
                            Log.d(TAG + ": ", "Error Response code: " + error.networkResponse.statusCode);
                            listener.getResult(null);
                        }
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("Cookie", sessionCookie);
                        params.put("userId", userId);
                        return params;
                    }
                };
        requestQueue.add(request);
    }


}
